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

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.creditbureau.data.CreditBureauReportData;
import org.apache.fineract.integrationtests.common.CreditBureauConfigurationHelper;
import org.apache.fineract.integrationtests.common.CreditBureauIntegrationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance().options(wireMockConfig().port(3558)).build();

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        configureCreditBureauService();
    }

    private void configureCreditBureauService() {
        String organisations = CreditBureauConfigurationHelper.getOrganisationCreditBureauConfiguration();

        if (new Gson().fromJson(organisations, List.class).isEmpty()) {
            CreditBureauConfigurationHelper.addOrganisationCreditBureau(1L, "SAMPLE_ALIAS", true);
        } else {
            CreditBureauConfigurationHelper.updateOrganisationCreditBureau("1", true);
        }
        String configJson = CreditBureauConfigurationHelper.getCreditBureauConfiguration(1L);
        List<Map<String, Object>> configurations = new Gson().fromJson(configJson, new TypeToken<List<Map<String, Object>>>() {}.getType());
        Assertions.assertNotNull(configurations);
        Map<String, Long> currentConfiguration = configurations.stream()
                .collect(Collectors.toMap(k -> String.valueOf(k.get("configurationKey")).toUpperCase(),
                        v -> ((Number) v.get("creditBureauConfigurationId")).longValue()));
        final String usernameResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("USERNAME"), "USERNAME", "testUser");
        Assertions.assertNotNull(usernameResponse);
        final String passwordResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("PASSWORD"), "PASSWORD", "testPassword");
        Assertions.assertNotNull(passwordResponse);
        final String creditReportUrlResponse = CreditBureauConfigurationHelper.updateCreditBureauConfiguration(
                currentConfiguration.get("CREDITREPORTURL"), "CREDITREPORTURL", "http://localhost:3558/report/");
        Assertions.assertNotNull(creditReportUrlResponse);
        final String searchUrlResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("SEARCHURL"), "SEARCHURL", "http://localhost:3558/search/");
        Assertions.assertNotNull(searchUrlResponse);
        final String tokenUrlResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("TOKENURL"), "TOKENURL", "http://localhost:3558/token/");
        Assertions.assertNotNull(tokenUrlResponse);
        final String subscriptionIdResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("SUBSCRIPTIONID"), "SUBSCRIPTIONID", "subscriptionID123");
        Assertions.assertNotNull(subscriptionIdResponse);
        final String subscriptionKeyResponse = CreditBureauConfigurationHelper
                .updateCreditBureauConfiguration(currentConfiguration.get("SUBSCRIPTIONKEY"), "SUBSCRIPTIONKEY", "subscriptionKey456");
        Assertions.assertNotNull(subscriptionKeyResponse);
        final String addCreditReportUrlResponse = CreditBureauConfigurationHelper.updateCreditBureauConfiguration(
                currentConfiguration.get("ADDCREDITREPORTURL"), "addCreditReporturl", "http://localhost:3558/upload/");
        Assertions.assertNotNull(addCreditReportUrlResponse);
    }

    @Test
    public void creditBureauIntegrationTest() throws JsonProcessingException {
        ObjectNode jsonResponse = MAPPER.createObjectNode();
        jsonResponse.put("access_token", "AccessToken");
        jsonResponse.put("expires_in", 3600);
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("userName", "testUser");
        jsonResponse.put(".issued", "sample");
        jsonResponse.put(".expires", ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(3600)
                .format(new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy kk:mm:ss zzz").toFormatter()));
        wm.stubFor(WireMock.post("/token/").willReturn(WireMock.jsonResponse(MAPPER.writeValueAsString(jsonResponse), 200)));
        wm.stubFor(WireMock.post("/search/NRC213")
                .willReturn(WireMock.jsonResponse("{\"ResponseMessage\":\"OK\",\"Data\":[{\"UniqueID\":\"123456\"}]}", 200)));
        wm.stubFor(WireMock.get("/report/123456").willReturn(
                WireMock.jsonResponse("{\"ResponseMessage\":\"OK\",\"Data\":{" + "\"BorrowerInfo\":{" + "\"Name\":\"Test Name\","
                        + "\"Gender\":\"male\"," + "\"Address\":\"Test Address\"" + "}," + "\"CreditScore\": {\"Score\":  \"500\"},"
                        + "\"ActiveLoans\": [\"Loan1\", \"Loan2\"]," + "\"WriteOffLoans\": [\"Loan3\", \"Loan4\"]" + "}}", 200)));

        Object serviceResult = CreditBureauIntegrationHelper.getCreditReport(this.requestSpec, this.responseSpec, "1", "NRC213");
        Assertions.assertNotNull(serviceResult);
        Gson gson = new Gson();
        CreditBureauReportData responseData = gson.fromJson(
                gson.toJson(JsonParser.parseString(String.valueOf(serviceResult)).getAsJsonObject().get("creditBureauReportData")),
                CreditBureauReportData.class);
        Assertions.assertEquals("\"Test Name\"", responseData.getName());
        Assertions.assertEquals("{\"Score\":\"500\"}", responseData.getCreditScore());

        Assertions.assertEquals("\"male\"", responseData.getGender());
        Assertions.assertEquals("\"Test Address\"", responseData.getAddress());

        Assertions.assertEquals(2, responseData.getClosedAccounts().length);
        Assertions.assertEquals(2, responseData.getOpenAccounts().length);
        Assertions.assertEquals("\"Loan3\"", responseData.getClosedAccounts()[0]);
        Assertions.assertEquals("\"Loan4\"", responseData.getClosedAccounts()[1]);
        Assertions.assertEquals("\"Loan1\"", responseData.getOpenAccounts()[0]);
        Assertions.assertEquals("\"Loan2\"", responseData.getOpenAccounts()[1]);
    }

    @Test
    public void creditBureauNoLoanTest() throws JsonProcessingException {
        ObjectNode jsonResponse = MAPPER.createObjectNode();
        jsonResponse.put("access_token", "AccessToken");
        jsonResponse.put("expires_in", 3600);
        jsonResponse.put("token_type", "Bearer");
        jsonResponse.put("userName", "testUser");
        jsonResponse.put(".issued", "sample");
        jsonResponse.put(".expires", ZonedDateTime.now(ZoneId.systemDefault()).plusSeconds(3600)
                .format(new DateTimeFormatterBuilder().appendPattern("EEE, dd MMM yyyy kk:mm:ss zzz").toFormatter()));
        wm.stubFor(WireMock.post("/token/").willReturn(WireMock.jsonResponse(MAPPER.writeValueAsString(jsonResponse), 200)));
        wm.stubFor(WireMock.post("/search/NRC213")
                .willReturn(WireMock.jsonResponse("{\"ResponseMessage\":\"OK\",\"Data\":[{\"UniqueID\":\"123456\"}]}", 200)));
        wm.stubFor(WireMock.get("/report/123456")
                .willReturn(WireMock.jsonResponse("{\"ResponseMessage\":\"OK\",\"Data\":{" + "\"BorrowerInfo\":{"
                        + "\"Name\":\"Test Name\"," + "\"Gender\":\"male\"," + "\"Address\":\"Test Address\"" + "},"
                        + "\"CreditScore\": {\"Score\":  \"500\"}," + "\"ActiveLoans\": []," + "\"WriteOffLoans\": []" + "}}", 200)));

        Object serviceResult = CreditBureauIntegrationHelper.getCreditReport(this.requestSpec, this.responseSpec, "1", "NRC213");
        Assertions.assertNotNull(serviceResult);
        Gson gson = new Gson();
        CreditBureauReportData responseData = gson.fromJson(
                gson.toJson(JsonParser.parseString(String.valueOf(serviceResult)).getAsJsonObject().get("creditBureauReportData")),
                CreditBureauReportData.class);
        Assertions.assertEquals("\"Test Name\"", responseData.getName());
        Assertions.assertEquals("{\"Score\":\"500\"}", responseData.getCreditScore());

        Assertions.assertEquals("\"male\"", responseData.getGender());
        Assertions.assertEquals("\"Test Address\"", responseData.getAddress());

        Assertions.assertEquals(0, responseData.getClosedAccounts().length);
        Assertions.assertEquals(0, responseData.getOpenAccounts().length);
    }

}
