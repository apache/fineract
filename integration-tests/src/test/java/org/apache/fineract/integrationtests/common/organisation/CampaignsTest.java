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
package org.apache.fineract.integrationtests.common.organisation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSmsCampaignHelper;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.MediaType;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = { 9191 })
public class CampaignsTest extends FeignIntegrationTest {

    private FeignSmsCampaignHelper campaignsHelper;

    private static final String NON_TRIGGERED_REPORT_NAME = "Prospective Clients";
    private static final String TRIGGERED_REPORT_NAME = "Client Activated";

    private static final long DIRECT_TRIGGER_TYPE = 1L;
    private static final long SCHEDULED_TRIGGER_TYPE = 2L;
    private static final long TRIGGERED_TRIGGER_TYPE = 3L;

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String CLOSE_COMMAND = "close";
    private static final String REACTIVATE_COMMAND = "reactivate";

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    @BeforeEach
    public void setup(ClientAndServer client) {
        // Set up mock server for message-gateway
        client.when(request().withMethod("GET").withPath("/smsbridges"))
                .respond(response().withContentType(MediaType.APPLICATION_JSON).withBody("[\n" //
                        + "    {\n" //
                        + "        \"id\": 1,\n" //
                        + "        \"tenantId\": 1,\n" //
                        + "        \"phoneNo\": \"+1234567890\",\n" //
                        + "        \"providerName\": \"Dummy SMS Provider - Testing\",\n" //
                        + "        \"providerDescription\": \"Dummy, just for testing\"\n" //
                        + "     }\n" //
                        + "]") //
                );
        this.campaignsHelper = new FeignSmsCampaignHelper(fineractClient());
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsDirect() {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            // creating new campaign
            Long campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
            assertEquals(campaignId, this.campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");

            // updating campaign
            Long updatedCampaignId = this.campaignsHelper.updateCampaign(campaignId, NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
            assertEquals(campaignId, updatedCampaignId);

            // activating campaign
            Long activatedCampaignId = this.campaignsHelper.performAction(campaignId, ACTIVATE_COMMAND);
            assertEquals(activatedCampaignId, campaignId);

            // closing campaign
            Long closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // reactivating campaign
            Long reactivateCampaignId = this.campaignsHelper.performAction(campaignId, REACTIVATE_COMMAND);
            assertEquals(reactivateCampaignId, campaignId);

            // closing campaign again for deletion
            closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // deleting campaign
            Long deletedCampaignId = this.campaignsHelper.deleteCampaign(campaignId);
            assertEquals(deletedCampaignId, campaignId);
        });
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsScheduled() {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            // creating new campaign
            Long campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, SCHEDULED_TRIGGER_TYPE);
            assertEquals(campaignId, this.campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");

            // updating campaign
            Long updatedCampaignId = this.campaignsHelper.updateCampaign(campaignId, NON_TRIGGERED_REPORT_NAME, SCHEDULED_TRIGGER_TYPE);
            assertEquals(campaignId, updatedCampaignId);

            // activating campaign
            Long activatedCampaignId = this.campaignsHelper.performAction(campaignId, ACTIVATE_COMMAND);
            assertEquals(activatedCampaignId, campaignId);

            // closing campaign
            Long closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // reactivating campaign
            Long reactivateCampaignId = this.campaignsHelper.performAction(campaignId, REACTIVATE_COMMAND);
            assertEquals(reactivateCampaignId, campaignId);

            // closing campaign again for deletion
            closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // deleting campaign
            Long deletedCampaignId = this.campaignsHelper.deleteCampaign(campaignId);
            assertEquals(deletedCampaignId, campaignId);
        });
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsTriggered() {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            // creating new campaign
            Long campaignId = this.campaignsHelper.createCampaign(TRIGGERED_REPORT_NAME, TRIGGERED_TRIGGER_TYPE);
            assertEquals(campaignId, this.campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");

            // updating campaign
            Long updatedCampaignId = this.campaignsHelper.updateCampaign(campaignId, TRIGGERED_REPORT_NAME, TRIGGERED_TRIGGER_TYPE);
            assertEquals(campaignId, updatedCampaignId);

            // activating campaign
            Long activatedCampaignId = this.campaignsHelper.performAction(campaignId, ACTIVATE_COMMAND);
            assertEquals(activatedCampaignId, campaignId);

            // closing campaign
            Long closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // reactivating campaign
            Long reactivateCampaignId = this.campaignsHelper.performAction(campaignId, REACTIVATE_COMMAND);
            assertEquals(reactivateCampaignId, campaignId);

            // closing campaign again for deletion
            closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // deleting campaign
            Long deletedCampaignId = this.campaignsHelper.deleteCampaign(campaignId);
            assertEquals(deletedCampaignId, campaignId);
        });
    }

    @Test
    public void testSupportedActionsForCampaignWithError() {
        BusinessDateHelper.runAt(DateTimeFormatter.ofPattern(DATE_FORMAT).format(Utils.getLocalDateOfTenant()), () -> {
            // creating new campaign
            Long campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
            assertEquals(campaignId, this.campaignsHelper.retrieveCampaign(campaignId).getId(), "ERROR IN CREATING THE CAMPAIGN");

            // activating campaign with failure
            CallFailedRuntimeException campaignDateValidationData = this.campaignsHelper.performActionExpectingError(campaignId,
                    ACTIVATE_COMMAND, Utils.getLocalDateOfTenant().plusDays(1).format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            assertEquals(400, campaignDateValidationData.getStatus());
            assertEquals("error.msg.campaign.activationDate.in.the.future",
                    FeignErrors.firstError(campaignDateValidationData).userMessageGlobalisationCode());

            // activating campaign
            Long activatedCampaignId = this.campaignsHelper.performAction(campaignId, ACTIVATE_COMMAND);
            assertEquals(activatedCampaignId, campaignId);

            // activating campaign with failure
            CallFailedRuntimeException campaignErrorData = this.campaignsHelper.performActionExpectingError(activatedCampaignId,
                    ACTIVATE_COMMAND, Utils.getLocalDateOfTenant().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            assertEquals(400, campaignErrorData.getStatus());
            assertEquals("error.msg.campaign.already.active", FeignErrors.firstError(campaignErrorData).userMessageGlobalisationCode());

            // closing campaign again for deletion
            Long closedCampaignId = this.campaignsHelper.performAction(campaignId, CLOSE_COMMAND);
            assertEquals(closedCampaignId, campaignId);

            // deleting campaign
            Long deletedCampaignId = this.campaignsHelper.deleteCampaign(campaignId);
            assertEquals(deletedCampaignId, campaignId);
        });
    }
}
