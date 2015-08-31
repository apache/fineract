/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.util.Map;
import java.util.Set;

import org.mifosplatform.accounting.journalentry.service.JournalEntryWritePlatformService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientCharge;
import org.mifosplatform.portfolio.client.domain.ClientChargePaidBy;
import org.mifosplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.mifosplatform.portfolio.client.domain.ClientTransaction;
import org.mifosplatform.portfolio.client.domain.ClientTransactionRepositoryWrapper;
import org.mifosplatform.portfolio.client.exception.ClientTransactionCannotBeUndoneException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientTransactionWritePlatformServiceJpaRepositoryImpl implements ClientTransactionWritePlatformService {

    private final ClientTransactionRepositoryWrapper clientTransactionRepository;

    private final ClientRepositoryWrapper clientRepository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;

    @Autowired
    public ClientTransactionWritePlatformServiceJpaRepositoryImpl(final ClientTransactionRepositoryWrapper clientTransactionRepository,
            final ClientRepositoryWrapper clientRepositoryWrapper,
            final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepositoryWrapper,
            JournalEntryWritePlatformService journalEntryWritePlatformService) {
        this.clientTransactionRepository = clientTransactionRepository;
        this.clientRepository = clientRepositoryWrapper;
        this.organisationCurrencyRepository = organisationCurrencyRepositoryWrapper;
        this.journalEntryWritePlatformService = journalEntryWritePlatformService;
    }

    @Override
    public CommandProcessingResult undo(Long clientId, Long transactionId) {

        final Client client = this.clientRepository.getActiveClient(clientId);

        final ClientTransaction clientTransaction = this.clientTransactionRepository.findOneWithNotFoundDetection(clientId, transactionId);

        // validate that transaction can be undone
        if (clientTransaction.isReversed()) { throw new ClientTransactionCannotBeUndoneException(clientId, transactionId); }

        // mark transaction as reversed
        clientTransaction.reverse();

        // revert any charges paid back to their original state
        if (clientTransaction.isPayChargeTransaction() || clientTransaction.isWaiveChargeTransaction()) {
            // undo charge
            final Set<ClientChargePaidBy> chargesPaidBy = clientTransaction.getClientChargePaidByCollection();
            for (final ClientChargePaidBy clientChargePaidBy : chargesPaidBy) {
                final ClientCharge clientCharge = clientChargePaidBy.getClientCharge();
                clientCharge.setCurrency(
                        organisationCurrencyRepository.findOneWithNotFoundDetection(clientCharge.getCharge().getCurrencyCode()));
                if (clientTransaction.isPayChargeTransaction()) {
                    clientCharge.undoPayment(clientTransaction.getAmount());
                } else if (clientTransaction.isWaiveChargeTransaction()) {
                    clientCharge.undoWaiver(clientTransaction.getAmount());
                }
            }
        }

        // generate accounting entries
        this.clientTransactionRepository.saveAndFlush(clientTransaction);
        generateAccountingEntries(clientTransaction);

        return new CommandProcessingResultBuilder() //
                .withEntityId(transactionId) //
                .withOfficeId(client.officeId()) //
                .withClientId(clientId) //
                .build();
    }

    private void generateAccountingEntries(ClientTransaction clientTransaction) {
        Map<String, Object> accountingBridgeData = clientTransaction.toMapData();
        journalEntryWritePlatformService.createJournalEntriesForClientTransactions(accountingBridgeData);
    }

}
