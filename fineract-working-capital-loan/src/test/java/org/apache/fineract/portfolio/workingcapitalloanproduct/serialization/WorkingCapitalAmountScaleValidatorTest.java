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
package org.apache.fineract.portfolio.workingcapitalloanproduct.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.junit.jupiter.api.Test;

class WorkingCapitalAmountScaleValidatorTest {

    private static final String PARAM = "paymentAmount";
    private static final String SCALE_ERROR = "validation.msg.resource.paymentAmount.scale.is.greater.than.2";

    @Test
    void valueAtTheCurrencyScale_IsAccepted() {
        assertEquals(List.of(), validate(new BigDecimal("47.22"), 2));
    }

    @Test
    void valueCoarserThanTheCurrency_IsAccepted() {
        assertEquals(List.of(), validate(new BigDecimal("47.2"), 2));
    }

    @Test
    void trailingZerosBeyondTheCurrencyScale_AreAccepted() {
        assertEquals(List.of(), validate(new BigDecimal("47.2200"), 2));
    }

    @Test
    void valueFinerThanTheCurrency_IsRejected() {
        assertEquals(List.of(SCALE_ERROR), validate(new BigDecimal("47.225"), 2));
    }

    @Test
    void negativeValueFinerThanTheCurrency_IsRejectedOnScaleToo() {
        assertEquals(List.of(SCALE_ERROR), validate(new BigDecimal("-47.225"), 2));
    }

    @Test
    void zero_IsAccepted() {
        assertEquals(List.of(), validate(BigDecimal.ZERO, 2));
    }

    @Test
    void zeroDecimalCurrency_RejectsAnyFraction() {
        assertEquals(List.of("validation.msg.resource.paymentAmount.scale.is.greater.than.0"), validate(new BigDecimal("47.22"), 0));
    }

    @Test
    void unknownCurrencyDigits_SkipsTheCheck() {
        assertEquals(List.of(), validate(new BigDecimal("47.225"), null));
    }

    @Test
    void nullValue_SkipsTheCheck() {
        assertEquals(List.of(), validate(null, 2));
    }

    private static List<String> validate(final BigDecimal value, final Integer currencyDigits) {
        final List<ApiParameterError> errors = new ArrayList<>();
        final DataValidatorBuilder validator = new DataValidatorBuilder(errors).resource("resource");
        WorkingCapitalAmountScaleValidator.validateNotFinerThanCurrency(validator, PARAM, value, currencyDigits);
        return errors.stream().map(ApiParameterError::getUserMessageGlobalisationCode).toList();
    }
}
