package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EnumOptionList {

	private Collection<EnumOptionData> options = new ArrayList<EnumOptionData>();

	protected EnumOptionList() {
		//
	}

	public EnumOptionList(final List<EnumOptionData> options) {
		this.options = options;
	}

	public Collection<EnumOptionData> getOptions() {
		return this.options;
	}

	public void setOptions(final Collection<EnumOptionData> options) {
		this.options = options;
	}
}