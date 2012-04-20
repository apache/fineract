package org.mifosng.data.reports;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResultsetColumnHeader {

	private String columnName;
	private String columnType;
	private Integer columnLength;
	private String columnDisplayType;
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

	public Integer getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(Integer columnLength) {
		this.columnLength = columnLength;
	}

	public String getColumnDisplayType() {
		return columnDisplayType;
	}

	public void setColumnDisplayType(String columnDisplayType) {
		this.columnDisplayType = columnDisplayType;
	}

}
