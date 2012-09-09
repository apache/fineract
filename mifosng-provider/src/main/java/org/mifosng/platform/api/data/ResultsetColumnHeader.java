package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

public class ResultsetColumnHeader {

	private String columnName;
	private String columnType;
	private Long columnLength;
	private String columnDisplayType;
	private boolean isColumnNullable;
	private boolean isColumnPrimaryKey;
	private List<String> columnValues = new ArrayList<String>();
	private List<ResultsetColumnValue> columnValuesNew = new ArrayList<ResultsetColumnValue>();

	public ResultsetColumnHeader() {

	}

	public List<String> getColumnValues() {
		return columnValues;
	}

	public void setColumnValues(List<String> columnValues) {
		this.columnValues = columnValues;
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

	public String getColumnDisplayType() {
		return columnDisplayType;
	}

	public void setColumnDisplayType(String columnDisplayType) {
		this.columnDisplayType = columnDisplayType;
	}

	public List<ResultsetColumnValue> getColumnValuesNew() {
		return columnValuesNew;
	}

	public void setColumnValuesNew(List<ResultsetColumnValue> columnValuesNew) {
		this.columnValuesNew = columnValuesNew;
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

}
