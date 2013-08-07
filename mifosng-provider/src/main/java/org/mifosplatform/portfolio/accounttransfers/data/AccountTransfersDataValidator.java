/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounttransfers.data;

import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.REQUEST_DATA_PARAMETERS;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferDescriptionParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromAccountTypeParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromClientIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.fromOfficeIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toAccountIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toAccountTypeParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toClientIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.toOfficeIdParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferAmountParamName;
import static org.mifosplatform.portfolio.accounttransfers.AccountTransfersApiConstants.transferDateParamName;

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

    @Autowired
    public AccountTransfersDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validate(final JsonCommand command) {

        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ACCOUNT_TRANSFER_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final Long fromOfficeId = fromApiJsonHelper.extractLongNamed(fromOfficeIdParamName, element);
        baseDataValidator.reset().parameter(fromOfficeIdParamName).value(fromOfficeId).notNull().integerGreaterThanZero();

        final Long fromClientId = fromApiJsonHelper.extractLongNamed(fromClientIdParamName, element);
        baseDataValidator.reset().parameter(fromClientIdParamName).value(fromClientId).notNull().integerGreaterThanZero();

        final Long fromAccountId = fromApiJsonHelper.extractLongNamed(fromAccountIdParamName, element);
        baseDataValidator.reset().parameter(fromAccountIdParamName).value(fromAccountId).notNull().integerGreaterThanZero();

        final Integer fromAccountType = fromApiJsonHelper.extractIntegerSansLocaleNamed(fromAccountTypeParamName, element);
        baseDataValidator.reset().parameter(fromAccountTypeParamName).value(fromAccountType)
                .isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

        final Long toOfficeId = fromApiJsonHelper.extractLongNamed(toOfficeIdParamName, element);
        baseDataValidator.reset().parameter(toOfficeIdParamName).value(toOfficeId).notNull().integerGreaterThanZero();

        final Long toClientId = fromApiJsonHelper.extractLongNamed(toClientIdParamName, element);
        baseDataValidator.reset().parameter(toClientIdParamName).value(toClientId).notNull().integerGreaterThanZero();

        final Long toAccountId = fromApiJsonHelper.extractLongNamed(toAccountIdParamName, element);
        baseDataValidator.reset().parameter(toAccountIdParamName).value(toAccountId).notNull().integerGreaterThanZero();

        final Integer toAccountType = fromApiJsonHelper.extractIntegerSansLocaleNamed(toAccountTypeParamName, element);
        baseDataValidator.reset().parameter(toAccountTypeParamName).value(toAccountType)
                .isOneOfTheseValues(Integer.valueOf(1), Integer.valueOf(2));

        final LocalDate transactionDate = fromApiJsonHelper.extractLocalDateNamed(transferDateParamName, element);
        baseDataValidator.reset().parameter(transferDateParamName).value(transactionDate).notNull();

        final BigDecimal transactionAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(transferAmountParamName, element);
        baseDataValidator.reset().parameter(transferAmountParamName).value(transactionAmount).notNull().positiveAmount();

        final String transactionDescription = fromApiJsonHelper.extractStringNamed(transferDescriptionParamName, element);
        baseDataValidator.reset().parameter(transferDescriptionParamName).value(transactionDescription).notBlank().notExceedingLengthOf(200);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}