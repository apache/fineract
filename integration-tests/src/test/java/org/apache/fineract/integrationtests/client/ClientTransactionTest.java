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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsResponse;
import org.apache.fineract.client.models.GetClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsClientIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientTransactionTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        clientHelper = new ClientHelper(requestSpec, responseSpec);
    }

    @Test
    public void testClientTransactions() {
        PostClientsRequest createClientRequest = ClientHelper.defaultClientCreationRequest();
        String clientExternalId = UUID.randomUUID().toString();
        createClientRequest.setExternalId(clientExternalId);
        PostClientsResponse client = clientHelper.createClient(createClientRequest);
        Long clientId = client.getClientId();
        assertNotNull(clientId);

        final Integer chargeId = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getChargeSpecifiedDueDateJSON());
        Assertions.assertNotNull(chargeId);
        final Integer clientChargeId1 = ClientHelper.addChargesForClient(requestSpec, responseSpec, clientId.intValue(),
                ClientHelper.getSpecifiedDueDateChargesClientAsJSON(chargeId.toString(), "29 October 2011"));
        Assertions.assertNotNull(clientChargeId1);
        String transactionExternalId = UUID.randomUUID().toString();
        final String clientChargePaidTransactionId1 = ClientHelper.payChargesForClients(requestSpec, responseSpec, clientId.intValue(),
                clientChargeId1, ClientHelper.getPayChargeJSON("25 AUGUST 2015", "10"));
        assertNotNull(clientChargePaidTransactionId1);

        final Integer clientChargeId2 = ClientHelper.addChargesForClient(requestSpec, responseSpec, clientId.intValue(),
                ClientHelper.getSpecifiedDueDateChargesClientAsJSON(chargeId.toString(), "29 October 2011"));
        Assertions.assertNotNull(clientChargeId2);
        final String clientChargePaidTransactionExternalId = ClientHelper.payChargesForClientsTransactionExternalId(requestSpec,
                responseSpec, clientId.intValue(), clientChargeId2,
                ClientHelper.getPayChargeJSONWithExternalId("25 AUGUST 2015", "12", transactionExternalId));
        assertNotNull(clientChargePaidTransactionExternalId);

        GetClientsClientIdTransactionsResponse allClientTransactionsByExternalId = clientHelper
                .getAllClientTransactionsByExternalId(clientExternalId);
        assertEquals(2, allClientTransactionsByExternalId.getTotalFilteredRecords());

        GetClientsClientIdTransactionsTransactionIdResponse clientTransactionByExternalId = clientHelper
                .getClientTransactionByExternalId(clientExternalId, clientChargePaidTransactionId1);
        assertEquals(Integer.parseInt(clientChargePaidTransactionId1), clientTransactionByExternalId.getId());

        GetClientsClientIdTransactionsTransactionIdResponse clientTransactionByTransactionExternalId = clientHelper
                .getClientTransactionByTransactionExternalId(clientId, clientChargePaidTransactionExternalId);
        assertNotNull(clientTransactionByTransactionExternalId);
        assertEquals(BigDecimal.valueOf(12), clientTransactionByTransactionExternalId.getAmount().stripTrailingZeros());

        PostClientsClientIdTransactionsTransactionIdResponse undoTransactionResponse = clientHelper
                .undoClientTransactionByExternalId(clientExternalId, clientChargePaidTransactionId1);
        assertNotNull(undoTransactionResponse.getResourceId());

        PostClientsClientIdTransactionsTransactionIdResponse undoTransactionResponse2 = clientHelper
                .undoClientTransactionByTransactionExternalId(clientId, clientChargePaidTransactionExternalId);
        assertNotNull(undoTransactionResponse2.getResourceId());
    }
}
