package org.mifosplatform.organisation.monetary.service;

import java.util.Collection;

import org.mifosplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganisationCurrencyReadPlatformServiceImpl implements OrganisationCurrencyReadPlatformService {

    private final CurrencyReadPlatformService currencyReadPlatformService;

    @Autowired
    public OrganisationCurrencyReadPlatformServiceImpl(final CurrencyReadPlatformService currencyReadPlatformService) {
        this.currencyReadPlatformService = currencyReadPlatformService;
    }

    @Override
    public ApplicationCurrencyConfigurationData retrieveCurrencyConfiguration() {

        final Collection<CurrencyData> selectedCurrencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllPlatformCurrencies();

        // remove selected currency options
        currencyOptions.removeAll(selectedCurrencyOptions);

        return new ApplicationCurrencyConfigurationData(currencyOptions, selectedCurrencyOptions);
    }
}