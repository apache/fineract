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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@Ignore
public class CampaignsTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private CampaignsHelper campaignsHelper;

    private static final String NON_TRIGGERED_REPORT_NAME = "Prospective Clients";
    private static final String TRIGGERED_REPORT_NAME = "Client Activated";

    private static final Integer DIRECT_TRIGGER_TYPE = 1;
    private static final Integer SCHEDULED_TRIGGER_TYPE = 2;
    private static final Integer TRIGGERED_TRIGGER_TYPE = 3;

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String CLOSE_COMMAND = "close";
    private static final String REACTIVATE_COMMAND = "reactivate";

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.campaignsHelper = new CampaignsHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsDirect() {
        // creating new campaign
        Integer campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
        this.campaignsHelper.verifyCampaignCreatedOnServer(this.requestSpec, this.responseSpec, campaignId);

        // updating campaign
        Integer updatedCampaignId = this.campaignsHelper.updateCampaign(this.requestSpec, this.responseSpec, campaignId,
                NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
        assertEquals(campaignId, updatedCampaignId);

        // activating campaign
        Integer activatedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                ACTIVATE_COMMAND);
        assertEquals(activatedCampaignId, campaignId);

        // closing campaign
        Integer closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // reactivating campaign
        Integer reactivateCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                REACTIVATE_COMMAND);
        assertEquals(reactivateCampaignId, campaignId);

        // closing campaign again for deletion
        closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId, CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // deleting campaign
        Integer deletedCampaignId = this.campaignsHelper.deleteCampaign(this.requestSpec, this.responseSpec, campaignId);
        assertEquals(deletedCampaignId, campaignId);
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsScheduled() {
        // creating new campaign
        Integer campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, SCHEDULED_TRIGGER_TYPE);
        this.campaignsHelper.verifyCampaignCreatedOnServer(this.requestSpec, this.responseSpec, campaignId);

        // updating campaign
        Integer updatedCampaignId = this.campaignsHelper.updateCampaign(this.requestSpec, this.responseSpec, campaignId,
                NON_TRIGGERED_REPORT_NAME, SCHEDULED_TRIGGER_TYPE);
        assertEquals(campaignId, updatedCampaignId);

        // activating campaign
        Integer activatedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                ACTIVATE_COMMAND);
        assertEquals(activatedCampaignId, campaignId);

        // closing campaign
        Integer closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // reactivating campaign
        Integer reactivateCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                REACTIVATE_COMMAND);
        assertEquals(reactivateCampaignId, campaignId);

        // closing campaign again for deletion
        closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId, CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // deleting campaign
        Integer deletedCampaignId = this.campaignsHelper.deleteCampaign(this.requestSpec, this.responseSpec, campaignId);
        assertEquals(deletedCampaignId, campaignId);
    }

    @Test
    public void testSupportedActionsForCampaignWithTriggerTypeAsTriggered() {
        // creating new campaign
        Integer campaignId = this.campaignsHelper.createCampaign(TRIGGERED_REPORT_NAME, TRIGGERED_TRIGGER_TYPE);
        this.campaignsHelper.verifyCampaignCreatedOnServer(this.requestSpec, this.responseSpec, campaignId);

        // updating campaign
        Integer updatedCampaignId = this.campaignsHelper.updateCampaign(this.requestSpec, this.responseSpec, campaignId,
                TRIGGERED_REPORT_NAME, TRIGGERED_TRIGGER_TYPE);
        assertEquals(campaignId, updatedCampaignId);

        // activating campaign
        Integer activatedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                ACTIVATE_COMMAND);
        assertEquals(activatedCampaignId, campaignId);

        // closing campaign
        Integer closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // reactivating campaign
        Integer reactivateCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                REACTIVATE_COMMAND);
        assertEquals(reactivateCampaignId, campaignId);

        // closing campaign again for deletion
        closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId, CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // deleting campaign
        Integer deletedCampaignId = this.campaignsHelper.deleteCampaign(this.requestSpec, this.responseSpec, campaignId);
        assertEquals(deletedCampaignId, campaignId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSupportedActionsForCampaignWithError() {
        final ResponseSpecification responseSpecWithError = new ResponseSpecBuilder().expectStatusCode(400).build();
        CampaignsHelper campaignsHelperWithError = new CampaignsHelper(this.requestSpec, responseSpecWithError);
        // creating new campaign
        Integer campaignId = this.campaignsHelper.createCampaign(NON_TRIGGERED_REPORT_NAME, DIRECT_TRIGGER_TYPE);
        this.campaignsHelper.verifyCampaignCreatedOnServer(this.requestSpec, this.responseSpec, campaignId);

        // activating campaign with failure
        ArrayList<HashMap> campaignDateValidationData = (ArrayList<HashMap>) campaignsHelperWithError.performActionsOnCampaignWithFailure(
                campaignId, ACTIVATE_COMMAND, Utils.getLocalDateOfTenant().plusDays(1).toString(DATE_FORMAT),
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.campaign.activationDate.in.the.future",
                ((HashMap<String, Object>) campaignDateValidationData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // activating campaign
        Integer activatedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                ACTIVATE_COMMAND);
        assertEquals(activatedCampaignId, campaignId);

        // activating campaign with failure
        ArrayList<HashMap> campaignErrorData = (ArrayList<HashMap>) campaignsHelperWithError.performActionsOnCampaignWithFailure(
                activatedCampaignId, ACTIVATE_COMMAND, Utils.getLocalDateOfTenant().toString(DATE_FORMAT), CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.campaign.already.active",
                ((HashMap<String, Object>) campaignErrorData.get(0)).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        // closing campaign again for deletion
        Integer closedCampaignId = this.campaignsHelper.performActionsOnCampaign(this.requestSpec, this.responseSpec, campaignId,
                CLOSE_COMMAND);
        assertEquals(closedCampaignId, campaignId);

        // deleting campaign
        Integer deletedCampaignId = this.campaignsHelper.deleteCampaign(this.requestSpec, this.responseSpec, campaignId);
        assertEquals(deletedCampaignId, campaignId);

    }
}
