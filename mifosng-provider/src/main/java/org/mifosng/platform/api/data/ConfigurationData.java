package org.mifosng.platform.api.data;

import java.util.List;

public class ConfigurationData {

	private final List<CurrencyData> selectedCurrencyOptions;
	private final List<CurrencyData> currencyOptions;

	public ConfigurationData(final List<CurrencyData> currencyOptions, final List<CurrencyData> selectedCurrencyOptions) {
		this.currencyOptions = currencyOptions;
		this.selectedCurrencyOptions = selectedCurrencyOptions;
	}

	public List<CurrencyData> getSelectedCurrencyOptions() {
		return selectedCurrencyOptions;
	}

	public List<CurrencyData> getCurrencyOptions() {
		return currencyOptions;
	}
}