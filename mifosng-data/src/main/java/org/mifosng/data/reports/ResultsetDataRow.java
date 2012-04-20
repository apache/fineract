package org.mifosng.data.reports;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResultsetDataRow {

	private List<String> row = new ArrayList<String>();
	
	public ResultsetDataRow() {
		
	}

	public List<String> getRow() {
		return row;
	}

	public void setRow(List<String> row) {
		this.row = row;
	}
}
