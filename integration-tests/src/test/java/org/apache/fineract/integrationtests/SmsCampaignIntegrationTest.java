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
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.organisation.CampaignsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;

/**
 * Integration tests for SMS Campaign duplicate name validation.
 */
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { 9191 })
public class SmsCampaignIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private ResponseSpecification errorResponseSpec;
    private CampaignsHelper campaignsHelper;
    private final ClientAndServer client;

    public SmsCampaignIntegrationTest(ClientAndServer client) {
        this.client = client;
        this.client.when(HttpRequest.request().withMethod("GET").withPath("/smsbridges"))
                .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON).withBody(
                        "[{\"id\":1,\"tenantId\":1,\"phoneNo\":\"+1234567890\",\"providerName\":\"Dummy SMS Provider - Testing\",\"providerDescription\":\"Dummy, just for testing\"}]"));
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.errorResponseSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.campaignsHelper = new CampaignsHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testCreateCampaignWithDuplicateNameShouldFail() {
        String reportName = "Prospective Clients";
        int triggerType = 1;
        String campaignName = "Duplicate_Test_Campaign_" + System.currentTimeMillis();

        // Create first campaign with specific name
        Integer firstCampaignId = campaignsHelper.createCampaignWithName(reportName, triggerType, campaignName);
        assertNotNull(firstCampaignId, "First campaign should be created successfully");
        campaignsHelper.verifyCampaignCreatedOnServer(requestSpec, responseSpec, firstCampaignId);

        // Attempt to create second campaign with the same name - should fail
        List<HashMap> errors = campaignsHelper.createCampaignWithNameExpectingError(errorResponseSpec, reportName, triggerType,
                campaignName);

        assertNotNull(errors, "Error response should not be null");
        assertEquals(1, errors.size(), "Should have exactly one error");
        assertEquals("error.msg.sms.campaign.duplicate.name", errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE),
                "Error code should indicate duplicate campaign name");
    }

    @Test
    public void testCreateCampaignWithUniqueNameShouldSucceed() {
        String reportName = "Prospective Clients";
        int triggerType = 1;
        String campaignName1 = "Unique_Campaign_1_" + System.currentTimeMillis();
        String campaignName2 = "Unique_Campaign_2_" + System.currentTimeMillis();

        // Create first campaign
        Integer firstCampaignId = campaignsHelper.createCampaignWithName(reportName, triggerType, campaignName1);
        assertNotNull(firstCampaignId, "First campaign should be created successfully");

        // Create second campaign with different name - should succeed
        Integer secondCampaignId = campaignsHelper.createCampaignWithName(reportName, triggerType, campaignName2);
        assertNotNull(secondCampaignId, "Second campaign with different name should be created successfully");

        // Verify both campaigns exist
        campaignsHelper.verifyCampaignCreatedOnServer(requestSpec, responseSpec, firstCampaignId);
        campaignsHelper.verifyCampaignCreatedOnServer(requestSpec, responseSpec, secondCampaignId);
    }
}
