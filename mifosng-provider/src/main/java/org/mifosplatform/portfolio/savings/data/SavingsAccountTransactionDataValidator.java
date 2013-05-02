/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activationDateParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.transactionAmountParamName;
import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.transactionDateParamName;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.savings.api.SavingsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class SavingsAccountTransactionDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SavingsAccountTransactionDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsApiConstants.SAVINGS_ACCOUNT_TRANSACTION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_TRANSACTION_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed(transactionDateParamName, element);
        baseDataValidator.reset().parameter(transactionDateParamName).value(transactionDate).notNull();

        final BigDecimal transactionAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transactionAmountParamName, element);
        baseDataValidator.reset().parameter(transactionAmountParamName).value(transactionAmount).notNull().positiveAmount();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateActivation(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                SavingsApiConstants.SAVINGS_ACCOUNT_ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = fromApiJsonHelper.extractLocalDateNamed(activationDateParamName, element);
        baseDataValidator.reset().parameter(activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}