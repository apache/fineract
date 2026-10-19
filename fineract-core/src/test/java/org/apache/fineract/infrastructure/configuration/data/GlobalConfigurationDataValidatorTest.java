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

class GlobalConfigurationDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private GlobalConfigurationDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GlobalConfigurationDataValidator(fromJsonHelper);
    }

    /**
     * Regression test: a negative {@code value} used to be reported under the {@code enabled} parameter name instead of
     * {@code value}, because the validator reused the wrong constant when building the validation error.
     */
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

    @Test
    void validateAllowedValuesRejectsStringValueThatIsNotAllowed() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAllowedValues(stringValueCommand("weekly"), GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME));

        assertEquals(1, ex.getErrors().size());
        assertEquals("validation.msg.globalConfiguration.stringValue.is.not.one.of.expected.enumerations",
                ex.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void validateAllowedValuesAcceptsAllowedStringValueInAnyCase() {
        assertDoesNotThrow(() -> validator.validateAllowedValues(stringValueCommand("Real-Time"),
                GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME));
    }

    @Test
    void validateForUpdateRejectsBlankStringValue() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate(stringValueCommand(" ")));

        assertEquals(1, ex.getErrors().size());
        assertEquals("validation.msg.globalConfiguration.stringValue.cannot.be.blank",
                ex.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void validateAllowedValuesReportsEachNotAllowedElementOfMultipleValues() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateAllowedValues(stringValueCommand("ACTIVE,ACTIV,CLOSED"),
                        GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER));

        assertEquals(2, ex.getErrors().size());
    }

    @Test
    void validateAllowedValuesAcceptsAnyStringValueForUnrestrictedConfig() {
        assertDoesNotThrow(() -> validator.validateAllowedValues(stringValueCommand("any folder"),
                GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME));
    }

    @Test
    void validateAllowedValuesIgnoresMissingStringValue() {
        JsonCommand command = new JsonCommand(1L, fromJsonHelper.parse("{\"enabled\": true}"), fromJsonHelper);

        assertDoesNotThrow(() -> validator.validateAllowedValues(command, GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE));
    }

    private JsonCommand stringValueCommand(final String stringValue) {
        return new JsonCommand(1L, fromJsonHelper.parse("{\"stringValue\": \"" + stringValue + "\"}"), fromJsonHelper);
    }
}
