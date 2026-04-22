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
package org.apache.fineract.test.stepdef.loan;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalBreachScheduleStepDef extends AbstractStepDef {

    private final FineractFeignClient fineractClient;

    @Then("Working Capital loan breach schedule has no data")
    public void verifyBreachScheduleIsEmpty() {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachScheduleData> schedule = retrieveBreachSchedule(loanId);

        assertThat(schedule).as("Breach schedule should be empty").isEmpty();

        log.info("Verified that loan {} has no breach schedule", loanId);
    }

    @Then("Working Capital loan breach schedule has {int} period(s)")
    public void verifyBreachScheduleSize(final int expectedSize) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachScheduleData> schedule = retrieveBreachSchedule(loanId);

        assertThat(schedule).as("Breach schedule size for loan %d", loanId).hasSize(expectedSize);

        log.info("Verified that loan {} has {} breach schedule period(s)", loanId, expectedSize);
    }

    @Then("Retrieving Working Capital loan breach schedule for non-existent loanId {long} fails with status code {int}")
    public void verifyBreachScheduleNotFound(final long loanId, final int expectedStatus) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachSchedule().retrieveBreachSchedule(loanId));

        assertThat(exception.getStatus())
                .as(ErrorMessageHelper.wrongStatusCodeInBreachScheduleRetrieval(exception.getStatus(), expectedStatus, loanId))
                .isEqualTo(expectedStatus);

        log.info("Verified that GET breach schedule for loanId {} fails with status {}", loanId, expectedStatus);
    }

    @Then("Working Capital loan breach schedule has the following data:")
    public void verifyBreachSchedule(final DataTable dataTable) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachScheduleData> schedule = retrieveBreachSchedule(loanId);

        final List<List<String>> rows = dataTable.asLists();
        final List<String> headers = rows.getFirst();
        final List<List<String>> expectedData = rows.subList(1, rows.size());

        assertThat(schedule).as("Breach schedule size should match expected data").hasSize(expectedData.size());

        for (int i = 0; i < expectedData.size(); i++) {
            final List<String> expectedRow = expectedData.get(i);
            final WorkingCapitalLoanBreachScheduleData actualRow = schedule.get(i);

            for (int j = 0; j < headers.size(); j++) {
                final String header = headers.get(j);
                final String expectedValue = expectedRow.get(j);
                verifyField(actualRow, header, expectedValue, i + 1);
            }
        }

        log.info("Successfully verified {} breach schedule entries for loan {}", schedule.size(), loanId);
    }

    private Long extractLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private List<WorkingCapitalLoanBreachScheduleData> retrieveBreachSchedule(final Long loanId) {
        final List<WorkingCapitalLoanBreachScheduleData> schedule = ok(
                () -> fineractClient.workingCapitalLoanBreachSchedule().retrieveBreachSchedule(loanId));
        log.debug("Breach schedule for loan {}: {}", loanId, schedule);
        return schedule;
    }

    private void verifyField(final WorkingCapitalLoanBreachScheduleData actual, final String fieldName, final String expectedValue,
            final int rowNumber) {
        switch (fieldName) {
            case "periodNumber" ->
                assertThat(actual.getPeriodNumber()).as("Period number for row %d", rowNumber).isEqualTo(Integer.parseInt(expectedValue));
            case "fromDate" ->
                assertThat(actual.getFromDate()).as("From date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
            case "toDate" -> assertThat(actual.getToDate()).as("To date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
            case "numberOfDays" -> verifyNullableInteger(actual.getNumberOfDays(), expectedValue, "Number of days", rowNumber);
            case "minPaymentAmount" ->
                verifyNullableBigDecimal(actual.getMinPaymentAmount(), expectedValue, "Min payment amount", rowNumber);
            case "outstandingAmount" ->
                verifyNullableBigDecimal(actual.getOutstandingAmount(), expectedValue, "Outstanding amount", rowNumber);
            case "nearBreach" -> verifyNullableBoolean(actual.getNearBreach(), expectedValue, "Near breach", rowNumber);
            case "breach" -> verifyNullableBoolean(actual.getBreach(), expectedValue, "Breach", rowNumber);
            default -> throw new IllegalArgumentException("Unknown field name: " + fieldName);
        }
    }

    private void verifyNullableBoolean(final Boolean actualValue, final String expectedValue, final String fieldDescription,
            final int rowNumber) {
        if ("null".equals(expectedValue)) {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isNull();
        } else {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isEqualTo(Boolean.parseBoolean(expectedValue));
        }
    }

    private void verifyNullableBigDecimal(final BigDecimal actualValue, final String expectedValue, final String fieldDescription,
            final int rowNumber) {
        if ("null".equals(expectedValue)) {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isNull();
        } else {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isEqualByComparingTo(new BigDecimal(expectedValue));
        }
    }

    private void verifyNullableInteger(final Integer actualValue, final String expectedValue, final String fieldDescription,
            final int rowNumber) {
        if ("null".equals(expectedValue)) {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isNull();
        } else {
            assertThat(actualValue).as("%s for row %d", fieldDescription, rowNumber).isEqualTo(Integer.parseInt(expectedValue));
        }
    }

}
