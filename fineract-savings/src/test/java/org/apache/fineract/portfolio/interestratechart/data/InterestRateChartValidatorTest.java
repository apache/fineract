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
package org.apache.fineract.portfolio.interestratechart.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartFields;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabFields;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.junit.jupiter.api.Test;

class InterestRateChartValidatorTest {

    private final List<ApiParameterError> errors = new ArrayList<>();
    private final DataValidatorBuilder validator = new DataValidatorBuilder(errors).resource("interestRateChart");

    @Test
    void contiguousPeriodSlabsPass() {
        InterestRateChart chart = chart(2022);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 0, 6);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 7, null);

        InterestRateChartValidator.validateChartSlabs(chart, validator);

        assertTrue(errors.isEmpty(), () -> "Unexpected errors: " + codes());
    }

    @Test
    void overlappingSlabsAreReportedAgainstTheSlabsParameter() {
        InterestRateChart chart = chart(2022);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 0, 6);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 3, null);

        InterestRateChartValidator.validateChartSlabs(chart, validator);

        assertEquals(List.of("validation.msg.savings.interestRateChart.slabs.overlap"), codes());
    }

    @Test
    void gapBetweenSlabsIsReportedAgainstTheSlabsParameter() {
        InterestRateChart chart = chart(2022);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 0, 6);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 9, null);

        InterestRateChartValidator.validateChartSlabs(chart, validator);

        assertEquals(List.of("validation.msg.savings.interestRateChart.slabs.gap"), codes());
    }

    @Test
    void slabsMustShareThePeriodType() {
        InterestRateChart chart = chart(2022);
        addSlab(chart, SavingsPeriodFrequencyType.MONTHS, 0, 6);
        addSlab(chart, SavingsPeriodFrequencyType.YEARS, 7, null);

        InterestRateChartValidator.validateChartSlabs(chart, validator);

        assertEquals(List.of("validation.msg.interestRateChart.periodType.period.type.is.not.same"), codes());
    }

    @Test
    void chartOverlappingAnotherChartIsReported() {
        InterestRateChart chart = chart(2022);
        InterestRateChart other = chart(2022);

        InterestRateChartValidator.validateNoOverlapWithOtherCharts(chart, Set.of(chart, other), validator);

        assertEquals(List.of("validation.msg.interestRateChart.chart.overlapping.from.and.end.dates"), codes());
    }

    @Test
    void chartDoesNotOverlapItselfOrALaterChart() {
        InterestRateChart chart = chart(2022);
        InterestRateChart other = chart(2023);

        InterestRateChartValidator.validateNoOverlapWithOtherCharts(chart, Set.of(chart, other), validator);

        assertTrue(errors.isEmpty(), () -> "Unexpected errors: " + codes());
    }

    private static InterestRateChart chart(int year) {
        InterestRateChartFields fields = InterestRateChartFields.createNew("chart", "chart", LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31), false);
        return InterestRateChart.createNew(fields, List.of());
    }

    private static void addSlab(InterestRateChart chart, SavingsPeriodFrequencyType periodType, Integer fromPeriod, Integer toPeriod) {
        InterestRateChartSlab.createNew(
                InterestRateChartSlabFields.createNew("slab", periodType, fromPeriod, toPeriod, null, null, new BigDecimal("12"), "USD"),
                chart);
    }

    private List<String> codes() {
        return errors.stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList();
    }
}
