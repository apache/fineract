package org.mifosplatform.infrastructure.configuration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when global configuration properties are
 * not found.
 */
public class GlobalConfigurationPropertyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public GlobalConfigurationPropertyNotFoundException(final String propertyName) {
        super("error.msg.configuration.property.invalid", "Configuration property `" + propertyName + "` does not exist", propertyName);
    }
}