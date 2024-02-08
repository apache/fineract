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
import org.apache.fineract.adhocquery.domain.ReportRunFrequency;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class AdHocDataValidator {

    public static final String NAME = "name";
    public static final String QUERY = "query";
    public static final String TABLE_NAME = "tableName";
    public static final String TABLE_FIELDS = "tableFields";
    public static final String EMAIL = "email";
    public static final String REPORT_RUN_FREQUENCY = "reportRunFrequency";
    public static final String REPORT_RUN_EVERY = "reportRunEvery";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList(NAME, QUERY, TABLE_NAME, TABLE_FIELDS, EMAIL, "isActive", REPORT_RUN_FREQUENCY, REPORT_RUN_EVERY));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AdHocDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("adhoc");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
        baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed(QUERY, element);
        baseDataValidator.reset().parameter(QUERY).value(description).notBlank().notExceedingLengthOf(2000);

        final String tableName = this.fromApiJsonHelper.extractStringNamed(TABLE_NAME, element);
        baseDataValidator.reset().parameter(TABLE_NAME).value(tableName).notBlank().notExceedingLengthOf(100);

        final String tableFields = this.fromApiJsonHelper.extractStringNamed(TABLE_FIELDS, element);
        baseDataValidator.reset().parameter(TABLE_FIELDS).value(tableFields).notBlank().notExceedingLengthOf(1000);

        final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
        baseDataValidator.reset().parameter(EMAIL).value(email).ignoreIfNull().notExceedingLengthOf(500);

        final Long reportRunFrequencyCode = this.fromApiJsonHelper.extractLongNamed(REPORT_RUN_FREQUENCY, element);
        if (reportRunFrequencyCode != null) {
            baseDataValidator.reset().parameter(REPORT_RUN_FREQUENCY).value(reportRunFrequencyCode)
                    .inMinMaxRange((int) ReportRunFrequency.DAILY.getValue(), (int) ReportRunFrequency.CUSTOM.getValue());
        }

        final Long reportRunEvery = this.fromApiJsonHelper.extractLongNamed(REPORT_RUN_EVERY, element);
        if (reportRunEvery != null) {
            baseDataValidator.reset().parameter(REPORT_RUN_EVERY).value(reportRunFrequencyCode).integerGreaterThanZero();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("adhoc");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(NAME, element);
            baseDataValidator.reset().parameter(NAME).value(name).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(QUERY, element)) {
            final String query = this.fromApiJsonHelper.extractStringNamed(QUERY, element);
            baseDataValidator.reset().parameter(QUERY).value(query).notBlank().notExceedingLengthOf(2000);
        }
        if (this.fromApiJsonHelper.parameterExists(TABLE_NAME, element)) {
            final String tableName = this.fromApiJsonHelper.extractStringNamed(TABLE_NAME, element);
            baseDataValidator.reset().parameter(TABLE_NAME).value(tableName).notBlank().notExceedingLengthOf(100);
        }
        if (this.fromApiJsonHelper.parameterExists(TABLE_FIELDS, element)) {
            final String tableField = this.fromApiJsonHelper.extractStringNamed(TABLE_FIELDS, element);
            baseDataValidator.reset().parameter(TABLE_FIELDS).value(tableField).notBlank().notExceedingLengthOf(2000);
        }
        if (this.fromApiJsonHelper.parameterExists(EMAIL, element)) {
            final String email = this.fromApiJsonHelper.extractStringNamed(EMAIL, element);
            baseDataValidator.reset().parameter(EMAIL).value(email).ignoreIfNull().notExceedingLengthOf(500);
        }
        if (this.fromApiJsonHelper.parameterExists(REPORT_RUN_FREQUENCY, element)) {
            final Long reportRunFrequencyCode = this.fromApiJsonHelper.extractLongNamed(REPORT_RUN_FREQUENCY, element);
            baseDataValidator.reset().parameter(REPORT_RUN_FREQUENCY).value(reportRunFrequencyCode)
                    .inMinMaxRange((int) ReportRunFrequency.DAILY.getValue(), (int) ReportRunFrequency.CUSTOM.getValue());
        }
        if (this.fromApiJsonHelper.parameterExists(REPORT_RUN_EVERY, element)) {
            final Long reportRunEvery = this.fromApiJsonHelper.extractLongNamed(REPORT_RUN_EVERY, element);
            baseDataValidator.reset().parameter(REPORT_RUN_EVERY).value(reportRunEvery).integerGreaterThanZero();
        }
        /*
         * if (this.fromApiJsonHelper.parameterExists("isActive", element)) { final Integer isActive =
         * this.fromApiJsonHelper.extractIntegerNamed("isActive", element, Locale.getDefault());
         * baseDataValidator.reset().parameter("isActive").value(isActive). notNull().inMinMaxRange(1, 2); }
         */

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
