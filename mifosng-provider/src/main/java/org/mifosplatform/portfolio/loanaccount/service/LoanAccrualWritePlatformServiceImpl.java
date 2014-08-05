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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.exception.JobExecutionException;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.loanaccount.data.LoanScheduleAccrualData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTransactionType;
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
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final JpaTransactionManager transactionManager;

    @Autowired
    public LoanAccrualWritePlatformServiceImpl(final RoutingDataSource dataSource, final LoanReadPlatformService loanReadPlatformService,
            final JournalEntryWritePlatformService journalEntryWritePlatformService, final JpaTransactionManager transactionManager) {
        this.loanReadPlatformService = loanReadPlatformService;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
        this.transactionManager = transactionManager;
    }

    @Override
    @CronTarget(jobName = JobName.ADD_ACCRUAL_ENTRIES)
    public void addAccrualAccounting() throws JobExecutionException {
        Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas = this.loanReadPlatformService.retriveScheduleAccrualData();
        StringBuilder sb = new StringBuilder();
        Set<Long> loansIds = new HashSet<>();
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            try {
                if (!loansIds.contains(accrualData.getLoanId())) {
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

    private String addPeriodicAccruals(final LocalDate tilldate, Collection<LoanScheduleAccrualData> loanScheduleAccrualDatas) {
        StringBuilder sb = new StringBuilder();
        Set<Long> loansIds = new HashSet<>();
        LocalDate accruredTill = null;
        for (final LoanScheduleAccrualData accrualData : loanScheduleAccrualDatas) {
            try {
                if (!loansIds.contains(accrualData.getLoanId())) {
                    if (accrualData.getDueDateAsLocaldate().isAfter(tilldate)) {
                        //if (accruredTill == null) {
                            accruredTill = accrualData.getAccruedTill();
                        //}
                        addAccrualTillSpecificDate(tilldate, accrualData, accruredTill);
                    } else {
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
        }
        return sb.toString();
    }

    private void addAccrualTillSpecificDate(final LocalDate tilldate, final LoanScheduleAccrualData accrualData,
            final LocalDate accruredTill) throws Exception {
        int totalNumberOfDays = Days.daysBetween(accrualData.getFromDateAsLocaldate(), accrualData.getDueDateAsLocaldate()).getDays();
        LocalDate startDate = accruredTill;
        if (startDate == null) {
            startDate = accrualData.getFromDateAsLocaldate();
        }
        int daysToBeAccrued = Days.daysBetween(startDate, tilldate).getDays();
        if (daysToBeAccrued < 1) { return; }
        int daysInSchedule = Days.daysBetween(accrualData.getFromDateAsLocaldate(), tilldate).getDays();
        switch (accrualData.getRepaymentFrequency()) {
            case MONTHS:
                totalNumberOfDays = calculateTotalNumberOfDaysForMonth(totalNumberOfDays, accrualData);
            break;
            case YEARS:
                totalNumberOfDays = calculateTotalNumberOfDaysForYear(totalNumberOfDays, accrualData);
            break;
            default:
            break;
        }

        double interestPerDay = accrualData.getInterestIncome().doubleValue() / totalNumberOfDays;
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal feeportion = accrualData.getDueDateFeeIncome();
        BigDecimal penaltyportion = accrualData.getDueDatePenaltyIncome();
        if (daysInSchedule >= totalNumberOfDays) {
            interestportion = accrualData.getInterestIncome();
        } else {
            double iterest = interestPerDay * daysToBeAccrued;
            interestportion = BigDecimal.valueOf(iterest);
        }
        interestportion = interestportion.setScale(accrualData.getCurrencyData().decimalPlaces(), RoundingMode.HALF_EVEN);
        amount = amount.add(interestportion);

        BigDecimal totalAccInterest = accrualData.getAccruedInterestIncome();
        if (totalAccInterest == null) {
            totalAccInterest = BigDecimal.ZERO;
        }
        totalAccInterest = totalAccInterest.add(interestportion);
        BigDecimal totalAccFee = accrualData.getAccruedFeeIncome();

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
        BigDecimal totalAccPenalty = accrualData.getAccruedPenaltyIncome();
        if (penaltyportion != null) {
            if (totalAccPenalty == null) {
                totalAccPenalty = BigDecimal.ZERO;
            }
            penaltyportion = penaltyportion.subtract(totalAccPenalty);
            amount = amount.add(penaltyportion);
            totalAccPenalty = totalAccFee.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) != 0) {
            addAccrualAccounting(accrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                    totalAccPenalty, tilldate);
        }
    }

    private int calculateTotalNumberOfDaysForMonth(final int totalNumberOfDays, final LoanScheduleAccrualData accrualData) {
        int numberOfDays = totalNumberOfDays;
        DaysInMonthType type = DaysInMonthType.fromInt(accrualData.getNumberOfDaysInMonth());
        switch (type) {
            case DAYS_30:
                numberOfDays = 30 * accrualData.getRepayEvery();
            break;
            default:
            break;
        }
        return numberOfDays;
    }

    private int calculateTotalNumberOfDaysForYear(final int totalNumberOfDays, final LoanScheduleAccrualData accrualData) {
        int numberOfDays = totalNumberOfDays;
        DaysInYearType type = DaysInYearType.fromInt(accrualData.getNumberOfDaysInMonth());
        switch (type) {
            case DAYS_360:
                numberOfDays = 360 * accrualData.getRepayEvery();
            break;
            case DAYS_364:
                numberOfDays = 364 * accrualData.getRepayEvery();
            break;
            case DAYS_365:
                numberOfDays = 365 * accrualData.getRepayEvery();
            break;
            default:
            break;
        }
        return numberOfDays;
    }

    @Transactional
    public void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData) throws Exception {

        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal interestportion = null;
        BigDecimal totalAccInterest = null;
        if (scheduleAccrualData.getInterestIncome() != null) {
            interestportion = scheduleAccrualData.getInterestIncome();
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
        if (scheduleAccrualData.getFeeIncome() != null) {
            feeportion = scheduleAccrualData.getFeeIncome();
            totalAccFee = feeportion;
            if (scheduleAccrualData.getAccruedFeeIncome() != null) {
                feeportion.subtract(scheduleAccrualData.getAccruedFeeIncome());
            }
            amount = amount.add(feeportion);
            if (feeportion.compareTo(BigDecimal.ZERO) == 0) {
                feeportion = null;
            }
        }

        BigDecimal penaltyportion = null;
        BigDecimal totalAccPenalty = null;
        if (scheduleAccrualData.getPenaltyIncome() != null) {
            penaltyportion = scheduleAccrualData.getPenaltyIncome();
            totalAccPenalty = penaltyportion;
            if (scheduleAccrualData.getAccruedPenaltyIncome() != null) {
                penaltyportion.subtract(scheduleAccrualData.getAccruedPenaltyIncome());
            }
            amount = amount.add(penaltyportion);
            if (penaltyportion.compareTo(BigDecimal.ZERO) == 0) {
                penaltyportion = null;
            }
        }
        addAccrualAccounting(scheduleAccrualData, amount, interestportion, totalAccInterest, feeportion, totalAccFee, penaltyportion,
                totalAccPenalty, scheduleAccrualData.getDueDateAsLocaldate());
    }

    private void addAccrualAccounting(LoanScheduleAccrualData scheduleAccrualData, BigDecimal amount, BigDecimal interestportion,
            BigDecimal totalAccInterest, BigDecimal feeportion, BigDecimal totalAccFee, BigDecimal penaltyportion,
            BigDecimal totalAccPenalty, final LocalDate accruredTill) throws Exception {
        TransactionStatus transactionStatus = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            String transactionSql = "INSERT INTO m_loan_transaction  (loan_id,office_id,is_reversed,transaction_type_enum,transaction_date,amount,interest_portion_derived,"
                    + "fee_charges_portion_derived,penalty_charges_portion_derived) VALUES (?, ?, 0, ?, ?, ?, ?, ?, ?)";
            this.jdbcTemplate.update(transactionSql, scheduleAccrualData.getLoanId(), scheduleAccrualData.getOfficeId(),
                    LoanTransactionType.ACCRUAL.getValue(), accruredTill.toDate(), amount, interestportion, feeportion, penaltyportion);
            final Long transactonId = this.jdbcTemplate.queryForLong("SELECT LAST_INSERT_ID()");

            Map<String, Object> transactionMap = toMapData(transactonId, amount, interestportion, feeportion, penaltyportion,
                    scheduleAccrualData);

            String repaymetUpdatesql = "UPDATE m_loan_repayment_schedule SET accrual_interest_derived=?, accrual_fee_charges_derived=?, "
                    + "accrual_penalty_charges_derived=? WHERE  id=?";
            this.jdbcTemplate.update(repaymetUpdatesql, totalAccInterest, totalAccFee, totalAccPenalty,
                    scheduleAccrualData.getRepaymentScheduleId());

            String updateLoan = "UPDATE m_loan  SET accrued_till=?  WHERE  id=?";
            this.jdbcTemplate.update(updateLoan, accruredTill.toDate(), scheduleAccrualData.getLoanId());
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
            final BigDecimal feeportion, final BigDecimal penaltyportion, final LoanScheduleAccrualData loanScheduleAccrualData) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(LoanTransactionType.ACCRUAL);

        thisTransactionData.put("id", id);
        thisTransactionData.put("officeId", loanScheduleAccrualData.getOfficeId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", false);
        thisTransactionData.put("date", loanScheduleAccrualData.getDueDateAsLocaldate());
        thisTransactionData.put("currency", loanScheduleAccrualData.getCurrencyData());
        thisTransactionData.put("amount", amount);
        thisTransactionData.put("principalPortion", null);
        thisTransactionData.put("interestPortion", interestportion);
        thisTransactionData.put("feeChargesPortion", feeportion);
        thisTransactionData.put("penaltyChargesPortion", penaltyportion);
        thisTransactionData.put("overPaymentPortion", null);

        return thisTransactionData;
    }

}
