/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import static org.mifosplatform.portfolio.savings.api.SavingsApiConstants.activationDateParamName;

import java.lang.reflect.Type;
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
import org.mifosplatform.portfolio.client.api.ClientApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class ClientDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ClientDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_REQUEST_DATA_PARAMETERS);
        final JsonElement element = fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).ignoreIfNull()
                    .notExceedingLengthOf(100);
        }

        final Long officeId = fromApiJsonHelper.extractLongNamed(ClientApiConstants.officeIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.groupIdParamName, element)) {
            final Long groupId = fromApiJsonHelper.extractLongNamed(ClientApiConstants.groupIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.groupIdParamName).value(groupId).notNull().integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        } else {
            baseDataValidator.reset().parameter(ClientApiConstants.activeParamName).value(active).trueOrFalseRequired(false);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.CLIENT_REQUEST_DATA_PARAMETERS);
        final JsonElement element = fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (fromApiJsonHelper.parameterExists(ClientApiConstants.accountNoParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String accountNo = fromApiJsonHelper.extractStringNamed(ClientApiConstants.accountNoParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.accountNoParamName).value(accountNo).notBlank().notExceedingLengthOf(20);
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.externalIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String externalId = fromApiJsonHelper.extractStringNamed(ClientApiConstants.externalIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.externalIdParamName).value(externalId).notExceedingLengthOf(100);
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.fullnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.lastnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.middlenameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.firstnameParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (fromApiJsonHelper.parameterExists(ClientApiConstants.officeIdParamName, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final Long officeId = fromApiJsonHelper.extractLongNamed(ClientApiConstants.officeIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.officeIdParamName).value(officeId).notNull().integerGreaterThanZero();
        }

        final Boolean active = fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.activeParamName, element);
        if (active != null) {
            if (active.booleanValue()) {
                final LocalDate joinedDate = fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.activationDateParamName, element);
                baseDataValidator.reset().parameter(ClientApiConstants.activationDateParamName).value(joinedDate).notNull();
            }
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateActivation(final JsonCommand command) {
        final String json = command.json();

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, ClientApiConstants.ACTIVATION_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonElement element = command.parsedJson();

        final LocalDate activationDate = fromApiJsonHelper.extractLocalDateNamed(activationDateParamName, element);
        baseDataValidator.reset().parameter(activationDateParamName).value(activationDate).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}