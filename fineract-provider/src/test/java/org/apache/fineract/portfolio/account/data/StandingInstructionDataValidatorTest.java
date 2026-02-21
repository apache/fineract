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
package org.apache.fineract.portfolio.account.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StandingInstructionDataValidatorTest {

    @Mock
    private FromJsonHelper fromJsonHelper;

    @Mock
    private AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;

    @InjectMocks
    private StandingInstructionDataValidator validator;

    private JsonCommand command;
    private JsonElement element;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        doNothing().when(accountTransfersDetailDataValidator).validate(any(), any());
        when(fromJsonHelper.extractMonthDayNamed(any(), any())).thenReturn(null);

        command = mock(JsonCommand.class);
        element = new JsonObject();
        when(command.json()).thenReturn("{}");
        when(command.parsedJson()).thenReturn(element);
    }

    private void setupMandatoryCreateFields() {

        when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.statusParamName),
            eq(element),
            any(Locale.class)))
            .thenReturn(1);

        when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.statusParamName),
            eq(element),
            org.mockito.ArgumentMatchers.<java.util.Locale>any()))
            .thenReturn(1);

        when(fromJsonHelper.extractLocalDateNamed(
            eq(StandingInstructionApiConstants.validFromParamName),
            eq(element)))
            .thenReturn(LocalDate.of(2024, 1, 1));

        when(fromJsonHelper.extractIntegerNamed(
            eq(AccountDetailConstants.transferTypeParamName),
            eq(element),
            any(Locale.class)))
            .thenReturn(1);

        when(fromJsonHelper.extractIntegerNamed(
            eq(AccountDetailConstants.transferTypeParamName),
            eq(element),
            org.mockito.ArgumentMatchers.<java.util.Locale>any()))
            .thenReturn(1);

        when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.priorityParamName),
            eq(element),
            org.mockito.ArgumentMatchers.<Set<String>>any()))
            .thenReturn(1);

        when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.priorityParamName),
            eq(element),
            org.mockito.ArgumentMatchers.<java.util.Locale>any()))
            .thenReturn(1);

        when(fromJsonHelper.extractStringNamed(
            eq(StandingInstructionApiConstants.nameParamName),
            eq(element)))
            .thenReturn("Test SI");
}

    @Nested
    class FixedAmountTransferTypeTests {

        @Test
        void allFieldsProvided_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrence(1, 30, 1, "10-15");
            setupRecurrenceType(1);
            setupRecurrenceInterval(1);

            when(fromJsonHelper.parameterExists(eq(StandingInstructionApiConstants.amountParamName), eq(element))).thenReturn(true);

            when(fromJsonHelper.parameterExists(eq(StandingInstructionApiConstants.recurrenceTypeParamName), eq(element))).thenReturn(true);

            when(fromJsonHelper.parameterExists(eq(StandingInstructionApiConstants.recurrenceIntervalParamName), eq(element)))
                    .thenReturn(true);

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void nullAmount_shouldFail() {
            setupInstructionType(1);
            setupAmount(null);

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.amountParamName);
        }

        @Test
        void missingRecurrence_shouldFail() {
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrenceMissing();

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.recurrenceIntervalParamName);
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, 99 })
        void fixedInvalidRecurrenceType_shouldFail(int invalidType) {
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(1000));

            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceTypeParamName, element)).thenReturn(true);

            when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.recurrenceTypeParamName), eq(element),
                    org.mockito.ArgumentMatchers.<Set<String>>any())).thenReturn(invalidType);

            assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
        }

        @Test
        void negativeAmount_shouldFail() {
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(-100));
            setupRecurrenceType(1);
            setupRecurrenceInterval(1);

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.amountParamName);
        }

        @Test
        void zeroAmount_shouldFail() {
            setupInstructionType(1);
            setupAmount(BigDecimal.ZERO);

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.amountParamName);
        }

        @Test
        void zeroInterval_shouldFail() {
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrenceInterval(0);
            setupRecurrenceType(1);

            when(fromJsonHelper.parameterExists(eq(StandingInstructionApiConstants.recurrenceIntervalParamName), eq(element)))
                    .thenReturn(true);

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.recurrenceIntervalParamName);
        }
    }

    @Nested
    class DuesAmountTransferTypeTests {

        @Test
        void allNull_shouldPass() {
            setupInstructionType(2);
            setupAmount(null);
            setupRecurrenceMissing();

            assertDoesNotThrow(() -> validator.validateForUpdate(command));
        }

        @Test
        void amountOnly_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(500));
            setupRecurrenceMissing();

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void recurrenceOnly_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(null);
            setupRecurrence(1, 30, 1, "10-15");

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void allFieldsProvided_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrence(1, 30, 1, "10-15");

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void negativeAmount_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(-100));
            setupRecurrenceMissing();

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void zeroInterval_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrenceType(1);
            setupRecurrenceInterval(0);

            assertDoesNotThrow(() -> validator.validateForCreate(command));
        }

        @Test
        void missingAmountParam_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.amountParamName, element)).thenReturn(false);

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void explicitNullAmount_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(null);

            assertDoesNotThrow(() -> validator.validateForCreate(command));
        }
    }

    @Nested
    class InstructionTypeEdgeCases {

        @Test
        void nullType_shouldFail() {
            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.instructionTypeParamName, element))
                    .thenReturn(true);
            when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.instructionTypeParamName),
                    eq(element), org.mockito.ArgumentMatchers.<Set<String>>any())).thenReturn(null);

            assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, 3, 999, -1 })
        void invalidTypeValues_shouldFail(int invalidType) {
            setupInstructionType(invalidType);
            assertThrows(PlatformApiDataValidationException.class, () -> validator.validateForCreate(command));
        }

        @Test
        void missingTypeParam_shouldFail() {
            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.instructionTypeParamName, element))
                    .thenReturn(false);

            PlatformApiDataValidationException ex = assertThrows(
                    PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.instructionTypeParamName);
        }
    }

    @Nested
    class PartialDataTests {

        @Test
        void fixedPartialRecurrence_shouldFail() {
            setupInstructionType(1);
            setupAmount(BigDecimal.valueOf(1000));
            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceIntervalParamName, element)).thenReturn(true);
            when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.recurrenceIntervalParamName), eq(element),
                    org.mockito.ArgumentMatchers.<Set<String>>any())).thenReturn(30);

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertTrue(ex.getErrors().size() >= 1);
        }

        @Test
        void duesPartialInput_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrenceInterval(30);

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }

        @Test
        void fixedOnlyRecurrence_shouldFail() {
            setupInstructionType(1);
            setupAmount(null);
            setupRecurrence(1, 30, 1, "10-15");

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            assertHasError(ex, StandingInstructionApiConstants.amountParamName);
        }

        @Test
        void emptyJson_shouldFail() {
            when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.instructionTypeParamName, element))
                    .thenReturn(false);

            assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
        }
    }

    @Nested
    class ErrorHandlingTests {

        @Test
        void fixedMultipleErrors_shouldAccumulate() {
            setupInstructionType(1);
            setupAmount(null);
            setupRecurrenceMissing();

            PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                    () -> validator.validateForCreate(command));
            Set<ApiParameterError> errors = new HashSet<>(ex.getErrors());
            assertTrue(errors.size() > 1);
        }

        @Test
        void duesValidAmountInvalidRecurrence_shouldPass() {
            setupMandatoryCreateFields();
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(1000));
            setupRecurrenceType(1);
            setupRecurrenceInterval(0);

            assertDoesNotThrow(() -> validator.validateForCreate(command));

        }
    }

    @Nested
    class UpdateValidationTests {

        @Test
        void fixedNullAmount_shouldPass() {

            when(fromJsonHelper.parameterExists(
             eq(StandingInstructionApiConstants.amountParamName),
             eq(element))).thenReturn(true);

            when(fromJsonHelper.extractBigDecimalWithLocaleNamed(
             eq(StandingInstructionApiConstants.amountParamName),
             eq(element),
             org.mockito.ArgumentMatchers.<Set<String>>any()))
             .thenReturn(null);

            assertDoesNotThrow(() -> validator.validateForUpdate(command));

        }

        @Test
        void duesNullAmount_shouldPass() {
            setupInstructionType(2);
            setupAmount(null);

            assertDoesNotThrow(() -> validator.validateForUpdate(command));

        }

        @Test
        void duesPartialUpdate_shouldPass() {
            setupInstructionType(2);
            setupAmount(BigDecimal.valueOf(500));

            assertDoesNotThrow(() -> validator.validateForUpdate(command));

        }
    }

    private void setupInstructionType(int type) {

    when(fromJsonHelper.parameterExists(
            eq(StandingInstructionApiConstants.instructionTypeParamName),
            eq(element)))
        .thenReturn(true);

    when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.instructionTypeParamName),
            eq(element),
            any(Locale.class)))
        .thenReturn(type);
}


private void setupAmount(BigDecimal amount) {

    when(fromJsonHelper.parameterExists(
            eq(StandingInstructionApiConstants.amountParamName),
            eq(element)))
        .thenReturn(true);

    when(fromJsonHelper.extractBigDecimalWithLocaleNamed(
            eq(StandingInstructionApiConstants.amountParamName),
            eq(element)))
        .thenReturn(amount);
}

    private void setupRecurrence(int recurrenceType, int interval, int frequency, String monthDay) {
        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceTypeParamName, element))
                .thenReturn(true);
        when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.recurrenceTypeParamName),
                eq(element), any(Locale.class))).thenReturn(recurrenceType);

        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceIntervalParamName, element))
                .thenReturn(true);
        when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.recurrenceIntervalParamName),
                eq(element), any(Locale.class))).thenReturn(interval);

        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceFrequencyParamName, element))
                .thenReturn(true);
        when(fromJsonHelper.extractIntegerNamed(eq(StandingInstructionApiConstants.recurrenceFrequencyParamName),
                eq(element), any(Locale.class))).thenReturn(frequency);

        when(fromJsonHelper.extractMonthDayNamed(eq(StandingInstructionApiConstants.recurrenceOnMonthDayParamName),
                eq(element))).thenReturn(monthDay != null ? MonthDay.parse("--" + monthDay) : null);
    }

    private void setupRecurrenceInterval(Integer interval) {

    when(fromJsonHelper.parameterExists(
            eq(StandingInstructionApiConstants.recurrenceIntervalParamName),
            eq(element)))
        .thenReturn(true);

    when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.recurrenceIntervalParamName),
            eq(element),
            any(Locale.class)))
        .thenReturn(interval);
}

    private void setupRecurrenceType(Integer type) {

    when(fromJsonHelper.parameterExists(
            eq(StandingInstructionApiConstants.recurrenceTypeParamName),
            eq(element)))
        .thenReturn(true);

    when(fromJsonHelper.extractIntegerNamed(
            eq(StandingInstructionApiConstants.recurrenceTypeParamName),
            eq(element),
            any(Locale.class)))
        .thenReturn(type);
}

    private void setupRecurrenceMissing() {
        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceTypeParamName, element)).thenReturn(false);
        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceIntervalParamName, element)).thenReturn(false);
        when(fromJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceFrequencyParamName, element)).thenReturn(false);
    }

    private void assertHasError(PlatformApiDataValidationException ex, String paramName) {
        boolean found = ex.getErrors().stream().anyMatch(e -> paramName.equals(e.getParameterName()));
        assertTrue(found, "Expected validation error for parameter: " + paramName);
    }
}
