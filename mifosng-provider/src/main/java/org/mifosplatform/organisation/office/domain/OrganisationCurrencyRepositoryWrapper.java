/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.domain;

import org.mifosplatform.organisation.monetary.exception.OrganizationalCurrencyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link OrganisationCurrency} that is responsible for checking if
 * {@link OrganisationCurrency} is returned when using <code>findOne</code>
 * repository method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link OrganisationCurrency} is required.
 * </p>
 */
@Service
public class OrganisationCurrencyRepositoryWrapper {

    private final OrganisationCurrencyRepository repository;

    @Autowired
    public OrganisationCurrencyRepositoryWrapper(final OrganisationCurrencyRepository repository) {
        this.repository = repository;
    }

    public OrganisationCurrency findOneWithNotFoundDetection(final String currencyCode) {
        final OrganisationCurrency organisationCurrency = this.repository.findOneByCode(currencyCode);
        if (organisationCurrency == null) { throw new OrganizationalCurrencyNotFoundException(currencyCode); }
        return organisationCurrency;
    }
}