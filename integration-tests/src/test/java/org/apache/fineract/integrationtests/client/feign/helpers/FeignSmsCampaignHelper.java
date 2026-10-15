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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.SmsBusinessRulesData;
import org.apache.fineract.client.models.SmsCampaignCreationDto;
import org.apache.fineract.client.models.SmsCampaignData;
import org.apache.fineract.client.models.SmsCampaignHandlerDto;
import org.apache.fineract.client.models.SmsCampaignParamReq;
import org.apache.fineract.client.models.SmsCampaignUpdateDto;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignSmsCampaignHelper {

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm:ss";
    private static final long SCHEDULED_TRIGGER_TYPE = 2L;
    private static final String MESSAGE = "Hi, this is from integtration tests runner";

    private final FineractFeignClient fineractClient;

    public FeignSmsCampaignHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createCampaign(String reportName, long triggerType) {
        return createCampaign(reportName, triggerType, Utils.randomStringGenerator("Campaign_Name_", 5));
    }

    public Long createCampaign(String reportName, long triggerType, String campaignName) {
        return ok(() -> fineractClient.defaultApi().createSmsCampaign(creationRequest(reportName, triggerType, campaignName)))
                .getResourceId();
    }

    public CallFailedRuntimeException createCampaignExpectingError(String reportName, long triggerType, String campaignName) {
        SmsCampaignCreationDto request = creationRequest(reportName, triggerType, campaignName);
        return fail(() -> fineractClient.defaultApi().createSmsCampaign(request));
    }

    public SmsCampaignData retrieveCampaign(Long campaignId) {
        return ok(() -> fineractClient.defaultApi().retrieveOneSmsCampaign(campaignId));
    }

    public Long updateCampaign(Long campaignId, String reportName, long triggerType) {
        SmsCampaignUpdateDto request = new SmsCampaignUpdateDto().triggerType(triggerType)
                .campaignName(Utils.randomStringGenerator("Campaign_Name_", 5)).campaignType(1L).message(MESSAGE).locale("en")
                .dateFormat(DATE_FORMAT).dateTimeFormat(DATE_TIME_FORMAT).runReportId(reportId(reportName))
                .paramValue(paramValue(reportName));
        if (triggerType == SCHEDULED_TRIGGER_TYPE) {
            request.recurrenceStartDate(recurrenceStartDate());
        }
        return ok(() -> fineractClient.defaultApi().updateSmsCampaign(campaignId, request)).getResourceId();
    }

    public Long performAction(Long campaignId, String command) {
        return performAction(campaignId, command, Utils.getLocalDateOfTenant().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
    }

    public Long performAction(Long campaignId, String command, String actionDate) {
        SmsCampaignHandlerDto request = actionRequest(command, actionDate);
        return ok(() -> fineractClient.defaultApi().handleCommandsSmsCampaign(campaignId, request, command)).getResourceId();
    }

    public CallFailedRuntimeException performActionExpectingError(Long campaignId, String command, String actionDate) {
        SmsCampaignHandlerDto request = actionRequest(command, actionDate);
        return fail(() -> fineractClient.defaultApi().handleCommandsSmsCampaign(campaignId, request, command));
    }

    public Long deleteCampaign(Long campaignId) {
        CommandProcessingResult result = ok(() -> fineractClient.defaultApi().deleteSmsCampaign(campaignId));
        return result.getResourceId();
    }

    private SmsCampaignCreationDto creationRequest(String reportName, long triggerType, String campaignName) {
        SmsCampaignCreationDto request = new SmsCampaignCreationDto().providerId(1L).triggerType(triggerType).campaignName(campaignName)
                .campaignType(1L).message(MESSAGE).locale("en").dateFormat(DATE_FORMAT).dateTimeFormat(DATE_TIME_FORMAT)
                .runReportId(reportId(reportName)).paramValue(paramValue(reportName));
        if (triggerType == SCHEDULED_TRIGGER_TYPE) {
            request.recurrenceStartDate(recurrenceStartDate()).frequency("1").interval("1");
        }
        return request;
    }

    private static SmsCampaignHandlerDto actionRequest(String command, String actionDate) {
        SmsCampaignHandlerDto request = new SmsCampaignHandlerDto().locale("en").dateFormat(DATE_FORMAT);
        return "close".equalsIgnoreCase(command) ? request.closureDate(actionDate) : request.activationDate(actionDate);
    }

    private static SmsCampaignParamReq paramValue(String reportName) {
        return new SmsCampaignParamReq().officeId(1).loanOfficerId(1).reportName(reportName);
    }

    private static String recurrenceStartDate() {
        return Utils.getLocalDateTimeOfTenant().plusMinutes(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    private Long reportId(String reportName) {
        return ok(() -> fineractClient.defaultApi().retrieveTemplateSmsCampaign()).getBusinessRulesOptions().stream()
                .filter(report -> reportName.equals(report.getReportName())).map(SmsBusinessRulesData::getReportId).findFirst()
                .orElseThrow(() -> new IllegalStateException("No SMS campaign report named " + reportName));
    }
}
