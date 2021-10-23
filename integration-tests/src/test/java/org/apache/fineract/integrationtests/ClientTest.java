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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;
    private static final SecureRandom rand = new SecureRandom();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testClientStatus() {

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);
        // Assertions.assertNotNull(clientId);

        HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));
        ClientStatusChecker.verifyClientIsActive(status);

        HashMap<String, Object> clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.rejectClient(clientId);
        ClientStatusChecker.verifyClientRejected(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.activateClient(clientId);
        ClientStatusChecker.verifyClientActiavted(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.withdrawClient(clientId);
        ClientStatusChecker.verifyClientWithdrawn(clientStatusHashMap);

    }

    @Test
    public void testClientAsPersonStatus() {

        final Integer clientId = ClientHelper.createClientAsPerson(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);
        // Assertions.assertNotNull(clientId);

        HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));
        ClientStatusChecker.verifyClientIsActive(status);

        HashMap<String, Object> clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.rejectClient(clientId);
        ClientStatusChecker.verifyClientRejected(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.activateClient(clientId);
        ClientStatusChecker.verifyClientActiavted(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.withdrawClient(clientId);
        ClientStatusChecker.verifyClientWithdrawn(clientStatusHashMap);

    }

    @Test
    public void testClientAsEntityStatus() {

        final Integer clientId = ClientHelper.createClientAsEntity(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientId);
        // Assertions.assertNotNull(clientId);

        HashMap<String, Object> status = ClientHelper.getClientStatus(requestSpec, responseSpec, String.valueOf(clientId));
        ClientStatusChecker.verifyClientIsActive(status);

        HashMap<String, Object> clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.rejectClient(clientId);
        ClientStatusChecker.verifyClientRejected(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.activateClient(clientId);
        ClientStatusChecker.verifyClientActiavted(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.closeClient(clientId);
        ClientStatusChecker.verifyClientClosed(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.reactivateClient(clientId);
        ClientStatusChecker.verifyClientPending(clientStatusHashMap);

        clientStatusHashMap = this.clientHelper.withdrawClient(clientId);
        ClientStatusChecker.verifyClientWithdrawn(clientStatusHashMap);

    }

    @SuppressWarnings("unchecked")
    @Test
    @SuppressFBWarnings(value = {
            "DMI_RANDOM_USED_ONLY_ONCE" }, justification = "False positive for random object created and used only once")
    public void testPendingOnlyClientRequest() {

        // Add a few clients to the server and activate a random amount of them
        for (int i = 0; i < 15; i++) {
            final Integer clientId = ClientHelper.createClientAsEntity(this.requestSpec, this.responseSpec);
            if (rand.nextInt(10) % 2 == 0) {
                // Takes Client to pending status
                this.clientHelper.closeClient(clientId);
                this.clientHelper.reactivateClient(clientId);
            }
            // Other clients stay in Active status
        }
        List<HashMap<String, Object>> clientsRecieved = (List<HashMap<String, Object>>) clientHelper.getClientWithStatus(50, "pending");
        assertNotEquals(clientsRecieved.size(), 0);
        for (int i = 0; i < clientsRecieved.size(); i++) {
            HashMap<String, Object> clientStatus = ClientHelper.getClientStatus(requestSpec, responseSpec,
                    String.valueOf(clientsRecieved.get(i).get("id")));
            ClientStatusChecker.verifyClientPending(clientStatus);
        }

        clientsRecieved = (List<HashMap<String, Object>>) clientHelper.getClientWithStatus(50, "active");
        assertNotEquals(clientsRecieved.size(), 0);
        for (int i = 0; i < clientsRecieved.size(); i++) {
            HashMap<String, Object> clientStatus = ClientHelper.getClientStatus(requestSpec, responseSpec,
                    String.valueOf(clientsRecieved.get(i).get("id")));
            ClientStatusChecker.verifyClientIsActive(clientStatus);
        }
    }

}
