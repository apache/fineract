package org.mifosng.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CurrencyList {

	private Collection<CurrencyData> currencies = new ArrayList<CurrencyData>();

	protected CurrencyList() {
		//
	}

	public CurrencyList(final List<CurrencyData> currencyOptions) {
		this.currencies = currencyOptions;
	}

	public Collection<CurrencyData> getCurrencies() {
		return this.currencies;
	}

	public void setCurrencies(final Collection<CurrencyData> currencies) {
		this.currencies = currencies;
	}
}