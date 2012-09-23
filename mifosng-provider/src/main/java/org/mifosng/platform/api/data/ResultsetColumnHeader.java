package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.platform.exceptions.PlatformDataIntegrityException;

public class ResultsetColumnHeader {

	private String columnName;
	private String columnType;
	private Long columnLength;
	private String columnDisplayTypeNew;
	// TODO - remove columnDisplayType when Additional Fields functionality
	// removed and rename columnDisplayTypeNew to columnDisplayType
	private String columnDisplayType;
	private boolean isColumnNullable;
	private boolean isColumnPrimaryKey;
	private List<ResultsetColumnValue> columnValuesNew = new ArrayList<ResultsetColumnValue>();
	// TODO - remove columnValues when Additional Fields functionality removed
	// and rename columnValuesNew to columnValues
	private List<String> columnValues = new ArrayList<String>();

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

	public String getColumnDisplayTypeNew() {
		return columnDisplayTypeNew;
	}

	public void setColumnDisplayTypeNew() {

		if (this.getColumnValuesNew().size() > 0) {

			if (this.getColumnType().equalsIgnoreCase("int")) {
				this.columnDisplayTypeNew = "CODELOOKUP";
				return;
			}
			if (this.getColumnType().equalsIgnoreCase("varchar")) {
				this.columnDisplayTypeNew = "CODEVALUE";
				return;
			}

			throw new PlatformDataIntegrityException(
					"error.msg.invalid.lookup.type", "Invalid Lookup Type:"
							+ this.getColumnType() + " - Column Name: "
							+ this.getColumnName());
		}

		if (this.getColumnType().equalsIgnoreCase("varchar") || this.getColumnType().equalsIgnoreCase("char")) {
			this.columnDisplayTypeNew = "STRING";
			return;
		}

		if (this.getColumnType().equalsIgnoreCase("int")
				|| this.getColumnType().equalsIgnoreCase("bigint")
				|| this.getColumnType().equalsIgnoreCase("smallint")
				|| this.getColumnType().equalsIgnoreCase("mediumint")
				|| this.getColumnType().equalsIgnoreCase("tinyint")) {
			this.columnDisplayTypeNew = "INTEGER";
			return;
		}

		if (this.getColumnType().equalsIgnoreCase("date")) {
			this.columnDisplayTypeNew = "DATE";
			return;
		}

		if (this.getColumnType().equalsIgnoreCase("decimal")) {
			this.columnDisplayTypeNew = "DECIMAL";
			return;
		}

		if (this.getColumnType().equalsIgnoreCase("text")
				|| this.getColumnType().equalsIgnoreCase("mediumtext")
				|| this.getColumnType().equalsIgnoreCase("longtext")
				|| this.getColumnType().equalsIgnoreCase("tinytext")) {
			this.columnDisplayTypeNew = "TEXT";
			return;
		}

		throw new PlatformDataIntegrityException(
				"error.msg.unsupported.column.type", "Unsupported Column Type:"
						+ this.getColumnType() + " - Column Name: "
						+ this.getColumnName());
	}

}
