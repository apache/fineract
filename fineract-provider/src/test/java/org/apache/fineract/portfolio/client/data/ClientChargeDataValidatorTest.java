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
package org.apache.fineract.portfolio.client.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;

class ClientChargeDataValidatorTest {

    private final ClientChargeDataValidator validator = new ClientChargeDataValidator(new FromJsonHelper());

    private String payment(String parameters) {
        return "{\"amount\":10,\"transactionDate\":\"2026-11-05\",\"dateFormat\":\"yyyy-MM-dd\",\"locale\":\"en\"" + parameters + "}";
    }

    @Test
    void acceptsSavingsFunding() {
        assertDoesNotThrow(() -> validator.validatePayCharge(payment(",\"savingsAccountId\":1")));
    }

    @Test
    void acceptsExistingPaymentWithoutSavingsFunding() {
        assertDoesNotThrow(() -> validator.validatePayCharge(payment("")));
    }

    @Test
    void rejectsInvalidSavingsFunding() {
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validatePayCharge(payment(",\"savingsAccountId\":0")));
        assertThrows(PlatformApiDataValidationException.class, () -> validator.validatePayCharge(payment(",\"savingsAccountId\":null")));
    }

    @Test
    void rejectsCashPaymentDetailsWithSavingsFunding() {
        assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validatePayCharge(payment(",\"savingsAccountId\":1,\"paymentTypeId\":1")));
    }
}
