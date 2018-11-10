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
package org.apache.fineract.adhocquery.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public final class AdHocDataValidator {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList(
        "name","query","tableName","tableFields","email","isActive", "reportRunFrequency", "reportRunEvery"
    ));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AdHocDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("adhoc");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
        baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed("query", element);
        baseDataValidator.reset().parameter("query").value(description).notBlank().notExceedingLengthOf(2000);
        
        final String tableName = this.fromApiJsonHelper.extractStringNamed("tableName", element);
        baseDataValidator.reset().parameter("tableName").value(tableName).notBlank().notExceedingLengthOf(100);
        
        final String tableFields = this.fromApiJsonHelper.extractStringNamed("tableFields", element);
        baseDataValidator.reset().parameter("tableFields").value(tableFields).notBlank().notExceedingLengthOf(1000);

        final String email = this.fromApiJsonHelper.extractStringNamed("email", element);
        baseDataValidator.reset().parameter("email").value(email).ignoreIfNull().notExceedingLengthOf(500);

        final Long reportRunFrequencyCode = this.fromApiJsonHelper.extractLongNamed("reportRunFrequency", element);
        if (reportRunFrequencyCode != null) {
            baseDataValidator.reset().parameter("reportRunFrequency").value(reportRunFrequencyCode)
                .inMinMaxRange((int) ReportRunFrequency.DAILY.getValue(), (int) ReportRunFrequency.CUSTOM.getValue());
        }

        final Long reportRunEvery = this.fromApiJsonHelper.extractLongNamed("reportRunEvery", element);
        if (reportRunEvery != null) {
            baseDataValidator.reset().parameter("reportRunEvery").value(reportRunFrequencyCode).integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("adhoc");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists("name", element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed("name", element);
            baseDataValidator.reset().parameter("name").value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists("query", element)) {
            final String query = this.fromApiJsonHelper.extractStringNamed("query", element);
            baseDataValidator.reset().parameter("query").value(query).notBlank().notExceedingLengthOf(2000);
        }
        if (this.fromApiJsonHelper.parameterExists("tableName", element)) {
            final String tableName = this.fromApiJsonHelper.extractStringNamed("tableName", element);
            baseDataValidator.reset().parameter("tableName").value(tableName).notBlank().notExceedingLengthOf(100);
        }
        if (this.fromApiJsonHelper.parameterExists("tableFields", element)) {
            final String tableField = this.fromApiJsonHelper.extractStringNamed("tableFields", element);
            baseDataValidator.reset().parameter("tableFields").value(tableField).notBlank().notExceedingLengthOf(2000);
        }
        if (this.fromApiJsonHelper.parameterExists("email", element)) {
            final String email = this.fromApiJsonHelper.extractStringNamed("email", element);
            baseDataValidator.reset().parameter("email").value(email).ignoreIfNull().notExceedingLengthOf(500);
        }
        if (this.fromApiJsonHelper.parameterExists("reportRunFrequency", element)) {
            final Long reportRunFrequencyCode = this.fromApiJsonHelper.extractLongNamed("reportRunFrequency", element);
            baseDataValidator.reset().parameter("reportRunFrequency").value(reportRunFrequencyCode)
                .inMinMaxRange((int) ReportRunFrequency.DAILY.getValue(), (int) ReportRunFrequency.CUSTOM.getValue());
        }
        if (this.fromApiJsonHelper.parameterExists("reportRunEvery", element)) {
            final Long reportRunEvery = this.fromApiJsonHelper.extractLongNamed("reportRunEvery", element);
            baseDataValidator.reset().parameter("reportRunEvery").value(reportRunEvery).integerGreaterThanZero();
        }
        /*if (this.fromApiJsonHelper.parameterExists("isActive", element)) {
            final Integer isActive = this.fromApiJsonHelper.extractIntegerNamed("isActive", element, Locale.getDefault());
            baseDataValidator.reset().parameter("isActive").value(isActive).notNull().inMinMaxRange(1, 2);
        }*/
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException(dataValidationErrors); }
    }
}