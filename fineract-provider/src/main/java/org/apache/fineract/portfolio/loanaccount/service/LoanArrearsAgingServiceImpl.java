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

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanApplyOverdueChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanDisbursalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanAddChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.charge.LoanWaiveChargeBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanChargePaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanForeClosurePostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionGoodwillCreditPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionMakeRepaymentPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionPayoutRefundPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanUndoWrittenOffBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanWaiveInterestBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummary;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@Slf4j
@RequiredArgsConstructor
public class LoanArrearsAgingServiceImpl implements LoanArrearsAgingService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    @PostConstruct
    public void registerForNotification() {
        businessEventNotifierService.addPostBusinessEventListener(LoanRefundPostBusinessEvent.class, new RefundEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanAdjustTransactionBusinessEvent.class,
                new AdjustTransactionBusinessEventEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionMakeRepaymentPostBusinessEvent.class,
                new MakeRepaymentEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionGoodwillCreditPostBusinessEvent.class,
                new GoodwillCreditEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanTransactionPayoutRefundPostBusinessEvent.class,
                new PaymentRefundEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanUndoWrittenOffBusinessEvent.class, new UndoWrittenOffEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanWaiveInterestBusinessEvent.class, new WaiveInterestEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanAddChargeBusinessEvent.class, new AddChargeEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanWaiveChargeBusinessEvent.class, new WaiveChargeEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanChargePaymentPostBusinessEvent.class,
                new LoanChargePaymentEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanApplyOverdueChargeBusinessEvent.class,
                new ApplyOverdueChargeEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanDisbursalBusinessEvent.class, new DisbursementEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanForeClosurePostBusinessEvent.class,
                new LoanForeClosureEventListener());
        businessEventNotifierService.addPostBusinessEventListener(LoanBalanceChangedBusinessEvent.class,
                new LoanBalanceChangedEventListener());
    }

    @Override
    public void updateLoanArrearsAgeingDetailsWithOriginalSchedule(final Loan loan) {
        int count = this.jdbcTemplate.queryForObject("select count(mla.loan_id) from m_loan_arrears_aging mla where mla.loan_id =?",
                Integer.class, loan.getId());
        List<String> updateStatement = new ArrayList<>();
        OriginalScheduleExtractor originalScheduleExtractor = new OriginalScheduleExtractor(loan.getId().toString(), sqlGenerator);
        Map<Long, List<LoanSchedulePeriodData>> scheduleDate = this.jdbcTemplate.query(originalScheduleExtractor.schema,
                originalScheduleExtractor);
        if (scheduleDate.size() > 0) {
            List<Map<String, Object>> transactions = getLoanSummary(loan.getId(), loan.getLoanSummary());
            updateScheduleWithPaidDetail(scheduleDate, transactions);
            createInsertStatements(updateStatement, scheduleDate, count == 0);
            if (updateStatement.size() == 1) {
                this.jdbcTemplate.update(updateStatement.get(0));
            } else {
                String deletestatement = "DELETE FROM m_loan_arrears_aging WHERE  loan_id=?";
                this.jdbcTemplate.update(deletestatement, loan.getId()); // NOSONAR
            }
        }
    }

    @Override
    public Map<Long, List<LoanSchedulePeriodData>> getScheduleDate(String loanId) {
        OriginalScheduleExtractor originalScheduleExtractor = new OriginalScheduleExtractor(loanId, sqlGenerator);
        return this.jdbcTemplate.query(originalScheduleExtractor.schema, originalScheduleExtractor);
    }

    @Override
    public void updateLoanArrearsAgeingDetails(final Loan loan) {
        if (loan != null) {
            int count = this.jdbcTemplate.queryForObject("select count(mla.loan_id) from m_loan_arrears_aging mla where mla.loan_id =?",
                    Integer.class, loan.getId());
            String updateStatement = constructUpdateStatement(loan, count == 0);
            if (updateStatement == null) {
                String deletestatement = "DELETE FROM m_loan_arrears_aging WHERE  loan_id=?";
                this.jdbcTemplate.update(deletestatement, loan.getId()); // NOSONAR
            } else {
                this.jdbcTemplate.update(updateStatement);
            }
        }
    }

    private String constructUpdateStatement(final Loan loan, boolean isInsertStatement) {
        String updateSql = null;
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        BigDecimal principalOverdue = BigDecimal.ZERO;
        BigDecimal interestOverdue = BigDecimal.ZERO;
        BigDecimal feeOverdue = BigDecimal.ZERO;
        BigDecimal penaltyOverdue = BigDecimal.ZERO;
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate overDueSince = businessDate;
        for (LoanRepaymentScheduleInstallment installment : installments) {
            if (DateUtils.isBefore(installment.getDueDate(), businessDate)) {
                principalOverdue = principalOverdue.add(installment.getPrincipalOutstanding(loan.getCurrency()).getAmount());
                interestOverdue = interestOverdue.add(installment.getInterestOutstanding(loan.getCurrency()).getAmount());
                feeOverdue = feeOverdue.add(installment.getFeeChargesOutstanding(loan.getCurrency()).getAmount());
                penaltyOverdue = penaltyOverdue.add(installment.getPenaltyChargesOutstanding(loan.getCurrency()).getAmount());
                if (installment.isNotFullyPaidOff() && DateUtils.isAfter(overDueSince, installment.getDueDate())) {
                    overDueSince = installment.getDueDate();
                }
            }
        }

        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        if (totalOverDue.compareTo(BigDecimal.ZERO) > 0) {
            if (isInsertStatement) {
                updateSql = constructInsertStatement(loan.getId(), principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                        overDueSince);
            } else {
                updateSql = constructUpdateStatement(loan.getId(), principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                        overDueSince);
            }
        }
        return updateSql;
    }

    private List<Map<String, Object>> getLoanSummary(final Long loanId, final LoanSummary loanSummary) {
        List<Map<String, Object>> transactionDetail = new ArrayList<>();
        Map<String, Object> transactionMap = new HashMap<>();

        transactionMap.put("loanId", loanId);
        transactionMap.put("principalAmtPaid", loanSummary.getTotalPrincipalRepaid());
        transactionMap.put("principalAmtWrittenoff", loanSummary.getTotalPrincipalWrittenOff());
        transactionMap.put("interestAmtPaid", loanSummary.getTotalInterestRepaid());
        transactionMap.put("interestAmtWaived", loanSummary.getTotalInterestWaived());
        transactionMap.put("feeAmtPaid", loanSummary.getTotalFeeChargesRepaid());
        transactionMap.put("feeAmtWaived", loanSummary.getTotalFeeChargesWaived());
        transactionMap.put("penaltyAmtPaid", loanSummary.getTotalPenaltyChargesRepaid());
        transactionMap.put("penaltyAmtWaived", loanSummary.getTotalPenaltyChargesWaived());
        transactionDetail.add(transactionMap);
        return transactionDetail;

    }

    @Override
    public void createInsertStatements(List<String> insertStatement, Map<Long, List<LoanSchedulePeriodData>> scheduleDate,
            boolean isInsertStatement) {
        for (Map.Entry<Long, List<LoanSchedulePeriodData>> entry : scheduleDate.entrySet()) {
            final Long loanId = entry.getKey();
            BigDecimal principalOverdue = BigDecimal.ZERO;
            BigDecimal interestOverdue = BigDecimal.ZERO;
            BigDecimal feeOverdue = BigDecimal.ZERO;
            BigDecimal penaltyOverdue = BigDecimal.ZERO;
            LocalDate overDueSince = DateUtils.getBusinessLocalDate();

            for (LoanSchedulePeriodData loanSchedulePeriodData : entry.getValue()) {
                if (!loanSchedulePeriodData.getComplete()) {
                    principalOverdue = principalOverdue
                            .add(loanSchedulePeriodData.getPrincipalDue().subtract(loanSchedulePeriodData.getPrincipalPaid()));
                    interestOverdue = interestOverdue
                            .add(loanSchedulePeriodData.getInterestDue().subtract(loanSchedulePeriodData.getInterestPaid()));
                    feeOverdue = feeOverdue
                            .add(loanSchedulePeriodData.getFeeChargesDue().subtract(loanSchedulePeriodData.getFeeChargesPaid()));
                    penaltyOverdue = penaltyOverdue
                            .add(loanSchedulePeriodData.getPenaltyChargesDue().subtract(loanSchedulePeriodData.getPenaltyChargesPaid()));
                    if (DateUtils.isAfter(overDueSince, loanSchedulePeriodData.getDueDate()) && MathUtil
                            .isGreaterThan(loanSchedulePeriodData.getPrincipalDue(), loanSchedulePeriodData.getPrincipalPaid())) {
                        overDueSince = loanSchedulePeriodData.getDueDate();
                    }
                }
            }
            if (principalOverdue.compareTo(BigDecimal.ZERO) > 0) {
                String sqlStatement = null;
                if (isInsertStatement) {
                    sqlStatement = constructInsertStatement(loanId, principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                            overDueSince);
                } else {
                    sqlStatement = constructUpdateStatement(loanId, principalOverdue, interestOverdue, feeOverdue, penaltyOverdue,
                            overDueSince);
                }
                insertStatement.add(sqlStatement);
            }

        }
    }

    private String constructInsertStatement(final Long loanId, BigDecimal principalOverdue, BigDecimal interestOverdue,
            BigDecimal feeOverdue, BigDecimal penaltyOverdue, LocalDate overDueSince) {
        final StringBuilder insertStatementBuilder = new StringBuilder(900);
        insertStatementBuilder.append("INSERT INTO m_loan_arrears_aging(loan_id,principal_overdue_derived,interest_overdue_derived,")
                .append("fee_charges_overdue_derived,penalty_charges_overdue_derived,total_overdue_derived,overdue_since_date_derived) VALUES(");
        insertStatementBuilder.append(loanId).append(",");
        insertStatementBuilder.append(principalOverdue).append(",");
        insertStatementBuilder.append(interestOverdue).append(",");
        insertStatementBuilder.append(feeOverdue).append(",");
        insertStatementBuilder.append(penaltyOverdue).append(",");
        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        insertStatementBuilder.append(totalOverDue).append(",'");
        insertStatementBuilder.append(this.formatter.format(overDueSince)).append("')");
        return insertStatementBuilder.toString();
    }

    private String constructUpdateStatement(final Long loanId, BigDecimal principalOverdue, BigDecimal interestOverdue,
            BigDecimal feeOverdue, BigDecimal penaltyOverdue, LocalDate overDueSince) {
        final StringBuilder insertStatementBuilder = new StringBuilder(900);
        insertStatementBuilder.append("UPDATE m_loan_arrears_aging SET principal_overdue_derived=");
        insertStatementBuilder.append(principalOverdue).append(", interest_overdue_derived=");
        insertStatementBuilder.append(interestOverdue).append(", fee_charges_overdue_derived=");
        insertStatementBuilder.append(feeOverdue).append(", penalty_charges_overdue_derived=");
        insertStatementBuilder.append(penaltyOverdue).append(", total_overdue_derived=");
        BigDecimal totalOverDue = principalOverdue.add(interestOverdue).add(feeOverdue).add(penaltyOverdue);
        insertStatementBuilder.append(totalOverDue).append(",overdue_since_date_derived= '");
        insertStatementBuilder.append(this.formatter.format(overDueSince)).append("' ");
        insertStatementBuilder.append("WHERE  loan_id=").append(loanId);
        return insertStatementBuilder.toString();
    }

    @Override
    public void updateScheduleWithPaidDetail(Map<Long, List<LoanSchedulePeriodData>> scheduleDate, List<Map<String, Object>> loanSummary) {
        for (Map<String, Object> transactionMap : loanSummary) {
            String longValue = transactionMap.get("loanId").toString();
            Long loanId = Long.parseLong(longValue);
            BigDecimal principalAmtPaid = (BigDecimal) transactionMap.get("principalAmtPaid");
            BigDecimal principalAmtWrittenoff = (BigDecimal) transactionMap.get("principalAmtWrittenoff");
            BigDecimal interestAmtPaid = (BigDecimal) transactionMap.get("interestAmtPaid");
            BigDecimal interestAmtWaived = (BigDecimal) transactionMap.get("interestAmtWaived");
            BigDecimal feeAmtPaid = (BigDecimal) transactionMap.get("feeAmtPaid");
            BigDecimal feeAmtWaived = (BigDecimal) transactionMap.get("feeAmtWaived");
            BigDecimal penaltyAmtPaid = (BigDecimal) transactionMap.get("penaltyAmtPaid");
            BigDecimal penaltyAmtWaived = (BigDecimal) transactionMap.get("penaltyAmtWaived");

            BigDecimal principalAmt = principalAmtPaid.add(principalAmtWrittenoff);
            BigDecimal interestAmt = interestAmtPaid.add(interestAmtWaived);
            BigDecimal feeAmt = feeAmtPaid.add(feeAmtWaived);
            BigDecimal penaltyAmt = penaltyAmtPaid.add(penaltyAmtWaived);

            List<LoanSchedulePeriodData> loanSchedulePeriodDataList = scheduleDate.get(loanId);
            if (loanSchedulePeriodDataList != null) {
                List<LoanSchedulePeriodData> updatedPeriodData = new ArrayList<>(loanSchedulePeriodDataList.size());
                for (LoanSchedulePeriodData loanSchedulePeriodData : loanSchedulePeriodDataList) {
                    BigDecimal principalPaid;
                    BigDecimal interestPaid;
                    BigDecimal feeChargesPaid;
                    BigDecimal penaltyChargesPaid;
                    boolean isComplete = true;
                    if (loanSchedulePeriodData.getPrincipalDue().compareTo(principalAmt) > 0) {
                        principalPaid = principalAmt;
                        principalAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        principalPaid = loanSchedulePeriodData.getPrincipalDue();
                        principalAmt = principalAmt.subtract(loanSchedulePeriodData.getPrincipalDue());
                    }

                    if (loanSchedulePeriodData.getInterestDue().compareTo(interestAmt) > 0) {
                        interestPaid = interestAmt;
                        interestAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        interestPaid = loanSchedulePeriodData.getInterestDue();
                        interestAmt = interestAmt.subtract(loanSchedulePeriodData.getInterestDue());
                    }
                    if (loanSchedulePeriodData.getFeeChargesDue().compareTo(feeAmt) > 0) {
                        feeChargesPaid = feeAmt;
                        feeAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        feeChargesPaid = loanSchedulePeriodData.getFeeChargesDue();
                        feeAmt = feeAmt.subtract(loanSchedulePeriodData.getFeeChargesDue());
                    }
                    if (loanSchedulePeriodData.getPenaltyChargesDue().compareTo(penaltyAmt) > 0) {
                        penaltyChargesPaid = penaltyAmt;
                        penaltyAmt = BigDecimal.ZERO;
                        isComplete = false;
                    } else {
                        penaltyChargesPaid = loanSchedulePeriodData.getPenaltyChargesDue();
                        penaltyAmt = penaltyAmt.subtract(loanSchedulePeriodData.getPenaltyChargesDue());
                    }

                    LoanSchedulePeriodData periodData = LoanSchedulePeriodData.withPaidDetail(loanSchedulePeriodData, isComplete,
                            principalPaid, interestPaid, feeChargesPaid, penaltyChargesPaid);
                    updatedPeriodData.add(periodData);
                }
                loanSchedulePeriodDataList.clear();
                loanSchedulePeriodDataList.addAll(updatedPeriodData);
            }
        }
    }

    private static final class OriginalScheduleExtractor implements ResultSetExtractor<Map<Long, List<LoanSchedulePeriodData>>> {

        private final String schema;

        OriginalScheduleExtractor(final String loanIdsAsString, DatabaseSpecificSQLGenerator sqlGenerator) {
            final StringBuilder scheduleDetail = new StringBuilder();
            scheduleDetail.append("select ml.id as loanId, mr.duedate as dueDate, mr.principal_amount as principalAmount, ");
            scheduleDetail.append(
                    "mr.interest_amount as interestAmount, mr.fee_charges_amount as feeAmount, mr.penalty_charges_amount as penaltyAmount  ");
            scheduleDetail.append("from m_loan ml  INNER JOIN m_loan_repayment_schedule_history mr on mr.loan_id = ml.id ");
            scheduleDetail.append("where mr.duedate  < "
                    + sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day") + " and ");
            scheduleDetail.append("ml.id IN(").append(loanIdsAsString).append(") and  mr.version = (");
            scheduleDetail.append("select max(lrs.version) from m_loan_repayment_schedule_history lrs where mr.loan_id = lrs.loan_id");
            scheduleDetail.append(") order by ml.id,mr.duedate");
            this.schema = scheduleDetail.toString();
        }

        @Override
        public Map<Long, List<LoanSchedulePeriodData>> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = new HashMap<>();

            while (rs.next()) {
                Long loanId = rs.getLong("loanId");

                List<LoanSchedulePeriodData> periodDatas = scheduleDate.computeIfAbsent(loanId, k -> new ArrayList<>());

                periodDatas.add(fetchLoanSchedulePeriodData(rs));
            }

            return scheduleDate;
        }

        private LoanSchedulePeriodData fetchLoanSchedulePeriodData(ResultSet rs) throws SQLException {
            final LocalDate dueDate = JdbcSupport.getLocalDate(rs, "dueDate");
            final BigDecimal principalDue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalAmount");
            final BigDecimal interestDueOnPrincipalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestAmount");
            final BigDecimal totalInstallmentAmount = principalDue.add(interestDueOnPrincipalOutstanding);
            final BigDecimal feeChargesDueForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeAmount");
            final BigDecimal penaltyChargesDueForPeriod = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyAmount");
            final Integer periodNumber = null;
            final LocalDate fromDate = null;
            final BigDecimal principalOutstanding = null;
            final BigDecimal totalDueForPeriod = null;
            return LoanSchedulePeriodData.repaymentOnlyPeriod(periodNumber, fromDate, dueDate, principalDue, principalOutstanding,
                    interestDueOnPrincipalOutstanding, feeChargesDueForPeriod, penaltyChargesDueForPeriod, totalDueForPeriod,
                    totalInstallmentAmount);

        }
    }

    private void handleArrearsForLoan(Loan loan) {
        if (loan != null && loan.isOpen() && loan.repaymentScheduleDetail().isInterestRecalculationEnabled()
                && loan.loanProduct().isArrearsBasedOnOriginalSchedule()) {
            updateLoanArrearsAgeingDetailsWithOriginalSchedule(loan);
        } else {
            updateLoanArrearsAgeingDetails(loan);
        }
    }

    private final class RefundEventListener implements BusinessEventListener<LoanRefundPostBusinessEvent> {

        @SuppressWarnings("unused")
        @Override
        public void onBusinessEvent(LoanRefundPostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class AdjustTransactionBusinessEventEventListener implements BusinessEventListener<LoanAdjustTransactionBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanAdjustTransactionBusinessEvent event) {
            LoanTransaction loanTransaction = event.get().getTransactionToAdjust();
            if (loanTransaction == null) {
                loanTransaction = event.get().getNewTransactionDetail();
            }
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class MakeRepaymentEventListener implements BusinessEventListener<LoanTransactionMakeRepaymentPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanTransactionMakeRepaymentPostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class UndoWrittenOffEventListener implements BusinessEventListener<LoanUndoWrittenOffBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanUndoWrittenOffBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class WaiveInterestEventListener implements BusinessEventListener<LoanWaiveInterestBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanWaiveInterestBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class LoanForeClosureEventListener implements BusinessEventListener<LoanForeClosurePostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanForeClosurePostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class LoanChargePaymentEventListener implements BusinessEventListener<LoanChargePaymentPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanChargePaymentPostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class AddChargeEventListener implements BusinessEventListener<LoanAddChargeBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanAddChargeBusinessEvent event) {
            LoanCharge loanCharge = event.get();
            Loan loan = loanCharge.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class WaiveChargeEventListener implements BusinessEventListener<LoanWaiveChargeBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanWaiveChargeBusinessEvent event) {
            LoanCharge loanCharge = event.get();
            Loan loan = loanCharge.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class ApplyOverdueChargeEventListener implements BusinessEventListener<LoanApplyOverdueChargeBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanApplyOverdueChargeBusinessEvent event) {
            Loan loan = event.get();
            handleArrearsForLoan(loan);
        }
    }

    private final class DisbursementEventListener implements BusinessEventListener<LoanDisbursalBusinessEvent> {

        @SuppressWarnings("unused")
        @Override
        public void onBusinessEvent(LoanDisbursalBusinessEvent event) {
            Loan loan = event.get();
            updateLoanArrearsAgeingDetails(loan);
        }
    }

    private final class GoodwillCreditEventListener implements BusinessEventListener<LoanTransactionGoodwillCreditPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanTransactionGoodwillCreditPostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class PaymentRefundEventListener implements BusinessEventListener<LoanTransactionPayoutRefundPostBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanTransactionPayoutRefundPostBusinessEvent event) {
            LoanTransaction loanTransaction = event.get();
            Loan loan = loanTransaction.getLoan();
            handleArrearsForLoan(loan);
        }
    }

    private final class LoanBalanceChangedEventListener implements BusinessEventListener<LoanBalanceChangedBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanBalanceChangedBusinessEvent event) {
            Loan loan = event.get();
            handleArrearsForLoan(loan);
        }
    }
}
