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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INCOME_POSTING;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanIncomePostingServiceImpl implements LoanIncomePostingService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanBalanceService loanBalanceService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanAccrualTransactionService loanAccrualTransactionService;

    @Transactional
    @Override
    public void addIncomePostingAndAccruals(Long loanId) throws LoanNotFoundException {
        if (loanId == null) {
            return;
        }
        Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            return;
        }
        processIncomePostingAndAccruals(loan, true);
        this.loanRepositoryWrapper.saveAndFlush(loan);
    }

    /**
     * method calculates accruals for loan with interest recalculation and compounding to be posted as income
     */
    @Override
    public void processIncomePostingAndAccruals(@NonNull final Loan loan, final boolean addEvent) {
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            return;
        }
        final LoanInterestRecalculationDetails recalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (recalculationDetails == null || !recalculationDetails.isCompoundingToBePostedAsTransaction()) {
            return;
        }
        LocalDate lastCompoundingDate = loan.getDisbursementDate();
        final List<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = extractInterestRecalculationAdditionalDetails(loan);
        for (LoanInterestRecalcualtionAdditionalDetails compoundingDetail : compoundingDetails) {
            if (!DateUtils.isBeforeBusinessDate(compoundingDetail.getEffectiveDate())) {
                break;
            }

            addUpdateIncomeAndAccrualTransaction(loan, compoundingDetail, lastCompoundingDate, addEvent);
            lastCompoundingDate = compoundingDetail.getEffectiveDate();
        }
        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        final LoanRepaymentScheduleInstallment lastInstallment = LoanRepaymentScheduleInstallment
                .getLastNonDownPaymentInstallment(installments);

        loanAccrualTransactionService.reverseTransactionsAfter(loan, Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT, INCOME_POSTING),
                lastInstallment.getDueDate(), addEvent);
    }

    private List<LoanInterestRecalcualtionAdditionalDetails> extractInterestRecalculationAdditionalDetails(Loan loan) {
        List<LoanInterestRecalcualtionAdditionalDetails> retDetails = new ArrayList<>();
        List<LoanRepaymentScheduleInstallment> repaymentSchedule = loan.getRepaymentScheduleInstallments();
        if (null != repaymentSchedule) {
            for (LoanRepaymentScheduleInstallment installment : repaymentSchedule) {
                if (null != installment.getLoanCompoundingDetails()) {
                    retDetails.addAll(installment.getLoanCompoundingDetails());
                }
            }
        }
        retDetails.sort(Comparator.comparing(LoanInterestRecalcualtionAdditionalDetails::getEffectiveDate));
        return retDetails;
    }

    private void addUpdateIncomeAndAccrualTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            LocalDate lastCompoundingDate, boolean addEvent) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalties = BigDecimal.ZERO;
        HashMap<String, Object> feeDetails = new HashMap<>();

        if (loan.getLoanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.INTEREST)) {
            interest = compoundingDetail.getAmount();
        } else if (loan.getLoanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.FEE)) {
            loanAccrualTransactionService.determineFeeDetails(loan, lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
            fee = (BigDecimal) feeDetails.get(Loan.FEE);
            penalties = (BigDecimal) feeDetails.get(Loan.PENALTIES);
        } else if (loan.getLoanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE)) {
            loanAccrualTransactionService.determineFeeDetails(loan, lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
            fee = (BigDecimal) feeDetails.get(Loan.FEE);
            penalties = (BigDecimal) feeDetails.get(Loan.PENALTIES);
            interest = compoundingDetail.getAmount().subtract(fee).subtract(penalties);
        }

        ExternalId externalId = ExternalId.empty();
        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        createUpdateIncomePostingTransaction(loan, compoundingDetail, interest, fee, penalties, externalId);
        createUpdateAccrualTransaction(loan, compoundingDetail, interest, fee, penalties, feeDetails, externalId, addEvent);
        loanBalanceService.updateLoanOutstandingBalances(loan);
    }

    private void createUpdateIncomePostingTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            BigDecimal interest, BigDecimal fee, BigDecimal penalties, ExternalId externalId) {
        final Optional<LoanTransaction> incomeTransaction = loanTransactionRepository.findNonReversedByLoanAndTypesAndDate(loan,
                Set.of(INCOME_POSTING), compoundingDetail.getEffectiveDate());
        if (incomeTransaction.isEmpty()) {
            final LoanTransaction transaction = LoanTransaction.incomePosting(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            final LoanTransaction savedTransaction = loanTransactionRepository.save(transaction);
            loan.addLoanTransaction(savedTransaction);
            journalEntryPoster.postJournalEntriesForLoanTransaction(savedTransaction, false, false);
        } else if (incomeTransaction.get().getAmount(loan.getCurrency()).getAmount().compareTo(compoundingDetail.getAmount()) != 0) {
            incomeTransaction.get().reverse();
            journalEntryPoster.postJournalEntriesForLoanTransaction(incomeTransaction.get(), false, false);
            final LoanTransaction transaction = LoanTransaction.incomePosting(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            final LoanTransaction savedTransaction = loanTransactionRepository.save(transaction);
            loan.addLoanTransaction(savedTransaction);
            journalEntryPoster.postJournalEntriesForLoanTransaction(savedTransaction, false, false);
        }
    }

    private void createUpdateAccrualTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            BigDecimal interest, BigDecimal fee, BigDecimal penalties, HashMap<String, Object> feeDetails, ExternalId externalId,
            boolean addEvent) {
        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            final Optional<LoanTransaction> accrualTransaction = loanTransactionRepository.findNonReversedByLoanAndTypesAndDate(loan,
                    Set.of(LoanTransactionType.ACCRUAL, LoanTransactionType.ACCRUAL_ADJUSTMENT), compoundingDetail.getEffectiveDate());

            if (accrualTransaction.isEmpty() || !MathUtil.isEqualTo(accrualTransaction.get().getAmount(), compoundingDetail.getAmount())) {
                accrualTransaction.ifPresent(accrualTrans -> loanAccrualTransactionService.reverseTransaction(accrualTrans, addEvent));
                LoanTransaction accrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                loanAccrualTransactionService.updateLoanChargesPaidBy(loan, accrual, feeDetails, null);
                LoanTransaction savedAccrual = loanTransactionRepository.save(accrual);
                loan.addLoanTransaction(savedAccrual);
                if (addEvent) {
                    journalEntryPoster.postJournalEntriesForLoanTransaction(savedAccrual, false, false);
                    final LoanTransactionBusinessEvent businessEvent = new LoanAccrualTransactionCreatedBusinessEvent(savedAccrual);
                    businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                }
            }
        }
    }
}
