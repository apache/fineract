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

import com.google.gson.JsonArray;
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
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatatableCommandFromApiJsonDeserializer {

    public static final String DATATABLE_NAME = "datatableName";
    public static final String ENTITY_SUB_TYPE = "entitySubType";
    public static final String APPTABLE_NAME = "apptableName";
    public static final String MULTI_ROW = "multiRow";
    public static final String COLUMNS = "columns";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String LENGTH = "length";
    public static final String MANDATORY = "mandatory";
    public static final String CODE = "code";
    public static final String CHANGE_COLUMNS = "changeColumns";
    public static final String ADD_COLUMNS = "addColumns";
    public static final String DROP_COLUMNS = "dropColumns";
    public static final String AFTER = "after";
    public static final String NEW_CODE = "newCode";
    public static final String M_LOAN = "m_loan";
    public static final String M_SAVINGS_ACCOUNT = "m_savings_account";
    public static final String M_CLIENT = "m_client";
    public static final String M_GROUP = "m_group";
    public static final String M_CENTER = "m_center";
    public static final String M_OFFICE = "m_office";
    public static final String M_SAVINGS_PRODUCT = "m_savings_product";
    public static final String M_PRODUCT_LOAN = "m_product_loan";
    public static final String NEW_NAME = "newName";
    public static final String STRING = "string";
    public static final String NUMBER = "number";
    public static final String BOOLEAN = "boolean";
    public static final String DECIMAL = "decimal";
    public static final String DATE = "date";
    public static final String DATETIME = "datetime";
    public static final String TEXT = "text";
    public static final String DROPDOWN = "dropdown";
    private static final String DATATABLE_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,48}[a-zA-Z0-9]$";
    private static final String DATATABLE_COLUMN_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,}[a-zA-Z0-9]$";
    private static final String INDEXED = "indexed";
    private static final String UNIQUE = "unique";
    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CREATE = new HashSet<>(
            Arrays.asList(DATATABLE_NAME, ENTITY_SUB_TYPE, APPTABLE_NAME, MULTI_ROW, COLUMNS));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CREATE_COLUMNS = new HashSet<>(
            Arrays.asList(NAME, TYPE, LENGTH, MANDATORY, CODE, UNIQUE, INDEXED));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_UPDATE = new HashSet<>(
            Arrays.asList(APPTABLE_NAME, ENTITY_SUB_TYPE, CHANGE_COLUMNS, ADD_COLUMNS, DROP_COLUMNS));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_ADD_COLUMNS = new HashSet<>(
            Arrays.asList(NAME, TYPE, LENGTH, MANDATORY, AFTER, CODE, UNIQUE, INDEXED));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CHANGE_COLUMNS = new HashSet<>(
            Arrays.asList(NAME, NEW_NAME, LENGTH, MANDATORY, AFTER, CODE, NEW_CODE, UNIQUE, INDEXED));
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_DROP_COLUMNS = new HashSet<>(List.of(NAME));
    private static final Object[] SUPPORTED_COLUMN_TYPES = { STRING, NUMBER, BOOLEAN, DECIMAL, DATE, DATETIME, TEXT, DROPDOWN };
    private static final Object[] SUPPORTED_APPTABLE_NAMES = { M_LOAN, M_SAVINGS_ACCOUNT, M_CLIENT, M_GROUP, M_CENTER, M_OFFICE,
            M_SAVINGS_PRODUCT, M_PRODUCT_LOAN };

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DatatableCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    private void validateType(final DataValidatorBuilder baseDataValidator, final JsonElement column) {
        final String type = this.fromApiJsonHelper.extractStringNamed(TYPE, column);
        baseDataValidator.reset().parameter(TYPE).value(type).notBlank().isOneOfTheseStringValues(SUPPORTED_COLUMN_TYPES);

        if (type != null && type.equalsIgnoreCase("String")) {
            if (this.fromApiJsonHelper.parameterExists(LENGTH, column)) {
                final String lengthStr = this.fromApiJsonHelper.extractStringNamed(LENGTH, column);
                if (lengthStr != null && !StringUtils.isWhitespace(lengthStr) && StringUtils.isNumeric(lengthStr)
                        && StringUtils.isNotBlank(lengthStr)) {
                    final Integer length = Integer.parseInt(lengthStr);
                    baseDataValidator.reset().parameter(LENGTH).value(length).positiveAmount();
                } else if (StringUtils.isBlank(lengthStr) || StringUtils.isWhitespace(lengthStr)) {
                    baseDataValidator.reset().parameter(LENGTH).failWithCode("must.be.provided.when.type.is.String");
                } else if (!StringUtils.isNumeric(lengthStr)) {
                    baseDataValidator.reset().parameter(LENGTH).failWithCode("not.greater.than.zero");
                }
            } else {
                baseDataValidator.reset().parameter(LENGTH).failWithCode("must.be.provided.when.type.is.String");
            }
        } else {
            baseDataValidator.reset().parameter(LENGTH).mustBeBlankWhenParameterProvidedIs(TYPE, type);
        }

        final String code = this.fromApiJsonHelper.extractStringNamed(CODE, column);
        if (type != null && type.equalsIgnoreCase(DROPDOWN)) {
            if (code != null) {
                baseDataValidator.reset().parameter(CODE).value(code).notBlank().matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);
            } else {
                baseDataValidator.reset().parameter(CODE).value(code).cantBeBlankWhenParameterProvidedIs(TYPE, type);
            }
        } else {
            baseDataValidator.reset().parameter(CODE).value(code).mustBeBlankWhenParameterProvided(TYPE, type);
        }
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS_FOR_CREATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String datatableName = this.fromApiJsonHelper.extractStringNamed(DATATABLE_NAME, element);
        baseDataValidator.reset().parameter(DATATABLE_NAME).value(datatableName).notBlank().notExceedingLengthOf(50)
                .matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

        final String apptableName = this.fromApiJsonHelper.extractStringNamed(APPTABLE_NAME, element);
        baseDataValidator.reset().parameter(APPTABLE_NAME).value(apptableName).notBlank().notExceedingLengthOf(50)
                .isOneOfTheseValues(SUPPORTED_APPTABLE_NAMES);

        if (M_CLIENT.equals(apptableName)) {
            String entitySubType = this.fromApiJsonHelper.extractStringNamed(ENTITY_SUB_TYPE, element);
            baseDataValidator.reset().parameter(ENTITY_SUB_TYPE).value(entitySubType).notBlank(); // Person or Entity
        }
        final String fkColumnName = (apptableName != null) ? apptableName.substring(2) + "_id" : "";

        final Boolean multiRow = this.fromApiJsonHelper.extractBooleanNamed(MULTI_ROW, element);
        baseDataValidator.reset().parameter(MULTI_ROW).value(multiRow).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

        final JsonArray columns = this.fromApiJsonHelper.extractJsonArrayNamed(COLUMNS, element);
        baseDataValidator.reset().parameter(COLUMNS).value(columns).notNull().jsonArrayNotEmpty();

        if (columns != null) {
            for (final JsonElement column : columns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_CREATE_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(NAME, column);
                baseDataValidator.reset().parameter(NAME).value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(MANDATORY, column);
                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(UNIQUE, column);
                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(INDEXED, column);
                baseDataValidator.reset().parameter(MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
                baseDataValidator.reset().parameter(UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
                baseDataValidator.reset().parameter(INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        // Because all parameters are optional, a check to see if at least one
        // parameter
        // has been specified is necessary in order to avoid JSON requests with
        // no parameters
        if (!json.matches("(?s)\\A\\{.*?(\\\".*?\\\"\\s*?:\\s*?)+.*?\\}\\z")) { // NOSONAR
            throw new PlatformDataIntegrityException("error.msg.invalid.request.body.no.parameters",
                    "Provided JSON request body does not have any parameters.");
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS_FOR_UPDATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final String apptableName = this.fromApiJsonHelper.extractStringNamed(APPTABLE_NAME, element);
        baseDataValidator.reset().parameter(APPTABLE_NAME).value(apptableName).ignoreIfNull().notBlank()
                .isOneOfTheseValues(SUPPORTED_APPTABLE_NAMES);

        if (M_CLIENT.equals(apptableName)) {
            String entitySubType = this.fromApiJsonHelper.extractStringNamed(ENTITY_SUB_TYPE, element);
            baseDataValidator.reset().parameter(ENTITY_SUB_TYPE).value(entitySubType).notBlank(); // Person or Entity
        }

        final String fkColumnName = (apptableName != null) ? apptableName.substring(2) + "_id" : "";

        final JsonArray changeColumns = this.fromApiJsonHelper.extractJsonArrayNamed(CHANGE_COLUMNS, element);
        baseDataValidator.reset().parameter(CHANGE_COLUMNS).value(changeColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (changeColumns != null) {
            for (final JsonElement column : changeColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_CHANGE_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(NAME, column);
                baseDataValidator.reset().parameter(NAME).value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newName = this.fromApiJsonHelper.extractStringNamed(NEW_NAME, column);
                baseDataValidator.reset().parameter(NEW_NAME).value(newName).ignoreIfNull().notBlank().notExceedingLengthOf(50)
                        .isNotOneOfTheseValues("id", fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (this.fromApiJsonHelper.parameterExists(LENGTH, column)) {
                    final String lengthStr = this.fromApiJsonHelper.extractStringNamed(LENGTH, column);
                    if (StringUtils.isWhitespace(lengthStr) || !StringUtils.isNumeric(lengthStr) || StringUtils.isBlank(lengthStr)) {
                        baseDataValidator.reset().parameter(LENGTH).failWithCode("not.greater.than.zero");
                    } else {
                        final Integer length = Integer.parseInt(lengthStr);
                        baseDataValidator.reset().parameter(LENGTH).value(length).ignoreIfNull().notBlank().positiveAmount();
                    }
                }

                final String code = this.fromApiJsonHelper.extractStringNamed(CODE, column);
                baseDataValidator.reset().parameter(CODE).value(code).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newCode = this.fromApiJsonHelper.extractStringNamed(NEW_CODE, column);
                baseDataValidator.reset().parameter(NEW_CODE).value(newCode).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (StringUtils.isBlank(code) && StringUtils.isNotBlank(newCode)) {
                    baseDataValidator.reset().parameter(CODE).value(code).cantBeBlankWhenParameterProvidedIs(NEW_CODE, newCode);
                }

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(MANDATORY, column);
                baseDataValidator.reset().parameter(MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed(AFTER, column);
                baseDataValidator.reset().parameter(AFTER).value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(UNIQUE, column);
                baseDataValidator.reset().parameter(UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(INDEXED, column);
                baseDataValidator.reset().parameter(INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        final JsonArray addColumns = this.fromApiJsonHelper.extractJsonArrayNamed(ADD_COLUMNS, element);
        baseDataValidator.reset().parameter(ADD_COLUMNS).value(addColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (addColumns != null) {
            for (final JsonElement column : addColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_ADD_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(NAME, column);
                baseDataValidator.reset().parameter(NAME).value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(MANDATORY, column);
                baseDataValidator.reset().parameter(MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed(AFTER, column);
                baseDataValidator.reset().parameter(AFTER).value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(UNIQUE, column);
                baseDataValidator.reset().parameter(UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(INDEXED, column);
                baseDataValidator.reset().parameter(INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);
            }
        }

        final JsonArray dropColumns = this.fromApiJsonHelper.extractJsonArrayNamed(DROP_COLUMNS, element);
        baseDataValidator.reset().parameter(DROP_COLUMNS).value(dropColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (dropColumns != null) {
            for (final JsonElement column : dropColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_DROP_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(NAME, column);
                baseDataValidator.reset().parameter(NAME).value(name).notBlank().isNotOneOfTheseValues("id", fkColumnName)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
