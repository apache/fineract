package org.mifosplatform.infrastructure.configuration.data;

/**
 * Immutable data object for global configuration property.
 */
public class GlobalConfigurationPropertyData {

    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private boolean enabled;

    public GlobalConfigurationPropertyData(final String name, final boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }
}