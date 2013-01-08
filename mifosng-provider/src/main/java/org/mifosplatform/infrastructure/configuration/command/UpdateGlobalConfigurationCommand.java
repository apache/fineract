package org.mifosplatform.infrastructure.configuration.command;

import java.util.Map;

/**
 * Immutable command for updating global configuration settings.
 */
public class UpdateGlobalConfigurationCommand {

    private final Map<String, Boolean> globalConfiguration;

    public UpdateGlobalConfigurationCommand(final Map<String, Boolean> globalConfigurationMap) {
        this.globalConfiguration = globalConfigurationMap;
    }

    public Map<String, Boolean> getGlobalConfiguration() {
        return this.globalConfiguration;
    }
}