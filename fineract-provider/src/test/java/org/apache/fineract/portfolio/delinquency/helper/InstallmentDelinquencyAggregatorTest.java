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
package org.apache.fineract.portfolio.delinquency.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import org.apache.fineract.portfolio.loanaccount.data.InstallmentLevelDelinquency;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for InstallmentDelinquencyAggregator.
 *
 * These tests cover the critical aggregation logic that groups installment-level delinquency data by range and sums
 * amounts. This logic is essential for financial reporting and has zero test coverage before this test class was
 * created.
 *
 * Test scenarios cover: - Same range aggregation (summing amounts) - Different range separation - Multiple installments
 * with mixed ranges - Sorting by minimumAgeDays - Empty input handling
 */
class InstallmentDelinquencyAggregatorTest {

    private FineractPlatformTenant testTenant;
    private FineractPlatformTenant originalTenant;

    @BeforeEach
    void setUp() {
        originalTenant = ThreadLocalContextUtil.getTenant();
        testTenant = new FineractPlatformTenant(1L, "test", "Test Tenant", "Asia/Kolkata", null);
        ThreadLocalContextUtil.setTenant(testTenant);
        MoneyHelper.initializeTenantRoundingMode("test", 4);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.setTenant(originalTenant);
        MoneyHelper.clearCache();
    }

    @Test
    void testAggregateAndSort_emptyInput_returnsEmptyList() {
        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void testAggregateAndSort_singleInstallment_returnsSameInstallment() {
        LoanInstallmentDelinquencyTagData data = createTagData(1L, 1L, "RANGE_1", 1, 3, "250.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data));

        assertThat(result).hasSize(1);
        assertInstallmentDelinquency(result.get(0), 1L, "RANGE_1", 1, 3, "250.00");
    }

    @Test
    void testAggregateAndSort_twoInstallmentsSameRange_sumsAmounts() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 3L, "RANGE_3", 4, 60, "250.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 3L, "RANGE_3", 4, 60, "500.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2));

        assertThat(result).hasSize(1);
        assertInstallmentDelinquency(result.get(0), 3L, "RANGE_3", 4, 60, "750.00");
    }

    @Test
    void testAggregateAndSort_threeInstallmentsSameRange_sumsAllAmounts() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 2L, "RANGE_2", 2, 3, "100.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 2L, "RANGE_2", 2, 3, "150.00");
        LoanInstallmentDelinquencyTagData data3 = createTagData(3L, 2L, "RANGE_2", 2, 3, "200.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2, data3));

        assertThat(result).hasSize(1);
        assertInstallmentDelinquency(result.get(0), 2L, "RANGE_2", 2, 3, "450.00");
    }

    @Test
    void testAggregateAndSort_twoInstallmentsDifferentRanges_remainsSeparate() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 1L, "RANGE_1", 1, 3, "250.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 3L, "RANGE_3", 4, 60, "250.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2));

        assertThat(result).hasSize(2);
        assertInstallmentDelinquency(result.get(0), 1L, "RANGE_1", 1, 3, "250.00");
        assertInstallmentDelinquency(result.get(1), 3L, "RANGE_3", 4, 60, "250.00");
    }

    @Test
    void testAggregateAndSort_multipleInstallmentsMixedRanges_aggregatesAndSeparatesCorrectly() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 1L, "RANGE_1", 1, 3, "100.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 1L, "RANGE_1", 1, 3, "150.00");
        LoanInstallmentDelinquencyTagData data3 = createTagData(3L, 3L, "RANGE_3", 4, 60, "200.00");
        LoanInstallmentDelinquencyTagData data4 = createTagData(4L, 3L, "RANGE_3", 4, 60, "300.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2, data3, data4));

        assertThat(result).hasSize(2);
        assertInstallmentDelinquency(result.get(0), 1L, "RANGE_1", 1, 3, "250.00");
        assertInstallmentDelinquency(result.get(1), 3L, "RANGE_3", 4, 60, "500.00");
    }

    @Test
    void testAggregateAndSort_sortsByMinimumAgeDaysAscending() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 3L, "RANGE_3", 4, 60, "250.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 1L, "RANGE_1", 1, 3, "250.00");
        LoanInstallmentDelinquencyTagData data3 = createTagData(3L, 2L, "RANGE_2", 2, 3, "250.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2, data3));

        assertThat(result).hasSize(3);
        assertEquals(1L, result.get(0).getRangeId());
        assertEquals(Integer.valueOf(1), result.get(0).getMinimumAgeDays());
        assertEquals(2L, result.get(1).getRangeId());
        assertEquals(Integer.valueOf(2), result.get(1).getMinimumAgeDays());
        assertEquals(3L, result.get(2).getRangeId());
        assertEquals(Integer.valueOf(4), result.get(2).getMinimumAgeDays());
    }

    @Test
    void testAggregateAndSort_complexScenario_aggregatesSortsCorrectly() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 3L, "RANGE_3", 4, 60, "500.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 1L, "RANGE_1", 1, 3, "250.00");
        LoanInstallmentDelinquencyTagData data3 = createTagData(3L, 3L, "RANGE_3", 4, 60, "250.00");
        LoanInstallmentDelinquencyTagData data4 = createTagData(4L, 2L, "RANGE_2", 2, 3, "100.00");
        LoanInstallmentDelinquencyTagData data5 = createTagData(5L, 1L, "RANGE_1", 1, 3, "150.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator
                .aggregateAndSort(List.of(data1, data2, data3, data4, data5));

        assertThat(result).hasSize(3);
        assertInstallmentDelinquency(result.get(0), 1L, "RANGE_1", 1, 3, "400.00");
        assertInstallmentDelinquency(result.get(1), 2L, "RANGE_2", 2, 3, "100.00");
        assertInstallmentDelinquency(result.get(2), 3L, "RANGE_3", 4, 60, "750.00");
    }

    @Test
    void testAggregateAndSort_nullMinimumAgeDays_treatsAsZero() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 1L, "NO_DELINQUENCY", null, null, "100.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 2L, "RANGE_1", 1, 3, "200.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2));

        assertThat(result).hasSize(2);
        assertEquals(1L, result.get(0).getRangeId());
        assertEquals(2L, result.get(1).getRangeId());
    }

    @Test
    void testAggregateAndSort_decimalPrecision_maintainsPrecision() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 1L, "RANGE_1", 1, 3, "100.12");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 1L, "RANGE_1", 1, 3, "200.34");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDelinquentAmount()).isEqualByComparingTo("300.46");
    }

    @Test
    void testAggregateAndSort_zeroAmounts_includesInResult() {
        LoanInstallmentDelinquencyTagData data1 = createTagData(1L, 1L, "RANGE_1", 1, 3, "0.00");
        LoanInstallmentDelinquencyTagData data2 = createTagData(2L, 2L, "RANGE_2", 2, 3, "100.00");

        List<InstallmentLevelDelinquency> result = InstallmentDelinquencyAggregator.aggregateAndSort(List.of(data1, data2));

        assertThat(result).hasSize(2);
        assertInstallmentDelinquency(result.get(0), 1L, "RANGE_1", 1, 3, "0.00");
        assertInstallmentDelinquency(result.get(1), 2L, "RANGE_2", 2, 3, "100.00");
    }

    private LoanInstallmentDelinquencyTagData createTagData(Long installmentId, Long rangeId, String classification, Integer minDays,
            Integer maxDays, String amount) {
        return new TestLoanInstallmentDelinquencyTagData(installmentId,
                new TestInstallmentDelinquencyRange(rangeId, classification, minDays, maxDays), new BigDecimal(amount));
    }

    private void assertInstallmentDelinquency(InstallmentLevelDelinquency actual, Long expectedRangeId, String expectedClassification,
            Integer expectedMinDays, Integer expectedMaxDays, String expectedAmount) {
        assertNotNull(actual);
        assertEquals(expectedRangeId, actual.getRangeId());
        assertEquals(expectedClassification, actual.getClassification());
        assertEquals(expectedMinDays, actual.getMinimumAgeDays());
        assertEquals(expectedMaxDays, actual.getMaximumAgeDays());
        assertThat(actual.getDelinquentAmount()).isEqualByComparingTo(expectedAmount);
    }

    private static class TestLoanInstallmentDelinquencyTagData implements LoanInstallmentDelinquencyTagData {

        private final Long id;
        private final InstallmentDelinquencyRange range;
        private final BigDecimal amount;

        TestLoanInstallmentDelinquencyTagData(Long id, InstallmentDelinquencyRange range, BigDecimal amount) {
            this.id = id;
            this.range = range;
            this.amount = amount;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public InstallmentDelinquencyRange getDelinquencyRange() {
            return range;
        }

        @Override
        public BigDecimal getOutstandingAmount() {
            return amount;
        }
    }

    private static class TestInstallmentDelinquencyRange implements LoanInstallmentDelinquencyTagData.InstallmentDelinquencyRange {

        private final Long id;
        private final String classification;
        private final Integer minDays;
        private final Integer maxDays;

        TestInstallmentDelinquencyRange(Long id, String classification, Integer minDays, Integer maxDays) {
            this.id = id;
            this.classification = classification;
            this.minDays = minDays;
            this.maxDays = maxDays;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getClassification() {
            return classification;
        }

        @Override
        public Integer getMinimumAgeDays() {
            return minDays;
        }

        @Override
        public Integer getMaximumAgeDays() {
            return maxDays;
        }
    }
}
