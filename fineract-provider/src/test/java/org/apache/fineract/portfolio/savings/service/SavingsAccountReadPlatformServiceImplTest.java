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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxComponentHistoryData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.data.TaxGroupMappingsData;
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
    void retrieveAllSavingsDataForInterestPosting_joinsTaxGroupMappingsAndSavingsReferenceMapping() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenReturn(List.of());

        new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate, null, null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(ResultSetExtractor.class), any(Object[].class));

        // the interest posting job can only withhold tax if the mapper loads the account's tax group
        // mappings, their components and the component history alongside the account
        assertThat(sql.getValue()).contains("left join m_tax_group_mappings tgm on tgm.tax_group_id = tg.id",
                "left join m_tax_component tgcomp on tgcomp.id = tgm.tax_component_id",
                "left join m_tax_component_history tgch on tgch.tax_component_id = tgcomp.id", "tgm.id as taxGroupMappingId",
                "tgm.start_date as taxGroupMappingStartDate", "tgm.end_date as taxGroupMappingEndDate",
                "tgcomp.id as taxGroupComponentId", "tgcomp.name as taxGroupComponentName",
                "tgcomp.credit_account_id as taxGroupComponentCreditAccountId", "tgch.id as taxGroupComponentHistoryId",
                "apm6.gl_account_id as glAccountIdForSavingsReference");
    }

    @Test
    @SuppressWarnings("unchecked")
    void retrieveAllSavingsDataForInterestPosting_loadsTaxGroupMappingsComponentsAndHistories() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 31))));
        final Map<String, Object> firstHistoryRow = interestPostingRow();
        firstHistoryRow.put("taxGroupComponentHistoryId", 31L);
        firstHistoryRow.put("taxGroupComponentHistoryPercentage", new BigDecimal("5"));
        firstHistoryRow.put("taxGroupComponentHistoryStartDate", LocalDate.of(2023, 6, 1));
        firstHistoryRow.put("taxGroupComponentHistoryEndDate", LocalDate.of(2023, 12, 31));
        final Map<String, Object> secondHistoryRow = interestPostingRow();
        secondHistoryRow.put("taxGroupComponentHistoryId", 32L);
        secondHistoryRow.put("taxGroupComponentHistoryPercentage", new BigDecimal("7"));
        secondHistoryRow.put("taxGroupComponentHistoryStartDate", LocalDate.of(2024, 1, 1));
        secondHistoryRow.put("taxGroupComponentHistoryEndDate", null);
        final ResultSet rs = resultSetOf(List.of(firstHistoryRow, secondHistoryRow));
        when(jdbcTemplate.query(anyString(), any(ResultSetExtractor.class), any(Object[].class))).thenAnswer(invocation -> {
            final ResultSetExtractor<List<SavingsAccountData>> extractor = invocation.getArgument(1);
            return extractor.extractData(rs);
        });

        final List<SavingsAccountData> accounts = new SavingsAccountReadPlatformServiceImpl(null, jdbcTemplate,
                mock(SavingsAccountAssembler.class), null, null, null, null, null)
                .retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);

        assertThat(accounts).hasSize(1);
        final SavingsAccountData account = accounts.get(0);
        assertThat(account.getGlAccountIdForSavingsReference()).isEqualTo(77L);
        final TaxGroupData taxGroup = account.getTaxGroup();
        assertThat(taxGroup).isNotNull();
        assertThat(taxGroup.getId()).isEqualTo(5L);
        // both rows carry the same mapping, so it must be loaded exactly once
        final Collection<TaxGroupMappingsData> associations = taxGroup.getTaxAssociations();
        assertThat(associations).hasSize(1);
        final TaxGroupMappingsData association = associations.iterator().next();
        assertThat(association.getId()).isEqualTo(11L);
        assertThat(association.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(association.getEndDate()).isNull();
        assertThat(association.getComponentEditable()).isNull();
        final TaxComponentData taxComponent = association.getTaxComponent();
        assertThat(taxComponent.getId()).isEqualTo(21L);
        assertThat(taxComponent.getName()).isEqualTo("Withholding tax");
        assertThat(taxComponent.getPercentage()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(taxComponent.getCreditAccount().getId()).isEqualTo(88L);
        assertThat(taxComponent.getDebitAccount()).isNull();
        // the history rows of both result rows must end up on the one loaded component
        final List<TaxComponentHistoryData> histories = new ArrayList<>(taxComponent.getTaxComponentHistories());
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getPercentage()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(histories.get(0).getStartDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(histories.get(0).getEndDate()).isEqualTo(LocalDate.of(2023, 12, 31));
        assertThat(histories.get(1).getPercentage()).isEqualByComparingTo(new BigDecimal("7"));
        assertThat(histories.get(1).getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(histories.get(1).getEndDate()).isNull();
    }

    private static Map<String, Object> interestPostingRow() {
        final Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("transactionId", 1001L);
        row.put("accountNo", "000000101");
        row.put("depositType", 100);
        row.put("glAccountIdForSavingsControl", 55L);
        row.put("glAccountIdForSavingsReference", 77L);
        row.put("productId", 2L);
        row.put("accountingType", 1);
        row.put("statusEnum", 300);
        row.put("subStatusEnum", 0);
        row.put("currencyCode", "USD");
        row.put("currencyDigits", 2);
        row.put("withHoldTax", true);
        row.put("taxGroupId", 5L);
        row.put("interestCompoundingPeriodType", 1);
        row.put("interestPostingPeriodType", 4);
        row.put("interestCalculationType", 1);
        row.put("interestCalculationDaysInYearType", 365);
        row.put("transactionType", 1);
        row.put("transactionDate", LocalDate.of(2024, 1, 15));
        row.put("officeId", 1L);
        row.put("taxGroupMappingId", 11L);
        row.put("taxGroupMappingStartDate", LocalDate.of(2024, 1, 1));
        row.put("taxGroupComponentId", 21L);
        row.put("taxGroupComponentName", "Withholding tax");
        row.put("taxGroupComponentPercentage", new BigDecimal("10"));
        row.put("taxGroupComponentCreditAccountId", 88L);
        row.put("taxGroupComponentStartDate", LocalDate.of(2023, 6, 1));
        return row;
    }

    private static ResultSet resultSetOf(final List<Map<String, Object>> rows) {
        final int[] cursor = { -1 };
        final boolean[] wasNull = { false };
        final Map<String, Integer> columnIndexes = new HashMap<>();
        final List<String> indexColumns = new ArrayList<>();
        indexColumns.add(null);
        return mock(ResultSet.class, invocation -> {
            final String method = invocation.getMethod().getName();
            final Object[] arguments = invocation.getArguments();
            switch (method) {
                case "next":
                    cursor[0]++;
                    return cursor[0] < rows.size();
                case "wasNull":
                    return wasNull[0];
                case "findColumn": {
                    final String label = (String) arguments[0];
                    Integer index = columnIndexes.get(label);
                    if (index == null) {
                        indexColumns.add(label);
                        index = indexColumns.size() - 1;
                        columnIndexes.put(label, index);
                    }
                    return index;
                }
                case "getLong", "getInt", "getBoolean", "getString", "getBigDecimal", "getDate", "getObject": {
                    final Object value = columnValue(rows.get(cursor[0]), indexColumns, arguments[0]);
                    wasNull[0] = value == null;
                    return convertValue(method, value);
                }
                default:
                    return defaultValue(invocation.getMethod().getReturnType());
            }
        });
    }

    private static Object columnValue(final Map<String, Object> row, final List<String> indexColumns, final Object column) {
        final String label = column instanceof String ? (String) column : indexColumns.get((Integer) column);
        return row.get(label);
    }

    private static Object convertValue(final String method, final Object value) {
        switch (method) {
            case "getLong":
                return value == null ? 0L : ((Number) value).longValue();
            case "getInt":
                return value == null ? 0 : ((Number) value).intValue();
            case "getBoolean":
                return value != null && (Boolean) value;
            case "getDate":
                return value == null ? null : Date.valueOf((LocalDate) value);
            default:
                return value;
        }
    }

    private static Object defaultValue(final Class<?> returnType) {
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == int.class) {
            return 0;
        }
        return null;
    }
}
