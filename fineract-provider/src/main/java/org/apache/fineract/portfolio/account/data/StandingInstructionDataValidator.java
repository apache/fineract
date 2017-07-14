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

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.transferTypeParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.api.StandingInstructionApiConstants;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
    public class StandingInstructionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;
	private static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			AccountDetailConstants.localeParamName, AccountDetailConstants.dateFormatParamName,
			AccountDetailConstants.fromOfficeIdParamName, AccountDetailConstants.fromClientIdParamName,
			AccountDetailConstants.fromAccountTypeParamName, AccountDetailConstants.fromAccountIdParamName,
			AccountDetailConstants.toOfficeIdParamName, AccountDetailConstants.toClientIdParamName,
			AccountDetailConstants.toAccountTypeParamName, AccountDetailConstants.toAccountIdParamName,
			AccountDetailConstants.transferTypeParamName, StandingInstructionApiConstants.priorityParamName,
			StandingInstructionApiConstants.instructionTypeParamName, StandingInstructionApiConstants.statusParamName,
			StandingInstructionApiConstants.amountParamName, StandingInstructionApiConstants.validFromParamName,
			StandingInstructionApiConstants.validTillParamName, StandingInstructionApiConstants.recurrenceTypeParamName,
			StandingInstructionApiConstants.recurrenceFrequencyParamName,
			StandingInstructionApiConstants.recurrenceIntervalParamName,
			StandingInstructionApiConstants.recurrenceOnMonthDayParamName,
			StandingInstructionApiConstants.nameParamName, StandingInstructionApiConstants.monthDayFormatParamName));

	private static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			AccountDetailConstants.localeParamName, AccountDetailConstants.dateFormatParamName,
			StandingInstructionApiConstants.priorityParamName, StandingInstructionApiConstants.instructionTypeParamName,
			StandingInstructionApiConstants.statusParamName, StandingInstructionApiConstants.amountParamName,
			StandingInstructionApiConstants.validFromParamName, StandingInstructionApiConstants.validTillParamName,
			StandingInstructionApiConstants.recurrenceTypeParamName,
			StandingInstructionApiConstants.recurrenceFrequencyParamName,
			StandingInstructionApiConstants.recurrenceIntervalParamName,
			StandingInstructionApiConstants.recurrenceOnMonthDayParamName,
			StandingInstructionApiConstants.monthDayFormatParamName));


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
                .resource(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);
        this.accountTransfersDetailDataValidator.validate(command, baseDataValidator);

        final JsonElement element = command.parsedJson();

		final Integer status = this.fromApiJsonHelper
				.extractIntegerNamed(StandingInstructionApiConstants.statusParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.statusParamName).value(status).notNull()
                .inMinMaxRange(1, 2);

		final LocalDate validFrom = this.fromApiJsonHelper
				.extractLocalDateNamed(StandingInstructionApiConstants.validFromParamName, element);
		baseDataValidator.reset().parameter(StandingInstructionApiConstants.validFromParamName).value(validFrom)
				.notNull();

		final LocalDate validTill = this.fromApiJsonHelper
				.extractLocalDateNamed(StandingInstructionApiConstants.validTillParamName, element);
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.validTillParamName).value(validTill)
                .validateDateAfter(validFrom);

        final BigDecimal transferAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(StandingInstructionApiConstants.amountParamName, element);
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.amountParamName).value(transferAmount).positiveAmount();

        final Integer transferType = this.fromApiJsonHelper.extractIntegerNamed(transferTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(transferTypeParamName).value(transferType).notNull().inMinMaxRange(1, 3);

        final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(StandingInstructionApiConstants.priorityParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.priorityParamName).value(priority).notNull().inMinMaxRange(1, 4);

        final Integer standingInstructionType = this.fromApiJsonHelper.extractIntegerNamed(StandingInstructionApiConstants.instructionTypeParamName, element,
                Locale.getDefault());
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.instructionTypeParamName).value(standingInstructionType).notNull().inMinMaxRange(1, 2);

        final Integer recurrenceType = this.fromApiJsonHelper.extractIntegerNamed(StandingInstructionApiConstants.recurrenceTypeParamName, element, Locale.getDefault());
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 2);
        boolean isPeriodic = false;
        if (recurrenceType != null) {
            isPeriodic = AccountTransferRecurrenceType.fromInt(recurrenceType).isPeriodicRecurrence();
        }

		final Integer recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(
				StandingInstructionApiConstants.recurrenceFrequencyParamName, element,
                Locale.getDefault());
		baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceFrequencyParamName)
				.value(recurrenceFrequency).inMinMaxRange(0, 3);

        if (recurrenceFrequency != null) {
            PeriodFrequencyType frequencyType = PeriodFrequencyType.fromInt(recurrenceFrequency);
            if (frequencyType.isMonthly() || frequencyType.isYearly()) {
				final MonthDay monthDay = this.fromApiJsonHelper
						.extractMonthDayNamed(StandingInstructionApiConstants.recurrenceOnMonthDayParamName, element);
				baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceOnMonthDayParamName)
						.value(monthDay).notNull();
            }
        }

		final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
				StandingInstructionApiConstants.recurrenceIntervalParamName, element,
                Locale.getDefault());
        if (isPeriodic) {
            baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceIntervalParamName).value(recurrenceInterval).notNull();
            baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceFrequencyParamName).value(recurrenceFrequency).notNull();
        }
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceIntervalParamName).value(recurrenceInterval).integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(StandingInstructionApiConstants.nameParamName, element);
        baseDataValidator.reset().parameter(StandingInstructionApiConstants.nameParamName).value(name).notNull();

        final Integer toAccountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(toAccountTypeParamName, element);
        if (toAccountType != null && PortfolioAccountType.fromInt(toAccountType).isSavingsAccount()) {
            baseDataValidator.reset().parameter(StandingInstructionApiConstants.instructionTypeParamName).value(standingInstructionType).notNull().inMinMaxRange(1, 1);
            baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceTypeParamName).value(recurrenceType).notNull().inMinMaxRange(1, 1);

        }
        if (standingInstructionType != null && StandingInstructionType.fromInt(standingInstructionType).isFixedAmoutTransfer()) {
            baseDataValidator.reset().parameter(StandingInstructionApiConstants.amountParamName).value(transferAmount).notNull();
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
                .resource(StandingInstructionApiConstants.STANDING_INSTRUCTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();
        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.validFromParamName, element)) {
			final LocalDate validFrom = this.fromApiJsonHelper
					.extractLocalDateNamed(StandingInstructionApiConstants.validFromParamName, element);
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.validFromParamName).value(validFrom)
					.notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.validTillParamName, element)) {
			final LocalDate validTill = this.fromApiJsonHelper
					.extractLocalDateNamed(StandingInstructionApiConstants.validTillParamName, element);
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.validTillParamName).value(validTill)
					.notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.amountParamName, element)) {
			final BigDecimal transferAmount = this.fromApiJsonHelper
					.extractBigDecimalWithLocaleNamed(StandingInstructionApiConstants.amountParamName, element);
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.amountParamName).value(transferAmount)
					.positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.statusParamName, element)) {
			final Integer status = this.fromApiJsonHelper
					.extractIntegerNamed(StandingInstructionApiConstants.statusParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.statusParamName).value(status).notNull()
					.inMinMaxRange(1, 2);
        }

        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.priorityParamName, element)) {
			final Integer priority = this.fromApiJsonHelper.extractIntegerNamed(
					StandingInstructionApiConstants.priorityParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.priorityParamName).value(priority)
					.notNull().inMinMaxRange(1, 4);
        }

        if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.instructionTypeParamName, element)) {
			final Integer standingInstructionType = this.fromApiJsonHelper.extractIntegerNamed(
					StandingInstructionApiConstants.instructionTypeParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.instructionTypeParamName)
					.value(standingInstructionType).notNull().inMinMaxRange(1, 2);
		}

		if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceTypeParamName, element)) {
			final Integer recurrenceType = this.fromApiJsonHelper.extractIntegerNamed(
					StandingInstructionApiConstants.recurrenceTypeParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceTypeParamName)
					.value(recurrenceType).notNull().inMinMaxRange(1, 2);
		}

		if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceFrequencyParamName,
				element)) {
			final Integer recurrenceFrequency = this.fromApiJsonHelper.extractIntegerNamed(
					StandingInstructionApiConstants.recurrenceFrequencyParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceFrequencyParamName)
					.value(recurrenceFrequency).inMinMaxRange(0, 3);
		}

		if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.recurrenceIntervalParamName,
				element)) {
			final Integer recurrenceInterval = this.fromApiJsonHelper.extractIntegerNamed(
					StandingInstructionApiConstants.recurrenceIntervalParamName, element, Locale.getDefault());
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.recurrenceIntervalParamName)
					.value(recurrenceInterval).integerGreaterThanZero();
		}

		if (this.fromApiJsonHelper.parameterExists(StandingInstructionApiConstants.nameParamName, element)) {
			final String name = this.fromApiJsonHelper.extractStringNamed(StandingInstructionApiConstants.nameParamName,
					element);
			baseDataValidator.reset().parameter(StandingInstructionApiConstants.nameParamName).value(name).notNull();
		}

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}