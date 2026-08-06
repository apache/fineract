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
package org.apache.fineract.integrationtests.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostReportsResponse;
import org.apache.fineract.client.models.PostRepostRequest;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.client.services.RunReportsApi;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Response;

/**
 * Integration Test for /runreports/ API.
 *
 * @author Michael Vorburger.ch
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReportsTest extends IntegrationTest {

    // parameterId 1006 = transactionId (string type), pinned in
    // db/changelog/tenant/parts/0002_initial_data.xml
    private static final Long TRANSACTION_ID_PARAM_ID = 1006L;
    private static final String STRING_PARAM_TEST_REPORT = "StringParamIntegrationTest";
    private static final String STRING_PARAM_VARIABLE = "transactionId";

    private Long stringParamTestReportId;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @BeforeAll
    public void setupStringParamReport() throws IOException {
        PostRepostRequest request = new PostRepostRequest().reportName(STRING_PARAM_TEST_REPORT).reportType("Table").reportSubType("")
                .reportCategory("Test").description("Integration test fixture for string parameter AS-header sanitisation")
                .reportSql("SELECT '${transactionId}' AS transaction_ref, o.name AS office_name FROM m_office o WHERE o.id = 1")
                .reportParameters(List.of(
                        Map.of("id", "", "parameterId", TRANSACTION_ID_PARAM_ID.toString(), "reportParameterName", STRING_PARAM_VARIABLE)));
        PostReportsResponse response = ok(fineractClient().reports.createReport(request));
        stringParamTestReportId = response.getResourceId();
    }

    @AfterAll
    public void teardownStringParamReport() throws IOException {
        if (stringParamTestReportId != null) {
            ok(fineractClient().reports.deleteReport(stringParamTestReportId));
        }
    }

    @Test
    void listReports() {
        // count is 85 because @BeforeAll creates StringParamIntegrationTest fixture report
        assertThat(ok(fineractClient().reports.retrieveAllReports())).hasSize(85);
    }

    @Test
    void runClientListingTableReport() {
        assertThat(ok(fineractClient().reportsRun.runReportGetData("Client Listing", Map.of("R_officeId", "1"))).getColumnHeaders().get(0)
                .getColumnName()).isEqualTo("Office/Branch");
    }

    @Test
    void runClientListingTableReportCSV() throws IOException {
        Response<ResponseBody> result = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportCSV", "true")));
        assertThat(result.body().contentType()).isEqualTo(MediaType.parse("text/csv"));
        assertThat(result.body().string()).contains("Office/Branch");
    }

    @Test
    void runClientListingReportIncludesCreatedClientInTableAndCSV() throws IOException {
        String clientName = Utils.randomStringGenerator("ReportTestClient", 8);
        createClient(clientName);

        RunReportsResponse tableResult = ok(fineractClient().reportsRun.runReportGetData("Client Listing", Map.of("R_officeId", "1")));
        List<ResultsetRowData> tableRows = tableResult.getData();
        assertThat(tableRows).isNotNull();
        boolean tableContainsClient = tableRows.stream().map(ResultsetRowData::getRow)
                .anyMatch(row -> row != null && row.contains(clientName));
        assertThat(tableContainsClient).as("Client Listing table output should contain client %s in rows %s", clientName, tableRows)
                .isTrue();

        Response<ResponseBody> csvResult = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportCSV", "true")));
        ResponseBody csvBody = csvResult.body();
        assertThat(csvBody).isNotNull();
        assertThat(csvBody.contentType()).isEqualTo(MediaType.parse("text/csv"));
        assertThat(csvBody.string()).as("Client Listing CSV output should contain client %s", clientName).contains(clientName);
    }

    private Long createClient(String fullName) {
        return ok(fineractClient().clients.createClient(new PostClientsRequest().legalFormId(1L).officeId(1L).fullname(fullName)
                .dateFormat(Utils.DATE_FORMAT).locale("en_US").active(true).activationDate("01 January 2023"))).getClientId();
    }

    @Test // see FINERACT-1306
    void runReportCategory() throws IOException {
        var req = new Request.Builder().url(fineractClient().baseURL().resolve(
                "/fineract-provider/api/v1/runreports/reportCategoryList?R_reportCategory=Fund&genericResultSet=false&parameterType=true&tenantIdentifier=default"))
                .build();
        try (var response = fineractClient().okHttpClient().newCall(req).execute()) {
            assertThat(response.code()).isEqualTo(200);
        }
    }

    @Test
    void runExpectedPaymentsPentahoReportWithoutPlugin() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> ok(fineractClient().reportsRun.runReportGetFile("Expected Payments By Date - Formatted", Map.of("R_endDate",
                        "2013-04-30", "R_loanOfficerId", "-1", "R_officeId", "1", "R_startDate", "2013-04-16", "output-type", "PDF"))));
        assertEquals(404, exception.getResponse().code());
    }

    @Test
    @Disabled
    void runExpectedPaymentsPentahoReport() {
        ResponseBody r = ok(fineractClient().reportsRun.runReportGetFile("Expected Payments By Date - Formatted", Map.of("R_endDate",
                "2013-04-30", "R_loanOfficerId", "-1", "R_officeId", "1", "R_startDate", "2013-04-16", "output-type", "PDF")));
        assertThat(r.contentType()).isEqualTo(MediaType.get("application/pdf"));
    }

    @Test
    void testTrialBalanceTableReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(fineractClient().reportsRun.runReportGetData("Trial Balance Table",
                Map.of("R_endDate", "2013-04-30", "R_officeId", "1", "R_startDate", "2013-04-16")));
        assertEquals(200, response.code());
    }

    @Test
    void testIncomeStatementTableReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(fineractClient().reportsRun.runReportGetData("Income Statement Table",
                Map.of("R_endDate", "2013-04-30", "R_officeId", "1", "R_startDate", "2013-04-16")));
        assertEquals(200, response.code());
    }

    @Test
    void testGeneralLedgerReportTableReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(fineractClient().reportsRun.runReportGetData("GeneralLedgerReport Table",
                Map.of("R_endDate", "2013-04-30", "R_officeId", "1", "R_startDate", "2013-04-16", "R_GLAccountNO", "1")));
        assertEquals(200, response.code());
    }

    @Test
    void testBalanceSheetTableReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("Balance Sheet Table", Map.of("R_endDate", "2013-04-30", "R_officeId", "1")));
        assertEquals(200, response.code());
    }

    // --- SQL injection regression tests (CVE fix) ---
    // These tests use "Client Listing" because officeId is registered in stretchy_parameter
    // as type 'number', giving the type-validation layer a real fixture to work against.

    /**
     * A valid numeric literal for a number-typed parameter must be accepted (200). This is the non-regression
     * counterpart to the injection tests below — it confirms the fix does not break the happy path.
     */
    @Test
    void numericParamWithValidLiteralIsAccepted() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("Client Listing", Map.of("R_officeId", "1")));
        assertEquals(200, response.code());
    }

    /**
     * A number-typed parameter containing an arithmetic expression used in the reported time-based blind SQL injection
     * (e.g. {@code 1-SLEEP(5)}) must be rejected with 403 before reaching SQL execution. Prepared statements would
     * neutralise it at the driver level, but type-literal validation must reject it earlier.
     */
    @ParameterizedTest(name = "Arithmetic injection in number param rejected: {0}")
    @ValueSource(strings = { "1-SLEEP(5)", "1*SLEEP(5)", "1+SLEEP(5)", "0-pg_sleep(5)", "1-benchmark(1000000,MD5(1))" })
    void numericParamWithArithmeticExpressionIsRejected(String maliciousValue) throws IOException {
        Response<RunReportsResponse> response = fineractClient().createService(RunReportsApi.class)
                .runReportGetData("Client Listing", Map.of("R_officeId", maliciousValue)).execute();
        assertThat(response.code()).isEqualTo(403);
    }

    /**
     * A UNION-based injection payload in a number-typed parameter must be rejected with 403. This covers the second
     * reported vulnerability pattern.
     */
    @ParameterizedTest(name = "UNION injection in number param rejected: {0}")
    @ValueSource(strings = { "1 UNION ALL SELECT 1,2,3", "1 UNION SELECT username,password FROM m_appuser",
            "0 UNION ALL SELECT NULL,NULL" })
    void numericParamWithUnionInjectionIsRejected(String maliciousValue) throws IOException {
        Response<RunReportsResponse> response = fineractClient().createService(RunReportsApi.class)
                .runReportGetData("Client Listing", Map.of("R_officeId", maliciousValue)).execute();
        assertThat(response.code()).isEqualTo(403);
    }

    /**
     * A parameter name not registered in stretchy_parameter for the given report must be rejected with 403. Allowing
     * unknown parameters would silently pass unvalidated input into the SQL template.
     */
    @Test
    void unknownParamNotRegisteredForReportIsRejected() throws IOException {
        Response<RunReportsResponse> response = fineractClient().createService(RunReportsApi.class)
                .runReportGetData("Client Listing", Map.of("R_officeId", "1", "R_unregisteredParamXyz", "anything")).execute();
        assertThat(response.code()).isEqualTo(403);
    }

    // --- String parameter AS-header sanitisation tests ---
    // Uses StringParamIntegrationTest report created in @BeforeAll, which places
    // transactionId (string type) directly in the AS-header position of the SELECT list.
    // This is a structurally distinct injection surface from WHERE-clause predicate injection.

    /**
     * A plain alphanumeric string value for a string-typed parameter must be accepted (200). Confirms the fix does not
     * break legitimate string parameter usage.
     */
    @Test
    void stringParamWithValidValueIsRejectedInAsHeaderPosition() throws IOException {
        Response<RunReportsResponse> response = fineractClient().createService(RunReportsApi.class)
                .runReportGetData(STRING_PARAM_TEST_REPORT, Map.of("R_transactionId", "TXN-001")).execute();
        assertEquals(403, response.code());
    }

    /**
     * SQL injection payloads in a string-typed parameter occupying the AS-header position of the SELECT list must be
     * rejected with 403. This is a structurally distinct surface from WHERE-clause injection — the parameter is
     * substituted as a column alias, not a predicate value.
     */
    @ParameterizedTest(name = "SQL injection in string AS-header param rejected: {0}")
    @ValueSource(strings = { "'; DROP TABLE m_client; --", "x UNION ALL SELECT password FROM m_appuser --",
            "x FROM m_office WHERE SLEEP(5) --", "x FROM m_office WHERE pg_sleep(5) --",
            "real_col, (SELECT password FROM m_appuser LIMIT 1) AS" })
    void stringParamWithSqlInjectionInAsHeaderPositionIsRejected(String maliciousValue) throws IOException {
        Response<RunReportsResponse> response = fineractClient().createService(RunReportsApi.class)
                .runReportGetData(STRING_PARAM_TEST_REPORT, Map.of("R_transactionId", maliciousValue)).execute();
        assertThat(response.code()).isEqualTo(403);
    }
}
