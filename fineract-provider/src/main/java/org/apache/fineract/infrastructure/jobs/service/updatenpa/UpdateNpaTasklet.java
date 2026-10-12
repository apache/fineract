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
package org.apache.fineract.infrastructure.jobs.service.updatenpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class UpdateNpaTasklet implements Tasklet {

    private final RoutingDataSourceServiceFactory dataSourceServiceFactory;
    private final DatabaseTypeResolver databaseTypeResolver;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PlatformSecurityContext context;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        AppUser user = context.getAuthenticatedUserIfPresent();
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSourceServiceFactory.determineDataSourceService().retrieveDataSource());

        final StringBuilder resetNPASqlBuilder = new StringBuilder();
        resetNPASqlBuilder.append("update m_loan loan ");
        String fromPart = " (SELECT loan2.* FROM m_loan loan2 left join m_loan_arrears_aging laa on laa.loan_id = loan2.id "
                + "inner join m_product_loan mpl on mpl.id = loan2.product_id and mpl.overdue_days_for_npa is not null "
                + "WHERE loan2.loan_status_id = 300 and mpl.account_moves_out_of_npa_only_on_arrears_completion = false"
                + " or (mpl.account_moves_out_of_npa_only_on_arrears_completion = true"
                + " and laa.overdue_since_date_derived is null)) sl";
        String wherePart = " where loan.id = sl.id ";

        if (databaseTypeResolver.isMySQL()) {
            resetNPASqlBuilder.append(", ").append(fromPart).append(" set loan.is_npa = false")
                    .append(", loan.last_modified_by = ?, loan.last_modified_on_utc = ? ").append(wherePart);
        } else {
            resetNPASqlBuilder.append("set is_npa = false").append(", last_modified_by = ?, last_modified_on_utc = ? ").append(" FROM ")
                    .append(fromPart).append(wherePart);
        }
        jdbcTemplate.update(resetNPASqlBuilder.toString(), user.getId(), DateUtils.getAuditOffsetDateTime());

        final StringBuilder updateSqlBuilder = new StringBuilder(900);

        fromPart = " (select loan.id " + " FROM m_loan_arrears_aging laa" + " INNER JOIN  m_loan loan on laa.loan_id = loan.id "
                + " INNER JOIN m_product_loan mpl on mpl.id = loan.product_id AND mpl.overdue_days_for_npa is not null "
                + "WHERE loan.loan_status_id = 300 and " + "laa.overdue_since_date_derived < "
                + sqlGenerator.subDate(sqlGenerator.currentBusinessDate(), "COALESCE(mpl.overdue_days_for_npa, 0)", "day")
                + " group by loan.id) as sl ";
        wherePart = " where ml.id=sl.id ";
        updateSqlBuilder.append("UPDATE m_loan as ml ");
        if (databaseTypeResolver.isMySQL()) {
            updateSqlBuilder.append(", ").append(fromPart).append(" SET ml.is_npa = true")
                    .append(", ml.last_modified_by = ?, ml.last_modified_on_utc = ? ").append(wherePart);
        } else {
            updateSqlBuilder.append(" SET is_npa = true").append(", last_modified_by = ?, last_modified_on_utc = ? ").append(" FROM ")
                    .append(fromPart).append(wherePart);
        }

        final int result = jdbcTemplate.update(updateSqlBuilder.toString(), user.getId(), DateUtils.getAuditOffsetDateTime());

        log.debug("{}: Records affected by updateNPA: {}", ThreadLocalContextUtil.getTenant().getName(), result);
        return RepeatStatus.FINISHED;
    }
}
