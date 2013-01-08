package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.configuration.data.GlobalConfigurationData;

public interface ConfigurationReadPlatformService {

    GlobalConfigurationData retrieveGlobalConfiguration();

}