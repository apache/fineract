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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.CreditBureauConfigurationData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCreditBureauHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauTest.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String LOCAL_EXTERNAL_HOST = "localhost";
    private static final String DOCKER_EXTERNAL_HOST = "host.docker.internal";
    private static String creditBureauHost = ConfigProperties.ExternalServices.HOST;

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance().options(wireMockConfig().port(3558)).build();

    private FeignCreditBureauHelper creditBureauHelper;

    @BeforeEach
    public void setup() {
        creditBureauHelper = new FeignCreditBureauHelper(fineractClient());
        configureCreditBureauService(creditBureauHost);
    }

    private void configureCreditBureauService(String creditBureauHost) {
        String creditBureauUrl = "http://" + creditBureauHost + ":3558";

        if (creditBureauHelper.retrieveOrganisationCreditBureaus().isEmpty()) {
            creditBureauHelper.addOrganisationCreditBureau(1L, "SAMPLE_ALIAS", true);
        } else {
            creditBureauHelper.updateOrganisationCreditBureau(1L, true);
        }
        List<CreditBureauConfigurationData> configurations = creditBureauHelper.retrieveConfigurations(1L);
        Assertions.assertNotNull(configurations);
        Map<String, Long> currentConfiguration = configurations.stream()
                .collect(Collectors.toMap(k -> k.getConfigurationKey().toUpperCase(Locale.ROOT), v -> v.getCreditBureauConfigurationId()));
        final CommandProcessingResult usernameResponse = creditBureauHelper.updateConfiguration(currentConfiguration.get("USERNAME"),
                "USERNAME", "testUser");
        Assertions.assertNotNull(usernameResponse);
        final CommandProcessingResult passwordResponse = creditBureauHelper.updateConfiguration(currentConfiguration.get("PASSWORD"),
                "PASSWORD", "testPassword");
        Assertions.assertNotNull(passwordResponse);
        final CommandProcessingResult creditReportUrlResponse = creditBureauHelper
                .updateConfiguration(currentConfiguration.get("CREDITREPORTURL"), "CREDITREPORTURL", creditBureauUrl + "/report/");
        Assertions.assertNotNull(creditReportUrlResponse);
        final CommandProcessingResult searchUrlResponse = creditBureauHelper.updateConfiguration(currentConfiguration.get("SEARCHURL"),
                "SEARCHURL", creditBureauUrl + "/search/");
        Assertions.assertNotNull(searchUrlResponse);
        final CommandProcessingResult tokenUrlResponse = creditBureauHelper.updateConfiguration(currentConfiguration.get("TOKENURL"),
                "TOKENURL", creditBureauUrl + "/token/");
        Assertions.assertNotNull(tokenUrlResponse);
        final CommandProcessingResult subscriptionIdResponse = creditBureauHelper
                .updateConfiguration(currentConfiguration.get("SUBSCRIPTIONID"), "SUBSCRIPTIONID", "subscriptionID123");
        Assertions.assertNotNull(subscriptionIdResponse);
        final CommandProcessingResult subscriptionKeyResponse = creditBureauHelper
                .updateConfiguration(currentConfiguration.get("SUBSCRIPTIONKEY"), "SUBSCRIPTIONKEY", "subscriptionKey456");
        Assertions.assertNotNull(subscriptionKeyResponse);
        final CommandProcessingResult addCreditReportUrlResponse = creditBureauHelper
                .updateConfiguration(currentConfiguration.get("ADDCREDITREPORTURL"), "addCreditReporturl", creditBureauUrl + "/upload/");
        Assertions.assertNotNull(addCreditReportUrlResponse);
    }

    private Map<String, Object> getCreditReport(String creditBureauId, String nrc) {
        try {
            return creditBureauHelper.fetchCreditReport(creditBureauId, nrc).getCreditBureauReportData();
        } catch (CallFailedRuntimeException e) {
            if (!LOCAL_EXTERNAL_HOST.equals(creditBureauHost) || !isConnectionFailure(e)) {
                throw e;
            }

            creditBureauHost = DOCKER_EXTERNAL_HOST;
            configureCreditBureauService(creditBureauHost);
            return creditBureauHelper.fetchCreditReport(creditBureauId, nrc).getCreditBureauReportData();
        }
    }

    private boolean isConnectionFailure(CallFailedRuntimeException e) {
        return e.getMessage() != null && e.getMessage().contains("HTTP Response Code: 0");
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

        Map<String, Object> responseData = getCreditReport("1", "NRC213");
        Assertions.assertNotNull(responseData);
        Assertions.assertEquals("\"Test Name\"", responseData.get("name"));
        Assertions.assertEquals("{\"Score\":\"500\"}", responseData.get("creditScore"));

        Assertions.assertEquals("\"male\"", responseData.get("gender"));
        Assertions.assertEquals("\"Test Address\"", responseData.get("address"));

        List<?> closedAccounts = (List<?>) responseData.get("closedAccounts");
        List<?> openAccounts = (List<?>) responseData.get("openAccounts");
        Assertions.assertEquals(2, closedAccounts.size());
        Assertions.assertEquals(2, openAccounts.size());
        Assertions.assertEquals("\"Loan3\"", closedAccounts.get(0));
        Assertions.assertEquals("\"Loan4\"", closedAccounts.get(1));
        Assertions.assertEquals("\"Loan1\"", openAccounts.get(0));
        Assertions.assertEquals("\"Loan2\"", openAccounts.get(1));
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

        Map<String, Object> responseData = getCreditReport("1", "NRC213");
        Assertions.assertNotNull(responseData);
        Assertions.assertEquals("\"Test Name\"", responseData.get("name"));
        Assertions.assertEquals("{\"Score\":\"500\"}", responseData.get("creditScore"));

        Assertions.assertEquals("\"male\"", responseData.get("gender"));
        Assertions.assertEquals("\"Test Address\"", responseData.get("address"));

        Assertions.assertEquals(0, ((List<?>) responseData.get("closedAccounts")).size());
        Assertions.assertEquals(0, ((List<?>) responseData.get("openAccounts")).size());
    }

}
