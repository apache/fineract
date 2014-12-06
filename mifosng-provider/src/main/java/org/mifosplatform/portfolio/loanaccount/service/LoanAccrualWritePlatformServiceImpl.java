/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.loanaccount.data.LoanChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanInstallmentChargeData;
import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;
import org.mifosplatform.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class LoanAccrualWritePlatformServiceImpl implements LoanAccrualWritePlatformService {

    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final JpaTransactionManager transactionManager;

    @Autowired
    public LoanAccrualWritePlatformServiceImpl(final RoutingDataSource dataSource, final LoanReadPlatformService loanReadPlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final JpaTransactionManager transactionManager,
            final LoanChargeReadPlatformService loanChargeReadPlatformService) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.transactionManager = transactionManager;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
    }

    @Override
    @CronTarget(jobName = JobName.ADD_ACCRUAL_ENTRIES)
    public void addAccrualAccounting() throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retriveScheduleAccrualData();
        StringBuilder sb = new StringBuilder();
        Set<Long> loansIds = new HashSet<>();
        Map<Long, Collection<LoanChargeData>> loanChargeMap = new HashMap<>();
        Map<Long, Collection<LoanTransactionData>> loanWaiverTansactions = new HashMap<>();
        Map<Long, Collection<LoanSchedulePeriodData>> loanWaiverSchedules = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            try {
                if (!loansIds.contains(accrualData.getLoanId())) {
                    if (!loanChargeMap.containsKey(accrualData.getLoanId())) {
                        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService
                                .retrieveLoanChargesForAccural(accrualData.getLoanId());
                        loanChargeMap.put(accrualData.getLoanId(), chargeData);
                    }
                    if (accrualData.getWaivedInterestIncome() != null && !loanWaiverTansactions.containsKey(accrualData.getLoanId())) {
                        loanWaiverSchedules.put(accrualData.getLoanId(),
                                this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId()));
                        loanWaiverTansactions.put(accrualData.getLoanId(),
                                this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId()));
                    }
                    updateCharges(loanChargeMap.get(accrualData.getLoanId()), accrualData, accrualData.getFromDateAsLocaldate(),
                            accrualData.getDueDateAsLocaldate());
                    updateInterestIncome(accrualData, loanWaiverTansactions, loanWaiverSchedules, accrualData.getDueDateAsLocaldate());
                    addAccrualAccounting(accrualData);
                }
            } catch (Exception e) {
                loansIds.add(accrualData.getLoanId());
                Throwable realCause = e;
                if (e.getCause() != null) {
                    realCause = e.getCause();
                }
                sb.append("failed to add accural transaction for repayment with id " + accrualData.getRepaymentScheduleId()
                        + " with message " + realCause.getMessage());
            }
        }

        if (sb.length() > 0) { throw new JobExecutionException(sb.toString()); }
    }

    @Override
    @CronTarget(jobName = JobName.ADD_PERIODIC_ACCRUAL_ENTRIES)
    public void addPeriodicAccruals() throws JobExecutionException {
        String errors = addPeriodicAccruals(LocalDate.now());
        if (errors.length() > 0) { throw new JobExecutionException(errors); }
    }

    @Override
    public String addPeriodicAccruals(final LocalDate tilldate) {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retrivePeriodicAccrualData(tilldate);
        return addPeriodicAccruals(tilldate, loanScheduleAccrualDatas);
    }

    @Override
    public String addPeriodicAccruals(final LocalDate tilldate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas) {
        StringBuilder sb = new StringBuilder();
        Set<Long> loansIds = new HashSet<>();
        LocalDate accruredTill = null;
        Long lastLoanId = null;
        Map<Long, Collection<LoanChargeData>> loanChargeMap = new HashMap<>();
        Map<Long, Collection<LoanTransactionData>> loanWaiverTansactions = new HashMap<>();
        Map<Long, Collection<LoanSchedulePeriodData>> loanWaiverSchedules = new HashMap<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            try {
                if (!loansIds.contains(accrualData.getLoanId())) {
                    if (accrualData.getWaivedInterestIncome() != null && !loanWaiverTansactions.containsKey(accrualData.getLoanId())) {
                        loanWaiverSchedules.put(accrualData.getLoanId(),
                                this.loanReadPlatformService.fetchWaiverInterestRepaymentData(accrualData.getLoanId()));
                        loanWaiverTansactions.put(accrualData.getLoanId(),
                                this.loanReadPlatformService.retrieveWaiverLoanTransactions(accrualData.getLoanId()));
                    }

                    if (!loanChargeMap.containsKey(accrualData.getLoanId())) {
                        Collection<LoanChargeData> chargeData = this.loanChargeReadPlatformService
                                .retrieveLoanChargesForAccural(accrualData.getLoanId());
                        loanChargeMap.put(accrualData.getLoanId(), chargeData);
                    }
                    if (accrualData.getDueDateAsLocaldate().isAfter(tilldate)) {
                        if (accruredTill == null || lastLoanId == null || !lastLoanId.equals(accrualData.getLoanId())) {
                            accruredTill = accrualData.getAccruedTill();
                        }
                        if (accruredTill == null || accruredTill.isBefore(tilldate)) {
                            updateCharges(loanChargeMap.get(accrualData.getLoanId()), accrualData, accrualData.getFromDateAsLocaldate(),
                                    tilldate);
                            updateInterestIncome(accrualData, loanWaiverTansactions, loanWaiverSchedules, tilldate);
                            addAccrualTillSpecificDate(tilldate, accrualData);
                        }
                    } else {
                        updateCharges(loanChargeMap.get(accrualData.getLoanId()), accrualData, accrualData.getFromDateAsLocaldate(),
                                accrualData.getDueDateAsLocaldate());
                        updateInterestIncome(accrualData, loanWaiverTansactions, loanWaiverSchedules, tilldate);
                        addAccrualAccounting(accrualData);
                        accruredTill = accrualData.getDueDateAsLocaldate();
                    }
                }
            } catch (Exception e) {
                loansIds.add(accrualData.getLoanId());
                Throwable realCause = e;
                if (e.getCause() != null) {
                    realCause = e.getCause();
                }
                sb.append("failed to add accural transaction for repayment with id " + accrualData.getRepaymentScheduleId()
                        + " with message " + realCause.getMessage());
            }
            lastLoanId = accrualData.getLoanId();
        }
        return sb.toString();
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
        interestportion = interestportion.setScale(accrualData.getCurrencyData().decimalPlaces(), RoundingMode.HALF_EVEN);

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
            BigDecimal totalAccPenalty, final LocalDate accruedTill) throws Exception {
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
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
        } catch (Exception e) {
            this.transactionManager.rollback(transactionStatus);
            throw e;
        }
        this.transactionManager.commit(transactionStatus);
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
            final Map<Long, Collection<LoanTransactionData>> loanWaiverTansactions,
            final Map<Long, Collection<LoanSchedulePeriodData>> loanWaiverSchedules, final LocalDate tilldate) {

        BigDecimal interestIncome = accrualData.getInterestIncome();
        if (accrualData.getWaivedInterestIncome() != null) {
            BigDecimal recognized = BigDecimal.ZERO;
            BigDecimal unrecognized = BigDecimal.ZERO;
            BigDecimal remainingAmt = BigDecimal.ZERO;
            Collection<LoanTransactionData> loanTransactionDatas = new ArrayList<>();
            Collection<LoanSchedulePeriodData> loanSchedulePeriodDatas = loanWaiverSchedules.get(accrualData.getLoanId());

            for (LoanTransactionData loanTransactionData : loanWaiverTansactions.get(accrualData.getLoanId())) {
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
}
