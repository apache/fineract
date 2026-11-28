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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxComponentHistoryData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.data.TaxGroupMappingsData;
import org.springframework.jdbc.core.ResultSetExtractor;

final class TaxGroupMapperForInterestPosting implements ResultSetExtractor<Map<Long, TaxGroupData>> {

    private final String schemaSql;

    TaxGroupMapperForInterestPosting() {
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("tg.id as taxGroupId, tg.name as taxGroupName, ");
        sqlBuilder.append("tgm.id as taxGroupMappingId, tgm.start_date as taxGroupMappingStartDate, ");
        sqlBuilder.append("tgm.end_date as taxGroupMappingEndDate, ");
        sqlBuilder.append("tgcomp.id as taxGroupComponentId, tgcomp.name as taxGroupComponentName, ");
        sqlBuilder.append("tgcomp.percentage as taxGroupComponentPercentage, ");
        sqlBuilder.append("tgcomp.debit_account_id as taxGroupComponentDebitAccountId, ");
        sqlBuilder.append("tgcomp.credit_account_id as taxGroupComponentCreditAccountId, ");
        sqlBuilder.append("tgcomp.start_date as taxGroupComponentStartDate, ");
        sqlBuilder.append("tgch.id as taxGroupComponentHistoryId, tgch.percentage as taxGroupComponentHistoryPercentage, ");
        sqlBuilder.append("tgch.start_date as taxGroupComponentHistoryStartDate, ");
        sqlBuilder.append("tgch.end_date as taxGroupComponentHistoryEndDate ");
        sqlBuilder.append("from m_tax_group tg ");
        sqlBuilder.append("left join m_tax_group_mappings tgm on tgm.tax_group_id = tg.id ");
        sqlBuilder.append("left join m_tax_component tgcomp on tgcomp.id = tgm.tax_component_id ");
        sqlBuilder.append("left join m_tax_component_history tgch on tgch.tax_component_id = tgcomp.id ");

        this.schemaSql = sqlBuilder.toString();
    }

    String schema() {
        return this.schemaSql;
    }

    @Override
    public Map<Long, TaxGroupData> extractData(final ResultSet rs) throws SQLException {
        final Map<Long, TaxGroupData> taxGroups = new HashMap<>();
        final Map<Long, List<TaxGroupMappingsData>> taxAssociationsByGroup = new HashMap<>();
        final Map<Long, TaxComponentData> taxComponents = new HashMap<>();
        final Map<Long, List<TaxComponentHistoryData>> taxComponentHistories = new HashMap<>();
        final Set<Long> taxGroupMappingIds = new HashSet<>();
        final Set<Long> taxGroupComponentHistoryIds = new HashSet<>();

        while (rs.next()) {
            final Long taxGroupId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxGroupId");
            if (taxGroupId == null) {
                continue;
            }
            if (!taxGroups.containsKey(taxGroupId)) {
                final List<TaxGroupMappingsData> taxAssociations = new ArrayList<>();
                taxAssociationsByGroup.put(taxGroupId, taxAssociations);
                taxGroups.put(taxGroupId, new TaxGroupData(taxGroupId, rs.getString("taxGroupName"), taxAssociations, null, null, null));
            }

            final Long taxGroupMappingId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxGroupMappingId");
            final Long taxGroupComponentId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxGroupComponentId");
            if (taxGroupMappingId != null && taxGroupComponentId != null && taxGroupMappingIds.add(taxGroupMappingId)) {
                TaxComponentData taxComponent = taxComponents.get(taxGroupComponentId);
                if (taxComponent == null) {
                    final List<TaxComponentHistoryData> componentHistories = new ArrayList<>();
                    taxComponentHistories.put(taxGroupComponentId, componentHistories);
                    final BigDecimal taxGroupComponentPercentage = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs,
                            "taxGroupComponentPercentage");
                    final Long taxGroupComponentDebitAccountId = JdbcSupport.getLongDefaultToNullIfZero(rs,
                            "taxGroupComponentDebitAccountId");
                    final GLAccountData debitAccount = taxGroupComponentDebitAccountId != null
                            ? GLAccountData.createFrom(taxGroupComponentDebitAccountId)
                            : null;
                    final Long taxGroupComponentCreditAccountId = JdbcSupport.getLongDefaultToNullIfZero(rs,
                            "taxGroupComponentCreditAccountId");
                    final GLAccountData creditAccount = taxGroupComponentCreditAccountId != null
                            ? GLAccountData.createFrom(taxGroupComponentCreditAccountId)
                            : null;
                    final LocalDate taxGroupComponentStartDate = JdbcSupport.getLocalDate(rs, "taxGroupComponentStartDate");
                    taxComponent = TaxComponentData.instance(taxGroupComponentId, rs.getString("taxGroupComponentName"),
                            taxGroupComponentPercentage, null, debitAccount, null, creditAccount, taxGroupComponentStartDate,
                            componentHistories);
                    taxComponents.put(taxGroupComponentId, taxComponent);
                }
                final LocalDate taxGroupMappingStartDate = JdbcSupport.getLocalDate(rs, "taxGroupMappingStartDate");
                final LocalDate taxGroupMappingEndDate = JdbcSupport.getLocalDate(rs, "taxGroupMappingEndDate");
                taxAssociationsByGroup.get(taxGroupId).add(
                        new TaxGroupMappingsData(taxGroupMappingId, taxComponent, taxGroupMappingStartDate, taxGroupMappingEndDate, null));
            }

            final Long taxGroupComponentHistoryId = JdbcSupport.getLongDefaultToNullIfZero(rs, "taxGroupComponentHistoryId");
            if (taxGroupComponentHistoryId != null && taxGroupComponentHistoryIds.add(taxGroupComponentHistoryId)) {
                final List<TaxComponentHistoryData> componentHistories = taxComponentHistories.get(taxGroupComponentId);
                if (componentHistories != null) {
                    componentHistories.add(new TaxComponentHistoryData(
                            JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "taxGroupComponentHistoryPercentage"),
                            JdbcSupport.getLocalDate(rs, "taxGroupComponentHistoryStartDate"),
                            JdbcSupport.getLocalDate(rs, "taxGroupComponentHistoryEndDate")));
                }
            }
        }
        return taxGroups;
    }
}
