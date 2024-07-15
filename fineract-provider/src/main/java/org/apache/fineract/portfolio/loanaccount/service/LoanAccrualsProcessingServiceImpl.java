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

import static org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction.accrueTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanAccrualTransactionCreatedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargePaidBy;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInstallmentCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanInterestRecalcualtionAdditionalDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleProcessingWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionComparator;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualsProcessingServiceImpl implements LoanAccrualsProcessingService {

    private static final String ACCRUAL_ON_CHARGE_DUE_DATE = "due-date";
    private static final String ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE = "submitted-date";
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final PlatformSecurityContext context;
    private final LoanRepository loanRepository;
    private final OfficeRepository officeRepository;
    private final LoanChargeRepository loanChargeRepository;

    /**
     * method adds accrual for batch job "Add Periodic Accrual Transactions" and add accruals api for Loan
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(final LocalDate tillDate) throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList = this.loanReadPlatformService
                .retrievePeriodicAccrualData(tillDate);
        addPeriodicAccruals(tillDate, loanScheduleAccrualDataList);
    }

    /**
     * method adds accrual for Loan COB business step
     */
    @Override
    @Transactional
    public void addPeriodicAccruals(final LocalDate tillDate, Loan loan) throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList = this.loanReadPlatformService.retrievePeriodicAccrualData(tillDate,
                loan);
        addPeriodicAccruals(tillDate, loanScheduleAccrualDataList);
    }

    private void addPeriodicAccruals(final LocalDate tillDate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDataList)
            throws JobExecutionException {
        Map<Long, Collection<LoanScheduleAccrualData>> loanDataMap = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDataList) {
            if (loanDataMap.containsKey(accrualData.getLoanId())) {
                loanDataMap.get(accrualData.getLoanId()).add(accrualData);
            } else {
                Collection<LoanScheduleAccrualData> accrualDataList = new ArrayList<>();
                accrualDataList.add(accrualData);
                loanDataMap.put(accrualData.getLoanId(), accrualDataList);
            }
        }

        List<Throwable> errors = new ArrayList<>();
        for (Map.Entry<Long, Collection<LoanScheduleAccrualData>> mapEntry : loanDataMap.entrySet()) {
            try {
                addPeriodicAccruals(tillDate, mapEntry.getKey(), mapEntry.getValue());
            } catch (Exception e) {
                log.error("Failed to add accrual transaction for loan {}", mapEntry.getKey(), e);
                errors.add(e);
            }
        }
        if (!errors.isEmpty()) {
            throw new JobExecutionException(errors);
        }
    }

    /**
     * method adds accrual for batch job "Add Accrual Transactions"
     */

    @Override
    @Transactional
    public void addAccrualAccounting(final Long loanId, final Collection<LoanScheduleAccrualData> loanScheduleAccrualData) {
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccrual(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTransactionData = new ArrayList<>(1);

        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualData) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTransactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }
            updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
            updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, accrualData.getDueDateAsLocaldate());
            calculateFinalAccrualsForScheduleAndAddAccrualAccounting(accrualData);
        }
    }

    private void addPeriodicAccruals(final LocalDate tillDate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualData) {
        boolean firstTime = true;
        LocalDate accruedTill = null;
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccrual(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTransactionData = new ArrayList<>(1);
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualData) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTransactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }

            if (DateUtils.isAfter(accrualData.getDueDateAsLocaldate(), tillDate)) {
                if (accruedTill == null || firstTime) {
                    accruedTill = accrualData.getAccruedTill();
                    firstTime = false;
                }
                if (accruedTill == null || DateUtils.isBefore(accruedTill, tillDate)) {
                    updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), tillDate);
                    updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, tillDate);
                    calculateFinalAccrualsForScheduleTillSpecificDateAndAddAccrualAccounting(tillDate, accrualData);
                }
            } else {
                updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
                updateInterestIncome(accrualData, loanWaiverTransactionData, loanWaiverScheduleData, tillDate);
                calculateFinalAccrualsForScheduleAndAddAccrualAccounting(accrualData);
                accruedTill = accrualData.getDueDateAsLocaldate();
            }
        }
    }

    @Transactional
    @Override
    public void addIncomeAndAccrualTransactions(Long loanId) throws LoanNotFoundException {
        if (loanId != null) {
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            if (loan == null) {
                throw new LoanNotFoundException(loanId);
            }
            final List<Long> existingTransactionIds = new ArrayList<>(loan.findExistingTransactionIds());
            final List<Long> existingReversedTransactionIds = new ArrayList<>(loan.findExistingReversedTransactionIds());
            processIncomePostingAndAccruals(loan);
            this.loanRepositoryWrapper.saveAndFlush(loan);
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
            loanAccrualTransactionBusinessEventService.raiseBusinessEventForAccrualTransactions(loan, existingTransactionIds);
        }
    }

    /**
     * method updates accrual derived fields on installments and reverse the unprocessed transactions for loan
     * reschedule
     */
    @Override
    public void reprocessExistingAccruals(Loan loan) {
        Collection<LoanTransaction> accruals = retrieveListOfAccrualTransactions(loan);
        if (!accruals.isEmpty()) {
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                reprocessPeriodicAccruals(loan, accruals);
            } else if (loan.isNoneOrCashOrUpfrontAccrualAccountingEnabledOnLoanProduct()) {
                reprocessNonPeriodicAccruals(loan, accruals);
            }
        }

    }

    /**
     * method calculates accruals for loan with interest recalculation on loan schedule when interest is recalculated
     */
    @Override
    @Transactional
    public void processAccrualsForInterestRecalculation(Loan loan, boolean isInterestRecalculationEnabled) {
        LocalDate accruedTill = loan.getAccruedTill();
        if (!loan.isPeriodicAccrualAccountingEnabledOnLoanProduct() || !isInterestRecalculationEnabled || accruedTill == null
                || loan.isNpa() || !loan.getStatus().isActive() || loan.isChargedOff()) {
            return;
        }

        Collection<LoanScheduleAccrualData> loanScheduleAccrualList = new ArrayList<>();
        accruedTill = createLoanScheduleAccrualDataList(loan, accruedTill, loanScheduleAccrualList);

        if (!loanScheduleAccrualList.isEmpty()) {
            try {
                addPeriodicAccruals(accruedTill, loanScheduleAccrualList);
            } catch (MultiException e) {
                String globalisationMessageCode = "error.msg.accrual.exception";
                throw new GeneralPlatformDomainRuleException(globalisationMessageCode, e.getMessage(), e);
            }
        }

    }

    /**
     * method calculates accruals for loan with interest recalculation and compounding to be posted as income
     */
    @Override
    public void processIncomePostingAndAccruals(Loan loan) {
        if (loan.getLoanInterestRecalculationDetails() != null
                && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction()) {
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
            LoanRepaymentScheduleInstallment lastInstallment = LoanRepaymentScheduleInstallment
                    .getLastNonDownPaymentInstallment(installments);
            reverseTransactionsPostEffectiveDate(incomeTransactions, lastInstallment.getDueDate());
            reverseTransactionsPostEffectiveDate(accrualTransactions, lastInstallment.getDueDate());
        }
    }

    /**
     * method calculates accruals for loan on loan closure
     */

    @Override
    public void processAccrualsForLoanClosure(Loan loan) {
        // check and process accruals for loan WITHOUT interest recalculation details and compounding posted as income
        processAccrualTransactionsOnLoanClosure(loan);

        // check and process accruals for loan WITH interest recalculation details and compounding posted as income
        processIncomeAndAccrualTransactionOnLoanClosure(loan);

    }

    /**
     * method calculates accruals for loan on loan fore closure
     */

    @Override
    public void processAccrualsForLoanForeClosure(Loan loan, LocalDate foreClosureDate,
            Collection<LoanTransaction> newAccrualTransactions) {
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                && (loan.getAccruedTill() == null || !DateUtils.isEqual(foreClosureDate, loan.getAccruedTill()))) {
            final LoanRepaymentScheduleInstallment foreCloseDetail = loan.fetchLoanForeclosureDetail(foreClosureDate);
            MonetaryCurrency currency = loan.getCurrency();
            reverseTransactionsPostEffectiveDate(retrieveListOfAccrualTransactions(loan), foreClosureDate);

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

    private void calculateFinalAccrualsForScheduleTillSpecificDateAndAddAccrualAccounting(final LocalDate tillDate,
            final LoanScheduleAccrualData accrualData) {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal feePortion = accrualData.getDueDateFeeIncome();
        BigDecimal penaltyPortion = accrualData.getDueDatePenaltyIncome();
        BigDecimal interestPortion = getInterestAccruedTillDate(tillDate, accrualData);

        BigDecimal totalAccInterest = accrualData.getAccruedInterestIncome();
        BigDecimal totalAccPenalty = accrualData.getAccruedPenaltyIncome();
        BigDecimal totalCreditedPenalty = accrualData.getCreditedPenalty();
        BigDecimal totalAccFee = accrualData.getAccruedFeeIncome();
        BigDecimal totalCreditedFee = accrualData.getCreditedFee();

        // interest
        if (totalAccInterest == null) {
            totalAccInterest = BigDecimal.ZERO;
        }
        interestPortion = interestPortion.subtract(totalAccInterest);
        amount = amount.add(interestPortion);
        totalAccInterest = totalAccInterest.add(interestPortion);
        if (interestPortion.compareTo(BigDecimal.ZERO) == 0) {
            interestPortion = null;
        }

        // fee
        if (feePortion != null) {
            if (totalAccFee == null) {
                totalAccFee = BigDecimal.ZERO;
            }
            if (totalCreditedFee == null) {
                totalCreditedFee = BigDecimal.ZERO;
            }
            feePortion = feePortion.subtract(totalAccFee).subtract(totalCreditedFee);
            amount = amount.add(feePortion);
            totalAccFee = totalAccFee.add(feePortion);
            if (feePortion.compareTo(BigDecimal.ZERO) == 0) {
                feePortion = null;
            }
        }

        // penalty
        if (penaltyPortion != null) {
            if (totalAccPenalty == null) {
                totalAccPenalty = BigDecimal.ZERO;
            }
            if (totalCreditedPenalty == null) {
                totalCreditedPenalty = BigDecimal.ZERO;
            }
            penaltyPortion = penaltyPortion.subtract(totalAccPenalty).subtract(totalCreditedPenalty);
            amount = amount.add(penaltyPortion);
            totalAccPenalty = totalAccPenalty.add(penaltyPortion);
            if (penaltyPortion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyPortion = null;
            }
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            addAccrualAccounting(accrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee, penaltyPortion,
                    totalAccPenalty, tillDate);
        }
    }

    private BigDecimal getInterestAccruedTillDate(LocalDate tillDate, LoanScheduleAccrualData accrualData) {
        BigDecimal interestPortion;
        LocalDate interestStartDate = accrualData.getFromDateAsLocaldate();
        if (DateUtils.isBefore(accrualData.getFromDateAsLocaldate(), accrualData.getInterestCalculatedFrom())) {
            if (DateUtils.isBefore(accrualData.getInterestCalculatedFrom(), accrualData.getDueDateAsLocaldate())) {
                interestStartDate = accrualData.getInterestCalculatedFrom();
            } else {
                interestStartDate = accrualData.getDueDateAsLocaldate();
            }
        }

        int totalNumberOfDays = Math.toIntExact(ChronoUnit.DAYS.between(interestStartDate, accrualData.getDueDateAsLocaldate()));
        LocalDate startDate = accrualData.getFromDateAsLocaldate();
        if (DateUtils.isBefore(startDate, accrualData.getInterestCalculatedFrom())) {
            if (DateUtils.isBefore(accrualData.getInterestCalculatedFrom(), tillDate)) {
                startDate = accrualData.getInterestCalculatedFrom();
            } else {
                startDate = tillDate;
            }
        }
        int daysToBeAccrued = Math.toIntExact(ChronoUnit.DAYS.between(startDate, tillDate));
        double interestPerDay = accrualData.getAccruableIncome().doubleValue() / totalNumberOfDays;

        if (daysToBeAccrued >= totalNumberOfDays) {
            interestPortion = accrualData.getAccruableIncome();
        } else {
            interestPortion = BigDecimal.valueOf(interestPerDay * daysToBeAccrued);
        }
        interestPortion = interestPortion.setScale(accrualData.getCurrencyData().getDecimalPlaces(), MoneyHelper.getRoundingMode());
        return interestPortion;
    }

    private void calculateFinalAccrualsForScheduleAndAddAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData) {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestPortion = null;
        BigDecimal totalAccInterest = null;

        // interest
        if (scheduleAccrualData.getAccruableIncome() != null) {
            interestPortion = scheduleAccrualData.getAccruableIncome();
            totalAccInterest = interestPortion;
            if (scheduleAccrualData.getAccruedInterestIncome() != null) {
                interestPortion = interestPortion.subtract(scheduleAccrualData.getAccruedInterestIncome());
            }
            amount = amount.add(interestPortion);
            if (interestPortion.compareTo(BigDecimal.ZERO) == 0) {
                interestPortion = null;
            }
        }

        // fee
        BigDecimal feePortion = null;
        BigDecimal totalAccFee = null;
        if (scheduleAccrualData.getDueDateFeeIncome() != null) {
            feePortion = scheduleAccrualData.getDueDateFeeIncome();
            totalAccFee = feePortion;
            if (scheduleAccrualData.getAccruedFeeIncome() != null) {
                feePortion = feePortion.subtract(scheduleAccrualData.getAccruedFeeIncome());
            }
            if (scheduleAccrualData.getCreditedFee() != null) {
                feePortion = feePortion.subtract(scheduleAccrualData.getCreditedFee());
            }
            amount = amount.add(feePortion);
            if (feePortion.compareTo(BigDecimal.ZERO) == 0) {
                feePortion = null;
            }
        }

        // penalty
        BigDecimal penaltyPortion = null;
        BigDecimal totalAccPenalty = null;
        if (scheduleAccrualData.getDueDatePenaltyIncome() != null) {
            penaltyPortion = scheduleAccrualData.getDueDatePenaltyIncome();
            totalAccPenalty = penaltyPortion;
            if (scheduleAccrualData.getAccruedPenaltyIncome() != null) {
                penaltyPortion = penaltyPortion.subtract(scheduleAccrualData.getAccruedPenaltyIncome());
            }
            if (scheduleAccrualData.getCreditedPenalty() != null) {
                penaltyPortion = penaltyPortion.subtract(scheduleAccrualData.getCreditedPenalty());
            }
            amount = amount.add(penaltyPortion);
            if (penaltyPortion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyPortion = null;
            }
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            final String chargeAccrualDateCriteria = configurationDomainService.getAccrualDateConfigForCharge();
            if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_DUE_DATE)) {
                addAccrualAccounting(scheduleAccrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee,
                        penaltyPortion, totalAccPenalty, scheduleAccrualData.getDueDateAsLocaldate());
            } else if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE)) {
                addAccrualAccounting(scheduleAccrualData, amount, interestPortion, totalAccInterest, feePortion, totalAccFee,
                        penaltyPortion, totalAccPenalty, DateUtils.getBusinessLocalDate());
            }
        }
    }

    private void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestPortion,
            BigDecimal totalAccInterest, BigDecimal feePortion, BigDecimal totalAccFee, BigDecimal penaltyPortion,
            BigDecimal totalAccPenalty, final LocalDate accruedTill) throws DataAccessException {

        AppUser user = context.authenticatedUser();
        Loan loan = loanRepository.getReferenceById(scheduleAccrualData.getLoanId());
        Office office = officeRepository.getReferenceById(scheduleAccrualData.getOfficeId());
        MonetaryCurrency currency = loan.getCurrency();

        // create accrual Transaction
        LoanTransaction loanTransaction = accrueTransaction(loan, office, accruedTill, amount, interestPortion, feePortion, penaltyPortion,
                externalIdFactory.create());

        // update charges paid by
        Map<LoanChargeData, BigDecimal> applicableCharges = scheduleAccrualData.getApplicableCharges();

        for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
            LoanChargeData chargeData = entry.getKey();
            //
            LoanCharge loanCharge = loanChargeRepository.getReferenceById(chargeData.getId());
            loanTransaction.getLoanChargesPaid()
                    .add(new LoanChargePaidBy(loanTransaction, loanCharge, entry.getValue(), scheduleAccrualData.getInstallmentNumber()));

        }

        loanTransactionRepository.saveAndFlush(loanTransaction);
        loan.addLoanTransaction(loanTransaction);

        Map<String, Object> transactionMap = toMapData(loanTransaction.getId(), amount, interestPortion, feePortion, penaltyPortion,
                scheduleAccrualData, accruedTill);

        // update repayment schedule portions

        LoanRepaymentScheduleInstallment loanScheduleInstallment = loan.getRepaymentScheduleInstallment(scheduleAccrualData.getDueDate());
        loanScheduleInstallment.updateAccrualPortion(Money.of(currency, totalAccInterest), Money.of(currency, totalAccFee),
                Money.of(currency, totalAccPenalty));

        // update loan accrued till date
        loan.setAccruedTill(accruedTill);
        loan.setLastModifiedBy(user.getId());
        loan.setLastModifiedDate(DateUtils.getAuditOffsetDateTime());

        loanRepository.saveAndFlush(loan);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccrualTransactionCreatedBusinessEvent(loanTransaction));

        final Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(scheduleAccrualData, transactionMap);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private Map<String, Object> deriveAccountingBridgeData(final LoanScheduleAccrualData loanScheduleAccrualData,
            final Map<String, Object> transactionMap) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", loanScheduleAccrualData.getLoanId());
        accountingBridgeData.put("loanProductId", loanScheduleAccrualData.getLoanProductId());
        accountingBridgeData.put("officeId", loanScheduleAccrualData.getOfficeId());
        accountingBridgeData.put("currencyCode", loanScheduleAccrualData.getCurrencyData().getCode());
        accountingBridgeData.put("cashBasedAccountingEnabled", false);
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", false);
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", true);
        accountingBridgeData.put("isAccountTransfer", false);
        accountingBridgeData.put("isChargeOff", false);
        accountingBridgeData.put("isFraud", false);

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        newLoanTransactions.add(transactionMap);

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Map<String, Object> toMapData(final Long id, final BigDecimal amount, final BigDecimal interestPortion,
            final BigDecimal feePortion, final BigDecimal penaltyPortion, final LoanScheduleAccrualData loanScheduleAccrualData,
            final LocalDate accruedTill) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.ACCRUAL);

        thisTransactionData.put("id", id);
        thisTransactionData.put("officeId", loanScheduleAccrualData.getOfficeId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", false);
        thisTransactionData.put("date", accruedTill);
        thisTransactionData.put("currency", loanScheduleAccrualData.getCurrencyData());
        thisTransactionData.put("amount", amount);
        thisTransactionData.put("principalPortion", null);
        thisTransactionData.put("interestPortion", interestPortion);
        thisTransactionData.put("feeChargesPortion", feePortion);
        thisTransactionData.put("penaltyChargesPortion", penaltyPortion);
        thisTransactionData.put("overPaymentPortion", null);

        Map<LoanChargeData, BigDecimal> applicableCharges = loanScheduleAccrualData.getApplicableCharges();
        if (applicableCharges != null && !applicableCharges.isEmpty()) {
            final List<Map<String, Object>> loanChargesPaidData = new ArrayList<>();
            for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
                LoanChargeData chargeData = entry.getKey();
                final Map<String, Object> loanChargePaidData = new LinkedHashMap<>();
                loanChargePaidData.put("chargeId", chargeData.getChargeId());
                loanChargePaidData.put("isPenalty", chargeData.isPenalty());
                loanChargePaidData.put("loanChargeId", chargeData.getId());
                loanChargePaidData.put("amount", entry.getValue());

                loanChargesPaidData.add(loanChargePaidData);
            }
            thisTransactionData.put("loanChargesPaid", loanChargesPaidData);
        }

        return thisTransactionData;
    }

    private void updateCharges(final Collection<LoanChargeData> chargesData, final LoanScheduleAccrualData accrualData,
            final LocalDate startDate, final LocalDate endDate) {
        final String chargeAccrualDateCriteria = configurationDomainService.getAccrualDateConfigForCharge();
        if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_DUE_DATE)) {
            updateChargeForDueDate(chargesData, accrualData, startDate, endDate);
        } else if (chargeAccrualDateCriteria.equalsIgnoreCase(ACCRUAL_ON_CHARGE_SUBMITTED_ON_DATE)) {
            updateChargeForSubmittedOnDate(chargesData, accrualData, startDate, endDate);
        }

    }

    private void updateChargeForSubmittedOnDate(Collection<LoanChargeData> chargesData, LoanScheduleAccrualData accrualData,
            LocalDate startDate, LocalDate endDate) {
        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal submittedDateFeeIncome = BigDecimal.ZERO;
        BigDecimal submittedDatePenaltyIncome = BigDecimal.ZERO;
        LocalDate scheduleEndDate = accrualData.getDueDateAsLocaldate();
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (isChargeSubmittedDateAndDueDateInRange(accrualData, startDate, endDate, scheduleEndDate, loanCharge)) {
                chargeAmount = loanCharge.getAmount();
                chargeAmount = calculateDueDateCharges(applicableCharges, loanCharge, chargeAmount);
            }
            if (loanCharge.isPenalty()) {
                submittedDatePenaltyIncome = submittedDatePenaltyIncome.add(chargeAmount);
            } else {
                submittedDateFeeIncome = submittedDateFeeIncome.add(chargeAmount);
            }

        }

        if (submittedDateFeeIncome.compareTo(BigDecimal.ZERO) == 0) {
            submittedDateFeeIncome = null;
        }

        if (submittedDatePenaltyIncome.compareTo(BigDecimal.ZERO) == 0) {
            submittedDatePenaltyIncome = null;
        }

        accrualData.updateChargeDetails(applicableCharges, submittedDateFeeIncome, submittedDatePenaltyIncome);
    }

    private boolean isChargeSubmittedDateAndDueDateInRange(LoanScheduleAccrualData accrualData, LocalDate startDate, LocalDate endDate,
            LocalDate scheduleEndDate, LoanChargeData loanCharge) {
        return ((accrualData.getInstallmentNumber() == 1 && DateUtils.isEqual(startDate, loanCharge.getSubmittedOnDate())
                && DateUtils.isEqual(startDate, loanCharge.getDueDate())) || DateUtils.isBefore(startDate, loanCharge.getDueDate()))
                && !DateUtils.isBefore(endDate, loanCharge.getSubmittedOnDate())
                && !DateUtils.isBefore(scheduleEndDate, loanCharge.getDueDate());
    }

    private void updateChargeForDueDate(Collection<LoanChargeData> chargesData, LoanScheduleAccrualData accrualData, LocalDate startDate,
            LocalDate endDate) {
        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
        BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (loanCharge.getDueDate() == null) {
                if (loanCharge.isInstallmentFee() && DateUtils.isEqual(endDate, accrualData.getDueDateAsLocaldate())) {
                    chargeAmount = calculateInstallmentFeeCharges(accrualData, applicableCharges, loanCharge, chargeAmount);
                }
            } else if (isChargeDueDateInRange(accrualData, startDate, endDate, loanCharge)) {
                chargeAmount = loanCharge.getAmount();
                chargeAmount = calculateDueDateCharges(applicableCharges, loanCharge, chargeAmount);
            }

            if (loanCharge.isPenalty()) {
                dueDatePenaltyIncome = dueDatePenaltyIncome.add(chargeAmount);
            } else {
                dueDateFeeIncome = dueDateFeeIncome.add(chargeAmount);
            }
        }

        if (dueDateFeeIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDateFeeIncome = null;
        }

        if (dueDatePenaltyIncome.compareTo(BigDecimal.ZERO) == 0) {
            dueDatePenaltyIncome = null;
        }

        accrualData.updateChargeDetails(applicableCharges, dueDateFeeIncome, dueDatePenaltyIncome);
    }

    private boolean isChargeDueDateInRange(LoanScheduleAccrualData accrualData, LocalDate startDate, LocalDate endDate,
            LoanChargeData loanCharge) {
        return ((accrualData.getInstallmentNumber() == 1 && DateUtils.isEqual(loanCharge.getDueDate(), startDate))
                || DateUtils.isAfter(loanCharge.getDueDate(), startDate)) && !DateUtils.isAfter(loanCharge.getDueDate(), endDate);
    }

    private BigDecimal calculateDueDateCharges(Map<LoanChargeData, BigDecimal> applicableCharges, LoanChargeData loanCharge,
            BigDecimal chargeAmount) {
        BigDecimal dueDateChargeAmount = chargeAmount;
        if (loanCharge.getAmountUnrecognized() != null) {
            dueDateChargeAmount = dueDateChargeAmount.subtract(loanCharge.getAmountUnrecognized());
        }
        boolean canAddCharge = dueDateChargeAmount.compareTo(BigDecimal.ZERO) > 0;
        if (canAddCharge && (loanCharge.getAmountAccrued() == null || chargeAmount.compareTo(loanCharge.getAmountAccrued()) != 0)) {
            BigDecimal amountForAccrual = dueDateChargeAmount;
            if (loanCharge.getAmountAccrued() != null) {
                amountForAccrual = dueDateChargeAmount.subtract(loanCharge.getAmountAccrued());
            }
            applicableCharges.put(loanCharge, amountForAccrual);
        }
        return dueDateChargeAmount;
    }

    private BigDecimal calculateInstallmentFeeCharges(LoanScheduleAccrualData accrualData,
            Map<LoanChargeData, BigDecimal> applicableCharges, LoanChargeData loanCharge, BigDecimal chargeAmount) {
        BigDecimal installmentFeeChargeAmount = chargeAmount;
        Collection<LoanInstallmentChargeData> installmentData = loanCharge.getInstallmentChargeData();
        for (LoanInstallmentChargeData installmentChargeData : installmentData) {

            if (installmentChargeData.getInstallmentNumber().equals(accrualData.getInstallmentNumber())) {
                BigDecimal accruableForInstallment = installmentChargeData.getAmount();
                if (installmentChargeData.getAmountUnrecognized() != null) {
                    accruableForInstallment = accruableForInstallment.subtract(installmentChargeData.getAmountUnrecognized());
                }
                installmentFeeChargeAmount = accruableForInstallment;
                boolean canAddCharge = installmentFeeChargeAmount.compareTo(BigDecimal.ZERO) > 0;
                if (canAddCharge && (installmentChargeData.getAmountAccrued() == null
                        || installmentFeeChargeAmount.compareTo(installmentChargeData.getAmountAccrued()) != 0)) {
                    BigDecimal amountForAccrual = installmentFeeChargeAmount;
                    if (installmentChargeData.getAmountAccrued() != null) {
                        amountForAccrual = installmentFeeChargeAmount.subtract(installmentChargeData.getAmountAccrued());
                    }
                    applicableCharges.put(loanCharge, amountForAccrual);
                    BigDecimal amountAccrued = installmentFeeChargeAmount;
                    if (loanCharge.getAmountAccrued() != null) {
                        amountAccrued = amountAccrued.add(loanCharge.getAmountAccrued());
                    }
                    loanCharge.updateAmountAccrued(amountAccrued);
                }
                break;
            }
        }
        return installmentFeeChargeAmount;
    }

    private void updateInterestIncome(final LoanScheduleAccrualData accrualData,
            final Collection<LoanTransactionData> loanWaiverTransactions,
            final Collection<LoanSchedulePeriodData> loanSchedulePeriodDataList, final LocalDate tillDate) {

        BigDecimal interestIncome = BigDecimal.ZERO;
        if (accrualData.getInterestIncome() != null) {
            interestIncome = accrualData.getInterestIncome();
        }
        if (accrualData.getWaivedInterestIncome() != null) {
            Collection<LoanTransactionData> loanTransactionDatas = new ArrayList<>();

            getLoanWaiverTransactionsInRange(accrualData, loanWaiverTransactions, tillDate, loanTransactionDatas);

            BigDecimal recognized = getWaivedInterestIncome(accrualData, loanSchedulePeriodDataList, loanTransactionDatas);

            BigDecimal interestWaived = accrualData.getWaivedInterestIncome();
            if (interestWaived.compareTo(recognized) > 0) {
                interestIncome = interestIncome.subtract(interestWaived.subtract(recognized));
            }
        }

        accrualData.updateAccruableIncome(interestIncome);
    }

    private BigDecimal getWaivedInterestIncome(LoanScheduleAccrualData accrualData,
            Collection<LoanSchedulePeriodData> loanSchedulePeriodDataList, Collection<LoanTransactionData> loanTransactionDatas) {
        BigDecimal recognized = BigDecimal.ZERO;
        BigDecimal unrecognized = BigDecimal.ZERO;
        BigDecimal remainingAmt = BigDecimal.ZERO;

        Iterator<LoanTransactionData> iterator = loanTransactionDatas.iterator();
        for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDataList) {
            if (MathUtil.isLessThanOrEqualZero(recognized) && MathUtil.isLessThanOrEqualZero(unrecognized) && iterator.hasNext()) {
                LoanTransactionData loanTransactionData = iterator.next();
                recognized = recognized.add(loanTransactionData.getInterestPortion());
                unrecognized = unrecognized.add(loanTransactionData.getUnrecognizedIncomePortion());
            }
            if (DateUtils.isBefore(loanSchedulePeriodData.getDueDate(), accrualData.getDueDateAsLocaldate())) {
                remainingAmt = remainingAmt.add(loanSchedulePeriodData.getInterestWaived());
                if (recognized.compareTo(remainingAmt) > 0) {
                    recognized = recognized.subtract(remainingAmt);
                    remainingAmt = BigDecimal.ZERO;
                } else {
                    remainingAmt = remainingAmt.subtract(recognized);
                    recognized = BigDecimal.ZERO;
                    if (unrecognized.compareTo(remainingAmt) >= 0) {
                        unrecognized = unrecognized.subtract(remainingAmt);
                        remainingAmt = BigDecimal.ZERO;
                    } else if (iterator.hasNext()) {
                        remainingAmt = remainingAmt.subtract(unrecognized);
                        unrecognized = BigDecimal.ZERO;
                    }
                }

            }
        }
        return recognized;
    }

    private void getLoanWaiverTransactionsInRange(LoanScheduleAccrualData accrualData,
            Collection<LoanTransactionData> loanWaiverTransactions, LocalDate tillDate,
            Collection<LoanTransactionData> loanTransactionDatas) {
        for (LoanTransactionData loanTransactionData : loanWaiverTransactions) {
            LocalDate transactionDate = loanTransactionData.getDate();
            if (!DateUtils.isAfter(transactionDate, accrualData.getFromDateAsLocaldate())
                    || (DateUtils.isAfter(transactionDate, accrualData.getFromDateAsLocaldate())
                            && !DateUtils.isAfter(transactionDate, accrualData.getDueDateAsLocaldate())
                            && !DateUtils.isAfter(transactionDate, tillDate))) {
                loanTransactionDatas.add(loanTransactionData);
            }
        }
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(currency.getCode(), existingTransactionIds,
                existingReversedTransactionIds, isAccountTransfer);
        journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    private void reprocessPeriodicAccruals(Loan loan, final Collection<LoanTransaction> accruals) {
        if (!loan.isChargedOff()) {
            List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
            boolean isBasedOnSubmittedOnDate = configurationDomainService.getAccrualDateConfigForCharge()
                    .equalsIgnoreCase("submitted-date");
            for (LoanRepaymentScheduleInstallment installment : installments) {
                checkAndUpdateAccrualsForInstallment(loan, accruals, installments, isBasedOnSubmittedOnDate, installment);
            }
            // reverse accruals after last installment
            LoanRepaymentScheduleInstallment lastInstallment = loan.getLastLoanRepaymentScheduleInstallment();
            reverseTransactionsPostEffectiveDate(accruals, lastInstallment.getDueDate());
        }
    }

    private void checkAndUpdateAccrualsForInstallment(Loan loan, Collection<LoanTransaction> accruals,
            List<LoanRepaymentScheduleInstallment> installments, boolean isBasedOnSubmittedOnDate,
            LoanRepaymentScheduleInstallment installment) {
        Money interest = Money.zero(loan.getCurrency());
        Money fee = Money.zero(loan.getCurrency());
        Money penalty = Money.zero(loan.getCurrency());
        for (LoanTransaction loanTransaction : accruals) {
            LocalDate transactionDateForRange = getDateForRangeCalculation(loanTransaction, isBasedOnSubmittedOnDate);
            boolean isInPeriod = LoanRepaymentScheduleProcessingWrapper.isInPeriod(transactionDateForRange, installment, installments);
            if (isInPeriod) {
                interest = interest.plus(loanTransaction.getInterestPortion(loan.getCurrency()));
                fee = fee.plus(loanTransaction.getFeeChargesPortion(loan.getCurrency()));
                penalty = penalty.plus(loanTransaction.getPenaltyChargesPortion(loan.getCurrency()));
                if (hasIncomeAmountChangedForInstallment(loan, installment, interest, fee, penalty, loanTransaction)) {
                    interest = interest.minus(loanTransaction.getInterestPortion(loan.getCurrency()));
                    fee = fee.minus(loanTransaction.getFeeChargesPortion(loan.getCurrency()));
                    penalty = penalty.minus(loanTransaction.getPenaltyChargesPortion(loan.getCurrency()));
                    loanTransaction.reverse();
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

    private void reprocessNonPeriodicAccruals(Loan loan, final Collection<LoanTransaction> accruals) {
        final Money interestApplied = Money.of(loan.getCurrency(), loan.getSummary().getTotalInterestCharged());
        ExternalId externalId = ExternalId.empty();
        boolean isExternalIdAutoGenerationEnabled = configurationDomainService.isExternalIdAutoGenerationEnabled();

        for (LoanTransaction loanTransaction : accruals) {
            if (loanTransaction.getInterestPortion(loan.getCurrency()).isGreaterThanZero()) {
                if (loanTransaction.getInterestPortion(loan.getCurrency()).isNotEqualTo(interestApplied)) {
                    loanTransaction.reverse();
                    if (isExternalIdAutoGenerationEnabled) {
                        externalId = ExternalId.generate();
                    }
                    final LoanTransaction interestAppliedTransaction = LoanTransaction.accrueInterest(loan.getOffice(), loan,
                            interestApplied, loan.getDisbursementDate(), externalId);
                    loan.addLoanTransaction(interestAppliedTransaction);
                }
            } else {
                Set<LoanChargePaidBy> chargePaidBies = loanTransaction.getLoanChargesPaid();
                for (final LoanChargePaidBy chargePaidBy : chargePaidBies) {
                    LoanCharge loanCharge = chargePaidBy.getLoanCharge();
                    Money chargeAmount = loanCharge.getAmount(loan.getCurrency());
                    if (chargeAmount.isNotEqualTo(loanTransaction.getAmount(loan.getCurrency()))) {
                        loanTransaction.reverse();
                        loan.handleChargeAppliedTransaction(loanCharge, loanTransaction.getTransactionDate());
                    }
                }
            }
        }
    }

    private LocalDate createLoanScheduleAccrualDataList(Loan loan, LocalDate accruedTill,
            Collection<LoanScheduleAccrualData> loanScheduleAccrualList) {
        boolean isOrganisationDateEnabled = configurationDomainService.isOrganisationstartDateEnabled();
        LocalDate organisationStartDate = DateUtils.getBusinessLocalDate();
        if (isOrganisationDateEnabled) {
            organisationStartDate = configurationDomainService.retrieveOrganisationStartDate();
        }
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        Long loanId = loan.getId();
        Long officeId = loan.getOfficeId();
        LocalDate accrualStartDate = null;
        PeriodFrequencyType repaymentFrequency = loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType();
        Integer repayEvery = loan.repaymentScheduleDetail().getRepayEvery();
        LocalDate interestCalculatedFrom = loan.getInterestChargedFromDate();
        Long loanProductId = loan.productId();
        MonetaryCurrency currency = loan.getCurrency();
        ApplicationCurrency applicationCurrency = applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        CurrencyData currencyData = applicationCurrency.toData();
        Set<LoanCharge> loanCharges = loan.getActiveCharges();
        int firstNormalInstallmentNumber = LoanRepaymentScheduleProcessingWrapper.fetchFirstNormalInstallmentNumber(installments);

        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (DateUtils.isAfter(installment.getDueDate(), loan.getMaturityDate())) {
                accruedTill = DateUtils.getBusinessLocalDate();
            }
            if (!isOrganisationDateEnabled || DateUtils.isBefore(organisationStartDate, installment.getDueDate())) {
                boolean isFirstNormalInstallment = installment.getInstallmentNumber().equals(firstNormalInstallmentNumber);
                generateLoanScheduleAccrualData(accruedTill, loanScheduleAccrualList, loanId, officeId, accrualStartDate,
                        repaymentFrequency, repayEvery, interestCalculatedFrom, loanProductId, currency, currencyData, loanCharges,
                        installment, isFirstNormalInstallment);
            }
        }
        return accruedTill;
    }

    private void generateLoanScheduleAccrualData(final LocalDate accruedTill,
            final Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas, final Long loanId, Long officeId,
            final LocalDate accrualStartDate, final PeriodFrequencyType repaymentFrequency, final Integer repayEvery,
            final LocalDate interestCalculatedFrom, final Long loanProductId, final MonetaryCurrency currency,
            final CurrencyData currencyData, final Set<LoanCharge> loanCharges, final LoanRepaymentScheduleInstallment installment,
            boolean isFirstNormalInstallment) {

        if (!DateUtils.isBefore(accruedTill, installment.getDueDate()) || (DateUtils.isAfter(accruedTill, installment.getFromDate())
                && !DateUtils.isAfter(accruedTill, installment.getDueDate()))) {
            BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
            BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
            LocalDate chargesTillDate = installment.getDueDate();
            if (!DateUtils.isAfter(accruedTill, installment.getDueDate())) {
                chargesTillDate = accruedTill;
            }

            for (final LoanCharge loanCharge : loanCharges) {
                boolean isDue = isFirstNormalInstallment
                        ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(installment.getFromDate(), chargesTillDate)
                        : loanCharge.isDueForCollectionFromAndUpToAndIncluding(installment.getFromDate(), chargesTillDate);
                if (isDue) {
                    if (loanCharge.isFeeCharge()) {
                        dueDateFeeIncome = dueDateFeeIncome.add(loanCharge.amount());
                    } else if (loanCharge.isPenaltyCharge()) {
                        dueDatePenaltyIncome = dueDatePenaltyIncome.add(loanCharge.amount());
                    }
                }
            }
            LoanScheduleAccrualData accrualData = new LoanScheduleAccrualData(loanId, officeId, installment.getInstallmentNumber(),
                    accrualStartDate, repaymentFrequency, repayEvery, installment.getDueDate(), installment.getFromDate(),
                    installment.getId(), loanProductId, installment.getInterestCharged(currency).getAmount(),
                    installment.getFeeChargesCharged(currency).getAmount(), installment.getPenaltyChargesCharged(currency).getAmount(),
                    installment.getInterestAccrued(currency).getAmount(), installment.getFeeAccrued(currency).getAmount(),
                    installment.getPenaltyAccrued(currency).getAmount(), currencyData, interestCalculatedFrom,
                    installment.getInterestWaived(currency).getAmount(), installment.getCreditedFee(currency).getAmount(),
                    installment.getCreditedPenalty(currency).getAmount());
            loanScheduleAccrualDatas.add(accrualData);

        }
    }

    private void createAccrualTransactionAndUpdateChargesPaidBy(Loan loan, LocalDate foreClosureDate,
            Collection<LoanTransaction> newAccrualTransactions, MonetaryCurrency currency, Money interestPortion, Money feePortion,
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
            boolean isDue = DateUtils.isEqual(fromDate, loan.getDisbursementDate())
                    ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(fromDate, foreClosureDate)
                    : loanCharge.isDueForCollectionFromAndUpToAndIncluding(fromDate, foreClosureDate);
            if (loanCharge.isActive() && !loanCharge.isPaid() && (isDue || loanCharge.isInstalmentFee())) {
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                        loanCharge.getAmountOutstanding(currency).getAmount(), null);
                accrualCharges.add(loanChargePaidBy);
                loanCharge.getLoanChargePaidBySet().add(loanChargePaidBy);
            }
        }
    }

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
                } else if (transaction.isRepaymentLikeType() || transaction.isChargePayment()) {
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

    private List<LoanTransaction> retrieveListOfAccrualTransactions(Loan loan) {
        return loan.getLoanTransactions().stream().filter(transaction -> transaction.isNotReversed() && transaction.isAccrual())
                .sorted(LoanTransactionComparator.INSTANCE).collect(Collectors.toList());
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

    private void createUpdateAccrualTransaction(Loan loan, LoanInterestRecalcualtionAdditionalDetails compoundingDetail,
            LoanTransaction existingAccrualTransaction, BigDecimal interest, BigDecimal fee, BigDecimal penalties,
            HashMap<String, Object> feeDetails, ExternalId externalId) {
        if (configurationDomainService.isExternalIdAutoGenerationEnabled()) {
            externalId = ExternalId.generate();
        }

        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
            if (existingAccrualTransaction == null) {
                LoanTransaction accrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(loan, accrual, feeDetails, null);
                loan.addLoanTransaction(accrual);
            } else if (existingAccrualTransaction.getAmount(loan.getCurrency()).getAmount().compareTo(compoundingDetail.getAmount()) != 0) {
                existingAccrualTransaction.reverse();
                LoanTransaction accrual = LoanTransaction.accrueTransaction(loan, loan.getOffice(), compoundingDetail.getEffectiveDate(),
                        compoundingDetail.getAmount(), interest, fee, penalties, externalId);
                updateLoanChargesPaidBy(loan, accrual, feeDetails, null);
                loan.addLoanTransaction(accrual);
            }
        }
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
            boolean isDue = DateUtils.isEqual(fromDate, loan.getDisbursementDate())
                    ? loanCharge.isDueForCollectionFromIncludingAndUpToAndIncluding(fromDate, toDate)
                    : loanCharge.isDueForCollectionFromAndUpToAndIncluding(fromDate, toDate);
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

    private void reverseTransactionsPostEffectiveDate(Collection<LoanTransaction> transactions, LocalDate effectiveDate) {
        for (LoanTransaction loanTransaction : transactions) {
            if (DateUtils.isAfter(loanTransaction.getTransactionDate(), effectiveDate)) {
                loanTransaction.reverse();
            }
        }
    }

    private void processAccrualTransactionsOnLoanClosure(Loan loan) {
        if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()
                // to avoid collision with processIncomeAccrualTransactionOnLoanClosure()
                && !(loan.getLoanInterestRecalculationDetails() != null
                        && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction())
                && !loan.isNpa() && !loan.isChargedOff()) {
            HashMap<String, Object> incomeDetails = new HashMap<>();
            MonetaryCurrency currency = loan.getCurrency();
            Money interestPortion = Money.zero(currency);
            Money feePortion = Money.zero(currency);
            Money penaltyPortion = Money.zero(currency);

            determineReceivableIncomeDetailsForLoanClosure(loan, incomeDetails);

            interestPortion = interestPortion.plus((Money) incomeDetails.get(Loan.INTEREST));
            feePortion = feePortion.plus((Money) incomeDetails.get(Loan.FEE));
            penaltyPortion = penaltyPortion.plus((Money) incomeDetails.get(Loan.PENALTIES));

            Money total = interestPortion.plus(feePortion).plus(penaltyPortion);

            if (total.isGreaterThanZero()) {
                LocalDate accrualTransactionDate = getFinalAccrualTransactionDate(loan);
                LoanTransaction accrualTransaction = createAccrualTransaction(loan, interestPortion, feePortion, penaltyPortion, total,
                        accrualTransactionDate);
                updateLoanChargesAndInstallmentChargesPaidBy(loan, accrualTransaction);
                // TODO check if this is required
                // saveLoanTransactionWithDataIntegrityViolationChecks(accrualTransaction);
                loan.addLoanTransaction(accrualTransaction);
                businessEventNotifierService.notifyPostBusinessEvent(new LoanAccrualTransactionCreatedBusinessEvent(accrualTransaction));

                updateLoanInstallmentAccruedPortion(loan);
            }
        }
    }

    private void updateLoanInstallmentAccruedPortion(Loan loan) {
        MonetaryCurrency currency = loan.getCurrency();
        loan.getRepaymentScheduleInstallments().forEach(installment -> {
            installment.updateAccrualPortion(installment.getInterestCharged(currency).minus(installment.getInterestWaived(currency)),
                    installment.getFeeChargesCharged(currency).minus(installment.getFeeChargesWaived(currency)),
                    installment.getPenaltyChargesCharged(currency).minus(installment.getPenaltyChargesWaived(currency)));
        });
    }

    private void updateLoanChargesAndInstallmentChargesPaidBy(Loan loan, LoanTransaction accrualTransaction) {
        MonetaryCurrency currency = loan.getCurrency();
        Set<LoanChargePaidBy> accrualCharges = accrualTransaction.getLoanChargesPaid();

        Map<Long, Money> accrualDetails = loan.getActiveCharges().stream()
                .collect(Collectors.toMap(LoanCharge::getId, v -> Money.zero(currency)));

        loan.getLoanTransactions(LoanTransaction::isAccrual).forEach(transaction -> {
            transaction.getLoanChargesPaid().forEach(loanChargePaid -> {
                accrualDetails.computeIfPresent(loanChargePaid.getLoanCharge().getId(),
                        (mappedKey, mappedValue) -> mappedValue.add(Money.of(currency, loanChargePaid.getAmount())));
            });
        });

        loan.getActiveCharges().forEach(loanCharge -> {
            Money amount = loanCharge.getAmount(currency).minus(loanCharge.getAmountWaived(currency));
            if (!loanCharge.isInstalmentFee() && loanCharge.isActive() && accrualDetails.get(loanCharge.getId()).isLessThan(amount)) {
                Money amountToBeAccrued = amount.minus(accrualDetails.get(loanCharge.getId()));
                final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction, loanCharge,
                        amountToBeAccrued.getAmount(), null);
                accrualCharges.add(loanChargePaidBy);
                loanCharge.getLoanChargePaidBySet().add(loanChargePaidBy);
            }
        });

        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loan.getRepaymentScheduleInstallments()) {
            for (LoanInstallmentCharge installmentCharge : loanRepaymentScheduleInstallment.getInstallmentCharges()) {
                if (installmentCharge.getLoanCharge().isActive()) {
                    Money notWaivedAmount = installmentCharge.getAmount(currency).minus(installmentCharge.getAmountWaived(currency));
                    if (notWaivedAmount.isGreaterThanZero()) {
                        Money amountToBeAccrued = notWaivedAmount.minus(accrualDetails.get(installmentCharge.getLoanCharge().getId()));
                        if (amountToBeAccrued.isGreaterThanZero()) {
                            final LoanChargePaidBy loanChargePaidBy = new LoanChargePaidBy(accrualTransaction,
                                    installmentCharge.getLoanCharge(), amountToBeAccrued.getAmount(),
                                    installmentCharge.getInstallment().getInstallmentNumber());
                            accrualCharges.add(loanChargePaidBy);
                            installmentCharge.getLoanCharge().getLoanChargePaidBySet().add(loanChargePaidBy);
                            accrualDetails.computeIfPresent(installmentCharge.getLoanCharge().getId(),
                                    (mappedKey, mappedValue) -> mappedValue.add(amountToBeAccrued));
                        }
                        accrualDetails.computeIfPresent(installmentCharge.getLoanCharge().getId(), (mappedKey, mappedValue) -> MathUtil
                                .negativeToZero(mappedValue.minus(Money.of(currency, installmentCharge.getAmount()))));
                    }
                }
            }
        }
    }

    private LoanTransaction createAccrualTransaction(Loan loan, Money interestPortion, Money feePortion, Money penaltyPortion, Money total,
            LocalDate accrualTransactionDate) {
        ExternalId externalId = externalIdFactory.create();
        LoanTransaction accrualTransaction = LoanTransaction.accrueTransaction(loan, loan.getOffice(), accrualTransactionDate,
                total.getAmount(), interestPortion.getAmount(), feePortion.getAmount(), penaltyPortion.getAmount(), externalId);
        return accrualTransaction;
    }

    private void determineReceivableIncomeDetailsForLoanClosure(Loan loan, Map<String, Object> incomeDetails) {

        MonetaryCurrency currency = loan.getCurrency();
        Money interestPortion = Money.zero(currency);
        Money feePortion = Money.zero(currency);
        Money penaltyPortion = Money.zero(currency);

        for (LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment : loan.getRepaymentScheduleInstallments()) {
            // TODO: test with interest waiving
            interestPortion = interestPortion.add(loanRepaymentScheduleInstallment.getInterestCharged(currency))
                    .minus(loanRepaymentScheduleInstallment.getInterestAccrued(currency))
                    .minus(loanRepaymentScheduleInstallment.getInterestWaived(currency));
        }

        for (LoanCharge loanCharge : loan.getLoanCharges()) {
            if (!loanCharge.isActive()) {
                continue;
            }
            BigDecimal accruedAmount = BigDecimal.ZERO;
            BigDecimal waivedAmount = BigDecimal.ZERO;
            for (LoanChargePaidBy loanChargePaidBy : loanCharge.getLoanChargePaidBySet()) {
                if (loanChargePaidBy.getLoanTransaction().isAccrual()) {
                    accruedAmount = accruedAmount.add(loanChargePaidBy.getLoanTransaction().getAmount());
                } else if (loanChargePaidBy.getLoanTransaction().isChargesWaiver()) {
                    waivedAmount = waivedAmount.add(loanChargePaidBy.getLoanTransaction().getAmount());
                }
            }
            Money needToAccrueAmount = MathUtil.negativeToZero(loanCharge.getAmount(currency).minus(accruedAmount).minus(waivedAmount));
            if (loanCharge.isPenaltyCharge()) {
                penaltyPortion = penaltyPortion.add(needToAccrueAmount);
            } else if (loanCharge.isFeeCharge()) {
                feePortion = feePortion.add(needToAccrueAmount);
            }
        }

        incomeDetails.put(Loan.INTEREST, interestPortion);
        incomeDetails.put(Loan.FEE, feePortion);
        incomeDetails.put(Loan.PENALTIES, penaltyPortion);

    }

    private void processIncomeAndAccrualTransactionOnLoanClosure(Loan loan) {
        if (loan.getLoanInterestRecalculationDetails() != null
                && loan.getLoanInterestRecalculationDetails().isCompoundingToBePostedAsTransaction()
                && loan.getStatus().isClosedObligationsMet() && !loan.isNpa() && !loan.isChargedOff()) {

            LocalDate closedDate = loan.getClosedOnDate();
            reverseTransactionsOnOrAfter(retrieveListOfIncomePostingTransactions(loan), closedDate);
            reverseTransactionsOnOrAfter(retrieveListOfAccrualTransactions(loan), closedDate);

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

    private void reverseTransactionsOnOrAfter(List<LoanTransaction> transactions, LocalDate date) {
        for (LoanTransaction loanTransaction : transactions) {
            if (!DateUtils.isBefore(loanTransaction.getTransactionDate(), date)) {
                loanTransaction.reverse();
            }
        }
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

    private void determineCumulativeIncomeDetails(Loan loan, Collection<LoanTransaction> transactions,
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

    private LocalDate getFinalAccrualTransactionDate(Loan loan) {
        return switch (loan.getStatus()) {
            case CLOSED_OBLIGATIONS_MET -> loan.getClosedOnDate();
            case OVERPAID -> loan.getOverpaidOnDate();
            default -> throw new IllegalStateException("Unexpected value: " + loan.getStatus());
        };
    }

}
