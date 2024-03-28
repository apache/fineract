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

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersRequest;
import org.apache.fineract.client.models.PostClientsClientIdIdentifiersResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.SortOrder;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientSearchTest {

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
    public void testClientSearchWorks_WithLastnameText_WithPaging() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, 0, 1);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    public void testClientSearchWorks_WhenNoExternalIdForClients() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setExternalId(null);
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setExternalId(null);
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setExternalId(null);
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, 0, 1);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumberOfElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    public void testClientSearchWorks_WithLastnameTextOnDefaultOrdering() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request3.getExternalId());
        assertThat(result.getContent().get(1).getExternalId().getValue()).isEqualTo(request2.getExternalId());
        assertThat(result.getContent().get(2).getExternalId().getValue()).isEqualTo(request1.getExternalId());
    }

    @Test
    public void testClientSearchWorks_WithLastnameText_OrderedByIdAsc() {
        // given
        String lastname = Utils.randomStringGenerator("Client_LastName_", 5);
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setLastname(lastname);
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setLastname(lastname);
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        request3.setLastname(lastname);
        clientHelper.createClient(request3);

        SortOrder sortOrder = new SortOrder().property("id").direction(SortOrder.DirectionEnum.ASC);
        // when
        PageClientSearchData result = clientHelper.searchClients(lastname, sortOrder);
        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request1.getExternalId());
        assertThat(result.getContent().get(1).getExternalId().getValue()).isEqualTo(request2.getExternalId());
        assertThat(result.getContent().get(2).getExternalId().getValue()).isEqualTo(request3.getExternalId());
    }

    @Test
    public void testClientSearchWorks_ByExternalId() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(request2.getExternalId());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getExternalId().getValue()).isEqualTo(request2.getExternalId());
    }

    @Test
    public void testClientSearchWorks_ByAccountNumber() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        PostClientsResponse response2 = clientHelper.createClient(request2);
        GetClientsClientIdResponse client2Data = ClientHelper.getClient(requestSpec, responseSpec,
                Math.toIntExact(response2.getClientId()));

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(client2Data.getAccountNo());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAccountNumber()).isEqualTo(client2Data.getAccountNo());
    }

    @Test
    public void testClientSearchWorks_ByDisplayName() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);
        String client2DisplayName = "%s %s".formatted(request2.getFirstname(), request2.getLastname());

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(client2DisplayName);
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDisplayName()).isEqualTo(client2DisplayName);
    }

    @Test
    public void testClientSearchWorks_ByMobileNo() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        request2.setMobileNo(Utils.randomNumberGenerator(8).toString());
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(request2.getMobileNo());
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMobileNo()).isEqualTo(request2.getMobileNo());
    }

    @Test
    public void testClientSearchDoesntReturnAnything_ByMobileNo() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request1);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(Utils.randomNumberGenerator(8).toString());
        // then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void testClientSearchWorks_ByClientIdentifier() {
        // given
        PostClientsRequest request1 = ClientHelper.defaultClientCreationRequest();
        request1.setMobileNo(Utils.randomNumberGenerator(8).toString());
        PostClientsResponse clientResponse = clientHelper.createClient(request1);
        final Long documentType = 1L;
        PostClientsClientIdIdentifiersRequest identifierRequest = ClientHelper.createClientIdentifer(documentType);
        final String documentKey = identifierRequest.getDocumentKey();
        PostClientsClientIdIdentifiersResponse clientIdentifierResponse = clientHelper.createClientIdentifer(clientResponse.getClientId(),
                identifierRequest);

        PostClientsRequest request2 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request2);

        PostClientsRequest request3 = ClientHelper.defaultClientCreationRequest();
        clientHelper.createClient(request3);
        // when
        PageClientSearchData result = clientHelper.searchClients(documentKey);
        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getMobileNo()).isEqualTo(request1.getMobileNo());
    }

}
