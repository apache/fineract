/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.mifosplatform.accounting.financialactivityaccount.api.FinancialActivityAccountsJsonInputParams;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class FinancialActivityAccountDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = FinancialActivityAccountsJsonInputParams.getAllValues();

    private final FromJsonHelper fromApiJsonHelper;

    private final String paramNameForFinancialActivity = FinancialActivityAccountsJsonInputParams.FINANCIAL_ACTIVITY_ID.getValue();
    private final String paramNameForGLAccount = FinancialActivityAccountsJsonInputParams.GL_ACCOUNT_ID.getValue();

    @Autowired
    public FinancialActivityAccountDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        validateJSONAndCheckForUnsupportedParams(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = getDataValidator(dataValidationErrors);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer financialActivityId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(paramNameForFinancialActivity, element);
        baseDataValidator.reset().parameter(paramNameForFinancialActivity).value(financialActivityId).notNull()
                .isOneOfTheseValues(FINANCIAL_ACTIVITY.ASSET_TRANSFER.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),
                		FINANCIAL_ACTIVITY.CASH_AT_MAINVAULT.getValue(), FINANCIAL_ACTIVITY.CASH_AT_TELLER.getValue(),FINANCIAL_ACTIVITY.OPENING_BALANCES_TRANSFER_CONTRA.getValue());

        final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(paramNameForGLAccount, element);
        baseDataValidator.reset().parameter(paramNameForGLAccount).value(glAccountId).notNull().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private DataValidatorBuilder getDataValidator(final List<ApiParameterError> dataValidationErrors) {
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("financialactivityaccount");
        return baseDataValidator;
    }

    public void validateForUpdate(final String json) {
        validateJSONAndCheckForUnsupportedParams(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = getDataValidator(dataValidationErrors);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(paramNameForFinancialActivity, element)) {
            final Integer financialActivityId = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(paramNameForFinancialActivity, element);
            baseDataValidator.reset().parameter(paramNameForFinancialActivity).value(financialActivityId).ignoreIfNull()
                    .isOneOfTheseValues(FINANCIAL_ACTIVITY.ASSET_TRANSFER.getValue(), FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue(),FINANCIAL_ACTIVITY.OPENING_BALANCES_TRANSFER_CONTRA.getValue());
        }

        if (this.fromApiJsonHelper.parameterExists(paramNameForGLAccount, element)) {
            final Long glAccountId = this.fromApiJsonHelper.extractLongNamed(paramNameForGLAccount, element);
            baseDataValidator.reset().parameter(paramNameForGLAccount).value(glAccountId).ignoreIfNull().integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateJSONAndCheckForUnsupportedParams(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}