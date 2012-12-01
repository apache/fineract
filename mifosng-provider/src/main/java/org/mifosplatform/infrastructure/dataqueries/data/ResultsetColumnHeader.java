package org.mifosplatform.infrastructure.dataqueries.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;

public class ResultsetColumnHeader {

    private String columnName;
    private String columnType;
    private Long columnLength;
    private String columnDisplayType;
    private boolean isColumnNullable;
    private boolean isColumnPrimaryKey;
    private List<ResultsetColumnValue> columnValues = new ArrayList<ResultsetColumnValue>();

    public ResultsetColumnHeader() {

    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Long getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(Long columnLength) {
        this.columnLength = columnLength;
    }

    public List<ResultsetColumnValue> getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(List<ResultsetColumnValue> columnValues) {
        this.columnValues = columnValues;
    }

    public boolean isColumnNullable() {
        return isColumnNullable;
    }

    public void setColumnNullable(boolean isColumnNullable) {
        this.isColumnNullable = isColumnNullable;
    }

    public boolean isColumnPrimaryKey() {
        return isColumnPrimaryKey;
    }

    public void setColumnPrimaryKey(boolean isColumnPrimaryKey) {
        this.isColumnPrimaryKey = isColumnPrimaryKey;
    }

    public String getColumnDisplayType() {
        return columnDisplayType;
    }

    public void setColumnDisplayType() {

        if (this.getColumnValues().size() > 0) {

            if (this.getColumnType().equalsIgnoreCase("int")) {
                this.columnDisplayType = "CODELOOKUP";
                return;
            }
            if (this.getColumnType().equalsIgnoreCase("varchar")) {
                this.columnDisplayType = "CODEVALUE";
                return;
            }

            throw new PlatformDataIntegrityException("error.msg.invalid.lookup.type", "Invalid Lookup Type:" + this.getColumnType()
                    + " - Column Name: " + this.getColumnName());
        }

        if (this.getColumnType().equalsIgnoreCase("varchar") || this.getColumnType().equalsIgnoreCase("char")) {
            this.columnDisplayType = "STRING";
            return;
        }

        if (this.getColumnType().equalsIgnoreCase("int") || this.getColumnType().equalsIgnoreCase("bigint")
                || this.getColumnType().equalsIgnoreCase("smallint") || this.getColumnType().equalsIgnoreCase("mediumint")
                || this.getColumnType().equalsIgnoreCase("tinyint")) {
            this.columnDisplayType = "INTEGER";
            return;
        }

        if (this.getColumnType().equalsIgnoreCase("date")) {
            this.columnDisplayType = "DATE";
            return;
        }

        if (this.getColumnType().equalsIgnoreCase("decimal")) {
            this.columnDisplayType = "DECIMAL";
            return;
        }

        if (this.getColumnType().equalsIgnoreCase("text") || this.getColumnType().equalsIgnoreCase("mediumtext")
                || this.getColumnType().equalsIgnoreCase("longtext") || this.getColumnType().equalsIgnoreCase("tinytext")) {
            this.columnDisplayType = "TEXT";
            return;
        }

        throw new PlatformDataIntegrityException("error.msg.unsupported.column.type", "Unsupported Column Type:" + this.getColumnType()
                + " - Column Name: " + this.getColumnName());
    }

}
