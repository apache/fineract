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

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
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
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalculationDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionToRepaymentScheduleMapping;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualsProcessingServiceImpl implements LoanAccrualsProcessingService {

    private static final Predicate<LoanTransaction> ACCRUAL_PREDICATE = t -> t.isNotReversed()
            && (t.isAccrual() || t.isAccrualAdjustment());

    private static final String ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE = "submitted-date";
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;

    /**
     * method adds accrual for batch job "Add Periodic Accrual Transactions" and add accruals api for Loan
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(@NotNull LocalDate tillDate) throws JobExecutionException {
        List<Loan> loans = loanRepositoryWrapper.findLoansForPeriodicAccrual(AccountingRuleType.ACCRUAL_PERIODIC.getValue(), tillDate,
                !isChargeOnDueDate());
        List<Throwable> errors = new ArrayList<>();
        for (Loan loan : loans) {
            try {
                setSetHelpers(loan);
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
    public void addPeriodicAccruals(@NotNull LocalDate tillDate, @NotNull Loan loan) {
        addAccruals(loan, tillDate, true, false, true);
    }

    /**
     * method adds accrual for batch job "Add Accrual Transactions"
     */
    @Override
    @Transactional
    public void addAccruals(@NotNull LocalDate tillDate) throws JobExecutionException {
        List<Loan> loans = loanRepositoryWrapper.findLoansForAddAccrual(AccountingRuleType.ACCRUAL_PERIODIC.getValue(), tillDate,
                !isChargeOnDueDate());
        List<Throwable> errors = new ArrayList<>();
        for (Loan loan : loans) {
            try {
                setSetHelpers(loan);
                addAccruals(loan, tillDate, false, false, true);
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
     * method updates accrual derived fields on installments and reverse the unprocessed transactions for loan
     * reschedule
     */
    @Override
    public void reprocessExistingAccruals(@NotNull Loan loan) {
        List<LoanTransaction> accrualTransactions = retrieveListOfAccrualTransactions(loan);
        if (!accrualTransactions.isEmpty()) {
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                reprocessPeriodicAccruals(loan, accrualTransactions);
            } else if (loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                reprocessNonPeriodicAccruals(loan, accrualTransactions);
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
            addAccruals(loan, accruedTill, true, false, addJournal);
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
        final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
        final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());
        processIncomePostingAndAccruals(loan);
        this.loanRepositoryWrapper.saveAndFlush(loan);
        postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
    }

    /**
     * method calculates accruals for loan with interest recalculation and compounding to be posted as income
     */
    @Override
    public void processIncomePostingAndAccruals(@NotNull Loan loan) {
        if (isProgressiveAccrual(loan)) {
            return;
        }
        LoanInterestRecalculationDetails recalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (recalculationDetails == null || !recalculationDetails.isCompoundingToBePostedAsTransaction()) {
            return;
        }
        LocalDate lastCompoundingDate = loan.getDisbursementDate();
        List<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails = extractInterestRecalculationAdditionalDetails(loan);
        List<LoanTransaction> incomeTransactions = retrieveListOfIncomePostingTransactions(loan);
        List<LoanTransaction> accrualTransactions = retrieveListOfAccrualTransactions(loan);
        for (LoanInterestRecalcualtionAdditionalDetails compoundingDetail : compoundingDetails) {
            if (!DateUtils.isBeforeBusinessDate(compoundingDetail.getEffectiveDate())) {
                break;
            }
            LoanTransaction incomeTransaction = getTransactionForDate(incomeTransactions, compoundingDetail.getEffectiveDate());
            LoanTransaction accrualTransaction = getTransactionForDate(accrualTransactions, compoundingDetail.getEffectiveDate());
            addUpdateIncomeAndAccrualTransaction(loan, compoundingDetail, lastCompoundingDate, incomeTransaction, accrualTransaction);
            lastCompoundingDate = compoundingDetail.getEffectiveDate();
        }
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        LoanRepaymentScheduleInstallment lastInstallment = LoanRepaymentScheduleInstallment.getLastNonDownPaymentInstallment(installments);
        reverseTransactionsAfter(incomeTransactions, lastInstallment.getDueDate(), false);
        reverseTransactionsAfter(accrualTransactions, lastInstallment.getDueDate(), false);
    }

    /**
     * method calculates accruals for loan on loan closure
     */
    @Override
    public void processAccrualsOnLoanClosure(@NotNull Loan loan) {
        // check and process accruals for loan WITHOUT interest recalculation details and compounding posted as income
        addAccruals(loan, loan.getLastLoanRepaymentScheduleInstallment().getDueDate(), false, true, false);
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
            final LoanRepaymentScheduleInstallment foreCloseDetail = loan.fetchLoanForeclosureDetail(foreClosureDate);
            MonetaryCurrency currency = loan.getCurrency();
            reverseTransactionsAfter(retrieveListOfAccrualTransactions(loan), foreClosureDate, false);

            HashMap<String, Object> incomeDetails = new HashMap<>();

            determineReceivableIncomeForeClosure(loan, foreClosureDate, incomeDetails);

            Money interestPortion = foreCloseDetail.getInterestCharged(currency).minus((Money) incomeDetails.get(Loan.INTEREST));
            Money feePortion = foreCloseDetail.getFeeChargesCharged(currency).minus((Money) incomeDetails.get(Loan.FEE));
            Money penaltyPortion = foreCloseDetail.getPenaltyChargesCharged(currency).minus((Money) incomeDetails.get(Loan.PENALTIES));
            Money total = interestPortion.plus(feePortion).plus(penaltyPortion);

            if (total.isGreaterThanZero()) {
                createAccrualTransactionAndUpdateChargesPaidBy(loan, foreClosureDate, newAccrualTransactions, currency, interestPortion,
                        feePortion, penaltyPortion, total);
            }
        }
    }

    // PeriodicAccruals

    private void addAccruals(@NotNull Loan loan, @NotNull LocalDate tillDate, boolean periodic, boolean isFinal, boolean addJournal) {
        if ((!isFinal && !loan.isOpen()) || loan.isNpa() || loan.isChargedOff()
                || !loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            return;
        }
        LoanInterestRecalculationDetails recalculationDetails = loan.getLoanInterestRecalculationDetails();
        if (recalculationDetails != null && recalculationDetails.isCompoundingToBePostedAsTransaction()) {
            return;
        }
        List<LoanTransaction> existingAccruals = retrieveListOfAccrualTransactions(loan);
        LocalDate lastDueDate = loan.getLastLoanRepaymentScheduleInstallment().getDueDate();
        reverseTransactionsAfter(existingAccruals, lastDueDate, addJournal);
        ensureAccrualTransactionMappings(loan);
        if (DateUtils.isAfter(tillDate, lastDueDate)) {
            tillDate = lastDueDate;
        }

        boolean progressiveAccrual = isProgressiveAccrual(loan);
        LocalDate accruedTill = loan.getAccruedTill();
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate accrualDate = isFinal
                ? (progressiveAccrual ? (DateUtils.isBefore(lastDueDate, businessDate) ? lastDueDate : businessDate)
                        : getFinalAccrualTransactionDate(loan))
                : tillDate;
        if (progressiveAccrual && accruedTill != null && !DateUtils.isAfter(tillDate, accruedTill)) {
            if (isFinal) {
                reverseTransactionsAfter(existingAccruals, accrualDate, addJournal);
            } else if (existingAccruals.stream().anyMatch(t -> !t.isReversed() && !DateUtils.isBefore(t.getDateOf(), accrualDate))) {
                return;
            }
        }

        AccrualPeriodsData accrualPeriods = calculateAccrualAmounts(loan, tillDate, periodic);
        boolean mergeTransactions = isFinal || progressiveAccrual;
        MonetaryCurrency currency = loan.getLoanProductRelatedDetail().getCurrency();
        List<LoanTransaction> accrualTransactions = new ArrayList<>();
        Money totalInterestPortion = null;
        LoanTransaction mergeAccrualTransaction = null;
        LoanTransaction mergeAdjustTransaction = null;
        for (AccrualPeriodData period : accrualPeriods.getPeriods()) {
            Money interestAccruable = MathUtil.nullToZero(period.getInterestAccruable(), currency);
            Money interestPortion = MathUtil.minus(interestAccruable, period.getInterestAccrued());
            Money feeAccruable = MathUtil.nullToZero(period.getFeeAccruable(), currency);
            Money feePortion = MathUtil.minus(feeAccruable, period.getFeeAccrued());
            Money penaltyAccruable = MathUtil.nullToZero(period.getPenaltyAccruable(), currency);
            Money penaltyPortion = MathUtil.minus(penaltyAccruable, period.getPenaltyAccrued());
            if (MathUtil.isEmpty(interestPortion) && MathUtil.isEmpty(feePortion) && MathUtil.isEmpty(penaltyPortion)) {
                continue;
            }
            if (mergeTransactions) {
                totalInterestPortion = MathUtil.plus(totalInterestPortion, interestPortion);
                if (progressiveAccrual) {
                    Money feeAdjustmentPortion = MathUtil.negate(feePortion);
                    Money penaltyAdjustmentPortion = MathUtil.negate(penaltyPortion);
                    mergeAdjustTransaction = createOrMergeAccrualTransaction(loan, mergeAdjustTransaction, accrualDate, period,
                            accrualTransactions, null, feeAdjustmentPortion, penaltyAdjustmentPortion, true);
                }
                mergeAccrualTransaction = createOrMergeAccrualTransaction(loan, mergeAccrualTransaction, accrualDate, period,
                        accrualTransactions, null, feePortion, penaltyPortion, false);
            } else {
                LocalDate dueDate = period.getDueDate();
                if (!isFinal && DateUtils.isAfter(dueDate, tillDate) && DateUtils.isBefore(tillDate, accruedTill)) {
                    continue;
                }
                LocalDate periodAccrualDate = DateUtils.isBefore(dueDate, accrualDate) ? dueDate : accrualDate;
                LoanTransaction accrualTransaction = addAccrualTransaction(loan, periodAccrualDate, period, interestPortion, feePortion,
                        penaltyPortion, false);
                if (accrualTransaction != null) {
                    accrualTransactions.add(accrualTransaction);
                }
            }
            LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(period.getInstallmentNumber());
            installment.updateAccrualPortion(interestAccruable, feeAccruable, penaltyAccruable);
        }
        if (mergeTransactions && !MathUtil.isEmpty(totalInterestPortion)) {
            if (progressiveAccrual) {
                Money interestAdjustmentPortion = MathUtil.negate(totalInterestPortion);
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
        ArrayList<Map<String, Object>> newTransactionMapping = new ArrayList<>();
        for (LoanTransaction accrualTransaction : accrualTransactions) {
            accrualTransaction = loanTransactionRepository.saveAndFlush(accrualTransaction);
            if (addJournal) {
                LoanTransactionBusinessEvent businessEvent = accrualTransaction.isAccrual()
                        ? new LoanAccrualTransactionCreatedBusinessEvent(accrualTransaction)
                        : new LoanAccrualAdjustmentTransactionBusinessEvent(accrualTransaction);
                businessEventNotifierService.notifyPostBusinessEvent(businessEvent);
                newTransactionMapping.add(accrualTransaction.toMapData(currency.getCode()));
            }
        }
        if (addJournal) {
            Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(loan, newTransactionMapping);
            this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
        }
    }

    private AccrualPeriodsData calculateAccrualAmounts(@NotNull Loan loan, @NotNull LocalDate tillDate, boolean periodic) {
        boolean chargeOnDueDate = isChargeOnDueDate();
        LoanProductRelatedDetail productDetail = loan.getLoanProductRelatedDetail();
        MonetaryCurrency currency = productDetail.getCurrency();
        LoanScheduleGenerator scheduleGenerator = loanScheduleFactory.create(productDetail.getLoanScheduleType(),
                productDetail.getInterestMethod());
        int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        List<LoanRepaymentScheduleInstallment> installments = getInstallmentsToAccrue(loan, tillDate, periodic);
        AccrualPeriodsData accrualPeriods = AccrualPeriodsData.create(installments, firstInstallmentNumber, currency);
        for (LoanRepaymentScheduleInstallment installment : installments) {
            addInterestAccrual(loan, tillDate, scheduleGenerator, installment, accrualPeriods);
            addChargeAccrual(loan, tillDate, chargeOnDueDate, installment, accrualPeriods);
        }
        return accrualPeriods;
    }

    @NotNull
    private List<LoanRepaymentScheduleInstallment> getInstallmentsToAccrue(@NotNull Loan loan, @NotNull LocalDate tillDate,
            boolean periodic) {
        LocalDate organisationStartDate = this.configurationDomainService.retrieveOrganisationStartDate();
        int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
        return loan.getRepaymentScheduleInstallments(i -> !i.isDownPayment() && (!isChargeOnDueDate()
                || (periodic ? !isBeforePeriod(tillDate, i, i.getInstallmentNumber().equals(firstInstallmentNumber))
                        : isFullPeriod(tillDate, i)))
                && !isAfterPeriod(organisationStartDate, i));
    }

    private void addInterestAccrual(@NotNull Loan loan, @NotNull LocalDate tillDate, LoanScheduleGenerator scheduleGenerator,
            @NotNull LoanRepaymentScheduleInstallment installment, @NotNull AccrualPeriodsData accrualPeriods) {
        if (installment.isAdditional() || installment.isReAged()) {
            return;
        }
        AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        MonetaryCurrency currency = accrualPeriods.getCurrency();
        Money interest = null;
        boolean isFullPeriod = isFullPeriod(tillDate, installment);
        boolean isInPeriod = isInPeriod(tillDate, installment, false);
        if (isFullPeriod) {
            interest = installment.getInterestCharged(currency);
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
            Money waived = isFullPeriod ? installment.getInterestWaived(currency) : MathUtil.plus(transactionWaived, unrecognizedWaived);
            accruable = MathUtil.minusToZero(period.getInterestAmount(), waived);
        }
        period.setInterestAccruable(accruable);
        Money transactionAccrued = MathUtil.toMoney(calcInterestAccruedAmount(installment, accrualPeriods, tillDate), currency);
        period.setTransactionAccrued(transactionAccrued);
        Money accrued = MathUtil.minusToZero(transactionAccrued, transactionWaived);
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
        Predicate<LoanTransaction> transactionPredicate = t -> !t.isReversed() && t.isInterestWaiver()
                && !DateUtils.isAfter(t.getTransactionDate(), toDate);
        Loan loan = installment.getLoan();
        BigDecimal totalUnrecognized = loan.getLoanTransactions().stream().filter(transactionPredicate)
                .map(LoanTransaction::getUnrecognizedIncomePortion).reduce(BigDecimal.ZERO, MathUtil::add);
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
            BigDecimal totalAccrued = loan.getLoanTransactions().stream().filter(ACCRUAL_PREDICATE)
                    .map(t -> t.isAccrual() ? t.getInterestPortion() : MathUtil.negate(t.getInterestPortion()))
                    .reduce(BigDecimal.ZERO, MathUtil::add);
            BigDecimal prevAccrued = accrualPeriods.getPeriods().stream()
                    .filter(p -> p.getInstallmentNumber() < installment.getInstallmentNumber())
                    .map(p -> MathUtil.toBigDecimal(p.getTransactionAccrued())).reduce(BigDecimal.ZERO, MathUtil::add);
            BigDecimal accrued = MathUtil.subtractToZero(totalAccrued, prevAccrued);
            // if this is the current-last period, all the remaining accrued amount is added
            return isInPeriod(tillDate, installment, false) ? accrued : MathUtil.min(installment.getInterestCharged(), accrued, false);
        } else {
            return isFullPeriod(tillDate, installment) ? installment.getInterestAccrued()
                    : loan.getLoanTransactions().stream()
                            .filter(t -> !t.isReversed() && t.isAccrual() && isInPeriod(t.getTransactionDate(), installment, false))
                            .map(LoanTransaction::getInterestPortion).reduce(BigDecimal.ZERO, MathUtil::add);
        }
    }

    private void addChargeAccrual(@NotNull Loan loan, @NotNull LocalDate tillDate, boolean chargeOnDueDate,
            @NotNull LoanRepaymentScheduleInstallment installment, @NotNull AccrualPeriodsData accrualPeriods) {
        AccrualPeriodData period = accrualPeriods.getPeriodByInstallmentNumber(installment.getInstallmentNumber());
        LocalDate dueDate = installment.getDueDate();
        List<LoanCharge> loanCharges = loan
                .getLoanCharges(lc -> !lc.isDueAtDisbursement() && (lc.isInstalmentFee() ? !DateUtils.isBefore(tillDate, dueDate)
                        : isChargeDue(lc, tillDate, chargeOnDueDate, installment, period.isFirstPeriod())));
        for (LoanCharge loanCharge : loanCharges) {
            addChargeAccrual(loanCharge, tillDate, chargeOnDueDate, installment, accrualPeriods);
        }
    }

    private void addChargeAccrual(@NotNull LoanCharge loanCharge, @NotNull LocalDate tillDate, boolean chargeOnDueDate,
            @NotNull LoanRepaymentScheduleInstallment installment, @NotNull AccrualPeriodsData accrualPeriods) {
        MonetaryCurrency currency = accrualPeriods.getCurrency();
        Integer firstInstallmentNumber = accrualPeriods.getFirstInstallmentNumber();
        boolean installmentFee = loanCharge.isInstalmentFee();
        LoanRepaymentScheduleInstallment dueInstallment = (installmentFee || chargeOnDueDate) ? installment
                : loanCharge.getLoan().getRepaymentScheduleInstallment(
                        i -> isInPeriod(loanCharge.getDueDate(), i, i.getInstallmentNumber().equals(firstInstallmentNumber)));
        AccrualPeriodData duePeriod = accrualPeriods.getPeriodByInstallmentNumber(dueInstallment.getInstallmentNumber());
        boolean isFullPeriod = isFullPeriod(tillDate, dueInstallment);

        Money chargeAmount;
        Money waived;
        Collection<LoanChargePaidBy> paidBys;
        Long installmentChargeId = null;
        if (installmentFee) {
            LoanInstallmentCharge installmentCharge = loanCharge.getInstallmentLoanCharge(dueInstallment.getInstallmentNumber());
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
        AccrualChargeData chargeData = new AccrualChargeData(loanCharge.getId(), installmentChargeId, loanCharge.isPenaltyCharge())
                .setChargeAmount(chargeAmount);
        chargeData.setChargeAccruable(MathUtil.minusToZero(chargeAmount, waived));

        Money unrecognizedWaived = MathUtil.toMoney(calcChargeUnrecognizedWaivedAmount(paidBys, tillDate), currency);
        Money transactionWaived = MathUtil.minusToZero(waived, unrecognizedWaived);
        Money transactionAccrued = MathUtil.toMoney(calcChargeAccruedAmount(paidBys), currency);
        chargeData.setTransactionAccrued(transactionAccrued);
        chargeData.setChargeAccrued(MathUtil.minusToZero(transactionAccrued, transactionWaived));

        duePeriod.addCharge(chargeData);
    }

    @NotNull
    private BigDecimal calcChargeWaivedAmount(@NotNull Collection<LoanChargePaidBy> loanChargePaidBy, @NotNull LocalDate tillDate) {
        return loanChargePaidBy.stream().filter(pb -> {
            LoanTransaction t = pb.getLoanTransaction();
            return !t.isReversed() && t.isWaiveCharge() && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        }).map(LoanChargePaidBy::getAmount).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    @NotNull
    private BigDecimal calcChargeUnrecognizedWaivedAmount(@NotNull Collection<LoanChargePaidBy> loanChargePaidBy,
            @NotNull LocalDate tillDate) {
        return loanChargePaidBy.stream().filter(pb -> {
            LoanTransaction t = pb.getLoanTransaction();
            return !t.isReversed() && t.isWaiveCharge() && !DateUtils.isAfter(t.getTransactionDate(), tillDate);
        }).map(pb -> pb.getLoanTransaction().getUnrecognizedIncomePortion()).reduce(BigDecimal.ZERO, MathUtil::add);
    }

    @NotNull
    private BigDecimal calcChargeAccruedAmount(@NotNull Collection<LoanChargePaidBy> loanChargePaidBy) {
        return loanChargePaidBy.stream().filter(pb -> ACCRUAL_PREDICATE.test(pb.getLoanTransaction()))
                .map(pb -> pb.getLoanTransaction().isAccrual() ? pb.getAmount() : MathUtil.negate(pb.getAmount()))
                .reduce(BigDecimal.ZERO, MathUtil::add);
    }

    private boolean isChargeDue(@NotNull LoanCharge loanCharge, @NotNull LocalDate tillDate, boolean chargeOnDueDate,
            LoanRepaymentScheduleInstallment installment, boolean isFirstPeriod) {
        LocalDate fromDate = installment.getFromDate();
        LocalDate dueDate = installment.getDueDate();
        LocalDate toDate = DateUtils.isBefore(dueDate, tillDate) ? dueDate : tillDate;
        return chargeOnDueDate ? loanCharge.isDueInPeriod(fromDate, toDate, isFirstPeriod)
                : isInPeriod(loanCharge.getSubmittedOnDate(), fromDate, toDate, isFirstPeriod);
    }

    private LoanTransaction createOrMergeAccrualTransaction(@NotNull Loan loan, LoanTransaction transaction, LocalDate transactionDate,
            AccrualPeriodData accrualPeriod, List<LoanTransaction> accrualTransactions, Money interest, Money fee, Money penalty,
            boolean adjustment) {
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
        loan.addLoanTransaction(transaction);

        // update repayment schedule portions
        addTransactionMappings(transaction, accrualPeriod, adjustment);
        return transaction;
    }

    private void mergeAccrualTransaction(@NotNull LoanTransaction transaction, AccrualPeriodData accrualPeriod, Money interestPortion,
            Money feePortion, Money penaltyPortion, boolean adjustment) {
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

    private void addTransactionMappings(@NotNull LoanTransaction transaction, AccrualPeriodData accrualPeriod, boolean adjustment) {
        if (accrualPeriod == null) {
            return;
        }
        Loan loan = transaction.getLoan();
        Integer installmentNumber = accrualPeriod.getInstallmentNumber();
        LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(installmentNumber);

        // add charges paid by mappings
        addPaidByMappings(transaction, installment, accrualPeriod, adjustment);
    }

    private void addPaidByMappings(@NotNull LoanTransaction transaction, LoanRepaymentScheduleInstallment installment,
            AccrualPeriodData accrualPeriod, boolean adjustment) {
        Loan loan = installment.getLoan();
        MonetaryCurrency currency = loan.getCurrency();
        for (AccrualChargeData accrualCharge : accrualPeriod.getCharges()) {
            Money chargeAccruable = MathUtil.nullToZero(accrualCharge.getChargeAccruable(), currency);
            Money chargePortion = MathUtil.minus(chargeAccruable, accrualCharge.getChargeAccrued());
            chargePortion = MathUtil.negativeToZero(adjustment ? MathUtil.negate(chargePortion) : chargePortion);
            if (MathUtil.isEmpty(chargePortion)) {
                continue;
            }
            BigDecimal chargeAmount = MathUtil.toBigDecimal(chargePortion);
            LoanCharge loanCharge = loan.fetchLoanChargesById(accrualCharge.getLoanChargeId());
            LoanChargePaidBy paidBy = new LoanChargePaidBy(transaction, loanCharge, chargeAmount, installment.getInstallmentNumber());
            loanCharge.getLoanChargePaidBySet().add(paidBy);
            transaction.getLoanChargesPaid().add(paidBy);
            Long installmentChargeId = accrualCharge.getLoanInstallmentChargeId();
            if (installmentChargeId != null) {
                LoanInstallmentCharge installmentCharge = new LoanInstallmentCharge(chargeAmount, loanCharge, installment);
                loanCharge.getLoanInstallmentCharge().add(installmentCharge);
                installment.getInstallmentCharges().add(installmentCharge);
            }
        }
    }

    private boolean isFullPeriod(@NotNull LocalDate tillDate, @NotNull LoanRepaymentScheduleInstallment installment) {
        return isAfterPeriod(tillDate, installment) || DateUtils.isEqual(tillDate, installment.getDueDate());
    }

    // ReprocessAccruals

    private void reprocessPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions) {
        if (loan.isChargedOff()) {
            return;
        }
        ensureAccrualTransactionMappings(loan);
        LoanRepaymentScheduleInstallment lastInstallment = loan.getLastLoanRepaymentScheduleInstallment();
        LocalDate lastDueDate = lastInstallment.getDueDate();
        if (isProgressiveAccrual(loan)) {
            AccrualPeriodsData accrualPeriods = calculateAccrualAmounts(loan, lastDueDate, true);
            for (AccrualPeriodData period : accrualPeriods.getPeriods()) {
                Money interestAccrued = period.getTransactionAccrued();
                Money feeAccrued = period.getFeeTransactionAccrued();
                Money penaltyAccrued = period.getPenaltyTransactionAccrued();
                LoanRepaymentScheduleInstallment installment = loan.fetchRepaymentScheduleInstallment(period.getInstallmentNumber());
                installment.updateAccrualPortion(interestAccrued, feeAccrued, penaltyAccrued);
            }
        } else {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            boolean isBasedOnSubmittedOnDate = !isChargeOnDueDate();
            for (LoanRepaymentScheduleInstallment installment : installments) {
                checkAndUpdateAccrualsForInstallment(loan, accrualTransactions, installments, isBasedOnSubmittedOnDate, installment);
            }
        }
        // reverse accruals after last installment
        reverseTransactionsAfter(accrualTransactions, lastDueDate, false);
    }

    private void reprocessNonPeriodicAccruals(Loan loan, final List<LoanTransaction> accrualTransactions) {
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
                    if (isExternalIdAutoGenerationEnabled) {
                        externalId = ExternalId.generate();
                    }
                    final LoanTransaction interestAccrualTransaction = LoanTransaction.accrueInterest(loan.getOffice(), loan,
                            interestApplied, loan.getDisbursementDate(), externalId);
                    loan.addLoanTransaction(interestAccrualTransaction);
                }
            } else {
                Set<LoanChargePaidBy> chargePaidBies = accrualTransaction.getLoanChargesPaid();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    Money chargeAmount = loanCharge.getAmount(loan.getCurrency());
                    if (chargeAmount.isNotEqualTo(accrualTransaction.getAmount(loan.getCurrency()))) {
                        accrualTransaction.reverse();
                        loan.handleChargeAppliedTransaction(loanCharge, accrualTransaction.getTransactionDate());
                    }
                }
            }
        }
    }

    private void checkAndUpdateAccrualsForInstallment(Loan loan, List<LoanTransaction> accrualTransactions,
            List<LoanRepaymentScheduleInstallment> installments, boolean isBasedOnSubmittedOnDate,
            LoanRepaymentScheduleInstallment installment) {
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
            LocalDate lastCompoundingDate, LoanTransaction existingIncomeTransaction, LoanTransaction existingAccrualTransaction) {
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

        createUpdateIncomePostingTransaction(loan, compoundingDetail, existingIncomeTransaction, interest, fee, penalties, externalId);
        createUpdateAccrualTransaction(loan, compoundingDetail, existingAccrualTransaction, interest, fee, penalties, feeDetails,
                externalId);
        loan.updateLoanOutstandingBalances();
    }

    private void createUpdateIncomePostingTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            LoanTransaction existingIncomeTransaction, BigDecimal interest, BigDecimal fee, BigDecimal penalties, ExternalId externalId) {
        if (existingIncomeTransaction == null) {
            LoanTransaction transaction = LoanTransaction.incomePosting(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            loan.addLoanTransaction(transaction);
        } else if (existingIncomeTransaction.getAmount(loan.getCurrency()).getAmount().compareTo(compoundingDetail.getAmount()) != 0) {
            existingIncomeTransaction.reverse();
            LoanTransaction transaction = LoanTransaction.incomePosting(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                    compoundingDetail.getAmount(), interest, fee, penalties, externalId);
            loan.addLoanTransaction(transaction);
        }
    }

    private void createUpdateAccrualTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            LoanTransaction existingAccrualTransaction, BigDecimal interest, BigDecimal fee, BigDecimal penalties,
            HashMap<String, Object> feeDetails, ExternalId externalId) {
        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            if (existingAccrualTransaction == null
                    || !MathUtil.isEqualTo(existingAccrualTransaction.getAmount(), compoundingDetail.getAmount())) {
                if (existingAccrualTransaction != null) {
                    existingAccrualTransaction.reverse();
                }
                LoanTransaction accrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(loan, accrual, feeDetails, null);
                loan.addLoanTransaction(accrual);
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
            reverseTransactionsOnOrAfter(retrieveListOfIncomePostingTransactions(loan), closedDate, false);
            reverseTransactionsOnOrAfter(retrieveListOfAccrualTransactions(loan), closedDate, false);

            HashMap<String, BigDecimal> cumulativeIncomeFromInstallments = new HashMap<>();
            determineCumulativeIncomeFromInstallments(loan, cumulativeIncomeFromInstallments);
            HashMap<String, BigDecimal> cumulativeIncomeFromIncomePosting = new HashMap<>();
            determineCumulativeIncomeDetails(loan, retrieveListOfIncomePostingTransactions(loan), cumulativeIncomeFromIncomePosting);

            BigDecimal interestToPost = cumulativeIncomeFromInstallments.get(Loan.INTEREST)
                    .subtract(cumulativeIncomeFromIncomePosting.get(Loan.INTEREST));
            BigDecimal feeToPost = cumulativeIncomeFromInstallments.get(Loan.FEE).subtract(cumulativeIncomeFromIncomePosting.get(Loan.FEE));
            BigDecimal penaltyToPost = cumulativeIncomeFromInstallments.get(Loan.PENALTY)
                    .subtract(cumulativeIncomeFromIncomePosting.get(Loan.PENALTY));
            BigDecimal amountToPost = interestToPost.add(feeToPost).add(penaltyToPost);

            createIncomePostingAndAccrualTransactionOnLoanClosure(loan, closedDate, interestToPost, feeToPost, penaltyToPost, amountToPost);
        }
        loan.updateLoanOutstandingBalances();
    }

    private void determineCumulativeIncomeFromInstallments(Loan loan, HashMap<String, BigDecimal> cumulativeIncomeFromInstallments) {
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

    private void determineCumulativeIncomeDetails(Loan loan, List<LoanTransaction> transactions,
            HashMap<String, BigDecimal> incomeDetailsMap) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal fee = BigDecimal.ZERO;
        BigDecimal penalty = BigDecimal.ZERO;
        for (LoanTransaction transaction : transactions) {
            interest = interest.add(transaction.getInterestPortion(loan.getCurrency()).getAmount());
            fee = fee.add(transaction.getFeeChargesPortion(loan.getCurrency()).getAmount());
            penalty = penalty.add(transaction.getPenaltyChargesPortion(loan.getCurrency()).getAmount());
        }
        incomeDetailsMap.put(Loan.INTEREST, interest);
        incomeDetailsMap.put(Loan.FEE, fee);
        incomeDetailsMap.put(Loan.PENALTY, penalty);
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
        loan.addLoanTransaction(finalIncomeTransaction);

        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            List<LoanTransaction> updatedAccrualTransactions = retrieveListOfAccrualTransactions(loan);
            LocalDate lastAccruedDate = loan.getDisbursementDate();
            if (!updatedAccrualTransactions.isEmpty()) {
                lastAccruedDate = updatedAccrualTransactions.get(updatedAccrualTransactions.size() - 1).getTransactionDate();
            }
            HashMap<String, Object> feeDetails = new HashMap<>();
            determineFeeDetails(loan, lastAccruedDate, closedDate, feeDetails);
            if (isExternalIdAutoGenerationEnabled) {
                externalId = ExternalId.generate();
            }
            LoanTransaction finalAccrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), closedDate, amountToPost,
                    interestToPost, feeToPost, penaltyToPost, externalId);
            updateLoanChargesPaidBy(loan, finalAccrual, feeDetails, null);
            loan.addLoanTransaction(finalAccrual);
        }
    }

    // LoanForClosure

    private void determineReceivableIncomeForeClosure(Loan loan, final LocalDate tillDate, Map<String, Object> incomeDetails) {
        MonetaryCurrency currency = loan.getCurrency();
        Money receivableInterest = Money.zero(currency);
        Money receivableFee = Money.zero(currency);
        Money receivablePenalty = Money.zero(currency);
        for (final LoanTransaction transaction : loan.getLoanTransactions()) {
            if (transaction.isNotReversed() && !transaction.isRepaymentAtDisbursement() && !transaction.isDisbursement()
                    && !DateUtils.isAfter(transaction.getTransactionDate(), tillDate)) {
                if (transaction.isAccrual()) {
                    receivableInterest = receivableInterest.plus(transaction.getInterestPortion(currency));
                    receivableFee = receivableFee.plus(transaction.getFeeChargesPortion(currency));
                    receivablePenalty = receivablePenalty.plus(transaction.getPenaltyChargesPortion(currency));
                } else if (transaction.isRepaymentLikeType() || transaction.isChargePayment() || transaction.isAccrualAdjustment()) {
                    receivableInterest = receivableInterest.minus(transaction.getInterestPortion(currency));
                    receivableFee = receivableFee.minus(transaction.getFeeChargesPortion(currency));
                    receivablePenalty = receivablePenalty.minus(transaction.getPenaltyChargesPortion(currency));
                }
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

        incomeDetails.put(Loan.INTEREST, receivableInterest);
        incomeDetails.put(Loan.FEE, receivableFee);
        incomeDetails.put(Loan.PENALTIES, receivablePenalty);
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
        loan.addLoanTransaction(accrualTransaction);
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

    // Utility

    private Map<String, Object> deriveAccountingBridgeData(@NotNull Loan loan, List<Map<String, Object>> newLoanTransactions) {
        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", loan.getId());
        accountingBridgeData.put("loanProductId", loan.getLoanProduct().getId());
        accountingBridgeData.put("officeId", loan.getOfficeId());
        accountingBridgeData.put("currencyCode", loan.getCurrencyCode());
        accountingBridgeData.put("cashBasedAccountingEnabled", loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", loan.isUpfrontAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", loan.isPeriodicAccrualAccountingEnabledOnLoanProduct());
        accountingBridgeData.put("isAccountTransfer", false);
        accountingBridgeData.put("isChargeOff", false);
        accountingBridgeData.put("isFraud", false);
        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                existingReversedTransactionIds, isAccountTransfer);
        journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private void ensureAccrualTransactionMappings(Loan loan) {
        boolean chargeOnDueDate = isChargeOnDueDate();
        List<LoanTransaction> transactions = retrieveListOfAccrualTransactions(loan);
        for (LoanTransaction transaction : transactions) {
            if (!MathUtil.isEmpty(transaction.getFeeChargesPortion()) || !MathUtil.isEmpty(transaction.getPenaltyChargesPortion())) {
                int firstInstallmentNumber = fetchFirstNormalInstallmentNumber(loan.getRepaymentScheduleInstallments());
                for (LoanChargePaidBy paidBy : transaction.getLoanChargesPaid()) {
                    LoanCharge loanCharge = paidBy.getLoanCharge();
                    if (paidBy.getInstallmentNumber() == null) {
                        LocalDate chargeDate = (chargeOnDueDate || loanCharge.isInstalmentFee()) ? transaction.getTransactionDate()
                                : loanCharge.getDueDate();
                        LoanRepaymentScheduleInstallment installment = loan.getRepaymentScheduleInstallment(
                                i -> isInPeriod(chargeDate, i, i.getInstallmentNumber().equals(firstInstallmentNumber)));
                        paidBy.setInstallmentNumber(installment.getInstallmentNumber());
                    }
                }
            }
        }
    }

    private List<LoanTransaction> retrieveListOfAccrualTransactions(Loan loan) {
        return loan.getLoanTransactions().stream().filter(ACCRUAL_PREDICATE).sorted(LoanTransactionComparator.INSTANCE)
                .collect(Collectors.toList());
    }

    private List<LoanTransaction> retrieveListOfIncomePostingTransactions(Loan loan) {
        return loan.getLoanTransactions().stream() //
                .filter(transaction -> transaction.isNotReversed() && transaction.isIncomePosting()) //
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
    }

    private LoanTransaction getTransactionForDate(List<LoanTransaction> transactions, LocalDate effectiveDate) {
        for (LoanTransaction loanTransaction : transactions) {
            if (DateUtils.isEqual(effectiveDate, loanTransaction.getTransactionDate())) {
                return loanTransaction;
            }
        }
        return null;
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

    private boolean reverseTransactionsAfter(List<LoanTransaction> accrualTransactions, LocalDate effectiveDate, boolean addEvent) {
        boolean reversed = false;
        for (LoanTransaction accrualTransaction : accrualTransactions) {
            if (!accrualTransaction.isReversed() && DateUtils.isAfter(accrualTransaction.getTransactionDate(), effectiveDate)) {
                reverseAccrual(accrualTransaction, addEvent);
                reversed = true;
            }
        }
        return reversed;
    }

    private boolean reverseTransactionsOnOrAfter(List<LoanTransaction> accrualTransactions, LocalDate date, boolean addEvent) {
        boolean reversed = false;
        for (LoanTransaction accrualTransaction : accrualTransactions) {
            if (!accrualTransaction.isReversed() && !DateUtils.isBefore(accrualTransaction.getTransactionDate(), date)) {
                reverseAccrual(accrualTransaction, addEvent);
                reversed = true;
            }
        }
        return reversed;
    }

    private void reverseAccrual(LoanTransaction transaction, boolean addEvent) {
        transaction.reverse();
        if (addEvent) {
            LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(transaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
        }
    }

    private LocalDate getFinalAccrualTransactionDate(Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }

    public boolean isProgressiveAccrual(@NotNull Loan loan) {
        return loan.isProgressiveSchedule();
    }

    private void setSetHelpers(Loan loan) {
        loan.setHelpers(null, transactionProcessorFactory);
    }
}
