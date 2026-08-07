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
import static org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME;
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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.validator.StandingInstructionHelper;
import org.apache.fineract.portfolio.account.data.StandingInstructionValidatorFactory;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferStandingInstruction;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StandingInstructionDataValidator {
    private final StandingInstructionHelper standingInstructionHelper;
    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;
    private static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, fromOfficeIdParamName, fromClientIdParamName, fromAccountTypeParamName,
                    fromAccountIdParamName, toOfficeIdParamName, toClientIdParamName, toAccountTypeParamName, toAccountIdParamName,
                    transferTypeParamName, priorityParamName, instructionTypeParamName, statusParamName, amountParamName,
                    validFromParamName, validTillParamName, recurrenceTypeParamName, recurrenceFrequencyParamName,
                    recurrenceIntervalParamName, recurrenceOnMonthDayParamName, nameParamName, monthDayFormatParamName));

    private static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(localeParamName, dateFormatParamName, nameParamName, priorityParamName, instructionTypeParamName, statusParamName,
                    amountParamName, validFromParamName, validTillParamName, recurrenceTypeParamName, recurrenceFrequencyParamName,
                    recurrenceIntervalParamName, recurrenceOnMonthDayParamName, monthDayFormatParamName));

    @Autowired
    public StandingInstructionDataValidator(final StandingInstructionHelper standingInstructionHelper, 
            final FromJsonHelper fromApiJsonHelper,
            final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator,
            final StandingInstructionValidatorFactory standingInstructionValidatorFactory) {
        this.standingInstructionHelper = standingInstructionHelper;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.accountTransfersDetailDataValidator = accountTransfersDetailDataValidator;
        this.standingInstructionValidatorFactory = standingInstructionValidatorFactory;
    }

    public void validateForCreate(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        command.checkForUnsupportedParameters(typeOfMap, json, CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);
        
        StandingInstruction instruction = this.standingInstructionHelper.extractStandingInstruction(command);
        
        AccountTransferDetails details = instruction.getAccountTransferDetails();
        this.accountTransfersDetailDataValidator.validate(this.standingInstructionHelper, details, baseDataValidator);

        StandingInstructionValidator validator = this.standingInstructionValidatorFactory.getValidator(instruction, baseDataValidator);
        validator.validate();
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final JsonCommand command, final AccountTransferStandingInstruction existingStandingInstruction) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        if (this.fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(priorityParamName, element)) {
            final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(priorityParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);
        }

        final Integer existingTransferType = existingStandingInstruction.getAccountTransferDetails().getTransferType();

        Integer instructionType = existingStandingInstruction.getInstructionType();
        if (this.fromApiJsonHelper.parameterExists(instructionTypeParamName, element)) {
            instructionType = this.fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(instructionTypeParamName).value(instructionType).notNull().inMinMaxRange(1, 2);
            if (isAccountTransfer(existingTransferType) && isDuesInstruction(instructionType)) {
                baseDataValidator.reset().parameter(instructionTypeParamName)
                        .failWithCode(StandingInstructionApiConstants.INSTRUCTION_TYPE_DUES_NOT_ALLOWED_FOR_ACCOUNT_TRANSFER_ERROR_CODE);
            }
        }

        if (this.fromApiJsonHelper.parameterExists(statusParamName, element)) {
            final Integer status = this.fromApiJsonHelper.extractIntegerNamed(statusParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(statusParamName).value(status).notNull().inMinMaxRange(1, 2);
        }

        LocalDate validFrom = existingStandingInstruction.getValidFrom();
        if (this.fromApiJsonHelper.parameterExists(validFromParamName, element)) {
            validFrom = this.fromApiJsonHelper.extractLocalDateNamed(validFromParamName, element);
            baseDataValidator.reset().parameter(validFromParamName).value(validFrom).notNull();
            LocalDate existingValidTill = existingStandingInstruction.getValidTill();
            if (validFrom != null && existingValidTill != null && validFrom.isAfter(existingValidTill)
                    && !this.fromApiJsonHelper.parameterExists(validTillParamName, element)) {
                baseDataValidator.reset().parameter(validFromParamName)
                        .failWithCode(StandingInstructionApiConstants.MUST_BE_BEFORE_EXISTING_VALID_TILL_ERROR_CODE);
            }
        }

        LocalDate validTill = existingStandingInstruction.getValidTill();
        if (this.fromApiJsonHelper.parameterExists(validTillParamName, element)) {
            validTill = this.fromApiJsonHelper.extractLocalDateNamed(validTillParamName, element);
            baseDataValidator.reset().parameter(validTillParamName).value(validTill).notNull();
            if (areNotNullDates(validFrom, validTill)) {
                baseDataValidator.reset().parameter(validTillParamName).value(validTill).validateDateAfter(validFrom);
            }
            if (areNotNullDates(existingStandingInstruction.getLastRunDate(), validTill)
                    && validTill.isBefore(existingStandingInstruction.getLastRunDate())) {
                baseDataValidator.reset().parameter(validTillParamName).value(validTill)
                        .failWithCode(StandingInstructionApiConstants.CANNOT_BE_BEFORE_LAST_RUN_DATE_ERROR_CODE);
            }
        }

        Integer recurrenceType = existingStandingInstruction.getRecurrenceType();
        if (this.fromApiJsonHelper.parameterExists(recurrenceTypeParamName, element)) {
            recurrenceType = this.fromApiJsonHelper.extractIntegerNamed(recurrenceTypeParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2);
            if (isAccountTransfer(existingTransferType) && isAsPerDuesRecurrence(recurrenceType)) {
                baseDataValidator.reset().parameter(recurrenceTypeParamName)
                        .failWithCode(StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_FOR_SAVINGS_ERROR_CODE);
            }
            if (isLoanRepayment(existingTransferType) && isFixedInstruction(instructionType) && isAsPerDuesRecurrence(recurrenceType)) {
                baseDataValidator.reset().parameter(recurrenceTypeParamName)
                        .failWithCode(StandingInstructionApiConstants.RECURRENCE_AS_PER_DUES_NOT_ALLOWED_WITH_FIXED_INSTRUCTION_ERROR_CODE);
            }
        }

        BigDecimal amount = null;
        if (this.fromApiJsonHelper.parameterExists(amountParamName, element)) {
            amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
        }

        if (isDuesInstruction(instructionType)) {
            if (amount != null) {
                baseDataValidator.reset().parameter(amountParamName)
                        .failWithCode(StandingInstructionApiConstants.AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE);
            }
        } else if (isFixedInstruction(instructionType) && isPeriodicRecurrence(recurrenceType)) {
            BigDecimal finalAmount = (amount != null) ? amount : existingStandingInstruction.getAmount();
            baseDataValidator.reset().parameter(amountParamName).value(finalAmount).positiveAmount();
        }

        Integer recurrenceFrequency = existingStandingInstruction.getRecurrenceFrequency();
        if (this.fromApiJsonHelper.parameterExists(recurrenceFrequencyParamName, element)) {
            recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(recurrenceFrequencyParamName, element, Locale.getDefault());
        }

        Integer recurrenceInterval = existingStandingInstruction.getRecurrenceInterval();
        if (this.fromApiJsonHelper.parameterExists(recurrenceIntervalParamName, element)) {
            recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(recurrenceIntervalParamName, element, Locale.getDefault());
        }

        if (isPeriodicRecurrence(recurrenceType)) {
            baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).notNull().inMinMaxRange(0, 3);
            baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).notNull().integerGreaterThanZero();
        }

        MonthDay monthDay = null;
        Integer existingMonth = existingStandingInstruction.getRecurrenceOnMonth();
        Integer existingDay = existingStandingInstruction.getRecurrenceOnDay();

        if (existingMonth != null && existingMonth > 0 && existingDay != null && existingDay > 0) {
            try {
                monthDay = MonthDay.of(existingMonth, existingDay);
            } catch (DateTimeException e) {
                monthDay = null;
            }
        }

        if (isPeriodicRecurrence(recurrenceType) && isValidFrequencyData(recurrenceFrequency, recurrenceInterval)
                && isMonthlyOrYearlyFrequency(recurrenceFrequency)) {
            boolean hasMonthDay = this.fromApiJsonHelper.parameterExists(monthDayFormatParamName, element);
            boolean hasMonthDayFormat = this.fromApiJsonHelper.parameterExists(recurrenceOnMonthDayParamName, element);
            if (hasMonthDay || hasMonthDayFormat) {
                String monthDayFormat = this.fromApiJsonHelper.extractStringNamed(monthDayFormatParamName, element);
                baseDataValidator.reset().parameter(monthDayFormatParamName).value(monthDayFormat).notBlank();

                String monthDayStr = this.fromApiJsonHelper.extractStringNamed(recurrenceOnMonthDayParamName, element);
                baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(monthDayStr).notBlank();

                if (areNotBlankMonthDayAndMonthDayFormat(monthDayStr, monthDayFormat)) {
                    try {
                        monthDay = this.fromApiJsonHelper.extractMonthDayNamed(recurrenceOnMonthDayParamName, element);
                    } catch (Exception e) {
                        baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName)
                                .failWithCode(StandingInstructionApiConstants.INVALID_MONTH_DAY_FORMAT_ERROR_CODE);
                    }
                }
            }
        }

        if (isPeriodicRecurrence(recurrenceType) && isValidFrequencyData(recurrenceFrequency, recurrenceInterval)
                && areNotNullDates(validFrom, validTill)) {
            LocalDate firstExecutionDate = getFirstExecutionDate(recurrenceFrequency, validFrom, recurrenceInterval, monthDay);
            if (firstExecutionDate != null && !validTill.isBefore(validFrom) && validTill.isBefore(firstExecutionDate)) {
                baseDataValidator.reset().parameter(validTillParamName).value(validTill)
                        .failWithCode(StandingInstructionApiConstants.BEFORE_FIRST_EXECUTION_DATE_ERROR_CODE);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private boolean isLoanAccount(final Integer accountType) {
        return PortfolioAccountType.LOAN.equals(PortfolioAccountType.fromInt(accountType));
    }

    private boolean isSavingsAccount(final Integer accountType) {
        return PortfolioAccountType.SAVINGS.equals(PortfolioAccountType.fromInt(accountType));
    }

    private boolean areEqualOfficesAndEqualAccounts(final Long fromOfficeId, final Long toOfficeId, final Long fromAccountId,
            final Long toAccountId) {
        return fromOfficeId != null && toOfficeId != null && fromOfficeId.equals(toOfficeId) && fromAccountId != null && toAccountId != null
                && fromAccountId.equals(toAccountId);
    }

    private boolean isAccountTransfer(final Integer transferType) {
        return transferType != null && AccountTransferType.fromInt(transferType).isAccountTransfer();
    }

    private boolean isLoanRepayment(final Integer transferType) {
        return transferType != null && AccountTransferType.fromInt(transferType).isLoanRepayment();
    }

    private boolean areNotNullDates(LocalDate validFrom, LocalDate validTill) {
        return validFrom != null && validTill != null;
    }

    private boolean isFixedInstruction(final Integer instructionType) {
        return instructionType != null && StandingInstructionType.fromInt(instructionType).isFixedAmoutTransfer();
    }

    private boolean isDuesInstruction(final Integer instructionType) {
        return instructionType != null && StandingInstructionType.fromInt(instructionType).isDuesAmoutTransfer();
    }

    private boolean isPeriodicRecurrence(final Integer recurrenceType) {
        return recurrenceType != null && AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence();
    }

    private boolean isAsPerDuesRecurrence(final Integer recurrenceType) {
        return recurrenceType != null && AccountTransferRecurrenceType.fromInt(recurrenceType).isDuesRecurrence();
    }

    private boolean isValidFrequencyData(final Integer recurrenceFrequency, final Integer recurrenceInterval) {
        return recurrenceFrequency != null && recurrenceInterval != null;
    }

    private boolean isMonthlyOrYearlyFrequency(final Integer recurrenceFrequency) {
        PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
        return frequencyType.isMonthly() || frequencyType.isYearly();
    }

    private boolean areNotBlankMonthDayAndMonthDayFormat(final String monthDay, final String monthDayFormat) {
        return StringUtils.isNotBlank(monthDay) && StringUtils.isNotBlank(monthDayFormat);
    }

    private LocalDate getFirstExecutionDate(final Integer recurrenceFrequency, final LocalDate validFrom, final Integer recurrenceInterval,
            MonthDay monthDay) {
        PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
        LocalDate date = null;
        if (frequencyType.isDaily()) {
            date = validFrom.plusDays(recurrenceInterval);
        } else if (frequencyType.isWeekly()) {
            date = validFrom.plusWeeks(recurrenceInterval);
        } else if (monthDay != null) {
            date = monthDay.atYear(validFrom.getYear());
            if (!date.isAfter(validFrom)) {
                date = frequencyType.isMonthly() ? date.plusMonths(recurrenceInterval) : date.plusYears(recurrenceInterval);
            }
        }
        return date;
    }
}
