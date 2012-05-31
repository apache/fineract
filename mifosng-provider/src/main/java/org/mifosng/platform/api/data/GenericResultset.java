package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericResultset {

	private List<ResultsetColumnHeader> columnHeaders = new ArrayList<ResultsetColumnHeader>();
	private List<ResultsetDataRow> data = new ArrayList<ResultsetDataRow>();
	
	public GenericResultset() {
		
	}

	public List<ResultsetColumnHeader> getColumnHeaders() {
		return columnHeaders;
	}

	public void setColumnHeaders(List<ResultsetColumnHeader> columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public List<ResultsetDataRow> getData() {
		return data;
	}

	public void setData(List<ResultsetDataRow> data) {
		this.data = data;
	}
}