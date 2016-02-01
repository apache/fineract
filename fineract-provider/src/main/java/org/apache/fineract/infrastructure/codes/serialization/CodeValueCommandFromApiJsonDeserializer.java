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
package org.apache.fineract.infrastructure.codes.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.codes.CodeConstants.CODEVALUE_JSON_INPUT_PARAMS;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class CodeValueCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = CODEVALUE_JSON_INPUT_PARAMS.getAllValues();
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CodeValueCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("code.value");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue(), element);
        baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue()).value(name).notBlank().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue()).value(description)
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue(), element)) {
            // Validate input value is a valid Integer
            this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue(), element);
        }

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue(), element)) {
            final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue(), element);
            baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue()).value(isActive).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("code.value");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue(), element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue(), element);
            baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.NAME.getValue()).value(name).notBlank()
                    .notExceedingLengthOf(100);
        }
        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue(), element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue(),
                    element);
            baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.DESCRIPTION.getValue()).value(description)
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue(), element)) {
            // Validate input value is a valid Integer
            this.fromApiJsonHelper.extractIntegerSansLocaleNamed(CODEVALUE_JSON_INPUT_PARAMS.POSITION.getValue(), element);
        }

        if (this.fromApiJsonHelper.parameterExists(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue(), element)) {
            final Boolean isActive = this.fromApiJsonHelper.extractBooleanNamed(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue(), element);
            baseDataValidator.reset().parameter(CODEVALUE_JSON_INPUT_PARAMS.IS_ACTIVE.getValue()).value(isActive).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}