package org.apache.fineract.infrastructure.configuration.exception;

import org.apache.fineract.infrastructure.core.exception.ConfigurationNotEnabledException;


public class GlobalConfigurationNotEnabledException extends ConfigurationNotEnabledException {

    public GlobalConfigurationNotEnabledException(final String configName){
        super("error.msg.configuration.not.enabled", "Configuration `" + configName + "` is not enabled", configName);
    }
}
