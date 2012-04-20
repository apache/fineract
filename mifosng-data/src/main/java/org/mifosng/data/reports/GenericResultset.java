package org.mifosng.data.reports;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericResultset {

	// [
		// [contact_email, field 2, field 3], 
	    // [
	         // ["abb.net", "r2", "x"], ["abb.net", "r3"]
	    // ]
	// ]
	
	// create types for these
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