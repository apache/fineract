/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.organisation.monetary.domain;

import org.apache.fineract.organisation.monetary.exception.CurrencyNotFoundException;
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