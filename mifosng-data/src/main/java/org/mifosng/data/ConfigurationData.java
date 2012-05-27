package org.mifosng.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonFilter;

@JsonFilter("myFilter")
public class ConfigurationData implements Serializable {

	private List<CurrencyData> selectedCurrencyOptions = new ArrayList<CurrencyData>();
	private List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>();

	public ConfigurationData() {
		//
	}

	public List<CurrencyData> getSelectedCurrencyOptions() {
		return selectedCurrencyOptions;
	}

	public void setSelectedCurrencyOptions(
			List<CurrencyData> selectedCurrencyOptions) {
		this.selectedCurrencyOptions = selectedCurrencyOptions;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}

	public void setCurrencyOptions(List<CurrencyData> currencyOptions) {
		this.currencyOptions = currencyOptions;
	}
}