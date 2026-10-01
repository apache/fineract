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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.feign.services.SmsApi.RetrieveAllSmsByStatusQueryParams;
import org.apache.fineract.client.models.PageSmsData;
import org.apache.fineract.client.models.SmsCreationRequest;
import org.apache.fineract.client.models.SmsData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSmsCampaignHelper;
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
 * Integration tests for the retrieveAllSmsByStatus endpoint in SmsApiResource. Ensures correct retrieval of SMS
 * messages by campaign and status.
 */
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { 9191 })
public class SmsApiResourceIntegrationTest extends FeignIntegrationTest {

    private FeignSmsCampaignHelper campaignsHelper;
    private FeignClientHelper clientHelper;

    @BeforeEach
    public void setup(ClientAndServer client) {
        client.when(HttpRequest.request().withMethod("GET").withPath("/smsbridges"))
                .respond(HttpResponse.response().withContentType(MediaType.APPLICATION_JSON).withBody(
                        "[{\"id\":1,\"tenantId\":1,\"phoneNo\":\"+1234567890\",\"providerName\":\"Dummy SMS Provider - Testing\",\"providerDescription\":\"Dummy, just for testing\"}]"));
        this.campaignsHelper = new FeignSmsCampaignHelper(fineractClient());
        this.clientHelper = new FeignClientHelper(fineractClient());
    }

    /**
     * Test retrieving SMS messages by status for a valid campaign.
     */
    @Test
    public void testRetrieveAllSmsByStatus_validStatus() {
        String reportName = "Prospective Clients";
        long triggerType = 1L;
        Long campaignId = campaignsHelper.createCampaign(reportName, triggerType);
        assertEquals(campaignId, campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");
        campaignsHelper.performAction(campaignId, "activate");

        Long clientId = clientHelper.createClient();

        assertNotNull(ok(() -> fineractClient().sms()
                .createSms(new SmsCreationRequest().clientId(clientId).message("Integration test message").campaignId(campaignId)))
                .getResourceId());

        List<SmsData> allSms = ok(() -> fineractClient().sms().retrieveAllSms());
        Long status = null;
        for (SmsData sms : allSms) {
            if (sms.getClientId() != null && sms.getCampaignName() != null && sms.getClientId().equals(clientId)
                    && sms.getCampaignName().equals("Campaign_Name_" + Long.toHexString(campaignId).toUpperCase(Locale.ROOT))) {
                if (sms.getStatus() != null) {
                    status = sms.getStatus().getId();
                    break;
                }
            }
        }
        if (status == null) {
            status = 100L;
        }
        int limit = 10;
        PageSmsData page = retrieveAllSmsByStatus(campaignId, status, limit);
        assertNotNull(page.getPageItems());
        assertTrue(page.getPageItems().stream().anyMatch(sms -> clientId.equals(sms.getClientId())));
    }

    /**
     * Test retrieving SMS messages by status for an invalid status value.
     */
    @Test
    public void testRetrieveAllSmsByStatus_invalidStatus() {
        String reportName = "Prospective Clients";
        long triggerType = 1L;
        Long campaignId = campaignsHelper.createCampaign(reportName, triggerType);
        assertEquals(campaignId, campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");
        campaignsHelper.performAction(campaignId, "activate");

        long invalidStatus = 9999L;
        int limit = 10;
        assertNotNull(retrieveAllSmsByStatus(campaignId, invalidStatus, limit).getPageItems());
    }

    private PageSmsData retrieveAllSmsByStatus(Long campaignId, Long status, int limit) {
        return ok(() -> fineractClient().sms().retrieveAllSmsByStatus(campaignId,
                new RetrieveAllSmsByStatusQueryParams().status(status).limit(limit)));
    }
}
