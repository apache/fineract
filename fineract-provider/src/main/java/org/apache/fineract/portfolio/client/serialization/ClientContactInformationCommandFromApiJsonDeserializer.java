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
package org.apache.fineract.portfolio.client.serialization;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClientContactInformationCommandFromApiJsonDeserializer {

    private final FromJsonHelper fromApiJsonHelper;
    private final Set<String> supportedParametersForCreate = new HashSet<>(Arrays.asList(ClientApiConstants.clientIdParamName,
            ClientApiConstants.contactTypeIdParamName, ClientApiConstants.contactKeyParamName, ClientApiConstants.statusParamName,
            ClientApiConstants.currentContactParamName));
    private final Set<String> supportedParametersForUpdate = new HashSet<>(Arrays.asList(ClientApiConstants.contactTypeIdParamName,
            ClientApiConstants.contactKeyParamName, ClientApiConstants.statusParamName, ClientApiConstants.currentContactParamName));

    @Autowired
    public ClientContactInformationCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForCreate);
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("FamilyMembers");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Long contactTypeId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.contactTypeIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.contactTypeIdParamName).value(contactTypeId).notNull()
                .integerGreaterThanZero();
        final String contactKey = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.contactKeyParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.contactKeyParamName).value(contactKey).notBlank().notExceedingLengthOf(120);
        final Boolean current = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.currentContactParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.currentContactParamName).value(current).notNull().validateForBooleanValue();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForUpdate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("FamilyMembers");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.contactTypeIdParamName, element)) {
            final Long contactTypeId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.contactTypeIdParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.contactTypeIdParamName).value(contactTypeId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.contactKeyParamName, element)) {
            final String contactKey = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.contactKeyParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.contactKeyParamName).value(contactKey).notBlank()
                    .notExceedingLengthOf(120);
        }

        if (this.fromApiJsonHelper.parameterExists(ClientApiConstants.currentContactParamName, element)) {
            final Boolean current = this.fromApiJsonHelper.extractBooleanNamed(ClientApiConstants.currentContactParamName, element);
            baseDataValidator.reset().parameter(ClientApiConstants.currentContactParamName).value(current).notNull()
                    .validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForDelete(final long clientInformationId) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("FamilyMembers");

        baseDataValidator.reset().value(clientInformationId).notBlank().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
