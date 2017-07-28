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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.fineract.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanAccrualWritePlatformServiceImpl implements LoanAccrualWritePlatformService {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final AppUserRepositoryWrapper userRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Autowired
    public LoanAccrualWritePlatformServiceImpl(final RoutingDataSource dataSource, final LoanReadPlatformService loanReadPlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService,
            final LoanChargeReadPlatformService loanChargeReadPlatformService, final AppUserRepositoryWrapper userRepository,
            final LoanRepositoryWrapper loanRepositoryWrapper, final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.userRepository = userRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
    }

    @Override
    @Transactional
    public void addAccrualAccounting(final Long loanId, final Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws Exception {
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccural(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTansactionData = new ArrayList<>(1);

        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTansactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }
            updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
            updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, accrualData.getDueDateAsLocaldate());
            addAccrualAccounting(accrualData);
        }
    }

    @Override
    @Transactional
    public void addPeriodicAccruals(final LocalDate tilldate, Long loanId, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas)
            throws Exception {
        boolean firstTime = true;
        LocalDate accruredTill = null;
        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService.retrieveLoanChargesForAccural(loanId);
        Collection<LoanSchedulePeriodData> loanWaiverScheduleData = new ArrayList<>(1);
        Collection<LoanTransactionData> loanWaiverTansactionData = new ArrayList<>(1);
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            if (accrualData.getWaivedInterestIncome() != null && loanWaiverScheduleData.isEmpty()) {
                loanWaiverScheduleData = this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId());
                loanWaiverTansactionData = this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId());
            }

            if (accrualData.getDueDateAsLocaldate().isAfter(tilldate)) {
                if (accruredTill == null || firstTime) {
                    accruredTill = accrualData.getAccruedTill();
                    firstTime = false;
                }
                if (accruredTill == null || accruredTill.isBefore(tilldate)) {
                    updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), tilldate);
                    updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, tilldate);
                    addAccrualTillSpecificDate(tilldate, accrualData);
                }
            } else {
                updateCharges(chargeData, accrualData, accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate());
                updateInterestIncome(accrualData, loanWaiverTansactionData, loanWaiverScheduleData, tilldate);
                addAccrualAccounting(accrualData);
                accruredTill = accrualData.getDueDateAsLocaldate();
            }
        }
    }

    private void addAccrualTillSpecificDate(final LocalDate tilldate, final LoanScheduleAccrualData accrualData) throws Exception {
        LocalDate interestStartDate = accrualData.getFromDateAsLocaldate();
        if (accrualData.getInterestCalculatedFrom() != null
                && accrualData.getFromDateAsLocaldate().isBefore(accrualData.getInterestCalculatedFrom())) {
            if (accrualData.getInterestCalculatedFrom().isBefore(accrualData.getDueDateAsLocaldate())) {
                interestStartDate = accrualData.getInterestCalculatedFrom();
            } else {
                interestStartDate = accrualData.getDueDateAsLocaldate();
            }
        }

        int totalNumberOfDays = Days.daysBetween(interestStartDate, accrualData.getDueDateAsLocaldate()).getDays();
        LocalDate startDate = accrualData.getFromDateAsLocaldate();
        if (accrualData.getInterestCalculatedFrom() != null && startDate.isBefore(accrualData.getInterestCalculatedFrom())) {
            if (accrualData.getInterestCalculatedFrom().isBefore(tilldate)) {
                startDate = accrualData.getInterestCalculatedFrom();
            } else {
                startDate = tilldate;
            }
        }
        int daysToBeAccrued = Days.daysBetween(startDate, tilldate).getDays();
        double interestPerDay = accrualData.getAccruableIncome().doubleValue() / totalNumberOfDays;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal feeportion = accrualData.getDueDateFeeIncome();
        BigDecimal penaltyportion = accrualData.getDueDatePenaltyIncome();
        if (daysToBeAccrued >= totalNumberOfDays) {
            interestportion = accrualData.getAccruableIncome();
        } else {
            double iterest = interestPerDay * daysToBeAccrued;
            interestportion = BigDecimal.valueOf(iterest);
        }
        interestportion = interestportion.setScale(accrualData.getCurrencyData().decimalPlaces(), MoneyHelper.getRoundingMode());

        BigDecimal totalAccInterest = accrualData.getAccruedInterestIncome();
        BigDecimal totalAccPenalty = accrualData.getAccruedPenaltyIncome();
        BigDecimal totalAccFee = accrualData.getAccruedFeeIncome();

        if (interestportion != null) {
            if (totalAccInterest == null) {
                totalAccInterest = BigDecimal.ZERO;
            }
            interestportion = interestportion.subtract(totalAccInterest);
            amount = amount.add(interestportion);
            totalAccInterest = totalAccInterest.add(interestportion);
            if (interestportion.compareTo(BigDecimal.ZERO) == 0) {
                interestportion = null;
            }
        }
        if (feeportion != null) {
            if (totalAccFee == null) {
                totalAccFee = BigDecimal.ZERO;
            }
            feeportion = feeportion.subtract(totalAccFee);
            amount = amount.add(feeportion);
            totalAccFee = totalAccFee.add(feeportion);
            if (feeportion.compareTo(BigDecimal.ZERO) == 0) {
                feeportion = null;
            }
        }

        if (penaltyportion != null) {
            if (totalAccPenalty == null) {
                totalAccPenalty = BigDecimal.ZERO;
            }
            penaltyportion = penaltyportion.subtract(totalAccPenalty);
            amount = amount.add(penaltyportion);
            totalAccPenalty = totalAccPenalty.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) == 1) {
            addAccrualAccounting(accrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                    totalAccPenalty, tilldate);
        }
    }

    @Transactional
    public void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData) throws Exception {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal totalAccInterest = null;
        if (scheduleAccrualData.getAccruableIncome() != null) {
            interestportion = scheduleAccrualData.getAccruableIncome();
            totalAccInterest = interestportion;
            if (scheduleAccrualData.getAccruedInterestIncome() != null) {
                interestportion = interestportion.subtract(scheduleAccrualData.getAccruedInterestIncome());
            }
            amount = amount.add(interestportion);
            if (interestportion.compareTo(BigDecimal.ZERO) == 0) {
                interestportion = null;
            }
        }

        BigDecimal feeportion = null;
        BigDecimal totalAccFee = null;
        if (scheduleAccrualData.getDueDateFeeIncome() != null) {
            feeportion = scheduleAccrualData.getDueDateFeeIncome();
            totalAccFee = feeportion;
            if (scheduleAccrualData.getAccruedFeeIncome() != null) {
                feeportion = feeportion.subtract(scheduleAccrualData.getAccruedFeeIncome());
            }
            amount = amount.add(feeportion);
            if (feeportion.compareTo(BigDecimal.ZERO) == 0) {
                feeportion = null;
            }
        }

        BigDecimal penaltyportion = null;
        BigDecimal totalAccPenalty = null;
        if (scheduleAccrualData.getDueDatePenaltyIncome() != null) {
            penaltyportion = scheduleAccrualData.getDueDatePenaltyIncome();
            totalAccPenalty = penaltyportion;
            if (scheduleAccrualData.getAccruedPenaltyIncome() != null) {
                penaltyportion = penaltyportion.subtract(scheduleAccrualData.getAccruedPenaltyIncome());
            }
            amount = amount.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) == 1) {
            addAccrualAccounting(scheduleAccrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                    totalAccPenalty, scheduleAccrualData.getDueDateAsLocaldate());
        }
    }

    private void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestportion,
            BigDecimal totalAccInterest, BigDecimal feeportion, BigDecimal totalAccFee, BigDecimal penaltyportion,
            BigDecimal totalAccPenalty, final LocalDate accruedTill) throws DataAccessException {
        String transactionSql = "INSERT INTO m_loan_transaction  (loan_id,office_id,is_reversed,transaction_type_enum,transaction_date,amount,interest_portion_derived,"
                + "fee_charges_portion_derived,penalty_charges_portion_derived, submitted_on_date) VALUES (?, ?, 0, ?, ?, ?, ?, ?, ?, ?)";
        this.jdbcTemplate.update(transactionSql, scheduleAccrualData.getLoanId(), scheduleAccrualData.getOfficeId(),
                LoanTransactionType.ACCRUAL.getValue(), accruedTill.toDate(), amount, interestportion, feeportion, penaltyportion,
                DateUtils.getDateOfTenant());
        @SuppressWarnings("deprecation")
        final Long transactonId = this.jdbcTemplate.queryForLong("SELECT LAST_INSERT_ID()");

        Map<LoanChargeData, BigDecimal> applicableCharges = scheduleAccrualData.getApplicableCharges();
        String chargespaidSql = "INSERT INTO m_loan_charge_paid_by (loan_transaction_id, loan_charge_id, amount,installment_number) VALUES (?,?,?,?)";
        for (Map.Entry<LoanChargeData, BigDecimal> entry : applicableCharges.entrySet()) {
            LoanChargeData chargeData = entry.getKey();
            this.jdbcTemplate.update(chargespaidSql, transactonId, chargeData.getId(), entry.getValue(),
                    scheduleAccrualData.getInstallmentNumber());
        }

        Map<String, Object> transactionMap = toMapData(transactonId, amount, interestportion, feeportion, penaltyportion,
                scheduleAccrualData, accruedTill);

        String repaymetUpdatesql = "UPDATE m_loan_repayment_schedule SET accrual_interest_derived=?, accrual_fee_charges_derived=?, "
                + "accrual_penalty_charges_derived=? WHERE  id=?";
        this.jdbcTemplate.update(repaymetUpdatesql, totalAccInterest, totalAccFee, totalAccPenalty,
                scheduleAccrualData.getRepaymentScheduleId());

        String updateLoan = "UPDATE m_loan  SET accrued_till=?  WHERE  id=?";
        this.jdbcTemplate.update(updateLoan, accruedTill.toDate(), scheduleAccrualData.getLoanId());
        final Map<String, Object> accountingBridgeData = deriveAccountingBridgeData(scheduleAccrualData, transactionMap);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }

    public Map<String, Object> deriveAccountingBridgeData(final LoanScheduleAccrualData loanScheduleAccrualData,
            final Map<String, Object> transactionMap) {

        final Map<String, Object> accountingBridgeData = new LinkedHashMap<>();
        accountingBridgeData.put("loanId", loanScheduleAccrualData.getLoanId());
        accountingBridgeData.put("loanProductId", loanScheduleAccrualData.getLoanProductId());
        accountingBridgeData.put("officeId", loanScheduleAccrualData.getOfficeId());
        accountingBridgeData.put("currency", loanScheduleAccrualData.getCurrencyData());
        accountingBridgeData.put("cashBasedAccountingEnabled", false);
        accountingBridgeData.put("upfrontAccrualBasedAccountingEnabled", false);
        accountingBridgeData.put("periodicAccrualBasedAccountingEnabled", true);
        accountingBridgeData.put("isAccountTransfer", false);

        final List<Map<String, Object>> newLoanTransactions = new ArrayList<>();
        newLoanTransactions.add(transactionMap);

        accountingBridgeData.put("newLoanTransactions", newLoanTransactions);
        return accountingBridgeData;
    }

    public Map<String, Object> toMapData(final Long id, final BigDecimal amount, final BigDecimal interestportion,
            final BigDecimal feeportion, final BigDecimal penaltyportion, final LoanScheduleAccrualData loanScheduleAccrualData,
            final LocalDate accruredTill) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.ACCRUAL);

        thisTransactionData.put("id", id);
        thisTransactionData.put("officeId", loanScheduleAccrualData.getOfficeId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", false);
        thisTransactionData.put("date", accruredTill);
        thisTransactionData.put("currency", loanScheduleAccrualData.getCurrencyData());
        thisTransactionData.put("amount", amount);
        thisTransactionData.put("principalPortion", null);
        thisTransactionData.put("interestPortion", interestportion);
        thisTransactionData.put("feeChargesPortion", feeportion);
        thisTransactionData.put("penaltyChargesPortion", penaltyportion);
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

        final Map<LoanChargeData, BigDecimal> applicableCharges = new HashMap<>();
        BigDecimal dueDateFeeIncome = BigDecimal.ZERO;
        BigDecimal dueDatePenaltyIncome = BigDecimal.ZERO;
        for (LoanChargeData loanCharge : chargesData) {
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (loanCharge.getDueDate() == null) {
                if (loanCharge.isInstallmentFee() && accrualData.getDueDateAsLocaldate().isEqual(endDate)) {
                    Collection<LoanInstallmentChargeData> installmentData = loanCharge.getInstallmentChargeData();
                    for (LoanInstallmentChargeData installmentChargeData : installmentData) {

                        if (installmentChargeData.getInstallmentNumber().equals(accrualData.getInstallmentNumber())) {
                            BigDecimal accruableForInstallment = installmentChargeData.getAmount();
                            if (installmentChargeData.getAmountUnrecognized() != null) {
                                accruableForInstallment = accruableForInstallment.subtract(installmentChargeData.getAmountUnrecognized());
                            }
                            chargeAmount = accruableForInstallment;
                            boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) == 1;
                            if (canAddCharge
                                    && (installmentChargeData.getAmountAccrued() == null || chargeAmount.compareTo(installmentChargeData
                                            .getAmountAccrued()) != 0)) {
                                BigDecimal amountForAccrual = chargeAmount;
                                if (installmentChargeData.getAmountAccrued() != null) {
                                    amountForAccrual = chargeAmount.subtract(installmentChargeData.getAmountAccrued());
                                }
                                applicableCharges.put(loanCharge, amountForAccrual);
                                BigDecimal amountAccrued = chargeAmount;
                                if (loanCharge.getAmountAccrued() != null) {
                                    amountAccrued = amountAccrued.add(loanCharge.getAmountAccrued());
                                }
                                loanCharge.updateAmountAccrued(amountAccrued);
                            }
                            break;
                        }
                    }
                }
            } else if (loanCharge.getDueDate().isAfter(startDate) && !loanCharge.getDueDate().isAfter(endDate)) {
                chargeAmount = loanCharge.getAmount();
                if (loanCharge.getAmountUnrecognized() != null) {
                    chargeAmount = chargeAmount.subtract(loanCharge.getAmountUnrecognized());
                }
                boolean canAddCharge = chargeAmount.compareTo(BigDecimal.ZERO) == 1;
                if (canAddCharge && (loanCharge.getAmountAccrued() == null || chargeAmount.compareTo(loanCharge.getAmountAccrued()) != 0)) {
                    BigDecimal amountForAccrual = chargeAmount;
                    if (loanCharge.getAmountAccrued() != null) {
                        amountForAccrual = chargeAmount.subtract(loanCharge.getAmountAccrued());
                    }
                    applicableCharges.put(loanCharge, amountForAccrual);
                }
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

    private void updateInterestIncome(final LoanScheduleAccrualData accrualData,
            final Collection<LoanTransactionData> loanWaiverTansactions, final Collection<LoanSchedulePeriodData> loanSchedulePeriodDatas,
            final LocalDate tilldate) {

        BigDecimal interestIncome = accrualData.getInterestIncome();
        if (accrualData.getWaivedInterestIncome() != null) {
            BigDecimal recognized = BigDecimal.ZERO;
            BigDecimal unrecognized = BigDecimal.ZERO;
            BigDecimal remainingAmt = BigDecimal.ZERO;
            Collection<LoanTransactionData> loanTransactionDatas = new ArrayList<>();

            for (LoanTransactionData loanTransactionData : loanWaiverTansactions) {
                if (!loanTransactionData.dateOf().isAfter(accrualData.getFromDateAsLocaldate())
                        || (loanTransactionData.dateOf().isAfter(accrualData.getFromDateAsLocaldate())
                                && !loanTransactionData.dateOf().isAfter(accrualData.getDueDateAsLocaldate()) && !loanTransactionData
                                .dateOf().isAfter(tilldate))) {
                    loanTransactionDatas.add(loanTransactionData);
                }
            }

            Iterator<LoanTransactionData> iterator = loanTransactionDatas.iterator();
            for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDatas) {
                if (recognized.compareTo(BigDecimal.ZERO) != 1 && unrecognized.compareTo(BigDecimal.ZERO) != 1 && iterator.hasNext()) {
                    LoanTransactionData loanTransactionData = iterator.next();
                    recognized = recognized.add(loanTransactionData.getInterestPortion());
                    unrecognized = unrecognized.add(loanTransactionData.getUnrecognizedIncomePortion());
                }
                if (loanSchedulePeriodData.periodDueDate().isBefore(accrualData.getDueDateAsLocaldate())) {
                    remainingAmt = remainingAmt.add(loanSchedulePeriodData.interestWaived());
                    if (recognized.compareTo(remainingAmt) == 1) {
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

            BigDecimal interestWaived = accrualData.getWaivedInterestIncome();
            if (interestWaived.compareTo(recognized) == 1) {
                interestIncome = interestIncome.subtract(interestWaived.subtract(recognized));
            }
        }

        accrualData.updateAccruableIncome(interestIncome);
    }

    @Override
    @Transactional
    public void addIncomeAndAccrualTransactions(Long loanId) throws LoanNotFoundException {
        if (loanId != null) {
            Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
            if (loan == null) { throw new LoanNotFoundException(loanId); }
            final List<Long> existingTransactionIds = new ArrayList<>();
            final List<Long> existingReversedTransactionIds = new ArrayList<>();
            existingTransactionIds.addAll(loan.findExistingTransactionIds());
            existingReversedTransactionIds.addAll(loan.findExistingReversedTransactionIds());
            loan.processIncomeTransactions(this.userRepository.fetchSystemUser());
            this.loanRepositoryWrapper.saveAndFlush(loan);
            postJournalEntries(loan, existingTransactionIds, existingReversedTransactionIds);
        }
    }

    private void postJournalEntries(final Loan loan, final List<Long> existingTransactionIds,
            final List<Long> existingReversedTransactionIds) {
        final MonetaryCurrency currency = loan.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        boolean isAccountTransfer = false;
        final Map<String, Object> accountingBridgeData = loan.deriveAccountingBridgeData(applicationCurrency.toData(),
                existingTransactionIds, existingReversedTransactionIds, isAccountTransfer);
        this.journalEntryWritePlatformService.createJournalEntriesForLoan(accountingBridgeData);
    }
}
