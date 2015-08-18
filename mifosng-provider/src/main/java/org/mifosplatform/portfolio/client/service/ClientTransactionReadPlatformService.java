package org.mifosplatform.portfolio.client.service;

import java.util.Collection;

import org.mifosplatform.portfolio.client.data.ClientTransactionData;
import org.springframework.transaction.annotation.Transactional;

public interface ClientTransactionReadPlatformService {

    @Transactional(readOnly = true)
    public Collection<ClientTransactionData> retrieveAllTransactions(final Long clientId);

    @Transactional(readOnly = true)
    public ClientTransactionData retrieveTransaction(Long clientId, Long transactionId);

}
