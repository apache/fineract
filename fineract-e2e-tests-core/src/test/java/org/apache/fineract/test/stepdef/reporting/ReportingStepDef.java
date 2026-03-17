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
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class ReportingStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final FineractFeignClient fineractClient;

    @Then("Transaction Summary Report for date {string} has the following data:")
    public void transactionSummaryReportHasData(final String dateStr, final DataTable dataTable) {
        verifyReportData("Transaction Summary Report", dateStr, dataTable);
    }

    @Then("Transaction Summary Report with Asset Owner for date {string} has the following data:")
    public void transactionSummaryReportWithAssetOwnerHasData(final String dateStr, final DataTable dataTable) {
        verifyReportData("Transaction Summary Report with Asset Owner", dateStr, dataTable);
    }

    private void verifyReportData(final String reportName, final String dateStr, final DataTable dataTable) {
        final PostOfficesResponse officeResponse = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(officeResponse).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();

        final String date = LocalDate.parse(dateStr, FORMATTER).toString();
        final RunReportsResponse response = fineractClient.runReports().runReportGetData(reportName, Map.of("R_endDate", date, "R_officeId",
                String.valueOf(officeResponse.getOfficeId()), "locale", "en", "dateFormat", "yyyy-MM-dd"));
        assertThat(response.getData()).as("Report '%s' returned no data", reportName).isNotNull();

        final List<List<String>> expected = dataTable.asLists();
        final List<String> headers = expected.getFirst();

        assertThat(response.getColumnHeaders()).isNotNull();
        final int[] colIdx = headers.stream().mapToInt(h -> findColumnIndex(response.getColumnHeaders(), h)).toArray();

        final List<List<String>> actual = response.getData().stream().map(row -> {
            assertThat(row.getRow()).as("Report '%s' returned a row with null cell list", reportName).isNotNull();
            return IntStream.of(colIdx).mapToObj(i -> {
                assertThat(i).as("Report '%s': column index %d is out of bounds (row size: %d)", reportName, i, row.getRow().size())
                        .isLessThan(row.getRow().size());
                return stringify(row.getRow().get(i));
            }).toList();
        }).toList();

        assertThat(actual).as("Report '%s' row count mismatch.\nActual rows:\n%s", reportName, formatRows(actual))
                .hasSize(expected.size() - 1);

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
