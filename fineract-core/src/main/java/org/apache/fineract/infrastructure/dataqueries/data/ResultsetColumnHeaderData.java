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
package org.apache.fineract.infrastructure.dataqueries.data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseType;
import org.apache.fineract.infrastructure.core.service.database.JdbcJavaType;

/**
 * Immutable data object representing a resultset column.
 */
public final class ResultsetColumnHeaderData implements Serializable {

    private final String columnName;
    private JdbcJavaType columnType;
    private final Long columnLength;
    private final DisplayType columnDisplayType;
    private final boolean isColumnNullable;
    private final boolean isColumnPrimaryKey;
    private final boolean isColumnUnique;
    private final boolean isColumnIndexed;

    private final List<ResultsetColumnValueData> columnValues;
    private final String columnCode;

    public static ResultsetColumnHeaderData basic(final String columnName, final String columnType, DatabaseType dialect) {
        final Long columnLength = null;
        final boolean columnNullable = false;
        final boolean columnIsPrimaryKey = false;
        final List<ResultsetColumnValueData> columnValues = new ArrayList<>();
        final String columnCode = null;
        final boolean columnIsUnique = false;
        final boolean columnIsIndexed = false;
        return new ResultsetColumnHeaderData(columnName, columnType, columnLength, columnNullable, columnIsPrimaryKey, columnValues,
                columnCode, columnIsUnique, columnIsIndexed, dialect);
    }

    public static ResultsetColumnHeaderData detailed(final String columnName, final String columnType, final Long columnLength,
            final boolean columnNullable, final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues,
            final String columnCode, final boolean columnIsUnique, final boolean columnIsIndexed, DatabaseType dialect) {
        return new ResultsetColumnHeaderData(columnName, columnType, columnLength, columnNullable, columnIsPrimaryKey, columnValues,
                columnCode, columnIsUnique, columnIsIndexed, dialect);
    }

    private ResultsetColumnHeaderData(final String columnName, String columnType, final Long columnLength, final boolean columnNullable,
            final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues, final String columnCode,
            final boolean columnIsUnique, final boolean columnIsIndexed, DatabaseType dialect) {
        this.columnName = columnName;
        this.columnLength = columnLength;
        this.isColumnNullable = columnNullable;
        this.isColumnPrimaryKey = columnIsPrimaryKey;
        this.columnValues = columnValues;
        this.columnCode = columnCode;
        this.isColumnUnique = columnIsUnique;
        this.isColumnIndexed = columnIsIndexed;

        // Refer org.drizzle.jdbc.internal.mysql.MySQLType.java
        this.columnType = JdbcJavaType.getByTypeName(dialect, adjustColumnType(columnType), true);

        this.columnDisplayType = calcDisplayType();
    }

    public String getColumnName() {
        return this.columnName;
    }

    public boolean isNamed(final String columnName) {
        return this.columnName.equalsIgnoreCase(columnName);
    }

    public JdbcJavaType getColumnType() {
        return this.columnType;
    }

    public Long getColumnLength() {
        return this.columnLength;
    }

    public boolean getIsColumnNullable() {
        return isColumnNullable;
    }

    public boolean getIsColumnPrimaryKey() {
        return isColumnPrimaryKey;
    }

    public boolean getIsColumnUnique() {
        return isColumnUnique;
    }

    public boolean getIsColumnIndexed() {
        return isColumnIndexed;
    }

    public DisplayType getColumnDisplayType() {
        return this.columnDisplayType;
    }

    public String getColumnCode() {
        return this.columnCode;
    }

    public List<ResultsetColumnValueData> getColumnValues() {
        return this.columnValues;
    }

    public boolean isDateDisplayType() {
        return columnDisplayType == DisplayType.DATE;
    }

    public boolean isDateTimeDisplayType() {
        return columnDisplayType == DisplayType.DATETIME;
    }

    public boolean isTimeDisplayType() {
        return columnDisplayType == DisplayType.TIME;
    }

    public boolean isIntegerDisplayType() {
        return columnDisplayType == DisplayType.INTEGER;
    }

    public boolean isDecimalDisplayType() {
        return columnDisplayType == DisplayType.DECIMAL;
    }

    public boolean isStringDisplayType() {
        return columnDisplayType == DisplayType.STRING;
    }

    public boolean isTextDisplayType() {
        return columnDisplayType == DisplayType.TEXT;
    }

    public boolean isBooleanDisplayType() {
        return columnDisplayType == DisplayType.BOOLEAN;
    }

    public boolean isCodeValueDisplayType() {
        return columnDisplayType == DisplayType.CODEVALUE;
    }

    public boolean isCodeLookupDisplayType() {
        return columnDisplayType == DisplayType.CODELOOKUP;
    }

    public boolean isMandatory() {
        return !this.isColumnNullable;
    }

    public boolean hasColumnValues() {
        return columnValues != null && !columnValues.isEmpty();
    }

    public boolean isColumnValueAllowed(final String match) {
        for (final ResultsetColumnValueData allowedValue : this.columnValues) {
            if (allowedValue.matches(match)) {
                return true;
            }
        }
        return false;
    }

    public boolean isColumnCodeAllowed(final Integer match) {
        for (final ResultsetColumnValueData allowedValue : this.columnValues) {
            if (allowedValue.codeMatches(match)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPrecision(@NotNull DatabaseType dialect) {
        return columnType.hasPrecision(dialect);
    }

    // --- Calculation ---

    private String adjustColumnType(String type) {
        type = type.toUpperCase();
        switch (type) {
            case "CLOB":
            case "ENUM":
            case "SET":
                return "VARCHAR";
            case "NEWDECIMAL":
                return "DECIMAL";
            case "LONGLONG":
                return "BIGINT";
            case "SHORT":
                return "SMALLINT";
            case "TINY":
                return "TINYINT";
            case "INT24":
                return "INT";
            default:
                return type;
        }
    }

    @NotNull
    private DisplayType calcDisplayType() {
        DisplayType displayType = null;
        if (this.columnCode == null) {
            displayType = calcColumnDisplayType(columnType);
        } else {
            if (columnType.isIntegerType()) {
                displayType = DisplayType.CODELOOKUP;
            } else if (columnType.isVarcharType()) {
                displayType = DisplayType.CODEVALUE;
            }

        }
        if (displayType == null) {
            throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type",
                    "Invalid Lookup Type:" + this.columnType + " - Column Name: " + this.columnName);
        }
        return displayType;
    }

    public static DisplayType calcColumnDisplayType(JdbcJavaType columnType) {
        if (columnType.isTextType()) {
            return DisplayType.TEXT;
        }
        if (columnType.isStringType()) {
            return DisplayType.STRING;
        }
        if (columnType.isAnyIntegerType()) {
            return DisplayType.INTEGER;
        }
        if (columnType.isAnyFloatType()) {
            return DisplayType.FLOAT;
        }
        if (columnType.isDecimalType()) { // Refer org.drizzle.jdbc.internal.mysql.MySQLType.java
            return DisplayType.DECIMAL;
        }
        if (columnType.isDateType()) {
            return DisplayType.DATE;
        }
        if (columnType.isDateTimeType()) {
            return DisplayType.DATETIME;
        }
        if (columnType.isTimeType()) {
            return DisplayType.TIME;
        }
        if (columnType.isBooleanType()) {
            return DisplayType.BOOLEAN;
        }
        if (columnType.isBinaryType()) {
            return DisplayType.BINARY;
        }
        return null;
    }

    public enum DisplayType {
        TEXT, STRING, INTEGER, FLOAT, DECIMAL, DATE, TIME, DATETIME, BOOLEAN, BINARY, CODELOOKUP, CODEVALUE,;
    }
}
