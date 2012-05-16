package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AdditionalFieldsSets {

	private List<AdditionalFieldsSet> additionalFieldsSets = new ArrayList<AdditionalFieldsSet>();

	protected AdditionalFieldsSets() {
		//
	}

	public AdditionalFieldsSets(final List<AdditionalFieldsSet> additionalFieldsSets) {
		this.additionalFieldsSets = additionalFieldsSets;
	}

	public List<AdditionalFieldsSet> getAdditionalFieldsSets() {
		return additionalFieldsSets;
	}

	public void setAdditionalFieldsSets(List<AdditionalFieldsSet> additionalFieldsSets) {
		this.additionalFieldsSets = additionalFieldsSets;
	}

}