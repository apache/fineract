/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepositoryWrapper;
import org.mifosplatform.portfolio.charge.exception.ChargeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClientChargeRepositoryWrapper {

    private final ClientChargeRepository repository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;

    @Autowired
    public ClientChargeRepositoryWrapper(final ClientChargeRepository repository,
            final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepositoryWrapper) {
        this.repository = repository;
        this.organisationCurrencyRepository = organisationCurrencyRepositoryWrapper;
    }

    public ClientCharge findOneWithNotFoundDetection(final Long id) {
        final ClientCharge clientCharge = this.repository.findOne(id);
        if (clientCharge == null) { throw new ChargeNotFoundException(id); }
        // enrich Client charge with details of Organizational currency
        clientCharge.setCurrency(organisationCurrencyRepository.findOneWithNotFoundDetection(clientCharge.getCharge().getCurrencyCode()));
        return clientCharge;
    }

    public void save(final ClientCharge clientCharge) {
        this.repository.save(clientCharge);
    }

    public void saveAndFlush(final ClientCharge clientCharge) {
        this.repository.saveAndFlush(clientCharge);
    }

    public void delete(final ClientCharge clientCharge) {
        this.repository.delete(clientCharge);
    }

}
