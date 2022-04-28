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

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BatchInstanceModeTest {

    private ResponseSpecification responseSpec200;
    private ResponseSpecification responseSpec405;
    private RequestSpecification requestSpec;

    private static final String TENANT_PARAM_NAME = "tenantIdentifier";
    private static final String DEFAULT_TENANT = "default";
    private static final String TENANT_IDENTIFIER = TENANT_PARAM_NAME + '=' + DEFAULT_TENANT;

    private static final String HEALTH_URL = "/fineract-provider/actuator/health";
    private static final String LOGIN_URL = "/fineract-provider/api/v1/authentication?" + TENANT_IDENTIFIER;
    private static final String JOBS_URL = "/fineract-provider/api/v1/jobs";
    private static final String OFFICES_URL = "/fineract-provider/api/v1/offices";

    @BeforeEach
    public void setup() throws InterruptedException {
        initializeRestAssured();

        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();

        // Login with basic authentication
        awaitSpringBootActuatorHealthyUp();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.responseSpec200 = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec405 = new ResponseSpecBuilder().expectStatusCode(405).build();
    }

    @Test
    public void acceptWriteRequestWhenIsWriteOnly() {
        loginIntoServerAndGetBase64EncodedAuthenticationKey(requestSpec, responseSpec200);
    }

    @Test
    public void acceptReadRequestWhenIsWriteOnly() {
        this.requestSpec.header("Authorization",
                "Basic " + loginIntoServerAndGetBase64EncodedAuthenticationKey(requestSpec, responseSpec200));
        final int statusCode = getHeadOffice(requestSpec, responseSpec200);
        assertEquals(HttpStatus.SC_OK, statusCode);
    }

    @Test
    public void rejectBatchJobRequestWhenIsWriteOnly() {
        this.requestSpec.header("Authorization",
                "Basic " + loginIntoServerAndGetBase64EncodedAuthenticationKey(requestSpec, responseSpec200));

        final String GET_ALL_SCHEDULER_JOBS_URL = JOBS_URL + "?" + TENANT_IDENTIFIER;
        List<Map<String, Object>> allSchedulerJobsData = getAllSchedulerJobs(requestSpec, responseSpec200, GET_ALL_SCHEDULER_JOBS_URL);
        assertNotNull(allSchedulerJobsData);
        final String jobName = "Add Accrual Transactions";
        final int jobId = this.getSchedulerJobIdByName(allSchedulerJobsData, jobName);
        final int statusCode = runSchedulerJob(requestSpec, responseSpec405, jobId);
        assertEquals(HttpStatus.SC_METHOD_NOT_ALLOWED, statusCode);
    }

    private static void initializeRestAssured() {
        RestAssured.baseURI = "https://localhost";
        RestAssured.port = 8443;
        RestAssured.keyStore("src/main/resources/keystore.jks", "openmf");
        RestAssured.useRelaxedHTTPSValidation();
    }

    private static void awaitSpringBootActuatorHealthyUp() throws InterruptedException {
        int attempt = 0;
        final int max_attempts = 10;
        Response response = null;

        do {
            try {
                response = RestAssured.get(HEALTH_URL);

                if (response.statusCode() == 200) {
                    return;
                }

                Thread.sleep(3000);
            } catch (Exception e) {
                Thread.sleep(3000);
            }
        } while (attempt < max_attempts);

        fail(HEALTH_URL + " returned " + response.prettyPrint());
    }

    private static String loginIntoServerAndGetBase64EncodedAuthenticationKey(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        try {
            String json = given().spec(requestSpec).body("{\"username\":\"mifos\", \"password\":\"password\"}").expect().spec(responseSpec)
                    .log().ifError().when().post(LOGIN_URL).asString();
            assertThat("Failed to login into fineract platform", StringUtils.isBlank(json), is(false));
            String key = JsonPath.with(json).get("base64EncodedAuthenticationKey");
            assertThat("Failed to obtain key: " + json, StringUtils.isBlank(key), is(false));
            return key;
        } catch (final Exception e) {
            if (e instanceof HttpHostConnectException) {
                final HttpHostConnectException hh = (HttpHostConnectException) e;
                fail("Failed to connect to fineract platform:" + hh.getMessage());
            }

            throw new RuntimeException(e);
        }
    }

    private int getHeadOffice(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return performServerGet(requestSpec, responseSpec, OFFICES_URL + "/1?" + TENANT_IDENTIFIER);
    }

    private List<Map<String, Object>> getAllSchedulerJobs(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL) {
        List<Map<String, Object>> response = performServerGet(requestSpec, responseSpec, getURL, "");
        assertNotNull(response);
        return response;
    }

    private int getSchedulerJobIdByName(List<Map<String, Object>> allSchedulerJobsData, String jobName) {
        for (Integer jobIndex = 0; jobIndex < allSchedulerJobsData.size(); jobIndex++) {
            if (allSchedulerJobsData.get(jobIndex).get("displayName").equals(jobName)) {
                return (Integer) allSchedulerJobsData.get(jobIndex).get("jobId");
            }
        }
        throw new IllegalArgumentException(
                "No such named Job (see org.apache.fineract.infrastructure.jobs.service.JobName enum):" + jobName);
    }

    private static String runSchedulerJobAsJSON() {
        final Map<String, String> map = new HashMap<>();
        String runSchedulerJob = new Gson().toJson(map);
        return runSchedulerJob;
    }

    private int runSchedulerJob(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final int jobId) {
        final String RUN_SCHEDULER_JOB_URL = JOBS_URL + "/" + jobId + "?command=executeJob&" + TENANT_IDENTIFIER;
        return performServerPost(requestSpec, responseSpec, RUN_SCHEDULER_JOB_URL, runSchedulerJobAsJSON());
    }

    @SuppressWarnings("unchecked")
    private static <T> T performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String getURL, final String jsonAttributeToGetBack) {
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getURL).andReturn().asString();
        if (jsonAttributeToGetBack == null) {
            return (T) json;
        }
        return (T) JsonPath.from(json).get(jsonAttributeToGetBack);
    }

    private int performServerGet(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String getURL) {
        return given().spec(requestSpec).expect().spec(responseSpec).when().get(getURL).getStatusCode();
    }

    private int performServerPost(final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final String postURL,
            final String jsonBodyToSend) {
        return given().spec(requestSpec).body(jsonBodyToSend).expect().spec(responseSpec).when().post(postURL).getStatusCode();
    }
}
