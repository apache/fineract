/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.sms.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.sms.SmsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class SmsDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SmsDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsApiConstants.CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsApiConstants.RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)) {
            final Long groupId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.groupIdParamName, element);
            baseDataValidator.reset().parameter(SmsApiConstants.groupIdParamName).value(groupId).notNull().integerGreaterThanZero();

            // ensure clientId and staffId are not passed
            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.clientIdParamName).failWithCode("cannot.be.passed.with.groupId");
            }

            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.staffIdParamName).failWithCode("cannot.be.passed.with.groupId");
            }
        } else if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)) {
            final Long clientId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.clientIdParamName, element);
            baseDataValidator.reset().parameter(SmsApiConstants.clientIdParamName).value(clientId).notNull().integerGreaterThanZero();

            // ensure groupId and staffId are not passed
            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.groupIdParamName).failWithCode("cannot.be.passed.with.clientId");
            }

            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.staffIdParamName).failWithCode("cannot.be.passed.with.clientId");
            }
        } else if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(SmsApiConstants.staffIdParamName, element);
            baseDataValidator.reset().parameter(SmsApiConstants.staffIdParamName).value(staffId).ignoreIfNull().longGreaterThanZero();

            // ensure groupId and clientId are not passed
            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.groupIdParamName).failWithCode("cannot.be.passed.with.staffId");
            }

            if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)) {
                baseDataValidator.reset().parameter(SmsApiConstants.clientIdParamName).failWithCode("cannot.be.passed.with.staffId");
            }
        }

        if (!this.fromApiJsonHelper.parameterExists(SmsApiConstants.groupIdParamName, element)
                && !this.fromApiJsonHelper.parameterExists(SmsApiConstants.clientIdParamName, element)
                && !this.fromApiJsonHelper.parameterExists(SmsApiConstants.staffIdParamName, element)) {
            baseDataValidator.reset().parameter(SmsApiConstants.staffIdParamName)
                    .failWithCodeNoParameterAddedToErrorCode("no.entity.provided");
        }

        final String message = this.fromApiJsonHelper.extractStringNamed(SmsApiConstants.messageParamName, element);
        baseDataValidator.reset().parameter(SmsApiConstants.messageParamName).value(message).notBlank().notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SmsApiConstants.UPDATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(SmsApiConstants.RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(SmsApiConstants.messageParamName, element)) {
            final String message = this.fromApiJsonHelper.extractStringNamed(SmsApiConstants.messageParamName, element);
            baseDataValidator.reset().parameter(SmsApiConstants.messageParamName).value(message).notBlank().notExceedingLengthOf(1000);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}