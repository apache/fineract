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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.UUID;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientApiTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateAndRetrieveClient() {
        String firstName = Utils.randomFirstNameGenerator();
        String lastName = Utils.randomLastNameGenerator();
        String externalId = UUID.randomUUID().toString();

        PostClientsRequest request = new PostClientsRequest()
                .officeId(1L)
                .legalFormId(ClientHelper.LEGALFORM_ID_PERSON)
                .firstname(firstName)
                .lastname(lastName)
                .externalId(externalId)
                .dateFormat(Utils.DATE_FORMAT)
                .locale("en")
                .active(true)
                .activationDate(ClientHelper.DEFAULT_DATE);

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, request);
        assertNotNull(clientId);

        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientId);

        GetClientsClientIdResponse client = ClientHelper.getClient(requestSpec, responseSpec, clientId);
        assertNotNull(client);
        assertEquals(firstName, client.getFirstname());
        assertEquals(lastName, client.getLastname());
        assertEquals(externalId, client.getExternalId());
    }
}
