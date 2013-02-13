/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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