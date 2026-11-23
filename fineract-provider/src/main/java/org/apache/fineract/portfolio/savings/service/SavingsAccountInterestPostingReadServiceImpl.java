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

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class SavingsAccountInterestPostingReadServiceImpl implements SavingsAccountInterestPostingReadService {

    private final JdbcTemplate jdbcTemplate;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingAccountMapperForInterestPosting savingAccountMapperForInterestPosting = new SavingAccountMapperForInterestPosting();
    private final TaxGroupMapperForInterestPosting taxGroupMapperForInterestPosting = new TaxGroupMapperForInterestPosting();

    @Override
    public List<SavingsAccountData> retrieveAllSavingsDataForInterestPosting(final boolean backdatedTxnsAllowedTill, final int pageSize,
            final Integer status, final Long maxSavingsId) {
        LocalDate yesterday = DateUtils.getBusinessLocalDate().minusDays(1);
        String sql = "select " + this.savingAccountMapperForInterestPosting.schema()
                + "join (select a.id from m_savings_account a where a.id > ? and a.status_enum = ? limit ?) b on b.id = sa.id ";
        if (backdatedTxnsAllowedTill) {
            sql = sql
                    + "where (CASE WHEN sa.interest_posted_till_date is not null THEN tr.transaction_date >= sa.interest_posted_till_date ELSE tr.transaction_date >= sa.activatedon_date END) ";
        }

        sql = sql + " and (sa.interest_posted_till_date is null or sa.interest_posted_till_date <= ? ) ";
        // #audit backward compatibility
        sql = sql + " order by sa.id, tr.transaction_date, tr." + CREATED_DATE_DB_FIELD + ", tr.created_date, tr.id";

        List<SavingsAccountData> savingsAccountDataList = this.jdbcTemplate.query(sql, this.savingAccountMapperForInterestPosting, // NOSONAR
                new Object[] { maxSavingsId, status, pageSize, yesterday });
        loadTaxGroupsForInterestPosting(savingsAccountDataList);
        for (SavingsAccountData savingsAccountData : savingsAccountDataList) {
            this.savingAccountAssembler.assembleSavings(savingsAccountData);
        }
        return savingsAccountDataList;
    }

    // The tax groups are loaded once per page instead of being joined into the transaction query above:
    // that join multiplied every transaction row by the group's mappings and component histories. There are
    // only a handful of tax groups in the whole system, so a second query for the distinct ids of the page
    // is far cheaper.
    private void loadTaxGroupsForInterestPosting(final List<SavingsAccountData> savingsAccountDataList) {
        final Set<Long> taxGroupIds = new HashSet<>();
        for (final SavingsAccountData savingsAccountData : savingsAccountDataList) {
            if (savingsAccountData.getTaxGroup() != null && savingsAccountData.getTaxGroup().getId() != null) {
                taxGroupIds.add(savingsAccountData.getTaxGroup().getId());
            }
        }
        if (taxGroupIds.isEmpty()) {
            return;
        }
        final Map<Long, TaxGroupData> taxGroupsById = retrieveTaxGroupsForInterestPosting(taxGroupIds);
        for (final SavingsAccountData savingsAccountData : savingsAccountDataList) {
            if (savingsAccountData.getTaxGroup() != null) {
                final TaxGroupData taxGroup = taxGroupsById.get(savingsAccountData.getTaxGroup().getId());
                if (taxGroup != null) {
                    savingsAccountData.setTaxGroup(taxGroup);
                }
            }
        }
    }

    private Map<Long, TaxGroupData> retrieveTaxGroupsForInterestPosting(final Set<Long> taxGroupIds) {
        final StringBuilder inSqlBuilder = new StringBuilder();
        for (int i = 0; i < taxGroupIds.size(); i++) {
            if (i > 0) {
                inSqlBuilder.append(", ");
            }
            inSqlBuilder.append("?");
        }
        final String sql = "select " + this.taxGroupMapperForInterestPosting.schema() + " where tg.id in (" + inSqlBuilder
                + ") order by tg.id, tgm.id, tgch.id";
        return this.jdbcTemplate.query(sql, this.taxGroupMapperForInterestPosting, taxGroupIds.toArray());
    }
}
