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
import static org.apache.fineract.test.data.LoanStatus.ACTIVE;
import static org.apache.fineract.test.data.LoanStatus.APPROVED;
import static org.apache.fineract.test.data.LoanStatus.SUBMITTED_AND_PENDING_APPROVAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.CaseFormat;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.JournalEntriesApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.DeleteWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetDisbursementDetail;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCodeValueDataResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsPaymentDetailRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdDiscountRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.helper.WorkingCapitalScheduleMatcher;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalLoanAccountStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Long NON_EXISTENT_LOAN_ID = 999_999_999L;
    private static final String WC_DISBURSE_CLASSIFICATION_ID = "wcDisburseClassificationId";
    private static final String WC_DISBURSE_CLASSIFICATION_CODE_NAME = "working_capital_loan_disbursement_classification";
    private static final String WC_CBR_CLASSIFICATION_CODE_NAME = "working_capital_loan_credit_balance_refund_classification";
    private static final String WC_CBR_TEMPLATE_RESPONSE = "wcCbrTemplateResponse";
    private static final String WC_CBR_JOURNAL_ENTRIES_BEFORE = "wcCbrJournalEntriesBefore";
    private static final String WC_CBR_JOURNAL_ENTRIES_AFTER = "wcCbrJournalEntriesAfter";

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;
    private final WorkingCapitalRequestFactory workingCapitalProductRequestFactory;
    private final CodeHelper codeHelper;
    private final EventCheckHelper eventCheckHelper;
    private final PaymentTypeResolver paymentTypeResolver;
    private final BusinessDateHelper businessDateHelper;

    @When("Admin creates a working capital loan with the following data:")
    public void createWorkingCapitalLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createWorkingCapitalLoanAccount(data.get(1));
    }

    @When("Admin creates a working capital loan using created product with the following data:")
    public void createWorkingCapitalLoanUsingCreatedProduct(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> rawData = data.get(1);
        final Long clientId = extractClientId();
        final PostWorkingCapitalLoanProductsResponse productResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long loanProductId = productResponse.getResourceId();

        final String submittedOnDate = rawData.get(0);
        final String expectedDisbursementDate = rawData.get(1);
        final String principal = rawData.get(2);
        final String totalPayment = rawData.get(3);
        final String periodPaymentRate = rawData.get(4);
        final String discount = rawData.get(5);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPayment(new BigDecimal(totalPayment))
                .periodPaymentRate(new BigDecimal(periodPaymentRate))
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);

        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        log.info("Working Capital Loan created with dynamic product ID: {}, Loan ID: {}", loanProductId, response.getLoanId());
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
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoansLoanIdResponse response = retrieveLoanDetails(loanId);

        final List<List<String>> data = table.asLists();
        final List<String> header = table.row(0);
        final List<String> expectedValues = data.get(1);

        final List<String> actualValues = fetchValuesOfWorkingCapitalLoan(header, response);

        assertThat(actualValues).as("Working capital loan data should match expected values").isEqualTo(expectedValues);

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
        assertValidationError(exception, "validation.msg.WORKINGCAPITALLOAN.discount.override.not.allowed.by.product");

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

    @When("Admin modifies the working capital loan by externalId with the following data:")
    public void modifyWorkingCapitalLoanByExternalId(final DataTable table) {
        final List<List<String>> data = table.asLists();
        modifyWorkingCapitalLoanAccountByExternalId(data.get(1));
    }

    @Then("Changing submittedOnDate after expectedDisbursementDate results an error:")
    public void changingSubmittedOnDateAfterExpectedDisbursementDateResultsAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).get(0);

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);

        assertHttpStatus(exception, 403);
        assertValidationError(exception, "The date on which a loan is submitted cannot be after its expected disbursement date");

        log.info("Verified working capital loan modification failed with submittedOnDate after expectedDisbursementDate");
    }

    @Then("Changing submittedOnDate after business date results an error:")
    public void changingSubmittedOnDateAfterBusinessDateResultsAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).get(0);
        final String expectedDisbursementDate = data.get(1).get(1);

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate);
        if (expectedDisbursementDate != null && !expectedDisbursementDate.isBlank()) {
            modifyRequest.expectedDisbursementDate(expectedDisbursementDate);
        }

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);

        assertHttpStatus(exception, 403);
        assertValidationError(exception, "The date on which a loan is submitted cannot be in the future.");
    }

    @When("Admin deletes the working capital loan account")
    public void deleteWorkingCapitalLoanAccount() {
        deleteLoan(false);
    }

    @When("Admin deletes the working capital loan account by externalId")
    public void deleteWorkingCapitalLoanAccountByExternalId() {
        deleteLoan(true);
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

    @Then("Modifying the working capital loan that is Disbursed in Active state results in an error")
    public void modifyingDisbursedWithActiveStateLoanResultsInAnError() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory
                .defaultModifyWorkingCapitalLoansRequest();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));

        assertThat(exception.getStatus()).as("HTTP status code should be 403").isEqualTo(403);
        assertThat(exception.getMessage()).as("Should contain no parameters error")
                .contains(String.format("Working Capital Loan with identifier %d cannot be modified in its current state.", loanId));
        log.info("Verified modification failed with disbursed Active status empty request");
    }

    @Then("Working capital loan modification fails with a 404 not found error")
    public void workingCapitalLoanModificationFailsWith404() {
        final CallFailedRuntimeException exception = testContext().get(TestContextKey.LOAN_MODIFY_RESPONSE);
        assertThat(exception.getStatus()).as("HTTP status code should be 404").isEqualTo(404);
        assertThat(exception.getMessage()).as("Should contain not found error").contains("does not exist");
        log.info("Verified modification failed: non-existent loan ID");
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

    @When("Admin successfully approves the working capital loan on {string} with {string} amount and expected disbursement date on {string}")
    public void approveWorkingCapitalLoan(final String approveDate, final String approvedAmount, final String expectedDisbursementDate) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest().approvedOnDate(approveDate).approvedLoanAmount(new BigDecimal(approvedAmount))
                .expectedDisbursementDate(expectedDisbursementDate);
        testContext().set(TestContextKey.LOAN_APPROVAL_REQUEST, approveRequest);

        executeStateTransition("approve", approveRequest, TestContextKey.LOAN_APPROVAL_RESPONSE, false);
    }

    @When("Admin successfully approves the working capital loan by externalId on {string} with {string} amount and expected disbursement date on {string}")
    public void approveWorkingCapitalLoanByExternalId(final String approveDate, final String approvedAmount,
            final String expectedDisbursementDate) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest().approvedOnDate(approveDate).approvedLoanAmount(new BigDecimal(approvedAmount))
                .expectedDisbursementDate(expectedDisbursementDate);
        testContext().set(TestContextKey.LOAN_APPROVAL_REQUEST, approveRequest);

        executeStateTransition("approve", approveRequest, TestContextKey.LOAN_APPROVAL_RESPONSE, true);
    }

    @When("Admin successfully approves the working capital loan on {string} with {string} amount and {string} discount amount and expected disbursement date on {string}")
    public void approveWorkingCapitalLoanWithDiscount(final String approveDate, final String approvedAmount, final String discountAmount,
            final String expectedDisbursementDate) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(approveDate)//
                .approvedLoanAmount(new BigDecimal(approvedAmount))//
                .discountAmount(new BigDecimal(discountAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);
        testContext().set(TestContextKey.LOAN_APPROVAL_REQUEST, approveRequest);

        executeStateTransition("approve", approveRequest, TestContextKey.LOAN_APPROVAL_RESPONSE, false);
    }

    @Then("Working capital loan approval was successful")
    public void verifyWorkingCapitalLoanApprovalSuccess() {
        verifyStateTransitionSuccess(TestContextKey.LOAN_APPROVAL_RESPONSE, "approval");
    }

    @Then("Approval of working capital loan on {string} with {string} amount and expected disbursement date on {string} results an error with the following data:")
    public void approvalOfWorkingCapitalLoanResultsAnError(final String approveDate, final String approvedAmount,
            final String expectedDisbursementDate, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(approveDate)//
                .approvedLoanAmount(new BigDecimal(approvedAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);//

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "approve", approveRequest));

        verifyErrorResponse(exception, table);
        log.info("Verified working capital loan approval failed with expected error");
    }

    @When("Admin failed to approve the working capital loan on {string} with {string} amount and expected disbursement date on {string} with {string} exceeded discount amount")
    public void approveWorkingCapitalLoanWithExceededDiscountFailure(final String approveDate, final String approvedAmount,
            final String expectedDisbursementDate, final String discountAmount) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(approveDate)//
                .approvedLoanAmount(new BigDecimal(approvedAmount))//
                .discountAmount(new BigDecimal(discountAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);//

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "approve", approveRequest));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.discountAmountExceedFailure()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.discountAmountExceedFailure());
    }

    @When("Admin failed to approve the working capital loan on {string} with {string} amount and expected disbursement date on {string} with {string} discount amount due to override disallowed by product")
    public void approveWorkingCapitalLoanWithDiscountOverrideDisallowedFailure(final String approveDate, final String approvedAmount,
            final String expectedDisbursementDate, final String discountAmount) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(approveDate)//
                .approvedLoanAmount(new BigDecimal(approvedAmount))//
                .discountAmount(new BigDecimal(discountAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);//

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "approve", approveRequest));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.discountOverrideDisallowedByProductFailure()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.discountOverrideDisallowedByProductFailure());
    }

    @When("Admin rejects the working capital loan on {string}")
    public void rejectWorkingCapitalLoan(final String rejectDate) {
        final PostWorkingCapitalLoansLoanIdRequest rejectRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanRejectRequest()
                .rejectedOnDate(rejectDate);

        executeStateTransition("reject", rejectRequest, TestContextKey.LOAN_REJECT_RESPONSE, false);
    }

    @Then("Working capital loan rejection was successful")
    public void verifyWorkingCapitalLoanRejectionSuccess() {
        verifyStateTransitionSuccess(TestContextKey.LOAN_REJECT_RESPONSE, "rejection");
    }

    @When("Admin makes undo approval on the working capital loan")
    public void undoApprovalWorkingCapitalLoan() {
        final PostWorkingCapitalLoansLoanIdRequest undoApprovalRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUndoApprovalRequest();

        executeStateTransition("undoApproval", undoApprovalRequest, TestContextKey.LOAN_UNDO_APPROVAL_RESPONSE, false);
    }

    @Then("Working capital loan undo approval was successful")
    public void verifyWorkingCapitalLoanUndoApprovalSuccess() {
        verifyStateTransitionSuccess(TestContextKey.LOAN_UNDO_APPROVAL_RESPONSE, "undo approval");
    }

    @When("Undo approval on the working capital loan results an error with the following data:")
    public void undoApprovalWorkingCapitalLoan(final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest undoApprovalRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUndoApprovalRequest();

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "undoApproval", undoApprovalRequest));

        verifyErrorResponse(exception, table);
        log.info("Verified working capital loan undo approval failed with expected error");
    }

    @Then("Working Capital loan status will be {string}")
    public void loanWCStatus(String statusExpected) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));

        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        Long loanStatusActualValue = loanDetailsResponse.getStatus().getId();

        LoanStatus loanStatusExpected = LoanStatus.valueOf(statusExpected);
        Long loanStatusExpectedValue = loanStatusExpected.getValue().longValue();

        assertThat(loanStatusActualValue)
                .as(ErrorMessageHelper.wrongLoanStatus(resourceId, loanStatusActualValue.intValue(), loanStatusExpectedValue.intValue()))
                .isEqualTo(loanStatusExpectedValue);
    }

    @And("Admin successfully disburse the Working Capital loan on {string} with {string} EUR transaction amount")
    public void disburseWCLoan(String actualDisbursementDate, String transactionAmount) {
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate)//
                .transactionAmount(new BigDecimal(transactionAmount));
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);

        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);
        verifyStateTransitionSuccess(TestContextKey.LOAN_DISBURSE_RESPONSE, "disbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_DISBURSE_RESPONSE, ACTIVE);
    }

    @And("Admin successfully disburse the Working Capital loan by externalId on {string} with {string} EUR transaction amount")
    public void disburseWCLoanByExternalId(String actualDisbursementDate, String transactionAmount) {
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate)//
                .transactionAmount(new BigDecimal(transactionAmount));
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);

        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, true);
        verifyStateTransitionSuccess(TestContextKey.LOAN_DISBURSE_RESPONSE, "disbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_DISBURSE_RESPONSE, ACTIVE);
    }

    @And("Admin successfully disburse the Working Capital loan on {string} with {string} EUR transaction amount and valid classification")
    public void disburseWCLoanWithClassification(final String actualDisbursementDate, final String transactionAmount) {
        final Long classificationCodeId = codeHelper.retrieveCodeByName(WC_DISBURSE_CLASSIFICATION_CODE_NAME).getId();
        final PostCodeValueDataResponse codeValue = codeHelper.createCodeValue(classificationCodeId,
                new PostCodeValuesDataRequest().name(Utils.randomStringGenerator("WCL_CLS_", 8)).isActive(true).position(0));
        final Long classificationId = codeValue.getSubResourceId();
        testContext().set(WC_DISBURSE_CLASSIFICATION_ID, classificationId);

        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest().actualDisbursementDate(actualDisbursementDate)//
                .transactionAmount(new BigDecimal(transactionAmount))//
                .classificationId(classificationId);
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);

        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);
        verifyStateTransitionSuccess(TestContextKey.LOAN_DISBURSE_RESPONSE, "disbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_DISBURSE_RESPONSE, ACTIVE);
    }

    @Then("Verify Working Capital loan disbursement was successful on {string} with {string} EUR transaction amount")
    public void checkDisbursementData(String actualDisbursementDate, String transactionAmount) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        String getLoanStatus = loanDetailsResponse.getStatus().getValue();
        assertThat(getLoanStatus.toUpperCase()).isEqualTo(ACTIVE.name());

        GetDisbursementDetail disbursementDetails = loanDetailsResponse.getDisbursementDetails().stream().findFirst()
                .orElseThrow(() -> new RuntimeException(""));
        String formattedDate = disbursementDetails.getActualDisbursementDate().format(FORMATTER);
        assertThat(formattedDate).isEqualTo(actualDisbursementDate);
        assertThat(disbursementDetails.getActualAmount().compareTo(new BigDecimal(transactionAmount))).isEqualTo(0);
    }

    @And("Admin successfully disburse the Working Capital loan on {string} with {string} EUR transaction amount and {string} discount amount")
    public void disburseWCLoanWithDiscount(String actualDisbursementDate, String transactionAmount, String discountAmount) {
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate)//
                .discountAmount(new BigDecimal(discountAmount)).transactionAmount(new BigDecimal(transactionAmount));
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);

        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);
        verifyStateTransitionSuccess(TestContextKey.LOAN_DISBURSE_RESPONSE, "disbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_DISBURSE_RESPONSE, ACTIVE);
    }

    @When("Admin failed to disburse the working capital loan on {string} with {string} amount with {string} exceeded discount amount")
    public void disburseWorkingCapitalLoanWithExceededDiscountFailure(String actualDisbursementDate, String transactionAmount,
            String discountAmount) {
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate)//
                .discountAmount(new BigDecimal(discountAmount)).transactionAmount(new BigDecimal(transactionAmount));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "disburse", disburseRequest));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.discountAmountExceedFailure()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.discountAmountExceedFailure());
    }

    @When("Admin failed to disburse the working capital loan on {string} with {string} amount with {string} discount amount due to override disallowed by product")
    public void disburseWorkingCapitalLoanWithDiscountOverrideDisallowedFailure(final String actualDisbursementDate,
            final String transactionAmount, final String discountAmount) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest().actualDisbursementDate(actualDisbursementDate)//
                .discountAmount(new BigDecimal(discountAmount)).transactionAmount(new BigDecimal(transactionAmount));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "disburse", disburseRequest));

        assertThat(exception.getStatus()).as(ErrorMessageHelper.discountOverrideDisallowedByProductFailure()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.discountOverrideDisallowedByProductFailure());
    }

    @Then("Verify Working Capital loan disbursement was successful")
    public void checkDisbursementData() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        String getLoanStatus = loanDetailsResponse.getStatus().getValue();
        assertThat(getLoanStatus.toUpperCase()).isEqualTo(ACTIVE.name());

        PostWorkingCapitalLoansLoanIdRequest disburseLoanRequest = testContext().get(TestContextKey.LOAN_DISBURSE_REQUEST);

        GetDisbursementDetail disbursementDetails = loanDetailsResponse.getDisbursementDetails().stream().findFirst()
                .orElseThrow(() -> new RuntimeException(""));
        String formattedDate = disbursementDetails.getActualDisbursementDate().format(FORMATTER);
        assertThat(formattedDate).isEqualTo(disburseLoanRequest.getActualDisbursementDate());
        assertThat(disbursementDetails.getActualAmount().compareTo(disburseLoanRequest.getTransactionAmount())).isEqualTo(0);
    }

    @Then("Verify Working Capital loan disbursement transaction has classification")
    public void verifyDisbursementTransactionHasClassification() {
        final Long expectedClassificationId = testContext().get(WC_DISBURSE_CLASSIFICATION_ID);
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        final GetWorkingCapitalLoanTransactionIdResponse disbursementTransaction = loanDetailsResponse.getTransactions().stream()
                .filter(t -> t.getType() != null && "loanTransactionType.disbursement".equals(t.getType().getCode())
                        && !Boolean.TRUE.equals(t.getReversed()))
                .reduce((first, second) -> second).orElseThrow(() -> new IllegalStateException("Disbursement transaction not found"));

        assertThat(disbursementTransaction.getClassification()).as("Disbursement classification").isNotNull();
        assertThat(disbursementTransaction.getClassification().getId()).as("Disbursement classification id")
                .isEqualTo(expectedClassificationId);
    }

    @Then("Admin successfully undo Working Capital disbursal")
    public void undoDisbursalWCLoan() {
        PostWorkingCapitalLoansLoanIdRequest undoDisbursalRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUndoDisburseRequest();

        executeStateTransition("undodisbursal", undoDisbursalRequest, TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, false);
        verifyStateTransitionSuccess(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, "undoDisbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, APPROVED);
    }

    @Then("Admin successfully undo Working Capital disbursal by externalId")
    public void undoDisbursalWCLoanByexternalId() {
        PostWorkingCapitalLoansLoanIdRequest undoDisbursalRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUndoDisburseRequest();

        executeStateTransition("undodisbursal", undoDisbursalRequest, TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, true);
        verifyStateTransitionSuccess(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, "undoDisbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_UNDO_DISBURSE_RESPONSE, APPROVED);
    }

    @Then("Working Capital disbursal transaction business event is raised")
    public void workingCapitalDisbursalTransactionBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanDisbursalTransactionEventCheck(getCreatedLoanId());
    }

    @Then("Working Capital disbursal transaction business event is raised with {string} amount and reversed {string}")
    public void workingCapitalDisbursalTransactionBusinessEventIsRaisedWithAmountAndReversed(final String amount, final String reversed) {
        eventCheckHelper.workingCapitalLoanDisbursalTransactionEventCheck(getCreatedLoanId(), new BigDecimal(amount));
        assertThat(Boolean.parseBoolean(reversed)).isFalse();
    }

    @Then("Working Capital undo disbursal transaction business event is raised")
    public void workingCapitalUndoDisbursalTransactionBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanUndoDisbursalTransactionEventCheck(getCreatedLoanId());
    }

    @Then("Working Capital undo disbursal transaction business event is raised with {string} amount and reversed {string}")
    public void workingCapitalUndoDisbursalTransactionBusinessEventIsRaisedWithAmountAndReversed(final String amount,
            final String reversed) {
        eventCheckHelper.workingCapitalLoanUndoDisbursalTransactionEventCheck(getCreatedLoanId(), new BigDecimal(amount));
        assertThat(Boolean.parseBoolean(reversed)).isTrue();
    }

    @Then("Admin fails to disburse the Working Capital loan on {string} with {string} EUR transaction amount because of not approved")
    public void disburseWCLoanFailureWithNotApproved(String actualDisbursementDate, String transactionAmount) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId,
                disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.dateFailureErrorCodeMsg()).isEqualTo(400);
        assertThat(exception.getDeveloperMessage())
                .contains(ErrorMessageHelper.disburseNotApprovedFailure(SUBMITTED_AND_PENDING_APPROVAL.name()));
    }

    @Then("Admin fails to disburse the Working Capital loan on {string} with {string} EUR transaction amount because of loan status {string} with status code {int}")
    public void disburseWCLoanFailureDueToStatus(String actualDisbursementDate, String transactionAmount, String loanStatus,
            int statusCode) {
        disburseWCLoanFailure(actualDisbursementDate, transactionAmount, statusCode,
                ErrorMessageHelper.disburseNotApprovedFailure(loanStatus));
    }

    @Then("Admin fails to disburse the Working Capital loan on {string} with {string} EUR transaction amount with invalid data outcomes with error message {string}")
    public void disburseWCLoanFailureWithInvalidData(String actualDisbursementDate, String transactionAmount,
            String errorMessageDescription) {
        String errorMessage = ErrorMessageHelper.disburseDateFailure(errorMessageDescription);
        disburseWCLoanFailure(actualDisbursementDate, transactionAmount, 400, errorMessage);
    }

    @Then("Admin fails to disburse the Working Capital loan on {string} with {string} EUR transaction amount without mandatory data outcomes with error message {string}")
    public void disburseWCLoanFailureWithoutMandatoryData(String actualDisbursementDate, String transactionAmount, String errorMessage) {
        disburseWCLoanFailure(actualDisbursementDate, transactionAmount, 400, errorMessage);
    }

    @Then("Admin fails to undo disbursal the Working Capital loan due to loan status {string}")
    public void undoDisbursalWCLoanFailure(String actualLoanStatus) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PostWorkingCapitalLoansLoanIdRequest undoDisbursalRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUndoDisburseRequest();

        CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId,
                undoDisbursalRequest, Map.of("command", "undodisbursal")));
        assertThat(exception.getStatus()).as(ErrorMessageHelper.undoDisbursalDisallowedFailure(actualLoanStatus)).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(ErrorMessageHelper.undoDisbursalDisallowedFailure(actualLoanStatus));
    }

    @And("Admin successfully update discount with {string} amount on Working Capital loan account")
    public void updateDiscountWCLoan(String discountAmount) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PutWorkingCapitalLoansLoanIdDiscountRequest updateDiscountRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUpdateDiscountRequest().discountAmount(new BigDecimal(discountAmount));

        PutWorkingCapitalLoansLoanIdResponse updateDiscountResponse = ok(
                () -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanDiscountById(loanId, updateDiscountRequest));

        log.info("Working Capital Loan discount updated with ID: {}", updateDiscountResponse.getResourceId());
    }

    @And("Update discount with {string} amount on Working Capital loan account failed due to already added discount before disbursement")
    public void updateDiscountWCLoanAlreadyAddedFailure(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountAlreadySetBeforeDisburseFailure();
        updateDiscountFailedCheck(discountAmount, errorMessage);
    }

    @And("Update discount with {string} amount on Working Capital loan account failed due to date diff from disbursement date")
    public void updateDiscountWCLoanDiffFromDisburseDateFailure(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountDiffDateFromDisburseFailure();
        updateDiscountFailedCheck(discountAmount, errorMessage);
    }

    @And("Update discount with {string} amount on Working Capital loan account failed due to override disallowed by product")
    public void updateDiscountWCLoanOverrideDisallowedByProductFailure(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountOverrideDisallowedByProductFailure();
        updateDiscountFailedCheck(discountAmount, errorMessage);
    }

    // ====================================
    // Private Helper Methods
    // ====================================

    // Loan Lifecycle Helpers
    private void createWorkingCapitalLoanAccount(final List<String> loanData) {
        final String loanProduct = loanData.get(0);
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData);
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);

        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        trackLoanIdIfEnabled(response.getLoanId());
        log.info("Working Capital Loan created with ID: {}", response.getLoanId());
    }

    @SuppressWarnings("unchecked")
    private void trackLoanIdIfEnabled(final Long loanId) {
        if (testContext().get(TestContextKey.WC_LOAN_IDS) == null) {
            testContext().set(TestContextKey.WC_LOAN_IDS, new ArrayList<>());
        }
        ((List<Long>) testContext().get(TestContextKey.WC_LOAN_IDS)).add(loanId);
    }

    private void modifyWorkingCapitalLoanAccount(final List<String> loanData) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = buildModifyLoanRequest(loanData);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified with ID: {}", response.getResourceId());
    }

    private void modifyWorkingCapitalLoanAccountByExternalId(final List<String> loanData) {
        final Long loanId = getCreatedLoanId();
        final String externalId = retrieveLoanExternalId(loanId);
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = buildModifyLoanRequest(loanData);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationByExternalId(externalId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified by externalId: {} with resource ID: {}", externalId, response.getResourceId());
    }

    private void deleteLoan(final boolean useExternalId) {
        final Long loanId = getCreatedLoanId();

        final DeleteWorkingCapitalLoansLoanIdResponse response;
        if (useExternalId) {
            final String externalId = retrieveLoanExternalId(loanId);
            response = ok(() -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplicationByExternalId(externalId));
            log.info("Working Capital Loan deleted by externalId: {} with resource ID: {}", externalId, response.getResourceId());
        } else {
            response = ok(() -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplication(loanId));
            log.info("Working Capital Loan deleted with ID: {}", response.getResourceId());
        }

        testContext().set(TestContextKey.LOAN_DELETE_RESPONSE, response);
    }

    private void executeStateTransition(final String command, final PostWorkingCapitalLoansLoanIdRequest request, final String responseKey,
            final boolean useExternalId) {
        final long loanId = getCreatedLoanId();

        final PostWorkingCapitalLoansLoanIdResponse response;
        if (useExternalId) {
            final String loanExternalId = retrieveLoanExternalId(loanId);
            response = ok(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanByExternalId(loanExternalId, command,
                    request));
            log.info("Working Capital Loan with externalId {} {} successful", loanExternalId, command);
        } else {
            response = ok(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, command, request));
            log.info("Working Capital Loan {} {} successful", loanId, command);
        }

        testContext().set(responseKey, response);
    }

    public void updateDiscountFailedCheck(String discountAmount, String errorMessage) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PutWorkingCapitalLoansLoanIdDiscountRequest updateDiscountRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUpdateDiscountRequest().discountAmount(new BigDecimal(discountAmount));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanDiscountById(loanId, updateDiscountRequest));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    // Data Extraction Helpers
    private Long getCreatedLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private Long extractClientId() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        return clientResponse.getClientId();
    }

    private Long resolveLoanProductId(final String loanProductName) {
        if ("WCLP_DELINQUENCY".equals(loanProductName)) {
            final PostWorkingCapitalLoanProductsResponse response = testContext()
                    .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
            return response.getResourceId();
        }
        final DefaultWorkingCapitalLoanProduct product = DefaultWorkingCapitalLoanProduct.valueOf(loanProductName);
        return workingCapitalLoanProductResolver.resolve(product);
    }

    private BigDecimal extractPrincipalFromModifyTable(final DataTable table) {
        final Map<String, String> data = table.asMaps().get(0);
        return new BigDecimal(data.get("principalAmount"));
    }

    private List<String> fetchValuesOfWorkingCapitalLoan(final List<String> header, final GetWorkingCapitalLoansLoanIdResponse response) {
        final List<String> actualValues = new ArrayList<>();
        for (final String headerName : header) {
            switch (headerName) {
                case "product.name" -> actualValues.add(response.getProduct() == null ? null : response.getProduct().getName());
                case "submittedOnDate" ->
                    actualValues.add(response.getSubmittedOnDate() == null ? null : response.getSubmittedOnDate().toString());
                case "expectedDisbursementDate" ->
                    actualValues.add(response.getDisbursementDetails() == null || response.getDisbursementDetails().isEmpty() ? null
                            : response.getDisbursementDetails().getFirst().getExpectedDisbursementDate().toString());
                case "status" -> actualValues.add(response.getStatus() == null ? null : response.getStatus().getValue());
                case "principal" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getPrincipalOutstanding() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getPrincipalOutstanding().doubleValue()).format());
                case "approvedPrincipal" -> actualValues.add(response.getApprovedPrincipal() == null ? "0"
                        : new Utils.DoubleFormatter(response.getApprovedPrincipal().doubleValue()).format());
                case "totalPayment" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getTotalPayment() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getTotalPayment().doubleValue()).format());
                case "periodPaymentRate" -> actualValues.add(response.getPeriodPaymentRate() == null ? null
                        : new Utils.DoubleFormatter(response.getPeriodPaymentRate().doubleValue()).format());
                case "discount" -> actualValues.add(
                        response.getDiscount() == null ? "null" : new Utils.DoubleFormatter(response.getDiscount().doubleValue()).format());
                case "totalPaidPrincipal" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getTotalPaidPrincipal() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getTotalPaidPrincipal().doubleValue()).format());
                case "realizedIncome" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getRealizedIncome() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getRealizedIncome().doubleValue()).format());
                case "unrealizedIncome" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getUnrealizedIncome() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getUnrealizedIncome().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    // Request Builders
    private PostWorkingCapitalLoansRequest buildCreateLoanRequest(final Long clientId, final Long productId, final List<String> loanData) {
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPayment = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principalAmount(new BigDecimal(principal))//
                .totalPayment(new BigDecimal(totalPayment))//
                .periodPaymentRate(new BigDecimal(periodPaymentRate))//
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);//
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

    // API Call Helpers
    private GetWorkingCapitalLoansLoanIdResponse retrieveLoanDetails(final Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
    }

    private String retrieveLoanExternalId(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        return loanDetails.getExternalId();
    }

    private CallFailedRuntimeException failModifyWithPrincipal(final Long loanId, final BigDecimal principal) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .principalAmount(principal);
        return fail(() -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
    }

    // Assertion Helpers
    private void assertHttpStatus(final CallFailedRuntimeException exception, final int expectedStatus) {
        log.info("HTTP status code: {}", exception.getStatus());
        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedStatus).isEqualTo(expectedStatus);
    }

    private void assertValidationError(final CallFailedRuntimeException exception, final String expectedMessage) {
        log.info("Validation error: {}", expectedMessage);
        assertThat(exception.getMessage()).as("Should contain validation error").contains(expectedMessage);
    }

    private void verifyStateTransitionSuccess(final String responseKey, final String operationName) {
        final PostWorkingCapitalLoansLoanIdResponse response = testContext().get(responseKey);

        assertNotNull(response, "Loan " + operationName + " response should not be null");
        assertNotNull(response.getLoanId(), "Loan ID should not be null");
        assertNotNull(response.getResourceId(), "Resource ID should not be null");
        assertTrue(response.getLoanId() > 0, "Loan ID should be greater than 0");

        log.info("Verified working capital loan {} was successful. Loan ID: {}", operationName, response.getLoanId());
    }

    private void verifyErrorResponse(final CallFailedRuntimeException exception, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String expectedHttpCode = data.get(1).get(0);
        final String expectedErrorMessage = data.get(1).get(1);

        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode)
                .isEqualTo(Integer.parseInt(expectedHttpCode));
        assertThat(exception.getMessage()).as("Should contain error message").contains(expectedErrorMessage);
    }

    public void checkChangesExpectedStatus(String responseKey, LoanStatus expectedStatus) {
        final PostWorkingCapitalLoansLoanIdResponse response = testContext().get(responseKey);
        final Object changes = response.getChanges();
        assertThat(changes).as("Changes map").isNotNull().isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        final Map<String, Object> changesMap = (Map<String, Object>) changes;
        assertThat(changesMap).as("Changes map should contain value '%s'", expectedStatus).containsValue(expectedStatus.name());
    }

    public void disburseWCLoanFailure(String actualDisbursementDate, String transactionAmount, int errorCode, String errorMessage) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();
        PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));

        CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId,
                disburseRequest, Map.of("command", "disburse")));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @When("Admin creates a Working Capital Loan Product with delinquencyGraceDays {int} and delinquencyStartType {string} for loan test")
    public void createProductWithGraceDaysForLoanTest(int graceDays, String startType) {
        final String name = "WCLP-GD-" + Utils.randomStringGenerator("", 8);
        final PostWorkingCapitalLoanProductsRequest request = workingCapitalProductRequestFactory.defaultWorkingCapitalLoanProductRequest() //
                .name(name) //
                .delinquencyGraceDays(graceDays) //
                .delinquencyStartType(startType);
        final PostWorkingCapitalLoanProductsResponse response = ok(
                () -> fineractClient.workingCapitalLoanProducts().createWorkingCapitalLoanProduct(request, Map.of()));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_FOR_LOAN_TEST, response.getResourceId());
        log.info("Created WC Loan Product with grace days for loan test, ID: {}", response.getResourceId());
    }

    @When("Admin creates a working capital loan with the grace days product and the following data:")
    public void createLoanWithGraceDaysProduct(final DataTable table) {
        final Map<String, String> row = table.asMaps().get(0);
        submitLoanAndStore(buildGraceDaysLoanRequest(row));
    }

    @When("Admin creates a working capital loan with grace days override and the following data:")
    public void createLoanWithGraceDaysOverride(final DataTable table) {
        final Map<String, String> row = table.asMaps().get(0);
        final PostWorkingCapitalLoansRequest request = buildGraceDaysLoanRequest(row) //
                .delinquencyGraceDays(
                        Optional.ofNullable(row.get("delinquencyGraceDays")).filter(s -> !s.isEmpty()).map(Integer::valueOf).orElse(null)) //
                .delinquencyStartType(row.get("delinquencyStartType"));
        submitLoanAndStore(request);
    }

    private PostWorkingCapitalLoansRequest buildGraceDaysLoanRequest(final Map<String, String> row) {
        final Long clientId = extractClientId();
        final Long productId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_FOR_LOAN_TEST);
        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId) //
                .productId(productId) //
                .submittedOnDate(row.get("submittedOnDate")) //
                .expectedDisbursementDate(row.get("expectedDisbursementDate")) //
                .principalAmount(new BigDecimal(row.get("principalAmount"))) //
                .totalPayment(new BigDecimal(row.get("totalPayment"))) //
                .periodPaymentRate(new BigDecimal(row.get("periodPaymentRate"))) //
                .discount(Optional.ofNullable(row.get("discount")).filter(s -> !s.isEmpty()).map(BigDecimal::new).orElse(null));
    }

    private void submitLoanAndStore(final PostWorkingCapitalLoansRequest request) {
        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        log.info("Working Capital Loan created, loan ID: {}", response.getLoanId());
    }

    @Then("Working capital loan account has delinquencyGraceDays {int} and delinquencyStartType {string}")
    public void verifyLoanGraceDays(int expectedGraceDays, String expectedStartType) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));

        assertThat(response.getDelinquencyGraceDays()).as("delinquencyGraceDays").isEqualTo(expectedGraceDays);
        assertThat(response.getDelinquencyStartType()).as("delinquencyStartType").isNotNull();
        assertThat(response.getDelinquencyStartType().getCode()).as("delinquencyStartType code").isEqualTo(expectedStartType);
    }

    @When("Admin modifies the working capital loan with grace days:")
    public void modifyLoanWithGraceDays(final DataTable table) {
        final Map<String, String> row = table.asMaps().get(0);
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .delinquencyGraceDays(
                        Optional.ofNullable(row.get("delinquencyGraceDays")).filter(s -> !s.isEmpty()).map(Integer::valueOf).orElse(null)) //
                .delinquencyStartType(row.get("delinquencyStartType"));

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
    }

    @When("Admin approves the working capital loan on {string}")
    public void approveWorkingCapitalLoan(final String approvedOnDate) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        final PostWorkingCapitalLoansLoanIdRequest approveRequest = new PostWorkingCapitalLoansLoanIdRequest() //
                .approvedOnDate(approvedOnDate) //
                .expectedDisbursementDate(approvedOnDate) //
                .dateFormat(DATE_FORMAT) //
                .locale(WorkingCapitalLoanRequestFactory.DEFAULT_LOCALE);

        ok(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "approve", approveRequest));
        log.info("Approved working capital loan {}", loanId);
    }

    @Then("Creating a working capital loan with invalid delinquencyGraceDays {int} will result with status code {int}")
    public void createLoanWithInvalidGraceDays(int graceDays, int expectedStatus) {
        final Long clientId = extractClientId();
        final Long productId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_FOR_LOAN_TEST);

        final PostWorkingCapitalLoansRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(productId) //
                .delinquencyGraceDays(graceDays);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        assertThat(exception.getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    }

    @Then("Creating a working capital loan with invalid delinquencyStartType {string} will result with status code {int}")
    public void createLoanWithInvalidStartType(String startType, int expectedStatus) {
        final Long clientId = extractClientId();
        final Long productId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_FOR_LOAN_TEST);

        final PostWorkingCapitalLoansRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(productId) //
                .delinquencyStartType(startType);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        assertThat(exception.getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    }

    @Then("Creating a working capital loan with breachId {long} on {string} will result with status code {int}")
    public void createLoanWithInvalidBreachId(final long breachId, final String submittedOnDate, final int expectedStatus) {
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(DefaultWorkingCapitalLoanProduct.WCLP.name());

        final PostWorkingCapitalLoansRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId) //
                .submittedOnDate(submittedOnDate) //
                .expectedDisbursementDate(submittedOnDate) //
                .principalAmount(new BigDecimal("100")) //
                .totalPayment(new BigDecimal("100")) //
                .periodPaymentRate(new BigDecimal("1")) //
                .discount(BigDecimal.ZERO) //
                .breachId(breachId);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        assertThat(exception.getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    }

    @Then("Creating a working capital loan with breach override allowed {string} on {string} will result with status code {int}")
    public void createLoanWithBreachOverrideAllowed(final String breachOverrideAllowed, final String submittedOnDate,
            final int expectedStatus) {
        final Long clientId = extractClientId();
        final boolean overrideAllowed = Boolean.parseBoolean(breachOverrideAllowed);

        final Long productBreachId = createBreachAndGetId();
        final Long overrideBreachId = createBreachAndGetId();
        final Long productId = createWorkingCapitalProductForBreachOverride(overrideAllowed, productBreachId);

        final PostWorkingCapitalLoansRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(productId) //
                .submittedOnDate(submittedOnDate) //
                .expectedDisbursementDate(submittedOnDate) //
                .principalAmount(new BigDecimal("100")) //
                .totalPayment(new BigDecimal("100")) //
                .periodPaymentRate(new BigDecimal("1")) //
                .discount(null) //
                .breachId(overrideBreachId);

        if (expectedStatus == 200) {
            final PostWorkingCapitalLoansResponse response = ok(
                    () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
            assertThat(response).isNotNull();
            assertThat(response.getLoanId()).isNotNull();
            return;
        }

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        assertThat(exception.getStatus()).as("HTTP status").isEqualTo(expectedStatus);
    }

    private Long createBreachAndGetId() {
        final CommandProcessingResult breachResponse = ok(() -> fineractClient.workingCapitalBreaches()
                .createWorkingCapitalBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalBreachRequest()));
        return breachResponse.getResourceId();
    }

    private Long createWorkingCapitalProductForBreachOverride(final boolean breachOverrideAllowed, final Long breachId) {
        final String name = DefaultWorkingCapitalLoanProduct.WCLP.getName() + Utils.randomStringGenerator("_", 10);
        final PostAllowAttributeOverrides allowOverrides = new PostAllowAttributeOverrides().breach(breachOverrideAllowed);

        final PostWorkingCapitalLoanProductsRequest productRequest = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest() //
                .name(name) //
                .breachId(breachId) //
                .allowAttributeOverrides(allowOverrides);

        final PostWorkingCapitalLoanProductsResponse productResponse = ok(
                () -> fineractClient.workingCapitalLoanProducts().createWorkingCapitalLoanProduct(productRequest, Map.of()));
        return productResponse.getResourceId();
    }

    @Then("Customer makes repayment on {string} with {double} transaction amount on Working Capital loan")
    public void makeWorkingCapitalLoanRepayment(final String transactionDate, final double transactionAmount) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount, null);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentById(loanId, repaymentRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    @Then("Customer makes repayment by loan external ID on {string} with {double} transaction amount on Working Capital loan")
    public void makeWorkingCapitalLoanRepaymentByExternalId(final String transactionDate, final double transactionAmount) {
        final Long loanId = getCreatedLoanId();
        final String loanExternalId = retrieveLoanExternalId(loanId);
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount, null);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentByExternalId(loanExternalId, repaymentRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanExternalId);
    }

    @Then("Customer makes repayment on {string} with {double} transaction amount on Working Capital loan with the following payment details:")
    public void makeWorkingCapitalLoanRepaymentWithPaymentDetails(final String transactionDate, final double transactionAmount,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails = buildPaymentDetailsFromTable(table);
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount,
                paymentDetails);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentById(loanId, repaymentRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    @Then("Customer makes credit balance refund on {string} with {double} transaction amount on Working Capital loan")
    public void makeWorkingCapitalLoanCreditBalanceRefund(final String transactionDate, final double transactionAmount) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null);
        final PostWorkingCapitalLoanTransactionsResponse response = executeCreditBalanceRefundById(loanId, cbrRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    @Then("Customer makes credit balance refund on {string} with {double} transaction amount on Working Capital loan with the following payment details:")
    public void makeWorkingCapitalLoanCreditBalanceRefundWithPaymentDetails(final String transactionDate, final double transactionAmount,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails = buildPaymentDetailsFromTable(table);
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                paymentDetails);
        final PostWorkingCapitalLoanTransactionsResponse response = executeCreditBalanceRefundById(loanId, cbrRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    private PostWorkingCapitalLoanTransactionsRequest buildCreditBalanceRefundRequest(final String transactionDate,
            final double transactionAmount, final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails) {
        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(BigDecimal.valueOf(transactionAmount));

        if (paymentDetails != null) {
            request.paymentDetails(paymentDetails);
        }
        return request;
    }

    private PostWorkingCapitalLoanTransactionsResponse executeCreditBalanceRefundById(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest cbrRequest) {
        log.debug("Making creditBalanceRefund for loan ID: {}, transactionDate: {}, transactionAmount: {}", loanId,
                cbrRequest.getTransactionDate(), cbrRequest.getTransactionAmount());
        final int before = countJournalEntriesForLoan(loanId);
        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));
        final int after = countJournalEntriesForLoan(loanId);
        testContext().set(WC_CBR_JOURNAL_ENTRIES_BEFORE, before);
        testContext().set(WC_CBR_JOURNAL_ENTRIES_AFTER, after);
        return response;
    }

    private PostWorkingCapitalLoanTransactionsRequest buildRepaymentRequest(final String transactionDate, final double transactionAmount,
            final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails) {
        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().transactionDate(transactionDate)
                .transactionAmount(BigDecimal.valueOf(transactionAmount));

        if (paymentDetails != null) {
            request.paymentDetails(paymentDetails);
        }

        return request;
    }

    private PostWorkingCapitalLoanTransactionsResponse executeRepaymentById(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest repaymentRequest) {
        log.debug("Making repayment for loan ID: {}, transactionDate: {}, transactionAmount: {}", loanId,
                repaymentRequest.getTransactionDate(), repaymentRequest.getTransactionAmount());

        return ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId, "repayment",
                repaymentRequest));
    }

    private PostWorkingCapitalLoanTransactionsResponse executeRepaymentByExternalId(final String loanExternalId,
            final PostWorkingCapitalLoanTransactionsRequest repaymentRequest) {
        log.debug("Making repayment for loan externalId: {}, transactionDate: {}, transactionAmount: {}", loanExternalId,
                repaymentRequest.getTransactionDate(), repaymentRequest.getTransactionAmount());

        return ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionByExternalId(loanExternalId,
                "repayment", repaymentRequest));
    }

    @Then("Working Capital loan amortization schedule has {int} periods, with the following data for periods:")
    public void verifyAmortizationSchedulePeriods(final int linesExpected, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final ProjectedAmortizationScheduleData schedule = ok(
                () -> fineractClient.workingCapitalLoans().retrieveAmortizationSchedule(loanId));
        assertNotNull(schedule, "Amortization schedule should not be null");
        assertNotNull(schedule.getPayments(), "Amortization schedule payments should not be null");

        final List<ProjectedAmortizationSchedulePaymentData> periods = schedule.getPayments();
        final int linesActual = (int) periods.stream().filter(p -> p.getPaymentNo() != null).count();

        final List<List<String>> data = table.asLists();
        for (int i = 1; i < data.size(); i++) {
            final List<String> expectedValues = data.get(i);
            final List<String> headers = data.getFirst();
            final int dateColumn = headers.indexOf("paymentDate");
            assertThat(dateColumn).as("Table must contain 'paymentDate' column").isGreaterThanOrEqualTo(0);
            final String paymentDateExpected = expectedValues.get(dateColumn);

            final List<ProjectedAmortizationSchedulePaymentData> matchingPeriods = periods.stream()
                    .filter(p -> p.getPaymentDate() != null && paymentDateExpected.equals(FORMATTER.format(p.getPaymentDate()))).toList();

            final boolean containsExpectedValues = matchingPeriods.stream()
                    .anyMatch(period -> matchesExpectedWcAmortizationRow(headers, expectedValues, period));
            assertThat(containsExpectedValues).as(
                    "Wrong value in line %s of amortization schedule. actual=%s, expected=%s", i, matchingPeriods.stream()
                            .map(period -> fetchValuesOfWcAmortizationSchedule(headers, period)).collect(Collectors.toList()),
                    expectedValues).isTrue();
        }

        assertThat(linesActual).as("Wrong number of lines in WC amortization schedule. actual=%s, expected=%s", linesActual, linesExpected)
                .isEqualTo(linesExpected);
    }

    private String asText(final BigDecimal value) {
        return value == null ? null : value.toString();
    }

    private boolean matchesExpectedWcAmortizationRow(final List<String> headers, final List<String> expectedValues,
            final ProjectedAmortizationSchedulePaymentData period) {
        for (int idx = 0; idx < headers.size(); idx++) {
            final String header = headers.get(idx);
            final String expected = expectedValues.get(idx);
            final String actual = extractWcScheduleCellValue(header, period);
            final boolean matches = "paymentDate".equals(header) ? WorkingCapitalScheduleMatcher.matchesFormattedDate(actual, expected)
                    : "discountFactor".equals(header)
                            ? WorkingCapitalScheduleMatcher.matchesDecimalWithScale(parseDecimal(actual), expected, 10)
                            : WorkingCapitalScheduleMatcher.matchesDecimal(parseDecimal(actual), expected);
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    private List<String> fetchValuesOfWcAmortizationSchedule(final List<String> header,
            final ProjectedAmortizationSchedulePaymentData period) {
        final List<String> actualValues = new ArrayList<>();
        for (final String headerName : header) {
            actualValues.add(extractWcScheduleCellValue(headerName, period));
        }
        return actualValues;
    }

    private String extractWcScheduleCellValue(final String headerName, final ProjectedAmortizationSchedulePaymentData period) {
        return switch (headerName) {
            case "paymentNo" -> period.getPaymentNo() == null ? null : period.getPaymentNo().toString();
            case "paymentDate" -> period.getPaymentDate() == null ? null : FORMATTER.format(period.getPaymentDate());
            case "count" -> period.getCount() == null ? null : period.getCount().toString();
            case "paymentsLeft" -> period.getPaymentsLeft() == null ? null : period.getPaymentsLeft().toString();
            case "expectedPaymentAmount" -> asText(period.getExpectedPaymentAmount());
            case "forecastPaymentAmount" -> asText(period.getForecastPaymentAmount());
            case "discountFactor" -> asText(period.getDiscountFactor());
            case "npvValue" -> asText(period.getNpvValue());
            case "balance" -> asText(period.getBalance());
            case "expectedAmortizationAmount" -> asText(period.getExpectedAmortizationAmount());
            case "netAmortizationAmount" -> asText(period.getNetAmortizationAmount());
            case "actualPaymentAmount" -> asText(period.getActualPaymentAmount());
            case "actualAmortizationAmount" -> asText(period.getActualAmortizationAmount());
            case "incomeModification" -> asText(period.getIncomeModification());
            case "deferredBalance" -> asText(period.getDeferredBalance());
            default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
        };
    }

    private BigDecimal parseDecimal(final String value) {
        return WorkingCapitalScheduleMatcher.isBlank(value) ? null : new BigDecimal(value);
    }

    private void validateRepaymentResponse(final PostWorkingCapitalLoanTransactionsResponse response, final double transactionAmount,
            final String transactionDate, final Object loanIdentifier) {
        assertNotNull(response, "Repayment response should not be null");
        assertNotNull(response.getResourceId(), "Repayment transaction ID should not be null");
        log.debug("Working Capital loan repayment of {} made on {} for loan {}, transaction ID: {}", transactionAmount, transactionDate,
                loanIdentifier, response.getResourceId());
    }

    private PostWorkingCapitalLoanTransactionsPaymentDetailRequest buildPaymentDetailsFromTable(final DataTable table) {
        final Map<String, String> paymentDetailsMap = convertDataTableToMap(table);
        return buildPaymentDetailsObject(paymentDetailsMap);
    }

    private Map<String, String> convertDataTableToMap(final DataTable table) {
        final List<List<String>> rows = table.asLists(String.class);
        final List<String> headers = rows.get(0);
        final List<String> values = rows.get(1);

        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i), values.get(i));
        }
        return map;
    }

    private PostWorkingCapitalLoanTransactionsPaymentDetailRequest buildPaymentDetailsObject(final Map<String, String> paymentDetailsMap) {
        final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails = new PostWorkingCapitalLoanTransactionsPaymentDetailRequest();

        if (paymentDetailsMap.containsKey("paymentType")) {
            final DefaultPaymentType paymentType = DefaultPaymentType.valueOf(paymentDetailsMap.get("paymentType"));
            final long paymentTypeId = paymentTypeResolver.resolve(paymentType);
            paymentDetails.paymentTypeId(paymentTypeId);
        }
        if (paymentDetailsMap.containsKey("accountNumber")) {
            paymentDetails.accountNumber(paymentDetailsMap.get("accountNumber"));
        }
        if (paymentDetailsMap.containsKey("checkNumber")) {
            paymentDetails.checkNumber(paymentDetailsMap.get("checkNumber"));
        }
        if (paymentDetailsMap.containsKey("routingCode")) {
            paymentDetails.routingCode(paymentDetailsMap.get("routingCode"));
        }
        if (paymentDetailsMap.containsKey("receiptNumber")) {
            paymentDetails.receiptNumber(paymentDetailsMap.get("receiptNumber"));
        }
        if (paymentDetailsMap.containsKey("bankNumber")) {
            paymentDetails.bankNumber(paymentDetailsMap.get("bankNumber"));
        }

        return paymentDetails;
    }

    @Then("Initiating a repayment on {string} with {double} transaction amount on Working Capital loan results an error with the following data:")
    public void initiateRepaymentResultsAnErrorWithDetails(final String transactionDate, final double transactionAmount,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount, null);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "repayment", repaymentRequest));

        if (table != null) {
            verifyRepaymentErrorWithTable(exception, table);
        }

        log.debug("Verified working capital loan repayment failed with expected error for loan {}", loanId);
    }

    @Then("Initiating a credit balance refund on {string} with {double} transaction amount on Working Capital loan results an error with the following data:")
    public void initiateCreditBalanceRefundResultsAnErrorWithDetails(final String transactionDate, final double transactionAmount,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));

        if (table != null) {
            verifyRepaymentErrorWithTable(exception, table);
        }
        log.debug("Verified working capital loan credit balance refund failed with expected error for loan {}", loanId);
    }

    private void verifyRepaymentErrorWithTable(final CallFailedRuntimeException exception, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String expectedHttpCode = data.get(1).get(0);
        final String expectedErrorMessage = data.get(1).get(1);

        log.debug("Checking for Http code: {} and error message: \"{}\"", expectedHttpCode, expectedErrorMessage);

        assertThat(exception.getStatus()).as("HTTP status code should be " + expectedHttpCode)
                .isEqualTo(Integer.parseInt(expectedHttpCode));
        assertThat(exception.getMessage()).as("Should contain error message").contains(expectedErrorMessage);
    }

    @Given("A code value {string} exists for code name {string}")
    public void ensureCodeValueExistsForCodeName(final String valueName, final String codeName) {
        final Long codeId = codeHelper.retrieveCodeByName(codeName).getId();
        final List<GetCodeValuesDataResponse> existing = ok(() -> fineractClient.codeValues().retrieveAllCodeValues(codeId));
        final Long codeValueId = existing.stream().filter(v -> valueName.equals(v.getName())).map(GetCodeValuesDataResponse::getId)
                .findFirst().orElseGet(
                        () -> codeHelper.createCodeValue(codeId, new PostCodeValuesDataRequest().name(valueName).isActive(true).position(0))
                                .getSubResourceId());
        testContext().set(cbrClassificationContextKey(codeName, valueName), codeValueId);
    }

    @When("Admin brings the working capital loan to {string}")
    public void bringWorkingCapitalLoanToStatus(final String status) {
        final Long loanId = getCreatedLoanId();
        final LoanStatus target = LoanStatus.valueOf(status);
        final PostWorkingCapitalLoansRequest createReq = testContext().get(TestContextKey.LOAN_CREATE_REQUEST);
        final String submissionDate = createReq.getSubmittedOnDate();
        switch (target) {
            case SUBMITTED_AND_PENDING_APPROVAL:
            break;
            case APPROVED:
                approveWCLoanOnDate(loanId, submissionDate, "9000");
            break;
            case ACTIVE:
                approveWCLoanOnDate(loanId, submissionDate, "9000");
                disburseWCLoanOnDate(loanId, submissionDate, "9000");
            break;
            case CLOSED_OBLIGATIONS_MET:
                approveWCLoanOnDate(loanId, submissionDate, "9000");
                disburseWCLoanOnDate(loanId, submissionDate, "9000");
                final String nextDay = nextDay(submissionDate);
                businessDateHelper.setBusinessDate(nextDay);
                makeWorkingCapitalLoanRepayment(nextDay, 9000.0);
            break;
            default:
                throw new IllegalArgumentException("Unsupported target status for bringWorkingCapitalLoanToStatus: " + status);
        }
    }

    private static String nextDay(final String date) {
        return FORMATTER.format(java.time.LocalDate.parse(date, FORMATTER).plusDays(1));
    }

    private void approveWCLoanOnDate(final Long loanId, final String approvalDate, final String amount) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest().approvedOnDate(approvalDate).expectedDisbursementDate(approvalDate)
                .approvedLoanAmount(new BigDecimal(amount));
        testContext().set(TestContextKey.LOAN_APPROVAL_REQUEST, approveRequest);
        executeStateTransition("approve", approveRequest, TestContextKey.LOAN_APPROVAL_RESPONSE, false);
    }

    private void disburseWCLoanOnDate(final Long loanId, final String actualDisbursementDate, final String amount) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest().actualDisbursementDate(actualDisbursementDate)
                .transactionAmount(new BigDecimal(amount));
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);
        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);
    }

    @When("Admin requests the Working Capital loan transaction template for command {string}")
    public void requestWorkingCapitalLoanTransactionTemplate(final String command) {
        final Long loanId = getCreatedLoanId();
        final WorkingCapitalLoanCommandTemplateData template = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanActionTemplate(loanId, command));
        testContext().set(WC_CBR_TEMPLATE_RESPONSE, template);
    }

    @Then("The Working Capital loan transaction template expectedAmount is {string}")
    public void assertWorkingCapitalLoanTransactionTemplateExpectedAmount(final String expected) {
        final WorkingCapitalLoanCommandTemplateData template = testContext().get(WC_CBR_TEMPLATE_RESPONSE);
        assertNotNull(template, "Template response should not be null");
        assertThat(template.getExpectedAmount()).as("Template expectedAmount").isNotNull();
        assertThat(template.getExpectedAmount().compareTo(new BigDecimal(expected)))
                .as("Template expectedAmount %s should equal %s", template.getExpectedAmount(), expected).isEqualTo(0);
    }

    @Then("Working Capital loan balance overpaymentAmount is {string}")
    public void assertWorkingCapitalLoanBalanceOverpaymentAmount(final String expected) {
        assertBalanceFieldEquals("overpaymentAmount", expected);
    }

    @Then("Working Capital loan balance principalOutstanding is {string}")
    public void assertWorkingCapitalLoanBalancePrincipalOutstanding(final String expected) {
        assertBalanceFieldEquals("principalOutstanding", expected);
    }

    @Then("Working Capital loan balance payload contains the following fields:")
    public void assertWorkingCapitalLoanBalancePayloadFields(final DataTable table) {
        final List<List<String>> rows = table.asLists();
        for (int i = 1; i < rows.size(); i++) {
            assertBalanceFieldEquals(rows.get(i).get(0), rows.get(i).get(1));
        }
    }

    private void assertBalanceFieldEquals(final String field, final String expected) {
        final GetBalance balance = retrieveLoanDetails(getCreatedLoanId()).getBalance();
        assertNotNull(balance, "Balance payload should not be null");
        final BigDecimal actual = switch (field) {
            case "overpaymentAmount" -> balance.getOverpaymentAmount();
            case "principalOutstanding" -> balance.getPrincipalOutstanding();
            case "totalPaidPrincipal" -> balance.getTotalPaidPrincipal();
            case "totalPayment" -> balance.getTotalPayment();
            case "realizedIncome" -> balance.getRealizedIncome();
            case "unrealizedIncome" -> balance.getUnrealizedIncome();
            default -> throw new IllegalArgumentException("Unknown balance field: " + field);
        };
        assertNotNull(actual, "Balance field " + field + " should not be null");
        assertThat(actual.compareTo(new BigDecimal(expected))).as("Balance field %s actual=%s, expected=%s", field, actual, expected)
                .isEqualTo(0);
    }

    @Then("Verify Working Capital loan credit balance refund transaction has classification {string}")
    public void verifyCreditBalanceRefundTransactionHasClassification(final String classificationName) {
        final GetWorkingCapitalLoanTransactionIdResponse cbr = latestCreditBalanceRefundTransaction();
        assertThat(cbr.getClassification()).as("CBR classification").isNotNull();
        assertThat(cbr.getClassification().getName()).as("CBR classification name").isEqualTo(classificationName);
    }

    @Then("Verify Working Capital loan credit balance refund transaction has type {string} and externalId {string}")
    public void verifyCreditBalanceRefundTransactionHasTypeAndExternalId(final String expectedType, final String expectedExternalId) {
        final String resolvedExternalId = resolveExternalIdSlot(expectedExternalId);
        final GetWorkingCapitalLoansLoanIdResponse loan = retrieveLoanDetails(getCreatedLoanId());
        final GetWorkingCapitalLoanTransactionIdResponse cbr = loan.getTransactions().stream()
                .filter(t -> resolvedExternalId.equals(t.getExternalId())).findFirst()
                .orElseThrow(() -> new IllegalStateException("No WC loan transaction with externalId " + resolvedExternalId));
        final String typeCode = cbr.getType() == null ? null : cbr.getType().getCode();
        assertThat(typeCode).as("CBR transaction type code")
                .isEqualTo("loanTransactionType." + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, expectedType));
        assertThat(cbr.getExternalId()).as("CBR externalId").isEqualTo(resolvedExternalId);
    }

    @Then("Working Capital credit balance refund transaction business event is raised with {string} amount and reversed {string}")
    public void workingCapitalCreditBalanceRefundTransactionEventRaised(final String amount, final String reversed) {
        eventCheckHelper.workingCapitalLoanCreditBalanceRefundTransactionEventCheck(getCreatedLoanId(), new BigDecimal(amount));
        assertThat(Boolean.parseBoolean(reversed)).isFalse();
    }

    @Then("No accounting journal entries are created for the Working Capital loan credit balance refund transaction")
    public void noJournalEntriesForCreditBalanceRefund() {
        final Integer before = testContext().get(WC_CBR_JOURNAL_ENTRIES_BEFORE);
        final Integer after = testContext().get(WC_CBR_JOURNAL_ENTRIES_AFTER);
        assertThat(before).as("Journal entries amount before CBR").isNotNull();
        assertThat(after).as("Journal entries amount after CBR").isNotNull();
        assertThat(after).as("CBR should not create accounting journal entries; before=%s, after=%s", before, after).isEqualTo(before);
    }

    private int countJournalEntriesForLoan(final Long loanId) {
        final JournalEntriesApi.RetrieveAllJournalEntriesQueryParams params = new JournalEntriesApi.RetrieveAllJournalEntriesQueryParams()
                .loanId(loanId).limit(-1);
        final GetJournalEntriesTransactionIdResponse response = ok(() -> fineractClient.journalEntries().retrieveAllJournalEntries(params));
        assertThat(response).as("Journal entries response").isNotNull();
        return response.getPageItems() == null ? 0 : response.getPageItems().size();
    }

    @Then("Customer makes credit balance refund on {string} with {double} transaction amount on Working Capital loan with valid classification {string}")
    public void makeCreditBalanceRefundWithValidClassification(final String transactionDate, final double transactionAmount,
            final String classificationName) {
        final Long classificationId = resolveCbrClassificationId(classificationName);
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).classificationId(classificationId);
        final PostWorkingCapitalLoanTransactionsResponse response = executeCreditBalanceRefundById(getCreatedLoanId(), cbrRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, getCreatedLoanId());
    }

    @Then("Customer makes credit balance refund on {string} with {double} transaction amount on Working Capital loan with note {string}")
    public void makeCreditBalanceRefundWithNote(final String transactionDate, final double transactionAmount, final String note) {
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).note(note);
        final PostWorkingCapitalLoanTransactionsResponse response = executeCreditBalanceRefundById(getCreatedLoanId(), cbrRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, getCreatedLoanId());
    }

    @Then("Customer makes credit balance refund on {string} with {double} transaction amount and externalId {string} on Working Capital loan")
    public void makeCreditBalanceRefundWithExternalId(final String transactionDate, final double transactionAmount,
            final String externalId) {
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).externalId(resolveExternalIdSlot(externalId));
        final PostWorkingCapitalLoanTransactionsResponse response = executeCreditBalanceRefundById(getCreatedLoanId(), cbrRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, getCreatedLoanId());
    }

    @Then("Initiating a credit balance refund on {string} with {double} transaction amount on Working Capital loan with classificationId {long} results an error with the following data:")
    public void initiateCreditBalanceRefundWithClassificationIdError(final String transactionDate, final double transactionAmount,
            final long classificationId, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).classificationId(classificationId);
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));
        if (table != null) {
            verifyRepaymentErrorWithTable(exception, table);
        }
    }

    @Then("Initiating a credit balance refund on {string} with {double} transaction amount and note of length {int} on Working Capital loan results an error with the following data:")
    public void initiateCreditBalanceRefundWithOverlongNoteError(final String transactionDate, final double transactionAmount,
            final int noteLength, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final String note = "a".repeat(noteLength);
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).note(note);
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));
        if (table != null) {
            verifyRepaymentErrorWithTable(exception, table);
        }
    }

    @Then("Initiating a credit balance refund on {string} with {double} transaction amount and externalId {string} on Working Capital loan results an error with the following data:")
    public void initiateCreditBalanceRefundWithExternalIdError(final String transactionDate, final double transactionAmount,
            final String externalId, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null).externalId(resolveExternalIdSlot(externalId));
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));
        if (table != null) {
            verifyRepaymentErrorWithTable(exception, table);
        }
    }

    private GetWorkingCapitalLoanTransactionIdResponse latestCreditBalanceRefundTransaction() {
        final GetWorkingCapitalLoansLoanIdResponse loan = retrieveLoanDetails(getCreatedLoanId());
        return loan.getTransactions().stream()
                .filter(t -> t.getType() != null && "loanTransactionType.creditBalanceRefund".equals(t.getType().getCode())
                        && !Boolean.TRUE.equals(t.getReversed()))
                .reduce((first, second) -> second).orElseThrow(() -> new IllegalStateException("No CBR transaction on loan"));
    }

    private Long resolveCbrClassificationId(final String classificationName) {
        final Object cached = testContext().get(cbrClassificationContextKey(WC_CBR_CLASSIFICATION_CODE_NAME, classificationName));
        if (cached instanceof Long id) {
            return id;
        }
        final Long codeId = codeHelper.retrieveCodeByName(WC_CBR_CLASSIFICATION_CODE_NAME).getId();
        return ok(() -> fineractClient.codeValues().retrieveAllCodeValues(codeId)).stream()
                .filter(v -> classificationName.equals(v.getName())).map(GetCodeValuesDataResponse::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("Classification value not seeded: " + classificationName));
    }

    private static String cbrClassificationContextKey(final String codeName, final String valueName) {
        return "codeValueId." + codeName + "." + valueName;
    }

    private String resolveExternalIdSlot(final String slotName) {
        final String key = "externalIdSlot." + slotName;
        final Object cached = testContext().get(key);
        if (cached instanceof String s) {
            return s;
        }
        final String resolved = slotName + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        testContext().set(key, resolved);
        return resolved;
    }

}
