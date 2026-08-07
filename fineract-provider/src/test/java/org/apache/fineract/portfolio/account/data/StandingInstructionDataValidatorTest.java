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

import static org.apache.fineract.portfolio.account.AccountDetailConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.localeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.monthDayFormatParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.nameParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.data.StandingInstructionValidatorFactory;
import org.apache.fineract.portfolio.account.domain.AccountTransferDetails;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StandingInstructionDataValidatorTest {

    private static final String VALIDATION_MSG_PREFIX = "validation.msg";
    private static final String MSG_SEPARATOR = ".";
    private static final String CANNOT_BE_BLANK_ERROR_CODE = "cannot.be.blank";
    private static final String OUT_OF_RANGE_ERROR_CODE = "is.not.within.expected.range";
    private static final String DATE_IS_BEFORE_ERROR_CODE = "is.less.than.date";
    private static final String NOT_GREATER_THAN_ZERO_ERROR_CODE = "not.greater.than.zero";

    private static final String invalidParamName = "invalidParam";
    private static final String invalidValue = "invalidValue";

    @Mock
    private AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;

    @Mock
    private AccountTransferStandingInstruction existingStandingInstruction;

    @Mock
    private AccountTransferDetails accountTransferDetails;

    @Mock
    private StandingInstructionHelper standingInstructionHelper;

    @Mock
    private StandingInstructionValidatorFactory standingInstructionValidatorFactory;

    private static final FromJsonHelper fromApiJsonHelper = new FromJsonHelper();
    private StandingInstructionDataValidator standingInstructionDataValidator;

    private ValidationMode validationMode = ValidationMode.CREATE;

    @BeforeEach
    public void setUp() {
        this.standingInstructionDataValidator = new StandingInstructionDataValidator(
            this.standingInstructionHelper, this.fromApiJsonHelper,
            this.accountTransfersDetailDataValidator, this.standingInstructionValidatorFactory);
    }

    @Nested
    class Common {

        @ParameterizedTest(name = "Mode: {0}")
        @MethodSource("org.apache.fineract.portfolio.account.data.StandingInstructionDataValidatorTest#validationModes")
        void shouldFailWhenRequestBodyIsNull(ValidationMode mode) {
            validationMode = mode;
            assertThrowsException(InvalidJsonException.class, null);
        }

        @ParameterizedTest(name = "Mode: {0}")
        @MethodSource("org.apache.fineract.portfolio.account.data.StandingInstructionDataValidatorTest#validationModes")
        void shouldFailWhenRequestContainsUnknownParameter(ValidationMode mode) {
            validationMode = mode;

            final JsonObject json = validationMode == ValidationMode.CREATE ? createAccountTransferRequest()
                    : commonValuesInUpdateRequest();

            json.addProperty(invalidParamName, invalidValue);

            assertThrowsException(UnsupportedParameterException.class, json);
        }
    }

    @Nested
    class WhenCreatingStandingInstruction {

        @BeforeEach
        public void setUpCreateMode() {
            validationMode = ValidationMode.CREATE;
        }

        @Nested
        class BaseRules {

            @Test
            void shouldValidateAccountTransferDetails() {
                final JsonObject json = createAccountTransferRequest();
                standingInstructionDataValidator.validateForCreate(command(json));

                verify(accountTransfersDetailDataValidator, times(1)).validate(any(JsonCommand.class), any(DataValidatorBuilder.class));
            }

            @ParameterizedTest
            @MethodSource("requiredBaseParameters")
            void shouldFailWhenRequiredParameterIsMissing(String parameter) {
                final JsonObject json = createAccountTransferRequest();
                json.remove(parameter);

                assertBlank(json, parameter);
            }

            @ParameterizedTest
            @MethodSource("parametersWithInvalidValues")
            void shouldFailWhenParameterHasInvalidValue(String parameter, Integer invalidValue) {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(parameter, invalidValue);

                assertRange(json, parameter);
            }

            @Test
            void shouldFailWhenValidTillIsBeforeValidFrom() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(validTillParamName, "15 May 2026");

                assertValidation(json, validTillParamName, DATE_IS_BEFORE_ERROR_CODE);
            }

            private static Stream<String> requiredBaseParameters() {
                return Stream.of(transferTypeParamName, nameParamName, priorityParamName, instructionTypeParamName, statusParamName,
                        validFromParamName, recurrenceTypeParamName);
            }

            private static Stream<Arguments> parametersWithInvalidValues() {
                return Stream.of(Arguments.of(transferTypeParamName, 4), Arguments.of(priorityParamName, 5),
                        Arguments.of(instructionTypeParamName, 3), Arguments.of(statusParamName, 3),
                        Arguments.of(recurrenceTypeParamName, 3));
            }
        }

        @Nested
        class PeriodicRecurrenceRules {

            @ParameterizedTest
            @MethodSource("requiredParameters")
            void shouldFailWhenPeriodicFieldIsMissing(String parameter) {
                final JsonObject json = createAccountTransferRequest();
                json.remove(parameter);

                assertBlank(json, parameter);
            }

            @Test
            void shouldFailWhenRecurrenceFrequencyHasInvalidValue() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(recurrenceFrequencyParamName, 4);

                assertRange(json, recurrenceFrequencyParamName);
            }

            @Test
            void shouldFailWhenRecurrenceIntervalHasInvalidValue() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(recurrenceIntervalParamName, 0);

                assertValidation(json, recurrenceIntervalParamName, NOT_GREATER_THAN_ZERO_ERROR_CODE);
            }

            @Test
            void shouldFailWhenRecurrenceOnMonthDayHasInvalidValue() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(recurrenceOnMonthDayParamName, "08 Mayo");

                assertValidation(json, recurrenceOnMonthDayParamName, StandingInstructionApiConstants.INVALID_MONTH_DAY_FORMAT_ERROR_CODE);
            }

            @ParameterizedTest
            @MethodSource("invalidExecutionDates")
            void shouldFailWhenValidTillIsBeforeFirstExecution(String validTill, Integer recurrenceFrequency, Integer recurrenceInterval,
                    String recurrenceOnMonthDay) {
                final JsonObject json = createAccountTransferRequest();

                json.addProperty(validTillParamName, validTill);
                json.addProperty(recurrenceFrequencyParamName, recurrenceFrequency);
                json.addProperty(recurrenceIntervalParamName, recurrenceInterval);

                if (recurrenceOnMonthDay != null) {
                    json.addProperty(recurrenceOnMonthDayParamName, recurrenceOnMonthDay);
                }

                assertValidation(json, validTillParamName, StandingInstructionApiConstants.BEFORE_FIRST_EXECUTION_DATE_ERROR_CODE);
            }

            private static Stream<String> requiredParameters() {
                return Stream.of(recurrenceFrequencyParamName, recurrenceIntervalParamName, monthDayFormatParamName,
                        recurrenceOnMonthDayParamName);
            }

            private static Stream<Arguments> invalidExecutionDates() {
                return Stream.of(Arguments.of("20 May 2026", 0, 5, null), Arguments.of("29 May 2026", 1, 2, null),
                        Arguments.of("17 May 2026", 2, 1, "18 May"));
            }

        }

        @Nested
        class AmountRules {

            @Test
            void shouldFailWhenAmountIsMissing() {
                final JsonObject json = createAccountTransferRequest();
                json.remove(amountParamName);

                assertBlank(json, amountParamName);
            }

            @Test
            void shouldFailWhenAmountValueIsNotPositive() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(amountParamName, BigDecimal.valueOf(-10.00));

                assertValidation(json, amountParamName, NOT_GREATER_THAN_ZERO_ERROR_CODE);
            }
        }

        @Nested
        class AccountTransferRules {

            @Test
            void shouldFailWithEqualAccountsAndEqualOffices() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(toAccountIdParamName, 1);

                assertValidation(json, toAccountIdParamName, StandingInstructionApiConstants.CANNOT_TRANSFER_TO_SAME_ACCOUNT_ERROR_CODE);
            }

            @Test
            void shouldFailWhenInstructionTypeIsDues() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(instructionTypeParamName, 2);

                assertValidation(json, instructionTypeParamName,
                        StandingInstructionApiConstants.INSTRUCTION_TYPE_DUES_NOT_ALLOWED_FOR_ACCOUNT_TRANSFER_ERROR_CODE);
            }

            @Test
            void shouldFailWhenRecurrenceTypeIsAsPerDues() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(recurrenceTypeParamName, 2);

                assertValidation(json, recurrenceTypeParamName,
                        StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_FOR_SAVINGS_ERROR_CODE);
            }

            @Test
            void shouldFailWhenAccountTransferInvolvesLoanAccount() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(fromAccountTypeParamName, 1);

                assertValidation(json, transferTypeParamName,
                        StandingInstructionApiConstants.ACCOUNT_TRANSFER_NOT_ALLOWED_FOR_LOAN_ERROR_CODE);
            }

            @Test
            void shouldPassWithDailyPeriodicRecurrence() {
                final JsonObject json = createAccountTransferRequest();

                json.remove(recurrenceOnMonthDayParamName);
                json.remove(monthDayFormatParamName);

                json.addProperty(recurrenceFrequencyParamName, 0);

                assertValidationSuccess(json);
            }

            @Test
            void shouldPassWithYearlyPeriodicRecurrence() {
                final JsonObject json = createAccountTransferRequest();
                json.addProperty(recurrenceFrequencyParamName, 3);

                assertValidationSuccess(json);
            }
        }

        @Nested
        class LoanRepaymentRules {

            @Test
            void shouldFailWhenInstructionTypeIsFixedAndRecurrenceTypeIsAsPerDues() {
                final JsonObject json = createLoanRepaymentRequest();
                json.addProperty(instructionTypeParamName, 1);

                assertValidation(json, recurrenceTypeParamName,
                        StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_WITH_FIXED_INSTRUCTION_ERROR_CODE);
            }

            @Test
            void shouldFailWhenInstructionTypeIsDuesAndAmountIsNotNull() {
                final JsonObject json = createLoanRepaymentRequest();
                json.addProperty(amountParamName, BigDecimal.TEN);

                assertValidation(json, amountParamName, StandingInstructionApiConstants.AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE);
            }

            @Test
            void shouldFailWhenIsNotAValidLoanRepayment() {
                final JsonObject json = createLoanRepaymentRequest();
                json.addProperty(toAccountTypeParamName, 2);

                assertValidation(json, transferTypeParamName, StandingInstructionApiConstants.NOT_A_VALID_LOAN_REPAYMENT_ERROR_CODE);
            }

            @Test
            void shouldPassWithFixedAmountAndPeriodicRecurrence() {
                final JsonObject json = createLoanRepaymentRequest();
                json.addProperty(instructionTypeParamName, 1);
                json.addProperty(amountParamName, BigDecimal.TEN);
                json.addProperty(recurrenceTypeParamName, 1);
                json.addProperty(recurrenceFrequencyParamName, 2);
                json.addProperty(recurrenceIntervalParamName, 1);
                json.addProperty(recurrenceOnMonthDayParamName, "15 May");
                json.addProperty(monthDayFormatParamName, "dd MMMM");

                assertValidationSuccess(json);
            }

            @Test
            void shouldPassWithPeriodicRecurrence() {
                final JsonObject json = createLoanRepaymentRequest();
                json.addProperty(recurrenceTypeParamName, 1);
                json.addProperty(recurrenceFrequencyParamName, 1);
                json.addProperty(recurrenceIntervalParamName, 20);

                assertValidationSuccess(json);
            }

            @Test
            void shouldPassWithTraditionalData() {
                assertValidationSuccess(createLoanRepaymentRequest());
            }
        }
    }

    @Nested
    class WhenUpdatingStandingInstruction {

        @BeforeEach
        public void setUpUpdateMode() {
            validationMode = ValidationMode.UPDATE;
        }

        @Nested
        class AccountTransfer {

            @BeforeEach
            public void setUp() {
                Mockito.lenient().when(existingStandingInstruction.getAccountTransferDetails()).thenReturn(accountTransferDetails);
                Mockito.lenient().when(accountTransferDetails.getTransferType()).thenReturn(1);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceFrequency()).thenReturn(null);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceInterval()).thenReturn(null);
            }

            @ParameterizedTest
            @MethodSource("nullParameters")
            void shouldFailWhenParameterExistsButIsNull(String parameter) {
                final JsonObject json = commonValuesInUpdateRequest();
                json.add(parameter, JsonNull.INSTANCE);

                assertBlank(json, parameter);
            }

            @ParameterizedTest
            @MethodSource("parametersWithInvalidValues")
            void shouldFailWhenParameterHasInvalidValue(String parameter, Integer invalidValue) {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(parameter, invalidValue);

                assertRange(json, parameter);
            }

            @Test
            void shouldFailWhenNewInstructionTypeIsDues() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(instructionTypeParamName, 2);

                assertValidation(json, instructionTypeParamName,
                        StandingInstructionApiConstants.INSTRUCTION_TYPE_DUES_NOT_ALLOWED_FOR_ACCOUNT_TRANSFER_ERROR_CODE);
            }

            @Test
            void shouldFailWhenNewValidFromIsAfterExistingValidTill() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(validFromParamName, "16 May 2026");

                Mockito.lenient().when(existingStandingInstruction.getValidTill()).thenReturn(LocalDate.of(2026, 5, 15));
                assertValidation(json, validFromParamName, StandingInstructionApiConstants.MUST_BE_BEFORE_EXISTING_VALID_TILL_ERROR_CODE);
            }

            @Test
            void shouldFailWhenNewValidTillIsBeforeExistingValidFrom() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(validTillParamName, "16 May 2026");

                Mockito.lenient().when(existingStandingInstruction.getValidFrom()).thenReturn(LocalDate.of(2026, 5, 17));
                assertValidation(json, validTillParamName, DATE_IS_BEFORE_ERROR_CODE);
            }

            @Test
            void shouldFailWhenNewValidTillIsBeforeLastRunDate() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(validTillParamName, "16 May 2026");

                Mockito.lenient().when(existingStandingInstruction.getLastRunDate()).thenReturn(LocalDate.of(2026, 5, 17));
                assertValidation(json, validTillParamName, StandingInstructionApiConstants.CANNOT_BE_BEFORE_LAST_RUN_DATE_ERROR_CODE);
            }

            @Test
            void shouldFailWhenNewRecurrenceTypeIsAsPerDues() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(recurrenceTypeParamName, 2);

                assertValidation(json, recurrenceTypeParamName,
                        StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_FOR_SAVINGS_ERROR_CODE);
            }

            @Test
            void shouldFailWhenNewAmountIsNotPositive() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(amountParamName, BigDecimal.valueOf(-10.00));

                Mockito.lenient().when(existingStandingInstruction.getInstructionType()).thenReturn(1);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceType()).thenReturn(1);
                assertValidation(json, amountParamName, NOT_GREATER_THAN_ZERO_ERROR_CODE);
            }

            @Test
            void shouldPassWhenUpdatingFromDailyToYearlyRecurrence() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(recurrenceFrequencyParamName, 3);
                json.addProperty(recurrenceIntervalParamName, 1);
                json.addProperty(recurrenceOnMonthDayParamName, "25 May");
                json.addProperty(monthDayFormatParamName, "dd MMMM");

                Mockito.lenient().when(existingStandingInstruction.getRecurrenceFrequency()).thenReturn(0);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceInterval()).thenReturn(1);

                Mockito.lenient().when(existingStandingInstruction.getRecurrenceOnMonth()).thenReturn(null);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceOnDay()).thenReturn(null);

                assertValidationSuccess(json);
            }

            private static Stream<String> nullParameters() {
                return Stream.of(nameParamName, priorityParamName, instructionTypeParamName, statusParamName, validFromParamName,
                        validTillParamName, recurrenceTypeParamName);
            }

            private static Stream<Arguments> parametersWithInvalidValues() {
                return Stream.of(Arguments.of(priorityParamName, 5), Arguments.of(instructionTypeParamName, 3),
                        Arguments.of(statusParamName, 3), Arguments.of(recurrenceTypeParamName, 3));
            }
        }

        class LoanRepayment {

            @BeforeEach
            public void setUp() {
                Mockito.lenient().when(existingStandingInstruction.getAccountTransferDetails()).thenReturn(accountTransferDetails);

                Mockito.lenient().when(accountTransferDetails.getTransferType()).thenReturn(2);
            }

            @Test
            void shouldFailWhenNewRecurrenceTypeIsAsPerDues() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(recurrenceTypeParamName, 2);

                Mockito.lenient().when(existingStandingInstruction.getInstructionType()).thenReturn(1);
                assertValidation(json, recurrenceTypeParamName,
                        StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_WITH_FIXED_INSTRUCTION_ERROR_CODE);
            }

            @Test
            void shouldFailWhenInstructionTypeIsDuesAndNewAmountIsNotNull() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.add(amountParamName, JsonNull.INSTANCE);

                Mockito.lenient().when(existingStandingInstruction.getInstructionType()).thenReturn(2);
                assertValidation(json, amountParamName, StandingInstructionApiConstants.AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE);
            }

            @Test
            void shouldPassWhenUpdatingFromMonthlyToWeeklyRecurrence() {
                final JsonObject json = commonValuesInUpdateRequest();
                json.addProperty(recurrenceFrequencyParamName, 1);
                json.addProperty(recurrenceIntervalParamName, 1);

                Mockito.lenient().when(existingStandingInstruction.getRecurrenceFrequency()).thenReturn(2);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceInterval()).thenReturn(1);

                Mockito.lenient().when(existingStandingInstruction.getRecurrenceOnMonth()).thenReturn(5);
                Mockito.lenient().when(existingStandingInstruction.getRecurrenceOnDay()).thenReturn(15);

                Mockito.lenient().when(existingStandingInstruction.getAccountTransferDetails()).thenReturn(accountTransferDetails);
                Mockito.lenient().when(accountTransferDetails.getTransferType()).thenReturn(2);

                assertValidationSuccess(json);
            }
        }
    }

    private JsonCommand command(JsonObject json) {
        final String j = json == null ? "" : json.toString();
        final JsonElement element = fromApiJsonHelper.parse(j);

        return JsonCommand.from(j, element, fromApiJsonHelper, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null);
    }

    private JsonObject commonValuesInCreateRequest() {
        JsonObject json = new JsonObject();
        json.addProperty(localeParamName, "en");
        json.addProperty(dateFormatParamName, "dd MMMM yyyy");
        json.addProperty(fromOfficeIdParamName, 1);
        json.addProperty(fromClientIdParamName, 1);
        json.addProperty(fromAccountIdParamName, 1);
        json.addProperty(fromAccountTypeParamName, 2);
        json.addProperty(toOfficeIdParamName, 1);
        json.addProperty(toClientIdParamName, 1);
        json.addProperty(priorityParamName, 1);
        json.addProperty(statusParamName, 1);
        json.addProperty(validFromParamName, "16 May 2026");
        json.addProperty(validTillParamName, "16 May 2027");

        return json;
    }

    private JsonObject createAccountTransferRequest() {
        JsonObject json = commonValuesInCreateRequest();
        json.addProperty(toAccountIdParamName, 2);
        json.addProperty(toAccountTypeParamName, 2);
        json.addProperty(transferTypeParamName, 1);
        json.addProperty(nameParamName, "BASIC ACCOUNT TRANSFER");
        json.addProperty(instructionTypeParamName, 1);
        json.addProperty(recurrenceTypeParamName, 1);
        json.addProperty(amountParamName, BigDecimal.TEN);
        json.addProperty(recurrenceFrequencyParamName, 2);
        json.addProperty(recurrenceIntervalParamName, 1);
        json.addProperty(recurrenceOnMonthDayParamName, "15 May");
        json.addProperty(monthDayFormatParamName, "dd MMMM");

        return json;
    }

    private JsonObject createLoanRepaymentRequest() {
        JsonObject json = commonValuesInCreateRequest();
        json.addProperty(toAccountIdParamName, 1);
        json.addProperty(toAccountTypeParamName, 1);
        json.addProperty(transferTypeParamName, 2);
        json.addProperty(nameParamName, "BASIC LOAN REPAYMENT");
        json.addProperty(instructionTypeParamName, 2);
        json.addProperty(recurrenceTypeParamName, 2);

        return json;
    }

    private JsonObject commonValuesInUpdateRequest() {
        JsonObject json = new JsonObject();
        json.addProperty(localeParamName, "en");
        json.addProperty(dateFormatParamName, "dd MMMM yyyy");

        return json;
    }

    private void validate(final JsonObject json) {
        if (this.validationMode == ValidationMode.CREATE) {
            this.standingInstructionDataValidator.validateForCreate(command(json));
        } else {
            this.standingInstructionDataValidator.validateForUpdate(command(json), existingStandingInstruction);
        }
    }

    private void assertThrowsException(Class<? extends Throwable> exceptionClass, JsonObject json) {
        assertThrows(exceptionClass, () -> validate(json));
    }

    private void assertValidation(final JsonObject json, final String parameter, final String reason) {
        final String expectedCode = String.join(MSG_SEPARATOR, VALIDATION_MSG_PREFIX,
                StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME, parameter, reason);

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class, () -> validate(json));

        boolean hasError = ex.getErrors().stream().anyMatch(
                error -> parameter.equals(error.getParameterName()) && expectedCode.equals(error.getUserMessageGlobalisationCode()));

        assertTrue(hasError);
    }

    private void assertBlank(final JsonObject json, final String parameter) {
        assertValidation(json, parameter, CANNOT_BE_BLANK_ERROR_CODE);
    }

    private void assertRange(final JsonObject json, final String parameter) {
        assertValidation(json, parameter, OUT_OF_RANGE_ERROR_CODE);
    }

    private void assertValidationSuccess(JsonObject json) {
        assertDoesNotThrow(() -> validate(json));
    }

    public enum ValidationMode {
        CREATE, UPDATE
    }

    private static Stream<ValidationMode> validationModes() {
        return Stream.of(ValidationMode.CREATE, ValidationMode.UPDATE);
    }
}
