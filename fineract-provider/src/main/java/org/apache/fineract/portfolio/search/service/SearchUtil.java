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
package org.apache.fineract.portfolio.search.service;

import static java.util.Locale.ENGLISH;
import static org.apache.fineract.infrastructure.core.data.ApiParameterError.parameterErrorWithValue;
import static org.apache.fineract.infrastructure.core.service.DateUtils.DEFAULT_DATETIME_FORMAT;
import static org.apache.fineract.infrastructure.core.service.DateUtils.DEFAULT_DATE_FORMAT;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_MANDATORY;
import static org.apache.fineract.portfolio.search.SearchConstants.API_PARAM_COLUMN;

import com.google.common.base.Predicate;
import com.google.gson.JsonObject;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.core.service.database.JdbcJavaType;
import org.apache.fineract.infrastructure.core.service.database.SqlOperator;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.security.utils.SQLInjectionValidator;
import org.apache.fineract.portfolio.search.data.ColumnFilterData;
import org.apache.fineract.portfolio.search.data.FilterData;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public final class SearchUtil {

    public static final int DEFAULT_PAGE_SIZE = 50;

    private static final JsonParserHelper helper = new JsonParserHelper();

    private SearchUtil() {}

    @NotNull
    public static Map<String, ResultsetColumnHeaderData> mapHeadersToName(@NotNull Collection<ResultsetColumnHeaderData> columnHeaders) {
        return columnHeaders.stream().collect(Collectors.toMap(ResultsetColumnHeaderData::getColumnName, e -> e));
    }

    public static ResultsetColumnHeaderData findFiltered(@NotNull Collection<ResultsetColumnHeaderData> columnHeaders,
            @NotNull Predicate<ResultsetColumnHeaderData> filter) {
        return columnHeaders.stream().filter(filter).findFirst().orElse(null);
    }

    public static ResultsetColumnHeaderData getFiltered(@NotNull Collection<ResultsetColumnHeaderData> columnHeaders,
            @NotNull Predicate<ResultsetColumnHeaderData> filter) {
        ResultsetColumnHeaderData filtered = findFiltered(columnHeaders, filter);
        if (filtered == null) {
            throw new PlatformDataIntegrityException("error.msg.column.not.exists", "Column filtered does not exist");
        }
        return filtered;
    }

    public static void extractJsonResult(@NotNull SqlRowSet rowSet, @NotNull List<String> selectColumns,
            @NotNull List<String> resultColumns, @NotNull List<JsonObject> results) {
        JsonObject json = new JsonObject();
        for (int i = 0; i < selectColumns.size(); i++) {
            String sCol = selectColumns.get(i).replaceAll("\"", "");
            Object rowValue = rowSet.getObject(sCol);
            if (rowValue != null) {
                String rCol = resultColumns.get(i);
                if (rowValue instanceof Character) {
                    json.addProperty(rCol, (Character) rowValue);
                } else if (rowValue instanceof Number) {
                    json.addProperty(rCol, new BigDecimal(rowValue.toString()));
                } else if (rowValue instanceof Boolean) {
                    json.addProperty(rCol, (Boolean) rowValue);
                } else if (rowValue instanceof LocalDateTime) {
                    json.addProperty(rCol, DateUtils.format((LocalDateTime) rowValue));
                } else if (rowValue instanceof Timestamp) {
                    json.addProperty(rCol, DateUtils.format(((Timestamp) rowValue).toLocalDateTime()));
                } else if (rowValue instanceof LocalDate) {
                    json.addProperty(rCol, DateUtils.format((LocalDate) rowValue));
                } else if (rowValue instanceof Date) {
                    json.addProperty(rCol, DateUtils.format(((Date) rowValue).toLocalDate()));
                } else {
                    json.addProperty(rCol, rowValue.toString());
                }
            }
        }

        if (json.size() > 0) {
            results.add(json);
        }
    }

    @NotNull
    public static List<String> validateToJdbcColumns(List<String> columns, Map<String, ResultsetColumnHeaderData> columnHeaders,
            List<ApiParameterError> errors, boolean allowEmpty) {
        List<String> result = new ArrayList<>();
        if (columns == null || columns.isEmpty()) {
            if (!allowEmpty) {
                errors.add(parameterErrorWithValue("error.msg.columns.empty", "Columns list is empty", API_PARAM_COLUMN, null));
            }
        } else {
            columns.forEach(rcn -> result.add(validateToJdbcColumn(rcn, columnHeaders, errors, allowEmpty)));
        }
        return result;
    }

    public static String validateToJdbcColumn(String column, Map<String, ResultsetColumnHeaderData> columnHeaders,
            List<ApiParameterError> errors, boolean allowEmpty) {
        if (!allowEmpty && column == null) {
            errors.add(parameterErrorWithValue("error.msg.column.empty", "Column filter is empty", API_PARAM_COLUMN, null));
        }
        if (column != null) {
            if (!columnHeaders.containsKey(column)) {
                column = camelToSnake(column);
                if (!columnHeaders.containsKey(column)) {
                    errors.add(
                            parameterErrorWithValue("error.msg.invalid.column", "Column not exist in database", API_PARAM_COLUMN, column));
                }
            }
        }
        return column;
    }

    public static boolean buildQueryCondition(List<ColumnFilterData> columnFilters, @NotNull StringBuilder where,
            @NotNull List<Object> params, String alias, Map<String, ResultsetColumnHeaderData> columnHeaders, boolean embedded,
            boolean strict, @NotNull DatabaseSpecificSQLGenerator sqlGenerator) {
        if (columnFilters == null) {
            return false;
        }
        boolean added = false;
        int isize = columnFilters.size();
        for (int i = 0; i < isize; i++) {
            boolean addedFilter = buildFilterCondition(columnFilters.get(i), where, params, alias, columnHeaders, embedded, strict,
                    sqlGenerator);
            if (addedFilter && i < isize - 1) {
                where.append(" AND ");
            }
            added |= addedFilter;
        }
        return added;
    }

    public static boolean buildFilterCondition(ColumnFilterData columnFilter, @NotNull StringBuilder where, @NotNull List<Object> params,
            String alias, Map<String, ResultsetColumnHeaderData> columnHeaders, boolean embedded, boolean strict,
            @NotNull DatabaseSpecificSQLGenerator sqlGenerator) {
        String columnName = columnFilter.getColumn();
        List<FilterData> filters = columnFilter.getFilters();
        int size = filters.size();
        for (int i = 0; i < size; i++) {
            if (!embedded && where.isEmpty()) {
                where.append(" WHERE ");
            }
            FilterData filter = filters.get(i);
            SqlOperator operator = filter.getOperator();
            List<String> values = filter.getValues();
            List<Object> objectValues = values == null ? null
                    : values.stream().map(e -> parseAndValidateJdbcColumnValue(columnName, e, columnHeaders, strict, sqlGenerator))
                            .toList();

            operator.validateValues(values);
            if (operator.isPlaceholderSupported()) {
                where.append(operator.formatPlaceholder(sqlGenerator, columnName, alias));
                if (objectValues != null) {
                    if (operator.isListType()) {
                        params.add(objectValues);
                    } else {
                        params.addAll(objectValues);
                    }
                }
            } else {
                where.append(operator.formatSql(sqlGenerator, columnHeaders.get(columnName).getColumnType(), columnName, alias, values));
            }
            if (i < size - 1) {
                where.append(" AND ");
            }
        }
        return size > 0;
    }

    public static Object parseAndValidateJdbcColumnValue(String column, String columnValue,
            Map<String, ResultsetColumnHeaderData> columnHeaders, boolean strict, @NotNull DatabaseSpecificSQLGenerator sqlGenerator) {
        List<ApiParameterError> errors = new ArrayList<>();
        column = validateToJdbcColumn(column, columnHeaders, errors, false);
        if (!errors.isEmpty()) {
            throw new PlatformApiDataValidationException(errors);
        }
        ResultsetColumnHeaderData columnHeader = columnHeaders.get(column);
        String dateFormat = columnHeader.getColumnType().isDateTimeType() ? DEFAULT_DATETIME_FORMAT : DEFAULT_DATE_FORMAT;
        return columnHeader.getColumnType().toJdbcValue(sqlGenerator.getDialect(),
                parseAndValidateColumnValue(columnHeader, columnValue, dateFormat, ENGLISH, strict, sqlGenerator), false);
    }

    public static Object parseAndValidateColumnValue(final ResultsetColumnHeaderData columnHeader, final String pValue,
            final String dateFormat, final Locale locale, boolean strict, @NotNull DatabaseSpecificSQLGenerator sqlGenerator) {
        String columnValue = pValue;
        JdbcJavaType colType = columnHeader.getColumnType();
        if (!colType.isStringType() || !columnHeader.isMandatory()) {
            columnValue = StringUtils.trimToNull(columnValue);
        }
        String errorCode = "validation.msg.validation.errors.exist";
        String errorMsg = "Validation errors exist.";
        if (columnValue == null && columnHeader.isMandatory()) {
            ApiParameterError error = ApiParameterError.parameterError("error.msg.column.mandatory", API_FIELD_MANDATORY,
                    columnHeader.getColumnName());
            throw new PlatformApiDataValidationException(errorCode, errorMsg, List.of(error));
        }
        if (StringUtils.isEmpty(columnValue)) {
            return columnValue;
        }
        if (strict) {
            SQLInjectionValidator.validateDynamicQuery(columnValue);
        }

        if (columnHeader.hasColumnValues()) {
            if (columnHeader.isCodeValueDisplayType()) {
                if (!columnHeader.isColumnValueAllowed(columnValue)) {
                    ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                            "Value not found in Allowed Value list", columnHeader.getColumnName(), columnValue);
                    throw new PlatformApiDataValidationException(errorCode, errorMsg, List.of(error));
                }
                return columnValue;
            } else if (columnHeader.isCodeLookupDisplayType()) {
                final Integer codeLookup = Integer.valueOf(columnValue);
                if (!columnHeader.isColumnCodeAllowed(codeLookup)) {
                    ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                            "Value not found in Allowed Value list", columnHeader.getColumnName(), columnValue);
                    throw new PlatformApiDataValidationException(errorCode, errorMsg, List.of(error));
                }
                return codeLookup;
            } else {
                throw new PlatformDataIntegrityException("error.msg.invalid.columnType.", "Code: " + columnHeader.getColumnName()
                        + " - Invalid Type " + colType.getJdbcName(sqlGenerator.getDialect()) + " (neither varchar nor int)");
            }
        }

        if (colType.isDateType()) {
            return JsonParserHelper.convertFrom(columnValue, columnHeader.getColumnName(), dateFormat, locale);
        }
        if (colType.isDateTimeType()) {
            return JsonParserHelper.convertDateTimeFrom(columnValue, columnHeader.getColumnName(), dateFormat, locale);
        }
        if (colType.isAnyIntegerType()) {
            return helper.convertToInteger(columnValue, columnHeader.getColumnName(), locale);
        }
        if (colType.isDecimalType()) {
            return helper.convertFrom(columnValue, columnHeader.getColumnName(), locale);
        }
        if (colType.isBooleanType()) {
            final Boolean boolValue = BooleanUtils.toBooleanObject(columnValue);
            if (boolValue == null) {
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.boolean.format",
                        "The parameter " + columnHeader.getColumnName() + " has value: " + columnValue + " which is invalid boolean value.",
                        columnHeader.getColumnName(), columnValue);
                throw new PlatformApiDataValidationException(errorCode, errorMsg, List.of(error));
            }
            return boolValue;
        }
        if (colType.isStringType()) {
            if (columnHeader.getColumnLength() > 0 && columnValue.length() > columnHeader.getColumnLength()) {
                ApiParameterError error = ApiParameterError.parameterError("validation.msg.datatable.entry.column.exceeds.maxlength",
                        "The column `" + columnHeader.getColumnName() + "` exceeds its defined max-length ", columnHeader.getColumnName(),
                        columnValue);
                throw new PlatformApiDataValidationException(errorCode, errorMsg, List.of(error));
            }
        }
        return columnValue;
    }

    public static String camelToSnake(final String camelStr) {
        return camelStr == null ? null
                : camelStr.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
