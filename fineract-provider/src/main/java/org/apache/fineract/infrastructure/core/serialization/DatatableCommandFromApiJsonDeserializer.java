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

import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.BOOLEAN;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.DATE;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.DATETIME;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.DECIMAL;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.INTEGER;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.TEXT;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.TIMESTAMP;
import static org.apache.fineract.infrastructure.core.service.database.JdbcJavaType.VARCHAR;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_AFTER;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_CODE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_INDEXED;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_LENGTH;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_MANDATORY;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NEWCODE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NEWNAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_BOOLEAN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DATE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DATETIME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DECIMAL;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_DROPDOWN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_NUMBER;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_STRING;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_TEXT;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_TYPE_TIMESTAMP;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_UNIQUE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_ADDCOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_APPTABLE_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_CHANGECOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_COLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_DATATABLE_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_DROPCOLUMNS;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_MULTIROW;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_PARAM_SUBTYPE;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_FIELD_ID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseTypeResolver;
import org.apache.fineract.infrastructure.core.service.database.JdbcJavaType;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatatableCommandFromApiJsonDeserializer {

    public static final String DATATABLE_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,48}[a-zA-Z0-9]$";
    public static final String DATATABLE_COLUMN_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,}[a-zA-Z0-9]$";

    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CREATE = Set.of(API_PARAM_DATATABLE_NAME, API_PARAM_SUBTYPE,
            API_PARAM_APPTABLE_NAME, API_PARAM_MULTIROW, API_PARAM_COLUMNS);
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CREATE_COLUMNS = Set.of(API_FIELD_NAME, API_FIELD_TYPE, API_FIELD_LENGTH,
            API_FIELD_MANDATORY, API_FIELD_CODE, API_FIELD_UNIQUE, API_FIELD_INDEXED);
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_UPDATE = Set.of(API_PARAM_APPTABLE_NAME, API_PARAM_SUBTYPE,
            API_PARAM_CHANGECOLUMNS, API_PARAM_ADDCOLUMNS, API_PARAM_DROPCOLUMNS);
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_ADD_COLUMNS = Set.of(API_FIELD_NAME, API_FIELD_TYPE, API_FIELD_LENGTH,
            API_FIELD_MANDATORY, API_FIELD_AFTER, API_FIELD_CODE, API_FIELD_UNIQUE, API_FIELD_INDEXED);
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_CHANGE_COLUMNS = Set.of(API_FIELD_NAME, API_FIELD_NEWNAME, API_FIELD_LENGTH,
            API_FIELD_MANDATORY, API_FIELD_AFTER, API_FIELD_CODE, API_FIELD_NEWCODE, API_FIELD_UNIQUE, API_FIELD_INDEXED);
    private static final Set<String> SUPPORTED_PARAMETERS_FOR_DROP_COLUMNS = Set.of(API_FIELD_NAME);
    private static final Object[] SUPPORTED_COLUMN_TYPES = { API_FIELD_TYPE_STRING, API_FIELD_TYPE_NUMBER, API_FIELD_TYPE_BOOLEAN,
            API_FIELD_TYPE_DECIMAL, API_FIELD_TYPE_DATE, API_FIELD_TYPE_DATETIME, API_FIELD_TYPE_TEXT, API_FIELD_TYPE_DROPDOWN };

    private final FromJsonHelper fromApiJsonHelper;
    private final DatabaseTypeResolver databaseTypeResolver;

    @Autowired
    public DatatableCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper, DatabaseTypeResolver databaseTypeResolver) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.databaseTypeResolver = databaseTypeResolver;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS_FOR_CREATE);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String datatableName = this.fromApiJsonHelper.extractStringNamed(API_PARAM_DATATABLE_NAME, element);
        baseDataValidator.reset().parameter(API_PARAM_DATATABLE_NAME).value(datatableName).notBlank().notExceedingLengthOf(50)
                .matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);

        final String apptableName = this.fromApiJsonHelper.extractStringNamed(API_PARAM_APPTABLE_NAME, element);
        baseDataValidator.reset().parameter(API_PARAM_APPTABLE_NAME).value(apptableName).notBlank().notExceedingLengthOf(50)
                .isOneOfTheseStringValues(EntityTables.getEntityNames());

        EntityTables entityTable = EntityTables.fromEntityName(apptableName);
        validateEntitySubType(baseDataValidator, element, entityTable);

        final String fkColumnName = entityTable.getForeignKeyColumnNameOnDatatable();

        final Boolean multiRow = this.fromApiJsonHelper.extractBooleanNamed(API_PARAM_MULTIROW, element);
        baseDataValidator.reset().parameter(API_PARAM_MULTIROW).value(multiRow).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

        final JsonArray columns = this.fromApiJsonHelper.extractJsonArrayNamed(API_PARAM_COLUMNS, element);
        baseDataValidator.reset().parameter(API_PARAM_COLUMNS).value(columns).notNull().jsonArrayNotEmpty();

        if (columns != null) {
            for (final JsonElement column : columns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_CREATE_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NAME, column);
                baseDataValidator.reset().parameter(API_FIELD_NAME).value(name).notBlank()
                        .isNotOneOfTheseValues(TABLE_FIELD_ID, fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_MANDATORY, column);
                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_UNIQUE, column);
                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_INDEXED, column);
                baseDataValidator.reset().parameter(API_FIELD_MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);
                baseDataValidator.reset().parameter(API_FIELD_UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);
                baseDataValidator.reset().parameter(API_FIELD_INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);
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
        final String apptableName = this.fromApiJsonHelper.extractStringNamed(API_PARAM_APPTABLE_NAME, element);
        baseDataValidator.reset().parameter(API_PARAM_APPTABLE_NAME).value(apptableName).ignoreIfNull().notBlank()
                .isOneOfTheseStringValues(EntityTables.getEntityNames());

        EntityTables entityTable = EntityTables.fromEntityName(apptableName);
        validateEntitySubType(baseDataValidator, element, entityTable);

        final String fkColumnName = entityTable.getForeignKeyColumnNameOnDatatable();

        final JsonArray changeColumns = this.fromApiJsonHelper.extractJsonArrayNamed(API_PARAM_CHANGECOLUMNS, element);
        baseDataValidator.reset().parameter(API_PARAM_CHANGECOLUMNS).value(changeColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (changeColumns != null) {
            for (final JsonElement column : changeColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_CHANGE_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NAME, column);
                baseDataValidator.reset().parameter(API_FIELD_NAME).value(name).notBlank()
                        .isNotOneOfTheseValues(TABLE_FIELD_ID, fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newName = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NEWNAME, column);
                baseDataValidator.reset().parameter(API_FIELD_NEWNAME).value(newName).ignoreIfNull().notBlank().notExceedingLengthOf(50)
                        .isNotOneOfTheseValues(TABLE_FIELD_ID, fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (this.fromApiJsonHelper.parameterExists(API_FIELD_LENGTH, column)) {
                    final String lengthStr = this.fromApiJsonHelper.extractStringNamed(API_FIELD_LENGTH, column);
                    if (StringUtils.isWhitespace(lengthStr) || !StringUtils.isNumeric(lengthStr) || StringUtils.isBlank(lengthStr)) {
                        baseDataValidator.reset().parameter(API_FIELD_LENGTH).failWithCode("not.greater.than.zero");
                    } else {
                        final Integer length = Integer.parseInt(lengthStr);
                        baseDataValidator.reset().parameter(API_FIELD_LENGTH).value(length).ignoreIfNull().notBlank().positiveAmount();
                    }
                }

                final String code = this.fromApiJsonHelper.extractStringNamed(API_FIELD_CODE, column);
                baseDataValidator.reset().parameter(API_FIELD_CODE).value(code).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                final String newCode = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NEWCODE, column);
                baseDataValidator.reset().parameter(API_FIELD_NEWCODE).value(newCode).ignoreIfNull().notBlank().notExceedingLengthOf(100)
                        .matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                if (StringUtils.isBlank(code) && StringUtils.isNotBlank(newCode)) {
                    baseDataValidator.reset().parameter(API_FIELD_CODE).value(code).cantBeBlankWhenParameterProvidedIs(API_FIELD_NEWCODE,
                            newCode);
                }

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_MANDATORY, column);
                baseDataValidator.reset().parameter(API_FIELD_MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_AFTER, column);
                baseDataValidator.reset().parameter(API_FIELD_AFTER).value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_UNIQUE, column);
                baseDataValidator.reset().parameter(API_FIELD_UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);

                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_INDEXED, column);
                baseDataValidator.reset().parameter(API_FIELD_INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);
            }
        }

        final JsonArray addColumns = this.fromApiJsonHelper.extractJsonArrayNamed(API_PARAM_ADDCOLUMNS, element);
        baseDataValidator.reset().parameter(API_PARAM_ADDCOLUMNS).value(addColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (addColumns != null) {
            for (final JsonElement column : addColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_ADD_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NAME, column);
                baseDataValidator.reset().parameter(API_FIELD_NAME).value(name).notBlank()
                        .isNotOneOfTheseValues(TABLE_FIELD_ID, fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);

                validateType(baseDataValidator, column);

                final Boolean mandatory = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_MANDATORY, column);
                baseDataValidator.reset().parameter(API_FIELD_MANDATORY).value(mandatory).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);

                final Boolean after = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_AFTER, column);
                baseDataValidator.reset().parameter(API_FIELD_AFTER).value(after).ignoreIfNull().notBlank().isOneOfTheseValues(true, false);

                final Boolean unique = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_UNIQUE, column);
                baseDataValidator.reset().parameter(API_FIELD_UNIQUE).value(unique).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);

                final Boolean indexed = this.fromApiJsonHelper.extractBooleanNamed(API_FIELD_INDEXED, column);
                baseDataValidator.reset().parameter(API_FIELD_INDEXED).value(indexed).ignoreIfNull().notBlank().isOneOfTheseValues(true,
                        false);
            }
        }

        final JsonArray dropColumns = this.fromApiJsonHelper.extractJsonArrayNamed(API_PARAM_DROPCOLUMNS, element);
        baseDataValidator.reset().parameter(API_PARAM_DROPCOLUMNS).value(dropColumns).ignoreIfNull().jsonArrayNotEmpty();

        if (dropColumns != null) {
            for (final JsonElement column : dropColumns) {
                this.fromApiJsonHelper.checkForUnsupportedParameters(column.getAsJsonObject(), SUPPORTED_PARAMETERS_FOR_DROP_COLUMNS);

                final String name = this.fromApiJsonHelper.extractStringNamed(API_FIELD_NAME, column);
                baseDataValidator.reset().parameter(API_FIELD_NAME).value(name).notBlank()
                        .isNotOneOfTheseValues(TABLE_FIELD_ID, fkColumnName).matchesRegularExpression(DATATABLE_COLUMN_NAME_REGEX_PATTERN);
            }
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void validateType(DataValidatorBuilder validator, final JsonElement column) {
        final String type = this.fromApiJsonHelper.extractStringNamed(API_FIELD_TYPE, column);
        validator.reset().parameter(API_FIELD_TYPE).value(type).notBlank().isOneOfTheseStringValues(SUPPORTED_COLUMN_TYPES);
        if (type == null) {
            return;
        }
        final Integer length = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(API_FIELD_LENGTH, column);
        JdbcJavaType jdbcType = mapApiTypeToJdbcType(type);
        validator = validator.reset().parameter(API_FIELD_LENGTH).value(length);
        if (jdbcType.hasPrecision(databaseTypeResolver.databaseType())) {
            if (jdbcType.isStringType() && length == null) {
                validator.failWithCode("must.be.provided.when.type.is.String");
            }
            validator.ignoreIfNull().positiveAmount();
        } // else, the precision is ignored

        final String code = this.fromApiJsonHelper.extractStringNamed(API_FIELD_CODE, column);
        if (type.equalsIgnoreCase(API_FIELD_TYPE_DROPDOWN)) {
            if (code != null) {
                validator.reset().parameter(API_FIELD_CODE).value(code).notBlank().matchesRegularExpression(DATATABLE_NAME_REGEX_PATTERN);
            } else {
                validator.reset().parameter(API_FIELD_CODE).value(code).cantBeBlankWhenParameterProvidedIs(API_FIELD_TYPE, type);
            }
        } else {
            validator.reset().parameter(API_FIELD_CODE).value(code).mustBeBlankWhenParameterProvided(API_FIELD_TYPE, type);
        }
    }

    @NotNull
    public static JdbcJavaType mapApiTypeToJdbcType(@NotNull String apiType) {
        return switch (apiType.toLowerCase()) {
            case API_FIELD_TYPE_STRING -> VARCHAR;
            case API_FIELD_TYPE_NUMBER, API_FIELD_TYPE_DROPDOWN -> INTEGER;
            case API_FIELD_TYPE_BOOLEAN -> BOOLEAN;
            case API_FIELD_TYPE_DECIMAL -> DECIMAL;
            case API_FIELD_TYPE_DATE -> DATE;
            case API_FIELD_TYPE_DATETIME -> DATETIME;
            case API_FIELD_TYPE_TIMESTAMP -> TIMESTAMP;
            case API_FIELD_TYPE_TEXT -> TEXT;
            default -> throw new PlatformDataIntegrityException("error.msg.datatable.column.type.invalid",
                    "Column type " + apiType + " is not supported.");
        };
    }

    private void validateEntitySubType(final DataValidatorBuilder baseDataValidator, final JsonElement element,
            final EntityTables entityTable) {
        if (entityTable == EntityTables.CLIENT) {
            String entitySubType = this.fromApiJsonHelper.extractStringNamed(API_PARAM_SUBTYPE, element);
            baseDataValidator.reset().parameter(API_PARAM_SUBTYPE).value(entitySubType).notBlank(); // Person or Entity
        }
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
