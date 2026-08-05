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

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public abstract class CommonStandingInstructionValidations implements StandingInstructionValidator {
    protected final FromJsonHelper fromApiJsonHelper;
    protected final JsonElement element;
    protected final DataValidatorBuilder baseDataValidator;

    protected CommonStandingInstructionValidations(final FromJsonHelper fromApiJsonHelper, final JsonElement element, final DataValidatorBuilder baseDataValidator) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.element = element;
        this.baseDataValidator = baseDataValidator;
    }

    @Override
    public final void validate() {
        validateCommonFields();
        validateSpecificFields();
    }

    private void validateCommonFields() {
        final Integer transferType = this.fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(transferTypeParamName).value(transferType).notNull().inMinMaxRange(1, 3);

        final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, this.element);
        this.baseDataValidator.reset().parameter(nameParamName).value(name).notBlank();

        final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(priorityParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);

        final Integer instructionType = this.fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(instructionTypeParamName).value(instructionType).notNull().inMinMaxRange(1, 2);

        final Integer status = this.fromApiJsonHelper.extractIntegerNamed(statusParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(statusParamName).value(status).notNull().inMinMaxRange(1, 2);

        final LocalDate validFrom = this.fromApiJsonHelper.extractLocalDateNamed(validFromParamName, this.element);
        this.baseDataValidator.reset().parameter(validFromParamName).value(validFrom).notNull();

        final LocalDate validTill = this.fromApiJsonHelper.extractLocalDateNamed(validTillParamName, this.element);
        this.baseDataValidator.reset().parameter(validTillParamName).value(validTill).validateDateAfter(validFrom);

        final Integer recurrenceType = this.fromApiJsonHelper.extractIntegerNamed(recurrenceTypeParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2);

        validateAccountTypesAndTransferEligibility(transferType);
    }

    private void validateAccountTypesAndTransferEligibility(final Integer transferType) {
        final Integer fromAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(fromAccountTypeParamName, this.element);
        final Integer toAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(toAccountTypeParamName, this.element);

        if (fromAccountType == null || toAccountType == null) {
            return;
        }

        validateTransferTypeEligibility(transferType, fromAccountType, toAccountType);
        validateSelfAccountTransfer(transferType, fromAccountType, toAccountType);
    }

    private void validateTransferTypeEligibility(final Integer transferType, final Integer fromAccountType, final Integer toAccountType) {
        if (isInvalidAccountTransfer(transferType, fromAccountType, toAccountType)) {
            this.baseDataValidator.reset().parameter(transferTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.ACCOUNT_TRANSFER_NOT_ALLOWED_FOR_LOAN_ERROR_CODE);
        } else if (isInvalidLoanRepayment(transferType, fromAccountType, toAccountType)) {
            this.baseDataValidator.reset().parameter(transferTypeParamName)
                    .failWithCode(StandingInstructionApiConstants.NOT_A_VALID_LOAN_REPAYMENT_ERROR_CODE);
        }
    }

    private boolean isInvalidAccountTransfer(final Integer transferType, final Integer fromAccountType, final Integer toAccountType) {
        return isAccountTransfer(transferType) && (isLoanAccount(fromAccountType) || isLoanAccount(toAccountType));
    }

    private boolean isInvalidLoanRepayment(final Integer transferType, final Integer fromAccountType, final Integer toAccountType) {
        return isLoanRepayment(transferType) && (isLoanAccount(fromAccountType) || isSavingsAccount(toAccountType));
    }

    private void validateSelfAccountTransfer(final Integer transferType, final Integer fromAccountType, final Integer toAccountType) {
        if (!isAccountTransfer(transferType) || !isSavingsAccount(fromAccountType) || !isSavingsAccount(toAccountType)) {
            return;
        }

        final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, this.element);
        final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, this.element);
        final Long fromAccountId = this.fromApiJsonHelper.extractLongNamed(fromAccountIdParamName, this.element);
        final Long toAccountId = this.fromApiJsonHelper.extractLongNamed(toAccountIdParamName, this.element);

        if (areEqualOfficesAndEqualAccounts(fromOfficeId, toOfficeId, fromAccountId, toAccountId)) {
            this.baseDataValidator.reset().parameter(toAccountIdParamName)
                    .failWithCode(StandingInstructionApiConstants.CANNOT_TRANSFER_TO_SAME_ACCOUNT_ERROR_CODE);
        }
    }

    protected abstract void validateSpecificFields();

    protected void validatePeriodicFields() {
        final Integer recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(recurrenceFrequencyParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).notNull().inMinMaxRange(0, 3);

        final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(recurrenceIntervalParamName, this.element, Locale.getDefault());
        this.baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).notNull().integerGreaterThanZero();

        if (!isValidFrequencyData(recurrenceFrequency, recurrenceInterval)) {
            return;
        }

        MonthDay monthDay = null;
        if (isMonthlyOrYearlyFrequency(recurrenceFrequency)) {
            final String monthDayFormat = this.fromApiJsonHelper.extractStringNamed(monthDayFormatParamName, this.element);
            this.baseDataValidator.reset().parameter(monthDayFormatParamName).value(monthDayFormat).notBlank();

            final String monthDayStr = this.fromApiJsonHelper.extractStringNamed(recurrenceOnMonthDayParamName, this.element);
            this.baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(monthDayStr).notBlank();

            monthDay = extractAndValidateMonthDay(monthDayStr, monthDayFormat);
        }

        final LocalDate validFrom = this.fromApiJsonHelper.extractLocalDateNamed(validFromParamName, this.element);
        if (!areValidDates(validFrom)) {
            return;
        }

        final LocalDate firstExecutionDate = getFirstExecutionDate(recurrenceFrequency, validFrom, recurrenceInterval, monthDay);

        final LocalDate validTill = this.fromApiJsonHelper.extractLocalDateNamed(validTillParamName, this.element);
        if (isValidTillBeforeFirstExecution(validFrom, firstExecutionDate, validTill)) {
            this.baseDataValidator.reset().parameter(validTillParamName).value(validTill)
                    .failWithCode(StandingInstructionApiConstants.BEFORE_FIRST_EXECUTION_DATE_ERROR_CODE);
        }
    }

    private boolean isValidFrequencyData(final Integer recurrenceFrequency, final Integer recurrenceInterval) {
        if (recurrenceFrequency == null || recurrenceInterval == null) {
            return false;
        }
        if (recurrenceFrequency < 0 || recurrenceFrequency > 4) {
            return false;
        }
        if (recurrenceInterval < 1) {
            return false;
        }

        return true;
    }

    private boolean isMonthlyOrYearlyFrequency(final Integer recurrenceFrequency) {
        final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
        return frequencyType != null && (frequencyType.isMonthly() || frequencyType.isYearly());
    }

    private MonthDay extractAndValidateMonthDay(final String monthDayStr, final String monthDayFormat) {
        if (StringUtils.isBlank(monthDayStr) || StringUtils.isBlank(monthDayFormat)) {
            return null;
        }

        try {
            return this.fromApiJsonHelper.extractMonthDayNamed(recurrenceOnMonthDayParamName, this.element);
        } catch (Exception e) {
            this.baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName)
                    .failWithCode(StandingInstructionApiConstants.INVALID_MONTH_DAY_FORMAT_ERROR_CODE);
            return null;
        }
    }

    private boolean areValidDates(LocalDate... dates) {
        return Arrays
                .stream(dates)
                .allMatch(Objects::nonNull);
    }

    private boolean isValidTillBeforeFirstExecution(final LocalDate validFrom, final LocalDate firstExecutionDate, final LocalDate validTill) {
        if (!areValidDates(validFrom, firstExecutionDate, validTill)) {
            return false;
        }

        final boolean isValidTillNotBeforeValidFrom = !validTill.isBefore(validFrom);
        final boolean isValidTillBeforeFirstExecution = validTill.isBefore(firstExecutionDate);

        return isValidTillNotBeforeValidFrom && isValidTillBeforeFirstExecution;
    }

    private LocalDate getFirstExecutionDate(final Integer recurrenceFrequency, final LocalDate validFrom, final Integer recurrenceInterval, final MonthDay monthDay) {
        final PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);

        if (frequencyType == null) {
            return null;
        }

        if (frequencyType.isDaily()) {
            return validFrom.plusDays(recurrenceInterval);
        }

        if (frequencyType.isWeekly()) {
            return validFrom.plusWeeks(recurrenceInterval);
        }

        if (monthDay == null) {
            return null;
        }

        LocalDate date = calculateBaseDate(frequencyType, validFrom, monthDay);

        if (areValidDates(date) && !date.isAfter(validFrom)) {
            date = advanceToNextCycle(frequencyType, date);
        }

        return date;
    }

    private LocalDate calculateBaseDate(final PeriodFrequencyType frequencyType, final LocalDate validFrom, final MonthDay monthDay) {
        if (frequencyType.isMonthly()) {
            return LocalDate.of(validFrom.getYear(), validFrom.getMonth(), monthDay.getDayOfMonth());
        }
        if (frequencyType.isYearly()) {
            return monthDay.atYear(validFrom.getYear());
        }
        return null;
    }

    private LocalDate advanceToNextCycle(final PeriodFrequencyType frequencyType, final LocalDate date) {
        if (frequencyType.isMonthly()) {
            return date.plusMonths(1);
        }
        if (frequencyType.isYearly()) {
            return date.plusYears(1);
        }
        return date;
    }

    protected void validateAmountForFixedInstructionType() {
        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, this.element);
        this.baseDataValidator.reset().parameter(amountParamName).value(amount).notNull().positiveAmount();
    }

    protected void validateAmountForDuesInstructionType() {
        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, this.element);

        if (amount != null) {
            this.baseDataValidator.reset().parameter(amountParamName)
                    .failWithCode(StandingInstructionApiConstants.AMOUNT_NOT_ALLOWED_FOR_DUES_ERROR_CODE);
        }
    }

    private boolean isValidInstructionType(final Integer instructionType) {
        return isFixedInstruction(instructionType) || isDuesInstruction(instructionType);
    }
    
    protected boolean isDuesInstruction(final Integer instructionType) {
        return isMatchingType(instructionType, StandingInstructionType::fromInt, StandingInstructionType::isDuesAmoutTransfer);
    }

    protected boolean isFixedInstruction(final Integer instructionType) {
        return isMatchingType(instructionType, StandingInstructionType::fromInt, StandingInstructionType::isFixedAmoutTransfer);
    }

    protected boolean isAsPerDuesRecurrence(final Integer recurrenceType) {
        return isMatchingType(recurrenceType, AccountTransferRecurrenceType::fromInt, AccountTransferRecurrenceType::isDuesRecurrence);
    }

    protected boolean isAccountTransfer(final Integer transferType) {
        return isMatchingType(transferType, AccountTransferType::fromInt, AccountTransferType::isAccountTransfer);
    }

    protected boolean isLoanRepayment(final Integer transferType) {
        return isMatchingType(transferType, AccountTransferType::fromInt, AccountTransferType::isLoanRepayment);
    }

    protected boolean isLoanAccount(final Integer accountType) {
        return isMatchingType(accountType, PortfolioAccountType::fromInt, PortfolioAccountType.LOAN::equals);
    }

    protected boolean isSavingsAccount(final Integer accountType) {
        return isMatchingType(accountType, PortfolioAccountType::fromInt, PortfolioAccountType.SAVINGS::equals);
    }

    private <T> boolean isMatchingType(final Integer codeType, final Function<Integer, T> resolver, final Function<T, Boolean> predicate) {
        return Optional.ofNullable(codeType)
                       .map(resolver)
                       .map(predicate)
                       .orElse(false);
    }

    protected boolean areEqualOfficesAndEqualAccounts(final Long fromOfficeId, final Long toOfficeId, final Long fromAccountId, final Long toAccountId) {
        return Objects.equals(fromOfficeId, toOfficeId) && Objects.equals(fromAccountId, toAccountId);
    }
}