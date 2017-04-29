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
package org.apache.fineract.infrastructure.sms.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.sms.SmsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class SmsDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
	private static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
			SmsApiConstants.localeParamName, SmsApiConstants.dateFormatParamName, SmsApiConstants.groupIdParamName,
			SmsApiConstants.clientIdParamName, SmsApiConstants.staffIdParamName, SmsApiConstants.messageParamName,
			SmsApiConstants.campaignIdParamName));

	public static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
			Arrays.asList(SmsApiConstants.messageParamName, SmsApiConstants.campaignIdParamName));

    @Autowired
    public SmsDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_REQUEST_DATA_PARAMETERS);
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
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_REQUEST_DATA_PARAMETERS);
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