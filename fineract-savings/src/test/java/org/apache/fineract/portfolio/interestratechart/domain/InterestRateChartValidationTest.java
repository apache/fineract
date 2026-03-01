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
package org.apache.fineract.portfolio.interestratechart.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for InterestRateChart validation error codes and arguments
 */
public class InterestRateChartValidationTest {

    private List<ApiParameterError> dataValidationErrors;

    @BeforeEach
    public void setUp() {
        dataValidationErrors = new ArrayList<>();
    }

    @Test
    public void testOverlappingRangesValidation() {
        // Given - use DataValidatorBuilder with resource/parameter context
        DataValidatorBuilder v = new DataValidatorBuilder(dataValidationErrors).resource("savings.interestRateChart").parameter("slabs");

        // When - simulate overlapping ranges validation
        v.failWithCode("overlap", 1, 12, 6, 18, 1000.0, 5000.0, 3000.0, 8000.0);

        // Then
        assertEquals(1, dataValidationErrors.size(), "Expected exactly one validation error");
        ApiParameterError actualError = dataValidationErrors.get(0);
        assertEquals("validation.msg.savings.interestRateChart.slabs.overlap", actualError.getUserMessageGlobalisationCode());
        // Check that arguments are present
        assertFalse(actualError.getArgs().isEmpty());
    }

    @Test
    public void testGapBetweenRangesValidation() {
        // Given - use DataValidatorBuilder with resource/parameter context
        DataValidatorBuilder v = new DataValidatorBuilder(dataValidationErrors).resource("savings.interestRateChart").parameter("slabs");

        // When - simulate gap between ranges validation
        v.failWithCode("gap", 1, 12, 15, 24, 1000.0, 5000.0, 6000.0, 10000.0);

        // Then
        assertEquals(1, dataValidationErrors.size(), "Expected exactly one validation error");
        ApiParameterError actualError = dataValidationErrors.get(0);
        assertEquals("validation.msg.savings.interestRateChart.slabs.gap", actualError.getUserMessageGlobalisationCode());
        // Check that arguments are present
        assertFalse(actualError.getArgs().isEmpty());
    }

    @Test
    public void testSinglePeriodChartSlabWithExplicitEndDoesNotFailValidation() {
        InterestRateChartFields chartFields = InterestRateChartFields.createNew("chart", "chart", LocalDate.of(2022, 1, 1),
                LocalDate.of(2022, 12, 31), false);
        InterestRateChart chart = InterestRateChart.createNew(chartFields, List.of());

        InterestRateChartSlabFields slabFields = InterestRateChartSlabFields.createNew("Level 1", SavingsPeriodFrequencyType.MONTHS, 0, 3,
                null, null, new BigDecimal("12"), "USD");
        InterestRateChartSlab.createNew(slabFields, chart);

        DataValidatorBuilder validator = new DataValidatorBuilder(dataValidationErrors).resource("savings.interestRateChart")
                .parameter("slabs");
        chart.validateChartSlabs(validator);

        assertTrue(dataValidationErrors.isEmpty(),
                "Expected no validation error for a single well-formed period slab with explicit toPeriod");
    }
}
