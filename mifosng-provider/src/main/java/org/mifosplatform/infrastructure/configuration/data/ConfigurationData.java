package org.mifosplatform.infrastructure.configuration.data;

import java.util.Collection;

/**
 * Immutable data object for configuration.
 */
public class ConfigurationData {

    @SuppressWarnings("unused")
    private final Collection<CurrencyData> selectedCurrencyOptions;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;

    public ConfigurationData(final Collection<CurrencyData> currencyOptions, final Collection<CurrencyData> selectedCurrencyOptions) {
        this.currencyOptions = currencyOptions;
        this.selectedCurrencyOptions = selectedCurrencyOptions;
    }
}