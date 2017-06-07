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
package org.apache.fineract.infrastructure.core.serialization;

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
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class DatatableCommandFromApiJsonDeserializer {

    private final static String DATATABLE_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,48}[a-zA-Z0-9]$";
    private final static String DATATABLE_COLUMN_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,}[a-zA-Z0-9]$";
    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParametersForCreate = new HashSet<>(Arrays.asList("datatableName", "apptableName", "multiRow",
            "columns"));
    private final Set<String> supportedParametersForCreateColumns = new HashSet<>(Arrays.asList("name", "type", "length",
            "mandatory", "code"));
    private final Set<String> supportedParametersForUpdate = new HashSet<>(Arrays.asList("apptableName", "changeColumns",
            "addColumns", "dropColumns"));
    private final Set<String> supportedParametersForAddColumns = new HashSet<>(Arrays.asList("name", "type", "length", "mandatory",
            "after", "code"));
    private final Set<String> supportedParametersForChangeColumns = new HashSet<>(Arrays.asList("name", "newName", "length",
            "mandatory", "after", "code", "newCode"));
    private final Set<String> supportedParametersForDropColumns = new HashSet<>(Arrays.asList("name"));
    private final Object[] supportedColumnTypes = { "string", "number", "boolean", "decimal", "date", "datetime", "text", "dropdown" };
    private final Object[] supportedApptableNames = { "m_loan", "m_savings_account", "m_client", "m_group", "m_center", "m_office",
            "m_savings_product", "m_product_loan" };

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DatatableCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void validateType(final DataValidatorBuilder baseDataValidator, final JsonElement column) {
        final String type = this.fromApiJsonHelper.extractStringNamed("type", column);
        baseDataValidator.reset().parameter("type").value(type).notBlank().isOneOfTheseStringValues(this.supportedColumnTypes);

        if (type != null && type.equalsIgnoreCase("String")) {
            if (this.fromApiJsonHelper.parameterExists("length", column)) {
                final String lengthStr = this.fromApiJsonHelper.extractStringNamed("length", column);
                if (lengthStr != null && !StringUtils.isWhitespace(lengthStr) && StringUtils.isNumeric(lengthStr)
                        && StringUtils.isNotBlank(lengthStr)) {
                    final Integer length = Integer.parseInt(lengthStr);
                    baseDataValidator.reset().parameter("length").value(length).positiveAmount();
                } else if (StringUtils.isBlank(lengthStr) || StringUtils.isWhitespace(lengthStr)) {
                    baseDataValidator.reset().parameter("length").failWithCode("must.be.provided.when.type.is.String");
                } else if (!StringUtils.isNumeric(lengthStr)) {
                    baseDataValidator.reset().parameter("length").failWithCode("not.greater.than.zero");
                }
            } else {
                baseDataValidator.reset().parameter("length").failWithCode("must.be.provided.when.type.is.String");
            }
        } else {
            baseDataValidator.reset().parameter("length").mustBeBlankWhenParameterProvidedIs("type", type);
        }

        final String code = this.fromApiJsonHelper.extractStringNamed("code", column);
        if (type != null && type.equalsIgnoreCase("Dropdown")) {
            if (code != null) {
                baseDataValidator.reset().parameter("code").value(code).notBlank().matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);
            } else {
                baseDataValidator.reset().parameter("code").value(code).cantBeBlankWhenParameterProvidedIs("type", type);
            }
        } else {
            baseDataValidator.reset().parameter("code").value(code).mustBeBlankWhenParameterProvided("type", type);
        }
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForCreate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String datatableName = this.fromApiJsonHelper.extractStringNamed("datatableName", element);
        baseDataValidator.reset().parameter("datatableName").value(datatableName).notBlank().notExceedingLengthOf(50)
                .matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

        final String apptableName = this.fromApiJsonHelper.extractStringNamed("apptableName", element);
        baseDataValidator.reset().parameter("apptableName").value(apptableName).notBlank().notExceedingLengthOf(50)
                .isOneOfTheseValues(this.supportedApptableNames);
        final String fkColumnName = (apptableName != null) ? apptableName.substring(2) + "_id" : "";

        final Boolean multiRow = this.fromApiJsonHelper.extractBooleanNamed("multiRow", element);
        baseDataValidator.reset().parameter("multiRow").value(multiRow).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

        final JsonArray columns = this.fromApiJsonHelper.extractJsonArrayNamed("columns", element);
        baseDataValidator.reset().parameter("columns").value(columns).notNull().jsonArrayNotEmpty();

        if (columns != null) {
            for (final JsonElement column : columns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), this.supportedParametersForCreateColumns);

                final String name = this.fromApiJsonHelper.extractStringNamed("name", column);
                baseDataValidator.reset().parameter("name").value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed("mandatory", column);
                baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        // Because all parameters are optional, a check to see if at least one
        // parameter
        // has been specified is necessary in order to avoid JSON requests with
        // no parameters
        if (!json.matches("(?s)\\A\\{.*?(\\\".*?\\\"\\s*?:\\s*?)+.*?\\}\\z")) { throw new PlatformDataIntegrityException(
                "error.msg.invalid.request.body.no.parameters", "Provided JSON request body does not have any parameters."); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParametersForUpdate);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String apptableName = this.fromApiJsonHelper.extractStringNamed("apptableName", element);
        baseDataValidator.reset().parameter("apptableName").value(apptableName).ignoreIfNull().notBlank()
                .isOneOfTheseValues(this.supportedApptableNames);
        final String fkColumnName = (apptableName != null) ? apptableName.substring(2) + "_id" : "";

        final JsonArray changeColumns = this.fromApiJsonHelper.extractJsonArrayNamed("changeColumns", element);
        baseDataValidator.reset().parameter("changeColumns").value(changeColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (changeColumns != null) {
            for (final JsonElement column : changeColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), this.supportedParametersForChangeColumns);

                final String name = this.fromApiJsonHelper.extractStringNamed("name", column);
                baseDataValidator.reset().parameter("name").value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newName = this.fromApiJsonHelper.extractStringNamed("newName", column);
                baseDataValidator.reset().parameter("newName").value(newName).ignoreIfNull().notBlank().notExceedingLengthOf(50)
                        .isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (this.fromApiJsonHelper.parameterExists("length", column)) {
                    final String lengthStr = this.fromApiJsonHelper.extractStringNamed("length", column);
                    if (StringUtils.isWhitespace(lengthStr) || !StringUtils.isNumeric(lengthStr) || StringUtils.isBlank(lengthStr)) {
                        baseDataValidator.reset().parameter("length").failWithCode("not.greater.than.zero");
                    } else {
                        final Integer length = Integer.parseInt(lengthStr);
                        baseDataValidator.reset().parameter("length").value(length).ignoreIfNull().notBlank().positiveAmount();
                    }
                }

                final String code = this.fromApiJsonHelper.extractStringNamed("code", column);
                baseDataValidator.reset().parameter("code").value(code).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newCode = this.fromApiJsonHelper.extractStringNamed("newCode", column);
                baseDataValidator.reset().parameter("newCode").value(newCode).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (StringUtils.isBlank(code) && StringUtils.isNotBlank(newCode)) {
                    baseDataValidator.reset().parameter("code").value(code).cantBeBlankWhenParameterProvidedIs("newCode", newCode);
                }

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed("mandatory", column);
                baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed("after", column);
                baseDataValidator.reset().parameter("after").value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        final JsonArray addColumns = this.fromApiJsonHelper.extractJsonArrayNamed("addColumns", element);
        baseDataValidator.reset().parameter("addColumns").value(addColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (addColumns != null) {
            for (final JsonElement column : addColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), this.supportedParametersForAddColumns);

                final String name = this.fromApiJsonHelper.extractStringNamed("name", column);
                baseDataValidator.reset().parameter("name").value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed("mandatory", column);
                baseDataValidator.reset().parameter("mandatory").value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed("after", column);
                baseDataValidator.reset().parameter("after").value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        final JsonArray dropColumns = this.fromApiJsonHelper.extractJsonArrayNamed("dropColumns", element);
        baseDataValidator.reset().parameter("dropColumns").value(dropColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (dropColumns != null) {
            for (final JsonElement column : dropColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), this.supportedParametersForDropColumns);

                final String name = this.fromApiJsonHelper.extractStringNamed("name", column);
                baseDataValidator.reset().parameter("name").value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
