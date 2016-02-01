/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.SearchParameters;
import org.mifosplatform.portfolio.client.data.ClientTransactionData;
import org.springframework.transaction.annotation.Transactional;

public interface ClientTransactionReadPlatformService {

    @Transactional(readOnly = true)
    public Page<ClientTransactionData> retrieveAllTransactions(Long clientId, SearchParameters parameters);

    @Transactional(readOnly = true)
    public Collection<ClientTransactionData> retrieveAllTransactions(final Long clientId, final Long chargeId);

    @Transactional(readOnly = true)
    public ClientTransactionData retrieveTransaction(Long clientId, Long transactionId);

}
