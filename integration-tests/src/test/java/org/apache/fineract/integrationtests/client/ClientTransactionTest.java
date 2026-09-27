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
package org.apache.fineract.integrationtests.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdChargesRequest;
import org.apache.fineract.client.models.PostClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ClientTransactionTest extends FeignIntegrationTest {

    private static final double CHARGE_AMOUNT = 100.0;
    private static final BigDecimal CLIENT_CHARGE_AMOUNT = BigDecimal.valueOf(200);
    private static final String CHARGE_DUE_DATE = "29 October 2011";

    private FeignClientHelper clientHelper;
    private FeignChargesHelper chargesHelper;

    @BeforeAll
    public void setup() {
        clientHelper = new FeignClientHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
    }

    @Test
    public void testClientTransactions() {
        PostClientsRequest createClientRequest = ClientRequestBuilders.defaultClient();
        String clientExternalId = UUID.randomUUID().toString();
        createClientRequest.setExternalId(clientExternalId);
        PostClientsResponse client = clientHelper.createClient(createClientRequest);
        Long clientId = client.getClientId();
        assertNotNull(clientId);

        final Long chargeId = chargesHelper.createClientSpecifiedDueDateCharge(CHARGE_AMOUNT).getResourceId();
        Assertions.assertNotNull(chargeId);
        final Long clientChargeId1 = chargesHelper.addClientCharge(clientId, clientCharge(chargeId)).getResourceId();
        Assertions.assertNotNull(clientChargeId1);
        String transactionExternalId = UUID.randomUUID().toString();
        final Long clientChargePaidTransactionId1 = chargesHelper
                .payClientCharge(clientId, clientChargeId1, ChargeRequestBuilders.payClientCharge("25 AUGUST 2015", 10)).getTransactionId();
        assertNotNull(clientChargePaidTransactionId1);

        final Long clientChargeId2 = chargesHelper.addClientCharge(clientId, clientCharge(chargeId)).getResourceId();
        Assertions.assertNotNull(clientChargeId2);
        final String clientChargePaidTransactionExternalId = chargesHelper
                .payClientCharge(clientId, clientChargeId2,
                        ChargeRequestBuilders.payClientCharge("25 AUGUST 2015", 12).externalId(transactionExternalId))
                .getSubResourceExternalId();
        assertNotNull(clientChargePaidTransactionExternalId);

        GetClientsClientIdTransactionsResponse allClientTransactionsByExternalId = clientHelper.getClientTransactions(clientExternalId);
        assertEquals(2, allClientTransactionsByExternalId.getTotalFilteredRecords());

        GetClientsClientIdTransactionsTransactionIdResponse clientTransactionByExternalId = clientHelper
                .getClientTransaction(clientExternalId, clientChargePaidTransactionId1);
        assertEquals(clientChargePaidTransactionId1, clientTransactionByExternalId.getId());

        GetClientsClientIdTransactionsTransactionIdResponse clientTransactionByTransactionExternalId = clientHelper
                .getClientTransactionByTransactionExternalId(clientId, clientChargePaidTransactionExternalId);
        assertNotNull(clientTransactionByTransactionExternalId);
        assertEquals(BigDecimal.valueOf(12), clientTransactionByTransactionExternalId.getAmount().stripTrailingZeros());

        PostClientsClientIdTransactionsTransactionIdResponse undoTransactionResponse = clientHelper.undoClientTransaction(clientExternalId,
                clientChargePaidTransactionId1);
        assertNotNull(undoTransactionResponse.getResourceId());

        PostClientsClientIdTransactionsTransactionIdResponse undoTransactionResponse2 = clientHelper
                .undoClientTransactionByTransactionExternalId(clientId, clientChargePaidTransactionExternalId);
        assertNotNull(undoTransactionResponse2.getResourceId());
    }

    private static PostClientsClientIdChargesRequest clientCharge(Long chargeId) {
        return ChargeRequestBuilders.clientCharge(chargeId, CHARGE_DUE_DATE, CLIENT_CHARGE_AMOUNT);
    }
}
