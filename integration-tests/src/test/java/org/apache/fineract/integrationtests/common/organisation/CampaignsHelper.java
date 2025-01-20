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

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.report.ReportData;
import org.springframework.util.Assert;

@Slf4j
public class CampaignsHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String SMS_CAMPAIGNS_URL = "/fineract-provider/api/v1/smscampaigns";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm:ss";

    private static final String BUSINESS_RULE_OPTIONS = "businessRulesOptions";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public CampaignsHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer createCampaign(String reportName, Integer triggerType) {
        log.info("---------------------------------CREATING A CAMPAIGN---------------------------------------------");
        final String CREATE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_SMS_CAMPAIGNS_URL, getCreateCampaignJSON(reportName, triggerType),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyCampaignCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId) {
        log.info("------------------------------CHECK CAMPAIGN DETAILS------------------------------------\n");
        final String RETRIEVE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseCampaignId = Utils.performServerGet(requestSpec, responseSpec, RETRIEVE_SMS_CAMPAIGNS_URL, "id");
        assertEquals(generatedCampaignId, responseCampaignId, "ERROR IN CREATING THE CAMPAIGN");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer updateCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId, String reportName, Integer triggerType) {
        log.info("------------------------------UPDATE CAMPAIGN DETAILS------------------------------------\n");
        final String UPDATE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_SMS_CAMPAIGNS_URL, getUpdateCampaignJSON(reportName, triggerType),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer deleteCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId) {
        log.info("------------------------------DELETE CAMPAIGN DETAILS------------------------------------\n");
        final String DELETE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(requestSpec, responseSpec, DELETE_SMS_CAMPAIGNS_URL, "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer performActionsOnCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId, String command) {
        log.info("------------------------------PERFORM ACTION ON CAMPAIGN DETAILS------------------------------------\n");
        final String SMS_CAMPAIGNS_ACTION_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
        String actionDate = Utils.getLocalDateOfTenant().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        return Utils.performServerPost(requestSpec, responseSpec, SMS_CAMPAIGNS_ACTION_URL, getJSONForCampaignAction(command, actionDate),
                "resourceId");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Object performActionsOnCampaignWithFailure(final Integer generatedCampaignId, String command, String actionDate,
            String responseJsonAttribute) {
        log.info("--------------------------PERFORM ACTION ON CAMPAIGN DETAILS WITH FAILURE-------------------------------\n");
        final String SMS_CAMPAIGNS_ACTION_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SMS_CAMPAIGNS_ACTION_URL,
                getJSONForCampaignAction(command, actionDate), responseJsonAttribute);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String getCreateCampaignJSON(String reportName, Integer triggerType) {
        final HashMap<String, Object> map = new HashMap<>();
        final HashMap<String, Object> paramValueMap = new HashMap<>();
        Long reportId = getSelectedReportId(reportName);
        map.put("providerId", 1);
        map.put("triggerType", triggerType);
        if (2 == triggerType) {
            map.put("recurrenceStartDate",
                    Utils.getLocalDateTimeOfTenant().plusMinutes(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
            map.put("frequency", 1);
            map.put("interval", "1");
        }
        map.put("campaignName", Utils.randomStringGenerator("Campaign_Name_", 5));
        map.put("campaignType", 1);
        map.put("message", "Hi, this is from integtration tests runner");
        map.put("locale", "en");
        map.put("dateFormat", DATE_FORMAT);
        map.put("dateTimeFormat", DATE_TIME_FORMAT);
        map.put("runReportId", reportId);
        paramValueMap.put("officeId", "1");
        paramValueMap.put("loanOfficerId", "1");
        paramValueMap.put("reportName", reportName);
        map.put("paramValue", paramValueMap);
        String json = new Gson().toJson(map);
        log.info("{}", json);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String getUpdateCampaignJSON(String reportName, Integer triggerType) {
        final HashMap<String, Object> map = new HashMap<>();
        final HashMap<String, Object> paramValueMap = new HashMap<>();
        Long reportId = getSelectedReportId(reportName);
        map.put("providerId", 1);
        map.put("triggerType", triggerType);
        if (2 == triggerType) {
            map.put("recurrenceStartDate",
                    Utils.getLocalDateTimeOfTenant().plusMinutes(1).format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        }
        map.put("campaignName", Utils.randomStringGenerator("Campaign_Name_", 5));
        map.put("campaignType", 1);
        map.put("message", "Hi, this is from integtration tests runner");
        map.put("locale", "en");
        map.put("dateFormat", DATE_FORMAT);
        map.put("dateTimeFormat", DATE_TIME_FORMAT);
        map.put("runReportId", reportId);
        paramValueMap.put("officeId", "1");
        paramValueMap.put("loanOfficerId", "1");
        paramValueMap.put("reportName", reportName);
        map.put("paramValue", paramValueMap);
        String json = new Gson().toJson(map);
        log.info("{}", json);
        return json;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String getJSONForCampaignAction(String command, String actionDate) {
        final HashMap<String, Object> map = new HashMap<>();
        String dateString = "close".equalsIgnoreCase(command) ? "closureDate" : "activationDate";
        map.put(dateString, actionDate);
        map.put("locale", "en");
        map.put("dateFormat", DATE_FORMAT);
        String json = new Gson().toJson(map);
        log.info("{}", json);
        return json;
    }

    public List<ReportData> getReports() {
        return getReports(BUSINESS_RULE_OPTIONS);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private List<ReportData> getReports(String jsonAttributeToGetBack) {
        log.info("--------------------------------- GET REPORTS OPTIONS -------------------------------");
        Assert.notNull(jsonAttributeToGetBack, "jsonAttributeToGetBack may not be null");
        final String templateUrl = SMS_CAMPAIGNS_URL + "/template?" + Utils.TENANT_IDENTIFIER;
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(templateUrl).andReturn()
                .asString();
        Assert.notNull(json, "json");
        return JsonPath.from(json).getList(jsonAttributeToGetBack, ReportData.class);
    }

    private Long getSelectedReportId(final String reportName) {
        List<ReportData> reports = getReports();

        if (reports != null && !reports.isEmpty()) {
            for (ReportData reportData : reports) {
                if (reportName.equals(reportData.getReportName())) {
                    return reportData.getReportId();
                }
            }
        }
        Assert.notNull(null, "null");
        return null;
    }
}
