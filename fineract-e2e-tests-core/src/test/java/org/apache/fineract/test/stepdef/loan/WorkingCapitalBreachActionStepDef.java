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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalBreachActionStepDef extends AbstractStepDef {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final Long NON_EXISTENT_LOAN_ID = 999999999L;

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;

    @When("Admin creates WC breach reschedule action with the following parameters:")
    public void createRescheduleAction(final DataTable table) {
        final Map<String, String> params = table.asMaps().getFirst();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRescheduleRequest(params);
        executeRescheduleAction(request);
    }

    @Then("Admin fails to create WC breach reschedule action with minimumPayment {int} {word} and frequency {int} {word} with error containing {string}")
    public void failToCreateRescheduleActionWithMessage(final int minimumPayment, final String minimumPaymentType, final int frequency,
            final String frequencyType, final String expectedMessage) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRescheduleRequest(new BigDecimal(minimumPayment),
                minimumPaymentType, frequency, frequencyType);
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).as("Developer message").contains(expectedMessage);
    }

    @Then("Admin fails to create WC breach reschedule action with no parameters with error containing {string}")
    public void failToCreateEmptyRescheduleAction(final String expectedMessage) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRescheduleRequest(Map.of());

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        assertThat(exception.getStatus()).as("HTTP status code").isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).as("Developer message").contains(expectedMessage);
    }

    @Then("WC loan breach actions have the following data:")
    public void verifyBreachActionsHistory(final DataTable table) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachActionData> actions = retrieveBreachActions(loanId);
        final List<Map<String, String>> expectedRows = table.asMaps();
        assertThat(actions).as("Breach actions count").hasSize(expectedRows.size());
        for (int i = 0; i < expectedRows.size(); i++) {
            final WorkingCapitalLoanBreachActionData actual = actions.get(i);
            final int rowNumber = i + 1;
            expectedRows.get(i).forEach((field, value) -> verifyActionField(actual, field, value, rowNumber));
        }
        log.info("Successfully verified {} breach action(s) for loan {}", actions.size(), loanId);
    }

    @Then("Retrieving breach actions for a non-existent Working Capital loan results in a 404 error")
    public void retrieveBreachActionsForNonExistentLoanResultsInNotFound() {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().retrieveBreachActions(NON_EXISTENT_LOAN_ID));

        assertThat(exception.getStatus()).as("HTTP status code should be 404").isEqualTo(404);

        log.info("Verified breach actions retrieval failed with 404 for non-existent loan {}", NON_EXISTENT_LOAN_ID);
    }

    @When("Admin initiate a Working Capital loan breach pause with startDate {string} and endDate {string}")
    public void initiateBreachPause(final String startDate, final String endDate) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildBreachActionRequest("pause", startDate, endDate);
        final PostWorkingCapitalLoansBreachActionResponse response = createBreachActionById(loanId, request);

        log.debug("Breach pause initiated for loan {} with startDate: {}, endDate: {}, response: {}", loanId, startDate, endDate, response);
    }

    @When("Admin initiate a Working Capital loan breach pause by external ID with startDate {string} and endDate {string}")
    public void initiateBreachPauseByExternalId(final String startDate, final String endDate) {
        final String loanExternalId = extractLoanExternalId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildBreachActionRequest("pause", startDate, endDate);
        final PostWorkingCapitalLoansBreachActionResponse response = createBreachActionByExternalId(loanExternalId, request);

        log.debug("Breach pause initiated for loan externalId {} with startDate: {}, endDate: {}, response: {}", loanExternalId, startDate,
                endDate, response);
    }

    @Then("Initiating a Working Capital loan breach pause with startDate {string} and endDate {string} results an error with the following data:")
    public void initiateBreachPauseResultsAnError(final String startDate, final String endDate, final DataTable table) {
        initiateBreachActionResultsAnError("pause", startDate, endDate, table);
    }

    @Then("Initiating a Working Capital loan breach action {string} with startDate {string} and endDate {string} results an error with the following data:")
    public void initiateBreachActionResultsAnError(final String action, final String startDate, final String endDate,
            final DataTable table) {
        final Long loanId = extractLoanId();

        final PostWorkingCapitalLoansBreachActionRequest request = buildBreachActionRequest(action, startDate, endDate);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));

        verifyBreachActionErrorWithTable(exception, table);

        log.info("Verified breach action initiation failed with expected error for loan {}", loanId);
    }

    @Then("Initiating a Working Capital loan breach action without {string} results an error with the following data:")
    public void initiateBreachActionWithoutFieldResultsAnError(final String omittedField, final DataTable table) {
        final Long loanId = extractLoanId();

        final PostWorkingCapitalLoansBreachActionRequest request = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoansBreachActionRequest("pause");
        switch (omittedField) {
            case "action" -> request.action(null);
            case "startDate" -> request.startDate(null);
            case "endDate" -> request.endDate(null);
            default -> throw new IllegalArgumentException("Unknown breach action field: " + omittedField);
        }

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));

        verifyBreachActionErrorWithTable(exception, table);

        log.info("Verified breach action initiation without '{}' failed with expected error for loan {}", omittedField, loanId);
    }

    @Then("Working Capital loan breach action has the following data:")
    public void verifyBreachActions(final DataTable dataTable) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachActionData> actualActions = retrieveBreachActions(loanId);
        verifyBreachActionsWithTable(actualActions, dataTable);
    }

    @Then("Working Capital loan breach action by external ID has the following data:")
    public void verifyBreachActionsByExternalId(final DataTable dataTable) {
        final String loanExternalId = extractLoanExternalId();
        final List<WorkingCapitalLoanBreachActionData> actualActions = retrieveBreachActionsByExternalId(loanExternalId);
        verifyBreachActionsWithTable(actualActions, dataTable);
    }

    private void executeRescheduleAction(final PostWorkingCapitalLoansBreachActionRequest request) {
        final Long loanId = extractLoanId();
        log.debug("Creating breach RESCHEDULE action for WC loan {}: {}", loanId, request);

        final PostWorkingCapitalLoansBreachActionResponse result = ok(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isNotNull();
        log.info("Breach RESCHEDULE action created with id={}", result.getResourceId());
    }

    private void verifyBreachActionsWithTable(final List<WorkingCapitalLoanBreachActionData> actualActions, final DataTable dataTable) {
        assertThat(actualActions).as("Breach actions should not be empty").isNotEmpty();

        final List<List<String>> rows = dataTable.asLists();
        final List<String> headers = rows.getFirst();
        final List<List<String>> expectedData = rows.subList(1, rows.size());

        assertThat(actualActions).as("Breach actions size should match expected data").hasSize(expectedData.size());

        for (int i = 0; i < expectedData.size(); i++) {
            final List<String> expectedRow = expectedData.get(i);
            final WorkingCapitalLoanBreachActionData actualAction = actualActions.get(i);

            for (int j = 0; j < headers.size(); j++) {
                final String header = headers.get(j);
                final String expectedValue = expectedRow.get(j);
                verifyBreachActionField(actualAction, header, expectedValue, i + 1);
            }
        }

        log.info("Successfully verified {} breach action(s)", actualActions.size());
    }

    private void verifyBreachActionField(final WorkingCapitalLoanBreachActionData actual, final String fieldName,
            final String expectedValue, final int rowNumber) {
        Assertions.assertNotNull(actual.getAction());
        switch (fieldName) {
            case "action" -> assertThat(actual.getAction().name()).as("Action for row %d", rowNumber).isEqualTo(expectedValue);
            case "startDate" ->
                assertThat(actual.getStartDate()).as("Start date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
            case "endDate" ->
                assertThat(actual.getEndDate()).as("End date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
            default -> throw new IllegalArgumentException("Unknown field name: " + fieldName);
        }
    }

    private void verifyActionField(final WorkingCapitalLoanBreachActionData actual, final String field, final String expected,
            final int rowNumber) {
        final String label = "Action " + rowNumber + " " + field;
        switch (field) {
            case "action" -> {
                assert actual.getAction() != null;
                assertThat(actual.getAction().name()).as(label).isEqualTo(expected);
            }
            case "startDate" -> assertThat(actual.getStartDate()).as(label).isEqualTo(LocalDate.parse(expected, DATE_FORMAT));
            case "minimumPayment" -> assertThat(actual.getMinimumPayment()).as(label).isEqualByComparingTo(new BigDecimal(expected));
            case "minimumPaymentType" ->
                verifyOptionalField(expected, v -> assertThat(String.valueOf(actual.getMinimumPaymentType())).as(label).isEqualTo(v),
                        () -> assertThat(actual.getMinimumPaymentType()).as(label).isNull());
            case "frequency" -> assertThat(actual.getFrequency()).as(label).isEqualTo(Integer.parseInt(expected));
            case "frequencyType" ->
                verifyOptionalField(expected, v -> assertThat(String.valueOf(actual.getFrequencyType())).as(label).isEqualTo(v),
                        () -> assertThat(actual.getFrequencyType()).as(label).isNull());
            default -> throw new IllegalArgumentException("Unknown action field: " + field);
        }
    }

    private void verifyOptionalField(final String expected, final Consumer<String> whenPresent, final Runnable whenAbsent) {
        Optional.ofNullable(expected).filter(Predicate.not(String::isBlank)).ifPresentOrElse(whenPresent, whenAbsent);
    }

    private Long extractLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertThat(loanResponse).isNotNull();
        return loanResponse.getLoanId();
    }

    private String extractLoanExternalId() {
        final Long loanId = extractLoanId();
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId)).getExternalId();
    }

    private PostWorkingCapitalLoansBreachActionRequest buildBreachActionRequest(final String action, final String startDate,
            final String endDate) {
        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansBreachActionRequest(action).startDate(startDate).endDate(endDate);
    }

    private PostWorkingCapitalLoansBreachActionRequest buildRescheduleRequest(final BigDecimal minimumPayment,
            final String minimumPaymentType, final int frequency, final String frequencyType) {
        return buildRescheduleRequest(Map.of("minimumPayment", minimumPayment.toPlainString(), "minimumPaymentType", minimumPaymentType,
                "frequency", String.valueOf(frequency), "frequencyType", frequencyType));
    }

    private PostWorkingCapitalLoansBreachActionRequest buildRescheduleRequest(final Map<String, String> params) {
        final PostWorkingCapitalLoansBreachActionRequest request = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoansBreachActionRequest("reschedule");
        Optional.ofNullable(params.get("minimumPayment")).ifPresent(v -> request.setMinimumPayment(new BigDecimal(v)));
        Optional.ofNullable(params.get("minimumPaymentType")).ifPresent(request::setMinimumPaymentType);
        Optional.ofNullable(params.get("frequency")).ifPresent(v -> request.setFrequency(Integer.parseInt(v)));
        Optional.ofNullable(params.get("frequencyType")).ifPresent(request::setFrequencyType);
        return request;
    }

    private PostWorkingCapitalLoansBreachActionResponse createBreachActionById(final Long loanId,
            final PostWorkingCapitalLoansBreachActionRequest request) {
        return ok(() -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
    }

    private PostWorkingCapitalLoansBreachActionResponse createBreachActionByExternalId(final String loanExternalId,
            final PostWorkingCapitalLoansBreachActionRequest request) {
        return ok(() -> fineractClient.workingCapitalLoanBreachActions().createBreachActionByExternalId(loanExternalId, request));
    }

    private List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(final Long loanId) {
        final List<WorkingCapitalLoanBreachActionData> actions = ok(
                () -> fineractClient.workingCapitalLoanBreachActions().retrieveBreachActions(loanId));
        log.debug("Breach actions for loan {}: {}", loanId, actions);
        return actions;
    }

    private List<WorkingCapitalLoanBreachActionData> retrieveBreachActionsByExternalId(final String loanExternalId) {
        final List<WorkingCapitalLoanBreachActionData> actions = ok(
                () -> fineractClient.workingCapitalLoanBreachActions().retrieveBreachActionsByExternalId(loanExternalId));
        log.debug("Breach actions for loan externalId {}: {}", loanExternalId, actions);
        return actions;
    }

    private void verifyBreachActionErrorWithTable(final CallFailedRuntimeException exception, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String expectedHttpCode = data.get(1).get(0);
        final String expectedErrorMessage = data.get(1).get(1);

        log.info("Checking for Http code: {} and error message: \"{}\"", expectedHttpCode, expectedErrorMessage);

        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode)
                .isEqualTo(Integer.parseInt(expectedHttpCode));
        assertThat(exception.getMessage()).as("Should contain error message").contains(expectedErrorMessage);
    }

}
