/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.self.account.data;

import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.fromOfficeIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toClientIdParamName;
import static org.mifosplatform.portfolio.account.AccountDetailConstants.toOfficeIdParamName;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.self.account.service.SelfAccountTransferReadService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Component
public class SelfAccountTransferDataValidator {

	private final PlatformSecurityContext context;
	private final SelfAccountTransferReadService selfAccountTransferReadService;
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public SelfAccountTransferDataValidator(
			final PlatformSecurityContext context,
			final SelfAccountTransferReadService selfAccountTransferReadService,
			final FromJsonHelper fromApiJsonHelper) {
		this.context = context;
		this.selfAccountTransferReadService = selfAccountTransferReadService;
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	public void validateCreate(String apiRequestBodyAsJson) {
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

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

		SelfAccountTemplateData fromAccount = new SelfAccountTemplateData(
				fromAccountId, fromAccountType, fromClientId, fromOfficeId);
		SelfAccountTemplateData toAccount = new SelfAccountTemplateData(
				toAccountId, toAccountType, toClientId, toOfficeId);

		validateSelfUserAccounts(fromAccount, toAccount, baseDataValidator);
		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void validateSelfUserAccounts(
			final SelfAccountTemplateData fromAccount,
			final SelfAccountTemplateData toAccount,
			final DataValidatorBuilder baseDataValidator) {
		AppUser user = this.context.authenticatedUser();
		Collection<SelfAccountTemplateData> userValidAccounts = this.selfAccountTransferReadService
				.retrieveSelfAccountTemplateData(user);

		boolean validFromAccount = false;
		for (SelfAccountTemplateData validAccount : userValidAccounts) {
			if (validAccount.equals(fromAccount)) {
				validFromAccount = true;
				break;
			}
		}

		boolean validToAccount = false;
		for (SelfAccountTemplateData validAccount : userValidAccounts) {
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
