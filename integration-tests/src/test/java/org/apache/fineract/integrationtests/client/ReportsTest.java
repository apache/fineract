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
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.client.services.RunReportsApi;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Response;

/**
 * Integration Test for /runreports/ API.
 *
 * @author Michael Vorburger.ch
 */
public class ReportsTest extends IntegrationTest {

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    void listReports() {
        assertThat(ok(fineractClient().reports.retrieveAllReports())).hasSize(84);
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

    @Test // see FINERACT-1306
    void runReportCategory() throws IOException {
        // Using raw OkHttp instead of Retrofit API here, because /runreports/reportCategoryList returns JSON Array -
        // but runReportGetData() expects columnHeaders/data JSON.
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
}
