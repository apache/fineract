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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;

/**
 * Immutable data object representing a resultset column.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class ResultsetColumnHeaderData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String columnName;
    private String columnType;
    private Long columnLength;
    private String columnDisplayType;
    private boolean isColumnNullable;
    private boolean isColumnPrimaryKey;

    private List<ResultsetColumnValueData> columnValues;
    private String columnCode;

    public static ResultsetColumnHeaderData basic(final String columnName, final String columnType) {

        final Long columnLength = null;
        final boolean columnNullable = false;
        final boolean columnIsPrimaryKey = false;
        final List<ResultsetColumnValueData> columnValues = new ArrayList<>();
        final String columnCode = null;
        return new ResultsetColumnHeaderData().setColumnName(columnName).setColumnType(columnType).setColumnLength(columnLength)
                .setColumnNullable(columnNullable).setColumnPrimaryKey(columnIsPrimaryKey).setColumnValues(columnValues)
                .setColumnCode(columnCode);
    }

    public static ResultsetColumnHeaderData detailed(final String columnName, final String columnType, final Long columnLength,
            final boolean columnNullable, final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues,
            final String columnCode) {
        return new ResultsetColumnHeaderData(columnName, columnType, columnLength, columnNullable, columnIsPrimaryKey, columnValues,
                columnCode);
    }

    private ResultsetColumnHeaderData(final String columnName, final String columnType, final Long columnLength,
            final boolean columnNullable, final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues,
            final String columnCode) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnLength = columnLength;
        this.isColumnNullable = columnNullable;
        this.isColumnPrimaryKey = columnIsPrimaryKey;
        this.columnValues = columnValues;
        this.columnCode = columnCode;

        // Refer org.drizzle.jdbc.internal.mysql.MySQLType.java
        adjustColumnTypes();

        String displayType = null;
        if (this.columnCode == null) {
            if (isString()) {
                displayType = "STRING";
            } else if (isAnyInteger()) {
                if (isInteger()) {
                    this.columnType = this.columnType.toUpperCase();
                }
                displayType = "INTEGER";
            } else if (isDate()) {
                displayType = "DATE";
            } else if (isDateTime()) {
                displayType = "DATETIME";
            } else if (isDecimal()) {
                displayType = "DECIMAL";
            } else if (isAnyText()) {
                displayType = "TEXT";
            } else if (isBit()) {
                displayType = "BOOLEAN";
            } else {
                throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type",
                        "Invalid Lookup Type:" + this.columnType + " - Column Name: " + this.columnName);
            }

        } else {
            if (isInt() || isInteger()) {
                displayType = "CODELOOKUP";
            } else if (isVarchar()) {
                displayType = "CODEVALUE";
            } else {
                throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type",
                        "Invalid Lookup Type:" + this.columnType + " - Column Name: " + this.columnName);
            }
        }

        this.columnDisplayType = displayType;
    }

    private void adjustColumnTypes() {
        switch (this.columnType) {
            case "NEWDECIMAL":
                this.columnType = "DECIMAL";
            break;
            case "CLOB":
            case "ENUM":
            case "SET":
                this.columnType = "varchar";
            break;
            case "LONGLONG":
                this.columnType = "bigint";
            break;
            case "SHORT":
                this.columnType = "smallint";
            break;
            case "TINY":
                this.columnType = "tinyint";
            break;
            case "INT24":
                this.columnType = "int";
            break;
            default:
            break;
        }
    }

    private boolean isAnyText() {
        return isText() || isTinyText() || isMediumText() || isLongText();
    }

    private boolean isText() {
        return "text".equalsIgnoreCase(this.columnType);
    }

    private boolean isTinyText() {
        return "tinytext".equalsIgnoreCase(this.columnType);
    }

    private boolean isMediumText() {
        return "mediumtext".equalsIgnoreCase(this.columnType);
    }

    private boolean isLongText() {
        return "longtext".equalsIgnoreCase(this.columnType);
    }

    private boolean isDecimal() {
        return "decimal".equalsIgnoreCase(this.columnType) || "NEWDECIMAL".equalsIgnoreCase(this.columnType)
                || "numeric".equalsIgnoreCase(this.columnType);
        // Refer org.drizzle.jdbc.internal.mysql.MySQLType.java
    }

    private boolean isDate() {
        return "date".equalsIgnoreCase(this.columnType);
    }

    private boolean isDateTime() {
        return "datetime".equalsIgnoreCase(this.columnType) || "timestamp without time zone".equalsIgnoreCase(this.columnType)
                || "timestamptz".equalsIgnoreCase(this.columnType) || "timestamp with time zone".equalsIgnoreCase(this.columnType);
    }

    public boolean isString() {
        return isVarchar() || isChar();
    }

    private boolean isChar() {
        return "char".equalsIgnoreCase(this.columnType) || "CHARACTER VARYING".equalsIgnoreCase(this.columnType)
                || "bpchar".equalsIgnoreCase(this.columnType);
    }

    private boolean isVarchar() {
        return "varchar".equalsIgnoreCase(this.columnType);
    }

    private boolean isAnyInteger() {
        return isInt() || isInteger() || isSmallInt() || isTinyInt() || isMediumInt() || isBigInt() || isLong() || isSerial();
    }

    private boolean isSerial() {
        return "SERIAL".equalsIgnoreCase(this.columnType) || "SERIAL4".equalsIgnoreCase(this.columnType)
                || "SERIAL8".equalsIgnoreCase(this.columnType) || "SMALLSERIAL".equalsIgnoreCase(this.columnType)
                || "BIGSERIAL".equalsIgnoreCase(this.columnType);
    }

    private boolean isInt() {
        return "int".equalsIgnoreCase(this.columnType);
    }

    private boolean isInteger() {
        return "integer".equalsIgnoreCase(this.columnType) || "int2".equalsIgnoreCase(this.columnType)
                || "int4".equalsIgnoreCase(this.columnType);
    }

    private boolean isSmallInt() {
        return "smallint".equalsIgnoreCase(this.columnType);
    }

    private boolean isTinyInt() {
        return "tinyint".equalsIgnoreCase(this.columnType);
    }

    private boolean isMediumInt() {
        return "mediumint".equalsIgnoreCase(this.columnType);
    }

    private boolean isBigInt() {
        return "bigint".equalsIgnoreCase(this.columnType) || "int8".equalsIgnoreCase(this.columnType);
    }

    private boolean isLong() {
        return "LONG".equalsIgnoreCase(this.columnType) || "LONGLONG".equalsIgnoreCase(this.columnType);
        // Refer org.drizzle.jdbc.internal.mysql.MySQLType.java
    }

    private boolean isBit() {
        return "bit".equalsIgnoreCase(this.columnType);
    }

    public boolean isDateDisplayType() {
        return "DATE".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isDateTimeDisplayType() {
        return "DATETIME".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isIntegerDisplayType() {
        return "INTEGER".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isDecimalDisplayType() {
        return "DECIMAL".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isBooleanDisplayType() {
        return "BOOLEAN".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isCodeValueDisplayType() {
        return "CODEVALUE".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isMandatory() {
        return !isOptional();
    }

    public boolean isOptional() {
        return this.isColumnNullable;
    }

    public boolean hasColumnValues() {
        return !this.columnValues.isEmpty();
    }

    public boolean isCodeLookupDisplayType() {
        return "CODELOOKUP".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isColumnValueAllowed(final String match) {
        boolean allowed = false;
        for (final ResultsetColumnValueData allowedValue : this.columnValues) {
            if (allowedValue.matches(match)) {
                allowed = true;
            }
        }
        return allowed;
    }

    public boolean isColumnValueNotAllowed(final String match) {
        return !isColumnValueAllowed(match);
    }

    public boolean isColumnCodeNotAllowed(final Integer match) {
        return !isColumnCodeAllowed(match);
    }

    public boolean isColumnCodeAllowed(final Integer match) {
        boolean allowed = false;
        for (final ResultsetColumnValueData allowedValue : this.columnValues) {
            if (allowedValue.codeMatches(match)) {
                allowed = true;
            }
        }
        return allowed;
    }
}
