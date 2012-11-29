package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.configuration.data.ConfigurationData;

public interface ConfigurationReadPlatformService {

    ConfigurationData retrieveCurrencyConfiguration();

}