/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.rule.api.AccountingRuleJsonInputParams;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AccountingRuleDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = AccountingRuleJsonInputParams.getAllValues();

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountingRuleDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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
        baseDataValidator.reset().parameter(AccountingRuleJsonInputParams.OFFICE_ID.getValue()).value(officeId).ignoreIfNull()
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

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
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

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
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

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
}
