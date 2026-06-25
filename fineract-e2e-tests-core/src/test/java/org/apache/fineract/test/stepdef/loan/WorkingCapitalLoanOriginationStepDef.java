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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.failVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdOriginatorData;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanOriginatorsResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansOriginatorData;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class WorkingCapitalLoanOriginationStepDef extends AbstractStepDef {

    private static final long NON_EXISTENT_ID = Long.MAX_VALUE;

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;
    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;

    @When("Admin attaches the originator to the working capital loan")
    public void attachOriginator() {
        attachOriginatorById(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
    }

    @When("Admin attaches the second originator to the working capital loan")
    public void attachSecondOriginator() {
        attachOriginatorById(TestContextKey.ORIGINATOR_SECOND_CREATE_RESPONSE);
    }

    @When("Admin detaches the originator from the working capital loan")
    public void detachOriginator() {
        final long loanId = getLoanId();
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId));
    }

    @When("Admin creates a working capital loan with originator attached inline and the following data:")
    public void createWorkingCapitalLoanWithOriginatorInline(final DataTable table) {
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        submitInlineAndStore(buildInlineRequest(table, List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId))));
    }

    @When("Admin creates a working capital loan with two originators attached inline and the following data:")
    public void createWorkingCapitalLoanWithTwoOriginatorsInline(final DataTable table) {
        final long firstOriginatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        final long secondOriginatorId = getOriginatorId(TestContextKey.ORIGINATOR_SECOND_CREATE_RESPONSE);
        submitInlineAndStore(buildInlineRequest(table, List.of(new PostWorkingCapitalLoansOriginatorData().id(firstOriginatorId),
                new PostWorkingCapitalLoansOriginatorData().id(secondOriginatorId))));
    }

    @When("Admin creates a working capital loan with the same originator listed twice inline and the following data:")
    public void createWorkingCapitalLoanWithDuplicateInlineOriginator(final DataTable table) {
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        submitInlineAndStore(buildInlineRequest(table, List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId),
                new PostWorkingCapitalLoansOriginatorData().id(originatorId))));
    }

    @When("Admin creates a working capital loan with an inline originator created by a new external id and name {string} and the following data:")
    public void createWorkingCapitalLoanWithInlineOriginatorByNewExternalId(final String originatorName, final DataTable table) {
        final String originatorExternalId = UUID.randomUUID().toString();
        testContext().set(TestContextKey.ORIGINATOR_EXTERNAL_ID, originatorExternalId);
        submitInlineAndStore(buildInlineRequest(table,
                List.of(new PostWorkingCapitalLoansOriginatorData().externalId(originatorExternalId).name(originatorName))));
    }

    @When("Admin creates a working capital loan with an inline originator referenced by the existing originator external id and the following data:")
    public void createWorkingCapitalLoanWithInlineOriginatorByExistingExternalId(final DataTable table) {
        final String originatorExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        submitInlineAndStore(
                buildInlineRequest(table, List.of(new PostWorkingCapitalLoansOriginatorData().externalId(originatorExternalId))));
    }

    @Then("Creating a working capital loan with the inline originator should fail with status {int} and the following data:")
    public void createWorkingCapitalLoanWithInlineOriginatorShouldFail(final int expectedStatus, final DataTable table) {
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        final PostWorkingCapitalLoansRequest request = buildInlineRequest(table,
                List.of(new PostWorkingCapitalLoansOriginatorData().id(originatorId)));
        assertCallFails(() -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request), expectedStatus);
    }

    @Then("Creating a working capital loan with a non-existent inline originator should fail with status {int} and the following data:")
    public void createWorkingCapitalLoanWithNonExistentInlineOriginatorShouldFail(final int expectedStatus, final DataTable table) {
        final PostWorkingCapitalLoansRequest request = buildInlineRequest(table,
                List.of(new PostWorkingCapitalLoansOriginatorData().id(NON_EXISTENT_ID)));
        assertCallFails(() -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request), expectedStatus);
    }

    @Then("Working capital loan details has the originator attached")
    public void detailsHasOriginator() {
        assertOriginatorPresent(getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID));
    }

    @Then("Working capital loan details has the second originator attached")
    public void detailsHasSecondOriginator() {
        assertOriginatorPresent(getExternalId(TestContextKey.ORIGINATOR_SECOND_EXTERNAL_ID));
    }

    @Then("Working capital loan details has {int} originator(s) attached")
    public void detailsHasOriginatorCount(final int expectedCount) {
        final int actual = Optional.ofNullable(retrieveLoanDetails().getOriginators()).map(List::size).orElse(0);
        assertThat(actual).as("Number of originators in WC loan details").isEqualTo(expectedCount);
    }

    @Then("Working capital loan details has no originator attached")
    public void detailsHasNoOriginator() {
        assertThat(retrieveLoanDetails().getOriginators()).as("Originators in WC loan details after detach").isNullOrEmpty();
    }

    @Then("Retrieving working capital loan originators returns {int} originator(s)")
    public void retrieveByLoanId(final int expectedCount) {
        final long loanId = getLoanId();
        final LoanOriginatorsResponse response = ok(
                () -> fineractClient.workingCapitalLoanOriginators().retrieveOriginatorsByWorkingCapitalLoanId(loanId));
        assertThat(response.getOriginators()).as("Originators from WC originators API").hasSize(expectedCount);
    }

    @Then("Retrieving working capital loan originators by external id returns {int} originator(s)")
    public void retrieveByLoanExternalId(final int expectedCount) {
        final String loanExternalId = retrieveLoanDetails().getExternalId();
        final LoanOriginatorsResponse response = ok(
                () -> fineractClient.workingCapitalLoanOriginators().retrieveOriginatorsByWorkingCapitalLoanExternalId(loanExternalId));
        assertThat(response.getOriginators()).as("Originators from WC originators API by external id").hasSize(expectedCount);
    }

    @Then("Attaching the originator to the working capital loan should fail with status {int}")
    public void attachShouldFail(final int expectedStatus) {
        final long loanId = getLoanId();
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        assertVoidCallFails(() -> fineractClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(loanId, originatorId),
                expectedStatus);
    }

    @Then("Detaching the originator from the working capital loan should fail with status {int}")
    public void detachShouldFail(final int expectedStatus) {
        final long loanId = getLoanId();
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        assertVoidCallFails(
                () -> fineractClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId),
                expectedStatus);
    }

    @Then("Attaching non-existent originator to the working capital loan should fail with status {int}")
    public void attachNonExistentOriginatorShouldFail(final int expectedStatus) {
        final long loanId = getLoanId();
        assertVoidCallFails(
                () -> fineractClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(loanId, NON_EXISTENT_ID),
                expectedStatus);
    }

    @Then("Attaching the originator to non-existent working capital loan should fail with status {int}")
    public void attachToNonExistentLoanShouldFail(final int expectedStatus) {
        assertVoidCallFails(
                () -> fineractClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(NON_EXISTENT_ID, NON_EXISTENT_ID),
                expectedStatus);
    }

    @Then("Detaching the originator from a non-existent working capital loan should fail with status {int}")
    public void detachFromNonExistentLoanShouldFail(final int expectedStatus) {
        assertVoidCallFails(() -> fineractClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(NON_EXISTENT_ID,
                NON_EXISTENT_ID), expectedStatus);
    }

    @Then("Detaching a non-existent originator from the working capital loan should fail with status {int}")
    public void detachNonExistentOriginatorShouldFail(final int expectedStatus) {
        final long loanId = getLoanId();
        assertVoidCallFails(
                () -> fineractClient.workingCapitalLoanOriginators().detachOriginatorFromWorkingCapitalLoan(loanId, NON_EXISTENT_ID),
                expectedStatus);
    }

    @Then("Retrieving working capital loan originators for a non-existent loan should fail with status {int}")
    public void retrieveByNonExistentLoanShouldFail(final int expectedStatus) {
        assertCallFails(() -> fineractClient.workingCapitalLoanOriginators().retrieveOriginatorsByWorkingCapitalLoanId(NON_EXISTENT_ID),
                expectedStatus);
    }

    @Then("Retrieving working capital loan originators by a non-existent external id should fail with status {int}")
    public void retrieveByNonExistentExternalIdShouldFail(final int expectedStatus) {
        final String nonExistentExternalId = UUID.randomUUID().toString();
        assertCallFails(() -> fineractClient.workingCapitalLoanOriginators()
                .retrieveOriginatorsByWorkingCapitalLoanExternalId(nonExistentExternalId), expectedStatus);
    }

    @When("Admin attaches the originator to the working capital loan by originator external id")
    public void attachOriginatorByOriginatorExternalId() {
        final long loanId = getLoanId();
        final String originatorExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoanByOriginatorExternalId(loanId,
                originatorExternalId));
    }

    @When("Admin attaches the originator to the working capital loan by loan external id")
    public void attachOriginatorByLoanExternalId() {
        final String loanExternalId = retrieveLoanDetails().getExternalId();
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators()
                .attachOriginatorToWorkingCapitalLoanByLoanExternalId(loanExternalId, originatorId));
    }

    @When("Admin attaches the originator to the working capital loan by both external ids")
    public void attachOriginatorByBothExternalIds() {
        final String loanExternalId = retrieveLoanDetails().getExternalId();
        final String originatorExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators()
                .attachOriginatorToWorkingCapitalLoanByBothExternalIds(loanExternalId, originatorExternalId));
    }

    @When("Admin detaches the originator from the working capital loan by originator external id")
    public void detachOriginatorByOriginatorExternalId() {
        final long loanId = getLoanId();
        final String originatorExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators()
                .detachOriginatorFromWorkingCapitalLoanByOriginatorExternalId(loanId, originatorExternalId));
    }

    @When("Admin detaches the originator from the working capital loan by loan external id")
    public void detachOriginatorByLoanExternalId() {
        final String loanExternalId = retrieveLoanDetails().getExternalId();
        final long originatorId = getOriginatorId(TestContextKey.ORIGINATOR_CREATE_RESPONSE);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators()
                .detachOriginatorFromWorkingCapitalLoanByLoanExternalId(loanExternalId, originatorId));
    }

    @When("Admin detaches the originator from the working capital loan by both external ids")
    public void detachOriginatorByBothExternalIds() {
        final String loanExternalId = retrieveLoanDetails().getExternalId();
        final String originatorExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators()
                .detachOriginatorFromWorkingCapitalLoanByBothExternalIds(loanExternalId, originatorExternalId));
    }

    @Then("Working capital loan details has the originator with all fields attached")
    public void detailsHasOriginatorWithAllFields() {
        final String expectedExternalId = getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID);
        final String expectedOriginatorTypeName = testContext().get(TestContextKey.ORIGINATOR_TYPE_NAME);
        final String expectedChannelTypeName = testContext().get(TestContextKey.ORIGINATOR_CHANNEL_TYPE_NAME);

        final List<GetWorkingCapitalLoansLoanIdOriginatorData> originators = retrieveLoanDetails().getOriginators();
        assertThat(originators).as("Originators in WC loan details").isNotNull().isNotEmpty();
        final GetWorkingCapitalLoansLoanIdOriginatorData originator = originators.stream()
                .filter(candidate -> expectedExternalId.equals(candidate.getExternalId())).findFirst().orElseThrow(
                        () -> new AssertionError("Originator with externalId " + expectedExternalId + " not found in WC loan details"));

        assertThat(originator.getId()).as("Originator id in WC loan details").isNotNull();
        assertThat(originator.getExternalId()).as("Originator externalId in WC loan details").isEqualTo(expectedExternalId);
        assertThat(originator.getName()).as("Originator name in WC loan details").isNotNull();
        assertThat(originator.getStatus()).as("Originator status in WC loan details").isEqualTo("ACTIVE");
        assertThat(originator.getOriginatorType()).as("Originator type in WC loan details").isNotNull();
        assertThat(originator.getOriginatorType().getName()).as("Originator type name in WC loan details")
                .isEqualTo(expectedOriginatorTypeName);
        assertThat(originator.getChannelType()).as("Channel type in WC loan details").isNotNull();
        assertThat(originator.getChannelType().getName()).as("Channel type name in WC loan details").isEqualTo(expectedChannelTypeName);
    }

    @Then("Working capital loan details has originator with name {string}")
    public void detailsHasOriginatorWithName(final String expectedName) {
        assertOriginatorPresentBy(GetWorkingCapitalLoansLoanIdOriginatorData::getName, expectedName, "name");
    }

    @Then("Working capital loan details does not have the originator attached")
    public void detailsDoesNotHaveOriginator() {
        assertOriginatorAbsent(getExternalId(TestContextKey.ORIGINATOR_EXTERNAL_ID));
    }

    private PostWorkingCapitalLoansRequest buildInlineRequest(final DataTable table,
            final List<PostWorkingCapitalLoansOriginatorData> originators) {
        final Map<String, String> row = table.asMaps().getFirst();
        final String loanProductName = row.get("LoanProduct");
        final String submittedOnDate = row.get("submittedOnDate");
        final String expectedDisbursementDate = row.get("expectedDisbursementDate");
        final String principal = row.get("principalAmount");
        final String totalPaymentVolume = row.get("totalPaymentVolume");
        final String periodPaymentRate = row.get("periodPaymentRate");
        final String discount = row.get("discount");

        final Long clientId = ((PostClientsResponse) testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE)).getClientId();
        final long productId = workingCapitalLoanProductResolver.resolve(DefaultWorkingCapitalLoanProduct.valueOf(loanProductName));

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId).productId(productId)
                .submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPaymentVolume(new BigDecimal(totalPaymentVolume))
                .periodPaymentRate(new BigDecimal(periodPaymentRate))
                .discount(Optional.ofNullable(discount).filter(value -> !value.isEmpty()).map(BigDecimal::new).orElse(null))
                .originators(originators);
    }

    private void submitInlineAndStore(final PostWorkingCapitalLoansRequest request) {
        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
    }

    private Long getLoanId() {
        return ((PostWorkingCapitalLoansResponse) testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE)).getLoanId();
    }

    private Long getOriginatorId(final String contextKey) {
        return ((PostLoanOriginatorsResponse) testContext().get(contextKey)).getResourceId();
    }

    private String getExternalId(final String contextKey) {
        return testContext().get(contextKey);
    }

    private GetWorkingCapitalLoansLoanIdResponse retrieveLoanDetails() {
        final long loanId = getLoanId();
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
    }

    private void assertOriginatorPresent(final String expectedExternalId) {
        assertOriginatorPresentBy(GetWorkingCapitalLoansLoanIdOriginatorData::getExternalId, expectedExternalId, "externalId");
    }

    private void assertOriginatorAbsent(final String expectedExternalId) {
        final List<GetWorkingCapitalLoansLoanIdOriginatorData> originators = retrieveLoanDetails().getOriginators();
        final boolean found = originators != null
                && originators.stream().anyMatch(originator -> expectedExternalId.equals(originator.getExternalId()));
        assertThat(found).as("Originator with externalId %s should not be present in WC loan details", expectedExternalId).isFalse();
    }

    private void assertOriginatorPresentBy(final Function<GetWorkingCapitalLoansLoanIdOriginatorData, String> fieldExtractor,
            final String expectedValue, final String fieldDescription) {
        final List<GetWorkingCapitalLoansLoanIdOriginatorData> originators = retrieveLoanDetails().getOriginators();
        assertThat(originators).as("Originators in WC loan details").isNotNull().isNotEmpty();
        final boolean found = originators.stream().anyMatch(originator -> expectedValue.equals(fieldExtractor.apply(originator)));
        assertThat(found).as("Expected originator with %s %s in WC loan details", fieldDescription, expectedValue).isTrue();
    }

    private void attachOriginatorById(final String originatorContextKey) {
        final long loanId = getLoanId();
        final long originatorId = getOriginatorId(originatorContextKey);
        executeVoid(() -> fineractClient.workingCapitalLoanOriginators().attachOriginatorToWorkingCapitalLoan(loanId, originatorId));
    }

    private void assertCallFails(final Supplier<?> feignCall, final int expectedStatus) {
        assertExpectedStatus(fail(feignCall), expectedStatus);
    }

    private void assertVoidCallFails(final Runnable feignCall, final int expectedStatus) {
        assertExpectedStatus(failVoid(feignCall), expectedStatus);
    }

    private void assertExpectedStatus(final CallFailedRuntimeException ex, final int expectedStatus) {
        assertThat(ex.getStatus()).as(ErrorMessageHelper.wrongErrorCode(ex.getStatus(), expectedStatus)).isEqualTo(expectedStatus);
    }
}
