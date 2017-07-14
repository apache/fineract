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
package org.apache.fineract.infrastructure.accountnumberformat.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AccountNumberFormatDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> ACCOUNT_NUMBER_FORMAT_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            AccountNumberFormatConstants.accountTypeParamName, AccountNumberFormatConstants.prefixTypeParamName));

	private static final Set<String> ACCOUNT_NUMBER_FORMAT_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(AccountNumberFormatConstants.prefixTypeParamName));

    @Autowired
    public AccountNumberFormatDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				ACCOUNT_NUMBER_FORMAT_CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AccountNumberFormatConstants.ENTITY_NAME);

        final Integer accountType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountNumberFormatConstants.accountTypeParamName,
                element);
        baseDataValidator.reset().parameter(AccountNumberFormatConstants.accountTypeParamName).value(accountType).notNull()
                .integerGreaterThanZero().inMinMaxRange(EntityAccountType.getMinValue(), EntityAccountType.getMaxValue());

        if (this.fromApiJsonHelper.parameterExists(AccountNumberFormatConstants.prefixTypeParamName, element)) {
            final Integer prefixType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    AccountNumberFormatConstants.prefixTypeParamName, element);
            DataValidatorBuilder dataValidatorForValidatingPrefixType = baseDataValidator.reset()
                    .parameter(AccountNumberFormatConstants.prefixTypeParamName).value(prefixType).notNull().integerGreaterThanZero();

            /**
             * Permitted values for prefix type vary based on the actual
             * selected accountType, carry out this validation only if data
             * validation errors do not exist for both entity type and prefix
             * type
             **/
            boolean areAccountTypeAndPrefixTypeValid = true;
            for (ApiParameterError apiParameterError : dataValidationErrors) {
                if (apiParameterError.getParameterName().equalsIgnoreCase(AccountNumberFormatConstants.accountTypeParamName)
                        || apiParameterError.getParameterName().equalsIgnoreCase(AccountNumberFormatConstants.prefixTypeParamName)) {
                    areAccountTypeAndPrefixTypeValid = false;
                }
            }

            if (areAccountTypeAndPrefixTypeValid) {
                EntityAccountType entityAccountType = EntityAccountType.fromInt(accountType);
                Set<Integer> validAccountNumberPrefixes = determineValidAccountNumberPrefixes(entityAccountType);
                dataValidatorForValidatingPrefixType.isOneOfTheseValues(validAccountNumberPrefixes.toArray());
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

	public Set<Integer> determineValidAccountNumberPrefixes(
			EntityAccountType entityAccountType) {
		Set<AccountNumberPrefixType> validAccountNumberPrefixes = new HashSet<>();

		switch (entityAccountType) {
		case CLIENT:
			validAccountNumberPrefixes = AccountNumberFormatEnumerations.accountNumberPrefixesForClientAccounts;
			break;

		case LOAN:
			validAccountNumberPrefixes = AccountNumberFormatEnumerations.accountNumberPrefixesForLoanAccounts;
			break;

		case SAVINGS:
			validAccountNumberPrefixes = AccountNumberFormatEnumerations.accountNumberPrefixesForSavingsAccounts;
			break;

		case CENTER:
			validAccountNumberPrefixes = AccountNumberFormatEnumerations.accountNumberPrefixesForCenters;
			break;
			
		case GROUP:
			validAccountNumberPrefixes = AccountNumberFormatEnumerations.accountNumberPrefixesForGroups;
			break;
		}

        Set<Integer> validAccountNumberPrefixValues = new HashSet<>();
        for (AccountNumberPrefixType validAccountNumberPrefix : validAccountNumberPrefixes) {
            validAccountNumberPrefixValues.add(validAccountNumberPrefix.getValue());
        }
        return validAccountNumberPrefixValues;
    }

    public void validateForUpdate(final String json, EntityAccountType entityAccountType) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				ACCOUNT_NUMBER_FORMAT_UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AccountNumberFormatConstants.ENTITY_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(AccountNumberFormatConstants.prefixTypeParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            Set<Integer> validAccountNumberPrefixes = determineValidAccountNumberPrefixes(entityAccountType);
            final Integer prefixType = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    AccountNumberFormatConstants.prefixTypeParamName, element);
            baseDataValidator.reset().parameter(AccountNumberFormatConstants.prefixTypeParamName).value(prefixType).notNull()
                    .integerGreaterThanZero().isOneOfTheseValues(validAccountNumberPrefixes.toArray());
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
