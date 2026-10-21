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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.GetReportsResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Runs the stored SQL of every enabled table report. The integration tests run against PostgreSQL, MySQL and MariaDB,
 * and each database has its own copy of the report SQL, so this catches a report that only breaks on one of them.
 */
public class StretchyReportsSmokeTest extends FeignIntegrationTest {

    /** Seeded rows that have no SQL of their own (report_sql is the text "(NULL)"). */
    private static final Set<String> NOT_RUNNABLE = Set.of("ReportCategoryList", "FullReportList");

    /** A value for each report parameter, keyed by stretchy_parameter.parameter_name. */
    private static final Map<String, Map.Entry<String, String>> PARAMETER_VALUES = Map.ofEntries(
            Map.entry("OfficeIdSelectOne", Map.entry("R_officeId", "1")),
            Map.entry("loanOfficerIdSelectAll", Map.entry("R_loanOfficerId", "-1")),
            Map.entry("currencyIdSelectAll", Map.entry("R_currencyId", "-1")), Map.entry("fundIdSelectAll", Map.entry("R_fundId", "-1")),
            Map.entry("loanProductIdSelectAll", Map.entry("R_loanProductId", "-1")),
            Map.entry("loanPurposeIdSelectAll", Map.entry("R_loanPurposeId", "-1")),
            Map.entry("parTypeSelect", Map.entry("R_parType", "1")), Map.entry("startDateSelect", Map.entry("R_startDate", "2013-01-01")),
            Map.entry("endDateSelect", Map.entry("R_endDate", "2030-12-31")),
            Map.entry("obligDateTypeSelect", Map.entry("R_obligDateType", "1")),
            Map.entry("SavingsAccountSubStatus", Map.entry("R_subStatus", "100")),
            Map.entry("SelectGLAccountNO", Map.entry("R_GLAccountNO", "1")));

    private static final String PAR_COLUMN = "Portfolio at Risk %";

    Stream<String> tableReports() {
        return ok(() -> fineractClient().reports().retrieveAllReports()).stream()
                .filter(report -> Boolean.TRUE.equals(report.getUseReport()) && "Table".equals(report.getReportType()))
                .map(GetReportsResponse::getReportName).filter(name -> !NOT_RUNNABLE.contains(name)).sorted();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tableReports")
    void tableReportRuns(String reportName) {
        ApiResponse<RunReportsResponse> response = runReport(reportName, parametersOf(reportName));

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData().getColumnHeaders()).as("column headers of %s", reportName).isNotEmpty();
    }

    /**
     * The percentage is built with CAST(... AS CHAR), which on PostgreSQL meant CHAR(1) and kept only the first digit.
     */
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "Portfolio at Risk", "Portfolio at Risk by Branch", "Active Loans - Summary",
            "Active Loans in last installment Summary", "Active Loans Passed Final Maturity Summary" })
    void portfolioAtRiskPercentageKeepsTwoDecimals(String reportName) {
        for (String parType : List.of("1", "2", "3", "4")) {
            Map<String, String> parameters = new HashMap<>(parametersOf(reportName));
            parameters.put("R_parType", parType);
            RunReportsResponse result = runReport(reportName, parameters).getData();

            int column = columnIndex(result, PAR_COLUMN);
            assertThat(column).as("%s has a %s column", reportName, PAR_COLUMN).isNotNegative();
            for (ResultsetRowData row : result.getData()) {
                Object value = row.getRow().get(column);
                if (value != null) {
                    assertThat(value.toString()).as("%s with R_parType=%s", reportName, parType).matches("-?\\d+\\.\\d{2}");
                }
            }
        }
    }

    @Test
    void happyBirthdayReportRuns() {
        ApiResponse<RunReportsResponse> response = runReport("Happy Birthday", Map.of("R_officeId", "1", "R_loanOfficerId", "-1"));

        assertThat(response.getStatusCode()).isEqualTo(200);
        // SMS campaigns read the recipient from the "mobileNo" column
        assertThat(columnIndex(response.getData(), "mobileNo")).isNotNegative();
    }

    private ApiResponse<RunReportsResponse> runReport(String reportName, Map<String, String> parameters) {
        return ok(() -> fineractClient().runReports().runReportGetDataWithHttpInfo(reportName, parameters));
    }

    private Map<String, String> parametersOf(String reportName) {
        GetReportsResponse report = ok(() -> fineractClient().reports().retrieveAllReports()).stream()
                .filter(r -> reportName.equals(r.getReportName())).findFirst().orElseThrow();
        Map<String, String> parameters = new HashMap<>();
        List<Object> reportParameters = Objects.requireNonNullElse(report.getReportParameters(), List.of());
        for (Object reportParameter : reportParameters) {
            String parameterName = String.valueOf(((Map<?, ?>) reportParameter).get("parameterName"));
            Map.Entry<String, String> value = PARAMETER_VALUES.get(parameterName);
            assertThat(value).as("value for parameter %s of report %s, add it to PARAMETER_VALUES", parameterName, reportName).isNotNull();
            parameters.put(value.getKey(), value.getValue());
        }
        return parameters;
    }

    private static int columnIndex(RunReportsResponse result, String columnName) {
        List<ResultsetColumnHeaderData> headers = result.getColumnHeaders();
        for (int i = 0; i < headers.size(); i++) {
            if (columnName.equals(headers.get(i).getColumnName())) {
                return i;
            }
        }
        return -1;
    }
}
