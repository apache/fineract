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
package org.apache.fineract.infrastructure.configuration.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: a negative {@code value} used to be reported under the {@code enabled} parameter name instead of
 * {@code value}, because the validator reused the wrong constant when building the validation error.
 */
class GlobalConfigurationDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private GlobalConfigurationDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GlobalConfigurationDataValidator(fromJsonHelper);
    }

    @Test
    void validateForUpdateWithNegativeValueReportsErrorForValueParameter() {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("{\"value\": -5}"), fromJsonHelper);

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate(command));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "value".equals(e.getParameterName())),
                "Expected validation error for parameter 'value'");
        assertFalse(ex.getErrors().stream().anyMatch(e -> "enabled".equals(e.getParameterName())),
                "Did not expect a validation error for parameter 'enabled'");
    }

    private JsonCommand stringValueCommand(final String stringValue) {
        return new JsonCommand(1L, fromJsonHelper.parse("{\"stringValue\": \"" + stringValue + "\"}"), fromJsonHelper);
    }

    @Test
    void validateStringValueRejectsUnknownOutstandingInterestStrategy() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateStringValueForUpdate(
                        GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY,
                        stringValueCommand("SOMETHING_ELSE")));

        assertEquals(1, ex.getErrors().size());
        assertEquals("stringValue", ex.getErrors().get(0).getParameterName());
    }

    @Test
    void validateStringValueOutstandingInterestStrategyIsCaseSensitive() {
        // the strategy is matched exactly where it is used, so a lower-case value would fail at runtime
        assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateStringValueForUpdate(
                        GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY,
                        stringValueCommand("total_outstanding_interest")));
        assertDoesNotThrow(() -> validator.validateStringValueForUpdate(
                GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY,
                stringValueCommand("PAYABLE_OUTSTANDING_INTEREST")));
    }

    @Test
    void validateStringValueNextPaymentDueDate() {
        assertThrows(PlatformApiDataValidationException.class, () -> validator
                .validateStringValueForUpdate(GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE, stringValueCommand("tomorrow")));
        // compared ignoring case where it is used
        assertDoesNotThrow(() -> validator.validateStringValueForUpdate(GlobalConfigurationConstants.NEXT_PAYMENT_DUE_DATE,
                stringValueCommand("Next-Unpaid-Due-Date")));
    }

    @Test
    void validateStringValueChargeAccrualDate() {
        assertThrows(PlatformApiDataValidationException.class, () -> validator
                .validateStringValueForUpdate(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, stringValueCommand("__invalid__")));
        assertDoesNotThrow(() -> validator.validateStringValueForUpdate(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                stringValueCommand("submitted-date")));
    }

    @Test
    void validateStringValueAllowsBlankValue() {
        // a blank value makes the platform fall back to the default
        assertDoesNotThrow(
                () -> validator.validateStringValueForUpdate(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE, stringValueCommand("")));
    }

    @Test
    void validateStringValueIgnoresFreeTextConfigurations() {
        assertDoesNotThrow(() -> validator.validateStringValueForUpdate(GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME,
                stringValueCommand("any-folder-name")));
    }

    @Test
    void validateStringValueIgnoresUpdatesWithoutStringValue() {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("{\"enabled\": true}"), fromJsonHelper);
        assertDoesNotThrow(() -> validator.validateStringValueForUpdate(
                GlobalConfigurationConstants.ASSET_OWNER_TRANSFER_OUTSTANDING_INTEREST_CALCULATION_STRATEGY, command));
    }
}
