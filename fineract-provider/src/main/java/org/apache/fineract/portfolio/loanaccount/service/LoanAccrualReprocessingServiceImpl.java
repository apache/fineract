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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isInPeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.AccrualBalances;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanAccrualReprocessingServiceImpl implements LoanAccrualReprocessingService {

    private static final Set<LoanTransactionType> ACCRUAL_TYPES = Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT);

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargeService loanChargeService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanAccrualTransactionService loanAccrualTransactionService;

    /**
     * method updates accrual derived fields on installments and reverse the unprocessed transactions for loan
     * reschedule
     */
    @Override
    public void reprocessExistingAccruals(@NonNull final Loan loan, final boolean addEvent) {
        if (loan.getId() == null) {
            // a not-yet-persisted loan cannot have accrual transactions; also, EclipseLink 5 types a
            // null entity parameter as VARCHAR, which PostgreSQL rejects against the bigint FK column
            return;
        }
        List<LoanTransaction> accrualTransactions = retrieveListOfAccrualTransactions(loan);
        if (!accrualTransactions.isEmpty()) {
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                reprocessPeriodicAccruals(loan, accrualTransactions, addEvent);
            } else if (configurationDomainService.isAllowCashAndNonCashAccrual() ? !loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                    : loan.isUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                reprocessNonPeriodicAccruals(loan, accrualTransactions, addEvent);
            }
        }
    }

    private void reprocessPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions, final boolean addEvent) {
        if (loan.isChargedOff()) {
            return;
        }
        final boolean realTimeChargeAccrual = loanAccrualTransactionService.isRealTimeChargeAccrual(loan);
        final boolean isChargeOnDueDate = loanAccrualTransactionService
                .resolveChargeOnDueDate(loanAccrualTransactionService.isChargeOnDueDate(), realTimeChargeAccrual);
        loanAccrualTransactionService.ensureAccrualTransactionMappings(loan, isChargeOnDueDate);
        LoanRepaymentScheduleInstallment lastInstallment = loan.getLastLoanRepaymentScheduleInstallment();
        LocalDate lastDueDate = lastInstallment.getDueDate();
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            AccrualBalances accrualBalances = new AccrualBalances();
            accrualTransactions.forEach(lt -> {
                switch (lt.getTypeOf()) {
                    case ACCRUAL -> {
                        accrualBalances.setFeePortion(MathUtil.add(accrualBalances.getFeePortion(), lt.getFeeChargesPortion()));
                        accrualBalances.setPenaltyPortion(MathUtil.add(accrualBalances.getPenaltyPortion(), lt.getPenaltyChargesPortion()));
                        accrualBalances.setInterestPortion(MathUtil.add(accrualBalances.getInterestPortion(), lt.getInterestPortion()));
                    }
                    case ACCRUAL_ADJUSTMENT -> {
                        accrualBalances.setFeePortion(MathUtil.subtract(accrualBalances.getFeePortion(), lt.getFeeChargesPortion()));
                        accrualBalances
                                .setPenaltyPortion(MathUtil.subtract(accrualBalances.getPenaltyPortion(), lt.getPenaltyChargesPortion()));
                        accrualBalances
                                .setInterestPortion(MathUtil.subtract(accrualBalances.getInterestPortion(), lt.getInterestPortion()));
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + lt.getTypeOf());
                }
            });
            for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
                BigDecimal maximumAccruableInterest = MathUtil.nullToZero(installment.getInterestCharged());
                BigDecimal maximumAccruableFee = MathUtil.nullToZero(installment.getFeeChargesCharged());
                BigDecimal maximumAccruablePenalty = MathUtil.nullToZero(installment.getPenaltyCharges());

                if (MathUtil.isLessThanOrEqualTo(maximumAccruableInterest, accrualBalances.getInterestPortion())) {
                    installment.setInterestAccrued(maximumAccruableInterest);
                    accrualBalances.setInterestPortion(accrualBalances.getInterestPortion().subtract(maximumAccruableInterest));
                } else {
                    installment.setInterestAccrued(accrualBalances.getInterestPortion());
                    accrualBalances.setInterestPortion(BigDecimal.ZERO);
                }

                if (realTimeChargeAccrual) {
                    continue;
                }

                if (MathUtil.isLessThanOrEqualTo(maximumAccruableFee, accrualBalances.getFeePortion())) {
                    installment.setFeeAccrued(maximumAccruableFee);
                    accrualBalances.setFeePortion(accrualBalances.getFeePortion().subtract(maximumAccruableFee));
                } else {
                    installment.setFeeAccrued(accrualBalances.getFeePortion());
                    accrualBalances.setFeePortion(BigDecimal.ZERO);
                }

                if (MathUtil.isLessThanOrEqualTo(maximumAccruablePenalty, accrualBalances.getPenaltyPortion())) {
                    installment.setPenaltyAccrued(maximumAccruablePenalty);
                    accrualBalances.setPenaltyPortion(accrualBalances.getPenaltyPortion().subtract(maximumAccruablePenalty));
                } else {
                    installment.setPenaltyAccrued(accrualBalances.getPenaltyPortion());
                    accrualBalances.setPenaltyPortion(BigDecimal.ZERO);
                }
            }
            if (realTimeChargeAccrual) {
                distributeChargeAccrualsByPaidBy(loan.getRepaymentScheduleInstallments(), accrualTransactions, accrualBalances);
            }
        } else {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            boolean isBasedOnSubmittedOnDate = !isChargeOnDueDate;
            for (LoanRepaymentScheduleInstallment installment : installments) {
                checkAndUpdateAccrualsForInstallment(loan, accrualTransactions, installments, isBasedOnSubmittedOnDate, installment,
                        addEvent);
            }
        }
        // For post-due-date accruals: create ACCRUAL_ADJUSTMENT instead of reversing. This path does NOT recreate
        // the accrual, so reversal would permanently de-recognize income. ACCRUAL_ADJUSTMENT preserves the audit trail.
        loanAccrualTransactionService.adjustAccrualsAfter(loan, lastDueDate, addEvent);
    }

    private void distributeChargeAccrualsByPaidBy(final List<LoanRepaymentScheduleInstallment> installments,
            final List<LoanTransaction> accrualTransactions, final AccrualBalances totals) {
        final Map<Integer, AccrualBalances> mapped = sumChargeAccrualsByInstallment(accrualTransactions);
        final AccrualBalances none = new AccrualBalances();
        final BigDecimal[] fees = distributeByInstallment(installments, totals.getFeePortion(),
                i -> mapped.getOrDefault(i.getInstallmentNumber(), none).getFeePortion(),
                LoanRepaymentScheduleInstallment::getFeeChargesCharged);
        final BigDecimal[] penalties = distributeByInstallment(installments, totals.getPenaltyPortion(),
                i -> mapped.getOrDefault(i.getInstallmentNumber(), none).getPenaltyPortion(),
                LoanRepaymentScheduleInstallment::getPenaltyCharges);
        for (int i = 0; i < installments.size(); i++) {
            installments.get(i).setFeeAccrued(fees[i]);
            installments.get(i).setPenaltyAccrued(penalties[i]);
        }
    }

    private static BigDecimal[] distributeByInstallment(final List<LoanRepaymentScheduleInstallment> installments, final BigDecimal total,
            final Function<LoanRepaymentScheduleInstallment, BigDecimal> mappedAmount,
            final Function<LoanRepaymentScheduleInstallment, BigDecimal> chargedAmount) {
        final BigDecimal[] accrued = new BigDecimal[installments.size()];
        BigDecimal residue = MathUtil.nullToZero(total);
        for (int i = 0; i < accrued.length; i++) {
            accrued[i] = MathUtil.min(MathUtil.negativeToZero(mappedAmount.apply(installments.get(i))),
                    MathUtil.nullToZero(chargedAmount.apply(installments.get(i))), true);
            residue = residue.subtract(accrued[i]);
        }
        for (int i = 0; i < accrued.length && residue.signum() > 0; i++) {
            final BigDecimal added = MathUtil.min(residue,
                    MathUtil.subtractToZero(MathUtil.nullToZero(chargedAmount.apply(installments.get(i))), accrued[i]), true);
            accrued[i] = accrued[i].add(added);
            residue = residue.subtract(added);
        }
        // Generic accrual adjustments have no paid-by mapping, so the residue can be negative; shave it from the latest
        // installments
        for (int i = accrued.length - 1; i >= 0 && residue.signum() < 0; i--) {
            final BigDecimal removed = MathUtil.min(residue.negate(), accrued[i], true);
            accrued[i] = accrued[i].subtract(removed);
            residue = residue.add(removed);
        }
        return accrued;
    }

    private Map<Integer, AccrualBalances> sumChargeAccrualsByInstallment(final List<LoanTransaction> accrualTransactions) {
        final Map<Integer, AccrualBalances> balances = new HashMap<>();
        for (LoanTransaction transaction : accrualTransactions) {
            final boolean adjustment = transaction.getTypeOf() == ACCRUAL_ADJUSTMENT;
            for (LoanChargePaidBy paidBy : transaction.getLoanChargesPaid()) {
                if (paidBy.getInstallmentNumber() == null) {
                    continue;
                }
                final BigDecimal amount = adjustment ? MathUtil.negate(paidBy.getAmount()) : paidBy.getAmount();
                final AccrualBalances installmentBalances = balances.computeIfAbsent(paidBy.getInstallmentNumber(),
                        k -> new AccrualBalances());
                if (paidBy.getLoanCharge().isPenaltyCharge()) {
                    installmentBalances.setPenaltyPortion(MathUtil.add(installmentBalances.getPenaltyPortion(), amount));
                } else {
                    installmentBalances.setFeePortion(MathUtil.add(installmentBalances.getFeePortion(), amount));
                }
            }
        }
        return balances;
    }

    private void reprocessNonPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions, final boolean addEvent) {
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            return;
        }
        final Money interestApplied = Money.of(loan.getCurrency(), loan.getSummary().getTotalInterestCharged());
        ExternalId externalId = ExternalId.empty();
        boolean isExternalIdAutoGenerationEnabled = configurationDomainService.isExternalIdAutoGenerationEnabled();

        for (LoanTransaction accrualTransaction : accrualTransactions) {
            if (accrualTransaction.getInterestPortion(loan.getCurrency()).isGreaterThanZero()) {
                if (accrualTransaction.getInterestPortion(loan.getCurrency()).isNotEqualTo(interestApplied)) {
                    loanAccrualTransactionService.reverseTransaction(accrualTransaction, addEvent);
                    if (isExternalIdAutoGenerationEnabled) {
                        externalId = ExternalId.generate();
                    }
                    final LoanTransaction interestAccrualTransaction = LoanTransaction.accrueInterest(loan.getOffice(), loan,
                            interestApplied, loan.getDisbursementDate(), externalId);
                    LoanTransaction savedInterestAccrualTransaction = loanTransactionRepository.saveAndFlush(interestAccrualTransaction);
                    loan.addLoanTransaction(savedInterestAccrualTransaction);
                    if (addEvent) {
                        journalEntryPoster.postJournalEntriesForLoanTransaction(savedInterestAccrualTransaction, false, false);
                        final LoanTransactionBusinessEvent businessEvent = new LoanAccrualTransactionCreatedBusinessEvent(
                                savedInterestAccrualTransaction);
                        businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                    }
                }
            } else {
                Set<LoanChargePaidBy> chargePaidBies = accrualTransaction.getLoanChargesPaid();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    Money chargeAmount = loanCharge.getAmount(loan.getCurrency());
                    if (chargeAmount.isNotEqualTo(accrualTransaction.getAmount(loan.getCurrency()))) {
                        loanAccrualTransactionService.reverseTransaction(accrualTransaction, addEvent);
                        final LoanTransaction applyLoanChargeTransaction = loanChargeService.handleChargeAppliedTransaction(loan,
                                loanCharge, accrualTransaction.getTransactionDate());
                        if (applyLoanChargeTransaction != null) {
                            LoanTransaction savedApplyLoanChargeTransaction = loanTransactionRepository.save(applyLoanChargeTransaction);
                            if (addEvent) {
                                journalEntryPoster.postJournalEntriesForLoanTransaction(savedApplyLoanChargeTransaction, false, false);
                                final LoanTransactionBusinessEvent businessEvent = new LoanAccrualTransactionCreatedBusinessEvent(
                                        savedApplyLoanChargeTransaction);
                                businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkAndUpdateAccrualsForInstallment(Loan loan, List<LoanTransaction> accrualTransactions,
            List<LoanRepaymentScheduleInstallment> installments, boolean isBasedOnSubmittedOnDate,
            LoanRepaymentScheduleInstallment installment, final boolean addEvent) {
        MonetaryCurrency currency = loan.getCurrency();
        Money zero = Money.zero(currency);
        Money interest = zero;
        Money fee = zero;
        Money penalty = zero;
        for (LoanTransaction accrualTransaction : accrualTransactions) {
            LocalDate transactionDateForRange = getDateForRangeCalculation(accrualTransaction, isBasedOnSubmittedOnDate);
            boolean isInPeriod = isInPeriod(transactionDateForRange, installment, installments);
            if (isInPeriod) {
                interest = MathUtil.plus(interest, accrualTransaction.getInterestPortion(currency));
                fee = MathUtil.plus(fee, accrualTransaction.getFeeChargesPortion(currency));
                penalty = MathUtil.plus(penalty, accrualTransaction.getPenaltyChargesPortion(currency));
                if (hasIncomeAmountChangedForInstallment(loan, installment, interest, fee, penalty, accrualTransaction)) {
                    interest = interest.minus(accrualTransaction.getInterestPortion(currency));
                    fee = fee.minus(accrualTransaction.getFeeChargesPortion(currency));
                    penalty = penalty.minus(accrualTransaction.getPenaltyChargesPortion(currency));
                    loanAccrualTransactionService.reverseTransaction(accrualTransaction, addEvent);
                }
            }
        }
        installment.updateAccrualPortion(interest, fee, penalty);
    }

    private boolean hasIncomeAmountChangedForInstallment(Loan loan, LoanRepaymentScheduleInstallment installment, Money interest, Money fee,
            Money penalty, LoanTransaction loanTransaction) {
        // if installment income amount is changed or if loan is interest bearing and interest income not accrued
        return installment.getFeeChargesCharged(loan.getCurrency()).isLessThan(fee)
                || installment.getInterestCharged(loan.getCurrency()).isLessThan(interest)
                || installment.getPenaltyChargesCharged(loan.getCurrency()).isLessThan(penalty)
                || (loan.isInterestBearing() && DateUtils.isEqual(loan.getAccruedTill(), loanTransaction.getTransactionDate())
                        && !DateUtils.isEqual(loan.getAccruedTill(), installment.getDueDate()));
    }

    private LocalDate getDateForRangeCalculation(LoanTransaction loanTransaction, boolean isChargeAccrualBasedOnSubmittedOnDate) {
        // check config for charge accrual date and return date
        return isChargeAccrualBasedOnSubmittedOnDate && !loanTransaction.getLoanChargesPaid().isEmpty()
                ? loanTransaction.getLoanChargesPaid().stream().findFirst().get().getLoanCharge().getEffectiveDueDate()
                : loanTransaction.getTransactionDate();
    }

    private List<LoanTransaction> retrieveListOfAccrualTransactions(final Loan loan) {
        return loanTransactionRepository.findNonReversedByLoanAndTypes(loan, ACCRUAL_TYPES).stream()
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }
}
