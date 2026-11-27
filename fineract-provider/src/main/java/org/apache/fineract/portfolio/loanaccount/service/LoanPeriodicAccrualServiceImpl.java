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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.TaskExecutorConstant;
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
import org.apache.fineract.portfolio.loanaccount.data.AccrualChargeData;
import org.apache.fineract.portfolio.loanaccount.data.AccrualPeriodData;
import org.apache.fineract.portfolio.loanaccount.data.AccrualPeriodsData;
import org.apache.fineract.portfolio.loanaccount.domain.DownPaymentFees;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeInterestHandlingType;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanPeriodicAccrualServiceImpl implements LoanPeriodicAccrualService {

    private static final Set<LoanTransactionType> ACCRUAL_TYPES = Set.of(ACCRUAL, ACCRUAL_ADJUSTMENT);

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
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanAccrualTransactionService loanAccrualTransactionService;

    /**
     * method adds accrual for batch job "Add Periodic Accrual Transactions" and add accruals api for Loan
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(@NonNull LocalDate tillDate) throws JobExecutionException {
        final boolean chargeOnDueDate = loanAccrualTransactionService.isChargeOnDueDate();
        final boolean plChargeAccrualRealTime = configurationDomainService.isPlChargeAccrualRealTime();
        List<Loan> loans = loanRepositoryWrapper.findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate,
                !chargeOnDueDate, plChargeAccrualRealTime);
        List<Throwable> errors = new ArrayList<>();
        for (Loan loan : loans) {
            try {
                final boolean realTimeChargeAccrual = loanAccrualTransactionService.isRealTimeChargeAccrual(loan, plChargeAccrualRealTime);
                addPeriodicAccruals(tillDate, loan,
                        loanAccrualTransactionService.resolveChargeOnDueDate(chargeOnDueDate, realTimeChargeAccrual),
                        realTimeChargeAccrual);
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
    public void addPeriodicAccruals(@NonNull final LocalDate tillDate, @NonNull final Loan loan) {
        final boolean realTimeChargeAccrual = loanAccrualTransactionService.isRealTimeChargeAccrual(loan);
        addPeriodicAccruals(tillDate, loan, loanAccrualTransactionService
                .resolveChargeOnDueDate(loanAccrualTransactionService.isChargeOnDueDate(), realTimeChargeAccrual), realTimeChargeAccrual);
    }

    private void addPeriodicAccruals(final LocalDate tillDate, final Loan loan, final boolean chargeOnDueDate,
            final boolean realTimeChargeAccrual) {
        if (loan.isClosed() || loan.getStatus().isOverpaid()) {
            return;
        }
        addAccruals(loan, tillDate, true, false, true, chargeOnDueDate, realTimeChargeAccrual);
    }

    /**
     * method adds accrual for batch job "Add Accrual Transactions"
     */
    @Override
    @Transactional
    public void addAccruals(@NonNull LocalDate tillDate) throws JobExecutionException {
        final boolean chargeOnDueDate = loanAccrualTransactionService.isChargeOnDueDate();
        final boolean plChargeAccrualRealTime = configurationDomainService.isPlChargeAccrualRealTime();
        List<Loan> loans = loanRepositoryWrapper.findLoansForAddAccrual(AccountingRuleType.ACCRUAL_PERIODIC, tillDate, !chargeOnDueDate,
                plChargeAccrualRealTime);

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
                            final boolean realTimeChargeAccrual = loanAccrualTransactionService.isRealTimeChargeAccrual(loan,
                                    plChargeAccrualRealTime);
                            addAccruals(loan, tillDate, false, false, true,
                                    loanAccrualTransactionService.resolveChargeOnDueDate(chargeOnDueDate, realTimeChargeAccrual),
                                    realTimeChargeAccrual);
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
     * method calculates accruals for loan with interest recalculation on loan schedule when interest is recalculated
     */
    @Override
    @Transactional
    public void processAccrualsOnInterestRecalculation(@NonNull Loan loan, boolean isInterestRecalculationEnabled, boolean addJournal) {
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
            return;
        }
        LocalDate accruedTill = loan.getAccruedTill();
        if (!isInterestRecalculationEnabled || accruedTill == null) {
            return;
        }
        try {
            final boolean chargeOnDueDate = loanAccrualTransactionService.isChargeOnDueDate();
            addAccruals(loan, accruedTill, true, false, addJournal, chargeOnDueDate, false);
        } catch (Exception e) {
            String globalisationMessageCode = "error.msg.accrual.exception";
            throw new GeneralPlatformDomainRuleException(globalisationMessageCode, e.getMessage(), e);
        }
    }

    @Override
    public boolean isRealTimeChargeAccrualApplicable(@NonNull final Loan loan) {
        return loanAccrualTransactionService.isRealTimeChargeAccrual(loan) && loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && loan.getStatus().isActive() && !loan.isNpa() && !loan.isChargedOff();
    }

    @Override
    @Transactional
    public void processRealTimeChargeAccruals(@NonNull final Loan loan, @NonNull final List<LoanCharge> loanCharges) {
        if (loanCharges.isEmpty() || !isRealTimeChargeAccrualApplicable(loan)) {
            return;
        }
        loanCharges.stream().filter(loanCharge -> isRealTimeAccruable(loan, loanCharge)).forEach(loanCharge -> loanAccrualTransactionService
                .postChargeAccrual(loanChargeService.createChargeAppliedTransaction(loan, loanCharge, null)));
    }

    private boolean isRealTimeAccruable(final Loan loan, final LoanCharge loanCharge) {
        return loanCharge.isActive() && !loanCharge.isDueAtDisbursement() && !loanCharge.isInstalmentFee()
                && loanCharge.getAmount(loan.getCurrency()).isGreaterThanZero()
                && !MathUtil.isGreaterThanZero(loanTransactionRepository.findChargeAccrualAmount(loanCharge));
    }

    @Override
    public void addAccruals(@NonNull final Loan loan, @NonNull LocalDate tillDate, final boolean periodic, final boolean isFinal,
            final boolean addJournal, final boolean chargeOnDueDate, final boolean realTimeChargeAccrual) {
        if ((!isFinal && !loan.isOpen()) || loan.isNpa() || loan.isChargedOff()
                || !loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            return;
        }

        final LoanInterestRecalculationDetails recalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (recalculationDetails != null && recalculationDetails.isCompoundingToBePostedAsTransaction()) {
            return;
        }

        final LocalDate lastDueDate = loan.getLastLoanRepaymentScheduleInstallment().getDueDate();
        final boolean progressiveAccrual = loanAccrualTransactionService.isProgressiveAccrual(loan);
        if (progressiveAccrual) {
            // Progressive: use ACCRUAL_ADJUSTMENT — the recalculation uses DB-level sums that account for adjustments
            loanAccrualTransactionService.adjustAccrualsAfter(loan, lastDueDate, addJournal);
        } else {
            // Cumulative: use reversal — the recalculation uses cached installment fields, then recreates the accrual
            loanAccrualTransactionService.reverseTransactionsAfter(loan, ACCRUAL_TYPES, lastDueDate, addJournal);
        }
        loanAccrualTransactionService.ensureAccrualTransactionMappings(loan, chargeOnDueDate);
        if (DateUtils.isAfter(tillDate, lastDueDate)) {
            tillDate = lastDueDate;
        }

        final LocalDate accruedTill = loan.getAccruedTill();
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final LocalDate accrualDate = isFinal
                ? (progressiveAccrual ? (DateUtils.isBefore(lastDueDate, businessDate) ? lastDueDate : businessDate)
                        : getFinalAccrualTransactionDate(loan))
                : tillDate;
        if (progressiveAccrual && accruedTill != null && !DateUtils.isAfter(tillDate, accruedTill)) {
            if (isFinal) {
                loanAccrualTransactionService.adjustAccrualsAfter(loan, accrualDate, addJournal);
            } else if (loanTransactionRepository.existsNonReversedByLoanAndTypesAndOnOrAfterDate(loan, ACCRUAL_TYPES, accrualDate)
                    && hasNoActiveChargeOnDate(loan, accrualDate)) {
                return;
            }
        }

        final AccrualPeriodsData accrualPeriods = calculateAccrualAmounts(loan, tillDate, periodic, isFinal, chargeOnDueDate,
                realTimeChargeAccrual);
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

    private AccrualPeriodsData calculateAccrualAmounts(@NonNull final Loan loan, @NonNull final LocalDate tillDate, final boolean periodic,
            final boolean isFinal, final boolean chargeOnDueDate, final boolean realTimeChargeAccrual) {
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
            addChargeAccrual(loan, tillDate, chargeOnDueDate, realTimeChargeAccrual, installment, accrualPeriods);
        }
        return accrualPeriods;
    }

    @NonNull
    private List<LoanRepaymentScheduleInstallment> getInstallmentsToAccrue(@NonNull final Loan loan, @NonNull final LocalDate tillDate,
            final boolean periodic, final boolean chargeOnDueDate) {
        final LocalDate organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        final int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        return loan.getRepaymentScheduleInstallments(i -> {
            // Down payment periods carry DOWN_PAYMENT fees and must be included for charge accrual once due.
            if (i.isDownPayment()) {
                return !DateUtils.isAfter(i.getDueDate(), tillDate) && !isAfterPeriod(organisationStartDate, i);
            }
            return (!chargeOnDueDate || (periodic ? !isBeforePeriod(tillDate, i, i.getInstallmentNumber().equals(firstInstallmentNumber))
                    : isFullPeriod(tillDate, i))) && !isAfterPeriod(organisationStartDate, i);
        });
    }

    private void addInterestAccrual(@NonNull final Loan loan, @NonNull final LocalDate tillDate,
            final LoanScheduleGenerator scheduleGenerator, @NonNull final LoanRepaymentScheduleInstallment installment,
            @NonNull final AccrualPeriodsData accrualPeriods) {
        // No interest, no need for calculations
        if (!loan.isInterestBearing()) {
            return;
        }
        final LoanTransaction reAgeTransaction = loan.findReAgeTransaction();
        final LoanReAgeParameter loanReAgeParameter = reAgeTransaction != null ? reAgeTransaction.getLoanReAgeParameter() : null;

        if (installment.isAdditional() || (installment.isReAged() && loanReAgeParameter != null
                && !(LoanReAgeInterestHandlingType.EQUAL_AMORTIZATION_FULL_INTEREST.equals(loanReAgeParameter.getInterestHandlingType())
                        || LoanReAgeInterestHandlingType.EQUAL_AMORTIZATION_PAYABLE_INTEREST
                                .equals(loanReAgeParameter.getInterestHandlingType())
                        || LoanReAgeInterestHandlingType.DEFAULT.equals(loanReAgeParameter.getInterestHandlingType())))) {
            return;
        }
        final AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        final MonetaryCurrency currency = accrualPeriods.getCurrency();
        Money interest = null;
        LocalDate effectiveDate = tillDate;
        if (loan.isClosed() && loan.getClosedOnDate() != null && DateUtils.isBefore(loan.getClosedOnDate(), tillDate)) {
            effectiveDate = loan.getClosedOnDate();
        } else if (loanBalanceService.isOverPaid(loan) && loan.getOverpaidOnDate() != null
                && DateUtils.isBefore(loan.getOverpaidOnDate(), tillDate)) {
            effectiveDate = loan.getOverpaidOnDate();
        }
        final boolean isPastPeriod = isAfterPeriod(effectiveDate, installment);
        final boolean isInPeriod = isInPeriod(effectiveDate, installment, false);
        if (isPastPeriod || loan.isClosed() || loanBalanceService.isOverPaid(loan)) {
            interest = installment.getInterestCharged(currency).minus(installment.getCreditedInterest());
        } else if (isInPeriod) {
            interest = scheduleGenerator.getPeriodInterestTillDate(installment, effectiveDate);
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

    @NonNull
    private BigDecimal calcInterestTransactionWaivedAmount(@NonNull LoanRepaymentScheduleInstallment installment,
            @NonNull LocalDate tillDate) {
        Predicate<LoanTransaction> transactionPredicate = t -> !t.isReversed() && t.isInterestWaiver()
                && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        return installment.getLoanTransactionToRepaymentScheduleMappings().stream()
                .filter(tm -> tm.getLoanTransaction() != null && transactionPredicate.test(tm.getLoanTransaction()))
                .map(LoanTransactionToRepaymentScheduleMapping::getInterestPortion).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    @NonNull
    private BigDecimal calcInterestUnrecognizedWaivedAmount(@NonNull LoanRepaymentScheduleInstallment installment,
            @NonNull AccrualPeriodsData accrualPeriods, @NonNull LocalDate tillDate) {
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

    @NonNull
    private BigDecimal calcInterestAccruedAmount(@NonNull LoanRepaymentScheduleInstallment installment,
            @NonNull AccrualPeriodsData accrualPeriods, @NonNull LocalDate tillDate) {
        Loan loan = installment.getLoan();
        if (loanAccrualTransactionService.isProgressiveAccrual(loan)) {
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

    private void addChargeAccrual(@NonNull final Loan loan, @NonNull final LocalDate tillDate, final boolean chargeOnDueDate,
            final boolean realTimeChargeAccrual, @NonNull final LoanRepaymentScheduleInstallment installment,
            @NonNull final AccrualPeriodsData accrualPeriods) {
        final AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        final LocalDate dueDate = installment.getDueDate();
        final Collection<LoanCharge> loanCharges;
        if (installment.isDownPayment()) {
            // DOWN_PAYMENT fees live on the DP period of their tranche; several DP periods can share a due date.
            loanCharges = DateUtils.isAfter(dueDate, tillDate) ? List.of()
                    : DownPaymentFees.ofPeriod(loan.getLoanCharges(), installment, loan.getRepaymentScheduleInstallments());
        } else {
            // Exclude DOWN_PAYMENT fees here — they are accrued via the down payment installment above.
            loanCharges = loan.getLoanCharges(
                    lc -> !lc.isDueAtDisbursementOrDownPayment() && (lc.isInstalmentFee() ? !DateUtils.isBefore(tillDate, dueDate)
                            : isChargeDue(lc, tillDate, chargeOnDueDate, realTimeChargeAccrual, installment, period.isFirstPeriod())));
        }
        for (LoanCharge loanCharge : loanCharges) {
            if (loanCharge.isActive()) {
                addChargeAccrual(loanCharge, tillDate, chargeOnDueDate, installment, accrualPeriods);
            }
        }
    }

    private void addChargeAccrual(@NonNull final LoanCharge loanCharge, @NonNull final LocalDate tillDate, final boolean chargeOnDueDate,
            @NonNull final LoanRepaymentScheduleInstallment installment, @NonNull final AccrualPeriodsData accrualPeriods) {
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

    @NonNull
    private BigDecimal calcChargeWaivedAmount(@NonNull final Collection<LoanChargePaidBy> loanChargePaidBy,
            @NonNull final LocalDate tillDate) {
        return loanChargePaidBy.stream().filter(pb -> {
            final LoanTransaction t = pb.getLoanTransaction();
            return t != null && !t.isReversed() && t.isWaiveCharge() && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        }).map(LoanChargePaidBy::getAmount).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    private boolean isChargeDue(@NonNull final LoanCharge loanCharge, @NonNull final LocalDate tillDate, boolean chargeOnDueDate,
            final boolean realTimeChargeAccrual, final LoanRepaymentScheduleInstallment installment, final boolean isFirstPeriod) {
        final LocalDate fromDate = installment.getFromDate();
        final LocalDate dueDate = installment.getDueDate();
        final LocalDate toDate = DateUtils.isBefore(dueDate, tillDate) ? dueDate : tillDate;
        chargeOnDueDate = chargeOnDueDate || loanCharge.getDueLocalDate().isBefore(loanCharge.getSubmittedOnDate())
                || (realTimeChargeAccrual && isSubmittedBeforeDisbursement(loanCharge));
        return chargeOnDueDate ? loanCharge.isDueInPeriod(fromDate, toDate, isFirstPeriod)
                : isInPeriod(loanCharge.getSubmittedOnDate(), fromDate, toDate, isFirstPeriod);
    }

    private static boolean isSubmittedBeforeDisbursement(final LoanCharge loanCharge) {
        return DateUtils.isBefore(loanCharge.getSubmittedOnDate(), loanCharge.getLoan().getDisbursementDate());
    }

    private LoanTransaction createOrMergeAccrualTransaction(@NonNull final Loan loan, LoanTransaction transaction,
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

    private LoanTransaction addAccrualTransaction(@NonNull Loan loan, @NonNull LocalDate transactionDate, AccrualPeriodData accrualPeriod,
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

    private void mergeAccrualTransaction(@NonNull final LoanTransaction transaction, final AccrualPeriodData accrualPeriod,
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

    private void addTransactionMappings(@NonNull final LoanTransaction transaction, final AccrualPeriodData accrualPeriod,
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

    private void addPaidByMappings(@NonNull final LoanTransaction transaction, final LoanRepaymentScheduleInstallment installment,
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

    private boolean isFullPeriod(@NonNull final LocalDate tillDate, @NonNull final LoanRepaymentScheduleInstallment installment) {
        return isAfterPeriod(tillDate, installment) || DateUtils.isEqual(tillDate, installment.getDueDate());
    }

    private LocalDate getFinalAccrualTransactionDate(final Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }
}
