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
package org.apache.fineract.portfolio.savings.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;

class SavingsAccountChargeDataValidatorTest {

    private final SavingsAccountChargeDataValidator validator = new SavingsAccountChargeDataValidator(new FromJsonHelper());

    @Test
    void validateAddAcceptsFeeOnMonthDayWithMonthDayFormatAndLocale() {
        assertDoesNotThrow(() -> validator
                .validateAdd("{\"chargeId\": 1, \"amount\": 10, \"feeOnMonthDay\": \"15 March\", \"monthDayFormat\": \"dd MMMM\", "
                        + "\"locale\": \"en\"}"));
    }

    @Test
    void validateAddRejectsFeeOnMonthDayWithoutMonthDayFormat() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAdd("{\"chargeId\": 1, \"amount\": 10, \"feeOnMonthDay\": \"15 March\", \"locale\": \"en\"}"));

        assertEquals("validation.msg.missing.monthDayFormat.parameter", exception.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void validateUpdateRejectsFeeOnMonthDayWithoutMonthDayFormat() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateUpdate("{\"amount\": 10, \"feeOnMonthDay\": \"15 March\", \"locale\": \"en\"}"));

        assertEquals("validation.msg.missing.monthDayFormat.parameter", exception.getErrors().get(0).getUserMessageGlobalisationCode());
    }
}
