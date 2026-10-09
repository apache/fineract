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
package org.apache.fineract.test.stepdef.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class ReportingStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final String TRIAL_BALANCE_REPORT = "Trial Balance Summary Report with Asset Owner";
    private static final String WC_TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report for Working Capital Loans";
    private static final String WC_TRANSACTION_SUMMARY_REPORT_WITH_ASSET_OWNER = "Transaction Summary Report with Asset Owner for Working Capital Loans";
    private final FineractFeignClient fineractClient;

    @Then("Transaction Summary Report for date {string} has the following data:")
    public void transactionSummaryReportHasData(final String dateStr, final DataTable dataTable) {
        verifyReportData("Transaction Summary Report", dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for date {string} has the following data:")
    public void transactionSummaryReportWithAssetOwnerHasData(final String dateStr, final DataTable dataTable) {
        verifyReportData("Transaction Summary Report with Asset Owner", dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for date {string} column {string} has non-empty value for all rows")
    public void transactionSummaryReportWithAssetOwnerColumnNonEmpty(final String dateStr, final String columnName) {
        verifyColumnNullability("Transaction Summary Report with Asset Owner", dateStr, columnName, false);
    }

    @Then("Transaction Summary Report with Asset Owner for date {string} has originatorId, asset owner externalId and the following data:")
    public void transactionSummaryReportWithAssetOwnerHasDataWithOwnerExternalIdAndOriginatorId(final String dateStr,
            final DataTable dataTable) {
        verifyReportDataWithOwnerExternalIdAndOriginatorId("Transaction Summary Report with Asset Owner", dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for date {string} column {string} has empty value for all rows")
    public void transactionSummaryReportWithAssetOwnerColumnEmpty(final String dateStr, final String columnName) {
        verifyColumnNullability("Transaction Summary Report with Asset Owner", dateStr, columnName, true);
    }

    @Then("Transaction Summary Report for Working Capital Loans for date {string} has the following data:")
    public void transactionSummaryReportForWorkingCapitalLoansHasData(final String dateStr, final DataTable dataTable) {
        verifyReportDataWithEmptyValues(WC_TRANSACTION_SUMMARY_REPORT, dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for Working Capital Loans for date {string} has the following data:")
    public void transactionSummaryReportWithAssetOwnerForWorkingCapitalLoansHasData(final String dateStr, final DataTable dataTable) {
        verifyReportDataWithEmptyValues(WC_TRANSACTION_SUMMARY_REPORT_WITH_ASSET_OWNER, dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for Working Capital Loans for date {string} has originatorId, asset owner externalId and the following data:")
    public void transactionSummaryReportWithAssetOwnerForWorkingCapitalLoansHasDataWithOriginatorId(final String dateStr,
            final DataTable dataTable) {
        verifyReportDataWithOwnerExternalIdAndOriginatorId(WC_TRANSACTION_SUMMARY_REPORT_WITH_ASSET_OWNER, dateStr, dataTable);
    }

    @Then("Trial Balance Summary Report with Asset Owner for date {string} has originatorId, asset owner externalId and the following data:")
    public void trialBalanceSummaryReportWithAssetOwnerHasDataWithOwnerExternalIdAndOriginatorId(final String dateStr,
            final DataTable dataTable) {
        verifyBalanceReportDataWithOwnerExternalIdAndOriginatorId("Trial Balance Summary Report with Asset Owner", dateStr, dataTable);
    }

    @Then("Trial Balance Summary Report with Asset Owner for date {string} has a row for GL account {string} with non-zero ending balance")
    public void trialBalanceHasNonZeroEndingBalanceForGlAccount(final String dateStr, final String glCode) {
        final BigDecimal ending = sumColumnForGlAccount(executeReport(TRIAL_BALANCE_REPORT, dateStr), glCode, "endingbalance");
        assertThat(ending).as("Trial Balance for %s: expected GL account '%s' to be present", dateStr, glCode).isNotNull();
        assertThat(ending.signum())
                .as("Trial Balance for %s: expected GL account '%s' ending balance to be non-zero but was %s", dateStr, glCode, ending)
                .isNotEqualTo(0);
    }

    @Then("Trial Balance Summary Report with Asset Owner for date {string} shows GL account {string} closed out")
    public void trialBalanceShowsGlAccountClosedOut(final String dateStr, final String glCode) {
        final BigDecimal ending = sumColumnForGlAccount(executeReport(TRIAL_BALANCE_REPORT, dateStr), glCode, "endingbalance");
        if (ending != null) {
            assertThat(ending.signum()).as(
                    "Trial Balance for %s: expected GL account '%s' to be closed out (absent or zero ending balance) but it has ending balance %s",
                    dateStr, glCode, ending).isEqualTo(0);
        }
    }

    private void verifyReportData(final String reportName, final String dateStr, final DataTable dataTable) {
        final RunReportsResponse response = executeReport(reportName, dateStr);

        final List<List<String>> expected = dataTable.asLists();
        final List<String> headers = expected.getFirst();

        verifyReportData(reportName, response, expected, headers);
    }

    private void verifyReportDataWithEmptyValues(final String reportName, final String dateStr, final DataTable dataTable) {
        final RunReportsResponse response = executeReport(reportName, dateStr);

        final List<List<String>> expected = dataTable.asLists();
        final List<String> headers = expected.getFirst();

        verifyReportDataWithEmptyValues(reportName, response, expected, headers);
    }

    private List<List<String>> verifyReportDataHeaders(final String reportName, final RunReportsResponse response,
            final List<List<String>> expected, List<String> headers) {
        final List<ResultsetColumnHeaderData> columnHeaders = response.getColumnHeaders();
        assertThat(columnHeaders).isNotNull();
        final int[] colIdx = headers.stream().mapToInt(h -> findColumnIndex(columnHeaders, h)).toArray();

        final List<ResultsetRowData> data = response.getData();
        assertThat(data).as("Report '%s' returned no data", reportName).isNotNull();
        final List<List<String>> actual = data.stream().map(row -> {
            final List<Object> cells = row.getRow();
            assertThat(cells).as("Report '%s' returned a row with null cell list", reportName).isNotNull();
            return IntStream.of(colIdx).mapToObj(i -> {
                assertThat(i).as("Report '%s': column index %d is out of bounds (row size: %d)", reportName, i, cells.size())
                        .isLessThan(cells.size());
                return stringify(cells.get(i));
            }).toList();
        }).toList();

        assertThat(actual).as("Report '%s' row count mismatch.\nActual rows:\n%s", reportName, formatRows(actual))
                .hasSize(expected.size() - 1);

        return actual;
    }

    private void verifyReportDataRows(final String reportName, List<List<String>> actual, final List<List<String>> expected,
            List<String> headers) {
        for (int i = 1; i < expected.size(); i++) {
            final List<String> expRow = expected.get(i).stream().map(v -> v == null ? "" : v).toList();
            final List<String> actRow = actual.get(i - 1);
            for (int j = 0; j < headers.size(); j++) {
                if (expRow.get(j).isEmpty()) {
                    continue;
                }
                if (!valuesMatch(expRow.get(j), actRow.get(j))) {
                    fail("Report '%s', row %d, column '%s': expected='%s', actual='%s'\nAll actual rows:\n%s", reportName, i,
                            headers.get(j), expRow.get(j), actRow.get(j), formatRows(actual));
                }
            }
        }
    }

    private void verifyReportDataRowsWithEmptyValues(final String reportName, List<List<String>> actual, final List<List<String>> expected,
            List<String> headers) {
        for (int i = 1; i < expected.size(); i++) {
            final List<String> expRow = expected.get(i).stream().map(v -> v == null ? "" : v).toList();
            final List<String> actRow = actual.get(i - 1);
            for (int j = 0; j < headers.size(); j++) {
                if (expRow.get(j).isEmpty()) {
                    if (!actRow.get(j).isEmpty() && !"null".equals(actRow.get(j))) {
                        fail("Report '%s', row %d, column '%s': expected empty value, actual='%s'\nAll actual rows:\n%s", reportName, i,
                                headers.get(j), actRow.get(j), formatRows(actual));
                    }
                    continue;
                }
                if (!valuesMatch(expRow.get(j), actRow.get(j))) {
                    fail("Report '%s', row %d, column '%s': expected='%s', actual='%s'\nAll actual rows:\n%s", reportName, i,
                            headers.get(j), expRow.get(j), actRow.get(j), formatRows(actual));
                }
            }
        }
    }

    private void verifyBalanceReportData(final String reportName, List<List<String>> actual, final List<List<String>> expected,
            List<String> headers) {
        for (int i = 1; i < expected.size(); i++) {
            final List<String> expRow = expected.get(i).stream().map(v -> v == null ? "" : v).toList();
            String expectedDescription = expRow.get(3);
            String expectedAssetOwnerId = expRow.get(4);
            final List<String> actRow = actual.stream()
                    .filter(actualRow -> actualRow.contains(expectedDescription) && actualRow.contains(expectedAssetOwnerId)).findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("No such row is found in %s report!", reportName)));
            for (int j = 0; j < headers.size(); j++) {
                if (expRow.get(j).isEmpty()) {
                    assertThat(actRow.get(j).isEmpty() || actRow.get(j).equals("null")).isTrue();
                    continue;
                }
                if (!valuesMatch(expRow.get(j), actRow.get(j))) {
                    fail("Report '%s', row %d, column '%s': expected='%s', actual='%s'\nAll actual rows:\n%s", reportName, i,
                            headers.get(j), expRow.get(j), actRow.get(j), formatRows(actual));
                }
            }
        }
    }

    private void verifyReportDataWithEmptyValues(final String reportName, final RunReportsResponse response,
            final List<List<String>> expected, List<String> headers) {
        final List<List<String>> actual = verifyReportDataHeaders(reportName, response, expected, headers);
        verifyReportDataRowsWithEmptyValues(reportName, actual, expected, headers);
    }

    private void verifyReportData(final String reportName, final RunReportsResponse response, final List<List<String>> expected,
            List<String> headers) {
        final List<List<String>> actual = verifyReportDataHeaders(reportName, response, expected, headers);
        verifyReportDataRows(reportName, actual, expected, headers);
    }

    private void verifyBalanceReportData(final String reportName, final RunReportsResponse response, final List<List<String>> expected,
            List<String> headers) {
        final List<List<String>> actual = verifyReportDataHeaders(reportName, response, expected, headers);
        verifyBalanceReportData(reportName, actual, expected, headers);
    }

    private void verifyReportDataWithOwnerExternalIdAndOriginatorId(final String reportName, final String dateStr,
            final DataTable dataTable) {
        String originatorExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        String previousOwnerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);

        final RunReportsResponse response = executeReport(reportName, dateStr);

        final List<List<String>> expected = dataTable.asLists();
        final List<String> headers = expected.getFirst();

        expected.stream().skip(1).forEach(expectedRow -> {
            if (expectedRow.contains("previous_owner_external_id")) {
                int index = expectedRow.indexOf("previous_owner_external_id");
                if (index != -1) {
                    expectedRow.set(index, previousOwnerExternalId);
                }
            }
            if (expectedRow.contains("owner_external_id")) {
                int index = expectedRow.indexOf("owner_external_id");
                if (index != -1) {
                    expectedRow.set(index, ownerExternalId);
                }
            }

            if (expectedRow.contains("originator_external_id")) {
                int index = expectedRow.indexOf("originator_external_id");
                if (index != -1) {
                    expectedRow.set(index, originatorExternalId);
                }
            }
        });
        verifyReportDataWithEmptyValues(reportName, response, expected, headers);
    }

    private void verifyBalanceReportDataWithOwnerExternalIdAndOriginatorId(final String reportName, final String dateStr,
            final DataTable dataTable) {
        String originatorExternalId = testContext().get(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        String ownerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_OWNER_EXTERNAL_ID);
        String previousOwnerExternalId = testContext().get(TestContextKey.ASSET_EXTERNALIZATION_PREVIOUS_OWNER_EXTERNAL_ID);

        final RunReportsResponse response = executeReport(reportName, dateStr);

        final List<List<String>> expected = dataTable.asLists();
        expected.stream().skip(1).forEach(expectedRow -> {
            if (expectedRow.contains("previous_owner_external_id")) {
                int index = expectedRow.indexOf("previous_owner_external_id");
                if (index != -1) {
                    expectedRow.set(index, previousOwnerExternalId);
                }
            } else if (expectedRow.contains("owner_external_id")) {
                int index = expectedRow.indexOf("owner_external_id");
                if (index != -1) {
                    expectedRow.set(index, ownerExternalId);
                }
            }
            if (expectedRow.contains("originator_external_id")) {
                int index = expectedRow.indexOf("originator_external_id");
                if (index != -1) {
                    expectedRow.set(index, originatorExternalId);
                }
            }
        });
        final List<String> headers = expected.getFirst();

        verifyBalanceReportData(reportName, response, expected, headers);
    }

    private void verifyColumnNullability(final String reportName, final String dateStr, final String columnName,
            final boolean expectEmpty) {
        final RunReportsResponse response = executeReport(reportName, dateStr);

        final List<ResultsetColumnHeaderData> columnHeaders = response.getColumnHeaders();
        assertThat(columnHeaders).isNotNull();
        final int colIdx = findColumnIndex(columnHeaders, columnName);

        final List<ResultsetRowData> data = response.getData();
        assertThat(data).as("Report '%s' returned no data", reportName).isNotNull();
        IntStream.range(0, data.size()).forEach(i -> {
            final List<Object> row = data.get(i).getRow();
            assertThat(row).as("Report '%s', row %d: null cell list", reportName, i + 1).isNotNull();
            assertThat(colIdx).as("Report '%s', row %d: column index out of bounds", reportName, i + 1).isLessThan(row.size());
            final String value = stringify(row.get(colIdx));
            final boolean isEmpty = value.isEmpty() || "null".equals(value);
            if (expectEmpty) {
                assertThat(isEmpty)
                        .as("Report '%s', row %d, column '%s': expected empty but was '%s'", reportName, i + 1, columnName, value).isTrue();
            } else {
                assertThat(isEmpty).as("Report '%s', row %d, column '%s': expected non-empty but was empty", reportName, i + 1, columnName)
                        .isFalse();
            }
        });
    }

    private RunReportsResponse executeReport(final String reportName, final String dateStr) {
        final PostOfficesResponse officeResponse = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(officeResponse).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();

        final String date = LocalDate.parse(dateStr, FORMATTER).toString();
        final RunReportsResponse response = fineractClient.runReports().runReportGetData(reportName, Map.of("R_endDate", date, "R_officeId",
                String.valueOf(officeResponse.getOfficeId()), "locale", "en", "dateFormat", "yyyy-MM-dd"));
        assertThat(response.getData()).as("Report '%s' returned no data", reportName).isNotNull();
        return response;
    }

    private BigDecimal sumColumnForGlAccount(final RunReportsResponse response, final String glCode, final String columnName) {
        final List<ResultsetColumnHeaderData> columnHeaders = response.getColumnHeaders();
        assertThat(columnHeaders).isNotNull();
        final int glIdx = findColumnIndex(columnHeaders, "glacct");
        final int colIdx = findColumnIndex(columnHeaders, columnName);

        final List<ResultsetRowData> data = response.getData();
        assertThat(data).isNotNull();
        return data.stream().map(ResultsetRowData::getRow).filter(Objects::nonNull).filter(row -> glCode.equals(stringify(row.get(glIdx))))
                .map(row -> new BigDecimal(Objects.toString(row.get(colIdx), "0"))).reduce(BigDecimal::add).orElse(null);
    }

    private boolean valuesMatch(final String expected, final String actual) {
        if (Objects.equals(expected, actual)) {
            return true;
        }
        if (isBooleanMatch(expected, actual)) {
            return true;
        }
        try {
            return new BigDecimal(expected).compareTo(new BigDecimal(actual)) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isBooleanMatch(final String expected, final String actual) {
        return ("0".equals(expected) && "false".equals(actual)) || ("false".equals(expected) && "0".equals(actual))
                || ("1".equals(expected) && "true".equals(actual)) || ("true".equals(expected) && "1".equals(actual));
    }

    private String stringify(final Object val) {
        return val == null ? "null" : String.valueOf(val);
    }

    private String formatRows(final List<List<String>> rows) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            sb.append(String.format("  [%d] %s%n", i + 1, String.join(" | ", rows.get(i))));
        }
        return sb.toString();
    }

    private int findColumnIndex(final List<ResultsetColumnHeaderData> headers, final String name) {
        return IntStream.range(0, headers.size()).filter(i -> name.equalsIgnoreCase(headers.get(i).getColumnName())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Column '" + name + "' not found in report"));
    }
}
