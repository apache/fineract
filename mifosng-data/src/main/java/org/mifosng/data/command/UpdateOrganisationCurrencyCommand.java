package org.mifosng.data.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UpdateOrganisationCurrencyCommand implements Serializable {

	private Collection<String> codes = new ArrayList<String>();

	public UpdateOrganisationCurrencyCommand() {
		//
	}

	public UpdateOrganisationCurrencyCommand(final List<String> currencyCodes) {
		this.codes = currencyCodes;
	}

	public Collection<String> getCodes() {
		return this.codes;
	}

	public void setCodes(final Collection<String> codes) {
		this.codes = codes;
	}
}