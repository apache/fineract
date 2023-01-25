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
package org.apache.fineract.portfolio.loanaccount.jobs.updateloanarrearsageing;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanArrearsAgeingUpdateHandler {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanArrearsAgingService loanArrearsAgingService;

    private void truncateLoanArrearsAgingDetails() {
        jdbcTemplate.execute("truncate table m_loan_arrears_aging");
    }

    private void deleteLoanArrearsAgingDetails(List<Long> loanIds) {
        // delete existing record for loan from m_loan_arrears_aging table
        for (Long loanId : loanIds) {
            jdbcTemplate.update("delete from m_loan_arrears_aging where loan_id=?", loanId);
        }
    }

    public void updateLoanArrearsAgeingDetailsForAllLoans() {
        truncateLoanArrearsAgingDetails();
        String insertSQLStatement = buildQueryForInsertAgeingDetails(Boolean.TRUE);
        List<String> insertStatements = updateLoanArrearsAgeingDetailsWithOriginalScheduleForAllLoans();
        insertStatements.add(0, insertSQLStatement);
        final int[] records = this.jdbcTemplate.batchUpdate(insertStatements.toArray(new String[0]));
        if (log.isDebugEnabled()) {
            int result = 0;
            for (int record : records) {
                result += record;
            }
            log.debug("Records affected by updateLoanArrearsAgeingDetails: {}", result);
        }
    }

    public void updateLoanArrearsAgeingDetails(List<Long> loanIdsForUpdate) {

        deleteLoanArrearsAgingDetails(loanIdsForUpdate);
        String insertSQLStatement = buildQueryForInsertAgeingDetails(Boolean.FALSE);
        List<Object[]> batch = new ArrayList<Object[]>();
        if (!loanIdsForUpdate.isEmpty()) {
            for (Long loanId : loanIdsForUpdate) {
                Object[] values = new Object[] { loanId };
                batch.add(values);
            }
        }
        final int[] recordsUpdatedWithoutOriginalSchedule = this.jdbcTemplate.batchUpdate(insertSQLStatement, batch);
        int[] recordsUpdatedWithOriginalSchedule = new int[0];
        List<String> insertStatements = updateLoanArrearsAgeingDetailsWithOriginalSchedule(loanIdsForUpdate);
        if (!insertStatements.isEmpty()) {
            recordsUpdatedWithOriginalSchedule = this.jdbcTemplate.batchUpdate(insertStatements.toArray(new String[0]));

        }
        if (log.isDebugEnabled()) {
            int result = 0;
            for (int recordWithoutOriginalSchedule : recordsUpdatedWithoutOriginalSchedule) {
                result += recordWithoutOriginalSchedule;
            }
            if (recordsUpdatedWithOriginalSchedule.length > 0) {
                for (int recordWithOriginalSchedule : recordsUpdatedWithOriginalSchedule) {
                    result += recordWithOriginalSchedule;
                }
            }
            log.debug("Records affected by updateLoanArrearsAgeingDetails: {}", result);
        }

    }

    private String buildQueryForInsertAgeingDetails(boolean isForAllLoans) {
        final StringBuilder insertSqlStatementBuilder = new StringBuilder(900);
        final String principalOverdueCalculationSql = "SUM(COALESCE(mr.principal_amount, 0) - coalesce(mr.principal_completed_derived, 0) - coalesce(mr.principal_writtenoff_derived, 0))";
        final String interestOverdueCalculationSql = "SUM(COALESCE(mr.interest_amount, 0) - coalesce(mr.interest_writtenoff_derived, 0) - coalesce(mr.interest_waived_derived, 0) - "
                + "coalesce(mr.interest_completed_derived, 0))";
        final String feeChargesOverdueCalculationSql = "SUM(COALESCE(mr.fee_charges_amount, 0) - coalesce(mr.fee_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.fee_charges_waived_derived, 0) - coalesce(mr.fee_charges_completed_derived, 0))";
        final String penaltyChargesOverdueCalculationSql = "SUM(COALESCE(mr.penalty_charges_amount, 0) - coalesce(mr.penalty_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.penalty_charges_waived_derived, 0) - coalesce(mr.penalty_charges_completed_derived, 0))";

        insertSqlStatementBuilder.append(
                "INSERT INTO m_loan_arrears_aging(loan_id,principal_overdue_derived,interest_overdue_derived,fee_charges_overdue_derived,penalty_charges_overdue_derived,total_overdue_derived,overdue_since_date_derived)");
        insertSqlStatementBuilder.append("select ml.id as loanId,");
        insertSqlStatementBuilder.append(principalOverdueCalculationSql + " as principal_overdue_derived,");
        insertSqlStatementBuilder.append(interestOverdueCalculationSql + " as interest_overdue_derived,");
        insertSqlStatementBuilder.append(feeChargesOverdueCalculationSql + " as fee_charges_overdue_derived,");
        insertSqlStatementBuilder.append(penaltyChargesOverdueCalculationSql + " as penalty_charges_overdue_derived,");
        insertSqlStatementBuilder.append(principalOverdueCalculationSql + "+" + interestOverdueCalculationSql + "+");
        insertSqlStatementBuilder
                .append(feeChargesOverdueCalculationSql + "+" + penaltyChargesOverdueCalculationSql + " as total_overdue_derived,");
        insertSqlStatementBuilder.append("MIN(mr.duedate) as overdue_since_date_derived ");
        insertSqlStatementBuilder.append(" FROM m_loan ml ");
        insertSqlStatementBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        insertSqlStatementBuilder.append(" left join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id ");
        insertSqlStatementBuilder.append(" WHERE ml.loan_status_id = 300 ");// active
        if (!isForAllLoans) {
            insertSqlStatementBuilder.append(" and ml.id IN (?)");
        }
        insertSqlStatementBuilder.append(" and mr.completed_derived is false ");
        insertSqlStatementBuilder.append(" and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day"))
                .append(" ");
        insertSqlStatementBuilder
                .append(" and (prd.arrears_based_on_original_schedule = false or prd.arrears_based_on_original_schedule is null) ");
        insertSqlStatementBuilder.append(" GROUP BY ml.id");
        return insertSqlStatementBuilder.toString();
    }

    private List<String> updateLoanArrearsAgeingDetailsWithOriginalSchedule(List<Long> loanIdsForUpdate) {
        List<String> insertStatement = new ArrayList<>();
        String sqlForLoanIdentifiers = buildQueryForLoanIdentifiersWithOriginalSchedule(Boolean.FALSE);
        List<Object> loanIdsForQuery = new ArrayList<>();
        for (Long loanId : loanIdsForUpdate) {
            loanIdsForQuery.add(loanId);
        }
        List<Long> loanIds = this.jdbcTemplate.queryForList(sqlForLoanIdentifiers, loanIdsForQuery.toArray(), new int[] { Types.BIGINT },
                Long.class);
        if (!loanIds.isEmpty()) {
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = getScheduleDate(loanIds);
            List<Map<String, Object>> loanSummary = getLoanSummary(loanIds);
            loanArrearsAgingService.updateScheduleWithPaidDetail(scheduleDate, loanSummary);
            loanArrearsAgingService.createInsertStatements(insertStatement, scheduleDate, true);
        }

        return insertStatement;
    }

    private List<String> updateLoanArrearsAgeingDetailsWithOriginalScheduleForAllLoans() {
        List<String> insertStatement = new ArrayList<>();
        String sqlForLoanIdentifiers = buildQueryForLoanIdentifiersWithOriginalSchedule(Boolean.TRUE);
        List<Long> loanIds = this.jdbcTemplate.queryForList(sqlForLoanIdentifiers, Long.class);
        if (!loanIds.isEmpty()) {
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = getScheduleDate(loanIds);
            List<Map<String, Object>> loanSummary = getLoanSummary(loanIds);
            loanArrearsAgingService.updateScheduleWithPaidDetail(scheduleDate, loanSummary);
            loanArrearsAgingService.createInsertStatements(insertStatement, scheduleDate, true);
        }

        return insertStatement;
    }

    private String buildQueryForLoanIdentifiersWithOriginalSchedule(boolean isForAllLoans) {
        final StringBuilder loanIdentifier = new StringBuilder();
        loanIdentifier.append("select ml.id as loanId FROM m_loan ml  ");
        loanIdentifier.append("INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        loanIdentifier.append(
                "inner join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id and prd.arrears_based_on_original_schedule = true  ");
        loanIdentifier.append("WHERE ml.loan_status_id = 300 ");
        if (!isForAllLoans) {
            loanIdentifier.append(" and ml.id IN (?)");
        }
        loanIdentifier.append(" and mr.completed_derived is false  and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day"))
                .append(" group by ml.id");
        return loanIdentifier.toString();
    }

    private List<Map<String, Object>> getLoanSummary(final List<Long> loanIds) {
        final StringBuilder transactionsSql = new StringBuilder();
        transactionsSql.append("select ml.id as loanId, ");
        transactionsSql
                .append("ml.principal_repaid_derived as principalAmtPaid, ml.principal_writtenoff_derived as  principalAmtWrittenoff, ");
        transactionsSql.append(" ml.interest_repaid_derived as interestAmtPaid, ml.interest_waived_derived as interestAmtWaived, ");
        transactionsSql.append("ml.fee_charges_repaid_derived as feeAmtPaid, ml.fee_charges_waived_derived as feeAmtWaived, ");
        transactionsSql
                .append("ml.penalty_charges_repaid_derived as penaltyAmtPaid, ml.penalty_charges_waived_derived as penaltyAmtWaived ");
        transactionsSql.append("from m_loan ml ");
        transactionsSql.append("where ml.id IN (:loanIds)").append(" order by ml.id");

        final NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        SqlParameterSource parameters = new MapSqlParameterSource("loanIds", loanIds);
        List<Map<String, Object>> loanSummary = namedJdbcTemplate.queryForList(transactionsSql.toString(), parameters);
        return loanSummary;
    }

    private Map<Long, List<LoanSchedulePeriodData>> getScheduleDate(List<Long> loanIds) {
        LoanOriginalScheduleExtractor loanOriginalScheduleExtractor = new LoanOriginalScheduleExtractor(sqlGenerator);
        final NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        SqlParameterSource parameters = new MapSqlParameterSource("loanIds", loanIds);
        return namedJdbcTemplate.query(loanOriginalScheduleExtractor.schema, parameters, loanOriginalScheduleExtractor);
    }

    private static final class LoanOriginalScheduleExtractor implements ResultSetExtractor<Map<Long, List<LoanSchedulePeriodData>>> {

        private final String schema;

        LoanOriginalScheduleExtractor(DatabaseSpecificSQLGenerator sqlGenerator) {
            final StringBuilder scheduleDetail = new StringBuilder();
            scheduleDetail.append("select ml.id as loanId, mr.duedate as dueDate, mr.principal_amount as principalAmount, ");
            scheduleDetail.append(
                    "mr.interest_amount as interestAmount, mr.fee_charges_amount as feeAmount, mr.penalty_charges_amount as penaltyAmount  ");
            scheduleDetail.append("from m_loan ml  INNER JOIN m_loan_repayment_schedule_history mr on mr.loan_id = ml.id ");
            scheduleDetail.append("where mr.duedate  < "
                    + sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day") + " and ");
            scheduleDetail.append("ml.id IN(:loanIds)").append(" and  mr.version = (");
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

}
