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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

class SavingsAccountReadPlatformServiceImplTest {

    private static final Pattern PRODUCT_MAPPING_JOIN = Pattern.compile("acc_product_mapping (\\w+) on (.+?)(?= left join | join |$)");

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_onlyJoinsSavingsProductMappings() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenReturn(List.of());

        new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate, null, null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(ResultSetExtractor.class), any(Object[].class));

        // acc_product_mapping holds loan mappings too, and loan and savings product ids overlap
        final List<String> aliases = new ArrayList<>();
        final Matcher join = PRODUCT_MAPPING_JOIN.matcher(sql.getValue());
        while (join.find()) {
            aliases.add(join.group(1));
            assertThat(join.group(2)).as("acc_product_mapping join %s", join.group(1)).contains(join.group(1) + ".product_type = 2");
        }
        assertThat(aliases).contains("apm", "apm1", "apm2", "apm3", "apm4", "apm5", "apm6");
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_doesNotJoinTaxGroupMappingsIntoTheTransactionQuery() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenReturn(List.of());

        new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate, null, null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(ResultSetExtractor.class), any(Object[].class));

        // the mappings, components and component histories of the tax group are loaded by a separate
        // query (see the test below): joining them into this query multiplied every transaction row of
        // the account by mappings x component histories. Only the tax group id is read here, plus the
        // savings reference mapping the withhold tax journal entries fall back to. The savings reference
        // join is restricted to the core mapping, like findCoreProductToFinAccountMapping: the payment
        // channel to fund source mappings share financial account type 1 and would multiply every row.
        assertThat(sql.getValue()).doesNotContain("m_tax_group_mappings", "m_tax_component_history", "taxGroupMappingId",
                "taxGroupComponentId", "taxGroupComponentHistoryId");
        assertThat(sql.getValue()).contains("tg.id as taxGroupId", "apm6.gl_account_id as glAccountIdForSavingsReference",
                "apm6.payment_type is null", "apm6.charge_id is null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_loadsTaxGroupsSeparatelyAndSetsThemOnTheAccounts() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        final TaxGroupData loadedTaxGroup = new TaxGroupData(5L, "Withholding tax", List.of(), null, null, null);
        final SavingsAccountData firstAccount = accountWithTaxGroup(5L);
        final SavingsAccountData secondAccount = accountWithTaxGroup(5L);
        final SavingsAccountData accountWithoutTaxGroup = mock(SavingsAccountData.class);

        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class)))
                .thenReturn(List.of(firstAccount, secondAccount, accountWithoutTaxGroup));
        when(jdbcTemplate.query(argThat(sql -> sql != null && sql.contains("from m_tax_group tg")), any(ResultSetExtractor.class),
                any(Object[].class))).thenReturn(Map.of(5L, loadedTaxGroup));

        final List<SavingsAccountData> accounts = new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate,
                mock(SavingsAccountAssembler.class), null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        assertThat(accounts).hasSize(3);
        // both accounts carry the same tax group, so the page loads it once and the same loaded instance
        // is set on both; the account without a tax group is left alone
        verify(firstAccount).setTaxGroup(loadedTaxGroup);
        verify(secondAccount).setTaxGroup(loadedTaxGroup);
        verify(accountWithoutTaxGroup, never()).setTaxGroup(any());

        final ArgumentCaptor<Object[]> taxGroupParams = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate, times(1)).query(argThat(sql -> sql != null && sql.contains("from m_tax_group tg")),
                any(ResultSetExtractor.class), taxGroupParams.capture());
        assertThat(taxGroupParams.getValue()).containsExactly(5L);

        // the separate query is the one that joins the mappings, their components and the component
        // histories, so the loaded tax group is complete
        final ArgumentCaptor<String> taxGroupSql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(2)).query(taxGroupSql.capture(), any(ResultSetExtractor.class), any(Object[].class));
        final String taxGroupQuery = taxGroupSql.getAllValues().stream().filter(sql -> sql.contains("from m_tax_group tg")).findFirst()
                .orElseThrow();
        assertThat(taxGroupQuery).contains("left join m_tax_group_mappings tgm on tgm.tax_group_id = tg.id",
                "left join m_tax_component tgcomp on tgcomp.id = tgm.tax_component_id",
                "left join m_tax_component_history tgch on tgch.tax_component_id = tgcomp.id", "tgm.id as taxGroupMappingId",
                "tgcomp.id as taxGroupComponentId", "tgch.id as taxGroupComponentHistoryId");
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_skipsTheTaxGroupLoadWhenNoAccountHasATaxGroup() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenReturn(List.of(account));

        new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate, mock(SavingsAccountAssembler.class), null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        verify(jdbcTemplate, times(1)).query(anyString(), any(ResultSetExtractor.class), any(Object[].class));
        verify(account, never()).setTaxGroup(any());
    }

    private static SavingsAccountData accountWithTaxGroup(final Long taxGroupId) {
        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getTaxGroup()).thenReturn(TaxGroupData.lookup(taxGroupId, null));
        return account;
    }
}
