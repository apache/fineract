package org.mifosplatform.infrastructure.configuration.data;

import java.util.List;

/**
 * Immutable data object for global configuration.
 */
public class GlobalConfigurationData {

    @SuppressWarnings("unused")
    private final List<GlobalConfigurationPropertyData> globalConfiguration;

    public GlobalConfigurationData(final List<GlobalConfigurationPropertyData> globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }
}