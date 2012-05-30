package org.mifosng.platform.configuration.service;

import java.util.ArrayList;
import java.util.List;

import org.mifosng.data.CurrencyData;
import org.mifosng.platform.api.data.ConfigurationData;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationReadPlatformServiceImpl implements ConfigurationReadPlatformService {

	private final CurrencyReadPlatformService currencyReadPlatformService;

	@Autowired
	public ConfigurationReadPlatformServiceImpl(final CurrencyReadPlatformService currencyReadPlatformService) {
		this.currencyReadPlatformService = currencyReadPlatformService;
	}

	@Override
	public ConfigurationData retrieveCurrencyConfiguration() {
		List<CurrencyData> selectedCurrencyOptions = new ArrayList<CurrencyData>(this.currencyReadPlatformService.retrieveAllowedCurrencies());
		List<CurrencyData> currencyOptions = new ArrayList<CurrencyData>(this.currencyReadPlatformService.retrieveAllPlatformCurrencies());

		// remove selected currency options
		currencyOptions.removeAll(selectedCurrencyOptions);
		
		ConfigurationData configurationData = new ConfigurationData();
		configurationData.setCurrencyOptions(currencyOptions);
		configurationData.setSelectedCurrencyOptions(selectedCurrencyOptions);
		
		return configurationData;
	}
}