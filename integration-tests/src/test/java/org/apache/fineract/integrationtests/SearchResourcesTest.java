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
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetSearchResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSearchHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.shares.ShareAccountHelper;
import org.apache.fineract.integrationtests.common.shares.ShareAccountTransactionHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SearchResourcesTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private FeignSearchHelper searchHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.searchHelper = new FeignSearchHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    public void searchAnyValueOverAllResources() {
        final String resources = "clients,clientIdentifiers,groups,savings,shares,loans";

        final String query = Utils.randomStringGenerator("C", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchAnyValueOverClientResources() {
        final String resources = "clients";

        final String query = Utils.randomStringGenerator("C", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchOverClientResources() {
        final String resources = "clients";

        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientResponse.getClientId();
        final GetClientsClientIdResponse getClientResponse = ClientHelper.getClient(requestSpec, responseSpec, clientId.intValue());
        final String query = getClientResponse.getAccountNo();

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);
        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());
        assertEquals(getClientResponse.getDisplayName(), searchResponse.get(0).getEntityName(), "Client name comparation");
    }

    @Test
    public void searchAnyValueOverLoanResources() {
        final String resources = "loans";

        final String query = Utils.randomStringGenerator("L", 12);
        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.TRUE);
        assertNotNull(searchResponse);
        assertEquals(0, searchResponse.size());
    }

    @Test
    public void searchOverSavingsResources() {
        final String resources = "savings";

        final PostClientsResponse clientResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientResponse.getClientId();

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");
        final SavingsAccountHelper savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        final String query = (String) savingsAccountHelper.getSavingsAccountDetail(savingsId, "accountNo");

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());

        final GetSearchResponse result = searchResponse.getFirst();

        assertEquals("SAVING", result.getEntityType());
        assertNotNull(result.getEntityStatus());
        assertNotNull(result.getEntityStatus().getId());
        assertNotNull(result.getEntityStatus().getCode());
        assertNotNull(result.getEntityStatus().getValue());
    }

    @Test
    public void searchOverSharesResources() {
        final String resources = "shares";

        final PostClientsResponse clientsResponse = ClientHelper.addClientAsPerson(ClientHelper.DEFAULT_OFFICE_ID,
                ClientHelper.LEGALFORM_ID_PERSON, null);
        final Long clientId = clientsResponse.getClientId();

        final ShareProductHelper shareProductHelper = new ShareProductHelper();
        final Integer productId = ShareProductTransactionHelper.createShareProduct(shareProductHelper.build(), requestSpec, responseSpec);

        final Integer savingsId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientId.intValue(), "1000");

        final String shareJson = new ShareAccountHelper().withClientId(String.valueOf(clientId)).withProductId(String.valueOf(productId))
                .withSavingsAccountId(String.valueOf(savingsId)).withSubmittedDate("01 January 2026").withApplicationDate("01 January 2026")
                .withRequestedShares("10").build();

        final Integer shareAccountId = ShareAccountTransactionHelper.createShareAccount(shareJson, requestSpec, responseSpec);

        final String approveJson = "{}";
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approveJson, requestSpec, responseSpec);

        final String activateJson = """
                {
                  "activatedDate": "01 January 2026",
                  "dateFormat": "dd MMMM yyyy",
                  "locale": "en"
                }
                """;
        ShareAccountTransactionHelper.postCommand("activate", shareAccountId, activateJson, requestSpec, responseSpec);

        final Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        final String query = (String) shareAccountData.get("accountNo");

        final List<GetSearchResponse> searchResponse = searchHelper.search(query, resources, Boolean.FALSE);

        assertNotNull(searchResponse);
        assertEquals(1, searchResponse.size());

        final GetSearchResponse result = searchResponse.getFirst();

        assertEquals("SHARE", result.getEntityType());
        assertNotNull(result.getEntityStatus());
        assertNotNull(result.getEntityStatus().getId());
        assertNotNull(result.getEntityStatus().getCode());
        assertNotNull(result.getEntityStatus().getValue());
    }
}
