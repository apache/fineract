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
package org.apache.fineract.portfolio.client.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.contract.ClientChargeReadService;
import org.apache.fineract.portfolio.client.contract.ClientJournalEntryWriteService;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientCharge;
import org.apache.fineract.portfolio.client.domain.ClientChargePaidBy;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.portfolio.client.domain.ClientTransactionRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientTransactionCannotBeUndoneException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientTransactionWritePlatformServiceJpaRepositoryImpl implements ClientTransactionWritePlatformService {

    private final ClientTransactionRepositoryWrapper clientTransactionRepository;

    private final ClientRepositoryWrapper clientRepository;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;
    private final ClientJournalEntryWriteService journalEntryWritePlatformService;
    private final ClientChargeReadService clientChargeReadService;

    @Override
    public CommandProcessingResult undo(Long clientId, Long transactionId) {

        final Client client = this.clientRepository.getActiveClientInUserScope(clientId);

        final ClientTransaction clientTransaction = this.clientTransactionRepository.findOneWithNotFoundDetection(clientId, transactionId);

        // validate that transaction can be undone
        if (clientTransaction.isReversed()) {
            throw new ClientTransactionCannotBeUndoneException(clientId, transactionId);
        }

        // mark transaction as reversed
        clientTransaction.reverse();

        // revert any charges paid back to their original state
        if (clientTransaction.isPayChargeTransaction() || clientTransaction.isWaiveChargeTransaction()) {
            // undo charge
            final Set<ClientChargePaidBy> chargesPaidBy = clientTransaction.getClientChargePaidByCollection();
            for (final ClientChargePaidBy clientChargePaidBy : chargesPaidBy) {
                final ClientCharge clientCharge = clientChargePaidBy.getClientCharge();
                final String currencyCode = this.clientChargeReadService.retrieveClientChargeDefinition(clientCharge.getChargeId())
                        .currencyCode();
                clientCharge.setCurrency(organisationCurrencyRepository.findOneWithNotFoundDetection(currencyCode));
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
        Map<String, Object> accountingBridgeData = clientTransaction.toMapData(incomeAccountIdByChargeId(clientTransaction));
        journalEntryWritePlatformService.createJournalEntriesForClientTransactions(accountingBridgeData);
    }

    private Map<Long, Long> incomeAccountIdByChargeId(final ClientTransaction clientTransaction) {
        final Map<Long, Long> incomeAccountIdByChargeId = new HashMap<>();
        for (final ClientChargePaidBy clientChargePaidBy : clientTransaction.getClientChargePaidByCollection()) {
            final Long chargeId = clientChargePaidBy.getClientCharge().getChargeId();
            if (!incomeAccountIdByChargeId.containsKey(chargeId)) {
                incomeAccountIdByChargeId.put(chargeId,
                        this.clientChargeReadService.retrieveClientChargeDefinition(chargeId).incomeAccountId());
            }
        }
        return incomeAccountIdByChargeId;
    }

}
