/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

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

}
