/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.service;

import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isInPeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrualAdjustment;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ChargeAccrualDateConfig;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidByRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanAccrualTransactionServiceImpl implements LoanAccrualTransactionService {

    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargePaidByRepository loanChargePaidByRepository;
    private final LoanJournalEntryPoster journalEntryPoster;

    @Override
    public void postChargeAccrual(@NonNull final LoanTransaction chargeAccrual) {
        final LoanTransaction saved = loanTransactionRepository.saveAndFlush(chargeAccrual);
        journalEntryPoster.postJournalEntriesForLoanTransaction(saved, false, false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccrualTransactionCreatedBusinessEvent(saved));
    }

    @Override
    public void ensureAccrualTransactionMappings(final Loan loan, final boolean chargeOnDueDate) {
        final List<LoanChargePaidBy> entriesToProcess = loanChargePaidByRepository.findChargePaidByMappingsWithoutInstallmentNumber(loan);

        if (entriesToProcess.isEmpty()) {
            return;
        }

        final int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        for (LoanChargePaidBy paidBy : entriesToProcess) {
            final LoanCharge loanCharge = paidBy.getLoanCharge();
            final LocalDate chargeDate = (chargeOnDueDate || loanCharge.isInstalmentFee())
                    ? paidBy.getLoanTransaction().getTransactionDate()
                    : loanCharge.getDueDate();
            final LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(
                    i -> isInPeriod(chargeDate, i, i.getInstallmentNumber().equals(firstInstallmentNumber)));
            if (installment != null) {
                paidBy.setInstallmentNumber(installment.getInstallmentNumber());
            }
        }
    }

    @Override
    public boolean isChargeOnDueDate() {
        return configurationDomainService.getAccrualDateConfigForCharge() != ChargeAccrualDateConfig.SUBMITTED_DATE;
    }

    @Override
    public boolean resolveChargeOnDueDate(final boolean chargeOnDueDateConfigured, final boolean realTimeChargeAccrual) {
        return chargeOnDueDateConfigured && !realTimeChargeAccrual;
    }

    @Override
    public boolean isRealTimeChargeAccrual(final Loan loan) {
        return isProgressiveAccrual(loan) && configurationDomainService.isPlChargeAccrualRealTime();
    }

    @Override
    public boolean isRealTimeChargeAccrual(final Loan loan, final boolean plChargeAccrualRealTime) {
        return isProgressiveAccrual(loan) && plChargeAccrualRealTime;
    }

    @Override
    public void determineFeeDetails(Loan loan, LocalDate fromDate, LocalDate toDate, Map<String, Object> feeDetails) {
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalties = BigDecimal.ZERO;

        List<Integer> installments = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = loan.getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : repaymentSchedule) {
            if (DateUtils.isAfter(loanRepaymentScheduleInstallment.getDueDate(), fromDate)
                    && !DateUtils.isAfter(loanRepaymentScheduleInstallment.getDueDate(), toDate)) {
                installments.add(loanRepaymentScheduleInstallment.getInstallmentNumber());
            }
        }

        List<LoanCharge> loanCharges = new ArrayList<>();
        List<LoanInstallmentCharge> loanInstallmentCharges = new ArrayList<>();
        for (LoanCharge loanCharge : loan.getActiveCharges()) {
            boolean isDue = loanCharge.isDueInPeriod(fromDate, toDate, DateUtils.isEqual(fromDate, loan.getDisbursementDate()));
            if (isDue) {
                if (loanCharge.isPenaltyCharge() && !loanCharge.isInstalmentFee()) {
                    penalties = penalties.add(loanCharge.amount());
                    loanCharges.add(loanCharge);
                } else if (!loanCharge.isInstalmentFee()) {
                    fee = fee.add(loanCharge.amount());
                    loanCharges.add(loanCharge);
                }
            } else if (loanCharge.isInstalmentFee()) {
                for (LoanInstallmentCharge installmentCharge : loanCharge.installmentCharges()) {
                    if (installments.contains(installmentCharge.getRepaymentInstallment().getInstallmentNumber())) {
                        fee = fee.add(installmentCharge.getAmount());
                        loanInstallmentCharges.add(installmentCharge);
                    }
                }
            }
        }

        feeDetails.put(Loan.FEE, fee);
        feeDetails.put(Loan.PENALTIES, penalties);
        feeDetails.put("loanCharges", loanCharges);
        feeDetails.put("loanInstallmentCharges", loanInstallmentCharges);
    }

    @Override
    public void updateLoanChargesPaidBy(Loan loan, LoanTransaction accrual, Map<String, Object> feeDetails,
            LoanRepaymentScheduleInstallment installment) {
        @SuppressWarnings("unchecked")
        List<LoanCharge> loanCharges = (List<LoanCharge>) feeDetails.get("loanCharges");
        @SuppressWarnings("unchecked")
        List<LoanInstallmentCharge> loanInstallmentCharges = (List<LoanInstallmentCharge>) feeDetails.get("loanInstallmentCharges");
        if (loanCharges != null) {
            for (LoanCharge loanCharge : loanCharges) {
                Integer installmentNumber = null == installment ? null : installment.getInstallmentNumber();
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrual, loanCharge,
                        loanCharge.getAmount(loan.getCurrency()).getAmount(), installmentNumber);
                accrual.getLoanChargesPaid().add(loanChargePaidBy);
            }
        }
        if (loanInstallmentCharges != null) {
            for (LoanInstallmentCharge loanInstallmentCharge : loanInstallmentCharges) {
                Integer installmentNumber = null == loanInstallmentCharge.getInstallment() ? null
                        : loanInstallmentCharge.getInstallment().getInstallmentNumber();
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrual, loanInstallmentCharge.getLoanCharge(),
                        loanInstallmentCharge.getAmount(loan.getCurrency()).getAmount(), installmentNumber);
                accrual.getLoanChargesPaid().add(loanChargePaidBy);
            }
        }
    }

    @Override
    public void reverseTransactionsAfter(final Loan loan, final Set<LoanTransactionType> types, final LocalDate effectiveDate,
            final boolean addEvent) {
        loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, types, effectiveDate)
                .forEach(transaction -> reverseTransaction(transaction, addEvent));
    }

    @Override
    public void reverseTransactionsOnOrAfter(final Loan loan, final Set<LoanTransactionType> types, final LocalDate date) {
        loanTransactionRepository.findNonReversedByLoanAndTypesAndOnOrAfterDate(loan, types, date)
                .forEach(transaction -> reverseTransaction(transaction, true));
    }

    /**
     * For post-due-date accruals that are reversed WITHOUT recreation (reprocessPeriodicAccruals line 817): create
     * ACCRUAL_ADJUSTMENT instead of reversing. This preserves the audit trail and allows downstream reconciliation.
     */
    @Override
    public void adjustAccrualsAfter(final Loan loan, final LocalDate lastDueDate, final boolean addEvent) {
        // Only query for ACCRUAL — ACCRUAL_ADJUSTMENT transactions are corrections that must persist
        loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, Set.of(ACCRUAL), lastDueDate).forEach(transaction -> {
            createAccrualAdjustment(loan, transaction, addEvent);
        });
    }

    private void createAccrualAdjustment(final Loan loan, final LoanTransaction accrualTransaction, final boolean addEvent) {
        final BigDecimal interest = accrualTransaction.getInterestPortion();
        final BigDecimal fee = accrualTransaction.getFeeChargesPortion();
        final BigDecimal penalty = accrualTransaction.getPenaltyChargesPortion();
        final BigDecimal amount = MathUtil.add(interest, fee, penalty);
        if (!MathUtil.isGreaterThanZero(amount)) {
            return;
        }
        if (isAlreadyAdjusted(loan, accrualTransaction)) {
            return;
        }
        final LoanTransaction adjustment = accrualAdjustment(loan, loan.getOffice(), accrualTransaction.getTransactionDate(), amount,
                interest, fee, penalty, externalIdFactory.create());
        // saveAndFlush to ensure generated ID is available for journal entry posting
        final LoanTransaction saved = loanTransactionRepository.saveAndFlush(adjustment);
        loan.addLoanTransaction(saved);
        if (addEvent) {
            journalEntryPoster.postJournalEntriesForLoanTransaction(saved, false, false);
            final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(saved);
            businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
        }
    }

    /**
     * Several processing passes can run on the same loan (e.g. CBR undo reprocessing followed by closure accruals), so
     * each same-day accrual with an identical interest/fee/penalty split may be offset by at most one adjustment.
     */
    private boolean isAlreadyAdjusted(final Loan loan, final LoanTransaction accrual) {
        final long matchingAccruals = countNonReversedWithSameSplit(loan, ACCRUAL, accrual);
        final long matchingAdjustments = countNonReversedWithSameSplit(loan, ACCRUAL_ADJUSTMENT, accrual);
        return matchingAdjustments >= Math.max(matchingAccruals, 1L);
    }

    private long countNonReversedWithSameSplit(final Loan loan, final LoanTransactionType type, final LoanTransaction accrual) {
        return loan.getLoanTransactions().stream()
                .filter(t -> t.isNotReversed() && t.getTypeOf() == type
                        && DateUtils.isEqual(t.getTransactionDate(), accrual.getTransactionDate())
                        && MathUtil.isEqualTo(t.getInterestPortion(), accrual.getInterestPortion())
                        && MathUtil.isEqualTo(t.getFeeChargesPortion(), accrual.getFeeChargesPortion())
                        && MathUtil.isEqualTo(t.getPenaltyChargesPortion(), accrual.getPenaltyChargesPortion()))
                .count();
    }

    @Override
    public void reverseTransaction(final LoanTransaction transaction, final boolean addEvent) {
        transaction.reverse();
        if (addEvent) {
            journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
            final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
        }
    }

    @Override
    public boolean isProgressiveAccrual(@NonNull Loan loan) {
        return loan.isProgressiveSchedule();
    }
}
