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
package org.apache.fineract.portfolio.self.account.data;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.apache.fineract.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;
import static org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants.transferDescriptionParamName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.self.account.service.SelfAccountTransferReadService;
import org.apache.fineract.portfolio.self.account.service.SelfBeneficiariesTPTReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SelfAccountTransferDataValidator {

	private final PlatformSecurityContext context;
	private final SelfAccountTransferReadService selfAccountTransferReadService;
	private final SelfBeneficiariesTPTReadPlatformService tptBeneficiaryReadPlatformService;
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public SelfAccountTransferDataValidator(
			final PlatformSecurityContext context,
			final SelfAccountTransferReadService selfAccountTransferReadService,
			final SelfBeneficiariesTPTReadPlatformService tptBeneficiaryReadPlatformService,
			final FromJsonHelper fromApiJsonHelper) {
		this.context = context;
		this.selfAccountTransferReadService = selfAccountTransferReadService;
		this.tptBeneficiaryReadPlatformService = tptBeneficiaryReadPlatformService;
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public Map<String,Object> validateCreate(String type, String apiRequestBodyAsJson) {
		if (StringUtils.isBlank(apiRequestBodyAsJson)) {
			throw new InvalidJsonException();
		}

		JsonElement element = this.fromApiJsonHelper
				.parse(apiRequestBodyAsJson);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource(ACCOUNT_TRANSFER_RESOURCE_NAME);

		final Long fromOfficeId = this.fromApiJsonHelper.extractLongNamed(
				fromOfficeIdParamName, element);
		baseDataValidator.reset().parameter(fromOfficeIdParamName)
				.value(fromOfficeId).notNull().integerGreaterThanZero();

		final Long fromClientId = this.fromApiJsonHelper.extractLongNamed(
				fromClientIdParamName, element);
		baseDataValidator.reset().parameter(fromClientIdParamName)
				.value(fromClientId).notNull().integerGreaterThanZero();

		final Long fromAccountId = this.fromApiJsonHelper.extractLongNamed(
				fromAccountIdParamName, element);
		baseDataValidator.reset().parameter(fromAccountIdParamName)
				.value(fromAccountId).notNull().integerGreaterThanZero();

		final Integer fromAccountType = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed(fromAccountTypeParamName,
						element);
		baseDataValidator.reset().parameter(fromAccountTypeParamName)
				.value(fromAccountType).notNull()
				.isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

		final Long toOfficeId = this.fromApiJsonHelper.extractLongNamed(
				toOfficeIdParamName, element);
		baseDataValidator.reset().parameter(toOfficeIdParamName)
				.value(toOfficeId).notNull().integerGreaterThanZero();

		final Long toClientId = this.fromApiJsonHelper.extractLongNamed(
				toClientIdParamName, element);
		baseDataValidator.reset().parameter(toClientIdParamName)
				.value(toClientId).notNull().integerGreaterThanZero();

		final Long toAccountId = this.fromApiJsonHelper.extractLongNamed(
				toAccountIdParamName, element);
		baseDataValidator.reset().parameter(toAccountIdParamName)
				.value(toAccountId).notNull().integerGreaterThanZero();

		final Integer toAccountType = this.fromApiJsonHelper
				.extractIntegerSansLocaleNamed(toAccountTypeParamName, element);
		baseDataValidator.reset().parameter(toAccountTypeParamName)
				.value(toAccountType).notNull()
				.isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

		if (fromAccountType != null && fromAccountType == 1
				&& toAccountType != null && toAccountType == 1) {
			baseDataValidator
					.reset()
					.failWithCode("loan.to.loan.transfer.not.allowed",
							"Cannot transfer from Loan account to another Loan account.");
		}

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transferDateParamName, element);
        baseDataValidator.reset().parameter(transferDateParamName).value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transferAmountParamName, element);
        baseDataValidator.reset().parameter(transferAmountParamName).value(transactionAmount).notNull().positiveAmount();

        final String transactionDescription = this.fromApiJsonHelper.extractStringNamed(transferDescriptionParamName, element);
        baseDataValidator.reset().parameter(transferDescriptionParamName).value(transactionDescription).notBlank()
                .notExceedingLengthOf(200);

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

		SelfAccountTemplateData fromAccount = new SelfAccountTemplateData(
				fromAccountId, fromAccountType, fromClientId, fromOfficeId);
		SelfAccountTemplateData toAccount = new SelfAccountTemplateData(
				toAccountId, toAccountType, toClientId, toOfficeId);

		validateUserAccounts(fromAccount, toAccount, baseDataValidator, type);
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
		Map<String, Object> ret = new HashMap<>();
		ret.put("fromAccount", fromAccount);
		ret.put("toAccount", toAccount);
		ret.put("transactionDate", transactionDate);
		ret.put("transactionAmount", transactionAmount);
		
		return ret;

	}

	private void validateUserAccounts(
			final SelfAccountTemplateData fromAccount,
			final SelfAccountTemplateData toAccount,
			final DataValidatorBuilder baseDataValidator, final String type) {
		AppUser user = this.context.authenticatedUser();
		Collection<SelfAccountTemplateData> validFromAccounts = this.selfAccountTransferReadService
				.retrieveSelfAccountTemplateData(user);

		Collection<SelfAccountTemplateData> validToAccounts = validFromAccounts;
		if (type.equals("tpt")) {
			validToAccounts = this.tptBeneficiaryReadPlatformService
					.retrieveTPTSelfAccountTemplateData(user);
		}

		boolean validFromAccount = false;
		for (SelfAccountTemplateData validAccount : validFromAccounts) {
			if (validAccount.equals(fromAccount)) {
				validFromAccount = true;
				break;
			}
		}

		boolean validToAccount = false;
		for (SelfAccountTemplateData validAccount : validToAccounts) {
			if (validAccount.equals(toAccount)) {
				validToAccount = true;
				break;
			}
		}

		if (!validFromAccount) {
			baseDataValidator
					.reset()
					.failWithCode("invalid.from.account.details",
							"Source account details doesn't match with valid user account details.");
		}

		if (!validToAccount) {
			baseDataValidator
					.reset()
					.failWithCode("invalid.to.account.details",
							"Destination account details doesn't match with valid user account details.");
		}

		if (fromAccount.equals(toAccount)) {
			baseDataValidator.reset().failWithCode(
					"same.from.to.account.details",
					"Source and Destination account details are same.");
		}

	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(dataValidationErrors);
		}
	}
}
