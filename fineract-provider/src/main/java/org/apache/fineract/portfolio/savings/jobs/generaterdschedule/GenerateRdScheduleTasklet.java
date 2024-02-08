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
package org.apache.fineract.portfolio.savings.jobs.generaterdschedule;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_BY_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE_DB_FIELD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.DepositAccountUtils;
import org.apache.fineract.portfolio.savings.service.DepositAccountReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class GenerateRdScheduleTasklet implements Tasklet {

    private final RoutingDataSourceServiceFactory dataSourceServiceFactory;
    private final DepositAccountReadPlatformService depositAccountReadPlatformService;
    private final PlatformSecurityContext securityContext;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceServiceFactory.determineDataSourceService().retrieveDataSource());
        final Collection<Map<String, Object>> scheduleDetails = depositAccountReadPlatformService.retriveDataForRDScheduleCreation();
        String insertSql = "INSERT INTO m_mandatory_savings_schedule (savings_account_id, duedate, installment, deposit_amount, completed_derived, "
                + CREATED_DATE_DB_FIELD + ", " + CREATED_BY_DB_FIELD + ", " + LAST_MODIFIED_DATE_DB_FIELD + ", " + LAST_MODIFIED_BY_DB_FIELD
                + ") " + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        List<Object[]> params = new ArrayList<>();
        Long userId = securityContext.authenticatedUser().getId();
        int iterations = 0;
        for (Map<String, Object> details : scheduleDetails) {
            Long count = (Long) details.get("futureInstallments");
            if (count == null) {
                count = 0L;
            }
            final Long savingsId = (Long) details.get("savingsId");
            final BigDecimal amount = (BigDecimal) details.get("amount");
            final String recurrence = (String) details.get("recurrence");
            LocalDate lastDepositDate = (LocalDate) details.get("dueDate");
            Integer installmentNumber = (Integer) details.get("installment");
            while (count < DepositAccountUtils.GENERATE_MINIMUM_NUMBER_OF_FUTURE_INSTALMENTS) {
                count++;
                installmentNumber++;
                lastDepositDate = DepositAccountUtils.calculateNextDepositDate(lastDepositDate, recurrence);
                OffsetDateTime auditTime = DateUtils.getAuditOffsetDateTime();
                params.add(new Object[] { savingsId, lastDepositDate, installmentNumber, amount, false, auditTime, userId, auditTime,
                        userId });
                iterations++;
            }
            if (iterations > 200) {
                jdbcTemplate.batchUpdate(insertSql, params);
                params.clear();
                iterations = 0;
            }
        }

        if (!params.isEmpty()) {
            jdbcTemplate.batchUpdate(insertSql, params);
        }
        return RepeatStatus.FINISHED;
    }
}
