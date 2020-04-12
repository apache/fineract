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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private ClientHelper clientHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

    }

    @Test
    public void testClientStatus() {
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

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

        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        final Integer clientId = ClientHelper.createClientAsPerson(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

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

        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        final Integer clientId = ClientHelper.createClientAsEntity(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

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

}
