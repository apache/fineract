/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.CREATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.UPDATE_REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.amountParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.instructionTypeParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.nameParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.priorityParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.recurrenceFrequencyParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.recurrenceIntervalParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.recurrenceOnMonthDayParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.recurrenceTypeParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.statusParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.validFromParamName;
import static org.mifosplatform.portfolio.account.api.StandingInstructionApiConstants.validTillParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.transferTypeParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.account.PortfolioAccountType;
import org.mifosplatform.portfolio.account.domain.AccountTransferRecurrenceType;
import org.mifosplatform.portfolio.account.domain.AccountTransferType;
import org.mifosplatform.portfolio.account.domain.StandingInstructionType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class StandingInstructionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;

    @Autowired
    public StandingInstructionDataValidator(final FromJsonHelper fromApiJsonHelper,
            final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.accountTransfersDetailDataValidator = accountTransfersDetailDataValidator;
    }

    public void validateForCreate(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);
        this.accountTransfersDetailDataValidator.validate(command, baseDataValidator);

        final JsonElement element = command.parsedJson();

        final Integer status = this.fromApiJsonHelper.extractIntegerNamed(statusParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(statusParamName).value(status).notNull().inMinMaxRange(1, 2);

        final LocalDate validFrom = this.fromApiJsonHelper.extractLocalDateNamed(validFromParamName, element);
        baseDataValidator.reset().parameter(validFromParamName).value(validFrom).notNull();

        final LocalDate validTill = this.fromApiJsonHelper.extractLocalDateNamed(validTillParamName, element);
        baseDataValidator.reset().parameter(validTillParamName).value(validTill).validateDateAfter(validFrom);

        final BigDecimal transferAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
        baseDataValidator.reset().parameter(amountParamName).value(transferAmount).positiveAmount();

        final Integer transferType = this.fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(transferTypeParamName).value(transferType).notNull().inMinMaxRange(1, 3);

        final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(priorityParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);

        final Integer standingInstructionType = this.fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(instructionTypeParamName).value(standingInstructionType).notNull().inMinMaxRange(1, 2);

        final Integer recurrenceType = this.fromApiJsonHelper.extractIntegerNamed(recurrenceTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2);
        boolean isPeriodic = false;
        if (recurrenceType != null) {
            isPeriodic = AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence();
        }

        final Integer recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(recurrenceFrequencyParamName, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).inMinMaxRange(0, 3);

        if (recurrenceFrequency != null) {
            PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
            if (frequencyType.isMonthly() || frequencyType.isYearly()) {
                final MonthDay monthDay = this.fromApiJsonHelper.extractMonthDayNamed(recurrenceOnMonthDayParamName, element);
                baseDataValidator.reset().parameter(recurrenceOnMonthDayParamName).value(monthDay).notNull();
            }
        }

        final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(recurrenceIntervalParamName, element,
                Locale.getDefault());
        if (isPeriodic) {
            baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).notNull();
            baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).notNull();
        }
        baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
        baseDataValidator.reset().parameter(nameParamName).value(name).notNull();

        final Integer toAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(toAccountTypeParamName, element);
        if (toAccountType != null && PortfolioAccountType.fromInt(toAccountType).isSavingsAccount()) {
            baseDataValidator.reset().parameter(instructionTypeParamName).value(standingInstructionType).notNull().inMinMaxRange(1, 1);
            baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 1);

        }
        if (standingInstructionType != null && StandingInstructionType.fromInt(standingInstructionType).isFixedAmoutTransfer()) {
            baseDataValidator.reset().parameter(amountParamName).value(transferAmount).notNull();
        }

        String errorCode = null;
        AccountTransferType accountTransferType = AccountTransferType.fromInt(transferType);
        final Integer fromAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(fromAccountTypeParamName, element);
        if (fromAccountType != null && toAccountType != null) {
            PortfolioAccountType fromPortfolioAccountType = PortfolioAccountType.fromInt(fromAccountType);
            PortfolioAccountType toPortfolioAccountType = PortfolioAccountType.fromInt(toAccountType);
            if (accountTransferType.isAccountTransfer()
                    && (fromPortfolioAccountType.isLoanAccount() || toPortfolioAccountType.isLoanAccount())) {
                errorCode = "not.account.transfer";
            } else if (accountTransferType.isLoanRepayment()
                    && (fromPortfolioAccountType.isLoanAccount() || toPortfolioAccountType.isSavingsAccount())) {
                errorCode = "not.loan.repayment";
            }
            if (errorCode != null) {
                baseDataValidator.reset().parameter(transferTypeParamName).failWithCode(errorCode);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(STANDING_INSTRUCTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();
        if (this.fromApiJsonHelper.parameterExists(validFromParamName, element)) {
            final LocalDate validFrom = this.fromApiJsonHelper.extractLocalDateNamed(validFromParamName, element);
            baseDataValidator.reset().parameter(validFromParamName).value(validFrom).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(validTillParamName, element)) {
            final LocalDate validTill = this.fromApiJsonHelper.extractLocalDateNamed(validTillParamName, element);
            baseDataValidator.reset().parameter(validTillParamName).value(validTill).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(amountParamName, element)) {
            final BigDecimal transferAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(amountParamName, element);
            baseDataValidator.reset().parameter(amountParamName).value(transferAmount).positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(statusParamName, element)) {
            final Integer status = this.fromApiJsonHelper.extractIntegerNamed(statusParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(statusParamName).value(status).notNull().inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(priorityParamName, element)) {
            final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(priorityParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);
        }

        if (this.fromApiJsonHelper.parameterExists(instructionTypeParamName, element)) {
            final Integer standingInstructionType = this.fromApiJsonHelper.extractIntegerNamed(instructionTypeParamName, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(instructionTypeParamName).value(standingInstructionType).notNull().inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(recurrenceTypeParamName, element)) {
            final Integer recurrenceType = this.fromApiJsonHelper
                    .extractIntegerNamed(recurrenceTypeParamName, element, Locale.getDefault());
            baseDataValidator.reset().parameter(recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(recurrenceFrequencyParamName, element)) {
            final Integer recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(recurrenceFrequencyParamName, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(recurrenceFrequencyParamName).value(recurrenceFrequency).inMinMaxRange(0, 3);
        }

        if (this.fromApiJsonHelper.parameterExists(recurrenceIntervalParamName, element)) {
            final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(recurrenceIntervalParamName, element,
                    Locale.getDefault());
            baseDataValidator.reset().parameter(recurrenceIntervalParamName).value(recurrenceInterval).integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(nameParamName, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(nameParamName, element);
            baseDataValidator.reset().parameter(nameParamName).value(name).notNull();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}