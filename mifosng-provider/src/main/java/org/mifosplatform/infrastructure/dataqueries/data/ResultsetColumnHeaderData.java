/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;

/**
 * Immutable data object representing a resultset column.
 */
public final class ResultsetColumnHeaderData {

    private final String columnName;
    private final String columnType;
    @SuppressWarnings("unused")
    private final Long columnLength;
    private final String columnDisplayType;
    private final boolean isColumnNullable;
    @SuppressWarnings("unused")
    private final boolean isColumnPrimaryKey;

    private final List<ResultsetColumnValueData> columnValues;

    public static ResultsetColumnHeaderData basic(final String columnName, final String columnType) {

        final Long columnLength = null;
        final boolean columnNullable = false;
        final boolean columnIsPrimaryKey = false;
        final List<ResultsetColumnValueData> columnValues = new ArrayList<ResultsetColumnValueData>();
        return new ResultsetColumnHeaderData(columnName, columnType, columnLength, columnNullable, columnIsPrimaryKey, columnValues);
    }

    public static ResultsetColumnHeaderData detailed(final String columnName, final String columnType, final Long columnLength,
            final boolean columnNullable, final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues) {
        return new ResultsetColumnHeaderData(columnName, columnType, columnLength, columnNullable, columnIsPrimaryKey, columnValues);
    }

    private ResultsetColumnHeaderData(final String columnName, final String columnType, final Long columnLength,
            final boolean columnNullable, final boolean columnIsPrimaryKey, final List<ResultsetColumnValueData> columnValues) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.columnLength = columnLength;
        this.isColumnNullable = columnNullable;
        this.isColumnPrimaryKey = columnIsPrimaryKey;
        this.columnValues = columnValues;

        String displayType = null;
        if (this.columnValues.isEmpty()) {
            if (isString()) {
                displayType = "STRING";
            } else if (isAnyInteger()) {
                displayType = "INTEGER";
            } else if (isDate()) {
                displayType = "DATE";
            } else if (isDecimal()) {
                displayType = "DECIMAL";
            } else if (isAnyText()) {
                displayType = "TEXT";
            } else {
                throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type", "Invalid Lookup Type:" + this.columnType
                        + " - Column Name: " + this.columnName);
            }

        } else {
            if (isInt()) {
                displayType = "CODELOOKUP";
            } else if (isVarchar()) {
                displayType = "CODEVALUE";
            } else {
                throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type", "Invalid Lookup Type:" + this.columnType
                        + " - Column Name: " + this.columnName);
            }
        }

        this.columnDisplayType = displayType;
    }

    public boolean isNamed(final String columnName) {
        return this.columnName.equalsIgnoreCase(columnName);
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
        return "decimal".equalsIgnoreCase(this.columnType);
    }

    private boolean isDate() {
        return "date".equalsIgnoreCase(this.columnType);
    }

    private boolean isString() {
        return isVarchar() || isChar();
    }

    private boolean isChar() {
        return "char".equalsIgnoreCase(this.columnType);
    }

    private boolean isVarchar() {
        return "varchar".equalsIgnoreCase(this.columnType);
    }

    private boolean isAnyInteger() {
        return isInt() || isSmallInt() || isTinyInt() || isMediumInt() || isBigInt();
    }

    private boolean isInt() {
        return "int".equalsIgnoreCase(this.columnType);
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
        return "bigint".equalsIgnoreCase(this.columnType);
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getColumnType() {
        return this.columnType;
    }

    public String getColumnDisplayType() {
        return this.columnDisplayType;
    }

    public boolean isDateDisplayType() {
        return "DATE".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isIntegerDisplayType() {
        return "INTEGER".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isDecimalDisplayType() {
        return "DECIMAL".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isCodeValueDisplayType() {
        return "CODEVALUE".equalsIgnoreCase(this.columnDisplayType);
    }

    public boolean isCodeLookupDisplayType() {
        return "CODELOOKUP".equalsIgnoreCase(this.columnDisplayType);
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

    public boolean isColumnValueAllowed(final String match) {
        boolean allowed = false;
        for (ResultsetColumnValueData allowedValue : this.columnValues) {
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
        for (ResultsetColumnValueData allowedValue : this.columnValues) {
            if (allowedValue.codeMatches(match)) {
                allowed = true;
            }
        }
        return allowed;
    }
}