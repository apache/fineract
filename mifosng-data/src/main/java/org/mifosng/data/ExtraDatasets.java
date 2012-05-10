package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExtraDatasets {

	private List<ExtraDatasetRow> extraDatasetRow = new ArrayList<ExtraDatasetRow>();

	protected ExtraDatasets() {
		//
	}

	public ExtraDatasets(final List<ExtraDatasetRow> extraDatasetRow) {
		this.extraDatasetRow = extraDatasetRow;
	}

	public List<ExtraDatasetRow> getExtraDatasetRow() {
		return extraDatasetRow;
	}

	public void setExtraDatasetRow(List<ExtraDatasetRow> extraDatasetRow) {
		this.extraDatasetRow = extraDatasetRow;
	}

}