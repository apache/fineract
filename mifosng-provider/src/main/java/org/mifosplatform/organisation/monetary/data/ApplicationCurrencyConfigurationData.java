package org.mifosplatform.organisation.monetary.data;

import java.util.Collection;

/**
 * Immutable data object for application currency.
 */
public class ApplicationCurrencyConfigurationData {

    @SuppressWarnings("unused")
    private final Collection<CurrencyData> selectedCurrencyOptions;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;

    public ApplicationCurrencyConfigurationData(final Collection<CurrencyData> currencyOptions, final Collection<CurrencyData> selectedCurrencyOptions) {
        this.currencyOptions = currencyOptions;
        this.selectedCurrencyOptions = selectedCurrencyOptions;
    }
}