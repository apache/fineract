package org.mifosng.data.command;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrganisationCurrencyCommand implements Serializable {

	private String[] notSelectedCurrencies;
	private String[] currencies;

	public OrganisationCurrencyCommand() {
		//
	}

	public OrganisationCurrencyCommand(final List<String> selectedCurrencyCodes) {
		this.currencies = selectedCurrencyCodes.toArray(new String[selectedCurrencyCodes.size()]);
	}

	public String[] getNotSelectedCurrencies() {
		return notSelectedCurrencies;
	}

	public void setNotSelectedCurrencies(String... notSelectedCurrencies) {
		this.notSelectedCurrencies = notSelectedCurrencies;
	}

	public String[] getCurrencies() {
		return currencies;
	}

	public void setCurrencies(String... currencies) {
		this.currencies = currencies;
	}
}