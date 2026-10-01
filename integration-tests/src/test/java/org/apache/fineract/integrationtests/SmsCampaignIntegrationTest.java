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

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSmsCampaignHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
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
public class SmsCampaignIntegrationTest extends FeignIntegrationTest {

    private FeignSmsCampaignHelper campaignsHelper;

    @BeforeEach
    public void setup(ClientAndServer client) {
        client.when(HttpRequest.request().withMethod("GET").withPath("/smsbridges"))
                .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON).withBody(
                        "[{\"id\":1,\"tenantId\":1,\"phoneNo\":\"+1234567890\",\"providerName\":\"Dummy SMS Provider - Testing\",\"providerDescription\":\"Dummy, just for testing\"}]"));
        this.campaignsHelper = new FeignSmsCampaignHelper(fineractClient());
    }

    @Test
    public void testCreateCampaignWithDuplicateNameShouldFail() {
        String reportName = "Prospective Clients";
        long triggerType = 1L;
        String campaignName = "Duplicate_Test_Campaign_" + System.currentTimeMillis();

        // Create first campaign with specific name
        Long firstCampaignId = campaignsHelper.createCampaign(reportName, triggerType, campaignName);
        assertNotNull(firstCampaignId, "First campaign should be created successfully");
        assertEquals(firstCampaignId, campaignsHelper.retrieveCampaign(firstCampaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");

        // Attempt to create second campaign with the same name - should fail
        CallFailedRuntimeException error = campaignsHelper.createCampaignExpectingError(reportName, triggerType, campaignName);

        assertEquals(403, error.getStatus());
        assertEquals(1, FeignErrors.reportedErrors(error).size(), "Should have exactly one error");
        assertEquals("error.msg.sms.campaign.duplicate.name", FeignErrors.firstError(error).userMessageGlobalisationCode(),
                "Error code should indicate duplicate campaign name");
    }

    @Test
    public void testCreateCampaignWithUniqueNameShouldSucceed() {
        String reportName = "Prospective Clients";
        long triggerType = 1L;
        String campaignName1 = "Unique_Campaign_1_" + System.currentTimeMillis();
        String campaignName2 = "Unique_Campaign_2_" + System.currentTimeMillis();

        // Create first campaign
        Long firstCampaignId = campaignsHelper.createCampaign(reportName, triggerType, campaignName1);
        assertNotNull(firstCampaignId, "First campaign should be created successfully");

        // Create second campaign with different name - should succeed
        Long secondCampaignId = campaignsHelper.createCampaign(reportName, triggerType, campaignName2);
        assertNotNull(secondCampaignId, "Second campaign with different name should be created successfully");

        // Verify both campaigns exist
        assertEquals(firstCampaignId, campaignsHelper.retrieveCampaign(firstCampaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");
        assertEquals(secondCampaignId, campaignsHelper.retrieveCampaign(secondCampaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");
    }
}
