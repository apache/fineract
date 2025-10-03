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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.migration.TenantDataSourceFactory;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.data.JournalEntryAggregationSummaryData;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class JournalEntryAggregationJobReader extends JdbcCursorItemReader<JournalEntryAggregationSummaryData> {

    private LocalDate aggregatedOnDateFrom;
    private LocalDate aggregatedOnDateTo;

    public JournalEntryAggregationJobReader(TenantDataSourceFactory tenantDataSourceFactory) {
        FineractPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        setDataSource(tenantDataSourceFactory.create(tenant));
        setSql(buildAggregationQuery());
        setRowMapper(this::mapRow);
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        ExecutionContext ctx = stepExecution.getJobExecution().getExecutionContext();

        this.aggregatedOnDateFrom = (LocalDate) ctx.get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM);
        this.aggregatedOnDateTo = (LocalDate) ctx.get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_TO);

        setPreparedStatementSetter(ps -> {
            ps.setObject(1, aggregatedOnDateFrom);
            ps.setObject(2, aggregatedOnDateTo);
        });

    }

    private JournalEntryAggregationSummaryData mapRow(ResultSet rs, int rowNum) throws SQLException {
        return JournalEntryAggregationSummaryData.builder() //
                .glAccountId(rs.getLong("glAccountId")) //
                .productId(rs.getLong("productId")) //
                .office(rs.getLong("officeId")) //
                .entityTypeEnum(rs.getLong("entityTypeEnum")) //
                .currencyCode(rs.getString("currencyCode")) //
                .submittedOnDate(ThreadLocalContextUtil.getBusinessDate()) //
                .aggregatedOnDate(JdbcSupport.getLocalDate(rs, "aggregatedOnDate")) //
                .externalOwnerId(JdbcSupport.getLong(rs, "externalOwner")) //
                .debitAmount(rs.getBigDecimal("debitAmount")) //
                .creditAmount(rs.getBigDecimal("creditAmount")) //
                .manualEntry(false) //
                .build(); //
    }

    private String buildAggregationQuery() {
        return """
                SELECT
                    COALESCE(
                        loan_product.id,
                        savings_product.id,
                        prov_product.id,
                        share_product.id
                    ) AS productId,
                    acc_gl_account.id AS glAccountId,
                    acc_gl_journal_entry.entity_type_enum AS entityTypeEnum,
                    acc_gl_journal_entry.office_id AS officeId,
                    aw.owner_id AS externalOwner,
                    SUM(CASE WHEN acc_gl_journal_entry.type_enum = 2 THEN amount ELSE 0 END) AS debitAmount,
                    SUM(CASE WHEN acc_gl_journal_entry.type_enum = 1 THEN amount ELSE 0 END) AS creditAmount,
                    acc_gl_journal_entry.submitted_on_date AS aggregatedOnDate,
                    acc_gl_journal_entry.currency_code AS currencyCode
                FROM acc_gl_account
                JOIN acc_gl_journal_entry
                    ON acc_gl_account.id = acc_gl_journal_entry.account_id

                -- entity_type_enum = 1 → LOAN
                LEFT JOIN m_loan loan
                    ON loan.id = acc_gl_journal_entry.entity_id
                    AND acc_gl_journal_entry.entity_type_enum = 1
                LEFT JOIN m_product_loan loan_product
                    ON loan_product.id = loan.product_id
                    AND acc_gl_journal_entry.entity_type_enum = 1

                -- entity_type_enum = 2 → SAVING
                LEFT JOIN m_savings_account savings
                    ON savings.id = acc_gl_journal_entry.entity_id
                    AND acc_gl_journal_entry.entity_type_enum = 2
                LEFT JOIN m_savings_product savings_product
                    ON savings_product.id = savings.product_id
                    AND acc_gl_journal_entry.entity_type_enum = 2

                -- entity_type_enum = 3 → PROVISIONING
                LEFT JOIN m_provisioning_history prov
                    ON prov.id = acc_gl_journal_entry.entity_id
                    AND acc_gl_journal_entry.entity_type_enum = 3
                LEFT JOIN m_loanproduct_provisioning_entry prov_entry
                    ON prov_entry.history_id = prov.id
                    AND acc_gl_journal_entry.entity_type_enum = 3
                LEFT JOIN m_product_loan prov_product
                    ON prov_product.id = prov_entry.product_id
                    AND acc_gl_journal_entry.entity_type_enum = 3

                -- entity_type_enum = 4 → SHARED
                LEFT JOIN m_share_account share
                    ON share.id = acc_gl_journal_entry.entity_id
                    AND acc_gl_journal_entry.entity_type_enum = 4
                LEFT JOIN m_share_product share_product
                    ON share_product.id = share.product_id
                    AND acc_gl_journal_entry.entity_type_enum = 4

                -- external owner
                LEFT JOIN m_external_asset_owner_journal_entry_mapping aw
                    ON aw.journal_entry_id = acc_gl_journal_entry.id

                WHERE acc_gl_journal_entry.submitted_on_date > ?
                  AND acc_gl_journal_entry.submitted_on_date <= ?

                GROUP BY
                    productId,
                    glAccountId,
                    externalOwner,
                    aggregatedOnDate,
                    currencyCode,
                    entityTypeEnum,
                    officeId
                """;
    }
}
