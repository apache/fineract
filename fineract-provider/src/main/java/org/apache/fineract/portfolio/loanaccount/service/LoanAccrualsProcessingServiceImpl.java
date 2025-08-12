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
import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isAfterPeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isBeforePeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper.isInPeriod;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrualAdjustment;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrueTransaction;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.ACCRUAL_ADJUSTMENT;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType.INCOME_POSTING;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.data.AccrualBalances;
import org.apache.fineract.portfolio.loanaccount.data.AccrualChargeData;
import org.apache.fineract.portfolio.loanaccount.data.AccrualPeriodData;
import org.apache.fineract.portfolio.loanaccount.data.AccrualPeriodsData;
import org.apache.fineract.portfolio.loanaccount.data.CumulativeIncomeFromIncomePosting;
import org.apache.fineract.portfolio.loanaccount.data.TransactionPortionsForForeclosure;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidByRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualsProcessingServiceImpl implements LoanAccrualsProcessingService {

    private static final Set<LoanTransactionType> ACCRUAL_TYPES = Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT);

    private static final String ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE = "submitted-date";
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;

    @Qualifier(TaskExecutorConstant.CONFIGURABLE_TASK_EXECUTOR_BEAN_NAME)
    private final ThreadPoolTaskExecutor taskExecutor;
    private final TransactionTemplate transactionTemplate;
    private final LoanChargeService loanChargeService;
    private final LoanBalanceService loanBalanceService;
    private final LoanChargePaidByRepository loanChargePaidByRepository;
    private final LoanJournalEntryPoster journalEntryPoster;

    /**
     * method adds accrual for batch job "Add Periodic Accrual Transactions" and add accruals api for Loan
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(@NotNull LocalDate tillDate) throws JobExecutionException {
        List<Loan> loans = loanRepositoryWrapper.findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate,
                !isChargeOnDueDate());
        List<Throwable> errors = new ArrayList<>();
        for (Loan loan : loans) {
            try {
                addPeriodicAccruals(tillDate, loan);
            } catch (Exception e) {
                log.error("Failed to add accrual for loan {}", loan.getId(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    /**
     * method adds accrual for Loan COB business step
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(@NotNull final LocalDate tillDate, @NotNull final Loan loan) {
        if (loan.isClosed() || loan.getStatus().isOverpaid()) {
            return;
        }
        final boolean chargeOnDueDate = isChargeOnDueDate();
        addAccruals(loan, tillDate, true, false, true, chargeOnDueDate);
    }

    /**
     * method adds accrual for batch job "Add Accrual Transactions"
     */
    @Override
    @Transactional
    public void addAccruals(@NotNull LocalDate tillDate) throws JobExecutionException {
        final boolean chargeOnDueDate = isChargeOnDueDate();
        List<Loan> loans = loanRepositoryWrapper.findLoansForAddAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, !chargeOnDueDate);

        List<Future<?>> loanTasks = new ArrayList<>();

        FineractContext context = ThreadLocalContextUtil.getContext();

        loans.forEach(outerLoan -> {
            loanTasks.add(taskExecutor.submit(() -> {
                try {
                    ThreadLocalContextUtil.init(context);
                    transactionTemplate.executeWithoutResult(status -> {
                        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(outerLoan.getId());
                        try {
                            log.debug("Adding accruals for loan '{}'", loan.getId());
                            addAccruals(loan, tillDate, false, false, true, chargeOnDueDate);
                            log.debug("Successfully processed loan: '{}' for accrual entries", loan.getId());
                        } catch (Exception e) {
                            log.error("Failed to add accrual for loan {}", loan.getId(), e);
                            throw new RuntimeException("Failed to add accrual for loan " + loan.getId(), e);
                        }
                    });
                } finally {
                    ThreadLocalContextUtil.reset();
                }
            }));
        });

        List<Throwable> errors = new ArrayList<>();
        for (Future<?> task : loanTasks) {
            try {
                task.get();
            } catch (Exception e) {
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    /**
     * method updates accrual derived fields on installments and reverse the unprocessed transactions for loan
     * reschedule
     */
    @Override
    public void reprocessExistingAccruals(@NotNull final Loan loan, final boolean addEvent) {
        List<LoanTransaction> accrualTransactions = retrieveListOfAccrualTransactions(loan);
        if (!accrualTransactions.isEmpty()) {
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                reprocessPeriodicAccruals(loan, accrualTransactions, addEvent);
            } else if (loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                reprocessNonPeriodicAccruals(loan, accrualTransactions, addEvent);
            }
        }
    }

    /**
     * method calculates accruals for loan with interest recalculation on loan schedule when interest is recalculated
     */
    @Override
    @Transactional
    public void processAccrualsOnInterestRecalculation(@NotNull Loan loan, boolean isInterestRecalculationEnabled, boolean addJournal) {
        if (isProgressiveAccrual(loan)) {
            return;
        }
        LocalDate accruedTill = loan.getAccruedTill();
        if (!isInterestRecalculationEnabled || accruedTill == null) {
            return;
        }
        try {
            final boolean chargeOnDueDate = isChargeOnDueDate();
            addAccruals(loan, accruedTill, true, false, addJournal, chargeOnDueDate);
        } catch (Exception e) {
            String globalisationMessageCode = "error.msg.accrual.exception";
            throw new GeneralPlatformDomainRuleException(globalisationMessageCode, e.getMessage(), e);
        }
    }

    @Transactional
    @Override
    public void addIncomePostingAndAccruals(Long loanId) throws LoanNotFoundException {
        if (loanId == null) {
            return;
        }
        Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        if (isProgressiveAccrual(loan)) {
            return;
        }
        processIncomePostingAndAccruals(loan, true);
        this.loanRepositoryWrapper.saveAndFlush(loan);
    }

    /**
     * method calculates accruals for loan with interest recalculation and compounding to be posted as income
     */
    @Override
    public void processIncomePostingAndAccruals(@NotNull final Loan loan, final boolean addEvent) {
        if (isProgressiveAccrual(loan)) {
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

        reverseTransactionsAfter(loan, Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT, INCOME_POSTING), lastInstallment.getDueDate(), addEvent);
    }

    /**
     * method calculates accruals for loan on loan closure
     */
    @Override
    public void processAccrualsOnLoanClosure(@NonNull final Loan loan, final boolean addJournal) {
        // check and process accruals for loan WITHOUT interest recalculation details and compounding posted as income
        final boolean chargeOnDueDate = isChargeOnDueDate();
        addAccruals(loan, loan.getLastLoanRepaymentScheduleInstallment().getDueDate(), false, true, addJournal, chargeOnDueDate);
        if (isProgressiveAccrual(loan)) {
            return;
        }
        // check and process accruals for loan WITH interest recalculation details and compounding posted as income
        processIncomeAndAccrualTransactionOnLoanClosure(loan);
    }

    /**
     * method calculates accruals for loan on loan fore closure
     */
    @Override
    public void processAccrualsOnLoanForeClosure(@NotNull Loan loan, @NotNull LocalDate foreClosureDate,
            @NotNull List<LoanTransaction> newAccrualTransactions) {
        // TODO implement progressive accrual case
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && (loan.getAccruedTill() == null || !DateUtils.isEqual(foreClosureDate, loan.getAccruedTill()))) {
            final LoanRepaymentScheduleInstallment foreCloseDetail = loanBalanceService.fetchLoanForeclosureDetail(loan, foreClosureDate);
            MonetaryCurrency currency = loan.getCurrency();
            reverseTransactionsAfter(loan, ACCRUAL_TYPES, foreClosureDate, false);

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

    // PeriodicAccruals

    private void addAccruals(@NotNull final Loan loan, @NotNull LocalDate tillDate, final boolean periodic, final boolean isFinal,
            final boolean addJournal, final boolean chargeOnDueDate) {
        if ((!isFinal && !loan.isOpen()) || loan.isNpa() || loan.isChargedOff() || !loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                || loan.isContractTermination()) {
            return;
        }

        final LoanInterestRecalculationDetails recalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (recalculationDetails != null && recalculationDetails.isCompoundingToBePostedAsTransaction()) {
            return;
        }

        final LocalDate lastDueDate = loan.getLastLoanRepaymentScheduleInstallment().getDueDate();
        reverseTransactionsAfter(loan, ACCRUAL_TYPES, lastDueDate, addJournal);
        ensureAccrualTransactionMappings(loan, chargeOnDueDate);
        if (DateUtils.isAfter(tillDate, lastDueDate)) {
            tillDate = lastDueDate;
        }

        final boolean progressiveAccrual = isProgressiveAccrual(loan);
        final LocalDate accruedTill = loan.getAccruedTill();
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final LocalDate accrualDate = isFinal
                ? (progressiveAccrual ? (DateUtils.isBefore(lastDueDate, businessDate) ? lastDueDate : businessDate)
                        : getFinalAccrualTransactionDate(loan))
                : tillDate;
        if (progressiveAccrual && accruedTill != null && !DateUtils.isAfter(tillDate, accruedTill)) {
            if (isFinal) {
                reverseTransactionsAfter(loan, ACCRUAL_TYPES, accrualDate, addJournal);
            } else if (loanTransactionRepository.existsNonReversedByLoanAndTypesAndOnOrAfterDate(loan, ACCRUAL_TYPES, accrualDate)
                    && hasNoActiveChargeOnDate(loan, accrualDate)) {
                return;
            }
        }

        final AccrualPeriodsData accrualPeriods = calculateAccrualAmounts(loan, tillDate, periodic, isFinal, chargeOnDueDate);
        final boolean mergeTransactions = isFinal || progressiveAccrual;
        final MonetaryCurrency currency = loan.getLoanProductRelatedDetail().getCurrency();
        List<LoanTransaction> accrualTransactions = new ArrayList<>();
        Money totalInterestPortion = null;
        LoanTransaction mergeAccrualTransaction = null;
        LoanTransaction mergeAdjustTransaction = null;
        for (AccrualPeriodData period : accrualPeriods.getPeriods()) {
            final Money interestAccruable = MathUtil.nullToZero(period.getInterestAccruable(), currency);
            final Money interestPortion = MathUtil.minus(interestAccruable, period.getInterestAccrued());
            final Money feeAccruable = MathUtil.nullToZero(period.getFeeAccruable(), currency);
            final Money feePortion = MathUtil.minus(feeAccruable, period.getFeeAccrued());
            final Money penaltyAccruable = MathUtil.nullToZero(period.getPenaltyAccruable(), currency);
            final Money penaltyPortion = MathUtil.minus(penaltyAccruable, period.getPenaltyAccrued());
            if (MathUtil.isEmpty(interestPortion) && MathUtil.isEmpty(feePortion) && MathUtil.isEmpty(penaltyPortion)) {
                continue;
            }
            if (mergeTransactions) {
                totalInterestPortion = MathUtil.plus(totalInterestPortion, interestPortion);
                if (progressiveAccrual) {
                    final Money feeAdjustmentPortion = MathUtil.negate(feePortion);
                    final Money penaltyAdjustmentPortion = MathUtil.negate(penaltyPortion);
                    mergeAdjustTransaction = createOrMergeAccrualTransaction(loan, mergeAdjustTransaction, accrualDate, period,
                            accrualTransactions, null, feeAdjustmentPortion, penaltyAdjustmentPortion, true);
                }
                mergeAccrualTransaction = createOrMergeAccrualTransaction(loan, mergeAccrualTransaction, accrualDate, period,
                        accrualTransactions, null, feePortion, penaltyPortion, false);
            } else {
                final LocalDate dueDate = period.getDueDate();
                if (!isFinal && DateUtils.isAfter(dueDate, tillDate) && DateUtils.isBefore(tillDate, accruedTill)) {
                    continue;
                }
                final LocalDate periodAccrualDate = DateUtils.isBefore(dueDate, accrualDate) ? dueDate : accrualDate;
                final LoanTransaction accrualTransaction = addAccrualTransaction(loan, periodAccrualDate, period, interestPortion,
                        feePortion, penaltyPortion, false);
                if (accrualTransaction != null) {
                    accrualTransactions.add(accrualTransaction);
                }
            }
            final LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(period.getInstallmentNumber());
            installment.updateAccrualPortion(interestAccruable, feeAccruable, penaltyAccruable);
        }
        if (mergeTransactions && !MathUtil.isEmpty(totalInterestPortion)) {
            if (progressiveAccrual) {
                final Money interestAdjustmentPortion = MathUtil.negate(totalInterestPortion);
                createOrMergeAccrualTransaction(loan, mergeAdjustTransaction, accrualDate, null, accrualTransactions,
                        interestAdjustmentPortion, null, null, true);
            }
            createOrMergeAccrualTransaction(loan, mergeAccrualTransaction, accrualDate, null, accrualTransactions, totalInterestPortion,
                    null, null, false);
        }
        if (accrualTransactions.isEmpty()) {
            return;
        }

        if (!isFinal || progressiveAccrual) {
            loan.setAccruedTill(isFinal ? accrualDate : tillDate);
        }

        accrualTransactions = loanTransactionRepository.saveAll(accrualTransactions);
        loanTransactionRepository.flush();

        if (addJournal) {
            for (LoanTransaction accrualTransaction : accrualTransactions) {
                final LoanTransactionBusinessEvent businessEvent = accrualTransaction.isAccrual()
                        ? new LoanAccrualTransactionCreatedBusinessEvent(accrualTransaction)
                        : new LoanAccrualAdjustmentTransactionBusinessEvent(accrualTransaction);
                businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                // Create journal entries immediately for this transaction
                journalEntryPoster.postJournalEntriesForLoanTransaction(accrualTransaction, false, false);
            }
        }
    }

    private boolean hasNoActiveChargeOnDate(Loan loan, LocalDate accrualDate) {
        return loan.getLoanCharges(t -> t.isActive() && DateUtils.isEqual(t.getDueDate(), accrualDate)).isEmpty();
    }

    private AccrualPeriodsData calculateAccrualAmounts(@NotNull final Loan loan, @NotNull final LocalDate tillDate, final boolean periodic,
            final boolean isFinal, final boolean chargeOnDueDate) {
        final LoanProductRelatedDetail productDetail = loan.getLoanProductRelatedDetail();
        final MonetaryCurrency currency = productDetail.getCurrency();
        final LoanScheduleGenerator scheduleGenerator = loanScheduleFactory.create(productDetail.getLoanScheduleType(),
                productDetail.getInterestMethod());
        final int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        final LocalDate interestCalculationTillDate = loan.isProgressiveSchedule()
                && loan.getLoanProductRelatedDetail().isInterestRecognitionOnDisbursementDate() ? tillDate.plusDays(1L) : tillDate;
        final List<LoanRepaymentScheduleInstallment> installments = isFinal ? loan.getRepaymentScheduleInstallments()
                : getInstallmentsToAccrue(loan, interestCalculationTillDate, periodic, chargeOnDueDate);
        final AccrualPeriodsData accrualPeriods = AccrualPeriodsData.create(installments, firstInstallmentNumber, currency);
        for (LoanRepaymentScheduleInstallment installment : installments) {
            addInterestAccrual(loan, interestCalculationTillDate, scheduleGenerator, installment, accrualPeriods);
            addChargeAccrual(loan, tillDate, chargeOnDueDate, installment, accrualPeriods);
        }
        return accrualPeriods;
    }

    @NotNull
    private List<LoanRepaymentScheduleInstallment> getInstallmentsToAccrue(@NotNull final Loan loan, @NotNull final LocalDate tillDate,
            final boolean periodic, final boolean chargeOnDueDate) {
        final LocalDate organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        final int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        return loan.getRepaymentScheduleInstallments(i -> !i.isDownPayment()
                && (!chargeOnDueDate || (periodic ? !isBeforePeriod(tillDate, i, i.getInstallmentNumber().equals(firstInstallmentNumber))
                        : isFullPeriod(tillDate, i)))
                && !isAfterPeriod(organisationStartDate, i));
    }

    private void addInterestAccrual(@NotNull final Loan loan, @NotNull final LocalDate tillDate,
            final LoanScheduleGenerator scheduleGenerator, @NotNull final LoanRepaymentScheduleInstallment installment,
            @NotNull final AccrualPeriodsData accrualPeriods) {
        if (installment.isAdditional() || installment.isReAged()) {
            return;
        }
        final AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        final MonetaryCurrency currency = accrualPeriods.getCurrency();
        Money interest = null;
        final boolean isPastPeriod = isAfterPeriod(tillDate, installment);
        final boolean isInPeriod = isInPeriod(tillDate, installment, false);
        if (isPastPeriod || loan.isClosed() || loanBalanceService.isOverPaid(loan)) {
            interest = installment.getInterestCharged(currency).minus(installment.getCreditedInterest());
        } else {
            if (isInPeriod) { // first period first day is not accrued
                interest = scheduleGenerator.getPeriodInterestTillDate(installment, tillDate);
            }
        }
        period.setInterestAmount(interest);
        Money accruable = null;
        Money transactionWaived = null;
        if (!MathUtil.isEmpty(interest)) {
            transactionWaived = MathUtil.toMoney(calcInterestTransactionWaivedAmount(installment, tillDate), currency);
            Money unrecognizedWaived = MathUtil.toMoney(calcInterestUnrecognizedWaivedAmount(installment, accrualPeriods, tillDate),
                    currency);
            // unrecognized maximum is the waived portion which is not covered by waiver transactions
            unrecognizedWaived = MathUtil.min(unrecognizedWaived,
                    MathUtil.minusToZero(installment.getInterestWaived(currency), transactionWaived), false);
            period.setUnrecognizedWaive(unrecognizedWaived);
            final Money waived = isPastPeriod ? installment.getInterestWaived(currency)
                    : MathUtil.plus(transactionWaived, unrecognizedWaived);
            accruable = MathUtil.minusToZero(period.getInterestAmount(), waived);
        }
        period.setInterestAccruable(accruable);
        final Money transactionAccrued = MathUtil.toMoney(calcInterestAccruedAmount(installment, accrualPeriods, tillDate), currency);
        period.setTransactionAccrued(transactionAccrued);
        final Money accrued = MathUtil.minusToZero(transactionAccrued, transactionWaived);
        period.setInterestAccrued(accrued);
    }

    @NotNull
    private BigDecimal calcInterestTransactionWaivedAmount(@NotNull LoanRepaymentScheduleInstallment installment,
            @NotNull LocalDate tillDate) {
        Predicate<LoanTransaction> transactionPredicate = t -> !t.isReversed() && t.isInterestWaiver()
                && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        return installment.getLoanTransactionToRepaymentScheduleMappings().stream()
                .filter(tm -> transactionPredicate.test(tm.getLoanTransaction()))
                .map(LoanTransactionToRepaymentScheduleMapping::getInterestPortion).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    @NotNull
    private BigDecimal calcInterestUnrecognizedWaivedAmount(@NotNull LoanRepaymentScheduleInstallment installment,
            @NotNull AccrualPeriodsData accrualPeriods, @NotNull LocalDate tillDate) {
        // unrecognized amount of the transaction is not mapped to installments
        LocalDate dueDate = installment.getDueDate();
        LocalDate toDate = DateUtils.isBefore(dueDate, tillDate) ? dueDate : tillDate;
        Loan loan = installment.getLoan();
        BigDecimal totalUnrecognized = loanTransactionRepository.findTotalUnrecognizedIncomeFromInterestWaiverByLoanAndDate(loan, toDate);
        // total unrecognized amount from previous periods
        BigDecimal prevUnrecognized = accrualPeriods.getPeriods().stream()
                .filter(p -> p.getInstallmentNumber() < installment.getInstallmentNumber())
                .map(p -> MathUtil.toBigDecimal(p.getUnrecognizedWaive())).reduce(BigDecimal.ZERO, MathUtil::add);
        // unrecognized amount left for this period (and maybe more)
        return MathUtil.min(installment.getInterestWaived(), MathUtil.subtractToZero(totalUnrecognized, prevUnrecognized), false);
    }

    @NotNull
    private BigDecimal calcInterestAccruedAmount(@NotNull LoanRepaymentScheduleInstallment installment,
            @NotNull AccrualPeriodsData accrualPeriods, @NotNull LocalDate tillDate) {
        Loan loan = installment.getLoan();
        if (isProgressiveAccrual(loan)) {
            BigDecimal totalAccrued = loanTransactionRepository.findTotalInterestAccruedAmount(loan);
            BigDecimal prevAccrued = accrualPeriods.getPeriods().stream()
                    .filter(p -> p.getInstallmentNumber() < installment.getInstallmentNumber())
                    .map(p -> MathUtil.toBigDecimal(p.getTransactionAccrued())).reduce(BigDecimal.ZERO, MathUtil::add);
            BigDecimal accrued = MathUtil.subtractToZero(totalAccrued, prevAccrued);
            // if this is the current-last period, all the remaining accrued amount is added
            return isInPeriod(tillDate, installment, false) ? accrued : MathUtil.min(installment.getInterestAccrued(), accrued, false);
        } else {
            return isFullPeriod(tillDate, installment) ? installment.getInterestAccrued()
                    : loanTransactionRepository.findAccrualInterestInPeriod(loan, installment.getFromDate(), installment.getDueDate());
        }
    }

    private void addChargeAccrual(@NotNull final Loan loan, @NotNull final LocalDate tillDate, final boolean chargeOnDueDate,
            @NotNull final LoanRepaymentScheduleInstallment installment, @NotNull final AccrualPeriodsData accrualPeriods) {
        final AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        final LocalDate dueDate = installment.getDueDate();
        final Collection<LoanCharge> loanCharges = loan
                .getLoanCharges(lc -> !lc.isDueAtDisbursement() && (lc.isInstalmentFee() ? !DateUtils.isBefore(tillDate, dueDate)
                        : isChargeDue(lc, tillDate, chargeOnDueDate, installment, period.isFirstPeriod())));
        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isActive()) {
                addChargeAccrual(loanCharge, tillDate, chargeOnDueDate, installment, accrualPeriods);
            }
        }
    }

    private void addChargeAccrual(@NotNull final LoanCharge loanCharge, @NotNull final LocalDate tillDate, final boolean chargeOnDueDate,
            @NotNull final LoanRepaymentScheduleInstallment installment, @NotNull final AccrualPeriodsData accrualPeriods) {
        final MonetaryCurrency currency = accrualPeriods.getCurrency();
        final Integer firstInstallmentNumber = accrualPeriods.getFirstInstallmentNumber();
        final boolean installmentFee = loanCharge.isInstalmentFee();
        final LoanRepaymentScheduleInstallment dueInstallment = (installmentFee || chargeOnDueDate) ? installment
                : loanCharge.getLoan().getRepaymentScheduleInstallment(
                        i -> isInPeriod(loanCharge.getDueDate(), i, i.getInstallmentNumber().equals(firstInstallmentNumber)));
        final AccrualPeriodData duePeriod = accrualPeriods.getPeriodByInstallmentNumber(dueInstallment.getInstallmentNumber());
        final boolean isFullPeriod = isFullPeriod(tillDate, dueInstallment);

        Money chargeAmount;
        Money waived;
        Collection<LoanChargePaidBy> paidBys;
        Long installmentChargeId = null;
        if (installmentFee) {
            final LoanInstallmentCharge installmentCharge = loanCharge.getInstallmentLoanCharge(dueInstallment.getInstallmentNumber());
            if (installmentCharge == null) {
                return;
            }
            chargeAmount = installmentCharge.getAmount(currency);
            paidBys = loanCharge.getLoanChargePaidBy(pb -> dueInstallment.getInstallmentNumber().equals(pb.getInstallmentNumber()));
            waived = isFullPeriod ? installmentCharge.getAmountWaived(currency)
                    : MathUtil.toMoney(calcChargeWaivedAmount(paidBys, tillDate), currency);
            installmentChargeId = installmentCharge.getId();
        } else {
            chargeAmount = loanCharge.getAmount(currency);
            paidBys = loanCharge.getLoanChargePaidBySet();
            waived = isFullPeriod ? loanCharge.getAmountWaived(currency)
                    : MathUtil.toMoney(calcChargeWaivedAmount(paidBys, tillDate), currency);
        }
        final AccrualChargeData chargeData = new AccrualChargeData(loanCharge.getId(), installmentChargeId, loanCharge.isPenaltyCharge())
                .setChargeAmount(chargeAmount);
        chargeData.setChargeAccruable(MathUtil.minusToZero(chargeAmount, waived));

        final Money unrecognizedWaived = MathUtil
                .toMoney(loanTransactionRepository.findChargeUnrecognizedWaivedAmount(loanCharge, tillDate), currency);
        final Money transactionWaived = MathUtil.minusToZero(waived, unrecognizedWaived);
        // For installment fees, use installment-specific accrual amount
        final Money transactionAccrued;
        if (installmentFee && installmentChargeId != null) {
            transactionAccrued = MathUtil.toMoney(
                    loanTransactionRepository.findChargeAccrualAmountByInstallment(loanCharge, dueInstallment.getInstallmentNumber()),
                    currency);
        } else {
            transactionAccrued = MathUtil.toMoney(loanTransactionRepository.findChargeAccrualAmount(loanCharge), currency);
        }
        chargeData.setTransactionAccrued(transactionAccrued);
        chargeData.setChargeAccrued(MathUtil.minusToZero(transactionAccrued, transactionWaived));

        duePeriod.addCharge(chargeData);
    }

    @NotNull
    private BigDecimal calcChargeWaivedAmount(@NotNull final Collection<LoanChargePaidBy> loanChargePaidBy,
            @NotNull final LocalDate tillDate) {
        return loanChargePaidBy.stream().filter(pb -> {
            final LoanTransaction t = pb.getLoanTransaction();
            return !t.isReversed() && t.isWaiveCharge() && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        }).map(LoanChargePaidBy::getAmount).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    private boolean isChargeDue(@NotNull final LoanCharge loanCharge, @NotNull final LocalDate tillDate, boolean chargeOnDueDate,
            final LoanRepaymentScheduleInstallment installment, final boolean isFirstPeriod) {
        final LocalDate fromDate = installment.getFromDate();
        final LocalDate dueDate = installment.getDueDate();
        final LocalDate toDate = DateUtils.isBefore(dueDate, tillDate) ? dueDate : tillDate;
        chargeOnDueDate = chargeOnDueDate || loanCharge.getDueLocalDate().isBefore(loanCharge.getSubmittedOnDate());
        return chargeOnDueDate ? loanCharge.isDueInPeriod(fromDate, toDate, isFirstPeriod)
                : isInPeriod(loanCharge.getSubmittedOnDate(), fromDate, toDate, isFirstPeriod);
    }

    private LoanTransaction createOrMergeAccrualTransaction(@NotNull final Loan loan, LoanTransaction transaction,
            final LocalDate transactionDate, final AccrualPeriodData accrualPeriod, final List<LoanTransaction> accrualTransactions,
            final Money interest, final Money fee, final Money penalty, final boolean adjustment) {
        if (transaction == null) {
            transaction = addAccrualTransaction(loan, transactionDate, accrualPeriod, interest, fee, penalty, adjustment);
            if (transaction != null) {
                accrualTransactions.add(transaction);
            }
        } else {
            mergeAccrualTransaction(transaction, accrualPeriod, interest, fee, penalty, adjustment);
        }
        return transaction;
    }

    private LoanTransaction addAccrualTransaction(@NotNull Loan loan, @NotNull LocalDate transactionDate, AccrualPeriodData accrualPeriod,
            Money interestPortion, Money feePortion, Money penaltyPortion, boolean adjustment) {
        interestPortion = MathUtil.negativeToZero(interestPortion);
        BigDecimal interest = MathUtil.toBigDecimal(interestPortion);
        feePortion = MathUtil.negativeToZero(feePortion);
        BigDecimal fee = MathUtil.toBigDecimal(feePortion);
        penaltyPortion = MathUtil.negativeToZero(penaltyPortion);
        BigDecimal penalty = MathUtil.toBigDecimal(penaltyPortion);
        BigDecimal amount = MathUtil.add(interest, fee, penalty);
        if (!MathUtil.isGreaterThanZero(amount)) {
            return null;
        }
        LoanTransaction transaction = adjustment
                ? accrualAdjustment(loan, loan.getOffice(), transactionDate, amount, interest, fee, penalty, externalIdFactory.create())
                : accrueTransaction(loan, loan.getOffice(), transactionDate, amount, interest, fee, penalty, externalIdFactory.create());

        // update repayment schedule portions
        addTransactionMappings(transaction, accrualPeriod, adjustment);
        LoanTransaction savedTransaction = loanTransactionRepository.save(transaction);
        loan.addLoanTransaction(savedTransaction);
        return savedTransaction;
    }

    private void mergeAccrualTransaction(@NotNull final LoanTransaction transaction, final AccrualPeriodData accrualPeriod,
            Money interestPortion, Money feePortion, Money penaltyPortion, final boolean adjustment) {
        interestPortion = MathUtil.negativeToZero(interestPortion);
        feePortion = MathUtil.negativeToZero(feePortion);
        penaltyPortion = MathUtil.negativeToZero(penaltyPortion);
        if (MathUtil.isEmpty(interestPortion) && MathUtil.isEmpty(feePortion) && MathUtil.isEmpty(penaltyPortion)) {
            return;
        }

        transaction.updateComponentsAndTotal(null, interestPortion, feePortion, penaltyPortion);
        // update repayment schedule portions
        addTransactionMappings(transaction, accrualPeriod, adjustment);
    }

    private void addTransactionMappings(@NotNull final LoanTransaction transaction, final AccrualPeriodData accrualPeriod,
            final boolean adjustment) {
        if (accrualPeriod == null) {
            return;
        }
        final Loan loan = transaction.getLoan();
        final Integer installmentNumber = accrualPeriod.getInstallmentNumber();
        final LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentNumber);

        // add charges paid by mappings
        addPaidByMappings(transaction, installment, accrualPeriod, adjustment);
    }

    private void addPaidByMappings(@NotNull final LoanTransaction transaction, final LoanRepaymentScheduleInstallment installment,
            final AccrualPeriodData accrualPeriod, final boolean adjustment) {
        final Loan loan = installment.getLoan();
        final MonetaryCurrency currency = loan.getCurrency();
        for (AccrualChargeData accrualCharge : accrualPeriod.getCharges()) {
            final Money chargeAccruable = MathUtil.nullToZero(accrualCharge.getChargeAccruable(), currency);
            Money chargePortion = MathUtil.minus(chargeAccruable, accrualCharge.getChargeAccrued());
            chargePortion = MathUtil.negativeToZero(adjustment ? MathUtil.negate(chargePortion) : chargePortion);
            if (MathUtil.isEmpty(chargePortion)) {
                continue;
            }
            final BigDecimal chargeAmount = MathUtil.toBigDecimal(chargePortion);
            final LoanCharge loanCharge = loanChargeService.fetchLoanChargesById(loan, accrualCharge.getLoanChargeId());
            final LoanChargePaidBy paidBy = new LoanChargePaidBy(transaction, loanCharge, chargeAmount, installment.getInstallmentNumber());
            loanCharge.getLoanChargePaidBySet().add(paidBy);
            transaction.getLoanChargesPaid().add(paidBy);
            final Long installmentChargeId = accrualCharge.getLoanInstallmentChargeId();
            if (installmentChargeId != null) {
                final LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge(chargeAmount, loanCharge, installment);
                loanCharge.getLoanInstallmentCharge().add(installmentCharge);
                installment.getInstallmentCharges().add(installmentCharge);
            }
        }
    }

    private boolean isFullPeriod(@NotNull final LocalDate tillDate, @NotNull final LoanRepaymentScheduleInstallment installment) {
        return isAfterPeriod(tillDate, installment) || DateUtils.isEqual(tillDate, installment.getDueDate());
    }

    // ReprocessAccruals

    private void reprocessPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions, final boolean addEvent) {
        if (loan.isChargedOff()) {
            return;
        }
        final boolean isChargeOnDueDate = isChargeOnDueDate();
        ensureAccrualTransactionMappings(loan, isChargeOnDueDate);
        LoanRepaymentScheduleInstallment lastInstallment = loan.getLastLoanRepaymentScheduleInstallment();
        LocalDate lastDueDate = lastInstallment.getDueDate();
        if (isProgressiveAccrual(loan)) {
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
        } else {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            boolean isBasedOnSubmittedOnDate = !isChargeOnDueDate;
            for (LoanRepaymentScheduleInstallment installment : installments) {
                checkAndUpdateAccrualsForInstallment(loan, accrualTransactions, installments, isBasedOnSubmittedOnDate, installment,
                        addEvent);
            }
        }
        // reverse accruals after last installment
        reverseTransactionsAfter(loan, ACCRUAL_TYPES, lastDueDate, addEvent);
    }

    private void reprocessNonPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions, final boolean addEvent) {
        if (isProgressiveAccrual(loan)) {
            return;
        }
        final Money interestApplied = Money.of(loan.getCurrency(), loan.getSummary().getTotalInterestCharged());
        ExternalId externalId = ExternalId.empty();
        boolean isExternalIdAutoGenerationEnabled = configurationDomainService.isExternalIdAutoGenerationEnabled();

        for (LoanTransaction accrualTransaction : accrualTransactions) {
            if (accrualTransaction.getInterestPortion(loan.getCurrency()).isGreaterThanZero()) {
                if (accrualTransaction.getInterestPortion(loan.getCurrency()).isNotEqualTo(interestApplied)) {
                    accrualTransaction.reverse();
                    if (addEvent) {
                        journalEntryPoster.postJournalEntriesForLoanTransaction(accrualTransaction, false, false);
                        final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(
                                accrualTransaction);
                        businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                    }
                    if (isExternalIdAutoGenerationEnabled) {
                        externalId = ExternalId.generate();
                    }
                    final LoanTransaction interestAccrualTransaction = LoanTransaction.accrueInterest(loan.getOffice(), loan,
                            interestApplied, loan.getDisbursementDate(), externalId);
                    LoanTransaction savedInterestAccrualTransaction = loanTransactionRepository.save(interestAccrualTransaction);
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
                        accrualTransaction.reverse();
                        if (addEvent) {
                            journalEntryPoster.postJournalEntriesForLoanTransaction(accrualTransaction, false, false);
                            final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(
                                    accrualTransaction);
                            businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                        }
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
                    accrualTransaction.reverse();
                    if (addEvent) {
                        journalEntryPoster.postJournalEntriesForLoanTransaction(accrualTransaction, false, false);
                        final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(
                                accrualTransaction);
                        businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                    }
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

    // IncomePosting

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
            determineFeeDetails(loan, lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
            fee = (BigDecimal) feeDetails.get(Loan.FEE);
            penalties = (BigDecimal) feeDetails.get(Loan.PENALTIES);
        } else if (loan.getLoanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod()
                .equals(InterestRecalculationCompoundingMethod.INTEREST_AND_FEE)) {
            determineFeeDetails(loan, lastCompoundingDate, compoundingDetail.getEffectiveDate(), feeDetails);
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
                accrualTransaction.ifPresent(accrualTrans -> {
                    accrualTrans.reverse();
                    if (addEvent) {
                        journalEntryPoster.postJournalEntriesForLoanTransaction(accrualTrans, false, false);
                        final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(accrualTrans);
                        businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                    }
                });
                LoanTransaction accrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(loan, accrual, feeDetails, null);
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

    // LoanClosure

    private void processIncomeAndAccrualTransactionOnLoanClosure(Loan loan) {
        // TODO analyze progressive accrual case
        if (loan.getLoanInterestRecalculationDetails() != null
                && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction()
                && loan.getStatus().isClosedObligationsMet() && !loan.isNpa() && !loan.isChargedOff()) {

            LocalDate closedDate = loan.getClosedOnDate();
            reverseTransactionsOnOrAfter(loan, Set.of(INCOME_POSTING, ACCRUAL, ACCRUAL_ADJUSTMENT), closedDate);

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
            determineFeeDetails(loan, lastAccruedDate, closedDate, feeDetails);
            if (isExternalIdAutoGenerationEnabled) {
                externalId = ExternalId.generate();
            }
            LoanTransaction finalAccrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), closedDate, amountToPost,
                    interestToPost, feeToPost, penaltyToPost, externalId);
            updateLoanChargesPaidBy(loan, finalAccrual, feeDetails, null);
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

    private void ensureAccrualTransactionMappings(final Loan loan, final boolean chargeOnDueDate) {
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

    private List<LoanTransaction> retrieveListOfAccrualTransactions(final Loan loan) {
        return loanTransactionRepository.findNonReversedByLoanAndTypes(loan, ACCRUAL_TYPES).stream()
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }

    private boolean isChargeOnDueDate() {
        final String chargeAccrualDateType = configurationDomainService.getAccrualDateConfigForCharge();
        return !ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE.equalsIgnoreCase(chargeAccrualDateType);
    }

    private void determineFeeDetails(Loan loan, LocalDate fromDate, LocalDate toDate, Map<String, Object> feeDetails) {
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

    private void updateLoanChargesPaidBy(Loan loan, LoanTransaction accrual, Map<String, Object> feeDetails,
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

    private void reverseTransactionsAfter(final Loan loan, final Set<LoanTransactionType> types, final LocalDate effectiveDate,
            final boolean addEvent) {
        loanTransactionRepository.findNonReversedByLoanAndTypesAndAfterDate(loan, types, effectiveDate)
                .forEach(transaction -> reverseAccrual(transaction, addEvent));
    }

    private void reverseTransactionsOnOrAfter(final Loan loan, final Set<LoanTransactionType> types, final LocalDate date) {
        loanTransactionRepository.findNonReversedByLoanAndTypesAndOnOrAfterDate(loan, types, date)
                .forEach(transaction -> reverseAccrual(transaction, true));
    }

    private void reverseAccrual(final LoanTransaction transaction, final boolean addEvent) {
        transaction.reverse();
        if (addEvent) {
            journalEntryPoster.postJournalEntriesForLoanTransaction(transaction, false, false);
            final LoanTransactionBusinessEvent businessEvent = new LoanAccrualAdjustmentTransactionBusinessEvent(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
        }
    }

    private LocalDate getFinalAccrualTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }

    public boolean isProgressiveAccrual(@NotNull Loan loan) {
        return loan.isProgressiveSchedule();
    }
}
