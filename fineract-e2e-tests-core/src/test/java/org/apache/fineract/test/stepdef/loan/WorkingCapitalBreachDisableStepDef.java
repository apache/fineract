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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.test.api.FineractClientConfiguration;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalBreachDisableStepDef extends AbstractStepDef {

    private static final Set<String> DISABLE_ENABLE_ACTIONS = Set.of("DISABLE", "ENABLE");

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;
    private final FineractClientConfiguration fineractClientConfiguration;

    @When("Admin initiate a Working Capital loan breach disable with startDate {string}")
    public void initiateBreachDisable(final String startDate) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRequest("disable", startDate);
        ok(() -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        log.debug("Breach disable initiated for loan {} with startDate {}", loanId, startDate);
    }

    @When("Admin initiate a Working Capital loan breach enable with startDate {string}")
    public void initiateBreachEnable(final String startDate) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRequest("enable", startDate);
        ok(() -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        log.debug("Breach enable initiated for loan {} with startDate {}", loanId, startDate);
    }

    @When("Admin initiate a Working Capital loan breach disable by external ID with startDate {string}")
    public void initiateBreachDisableByExternalId(final String startDate) {
        final String loanExternalId = extractLoanExternalId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRequest("disable", startDate);
        ok(() -> fineractClient.workingCapitalLoanBreachActions().createBreachActionByExternalId(loanExternalId, request));
        log.debug("Breach disable initiated for loan externalId {} with startDate {}", loanExternalId, startDate);
    }

    @Then("Initiating a Working Capital loan breach disable with startDate {string} results an error with the following data:")
    public void initiateBreachDisableResultsAnError(final String startDate, final DataTable table) {
        initiateBreachActionResultsAnError("disable", startDate, null, table);
    }

    @Then("Initiating a Working Capital loan breach enable with startDate {string} results an error with the following data:")
    public void initiateBreachEnableResultsAnError(final String startDate, final DataTable table) {
        initiateBreachActionResultsAnError("enable", startDate, null, table);
    }

    @Then("Initiating a Working Capital loan breach disable with startDate {string} and endDate {string} results an error with the following data:")
    public void initiateBreachDisableWithEndDateResultsAnError(final String startDate, final String endDate, final DataTable table) {
        initiateBreachActionResultsAnError("disable", startDate, endDate, table);
    }

    @Then("Created user with no CREATE_WC_BREACH_DISABLE permission gets an error when initiate a Working Capital loan breach disable with startDate {string}")
    public void initiateBreachDisableWithoutPermissionResultsAnError(final String startDate) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRequest("disable", startDate);
        final FineractFeignClient userClient = userClient();

        final CallFailedRuntimeException exception = fail(
                () -> userClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).as("Should contain authorization error message")
                .contains("User has no authority to CREATE wc_breach_disables");
        log.info("Verified breach disable denied for user without CREATE_WC_BREACH_DISABLE permission on loan {}", loanId);
    }

    @Then("Created user with no READ_WC_BREACH_ACTION permission gets an error when retrieving Working Capital loan breach actions")
    public void retrieveBreachActionsWithoutPermissionResultsAnError() {
        final Long loanId = extractLoanId();
        final FineractFeignClient userClient = userClient();

        final CallFailedRuntimeException exception = fail(() -> userClient.workingCapitalLoanBreachActions().retrieveBreachActions(loanId));

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);
        assertThat(exception.getDeveloperMessage()).as("Should contain authorization error message")
                .contains("User has no authority to READ wc_breach_actions");
        log.info("Verified breach actions retrieval denied for user without READ_WC_BREACH_ACTION permission on loan {}", loanId);
    }

    @Then("Working Capital loan breach disable action has the following data:")
    public void verifyBreachDisableActions(final DataTable dataTable) {
        final Long loanId = extractLoanId();
        final List<WorkingCapitalLoanBreachActionData> actions = disableEnableActions(retrieveBreachActions(loanId));
        verifyWithTable(actions, dataTable);
    }

    @Then("Working Capital loan breach disable action by external ID has the following data:")
    public void verifyBreachDisableActionsByExternalId(final DataTable dataTable) {
        final String loanExternalId = extractLoanExternalId();
        final List<WorkingCapitalLoanBreachActionData> actions = disableEnableActions(
                ok(() -> fineractClient.workingCapitalLoanBreachActions().retrieveBreachActionsByExternalId(loanExternalId)));
        verifyWithTable(actions, dataTable);
    }

    private void initiateBreachActionResultsAnError(final String action, final String startDate, final String endDate,
            final DataTable table) {
        final Long loanId = extractLoanId();
        final PostWorkingCapitalLoansBreachActionRequest request = buildRequest(action, startDate).endDate(endDate);
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
        verifyErrorWithTable(exception, table);
        log.info("Verified breach disable/enable initiation failed with expected error for loan {}", loanId);
    }

    private List<WorkingCapitalLoanBreachActionData> disableEnableActions(final List<WorkingCapitalLoanBreachActionData> actions) {
        return actions.stream().filter(action -> DISABLE_ENABLE_ACTIONS.contains(action.getAction().name())).toList();
    }

    private void verifyWithTable(final List<WorkingCapitalLoanBreachActionData> actualActions, final DataTable dataTable) {
        final List<List<String>> rows = dataTable.asLists();
        final List<String> headers = rows.getFirst();
        final List<List<String>> expectedData = rows.subList(1, rows.size());

        assertThat(actualActions).as("Breach disable actions size should match expected data").hasSize(expectedData.size());

        for (int i = 0; i < expectedData.size(); i++) {
            final List<String> expectedRow = expectedData.get(i);
            final WorkingCapitalLoanBreachActionData actualAction = actualActions.get(i);
            for (int j = 0; j < headers.size(); j++) {
                verifyField(actualAction, headers.get(j), expectedRow.get(j), i + 1);
            }
        }
        log.info("Successfully verified {} breach disable action(s)", actualActions.size());
    }

    private void verifyField(final WorkingCapitalLoanBreachActionData actual, final String fieldName, final String expectedValue,
            final int rowNumber) {
        Assertions.assertNotNull(actual.getAction());
        switch (fieldName) {
            case "action" -> assertThat(actual.getAction().name()).as("Action for row %d", rowNumber).isEqualTo(expectedValue);
            case "startDate" ->
                assertThat(actual.getStartDate()).as("Start date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
            case "endDate" -> {
                if (expectedValue == null || expectedValue.isBlank()) {
                    assertThat(actual.getEndDate()).as("End date for row %d", rowNumber).isNull();
                } else {
                    assertThat(actual.getEndDate()).as("End date for row %d", rowNumber).isEqualTo(LocalDate.parse(expectedValue));
                }
            }
            default -> throw new IllegalArgumentException("Unknown field name: " + fieldName);
        }
    }

    private void verifyErrorWithTable(final CallFailedRuntimeException exception, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String expectedHttpCode = data.get(1).get(0);
        final String expectedErrorMessage = data.get(1).get(1);
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode)
                .isEqualTo(Integer.parseInt(expectedHttpCode));
        assertThat(exception.getMessage()).as("Should contain error message").contains(expectedErrorMessage);
    }

    private PostWorkingCapitalLoansBreachActionRequest buildRequest(final String action, final String startDate) {
        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansBreachActionRequest(action).startDate(startDate).endDate(null);
    }

    private List<WorkingCapitalLoanBreachActionData> retrieveBreachActions(final Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoanBreachActions().retrieveBreachActions(loanId));
    }

    private FineractFeignClient userClient() {
        final String username = testContext().get(TestContextKey.CREATED_SIMPLE_USER_USERNAME);
        final String password = testContext().get(TestContextKey.CREATED_SIMPLE_USER_PASSWORD);
        return fineractClientConfiguration.fineractFeignClientForUser(username, password);
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
}
