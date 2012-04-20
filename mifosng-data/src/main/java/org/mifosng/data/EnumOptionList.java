package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EnumOptionList {

	private Collection<EnumOptionReadModel> options = new ArrayList<EnumOptionReadModel>();

	protected EnumOptionList() {
		//
	}

	public EnumOptionList(final List<EnumOptionReadModel> options) {
		this.options = options;
	}

	public Collection<EnumOptionReadModel> getOptions() {
		return this.options;
	}

	public void setOptions(final Collection<EnumOptionReadModel> options) {
		this.options = options;
	}
}