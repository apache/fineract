/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.data;

import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferDateParamName;
import static org.mifosplatform.portfolio.account.api.AccountTransfersApiConstants.transferDescriptionParamName;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class AccountTransfersDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator;

    @Autowired
    public AccountTransfersDataValidator(final FromJsonHelper fromApiJsonHelper,
            final AccountTransfersDetailDataValidator accountTransfersDetailDataValidator) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.accountTransfersDetailDataValidator = accountTransfersDetailDataValidator;
    }

    public void validate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ACCOUNT_TRANSFER_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        this.accountTransfersDetailDataValidator.validate(command, baseDataValidator);

        final LocalDate transactionDate = this.fromApiJsonHelper.extractLocalDateNamed(transferDateParamName, element);
        baseDataValidator.reset().parameter(transferDateParamName).value(transactionDate).notNull();

        final BigDecimal transactionAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transferAmountParamName, element);
        baseDataValidator.reset().parameter(transferAmountParamName).value(transactionAmount).notNull().positiveAmount();

        final String transactionDescription = this.fromApiJsonHelper.extractStringNamed(transferDescriptionParamName, element);
        baseDataValidator.reset().parameter(transferDescriptionParamName).value(transactionDescription).notBlank()
                .notExceedingLengthOf(200);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}