/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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