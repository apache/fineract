package org.mifosng.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExtraDatasets {

	private List<String> names = new ArrayList<String>();

	protected ExtraDatasets() {
		//
	}

	public ExtraDatasets(final List<String> names) {
		this.names = names;
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

}