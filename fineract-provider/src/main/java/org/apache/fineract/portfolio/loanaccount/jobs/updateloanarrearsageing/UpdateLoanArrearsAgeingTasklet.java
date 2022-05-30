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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.service.LoanArrearsAgingService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class UpdateLoanArrearsAgeingTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanArrearsAgingService loanArrearsAgingService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        jdbcTemplate.execute("truncate table m_loan_arrears_aging");

        final StringBuilder updateSqlBuilder = new StringBuilder(900);
        final String principalOverdueCalculationSql = "SUM(COALESCE(mr.principal_amount, 0) - coalesce(mr.principal_completed_derived, 0) - coalesce(mr.principal_writtenoff_derived, 0))";
        final String interestOverdueCalculationSql = "SUM(COALESCE(mr.interest_amount, 0) - coalesce(mr.interest_writtenoff_derived, 0) - coalesce(mr.interest_waived_derived, 0) - "
                + "coalesce(mr.interest_completed_derived, 0))";
        final String feeChargesOverdueCalculationSql = "SUM(COALESCE(mr.fee_charges_amount, 0) - coalesce(mr.fee_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.fee_charges_waived_derived, 0) - coalesce(mr.fee_charges_completed_derived, 0))";
        final String penaltyChargesOverdueCalculationSql = "SUM(COALESCE(mr.penalty_charges_amount, 0) - coalesce(mr.penalty_charges_writtenoff_derived, 0) - "
                + "coalesce(mr.penalty_charges_waived_derived, 0) - coalesce(mr.penalty_charges_completed_derived, 0))";

        updateSqlBuilder.append(
                "INSERT INTO m_loan_arrears_aging(loan_id,principal_overdue_derived,interest_overdue_derived,fee_charges_overdue_derived,penalty_charges_overdue_derived,total_overdue_derived,overdue_since_date_derived)");
        updateSqlBuilder.append("select ml.id as loanId,");
        updateSqlBuilder.append(principalOverdueCalculationSql + " as principal_overdue_derived,");
        updateSqlBuilder.append(interestOverdueCalculationSql + " as interest_overdue_derived,");
        updateSqlBuilder.append(feeChargesOverdueCalculationSql + " as fee_charges_overdue_derived,");
        updateSqlBuilder.append(penaltyChargesOverdueCalculationSql + " as penalty_charges_overdue_derived,");
        updateSqlBuilder.append(principalOverdueCalculationSql + "+" + interestOverdueCalculationSql + "+");
        updateSqlBuilder.append(feeChargesOverdueCalculationSql + "+" + penaltyChargesOverdueCalculationSql + " as total_overdue_derived,");
        updateSqlBuilder.append("MIN(mr.duedate) as overdue_since_date_derived ");
        updateSqlBuilder.append(" FROM m_loan ml ");
        updateSqlBuilder.append(" INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        updateSqlBuilder.append(" left join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id ");
        updateSqlBuilder.append(" WHERE ml.loan_status_id = 300 "); // active
        updateSqlBuilder.append(" and mr.completed_derived is false ");
        updateSqlBuilder.append(" and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day"))
                .append(" ");
        updateSqlBuilder.append(" and (prd.arrears_based_on_original_schedule = false or prd.arrears_based_on_original_schedule is null) ");
        updateSqlBuilder.append(" GROUP BY ml.id");

        List<String> insertStatements = updateLoanArrearsAgeingDetailsWithOriginalSchedule();
        insertStatements.add(0, updateSqlBuilder.toString());
        final int[] results = this.jdbcTemplate.batchUpdate(insertStatements.toArray(new String[0]));
        int result = 0;
        for (int i : results) {
            result += i;
        }

        log.info("{}: Records affected by updateLoanArrearsAgeingDetails: {}", ThreadLocalContextUtil.getTenant().getName(), result);
        return RepeatStatus.FINISHED;
    }

    private List<String> updateLoanArrearsAgeingDetailsWithOriginalSchedule() {
        List<String> insertStatement = new ArrayList<>();

        final StringBuilder loanIdentifier = new StringBuilder();
        loanIdentifier.append("select ml.id as loanId FROM m_loan ml  ");
        loanIdentifier.append("INNER JOIN m_loan_repayment_schedule mr on mr.loan_id = ml.id ");
        loanIdentifier.append(
                "inner join m_product_loan_recalculation_details prd on prd.product_id = ml.product_id and prd.arrears_based_on_original_schedule = true  ");
        loanIdentifier.append("WHERE ml.loan_status_id = 300  and mr.completed_derived is false  and mr.duedate < ")
                .append(sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(ml.grace_on_arrears_ageing, 0)", "day"))
                .append(" group by ml.id");
        List<Long> loanIds = this.jdbcTemplate.queryForList(loanIdentifier.toString(), Long.class);
        if (!loanIds.isEmpty()) {
            String loanIdsAsString = loanIds.toString();
            loanIdsAsString = loanIdsAsString.substring(1, loanIdsAsString.length() - 1);
            Map<Long, List<LoanSchedulePeriodData>> scheduleDate = loanArrearsAgingService.getScheduleDate(loanIdsAsString);

            List<Map<String, Object>> loanSummary = getLoanSummary(loanIdsAsString);
            loanArrearsAgingService.updateScheduleWithPaidDetail(scheduleDate, loanSummary);
            loanArrearsAgingService.createInsertStatements(insertStatement, scheduleDate, true);
        }

        return insertStatement;
    }

    private List<Map<String, Object>> getLoanSummary(final String loanIdsAsString) {
        final StringBuilder transactionsSql = new StringBuilder();
        transactionsSql.append("select ml.id as loanId, ");
        transactionsSql
                .append("ml.principal_repaid_derived as principalAmtPaid, ml.principal_writtenoff_derived as  principalAmtWrittenoff, ");
        transactionsSql.append(" ml.interest_repaid_derived as interestAmtPaid, ml.interest_waived_derived as interestAmtWaived, ");
        transactionsSql.append("ml.fee_charges_repaid_derived as feeAmtPaid, ml.fee_charges_waived_derived as feeAmtWaived, ");
        transactionsSql
                .append("ml.penalty_charges_repaid_derived as penaltyAmtPaid, ml.penalty_charges_waived_derived as penaltyAmtWaived ");
        transactionsSql.append("from m_loan ml ");
        transactionsSql.append("where ml.id IN (").append(loanIdsAsString).append(") order by ml.id");

        List<Map<String, Object>> loanSummary = this.jdbcTemplate.queryForList(transactionsSql.toString());
        return loanSummary;
    }
}
