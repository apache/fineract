package org.mifosplatform.infrastructure.configuration.service;

import java.util.List;

import org.mifosplatform.infrastructure.configuration.data.ConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.CurrencyData;
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

        final List<CurrencyData> selectedCurrencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllPlatformCurrencies();

        // remove selected currency options
        currencyOptions.removeAll(selectedCurrencyOptions);

        return new ConfigurationData(currencyOptions, selectedCurrencyOptions);
    }
}