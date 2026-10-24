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
package org.apache.fineract.portfolio.tax.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupMappingsData;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Unit tests for {@link TaxUtils}.
 *
 * The amounts below are chosen so that amount * percentage / 100 is exactly halfway between two values at the target
 * scale (or exactly on a whole value). Such ties are rounded by the configured rounding mode, so the split must be
 * computed exactly before rounding.
 */
class TaxUtilsTest {

    private static final LocalDate DATE = LocalDate.of(2026, 1, 1);

    private MockedStatic<MoneyHelper> moneyHelperMock;

    @BeforeEach
    void setUpMoneyHelper() {
        moneyHelperMock = mockStatic(MoneyHelper.class);
    }

    @AfterEach
    void tearDownMoneyHelper() {
        moneyHelperMock.close();
    }

    @Test
    void splitTax_roundsExactHalfCentTieWithConfiguredMode() {
        // 4.10 * 15 % = 0.615 exactly; HALF_EVEN must give 0.62
        roundingMode(RoundingMode.HALF_EVEN);
        TaxComponent component = taxComponentWithRate(new BigDecimal("15"));

        Map<TaxComponent, BigDecimal> result = TaxUtils.splitTax(new BigDecimal("4.10"), DATE, Set.of(mappingFor(component)), 2);

        assertThat(result.get(component)).isEqualByComparingTo(new BigDecimal("0.62"));
    }

    @Test
    void splitTax_roundsExactHalfUnitTieWithConfiguredMode_forZeroDecimalCurrency() {
        // 5250 * 1.4 % = 73.5 exactly; HALF_EVEN must give 74
        roundingMode(RoundingMode.HALF_EVEN);
        TaxComponent component = taxComponentWithRate(new BigDecimal("1.4"));

        Map<TaxComponent, BigDecimal> result = TaxUtils.splitTax(new BigDecimal("5250"), DATE, Set.of(mappingFor(component)), 0);

        assertThat(result.get(component)).isEqualByComparingTo(new BigDecimal("74"));
    }

    @Test
    void splitTax_keepsExactWholeAmount_whenRoundingDown() {
        // 5500 * 1.4 % = 77 exactly; DOWN must keep 77
        roundingMode(RoundingMode.DOWN);
        TaxComponent component = taxComponentWithRate(new BigDecimal("1.4"));

        Map<TaxComponent, BigDecimal> result = TaxUtils.splitTax(new BigDecimal("5500"), DATE, Set.of(mappingFor(component)), 0);

        assertThat(result.get(component)).isEqualByComparingTo(new BigDecimal("77"));
    }

    @Test
    void splitTax_returnsRequestedScale() {
        roundingMode(RoundingMode.HALF_EVEN);
        TaxComponent component = taxComponentWithRate(new BigDecimal("16"));

        Map<TaxComponent, BigDecimal> result = TaxUtils.splitTax(new BigDecimal("1000.00"), DATE, Set.of(mappingFor(component)), 2);

        assertThat(result.get(component)).isEqualTo(new BigDecimal("160.00"));
    }

    @Test
    void splitTaxData_roundsExactHalfCentTieWithConfiguredMode() {
        // 4.10 * 15 % = 0.615 exactly; HALF_EVEN must give 0.62
        roundingMode(RoundingMode.HALF_EVEN);
        TaxComponentData component = taxComponentDataWithRate(new BigDecimal("15"));

        Map<TaxComponentData, BigDecimal> result = TaxUtils.splitTaxData(new BigDecimal("4.10"), DATE, Set.of(mappingDataFor(component)),
                2);

        assertThat(result.get(component)).isEqualByComparingTo(new BigDecimal("0.62"));
    }

    @Test
    void splitTaxData_keepsExactWholeAmount_whenRoundingDown() {
        // 5500 * 1.4 % = 77 exactly; DOWN must keep 77
        roundingMode(RoundingMode.DOWN);
        TaxComponentData component = taxComponentDataWithRate(new BigDecimal("1.4"));

        Map<TaxComponentData, BigDecimal> result = TaxUtils.splitTaxData(new BigDecimal("5500"), DATE, Set.of(mappingDataFor(component)),
                0);

        assertThat(result.get(component)).isEqualByComparingTo(new BigDecimal("77"));
    }

    private void roundingMode(RoundingMode roundingMode) {
        moneyHelperMock.when(MoneyHelper::getRoundingMode).thenReturn(roundingMode);
    }

    private TaxComponent taxComponentWithRate(BigDecimal percentage) {
        TaxComponent component = mock(TaxComponent.class);
        when(component.getApplicablePercentage(any(LocalDate.class))).thenReturn(percentage);
        return component;
    }

    private TaxGroupMappings mappingFor(TaxComponent component) {
        TaxGroupMappings mapping = mock(TaxGroupMappings.class);
        when(mapping.getTaxComponent()).thenReturn(component);
        when(mapping.occursOnDayFromAndUpToAndIncluding(any(LocalDate.class))).thenReturn(true);
        return mapping;
    }

    private TaxComponentData taxComponentDataWithRate(BigDecimal percentage) {
        TaxComponentData component = mock(TaxComponentData.class);
        when(component.getApplicablePercentage(any(LocalDate.class))).thenReturn(percentage);
        return component;
    }

    private TaxGroupMappingsData mappingDataFor(TaxComponentData component) {
        TaxGroupMappingsData mapping = mock(TaxGroupMappingsData.class);
        when(mapping.getTaxComponent()).thenReturn(component);
        when(mapping.occursOnDayFromAndUpToAndIncluding(any(LocalDate.class))).thenReturn(true);
        return mapping;
    }
}
