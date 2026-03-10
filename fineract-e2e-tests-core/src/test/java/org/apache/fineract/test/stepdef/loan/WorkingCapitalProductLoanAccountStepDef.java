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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalProductLoanAccountStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Long NON_EXISTENT_LOAN_ID = 999_999_999L;

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;
    private final EventCheckHelper eventCheckHelper;

    @When("Admin creates a working capital loan with the following data:")
    public void createWorkingCapitalLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createWorkingCapitalLoanAccount(data.get(1));
    }

    private void createWorkingCapitalLoanAccount(final List<String> loanData) {
        final String loanProduct = loanData.get(0);
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData);

        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        log.info("Working Capital Loan created with ID: {}", response.getLoanId());
    }

    @Then("Working capital loan creation was successful")
    public void verifyWorkingCapitalLoanCreationSuccess() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);

        assertNotNull(loanResponse, "Loan creation response should not be null");
        assertNotNull(loanResponse.getLoanId(), "Loan ID should not be null");
        assertNotNull(loanResponse.getResourceId(), "Resource ID should not be null");
        assertTrue(loanResponse.getLoanId() > 0, "Loan ID should be greater than 0");

        log.info("Verified working capital loan creation was successful. Loan ID: {}", loanResponse.getLoanId());
    }

    @Then("Working capital loan account has the correct data:")
    public void verifyWorkingCapitalLoanAccountData(final DataTable table) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));

        final List<List<String>> data = table.asLists();
        final List<String> expectedData = data.get(1);

        final String expectedProductName = expectedData.get(0);
        final String expectedSubmittedOnDate = expectedData.get(1);
        final String expectedDisbursementDate = expectedData.get(2);
        final String expectedStatus = expectedData.get(3);
        final String expectedPrincipal = expectedData.get(4);
        final String expectedTotalPayment = expectedData.get(5);
        final String expectedPeriodPaymentRate = expectedData.get(6);
        final String expectedDiscount = expectedData.get(7);

        assertNotNull(response, "Loan response should not be null");
        assertNotNull(response.getProduct(), "Product should not be null");
        assertThat(response.getProduct().getName()).as("Product name should match").isEqualTo(expectedProductName);
        assert response.getSubmittedOnDate() != null;
        assertThat(response.getSubmittedOnDate().toString()).as("Submitted on date should match").isEqualTo(expectedSubmittedOnDate);
        assertNotNull(response.getDisbursementDetails(), "Disbursement details should be present");
        assertThat(response.getDisbursementDetails()).as("Disbursement details should not be empty").isNotEmpty();
        assertThat(response.getDisbursementDetails().getFirst().getExpectedDisbursementDate().toString())
                .as("Expected disbursement date should match").isEqualTo(expectedDisbursementDate);
        assert response.getStatus() != null;
        assertThat(response.getStatus().getValue()).as("Status should match").isEqualTo(expectedStatus);
        assertNotNull(response.getBalance(), "Balance should be present");
        assertThat(response.getBalance().getPrincipalOutstanding()).as("Principal should match")
                .isEqualByComparingTo(new BigDecimal(expectedPrincipal));
        assertThat(response.getBalance().getTotalPayment()).as("Total payment should match")
                .isEqualByComparingTo(new BigDecimal(expectedTotalPayment));
        assertThat(response.getPeriodPaymentRate()).as("Period payment rate should match")
                .isEqualByComparingTo(new BigDecimal(expectedPeriodPaymentRate));

        if ("null".equals(expectedDiscount)) {
            assertThat(response.getDiscount()).as("Discount should be null").isNull();
        } else {
            assertThat(response.getDiscount()).as("Discount should match").isEqualByComparingTo(new BigDecimal(expectedDiscount));
        }

        log.info("Verified working capital loan account data for loan ID: {}", loanId);
    }

    @Then("Creating a working capital loan with LP overridables disabled and with the following data will result an error:")
    public void creatingWorkingCapitalLoanWithLpOverridablesDisabledWillResultAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.get(0);
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPayment = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);
        final String delinquencyBucketId = loanData.get(7);
        final String repaymentEvery = loanData.get(8);
        final String repaymentFrequencyType = loanData.get(9);

        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPayment(new BigDecimal(totalPayment))
                .periodPaymentRate(new BigDecimal(periodPaymentRate))
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null)
                .delinquencyBucketId(
                        delinquencyBucketId != null && !delinquencyBucketId.isEmpty() ? Long.valueOf(delinquencyBucketId) : null)
                .repaymentEvery(repaymentEvery != null && !repaymentEvery.isEmpty() ? Integer.valueOf(repaymentEvery) : null)
                .repaymentFrequencyType(repaymentFrequencyType != null && !repaymentFrequencyType.isEmpty()
                        ? PostWorkingCapitalLoansRequest.RepaymentFrequencyTypeEnum.valueOf(repaymentFrequencyType)
                        : null);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.delinquencyBucketId.override.not.allowed.by.product");
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.repaymentEvery.override.not.allowed.by.product");
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.repaymentFrequencyType.override.not.allowed.by.product");

        log.info("Verified working capital loan creation failed with expected validation errors for LP overridables disabled");
    }

    @Then("Creating a working capital loan with principal amount greater than Working Capital Loan Product max will result an error:")
    public void creatingAWorkingCapitalLoanWithPrincipalAmountGreaterThanWorkingCapitalLoanProductMaxWillResultAnError(
            final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.get(0);
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.less.than.or.equal.to.max");

        log.info("Verified working capital loan creation failed with principal amount exceeding max");
    }

    @Then("Creating a working capital loan with principal amount smaller than Working Capital Loan Product min will result an error:")
    public void creatingAWorkingCapitalLoanWithPrincipalAmountSmallerThanWorkingCapitalLoanProductMinWillResultAnError(
            final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.get(0);
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.greater.than.or.equal.to.min");

        log.info("Verified working capital loan creation failed with principal amount below min");
    }

    @Then("Creating a working capital loan with missing mandatory fields will result an error:")
    public void creatingAWorkingCapitalLoanWithMissingMandatoryFieldsWillResultAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.get(0);
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPayment = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);

        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate != null && !submittedOnDate.isEmpty() ? submittedOnDate : null)
                .expectedDisbursementDate(
                        expectedDisbursementDate != null && !expectedDisbursementDate.isEmpty() ? expectedDisbursementDate : null)
                .principalAmount(principal != null && !principal.isEmpty() ? new BigDecimal(principal) : null)
                .totalPayment(totalPayment != null && !totalPayment.isEmpty() ? new BigDecimal(totalPayment) : null)
                .periodPaymentRate(periodPaymentRate != null && !periodPaymentRate.isEmpty() ? new BigDecimal(periodPaymentRate) : null)
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);

        // Check for missing mandatory field errors
        if (principal == null || principal.isEmpty()) {
            log.info("Checking for principalAmount error: The parameter `principalAmount` is mandatory.");
            assertThat(exception.getMessage()).as("Should contain principalAmount mandatory error")
                    .contains("The parameter `principalAmount` is mandatory.");
        }

        if (totalPayment == null || totalPayment.isEmpty()) {
            log.info("Checking for totalPayment error: The parameter `totalPayment` is mandatory.");
            assertThat(exception.getMessage()).as("Should contain totalPayment mandatory error")
                    .contains("The parameter `totalPayment` is mandatory.");
        }

        if (periodPaymentRate == null || periodPaymentRate.isEmpty()) {
            log.info("Checking for periodPaymentRate error: The parameter `periodPaymentRate` is mandatory.");
            assertThat(exception.getMessage()).as("Should contain periodPaymentRate mandatory error")
                    .contains("The parameter `periodPaymentRate` is mandatory.");
        }

        if (expectedDisbursementDate == null || expectedDisbursementDate.isEmpty()) {
            log.info("Checking for expectedDisbursementDate error: The parameter `expectedDisbursementDate` is mandatory.");
            assertThat(exception.getMessage()).as("Should contain expectedDisbursementDate mandatory error")
                    .contains("The parameter `expectedDisbursementDate` is mandatory.");
        }

        log.info("Verified working capital loan creation failed with missing mandatory fields");
    }

    @When("Admin modifies the working capital loan with the following data:")
    public void modifyWorkingCapitalLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        modifyWorkingCapitalLoanAccount(data.get(1));
    }

    private void modifyWorkingCapitalLoanAccount(final List<String> loanData) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = buildModifyLoanRequest(loanData);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified with ID: {}", response.getResourceId());
    }

    @When("Admin modifies the working capital loan by externalId with the following data:")
    public void modifyWorkingCapitalLoanByExternalId(final DataTable table) {
        final List<List<String>> data = table.asLists();
        modifyWorkingCapitalLoanAccountByExternalId(data.get(1));
    }

    private void modifyWorkingCapitalLoanAccountByExternalId(final List<String> loanData) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final String externalId = retrieveLoanExternalId(loanId);
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = buildModifyLoanRequest(loanData);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationByExternalId(externalId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified by externalId: {} with resource ID: {}", externalId, response.getResourceId());
    }

    @Then("Changing submittedOnDate after expectedDisbursementDate results an error:")
    public void changingSubmittedOnDateAfterExpectedDisbursementDateResultsAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).get(0);

        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);

        log.info("HTTP status code: {}", exception.getStatus());

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);

        log.info(
                "Checking for submittedOnDate after expectedDisbursementDate error: submittedOnDate cannot be after expectedDisbursementDate.");
        assertThat(exception.getMessage()).as("Should contain submittedOnDate after expectedDisbursementDate error")
                .contains("The date on which a loan is submitted cannot be after its expected disbursement date");

        log.info("Verified working capital loan modification failed with submittedOnDate after expectedDisbursementDate");
    }

    @Then("Changing submittedOnDate after business date results an error:")
    public void changingSubmittedOnDateAfterBusinessDateResultsAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).get(0);
        final String expectedDisbursementDate = data.get(1).get(1);

        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate);
        if (expectedDisbursementDate != null && !expectedDisbursementDate.isBlank()) {
            modifyRequest.expectedDisbursementDate(expectedDisbursementDate);
        }

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);
        assertThat(exception.getMessage()).as("Should contain submittedOnDate cannot be in the future error")
                .contains("The date on which a loan is submitted cannot be in the future.");
    }

    @When("Admin deletes the working capital loan account")
    public void deleteWorkingCapitalLoanAccount() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final DeleteWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplication(loanId));
        testContext().set(TestContextKey.LOAN_DELETE_RESPONSE, response);
        log.info("Working Capital Loan deleted with ID: {}", response.getResourceId());
    }

    @When("Admin deletes the working capital loan account by externalId")
    public void deleteWorkingCapitalLoanAccountByExternalId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final String externalId = retrieveLoanExternalId(loanId);

        final DeleteWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplicationByExternalId(externalId));
        testContext().set(TestContextKey.LOAN_DELETE_RESPONSE, response);
        log.info("Working Capital Loan deleted by externalId: {} with resource ID: {}", externalId, response.getResourceId());
    }

    @Then("Working capital loan account deletion was successful")
    public void workingCapitalLoanAccountDeletionWasSuccessful() {
        final DeleteWorkingCapitalLoansLoanIdResponse deleteResponse = testContext().get(TestContextKey.LOAN_DELETE_RESPONSE);
        assertNotNull(deleteResponse);
        assertNotNull(deleteResponse.getResourceId());
        log.info("Verified working capital loan deletion was successful for loan ID: {}", deleteResponse.getResourceId());
    }

    @Then("Modifying the working capital loan with principal exceeding product max results in an error:")
    public void modifyingWithPrincipalExceedingProductMaxResultsInAnError(final DataTable table) {
        final BigDecimal principal = extractPrincipalFromModifyTable(table);

        final CallFailedRuntimeException exception = failModifyWithPrincipal(getCreatedLoanId(), principal);

        assertThat(exception.getStatus()).as("HTTP status code should be 400").isEqualTo(400);
        assertThat(exception.getMessage()).as("Should contain principal max validation error")
                .contains("validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.less.than.or.equal.to.max");
        log.info("Verified modification failed: principal exceeds product max");
    }

    @Then("Modifying the working capital loan with principal below product min results in an error:")
    public void modifyingWithPrincipalBelowProductMinResultsInAnError(final DataTable table) {
        final BigDecimal principal = extractPrincipalFromModifyTable(table);

        final CallFailedRuntimeException exception = failModifyWithPrincipal(getCreatedLoanId(), principal);

        assertThat(exception.getStatus()).as("HTTP status code should be 400").isEqualTo(400);
        assertThat(exception.getMessage()).as("Should contain principal min validation error")
                .contains("validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.greater.than.or.equal.to.min");
        log.info("Verified modification failed: principal below product min");
    }

    @Then("Modifying the working capital loan with empty request results in an error")
    public void modifyingWithEmptyRequestResultsInAnError() {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory
                .defaultModifyWorkingCapitalLoansRequest();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));

        assertThat(exception.getStatus()).as("HTTP status code should be 400").isEqualTo(400);
        assertThat(exception.getMessage()).as("Should contain no parameters error")
                .contains("validation.msg.WORKINGCAPITALLOAN.no.parameters.for.update");
        log.info("Verified modification failed with empty request");
    }

    @Then("Modifying the working capital loan with future submittedOnDate results in an error:")
    public void modifyingWithFutureSubmittedOnDateResultsInAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).get(0);
        final String expectedDisbursementDate = data.get(1).get(1);

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);
        assertThat(exception.getMessage()).as("Should contain future date error").contains("cannot.be.a.future.date");
        log.info("Verified modification failed: future submittedOnDate");
    }

    @When("Admin attempts to modify a non-existent working capital loan")
    public void adminAttemptsToModifyNonExistentWorkingCapitalLoan() {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .principalAmount(new BigDecimal("100"));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .modifyWorkingCapitalLoanApplicationById(NON_EXISTENT_LOAN_ID, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);
        log.info("Attempted to modify non-existent working capital loan ID {}", NON_EXISTENT_LOAN_ID);
    }

    @Then("Working capital loan modification fails with a 404 not found error")
    public void workingCapitalLoanModificationFailsWith404() {
        final CallFailedRuntimeException exception = testContext().get(TestContextKey.LOAN_MODIFY_RESPONSE);
        assertThat(exception.getStatus()).as("HTTP status code should be 404").isEqualTo(404);
        assertThat(exception.getMessage()).as("Should contain not found error").contains("does not exist");
        log.info("Verified modification failed: non-existent loan ID");
    }

    private Long getCreatedLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private BigDecimal extractPrincipalFromModifyTable(final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);
        return new BigDecimal(data.get("principalAmount"));
    }

    private CallFailedRuntimeException failModifyWithPrincipal(final Long loanId, final BigDecimal principal) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .principalAmount(principal);
        return fail(() -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
    }

    private Long extractClientId() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        return clientResponse.getClientId();
    }

    private Long resolveLoanProductId(final String loanProductName) {
        final DefaultWorkingCapitalLoanProduct product = DefaultWorkingCapitalLoanProduct.valueOf(loanProductName);
        return workingCapitalLoanProductResolver.resolve(product);
    }

    private PostWorkingCapitalLoansRequest buildCreateLoanRequest(final Long clientId, final Long productId, final List<String> loanData) {
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPayment = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId).productId(productId)
                .submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPayment(new BigDecimal(totalPayment))
                .periodPaymentRate(new BigDecimal(periodPaymentRate))
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);
    }

    private PutWorkingCapitalLoansLoanIdRequest buildModifyLoanRequest(final List<String> loanData) {
        final String submittedOnDate = loanData.get(0);
        final String expectedDisbursementDate = loanData.get(1);
        final String principal = loanData.get(2);
        final String totalPayment = loanData.get(3);
        final String periodPaymentRate = loanData.get(4);
        final String discount = loanData.get(5);

        return workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate != null && !submittedOnDate.isEmpty() ? submittedOnDate : null)
                .expectedDisbursementDate(
                        expectedDisbursementDate != null && !expectedDisbursementDate.isEmpty() ? expectedDisbursementDate : null)
                .principalAmount(principal != null && !principal.isEmpty() ? new BigDecimal(principal) : null)
                .totalPayment(totalPayment != null && !totalPayment.isEmpty() ? new BigDecimal(totalPayment) : null)
                .periodPaymentRate(periodPaymentRate != null && !periodPaymentRate.isEmpty() ? new BigDecimal(periodPaymentRate) : null)
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);
    }

    private String retrieveLoanExternalId(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        return loanDetails.getExternalId();
    }

    private void assertHttpStatus(final CallFailedRuntimeException exception, final int expectedStatus) {
        log.info("HTTP status code: {}", exception.getStatus());
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedStatus).isEqualTo(expectedStatus);
    }

    private void assertValidationError(final CallFailedRuntimeException exception, final String expectedMessage) {
        log.info("Validation error: {}", expectedMessage);
        assertThat(exception.getMessage()).as("Should contain validation error").contains(expectedMessage);
    }

    @Then("Working capital loan modification response contains changes for {string}")
    public void verifyModificationResponseContainsChanges(final String expectedField) {
        final PutWorkingCapitalLoansLoanIdResponse modifyResponse = testContext().get(TestContextKey.LOAN_MODIFY_RESPONSE);
        assertThat(modifyResponse).as("Modification response").isNotNull();
        assertThat(modifyResponse.getResourceId()).as("Resource ID").isNotNull();

        final Object changes = modifyResponse.getChanges();
        assertThat(changes).as("Changes map").isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        final Map<String, Object> changesMap = (Map<String, Object>) changes;
        assertThat(changesMap).as("Changes map should contain key '%s'", expectedField).containsKey(expectedField);
        log.info("Verified modification response contains changes for '{}': {}", expectedField, changesMap.get(expectedField));
    }
}
