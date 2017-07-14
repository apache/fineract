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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.report.ReportData;
import org.joda.time.LocalDateTime;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class CampaignsHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String SMS_CAMPAIGNS_URL = "/fineract-provider/api/v1/smscampaigns";
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    public static final String DATE_TIME_FORMAT = "dd MMMM yyyy HH:mm:ss";

    private static final String BUSINESS_RULE_OPTIONS = "businessRulesOptions";

    public CampaignsHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Integer createCampaign(String reportName, Integer triggerType) {
        System.out.println("---------------------------------CREATING A CAMPAIGN---------------------------------------------");
        final String CREATE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_SMS_CAMPAIGNS_URL, getCreateCampaignJSON(reportName, triggerType),
                "resourceId");
    }

    public void verifyCampaignCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId) {
        System.out.println("------------------------------CHECK CAMPAIGN DETAILS------------------------------------\n");
        final String RETRIEVE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseCampaignId = Utils.performServerGet(requestSpec, responseSpec, RETRIEVE_SMS_CAMPAIGNS_URL, "id");
        assertEquals("ERROR IN CREATING THE CAMPAIGN", generatedCampaignId, responseCampaignId);
    }

    public Integer updateCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId, String reportName, Integer triggerType) {
        System.out.println("------------------------------UPDATE CAMPAIGN DETAILS------------------------------------\n");
        final String UPDATE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_SMS_CAMPAIGNS_URL, getUpdateCampaignJSON(reportName, triggerType),
                "resourceId");
    }

    public Integer deleteCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId) {
        System.out.println("------------------------------DELETE CAMPAIGN DETAILS------------------------------------\n");
        final String DELETE_SMS_CAMPAIGNS_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(requestSpec, responseSpec, DELETE_SMS_CAMPAIGNS_URL, "resourceId");
    }

    public Integer performActionsOnCampaign(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedCampaignId, String command) {
        System.out.println("------------------------------PERFORM ACTION ON CAMPAIGN DETAILS------------------------------------\n");
        final String SMS_CAMPAIGNS_ACTION_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
        String actionDate = Utils.getLocalDateOfTenant().toString(DATE_FORMAT);
        return Utils
                .performServerPost(requestSpec, responseSpec, SMS_CAMPAIGNS_ACTION_URL, getJSONForCampaignAction(command, actionDate), "resourceId");
    }

    public Object performActionsOnCampaignWithFailure(final Integer generatedCampaignId, String command, String actionDate, String responseJsonAttribute) {
        System.out.println("--------------------------PERFORM ACTION ON CAMPAIGN DETAILS WITH FAILURE-------------------------------\n");
        final String SMS_CAMPAIGNS_ACTION_URL = SMS_CAMPAIGNS_URL + "/" + generatedCampaignId + "?command=" + command + "&"
                + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(this.requestSpec, this.responseSpec, SMS_CAMPAIGNS_ACTION_URL, getJSONForCampaignAction(command, actionDate),
                responseJsonAttribute);
    }

    public String getCreateCampaignJSON(String reportName, Integer triggerType) {
        final HashMap<String, Object> map = new HashMap<>();
        final HashMap<String, Object> paramValueMap = new HashMap<>();
        Long reportId = getSelectedReportId(reportName);
        map.put("providerId", 1);
        map.put("triggerType", triggerType);
        if (2 == triggerType) {
            map.put("recurrenceStartDate", LocalDateTime.now().toString(DATE_TIME_FORMAT));
            map.put("frequency", 1);
            map.put("interval", "1");
        }
        map.put("campaignName", Utils.randomNameGenerator("Campaign_Name_", 5));
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
        System.out.println(json);
        return json;
    }

    public String getUpdateCampaignJSON(String reportName, Integer triggerType) {
        final HashMap<String, Object> map = new HashMap<>();
        final HashMap<String, Object> paramValueMap = new HashMap<>();
        Long reportId = getSelectedReportId(reportName);
        map.put("providerId", 1);
        map.put("triggerType", triggerType);
        if (2 == triggerType) {
            map.put("recurrenceStartDate", LocalDateTime.now().toString(DATE_TIME_FORMAT));
        }
        map.put("campaignName", Utils.randomNameGenerator("Campaign_Name_", 5));
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
        System.out.println(json);
        return json;
    }

    public String getJSONForCampaignAction(String command, String actionDate) {
        final HashMap<String, Object> map = new HashMap<>();
        String dateString = ("close".equalsIgnoreCase(command)) ? "closureDate" : "activationDate";
        map.put(dateString, actionDate);
        map.put("locale", "en");
        map.put("dateFormat", DATE_FORMAT);
        String json = new Gson().toJson(map);
        System.out.println(json);
        return json;
    }

    public ArrayList<ReportData> getReports() {
        return getReports(BUSINESS_RULE_OPTIONS);
    }

    private ArrayList<ReportData> getReports(String jsonAttributeToGetBack) {
        System.out.println("--------------------------------- GET REPORTS OPTIONS -------------------------------");
        Assert.notNull(jsonAttributeToGetBack);
        final String templateUrl = SMS_CAMPAIGNS_URL + "/template?" + Utils.TENANT_IDENTIFIER;
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(templateUrl).andReturn()
                .asString();
        Assert.notNull(json);
        ArrayList<ReportData> reportsList = new ArrayList<>();
        String reportsString = new Gson().toJson(from(json).get(jsonAttributeToGetBack));
        Assert.notNull(reportsString);
        final Gson gson = new Gson();
        final Type typeOfHashMap = new TypeToken<List<ReportData>>() {}.getType();
        reportsList = gson.fromJson(reportsString, typeOfHashMap);
        return reportsList;
    }

    private Long getSelectedReportId(final String reportName) {
        ArrayList<ReportData> reports = getReports();

        if (reports != null && !reports.isEmpty()) {
            for (ReportData reportData : reports) {
                if (reportName.equals(reportData.getReportName())) { return reportData.getReportId(); }
            }
        }
        Assert.notNull(null);
        return null;
    }
}
