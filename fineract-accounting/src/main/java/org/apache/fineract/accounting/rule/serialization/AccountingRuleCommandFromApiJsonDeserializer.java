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
package org.apache.fineract.accounting.rule.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.rule.api.AccountingRuleJsonInputParams;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
public class AccountingRuleCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = AccountingRuleJsonInputParams.getAllValues();

    private final FromJsonHelper fromApiJsonHelper;

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("AccountingRule");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long accountToDebitId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue()).value(accountToDebitId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long accountToCreditId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue()).value(accountToCreditId)
                .ignoreIfNull().integerGreaterThanZero();

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.OFFICE_ID.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.OFFICE_ID.getValue()).value(officeId).notNull()
                .integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.NAME.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.NAME.getValue()).value(name).notBlank().notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.DESCRIPTION.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                .notExceedingLengthOf(500);

        final String[] creditTags = this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue()).value(creditTags).ignoreIfNull()
                .arrayNotEmpty();
        validateCreditOrDebitTagArray(creditTags, baseDataValidator, AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue());

        final String[] debitTags = this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue()).value(debitTags).ignoreIfNull()
                .arrayNotEmpty();
        validateCreditOrDebitTagArray(debitTags, baseDataValidator, AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue());

        if (creditTags == null && accountToCreditId == null) {
            final String creditTag = AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue();
            final String creditAccount = AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue();
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(creditAccount).append(".or.")
                    .append(creditTag).append(".required");
            final StringBuilder defaultUserMessage = new StringBuilder("The parameter ").append(creditAccount).append(" or")
                    .append(creditTag).append(" required");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultUserMessage.toString(),
                    creditAccount + "," + creditTag, new Object[] { creditAccount, creditTag });
            dataValidationErrors.add(error);
        }

        if (debitTags == null && accountToDebitId == null) {
            final String debitTag = AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue();
            final String debitAccount = AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue();
            final StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append(debitAccount).append(".or.")
                    .append(debitTag).append(".required");
            final StringBuilder defaultUserMessage = new StringBuilder("The parameter ").append(debitAccount).append(" or").append(debitTag)
                    .append(" required");
            final ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultUserMessage.toString(),
                    debitAccount + "," + debitTag, new Object[] { debitAccount, debitTag });
            dataValidationErrors.add(error);
        }

        final String allowMultipleCredits = this.fromApiJsonHelper
                .extractStringNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())
                .value(allowMultipleCredits).ignoreIfNull().notBlank();
        final Boolean allowMultipleCreditEntries = this.fromApiJsonHelper
                .extractBooleanNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())
                .value(allowMultipleCreditEntries).ignoreIfNull();
        final String allowMultipleDebits = this.fromApiJsonHelper
                .extractStringNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())
                .value(allowMultipleDebits).ignoreIfNull().notBlank();
        final Boolean allowMultipleDebitEntries = this.fromApiJsonHelper
                .extractBooleanNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())
                .value(allowMultipleDebitEntries).ignoreIfNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateCreditOrDebitTagArray(final String[] creditOrDebitTagArray, final DataValidatorBuilder baseDataValidator,
            final String parameter) {
        if (creditOrDebitTagArray != null && !ObjectUtils.isEmpty(creditOrDebitTagArray)) {
            for (final String tag : creditOrDebitTagArray) {
                baseDataValidator.reset().parameter(parameter).value(tag).ignoreIfNull().notBlank().longGreaterThanZero();
            }
        }
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("AccountingRule");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long accountToDebitId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_DEBIT.getValue()).value(accountToDebitId)
                .ignoreIfNull().notBlank().integerGreaterThanZero();

        final Long accountToCreditId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ACCOUNT_TO_CREDIT.getValue()).value(accountToCreditId)
                .ignoreIfNull().notBlank().integerGreaterThanZero();

        final Long officeId = this.fromApiJsonHelper.extractLongNamed(AccountingRuleJsonInputParams.OFFICE_ID.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.OFFICE_ID.getValue()).value(officeId).ignoreIfNull()
                .integerGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.NAME.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.NAME.getValue()).value(name).ignoreIfNull().notBlank()
                .notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed(AccountingRuleJsonInputParams.DESCRIPTION.getValue(), element);

        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DESCRIPTION.getValue()).value(description).ignoreIfNull()
                .notExceedingLengthOf(500);

        final String[] creditTags = this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue()).value(creditTags).ignoreIfNull()
                .arrayNotEmpty();
        validateCreditOrDebitTagArray(creditTags, baseDataValidator, AccountingRuleJsonInputParams.CREDIT_ACCOUNT_TAGS.getValue());

        final String[] debitTags = this.fromApiJsonHelper.extractArrayNamed(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue(),
                element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue()).value(debitTags).ignoreIfNull()
                .arrayNotEmpty();
        validateCreditOrDebitTagArray(debitTags, baseDataValidator, AccountingRuleJsonInputParams.DEBIT_ACCOUNT_TAGS.getValue());

        final String allowMultipleCredits = this.fromApiJsonHelper
                .extractStringNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())
                .value(allowMultipleCredits).ignoreIfNull().notBlank();
        final Boolean allowMultipleCreditEntries = this.fromApiJsonHelper
                .extractBooleanNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_CREDIT_ENTRIES.getValue())
                .value(allowMultipleCreditEntries).ignoreIfNull();
        final String allowMultipleDebits = this.fromApiJsonHelper
                .extractStringNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())
                .value(allowMultipleDebits).ignoreIfNull().notBlank();
        final Boolean allowMultipleDebitEntries = this.fromApiJsonHelper
                .extractBooleanNamed(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue(), element);
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.ALLOW_MULTIPLE_DEBIT_ENTRIES.getValue())
                .value(allowMultipleDebitEntries).ignoreIfNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
