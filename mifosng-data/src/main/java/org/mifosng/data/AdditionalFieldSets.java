package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AdditionalFieldSets {

	private List<AdditionalFieldSet> additionalFieldsSets = new ArrayList<AdditionalFieldSet>();

	protected AdditionalFieldSets() {
		//
	}

	public AdditionalFieldSets(final List<AdditionalFieldSet> additionalFieldsSets) {
		this.additionalFieldsSets = additionalFieldsSets;
	}

	public List<AdditionalFieldSet> getAdditionalFieldsSets() {
		return additionalFieldsSets;
	}

	public void setAdditionalFieldsSets(List<AdditionalFieldSet> additionalFieldsSets) {
		this.additionalFieldsSets = additionalFieldsSets;
	}

}