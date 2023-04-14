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
package org.apache.fineract.cob.loan;

import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.data.LoanCOBParameter;
import org.apache.fineract.cob.domain.LockOwner;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class LockLoanTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) throws Exception {
        String businessDateParameter = (String) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME);
        LocalDate businessDate = LocalDate.parse(Objects.requireNonNull(businessDateParameter))
                .minusDays(LoanCOBConstant.NUMBER_OF_DAYS_BEHIND);
        LoanCOBParameter loanCOBParameter = (LoanCOBParameter) contribution.getStepExecution().getJobExecution().getExecutionContext()
                .get(LoanCOBConstant.LOAN_COB_PARAMETER);
        if (Objects.isNull(loanCOBParameter)
                || (Objects.isNull(loanCOBParameter.getMinLoanId()) && Objects.isNull(loanCOBParameter.getMaxLoanId()))) {
            loanCOBParameter = new LoanCOBParameter(0L, 0L);
        }
        applySoftLock(businessDate, loanCOBParameter);

        return RepeatStatus.FINISHED;
    }

    private void applySoftLock(LocalDate businessDate, LoanCOBParameter loanCOBParameter) {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        jdbcTemplate.update("""
                    INSERT INTO m_loan_account_locks (loan_id, version, lock_owner, lock_placed_on, lock_placed_on_cob_business_date)
                    SELECT loan.id, ?, ?, ?, ? FROM m_loan loan
                        WHERE loan.id NOT IN (SELECT loan_id FROM m_loan_account_locks)
                        AND loan.id BETWEEN ? AND ?
                        AND loan.loan_status_id IN (100,200,300,303,304)
                        AND (? = loan.last_closed_business_date OR loan.last_closed_business_date IS NULL)
                """, ps -> {
            ps.setLong(1, 1);
            ps.setString(2, LockOwner.LOAN_COB_PARTITIONING.name());
            ps.setObject(3, DateUtils.getOffsetDateTimeOfTenant());
            ps.setObject(4, cobBusinessDate);
            ps.setObject(5, loanCOBParameter.getMinLoanId());
            ps.setObject(6, loanCOBParameter.getMaxLoanId());
            ps.setObject(7, businessDate);
        });
    }
}
