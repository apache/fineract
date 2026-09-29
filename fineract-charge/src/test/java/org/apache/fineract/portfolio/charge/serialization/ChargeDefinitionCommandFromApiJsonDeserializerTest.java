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
package org.apache.fineract.portfolio.charge.serialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;

class ChargeDefinitionCommandFromApiJsonDeserializerTest {

    /**
     * SEMI_MONTHLY has no period length, so the date generator used for overdue fee schedules cannot step by it. The
     * charge definition must never accept it as a fee frequency.
     */
    private static final int SEMI_MONTHLY = PeriodFrequencyType.SEMI_MONTHLY.getValue();

    private final ChargeDefinitionCommandFromApiJsonDeserializer deserializer = new ChargeDefinitionCommandFromApiJsonDeserializer(
            new FromJsonHelper());

    @Test
    void createRejectsASemiMonthlyFeeFrequency() {
        final String json = """
                {"name": "Overdue fee", "currencyCode": "USD", "amount": 10, "locale": "en", "chargeAppliesTo": 1, \
                "chargeTimeType": 9, "chargeCalculationType": 1, "chargePaymentMode": 0, "penalty": true, "active": true, \
                "feeInterval": 1, "feeFrequency": %d}""".formatted(SEMI_MONTHLY);

        assertFeeFrequencyRejected(assertThrows(PlatformApiDataValidationException.class, () -> deserializer.validateForCreate(json)));
    }

    @Test
    void updateRejectsASemiMonthlyFeeFrequency() {
        final String json = """
                {"locale": "en", "feeFrequency": %d}""".formatted(SEMI_MONTHLY);

        assertFeeFrequencyRejected(assertThrows(PlatformApiDataValidationException.class, () -> deserializer.validateForUpdate(json)));
    }

    private static void assertFeeFrequencyRejected(final PlatformApiDataValidationException rejection) {
        assertTrue(
                rejection.getErrors().stream()
                        .anyMatch(error -> ChargeDefinitionCommandFromApiJsonDeserializer.FEE_FREQUENCY.equals(error.getParameterName())),
                "feeFrequency must be among the rejected parameters");
    }
}
