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
package org.apache.fineract.portfolio.tax.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.tax.api.TaxApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaxValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private TaxValidator taxValidator;

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 10);

    @BeforeEach
    void setUp() {
        taxValidator = new TaxValidator(fromJsonHelper);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, TODAY)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void testValidateStartDateBoundary1ExistingPastNewFutureRejected() {
        LocalDate existingStartDate = TODAY.minusDays(5);
        LocalDate newStartDate = TODAY.plusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(newStartDate);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> taxValidator.validateStartDate(existingStartDate, command));

        assertEquals("validation.msg.tax.component.startDate.start.date.cannot.be.modified.after.activation",
                exception.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void testValidateStartDateBoundary2ExistingPastSamePastDateAllowed() {
        LocalDate existingStartDate = TODAY.minusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(existingStartDate);

        assertDoesNotThrow(() -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary3ExistingTodayNewFutureRejected() {
        LocalDate existingStartDate = TODAY;
        LocalDate newStartDate = TODAY.plusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(newStartDate);

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> taxValidator.validateStartDate(existingStartDate, command));

        assertEquals("validation.msg.tax.component.startDate.start.date.cannot.be.modified.after.activation",
                exception.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void testValidateStartDateBoundary4ExistingTodaySameTodayAllowed() {
        LocalDate existingStartDate = TODAY;

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(TODAY);

        assertDoesNotThrow(() -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary5ExistingFutureAnotherFutureDateAllowed() {
        LocalDate existingStartDate = TODAY.plusDays(5);
        LocalDate newStartDate = TODAY.plusDays(10);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(newStartDate);

        assertDoesNotThrow(() -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary6ExistingFutureSameFutureDateAllowed() {
        LocalDate existingStartDate = TODAY.plusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(existingStartDate);

        assertDoesNotThrow(() -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary7ExistingFutureTodayRejected() {
        LocalDate existingStartDate = TODAY.plusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(TODAY);

        assertThrows(PlatformApiDataValidationException.class, () -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary8ExistingFuturePastRejected() {
        LocalDate existingStartDate = TODAY.plusDays(5);

        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(TODAY.minusDays(1));

        assertThrows(PlatformApiDataValidationException.class, () -> taxValidator.validateStartDate(existingStartDate, command));
    }

    @Test
    void testValidateStartDateBoundary9ExistingPastOrTodayNullRejected() {
        LocalDate existingPastDate = TODAY.minusDays(5);
        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(null);

        PlatformApiDataValidationException exceptionPast = assertThrows(PlatformApiDataValidationException.class,
                () -> taxValidator.validateStartDate(existingPastDate, command));
        assertEquals("validation.msg.tax.component.startDate.start.date.cannot.be.modified.after.activation",
                exceptionPast.getErrors().get(0).getUserMessageGlobalisationCode());

        LocalDate existingTodayDate = TODAY;
        PlatformApiDataValidationException exceptionToday = assertThrows(PlatformApiDataValidationException.class,
                () -> taxValidator.validateStartDate(existingTodayDate, command));
        assertEquals("validation.msg.tax.component.startDate.start.date.cannot.be.modified.after.activation",
                exceptionToday.getErrors().get(0).getUserMessageGlobalisationCode());
    }

    @Test
    void testValidateStartDateBoundary10ExistingFutureNullRejected() {
        LocalDate existingFutureDate = TODAY.plusDays(5);
        JsonCommand command = mock(JsonCommand.class);
        when(command.parameterExists(TaxApiConstants.startDateParamName)).thenReturn(true);
        when(command.localDateValueOfParameterNamed(TaxApiConstants.startDateParamName)).thenReturn(null);

        assertThrows(PlatformApiDataValidationException.class, () -> taxValidator.validateStartDate(existingFutureDate, command));
    }

    @Test
    void testValidateForTaxComponentUpdateWithSupportedGLParameters() {
        String json = """
                {
                    "debitAccountType": 1,
                    "debitAccountId": 10,
                    "creditAccountType": 4,
                    "creditAccountId": 20
                }
                """;

        assertDoesNotThrow(() -> taxValidator.validateForTaxComponentUpdate(json));
    }

    @Test
    void testValidateForTaxComponentUpdateWithUnsupportedParameterThrows() {
        String json = """
                {
                    "unsupportedParam": "value"
                }
                """;

        assertThrows(UnsupportedParameterException.class, () -> taxValidator.validateForTaxComponentUpdate(json));
    }
}
