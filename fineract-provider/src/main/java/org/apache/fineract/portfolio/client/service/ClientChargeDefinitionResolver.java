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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import org.apache.fineract.portfolio.client.contract.ClientChargeDefinitionData;
import org.apache.fineract.portfolio.client.contract.ClientChargeReadService;
import org.apache.fineract.portfolio.client.domain.ClientCharge;
import org.apache.fineract.portfolio.client.domain.ClientChargePaidBy;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.springframework.stereotype.Component;

/**
 * Looks up the charge definitions behind existing client charges through {@link ClientChargeReadService}, and applies
 * what the client feature needs from them: the currency of a client charge and the income account used for accounting.
 */
@Component
@RequiredArgsConstructor
public class ClientChargeDefinitionResolver {

    private final ClientChargeReadService clientChargeReadService;
    private final OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository;

    /**
     * Sets the currency of the client charge from its charge definition and returns that definition.
     */
    public ClientChargeDefinitionData applyCurrency(final ClientCharge clientCharge) {
        final ClientChargeDefinitionData chargeDefinition = this.clientChargeReadService
                .retrieveClientChargeDefinition(clientCharge.getChargeId());
        applyCurrency(clientCharge, chargeDefinition);
        return chargeDefinition;
    }

    public void applyCurrency(final ClientCharge clientCharge, final ClientChargeDefinitionData chargeDefinition) {
        clientCharge.setCurrency(this.organisationCurrencyRepository.findOneWithNotFoundDetection(chargeDefinition.currencyCode()));
    }

    /**
     * Retrieves the definition of every charge paid by the transaction, once per charge definition, keyed by charge id.
     */
    public Map<Long, ClientChargeDefinitionData> retrieveChargeDefinitions(final ClientTransaction clientTransaction) {
        final Map<Long, ClientChargeDefinitionData> chargeDefinitions = new HashMap<>();
        for (final ClientChargePaidBy clientChargePaidBy : clientTransaction.getClientChargePaidByCollection()) {
            chargeDefinitions.computeIfAbsent(clientChargePaidBy.getClientCharge().getChargeId(),
                    this.clientChargeReadService::retrieveClientChargeDefinition);
        }
        return chargeDefinitions;
    }

    public Map<String, Object> toAccountingBridgeData(final ClientTransaction clientTransaction,
            final Map<Long, ClientChargeDefinitionData> chargeDefinitions) {
        final Map<Long, Long> incomeAccountIdByChargeId = new HashMap<>();
        chargeDefinitions
                .forEach((chargeId, chargeDefinition) -> incomeAccountIdByChargeId.put(chargeId, chargeDefinition.incomeAccountId()));
        return clientTransaction.toMapData(incomeAccountIdByChargeId);
    }
}
