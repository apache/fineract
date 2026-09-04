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
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanNearBreachActionData;
import org.apache.fineract.test.helper.WorkingCapitalTenantDateHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalNearBreachActionStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalTenantDateHelper workingCapitalTenantDateHelper;

    @When("Admin creates a near breach reschedule action with threshold {string} frequency {int} frequencyType {string}")
    public void createNearBreachRescheduleAction(final String threshold, final int frequency, final String frequencyType) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request = buildRequest(threshold, frequency, frequencyType);
        ok(() -> fineractClient.workingCapitalLoanNearBreachActions().createWorkingCapitalLoanNearBreachActionById(loanId, request));
        log.info("Created near breach reschedule action for loan {} with threshold={} frequency={} frequencyType={}", loanId, threshold,
                frequency, frequencyType);
    }

    @When("Admin creates a near breach reschedule action with threshold {string} frequency {int} frequencyType {string} expecting error:")
    public void createNearBreachRescheduleActionExpectingError(final String threshold, final int frequency, final String frequencyType,
            final DataTable table) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request = buildRequest(threshold, frequency, frequencyType);
        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanNearBreachActions().createWorkingCapitalLoanNearBreachActionById(loanId, request));
        assertHttpStatus(exception, expectedHttpCode);
        assertErrorMessage(exception, expectedErrorMessage);
        log.info("Verified near breach reschedule action on loan {} failed with status {} and message: {}", loanId, exception.getStatus(),
                expectedErrorMessage);
    }

    @Then("Near breach action history has {int} entry")
    @Then("Near breach action history has {int} entries")
    public void verifyNearBreachActionHistorySize(final int expectedSize) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanNearBreachActionData> history = retrieveNearBreachActionHistory(loanId);
        assertThat(history).as("Near breach action history size for loan %d", loanId).hasSize(expectedSize);
        log.info("Verified near breach action history for loan {} has {} entries", loanId, expectedSize);
    }

    @Then("Near breach action history has the following data:")
    public void verifyNearBreachActionHistory(final DataTable dataTable) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanNearBreachActionData> history = retrieveNearBreachActionHistory(loanId);

        final List<List<String>> rows = dataTable.asLists();
        final List<String> headers = rows.getFirst();
        final List<List<String>> expectedData = rows.subList(1, rows.size());

        assertThat(history).as("Near breach action history size should match expected data").hasSize(expectedData.size());

        for (int i = 0; i < expectedData.size(); i++) {
            final List<String> expectedRow = expectedData.get(i);
            final WorkingCapitalLoanNearBreachActionData actual = history.get(i);

            for (int j = 0; j < headers.size(); j++) {
                final String header = headers.get(j);
                final String expectedValue = expectedRow.get(j);
                verifyActionField(actual, header, expectedValue, i + 1);
            }
        }

        log.info("Successfully verified {} near breach action history entries for loan {}", history.size(), loanId);
    }

    private void verifyActionField(final WorkingCapitalLoanNearBreachActionData actual, final String fieldName, final String expectedValue,
            final int rowNumber) {
        switch (fieldName) {
            case "action" -> assertThat(actual.getAction()).as("Action for row %d", rowNumber)
                    .isEqualTo(WorkingCapitalLoanNearBreachActionData.ActionEnum.fromValue(expectedValue));
            case "threshold" ->
                assertThat(actual.getThreshold()).as("Threshold for row %d", rowNumber).isEqualByComparingTo(new BigDecimal(expectedValue));
            case "frequency" ->
                assertThat(actual.getFrequency()).as("Frequency for row %d", rowNumber).isEqualTo(Integer.parseInt(expectedValue));
            case "frequencyType" ->
                assertThat(actual.getFrequencyType()).as("FrequencyType for row %d", rowNumber).isEqualTo(expectedValue);
            case "submittedOnDate" -> {
                assertThat(actual.getSubmittedOnDate()).as("SubmittedOnDate for row %d", rowNumber).isNotNull();
                assertThat(FORMATTER.format(actual.getSubmittedOnDate())).as("SubmittedOnDate for row %d", rowNumber)
                        .isEqualTo(expectedValue);
            }
            default -> throw new IllegalArgumentException("Unknown near breach action field: " + fieldName);
        }
    }

    @Then("Latest near breach action was submitted on the current tenant date")
    public void latestNearBreachActionSubmittedOnTenantDate() {
        final Long loanId = extractLoanId();
        final WorkingCapitalLoanNearBreachActionData latest = retrieveNearBreachActionHistory(loanId).stream()//
                .max(Comparator.comparing(WorkingCapitalLoanNearBreachActionData::getId))//
                .orElseThrow(() -> new IllegalStateException(String.format("No near breach action found on loan [%s]", loanId)));
        workingCapitalTenantDateHelper.assertStampedOnCurrentTenantDate(latest.getSubmittedOnDate(), loanId,
                String.format("submittedOnDate of latest near breach action on loan %d", loanId));
    }

    private PostWorkingCapitalLoansLoanIdNearBreachActionsRequest buildRequest(final String threshold, final int frequency,
            final String frequencyType) {
        return new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .action(PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.ActionEnum.RESCHEDULE)
                .nearBreachThreshold(new BigDecimal(threshold)).nearBreachFrequency(frequency).nearBreachFrequencyType(
                        PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.NearBreachFrequencyTypeEnum.fromValue(frequencyType));
    }

    private Long extractLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private void assertHttpStatus(final CallFailedRuntimeException exception, final int expectedStatus) {
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedStatus).isEqualTo(expectedStatus);
    }

    private void assertErrorMessage(final CallFailedRuntimeException exception, final String expectedMessage) {
        assertThat(exception.getMessage()).as("Error message should contain: " + expectedMessage).contains(expectedMessage);
    }

    private List<WorkingCapitalLoanNearBreachActionData> retrieveNearBreachActionHistory(final Long loanId) {
        final List<WorkingCapitalLoanNearBreachActionData> history = ok(
                () -> fineractClient.workingCapitalLoanNearBreachActions().getWorkingCapitalLoanNearBreachActionsById(loanId));
        log.debug("Near breach action history for loan {}: {}", loanId, history);
        return history;
    }
}
