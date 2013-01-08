package org.mifosplatform.organisation.monetary.service;

import org.mifosplatform.organisation.monetary.data.ApplicationCurrencyConfigurationData;

public interface OrganisationCurrencyReadPlatformService {

    ApplicationCurrencyConfigurationData retrieveCurrencyConfiguration();

}