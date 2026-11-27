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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.CumulativeIncomeFromIncomePosting;
import org.apache.fineract.portfolio.loanaccount.data.TransactionPortionsForForeclosure;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanAccrualsProcessingServiceImpl implements LoanAccrualsProcessingService {

    private static final Set<LoanTransactionType> ACCRUAL_TYPES = Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT);

    private final ExternalIdFactory externalIdFactory;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanBalanceService loanBalanceService;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanAccrualTransactionService loanAccrualTransactionService;
    private final LoanPeriodicAccrualService loanPeriodicAccrualService;
    private final LoanAccrualReprocessingService loanAccrualReprocessingService;
    private final LoanIncomePostingService loanIncomePostingService;

    /**
     * method calculates accruals for loan on loan closure
     */
    @Override
    public void processAccrualsOnLoanClosure(@NonNull final Loan loan, final boolean addJournal) {
        // check and process accruals for loan WITHOUT interest recalculation details and compounding posted as income
        final boolean realTimeChargeAccrual = loanAccrualTransactionService.isRealTimeChargeAccrual(loan);
        loanPeriodicAccrualService.addAccruals(
                loan, loan.getLastLoanRepaymentScheduleInstallment().getDueDate(), false, true, addJournal, loanAccrualTransactionService
                        .resolveChargeOnDueDate(loanAccrualTransactionService.isChargeOnDueDate(), realTimeChargeAccrual),
                realTimeChargeAccrual);
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            return;
        }
        // check and process accruals for loan WITH interest recalculation details and compounding posted as income
        processIncomeAndAccrualTransactionOnLoanClosure(loan);
    }

    /**
     * method calculates accruals for loan on loan fore closure
     */
    @Override
    public void processAccrualsOnLoanForeClosure(@NonNull Loan loan, @NonNull LocalDate foreClosureDate,
            @NonNull List<LoanTransaction> newAccrualTransactions) {
        // TODO implement progressive accrual case
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && (loan.getAccruedTill() == null || !DateUtils.isEqual(foreClosureDate, loan.getAccruedTill()))) {
            final LoanRepaymentScheduleInstallment foreCloseDetail = loanBalanceService.fetchLoanForeclosureDetail(loan, foreClosureDate);
            MonetaryCurrency currency = loan.getCurrency();
            loanAccrualTransactionService.reverseTransactionsAfter(loan, ACCRUAL_TYPES, foreClosureDate, false);

            final Map<String, Money> incomeDetails = determineReceivableIncomeForeClosure(loan, foreClosureDate);

            final Money interestPortion = foreCloseDetail.getInterestCharged(currency).minus(incomeDetails.get(Loan.INTEREST));
            final Money feePortion = foreCloseDetail.getFeeChargesCharged(currency).minus(incomeDetails.get(Loan.FEE));
            final Money penaltyPortion = foreCloseDetail.getPenaltyChargesCharged(currency).minus(incomeDetails.get(Loan.PENALTIES));
            final Money total = interestPortion.plus(feePortion).plus(penaltyPortion);

            if (total.isGreaterThanZero()) {
                createAccrualTransactionAndUpdateChargesPaidBy(loan, foreClosureDate, newAccrualTransactions, currency, interestPortion,
                        feePortion, penaltyPortion, total);
            }
        }
    }

    // LoanClosure

    private void processIncomeAndAccrualTransactionOnLoanClosure(Loan loan) {
        // TODO analyze progressive accrual case
        if (loan.getLoanInterestRecalculationDetails() != null
                && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction()
                && loan.getStatus().isClosedObligationsMet() && !loan.isNpa() && !loan.isChargedOff()) {

            LocalDate closedDate = loan.getClosedOnDate();
            loanAccrualTransactionService.reverseTransactionsOnOrAfter(loan, Set.of(INCOME_POSTING, ACCRUAL, ACCRUAL_ADJUSTMENT),
                    closedDate);

            final Map<String, BigDecimal> cumulativeIncomeFromInstallments = new HashMap<>();
            determineCumulativeIncomeFromInstallments(loan, cumulativeIncomeFromInstallments);

            final CumulativeIncomeFromIncomePosting cumulativeIncomeFromIncomePosting = loanTransactionRepository
                    .findCumulativeIncomeByLoanAndType(loan);

            final BigDecimal interestToPost = cumulativeIncomeFromInstallments.get(Loan.INTEREST)
                    .subtract(cumulativeIncomeFromIncomePosting.interestAmount());
            final BigDecimal feeToPost = cumulativeIncomeFromInstallments.get(Loan.FEE)
                    .subtract(cumulativeIncomeFromIncomePosting.feeAmount());
            final BigDecimal penaltyToPost = cumulativeIncomeFromInstallments.get(Loan.PENALTY)
                    .subtract(cumulativeIncomeFromIncomePosting.penaltyAmount());
            final BigDecimal amountToPost = interestToPost.add(feeToPost).add(penaltyToPost);

            createIncomePostingAndAccrualTransactionOnLoanClosure(loan, closedDate, interestToPost, feeToPost, penaltyToPost, amountToPost);
        }
        loanBalanceService.updateLoanOutstandingBalances(loan);
    }

    private void determineCumulativeIncomeFromInstallments(final Loan loan,
            final Map<String, BigDecimal> cumulativeIncomeFromInstallments) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalty = BigDecimal.ZERO;
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (LoanRepaymentScheduleInstallment installment : installments) {
            interest = interest.add(installment.getInterestCharged(loan.getCurrency()).getAmount());
            fee = fee.add(installment.getFeeChargesCharged(loan.getCurrency()).getAmount());
            penalty = penalty.add(installment.getPenaltyChargesCharged(loan.getCurrency()).getAmount());
        }
        cumulativeIncomeFromInstallments.put(Loan.INTEREST, interest);
        cumulativeIncomeFromInstallments.put(Loan.FEE, fee);
        cumulativeIncomeFromInstallments.put(Loan.PENALTY, penalty);
    }

    private void createIncomePostingAndAccrualTransactionOnLoanClosure(Loan loan, LocalDate closedDate, BigDecimal interestToPost,
            BigDecimal feeToPost, BigDecimal penaltyToPost, BigDecimal amountToPost) {
        ExternalId externalId = ExternalId.empty();
        boolean isExternalIdAutoGenerationEnabled = configurationDomainService.isExternalIdAutoGenerationEnabled();

        if (isExternalIdAutoGenerationEnabled) {
            externalId = ExternalId.generate();
        }
        LoanTransaction finalIncomeTransaction = LoanTransaction.incomePosting(loan, loan.getOffice(), closedDate, amountToPost,
                interestToPost, feeToPost, penaltyToPost, externalId);
        LoanTransaction savedFinalIncomeTransaction = loanTransactionRepository.save(finalIncomeTransaction);
        loan.addLoanTransaction(savedFinalIncomeTransaction);
        journalEntryPoster.postJournalEntriesForLoanTransaction(savedFinalIncomeTransaction, false, false);

        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            final LocalDate lastAccruedDate = loanTransactionRepository
                    .findLastNonReversedTransactionDateByLoanAndTypes(loan, ACCRUAL_TYPES) //
                    .orElse(loan.getDisbursementDate());

            HashMap<String, Object> feeDetails = new HashMap<>();
            loanAccrualTransactionService.determineFeeDetails(loan, lastAccruedDate, closedDate, feeDetails);
            if (isExternalIdAutoGenerationEnabled) {
                externalId = ExternalId.generate();
            }
            LoanTransaction finalAccrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), closedDate, amountToPost,
                    interestToPost, feeToPost, penaltyToPost, externalId);
            loanAccrualTransactionService.updateLoanChargesPaidBy(loan, finalAccrual, feeDetails, null);
            LoanTransaction savedFinalAccrual = loanTransactionRepository.save(finalAccrual);
            loan.addLoanTransaction(savedFinalAccrual);
            journalEntryPoster.postJournalEntriesForLoanTransaction(savedFinalAccrual, false, false);
        }
    }

    private Map<String, Money> determineReceivableIncomeForeClosure(final Loan loan, final LocalDate tillDate) {
        MonetaryCurrency currency = loan.getCurrency();
        Money receivableInterest = Money.zero(currency);
        Money receivableFee = Money.zero(currency);
        Money receivablePenalty = Money.zero(currency);

        final List<TransactionPortionsForForeclosure> transactionPortions = loanTransactionRepository
                .findTransactionDataForForeclosureIncome(loan, tillDate);

        for (TransactionPortionsForForeclosure transactionPortion : transactionPortions) {
            LoanTransactionType transactionType = transactionPortion.getTransactionType();
            BigDecimal interestPortion = transactionPortion.getInterestPortion();
            BigDecimal feePortion = transactionPortion.getFeeChargesPortion();
            BigDecimal penaltyPortion = transactionPortion.getPenaltyChargesPortion();

            if (transactionType.isAccrual()) {
                receivableInterest = receivableInterest.plus(Money.of(currency, interestPortion));
                receivableFee = receivableFee.plus(Money.of(currency, feePortion));
                receivablePenalty = receivablePenalty.plus(Money.of(currency, penaltyPortion));
            } else if (transactionType.isRepayment() || transactionType.isChargePayment() || transactionType.isAccrualAdjustment()) {
                receivableInterest = receivableInterest.minus(Money.of(currency, interestPortion));
                receivableFee = receivableFee.minus(Money.of(currency, feePortion));
                receivablePenalty = receivablePenalty.minus(Money.of(currency, penaltyPortion));
            }

            if (receivableInterest.isLessThanZero()) {
                receivableInterest = receivableInterest.zero();
            }
            if (receivableFee.isLessThanZero()) {
                receivableFee = receivableFee.zero();
            }
            if (receivablePenalty.isLessThanZero()) {
                receivablePenalty = receivablePenalty.zero();
            }
        }

        return Map.of(Loan.INTEREST, receivableInterest, Loan.FEE, receivableFee, Loan.PENALTIES, receivablePenalty);
    }

    private void createAccrualTransactionAndUpdateChargesPaidBy(Loan loan, LocalDate foreClosureDate,
            List<LoanTransaction> newAccrualTransactions, MonetaryCurrency currency, Money interestPortion, Money feePortion,
            Money penaltyPortion, Money total) {
        ExternalId accrualExternalId = externalIdFactory.create();
        LoanTransaction accrualTransaction = LoanTransaction.accrueTransaction(loan, loan.getOffice(), foreClosureDate, total.getAmount(),
                interestPortion.getAmount(), feePortion.getAmount(), penaltyPortion.getAmount(), accrualExternalId);
        LocalDate fromDate = loan.getDisbursementDate();
        if (loan.getAccruedTill() != null) {
            fromDate = loan.getAccruedTill();
        }
        newAccrualTransactions.add(accrualTransaction);
        Set<LoanChargePaidBy> accrualCharges = accrualTransaction.getLoanChargesPaid();
        for (LoanCharge loanCharge : loan.getActiveCharges()) {
            boolean isDue = loanCharge.isDueInPeriod(fromDate, foreClosureDate, DateUtils.isEqual(fromDate, loan.getDisbursementDate()));
            if (loanCharge.isActive() && !loanCharge.isPaid() && (isDue || loanCharge.isInstalmentFee())) {
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                        loanCharge.getAmountOutstanding(currency).getAmount(), null);
                accrualCharges.add(loanChargePaidBy);
                loanCharge.getLoanChargePaidBySet().add(loanChargePaidBy);
            }
        }
    }

    @Override
    public void postChargeAccrual(@NonNull final LoanTransaction chargeAccrual) {
        loanAccrualTransactionService.postChargeAccrual(chargeAccrual);
    }

    @Override
    public void addPeriodicAccruals(@NonNull LocalDate tillDate) throws JobExecutionException {
        loanPeriodicAccrualService.addPeriodicAccruals(tillDate);
    }

    @Override
    public void addPeriodicAccruals(@NonNull final LocalDate tillDate, @NonNull final Loan loan) {
        loanPeriodicAccrualService.addPeriodicAccruals(tillDate, loan);
    }

    @Override
    public void addAccruals(@NonNull LocalDate tillDate) throws JobExecutionException {
        loanPeriodicAccrualService.addAccruals(tillDate);
    }

    @Override
    public void processAccrualsOnInterestRecalculation(@NonNull Loan loan, boolean isInterestRecalculationEnabled, boolean addJournal) {
        loanPeriodicAccrualService.processAccrualsOnInterestRecalculation(loan, isInterestRecalculationEnabled, addJournal);
    }

    @Override
    public boolean isRealTimeChargeAccrualApplicable(@NonNull final Loan loan) {
        return loanPeriodicAccrualService.isRealTimeChargeAccrualApplicable(loan);
    }

    @Override
    public void processRealTimeChargeAccruals(@NonNull final Loan loan, @NonNull final List<LoanCharge> loanCharges) {
        loanPeriodicAccrualService.processRealTimeChargeAccruals(loan, loanCharges);
    }

    @Override
    public void reprocessExistingAccruals(@NonNull final Loan loan, final boolean addEvent) {
        loanAccrualReprocessingService.reprocessExistingAccruals(loan, addEvent);
    }

    @Override
    public void addIncomePostingAndAccruals(Long loanId) throws LoanNotFoundException {
        loanIncomePostingService.addIncomePostingAndAccruals(loanId);
    }

    @Override
    public void processIncomePostingAndAccruals(@NonNull final Loan loan, final boolean addEvent) {
        loanIncomePostingService.processIncomePostingAndAccruals(loan, addEvent);
    }
}
