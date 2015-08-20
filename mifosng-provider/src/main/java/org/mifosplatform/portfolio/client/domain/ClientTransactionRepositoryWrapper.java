/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.domain;

import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientTransactionNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClientTransactionRepositoryWrapper {

    private final ClientTransactionRepository repository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;

    @Autowired
    public ClientTransactionRepositoryWrapper(final ClientTransactionRepository repository,
            final OrganisationCurrencyRepositoryWrapper currencyRepositoryWrapper) {
        this.repository = repository;
        this.organisationCurrencyRepository = currencyRepositoryWrapper;
    }

    public ClientTransaction findOneWithNotFoundDetection(final Long clientId, final Long transactionId) {
        final ClientTransaction clientTransaction = this.repository.findOne(transactionId);
        if (clientTransaction == null
                || clientTransaction.getClientId() != clientId) { throw new ClientTransactionNotFoundException(clientId, transactionId); }
        // enrich Client charge with details of Organizational currency
        clientTransaction.setCurrency(organisationCurrencyRepository.findOneWithNotFoundDetection(clientTransaction.getCurrencyCode()));
        return clientTransaction;
    }

    public void save(final ClientTransaction clientTransaction) {
        this.repository.save(clientTransaction);
    }

    public void saveAndFlush(final ClientTransaction clientTransaction) {
        this.repository.saveAndFlush(clientTransaction);
    }

    public void delete(final ClientTransaction clientTransaction) {
        this.repository.delete(clientTransaction);
    }

}
