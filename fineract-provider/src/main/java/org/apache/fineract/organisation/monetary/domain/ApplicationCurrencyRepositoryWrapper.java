/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.domain;

import org.mifosplatform.organisation.monetary.exception.CurrencyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link ApplicationCurrencyRepository} that is responsible for
 * checking if {@link ApplicationCurrency} is returned when using
 * <code>findOne</code> repository method and throwing an appropriate not found
 * exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link ApplicationCurrencyRepository} is required.
 * </p>
 */
@Service
public class ApplicationCurrencyRepositoryWrapper {

    private final ApplicationCurrencyRepository repository;

    @Autowired
    public ApplicationCurrencyRepositoryWrapper(final ApplicationCurrencyRepository repository) {
        this.repository = repository;
    }

    public ApplicationCurrency findOneWithNotFoundDetection(final MonetaryCurrency currency) {

        final ApplicationCurrency defaultApplicationCurrency = this.repository.findOneByCode(currency.getCode());
        if (defaultApplicationCurrency == null) { throw new CurrencyNotFoundException(currency.getCode()); }

        final ApplicationCurrency applicationCurrency = ApplicationCurrency.from(defaultApplicationCurrency,
                currency.getDigitsAfterDecimal(), currency.getCurrencyInMultiplesOf());

        return applicationCurrency;
    }

    /**
     * Used when its not needed for {@link ApplicationCurrency} to inherit
     * decimal place settings of existing currency.
     */
    public ApplicationCurrency findOneWithNotFoundDetection(final String currencyCode) {
        final ApplicationCurrency applicationCurrency = this.repository.findOneByCode(currencyCode);
        if (applicationCurrency == null) { throw new CurrencyNotFoundException(currencyCode); }
        return applicationCurrency;
    }
}