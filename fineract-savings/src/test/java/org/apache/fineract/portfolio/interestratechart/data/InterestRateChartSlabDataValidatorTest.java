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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: when {@code amountRangeFrom} is greater than {@code amountRangeTo}, the validator used to report the
 * (unrelated, absent) {@code fromPeriod} value in the error message arguments instead of the actual
 * {@code amountRangeFrom} value, because it reused the wrong variable when building the validation error.
 */
class InterestRateChartSlabDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private InterestRateChartSlabDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InterestRateChartSlabDataValidator(fromJsonHelper, new InterestIncentiveDataValidator(fromJsonHelper));
    }

    @Test
    void validateChartSlabsCreateWithFromAmountGreaterThanToAmountReportsFromAmountInErrorArgs() {
        String json = """
                {
                  "amountRangeFrom": 1000,
                  "amountRangeTo": 10,
                  "annualInterestRate": 5
                }
                """;

        List<ApiParameterError> errors = new ArrayList<>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(errors).resource("chartslab");

        validator.validateChartSlabsCreate(fromJsonHelper.parse(json), baseDataValidator, Locale.US, null);

        ApiParameterError error = errors.stream()
                .filter(e -> "validation.msg.chartslab.amountRangeFrom.from.amount.greater.than.to.amount"
                        .equals(e.getUserMessageGlobalisationCode()))
                .findFirst().orElseThrow(() -> new AssertionError("Expected 'from.amount.greater.than.to.amount' validation error"));

        Object reportedValue = error.getArgs().get(0).getValue();
        assertEquals(BigDecimal.valueOf(1000), reportedValue, "Expected the reported value to be amountRangeFrom");
    }
}
