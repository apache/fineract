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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandResponse;
import org.apache.fineract.client.models.GetBalance;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.GetDisbursementDetail;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanChargePaidByData;
import org.apache.fineract.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.LoanTransactionEnumData;
import org.apache.fineract.client.models.MarkWorkingCapitalLoanAsFraudRequest;
import org.apache.fineract.client.models.PostAllowAttributeOverrides;
import org.apache.fineract.client.models.PostClientsRequest;
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
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanCommandTemplateData;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;
import org.apache.fineract.test.data.FundId;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.data.codevalue.CodeNames;
import org.apache.fineract.test.data.paymenttype.DefaultPaymentType;
import org.apache.fineract.test.data.paymenttype.PaymentTypeResolver;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.factory.WorkingCapitalRequestFactory;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.CodeHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.helper.WorkingCapitalScheduleMatcher;
import org.apache.fineract.test.messaging.event.EventCheckHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.stepdef.common.JournalEntriesStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalLoanAccountStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final Long NON_EXISTENT_LOAN_ID = 999_999_999L;
    private static final String WC_DISBURSE_CLASSIFICATION_ID = "wcDisburseClassificationId";
    private static final String WC_DISBURSE_CLASSIFICATION_CODE_NAME = "working_capital_loan_disbursement_classification";
    private static final String WC_CBR_CLASSIFICATION_CODE_NAME = "working_capital_loan_credit_balance_refund_classification";
    private static final String WC_CBR_TEMPLATE_RESPONSE = "wcCbrTemplateResponse";
    private static final String WC_CBR_JOURNAL_ENTRIES_BEFORE = "wcCbrJournalEntriesBefore";
    private static final String WC_CBR_JOURNAL_ENTRIES_AFTER = "wcCbrJournalEntriesAfter";
    private static final String WC_LAST_TRANSACTION_TYPE = "wcLastTransactionType";
    private static final String WC_LAST_TRANSACTION_DATE = "wcLastTransactionDate";
    private static final String WC_LAST_TRANSACTION_AMOUNT = "wcLastTransactionAmount";

    private final FineractFeignClient fineractClient;
    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;
    private final WorkingCapitalRequestFactory workingCapitalProductRequestFactory;
    private final CodeHelper codeHelper;
    private final EventCheckHelper eventCheckHelper;
    private final PaymentTypeResolver paymentTypeResolver;
    private final BusinessDateHelper businessDateHelper;
    private final JournalEntriesStepDef journalEntriesStepDef;
    private final ClientRequestFactory clientRequestFactory;

    @Given("Admin creates a client with random data and creates-approves-disburses a working capital loan with the following data:")
    public void createClientAndDisburseWorkingCapitalLoanWithData(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        // Extract data needed for approval/disbursement
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principalAmount = loanData.get(3);

        // Create client with random data
        final PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();
        final PostClientsResponse clientResponse = ok(() -> fineractClient.clients().createClient(clientsRequest));
        Assertions.assertNotNull(clientResponse);
        Assertions.assertNotNull(clientResponse.getClientId());
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, clientResponse);

        // Create working capital loan using existing helper
        createWorkingCapitalLoanAccount(loanData);

        // Approve loan using existing helper method
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(submittedOnDate)//
                .approvedLoanAmount(new BigDecimal(principalAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);
        executeStateTransition("approve", approveRequest, TestContextKey.LOAN_APPROVAL_RESPONSE, false);

        // Disburse loan using existing helper method
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest()//
                .actualDisbursementDate(submittedOnDate)//
                .transactionAmount(new BigDecimal(principalAmount));
        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);

        // Verify loan is ACTIVE
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        Assertions.assertNotNull(loanDetails);
        Assertions.assertEquals(300, loanDetails.getStatus().getId(), "Loan should be ACTIVE");
    }

    @When("Admin creates a working capital loan with the following data:")
    public void createWorkingCapitalLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        createWorkingCapitalLoanAccount(data.get(1));
    }

    @When("Admin creates a working capital loan with fund and the following data:")
    public void createWorkingCapitalLoanWithFund(final DataTable table) {
        final List<String> loanData = table.asLists().get(1);
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanData.getFirst());
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData)
                .fundId(FundId.LENDER_A.value);
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);

        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        trackLoanIdIfEnabled(response.getLoanId());
        log.info("Working Capital Loan created with fund {}, Loan ID: {}", FundId.LENDER_A.value, response.getLoanId());
    }

    @When("Admin creates a working capital loan using created product with the following data:")
    public void createWorkingCapitalLoanUsingCreatedProduct(final DataTable table) {
        submitLoanUsingCreatedProduct(table, null, null);
    }

    @When("Admin creates a working capital loan using created product with breachGraceDays {int} and the following data:")
    public void createWorkingCapitalLoanUsingCreatedProductWithBreachGraceDays(final int breachGraceDays, final DataTable table) {
        submitLoanUsingCreatedProduct(table, breachGraceDays, null);
    }

    @When("Admin creates a working capital loan using created product with breachStartType {string} and the following data:")
    public void createWorkingCapitalLoanUsingCreatedProductWithBreachStartType(final String breachStartType, final DataTable table) {
        submitLoanUsingCreatedProduct(table, null, breachStartType);
    }

    private void submitLoanUsingCreatedProduct(final DataTable table, final Integer breachGraceDays, final String breachStartType) {
        final List<List<String>> data = table.asLists();
        final List<String> rawData = data.get(1);
        final Long clientId = extractClientId();
        final PostWorkingCapitalLoanProductsResponse productResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long loanProductId = productResponse.getResourceId();

        final String submittedOnDate = rawData.getFirst();
        final String expectedDisbursementDate = rawData.get(1);
        final String principal = rawData.get(2);
        final String totalPaymentVolume = rawData.get(3);
        final String periodPaymentRate = rawData.get(4);
        final String discount = rawData.get(5);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPaymentVolume(new BigDecimal(totalPaymentVolume))
                .periodPaymentRate(new BigDecimal(periodPaymentRate))
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);
        if (breachGraceDays != null) {
            loansRequest.breachGraceDays(breachGraceDays);
        }
        if (breachStartType != null) {
            loansRequest.breachStartType(breachStartType);
        }
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);

        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        log.info("Working Capital Loan created with dynamic product ID: {}, Loan ID: {}", loanProductId, response.getLoanId());
    }

    @Then("Working capital loan account has breachGraceDays {int}")
    public void verifyLoanBreachGraceDays(final int expectedBreachGraceDays) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        assertThat(response.getBreachGraceDays()).as("breachGraceDays").isEqualTo(expectedBreachGraceDays);
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

    @Then("Working capital loan details has the auto-generated fields present")
    public void verifyAutoGeneratedFieldsPresent() {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoansLoanIdResponse response = retrieveLoanDetails(loanId);

        assertThat(response.getId()).as("id").isNotNull();
        assertThat(response.getAccountNo()).as("accountNo").isNotNull();
        assertThat(response.getExternalId()).as("externalId").isNotNull();
        assertThat(response.getClientId()).as("clientId").isNotNull();
    }

    @Then("Working capital loan details has the following field values:")
    public void verifyWorkingCapitalLoanDetailFieldValues(final DataTable table) {
        final GetWorkingCapitalLoansLoanIdResponse response = retrieveLoanDetails(getCreatedLoanId());

        table.asMap().forEach((field, expected) -> {
            final String actual = resolveFieldValue(response, field);
            if ("present".equals(expected)) {
                assertThat(actual).as("WC loan details field %s", field).isNotEqualTo("null");
            } else {
                assertThat(actual).as("WC loan details field %s", field).isEqualTo(expected);
            }
        });
    }

    private String resolveFieldValue(final Object root, final String path) {
        Object current = root;
        for (final String segment : path.split("\\.", -1)) {
            if (current == null) {
                return "null";
            }
            if (current instanceof List<?> list) {
                if ("size".equals(segment)) {
                    return String.valueOf(list.size());
                }
                current = list.get(Integer.parseInt(segment));
                continue;
            }
            current = invokeGetter(current, segment);
        }
        return current instanceof BigDecimal amount ? new Utils.DoubleFormatter(amount.doubleValue()).format() : asString(current);
    }

    private static Object invokeGetter(final Object owner, final String property) {
        final Method getter = Arrays.stream(owner.getClass().getMethods())
                .filter(method -> method.getParameterCount() == 0 && method.getName().equalsIgnoreCase("get" + property)).findFirst()
                .orElseThrow(
                        () -> new IllegalStateException(String.format("No getter '%s' on %s", property, owner.getClass().getSimpleName())));
        try {
            return getter.invoke(owner);
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String asString(final Object value) {
        return value == null ? "null" : value.toString();
    }

    @Then("Creating a working capital loan with LP overridables disabled and with the following data will result an error:")
    public void creatingWorkingCapitalLoanWithLpOverridablesDisabledWillResultAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.getFirst();
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPaymentVolume = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);
        final String delinquencyBucketId = loanData.get(7);
        final String repaymentEvery = loanData.get(8);
        final String repaymentFrequencyType = loanData.get(9);

        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principal)).totalPaymentVolume(new BigDecimal(totalPaymentVolume))
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
        String errorMessage = "validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.less.than.or.equal.to.max";
        creatingAWorkingCapitalLoanWithInvalidDataResultAnError(table, errorMessage);
    }

    @Then("Creating a working capital loan with principal amount smaller than Working Capital Loan Product min will result an error:")
    public void creatingAWorkingCapitalLoanWithPrincipalAmountSmallerThanWorkingCapitalLoanProductMinWillResultAnError(
            final DataTable table) {
        String errorMessage = "validation.msg.WORKINGCAPITALLOAN.principalAmount.must.be.greater.than.or.equal.to.min";
        creatingAWorkingCapitalLoanWithInvalidDataResultAnError(table, errorMessage);
    }

    @Then("Creating a working capital loan with input values that cause unable to calculate a valid EIR will result into an error:")
    public void creatingAWorkingCapitalLoanWithInvalidInputValuesCauseUnableToCalculateEIrResultAnError(final DataTable table) {
        String errorMessage = ErrorMessageHelper.workingCapitalInputValuesCauseUnableCalculateEIrFailure();
        creatingAWorkingCapitalLoanWithInvalidDataResultAnError(table, errorMessage);
    }

    @Then("Creating a working capital loan using created product with input values that cause unable to calculate a valid EIR will result into an error:")
    public void creatingAWorkingCapitalLoanUsingLpWithInvalidInputValuesCauseUnableToCalculateEIrResultAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long clientId = extractClientId();
        final PostWorkingCapitalLoanProductsResponse productResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
        final Long loanProductId = productResponse.getResourceId();

        final String submittedOnDate = loanData.get(0);
        final String expectedDisbursementDate = loanData.get(1);
        final String principal = loanData.get(2);
        final String totalPaymentVolume = loanData.get(3);
        final String periodPaymentRate = loanData.get(4);
        final String discount = loanData.get(5);

        assert loanProductId != null;
        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)//
                .productId(loanProductId)//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principalAmount(new BigDecimal(principal))//
                .totalPaymentVolume(new BigDecimal(totalPaymentVolume))//
                .periodPaymentRate(new BigDecimal(periodPaymentRate))//
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);//

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);
        String errorMessage = ErrorMessageHelper.workingCapitalInputValuesCauseUnableCalculateEIrErrorCodeFailure();
        assertValidationError(exception, errorMessage);

        log.info("Verified working capital loan creation failed with principal amount exceeding max");
    }

    @Then("Creating a working capital loan with missing mandatory fields will result an error:")
    public void creatingAWorkingCapitalLoanWithMissingMandatoryFieldsWillResultAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.getFirst();
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPaymentVolume = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);

        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        final PostWorkingCapitalLoansRequest loansRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(loanProductId).submittedOnDate(submittedOnDate != null && !submittedOnDate.isEmpty() ? submittedOnDate : null)
                .expectedDisbursementDate(
                        expectedDisbursementDate != null && !expectedDisbursementDate.isEmpty() ? expectedDisbursementDate : null)
                .principalAmount(principal != null && !principal.isEmpty() ? new BigDecimal(principal) : null)
                .totalPaymentVolume(totalPaymentVolume != null && !totalPaymentVolume.isEmpty() ? new BigDecimal(totalPaymentVolume) : null)
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

        if (totalPaymentVolume == null || totalPaymentVolume.isEmpty()) {
            log.info("Checking for totalPaymentVolume error: The parameter `totalPaymentVolume` is mandatory.");
            assertThat(exception.getMessage()).as("Should contain totalPaymentVolume mandatory error")
                    .contains("The parameter `totalPaymentVolume` is mandatory.");
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

    @Then("Creating a working capital loan with near breachId {long} on {string} will result with error")
    public void createLoanWithInvalidNearBreachId(final long nearBreachId, final String submittedOnDate) {
        final Long breachId = createBreachAndGetId();
        final PostWorkingCapitalLoansRequest request = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate).breachId(breachId)
                .nearBreachId(nearBreachId);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        assertThat(exception.getDeveloperMessage())
                .contains(String.format("Working Capital Near Breach with id %s was not found.", nearBreachId));
        assertThat(exception.getStatus()).as("HTTP status").isEqualTo(404);
    }

    @Then("Admin creates working capital loan with breach override allowed with breach override and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreach(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long overrideBreachId = createBreachAndGetId();
        createWorkingCapitalLoanAccountWithBreachNearBreachData(loanData, overrideBreachId, null);
    }

    @Then("Admin creates working capital loan with breach override allowed with breach and near breach override and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreachAndNearBreachOverride(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long overrideBreachId = createBreachAndGetId();
        final Long overrideNearBreachId = createNearBreachAndGetId();
        createWorkingCapitalLoanAccountWithBreachNearBreachData(loanData, overrideBreachId, overrideNearBreachId);
    }

    @Then("Admin creates working capital loan with {int} {string} breach override and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreachOverrideData(int breachFrequency, String breachFrequencyType,
            final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long overrideBreachId = createBreachOverrideAndGetId(breachFrequency, breachFrequencyType);
        createWorkingCapitalLoanAccountWithBreachNearBreachData(loanData, overrideBreachId, null);
    }

    @Then("Admin creates working capital loan with {int} {string} breach and {int} {string} near breach override and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreachAndNearBreachOverrideData(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final Long overrideBreachId = createBreachOverrideAndGetId(breachFrequency, breachFrequencyType);
        final Long overrideNearBreachId = createNearBreachOverrideAndGetId(nearBreachFrequency, nearBreachFrequencyType);
        createWorkingCapitalLoanAccountWithBreachNearBreachData(data.get(1), overrideBreachId, overrideNearBreachId);
    }

    @Then("Admin creates working capital loan with breach override allowed with {int} {string} breach and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreachhData(final DataTable table, int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) {
        final List<List<String>> data = table.asLists();
        final Long overrideBreachId = createBreachOverrideAndGetId(breachFrequency, breachFrequencyType);
        final Long overrideNearBreachId = createNearBreachOverrideAndGetId(nearBreachFrequency, nearBreachFrequencyType);
        createWorkingCapitalLoanAccountWithBreachNearBreachData(data.get(1), overrideBreachId, overrideNearBreachId);
    }

    @Then("Admin creates working capital loan with breach override allowed with {int} {string} breach and {int} {string} near breach and the following data:")
    public void createLoanWithBreachOverrideAllowedWithBreachAndNearBreachData(int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType, final DataTable table) {
        final List<List<String>> data = table.asLists();
        final Long overrideBreachId = createBreachAndGetId(breachFrequency, breachFrequencyType);
        final Long overrideNearBreachId = createNearBreachAndGetId(nearBreachFrequency, nearBreachFrequencyType);
        createWorkingCapitalLoanAccountWithBreachNearBreachData(data.get(1), overrideBreachId, overrideNearBreachId);
    }

    @Then("Admin creates working capital loan with breach from WCLP while override is allowed and the following data:")
    public void createLoanWithBreachFromWCLPOverrideAllowedData(DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.getFirst();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final Long breachIdFromWCLP = getBreachIdFromWCLP(loanProductId);
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachIdFromWCLP);

        createWorkingCapitalLoanAccountWithBreachNearBreachData(loanData, breachIdFromWCLP, null);
    }

    @Then("Admin creates working capital loan with breach and near breach from WCLP while override is allowed and the following data:")
    public void createLoanWithBreachNearBreachFromWCLPOverrideAllowedData(DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final String loanProduct = loanData.getFirst();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        final Long breachIdFromWCLP = getBreachIdFromWCLP(loanProductId);
        final Long nearBreachIdFromWCLP = getNearBreachIdFromWCLP(loanProductId);
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachIdFromWCLP);
        testContext().set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID, nearBreachIdFromWCLP);

        createWorkingCapitalLoanAccountWithBreachNearBreachData(loanData, breachIdFromWCLP, nearBreachIdFromWCLP);
    }

    @Then("Admin creates working capital loan with with breach and near breach on {string} date")
    public void createLoanWithBreachOverrideAllowedWithBreachAndNearBreachData(String submittedOnDate) {
        final Long breachId = createBreachAndGetId();
        final Long nearBreachId = createNearBreachAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate)
                .breachId(breachId).nearBreachId(nearBreachId);
        createWorkingCapitalLoanAccount(loansRequest);
    }

    @Then("Verify working capital loan account has been created with correct breach data")
    public void checkCreateWCLoanAccountBreachData() {
        final Long breachId = testContext().get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        checkCreateWCLoanAccountBreachData(breachId);
    }

    @Then("Verify working capital loan account has been created with correct breach data inherited from WCLP level")
    public void checkCreateWCLoanAccountBreachDataFromWCLP() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanProductResponse = fineractClient.workingCapitalLoans()
                .retrieveWorkingCapitalLoanById(loanId);

        Assertions.assertNotNull(loanProductResponse.getLoanProductId());
        final Long loanProductId = loanProductResponse.getLoanProductId();
        final Long breachIdFromWCLP = getBreachIdFromWCLP(loanProductId);

        checkCreateWCLoanAccountBreachData(breachIdFromWCLP);
    }

    @Then("Verify working capital loan account has been created with correct breach override data")
    public void checkCreateWCLoanAccountBreachOverrideData() {
        final Long breachIdFromWCLP = testContext().get(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE);
        checkCreateWCLoanAccountBreachData(breachIdFromWCLP);
    }

    @Then("Verify working capital loan account has been created with correct breach and near breach data")
    public void checkCreateWCLoanAccountBreachAndNearBreachData() {
        final Long breachIdFromWCLP = testContext().get(TestContextKey.WORKING_CAPITAL_BREACH_ID);
        final Long nearBreachIdFromWCLP = testContext().get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID);
        checkCreateWCLoanAccountBreachNearBreachData(breachIdFromWCLP, nearBreachIdFromWCLP);
    }

    @Then("Verify working capital loan account has been created with correct breach and near breach override data")
    public void checkCreateWCLoanAccountBreachAndNearBreachOverrideData() {
        final Long breachIdFromWCLP = testContext().get(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE);
        final Long nearBreachIdFromWCLP = testContext().get(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_OVERRIDE);
        checkCreateWCLoanAccountBreachNearBreachData(breachIdFromWCLP, nearBreachIdFromWCLP);
    }

    @Then("Verify working capital loan account has been created with correct breach and near breach data inherited from WCLP level")
    public void checkCreateWCLoanAccountBreachNearBreachDataFromWCLP() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanProductResponse = fineractClient.workingCapitalLoans()
                .retrieveWorkingCapitalLoanById(loanId);

        final Long loanProductId = loanProductResponse.getLoanProductId();
        final Long breachIdFromWCLP = getBreachIdFromWCLP(loanProductId);
        final Long nearBreachIdFromWCLP = getNearBreachIdFromWCLP(loanProductId);

        checkCreateWCLoanAccountBreachNearBreachData(breachIdFromWCLP, nearBreachIdFromWCLP);
    }

    @Then("Verify working capital loan account has been created with none breach nor near breach data")
    public void checkCreateWCLoanAccountNoneBreachNearBreachData() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse loanAccountResponse = retrieveLoanDetails(loanId);
        assertThat(loanAccountResponse.getBreach()).isNull();
        assertThat(loanAccountResponse.getNearBreach()).isNull();
    }

    @Then("Admin failed to create working capital loan while breach override disallowed with breach override and the following data:")
    public void createLoanWithBreachOverrideDisallowedWithBreachFailure(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long overrideBreachId = createBreachOverrideAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountWithBreachNearBreachRequest(loanData,
                overrideBreachId, null);
        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @Then("Admin failed to create working capital loan while breach override disallowed with breach override and default following data:")
    public void createLoanWithBreachOverrideDisallowedWithBreachDefaultFailure(final DataTable table) {
        final List<String> loanData = table.asLists().get(1);
        final String loanProduct = loanData.getFirst();
        final String submittedOnDate = loanData.get(1);

        final Long overrideBreachId = createBreachOverrideAndGetId();
        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(loanProduct, submittedOnDate)
                .breachId(overrideBreachId);
        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @Then("Admin failed to create working capital loan while breach override disallowed with breach and near breach override and the following data:")
    public void createLoanWithBreachOverrideDisallowedWithBreachAndNearBreachFailure(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final Long overrideBreachId = createBreachOverrideAndGetId();
        final Long overrideNearBreachId = createNearBreachOverrideAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountWithBreachNearBreachRequest(loanData,
                overrideBreachId, overrideNearBreachId);
        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @Then("Admin failed to create working capital loan while breach override disallowed with breach and near breach override and default following data:")
    public void createLoanWithBreachOverrideDisallowedWithBreachAndNearBreachDefaultFailure(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final String loanProduct = loanData.getFirst();
        final String submittedOnDate = loanData.get(1);

        final Long overrideBreachId = createBreachOverrideAndGetId();
        final Long overrideNearBreachId = createNearBreachOverrideAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(loanProduct, submittedOnDate)
                .breachId(overrideBreachId).nearBreachId(overrideNearBreachId);

        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @Then("Admin failed to create WC loan account on {string} with breach {int} {string} frequency lower then near breach {int} {string} frequency")
    public void createLoanWithBreachLowerThenNearBreachFailure(String submittedOnDate, int breachFrequency, String breachFrequencyType,
            int nearBreachFrequency, String nearBreachFrequencyType) {
        final Long breachId = createBreachAndGetId(breachFrequency, breachFrequencyType);
        final Long nearBreachId = createNearBreachAndGetId(nearBreachFrequency, nearBreachFrequencyType);

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate)
                .breachId(breachId).nearBreachId(nearBreachId);
        String message = ErrorMessageHelper.nearBreachMustBeLowerThenBreachFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @Then("Admin failed to create WC loan account on {string} without breach, but with near breach")
    public void createLoanWithoutBreachButWithNearBreachFailure(String submittedOnDate) {
        final Long nearBreachId = createNearBreachAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate)
                .nearBreachId(nearBreachId);
        String message = ErrorMessageHelper.nearBreachCannotEnableWithoutBreachFailure();
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, message);
    }

    @When("Admin failed to create Working Capital on {string} with period payment rate {string} value and outcomes with {} error message")
    public void adminAddWorkingCapitalPeriodPaymentRateInvalidDataFailure(String submittedOnDate, final String periodPaymentRate,
            final String errorMessage) {
        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate)
                .periodPaymentRate(new BigDecimal(periodPaymentRate));
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, errorMessage);
    }

    @When("Admin failed to create Working Capital with period payment rate {string} value and outcomes with {} error message with default following data:")
    public void createWorkingCapitalWithPeriodPaymentRateInvalidDataFailure(final String periodPaymentRate, final String errorMessage,
            final DataTable table) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);
        final String loanProduct = loanData.getFirst();
        final String submittedOnDate = loanData.get(1);

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(loanProduct, submittedOnDate)
                .periodPaymentRate(new BigDecimal(periodPaymentRate));
        verifyCreateWorkingCapitalLoanAccountFailure(loansRequest, 400, errorMessage);
    }

    @When("Admin modifies the working capital loan with the following data:")
    public void modifyWorkingCapitalLoan(final DataTable table) {
        final List<List<String>> data = table.asLists();
        modifyWorkingCapitalLoanAccount(data.get(1));
    }

    @When("Admin modifies the working capital loan with {int} {string} breach override data")
    public void modifyWorkingCapitalLoanWithBreachData(int breachFrequency, String breachFrequencyType) {
        final Long breachId = createBreachAndGetId(breachFrequency, breachFrequencyType);
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .breachId(breachId);
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE, breachId);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified with breach with ID: {}", response.getResourceId());
    }

    @When("Admin modifies the working capital loan with {int} {string} breach and {int} {string} near breach override data")
    public void modifyWorkingCapitalLoanWithBreachNearBreachData(int breachFrequency, String breachFrequencyType, int nearBreachFrequency,
            String nearBreachFrequencyType) {
        final Long breachId = createBreachAndGetId(breachFrequency, breachFrequencyType);
        final Long nearBreachId = createNearBreachAndGetId(nearBreachFrequency, nearBreachFrequencyType);
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .breachId(breachId).nearBreachId(nearBreachId);
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE, breachId);
        testContext().set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_OVERRIDE, nearBreachId);

        final PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, response);
        log.info("Working Capital Loan modified with breach and near breach with ID: {}", response.getResourceId());
    }

    @When("Admin modifies the working capital loan by externalId with the following data:")
    public void modifyWorkingCapitalLoanByExternalId(final DataTable table) {
        final List<List<String>> data = table.asLists();
        modifyWorkingCapitalLoanAccountByExternalId(data.get(1));
    }

    @Then("Changing submittedOnDate after expectedDisbursementDate results an error:")
    public void changingSubmittedOnDateAfterExpectedDisbursementDateResultsAnError(final DataTable table) {
        final List<List<String>> data = table.asLists();
        final String submittedOnDate = data.get(1).getFirst();

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
        final String submittedOnDate = data.get(1).getFirst();
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

    @Then("Admin failed to modify working capital loan while breach override disallowed with breach override")
    public void modifyLoanWithBreachOverrideDisallowedWithBreachDefaultFailure() {
        final Long overrideBreachId = createBreachOverrideAndGetId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .breachId(overrideBreachId); //

        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, message);
    }

    @Then("Admin failed to modify working capital loan while breach override disallowed with breach and near breach override")
    public void modifyLoanWithBreachOverrideDisallowedWithBreachAndNearBreachDefaultFailure() {
        final Long overrideBreachId = createBreachOverrideAndGetId();
        final Long overrideNearBreachId = createNearBreachOverrideAndGetId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .breachId(overrideBreachId)//
                .nearBreachId(overrideNearBreachId);//

        String message = ErrorMessageHelper.overrideDisallowedByProductFailure();
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, message);
    }

    @Then("Admin failed to modify WC loan account with breach {int} {string} frequency lower then near breach {int} {string} frequency")
    public void modifyLoanWithBreachLowerThenNearBreachFailure(int breachFrequency, String breachFrequencyType, int nearBreachFrequency,
            String nearBreachFrequencyType) {
        final Long breachId = createBreachAndGetId(breachFrequency, breachFrequencyType);
        final Long nearBreachId = createNearBreachAndGetId(nearBreachFrequency, nearBreachFrequencyType);

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .breachId(breachId)//
                .nearBreachId(nearBreachId);//
        String message = ErrorMessageHelper.nearBreachMustBeLowerThenBreachFailure();
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, message);
    }

    @Then("Admin failed to modify WC loan account without breach, but with near breach")
    public void modifyLoanWithoutBreachButWithNearBreachFailure() {
        final Long nearBreachId = createNearBreachAndGetId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .nearBreachId(nearBreachId);//
        String message = ErrorMessageHelper.nearBreachCannotEnableWithoutBreachFailure();
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, message);
    }

    @Then("Modify a working capital loan with breachId {long} will result with {int} and {} error message")
    public void modifyLoanWithInvalidBreachId(final long breachId, int statusCode, String errorMessage) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .breachId(breachId); //
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, statusCode, errorMessage);
    }

    @Then("Modify a working capital loan with near breachId {long} will result with an error")
    public void modifyLoanWithInvalidNearBreachId(final long nearBreachId) {
        final Long breachId = createBreachAndGetId();

        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .breachId(breachId) //
                .nearBreachId(nearBreachId); //
        String errorMessage = ErrorMessageHelper.nearBreachIdNotFoundFailure(nearBreachId);
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 404, errorMessage);
    }

    @Then("Admin failed to modify WC loan account with period payment rate {string} value and outcomes with {} error message")
    public void modifyLoanWithInvalidPeriodPaymentRateFailure(String periodPaymentRate, String errorMessage) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .periodPaymentRate(new BigDecimal(periodPaymentRate));//
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, errorMessage);
    }

    @Then("Admin failed to modify working capital loan with total payment value {string} that cause unable to calculate EIR")
    public void modifyLoanWithTotalPaymentValueCauseUnableCalculateEIrFailure(String totalPaymentValue) {
        final PutWorkingCapitalLoansLoanIdRequest modifyRequest = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest() //
                .totalPaymentVolume(new BigDecimal(totalPaymentValue)); //

        String errorMessage = ErrorMessageHelper.workingCapitalInputValuesCauseUnableCalculateEIrErrorCodeFailure();
        verifyModifyWorkingCapitalLoanAccountFailure(modifyRequest, 400, errorMessage);
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
        final String submittedOnDate = data.get(1).getFirst();
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
        final Long loanId = loanResponse.getLoanId();

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

    @Then("Approving the working capital loan on {string} with {string} amount and expected disbursement date on {string} with {string} discount amount results an error with the following data:")
    public void approveWorkingCapitalLoanWithDiscountResultsAnError(final String approveDate, final String approvedAmount,
            final String expectedDisbursementDate, final String discountAmount, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest()//
                .approvedOnDate(approveDate)//
                .approvedLoanAmount(new BigDecimal(approvedAmount))//
                .discountAmount(new BigDecimal(discountAmount))//
                .expectedDisbursementDate(expectedDisbursementDate);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(getCreatedLoanId(), "approve", approveRequest));

        verifyErrorResponse(exception, table);
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
        final Long loanId = loanResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));

        testContext().set(TestContextKey.LOAN_RESPONSE, loanDetailsResponse);
        Assertions.assertNotNull(loanDetailsResponse.getStatus());
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
        final Long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        String getLoanStatus = loanDetailsResponse.getStatus().getValue();
        assertThat(getLoanStatus.toUpperCase(Locale.ROOT)).isEqualTo(ACTIVE.name());

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

    @When("Admin successfully disburse the Working Capital loan on {string} with {string} EUR transaction amount and {string} discount amount and a random discountExternalId")
    public void disburseWCLoanWithDiscountAndRandomDiscountExternalId(final String actualDisbursementDate, final String transactionAmount,
            final String discountAmount) {
        final String randomDiscountExternalId = Utils.randomStringGenerator("TestDiscountExtId_", 10);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_DISBURSE_DISCOUNT_EXTERNAL_ID_USER_GENERATED, randomDiscountExternalId);

        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .transactionAmount(new BigDecimal(transactionAmount)) //
                .discountAmount(new BigDecimal(discountAmount)) //
                .discountExternalId(randomDiscountExternalId);
        testContext().set(TestContextKey.LOAN_DISBURSE_REQUEST, disburseRequest);

        executeStateTransition("disburse", disburseRequest, TestContextKey.LOAN_DISBURSE_RESPONSE, false);
        verifyStateTransitionSuccess(TestContextKey.LOAN_DISBURSE_RESPONSE, "disbursement");
        checkChangesExpectedStatus(TestContextKey.LOAN_DISBURSE_RESPONSE, ACTIVE);
    }

    @Then("Initiating disbursement on {string} with {string} EUR transaction amount and {string} discount amount reusing the previously shared discountExternalId on Working Capital loan results an error with the following data:")
    public void initiateDisbursementReusingSharedDiscountExternalIdResultsAnError(final String actualDisbursementDate,
            final String transactionAmount, final String discountAmount, final DataTable table) {
        final String sharedDiscountExternalId = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISBURSE_DISCOUNT_EXTERNAL_ID_USER_GENERATED);
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .transactionAmount(new BigDecimal(transactionAmount)) //
                .discountAmount(new BigDecimal(discountAmount)) //
                .discountExternalId(sharedDiscountExternalId);
        executeDisburseAndExpectError(disburseRequest, table);
    }

    @Then("Initiating disbursement on {string} with {string} EUR transaction amount and {string} discount amount and discountExternalId {string} on Working Capital loan results an error with the following data:")
    public void initiateDisbursementWithExplicitDiscountExternalIdResultsAnError(final String actualDisbursementDate,
            final String transactionAmount, final String discountAmount, final String discountExternalId, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .transactionAmount(new BigDecimal(transactionAmount)) //
                .discountAmount(new BigDecimal(discountAmount)) //
                .discountExternalId(discountExternalId);
        executeDisburseAndExpectError(disburseRequest, table);
    }

    @Then("Initiating disbursement on {string} with {string} EUR transaction amount and discountExternalId {string} without discountAmount on Working Capital loan results an error with the following data:")
    public void initiateDisbursementWithDiscountExternalIdAndNoDiscountAmountResultsAnError(final String actualDisbursementDate,
            final String transactionAmount, final String discountExternalId, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .transactionAmount(new BigDecimal(transactionAmount)) //
                .discountExternalId(discountExternalId);
        executeDisburseAndExpectError(disburseRequest, table);
    }

    @Then("Initiating disbursement on {string} with {string} EUR transaction amount and {string} discount amount using {string} for both externalId and discountExternalId on Working Capital loan results an error with the following data:")
    public void initiateDisbursementWithSameExternalIdForBothResultsAnError(final String actualDisbursementDate,
            final String transactionAmount, final String discountAmount, final String sharedExternalId, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .transactionAmount(new BigDecimal(transactionAmount)) //
                .discountAmount(new BigDecimal(discountAmount)) //
                .externalId(sharedExternalId) //
                .discountExternalId(sharedExternalId);
        executeDisburseAndExpectError(disburseRequest, table);
    }

    @Then("Disbursing the working capital loan on {string} with {string} amount and {string} discount amount results an error with the following data:")
    public void disburseWorkingCapitalLoanWithDiscountResultsAnError(final String actualDisbursementDate, final String transactionAmount,
            final String discountAmount, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest() //
                .actualDisbursementDate(actualDisbursementDate) //
                .discountAmount(new BigDecimal(discountAmount)) //
                .transactionAmount(new BigDecimal(transactionAmount));
        executeDisburseAndExpectError(disburseRequest, table);
    }

    private void executeDisburseAndExpectError(final PostWorkingCapitalLoansLoanIdRequest disburseRequest, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoans()
                .stateTransitionWorkingCapitalLoanById(loanId, disburseRequest, Map.of("command", "disburse")));
        verifyErrorResponse(exception, table);
    }

    @Then("Verify Working Capital loan disbursement was successful")
    public void checkDisbursementData() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        String getLoanStatus = loanDetailsResponse.getStatus().getValue();
        assertThat(getLoanStatus.toUpperCase(Locale.ROOT)).isEqualTo(ACTIVE.name());

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

        final GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        final GetWorkingCapitalLoanTransactionIdResponse disbursementTransaction = loanTransactionsResponse.getContent().stream()
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

    @Then("WorkingCapitalLoanDiscountFeeTransactionBusinessEvent is raised with amount {string} on {string} date")
    public void checkWorkingCapitalDiscountFeeBusinessEventIsRaised(final String amount, String transactionDate) {
        String resolvedTransactionType = TransactionType.DISCOUNT_FEE.getValue();
        eventCheckHelper.workingCapitalLoanDiscountFeeTransactionEventCheck(getCreatedLoanId(), resolvedTransactionType,
                new BigDecimal(amount), transactionDate);
    }

    @Then("WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent is raised with amount {string} on {string} date")
    public void checkWorkingCapitalDiscountFeeAdjustmentBusinessEventIsRaised(final String amount, String transactionDate) {
        String resolvedTransactionType = TransactionType.DISCOUNT_FEE_ADJUSTMENT.getValue();
        eventCheckHelper.workingCapitalLoanDiscountFeeAdjustmentTransactionEventCheck(getCreatedLoanId(), resolvedTransactionType,
                new BigDecimal(amount), transactionDate);
    }

    @Then("a Working Capital Loan Created business event is raised")
    public void aWorkingCapitalLoanCreatedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanCreatedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Application Modified business event is raised")
    public void aWorkingCapitalLoanApplicationModifiedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanApplicationModifiedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Approved business event is raised")
    public void aWorkingCapitalLoanApprovedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanApprovedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Undo Approval business event is raised")
    public void aWorkingCapitalLoanUndoApprovalBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanUndoApprovalEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Rejected business event is raised")
    public void aWorkingCapitalLoanRejectedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanRejectedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Disbursal business event is raised")
    public void aWorkingCapitalLoanDisbursalBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanDisbursalEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Undo Disbursal business event is raised")
    public void aWorkingCapitalLoanUndoDisbursalBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanUndoDisbursalEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Status Changed business event is raised")
    public void aWorkingCapitalLoanStatusChangedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanStatusChangedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Balance Changed business event is raised")
    public void aWorkingCapitalLoanBalanceChangedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Balance Changed business event is raised with charges:")
    public void aWorkingCapitalLoanBalanceChangedBusinessEventIsRaisedWithCharges(final DataTable table) {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventChargesCheck(getCreatedLoanId(), table.asMaps());
    }

    @Then("a Working Capital Loan Delinquency Range Change business event is raised")
    public void aWorkingCapitalLoanDelinquencyRangeChangeBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanDelinquencyRangeChangeEventCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Delinquency Range Change business event is raised naming the delinquency range")
    public void aWorkingCapitalLoanDelinquencyRangeChangeBusinessEventIsRaisedNamingTheDelinquencyRange() {
        eventCheckHelper.workingCapitalLoanDelinquencyRangeChangeEventNamesRangeCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Balance Changed business event is raised with transaction type totals:")
    public void aWorkingCapitalLoanBalanceChangedBusinessEventIsRaisedWithTransactionTypeTotals(final DataTable table) {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventSummaryTotalsCheck(getCreatedLoanId(), table.asMaps().get(0));
    }

    @Then("a Working Capital Loan Balance Changed business event is raised where charge accrual fields are not populated")
    public void aWorkingCapitalLoanBalanceChangedBusinessEventIsRaisedWithoutChargeAccrualFields() {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventChargesWithoutAccrualCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Disbursal business event is raised with loan details matching the API")
    public void aWorkingCapitalLoanDisbursalBusinessEventIsRaisedWithLoanDetailsMatchingTheApi() {
        eventCheckHelper.workingCapitalLoanDisbursalEventDeepPayloadCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Status Changed business event is raised with timeline matching the API")
    public void aWorkingCapitalLoanStatusChangedBusinessEventIsRaisedWithTimelineMatchingTheApi() {
        eventCheckHelper.workingCapitalLoanStatusChangedEventTimelineCheck(getCreatedLoanId());
    }

    @Then("a Working Capital Loan Balance Changed business event is raised with the delinquency pause periods")
    public void aWorkingCapitalLoanBalanceChangedBusinessEventIsRaisedWithDelinquencyPausePeriods() {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventPausePeriodsCheck(getCreatedLoanId());
    }

    @Then("no Working Capital Loan Status Changed business event is raised")
    public void noWorkingCapitalLoanStatusChangedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanStatusChangedEventNotRaisedCheck(getCreatedLoanId());
    }

    @Then("no Working Capital Loan Balance Changed business event is raised")
    public void noWorkingCapitalLoanBalanceChangedBusinessEventIsRaised() {
        eventCheckHelper.workingCapitalLoanBalanceChangedEventNotRaisedCheck(getCreatedLoanId());
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

    @And("Admin adds Discount fee with {string} amount on Working Capital loan account for last disbursement")
    public void addDiscountFeeWCLoanDisbursement(String discountAmount) {
        PostWorkingCapitalLoansLoanIdResponse lastDisbursementResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);

        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDisbursementResponse.getResourceId())
                .transactionAmount(new BigDecimal(discountAmount));

        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(getCreatedLoanId(), "discountFee", request));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE, response);
    }

    @When("Admin adds Discount fee with {string} amount and a random externalId on Working Capital loan account for last disbursement")
    public void addDiscountFeeWCLoanDisbursementWithRandomExternalId(final String discountAmount) {
        final PostWorkingCapitalLoansLoanIdResponse lastDisbursementResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);
        final String randomExternalId = Utils.randomStringGenerator("TestDiscountFeeExtId_", 10);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_EXTERNAL_ID_USER_GENERATED, randomExternalId);

        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDisbursementResponse.getResourceId())
                .transactionAmount(new BigDecimal(discountAmount)).externalId(randomExternalId);

        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(getCreatedLoanId(), "discountFee", request));
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE, response);
    }

    @Then("Adding Discount fee with {string} amount reusing the previously shared externalId on Working Capital loan account for last disbursement results an error with the following data:")
    public void addDiscountFeeReusingSharedExternalIdResultsAnError(final String discountAmount, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdResponse lastDisbursementResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);
        final String sharedExternalId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_EXTERNAL_ID_USER_GENERATED);

        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDisbursementResponse.getResourceId())
                .transactionAmount(new BigDecimal(discountAmount)).externalId(sharedExternalId);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(getCreatedLoanId(), "discountFee", request));
        verifyErrorResponse(exception, table);
    }

    @Then("Active Discount Fee transactions contain the user-generated externalId from DISCOUNTFEE")
    public void activeDiscountFeeTransactionsContainUserGeneratedExternalIdFromDiscountFee() {
        assertDiscountFeeContainsExpectedExternalId(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_EXTERNAL_ID_USER_GENERATED);
    }

    @Then("Active Discount Fee transactions contain the user-generated discountExternalId from disburse")
    public void activeDiscountFeeTransactionsContainUserGeneratedDiscountExternalIdFromDisburse() {
        assertDiscountFeeContainsExpectedExternalId(TestContextKey.WORKING_CAPITAL_LOAN_DISBURSE_DISCOUNT_EXTERNAL_ID_USER_GENERATED);
    }

    private void assertDiscountFeeContainsExpectedExternalId(final String testContextKey) {
        final String expectedExternalId = testContext().get(testContextKey);
        assertThat(expectedExternalId) //
                .as("Expected externalId must be set in test context under key `%s` by a prior setup step", testContextKey) //
                .isNotBlank();
        final List<GetWorkingCapitalLoanTransactionIdResponse> activeDiscounts = fetchActiveDiscountFeeTransactions();
        assertActiveDiscountFeeTransactionsNotEmpty(activeDiscounts);
        assertThat(activeDiscounts) //
                .as("Active Discount Fee transactions must contain one with the expected externalId %s", expectedExternalId) //
                .extracting(GetWorkingCapitalLoanTransactionIdResponse::getExternalId) //
                .contains(expectedExternalId);
    }

    @Then("All active Discount Fee transactions have an auto-generated externalId")
    public void allActiveDiscountFeeTransactionsHaveAutoGeneratedExternalId() {
        final List<GetWorkingCapitalLoanTransactionIdResponse> activeDiscounts = fetchActiveDiscountFeeTransactions();
        assertActiveDiscountFeeTransactionsNotEmpty(activeDiscounts);
        assertThat(activeDiscounts) //
                .as("Every active Discount Fee transaction must have a non-blank (auto-generated) externalId") //
                .allSatisfy(txn -> assertThat(txn.getExternalId()).isNotBlank());
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> fetchActiveDiscountFeeTransactions() {
        final GetWorkingCapitalLoanTransactionsResponse loanResponse = retrieveLoanTransactions(getCreatedLoanId());
        final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = loanResponse.getContent();
        if (transactions == null) {
            return List.of();
        }
        return transactions.stream() //
                .filter(t -> t.getType() != null && "loanTransactionType.discountFee".equals(t.getType().getCode())) //
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())) //
                .toList();
    }

    private void assertActiveDiscountFeeTransactionsNotEmpty(final List<GetWorkingCapitalLoanTransactionIdResponse> activeDiscounts) {
        assertThat(activeDiscounts).as("At least one active Discount Fee transaction must exist").isNotEmpty();
    }

    @Then("Adding Discount fee with {string} amount on Working Capital loan account results an error with the following data:")
    public void addingDiscountFeeWCLoanResultsAnError(final String discountAmount, final DataTable table) {
        final PostWorkingCapitalLoansLoanIdResponse lastDisbursementResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);
        Assertions.assertNotNull(lastDisbursementResponse);

        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDisbursementResponse.getResourceId())
                .transactionAmount(new BigDecimal(discountAmount));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(getCreatedLoanId(), "discountFee", request));
        verifyErrorResponse(exception, table);
    }

    @And("Admin adds Discount fee adjustment with {string} amount on Working Capital loan account for last discount")
    public void addDiscountFeeAdjustmentWCLoan(final String adjustmentAmount) {
        final PostWorkingCapitalLoanTransactionsResponse lastDiscountResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE);
        Assertions.assertNotNull(lastDiscountResponse);
        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDiscountResponse.getResourceId())
                .transactionAmount(new BigDecimal(adjustmentAmount));
        executeDiscountFeeAdjustmentById(getCreatedLoanId(), request);
    }

    @And("Admin adds Discount fee adjustment with {string} amount on transaction date {string} on Working Capital loan account for last discount")
    public void addDiscountFeeAdjustmentWCLoanWithTransactionDate(final String adjustmentAmount, final String transactionDate) {
        final PostWorkingCapitalLoanTransactionsResponse lastDiscountResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE);
        Assertions.assertNotNull(lastDiscountResponse);
        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDiscountResponse.getResourceId())
                .transactionAmount(new BigDecimal(adjustmentAmount)).transactionDate(transactionDate);
        executeDiscountFeeAdjustmentById(getCreatedLoanId(), request);
    }

    @And("Admin adds Discount fee adjustment with {string} amount on transaction date {string} on Working Capital loan account for last discount and {string} classification")
    public void addDiscountFeeAdjustmentWCLoanWithTransactionDate(final String adjustmentAmount, final String transactionDate,
            String classificationCodeValueName) {
        final PostWorkingCapitalLoanTransactionsResponse lastDiscountResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE);
        Assertions.assertNotNull(lastDiscountResponse);

        final Long classificationId = getClassificationCodeValueId(CodeNames.WORKING_CAPITAL_DISCOUNT_FEE_CLASSIFICATION.getValue(),
                classificationCodeValueName);

        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDiscountResponse.getResourceId())
                .transactionAmount(new BigDecimal(adjustmentAmount)).transactionDate(transactionDate).classificationId(classificationId);
        executeDiscountFeeAdjustmentById(getCreatedLoanId(), request);
    }

    @And("Admin loads discount fee transaction from Working Capital loan for adjustment")
    public void loadDiscountFeeTransactionFromLoanForAdjustment() {
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(getCreatedLoanId()));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }
        final GetWorkingCapitalLoanTransactionIdResponse discountTxn = body.getContent().stream()
                .filter(t -> t.getType() != null && "loanTransactionType.discountFee".equals(t.getType().getCode()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("Active discount fee transaction not found on loan"));
        final PostWorkingCapitalLoanTransactionsResponse synthetic = new PostWorkingCapitalLoanTransactionsResponse()
                .resourceId(discountTxn.getId());
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE, synthetic);
    }

    @When("Admin undo the last Discount fee adjustment on Working Capital loan account")
    public void undoLastDiscountFeeAdjustmentWCLoan() {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }
        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = body.getContent().stream()
                .filter(t -> t.getType() != null && "loanTransactionType.discountFeeAdjustment".equals(t.getType().getCode()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed()))
                .max(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId))
                .orElseThrow(() -> new IllegalStateException("Active discount fee adjustment transaction not found on loan"));
        final ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                adjustmentTxn.getId(), "undo", request));
    }

    @When("Admin undo the Discount fee adjustment with {string} amount on Working Capital loan account")
    public void undoDiscountFeeAdjustmentByAmountWCLoan(final String adjustmentAmount) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }

        final BigDecimal amount = new BigDecimal(adjustmentAmount);
        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = body.getContent().stream().filter(t -> t.getType() != null)
                .filter(t -> "loanTransactionType.discountFeeAdjustment".equals(t.getType().getCode()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())).filter(t -> t.getTransactionAmount() != null)
                .filter(t -> t.getTransactionAmount().compareTo(amount) == 0)
                .max(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId)).orElseThrow(() -> new IllegalStateException(
                        "Active discount fee adjustment transaction with amount " + adjustmentAmount + " not found on loan"));

        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                adjustmentTxn.getId(), "undo", request));
    }

    @Then("Undo the last Discount fee adjustment on Working Capital loan account failed due to already reversed transaction with status code {int}")
    public void undoLastDiscountFeeAdjustmentAlreadyReversedFailure(final int expectedStatus) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }

        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = body.getContent().stream().filter(t -> t.getType() != null)
                .filter(t -> "loanTransactionType.discountFeeAdjustment".equals(t.getType().getCode()))
                .max(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId))
                .orElseThrow(() -> new IllegalStateException("Discount fee adjustment transaction not found on loan"));

        final String errorMessage = ErrorMessageHelper.discountAdjustmentUndoAlreadyReversedFailure();

        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, adjustmentTxn.getId(), "undo", request));

        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(expectedStatus);

        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @Then("Undo discount fee adjustment referencing the discount fee transaction on Working Capital loan account failed due to invalid transaction type with status code {int}")
    public void undoDiscountFeeAdjustmentInvalidTypeFailure(final int expectedStatus) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsResponse lastDiscountResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE);

        Assertions.assertNotNull(lastDiscountResponse);

        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();

        final String errorMessage = ErrorMessageHelper.discountAdjustmentUndoInvalidTypeFailure();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(
                        loanId, lastDiscountResponse.getResourceId(), "undo", request));

        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(expectedStatus);

        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @Then("Undo discount fee adjustment with a non-existent transaction id on Working Capital loan account failed as not found with status code {int}")
    public void undoDiscountFeeAdjustmentNotFoundFailure(final int expectedStatus) {
        final Long loanId = getCreatedLoanId();
        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();

        final String errorMessage = ErrorMessageHelper.discountAdjustmentUndoTransactionNotFoundFailure();

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, 999999999L, "undo", request));

        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(expectedStatus);

        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @Then("Undo the last Discount fee adjustment on Working Capital loan account failed due to non active loan with status code {int}")
    public void undoLastDiscountFeeAdjustmentNotActiveLoanFailure(final int expectedStatus) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }

        final GetWorkingCapitalLoanTransactionIdResponse adjustmentTxn = body.getContent().stream().filter(t -> t.getType() != null)
                .filter(t -> "loanTransactionType.discountFeeAdjustment".equals(t.getType().getCode()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed()))
                .max(Comparator.comparing(GetWorkingCapitalLoanTransactionIdResponse::getId))
                .orElseThrow(() -> new IllegalStateException("Active discount fee adjustment transaction not found on loan"));

        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();

        final String errorMessage = ErrorMessageHelper.discountAdjustmentUndoNotActiveLoanFailure();

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, adjustmentTxn.getId(), "undo", request));

        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(expectedStatus);

        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    @And("Add Discount fee adjustment with {string} amount on Working Capital loan account failed due to exceeding discount amount")
    public void addDiscountFeeAdjustmentExceededFailure(final String adjustmentAmount) {
        addDiscountFeeAdjustmentFailedCheck(adjustmentAmount, null, ErrorMessageHelper.discountAdjustmentExceedFailure());
    }

    @Then("Add Discount fee adjustment with {string} amount and transaction date {string} on Working Capital loan account failed due to transaction date before discount fee date")
    public void addDiscountFeeAdjustmentBeforeDiscountDateFailure(final String adjustmentAmount, final String transactionDate) {
        addDiscountFeeAdjustmentFailedCheck(adjustmentAmount, transactionDate,
                ErrorMessageHelper.discountAdjustmentBeforeDiscountDateFailure());
    }

    @Then("Add Discount fee adjustment with {string} amount and transaction date {string} on Working Capital loan account failed due to future date")
    public void addDiscountFeeAdjustmentFutureDateFailure(final String adjustmentAmount, final String transactionDate) {
        addDiscountFeeAdjustmentFailedCheck(adjustmentAmount, transactionDate, ErrorMessageHelper.discountAdjustmentFutureDateFailure());
    }

    @Then("Add Discount fee adjustment with {string} amount and transaction date {string} on Working Capital loan account failed as amount must be greater then zero")
    public void addDiscountFeeAdjustmentZeroAmountFailure(final String adjustmentAmount, final String transactionDate) {
        addDiscountFeeAdjustmentFailedCheck(adjustmentAmount, transactionDate, ErrorMessageHelper.discountAdjustmentZeroAmountFailure());
    }

    @Then("Add Discount fee adjustment with {string} amount and transaction date {string} on Working Capital loan account failed due to not active loan")
    public void addDiscountFeeAdjustmentNotActiveLoanFailure(final String adjustmentAmount, final String transactionDate) {
        addDiscountFeeAdjustmentFailedCheck(adjustmentAmount, transactionDate, ErrorMessageHelper.discountAdjustmentNotActiveLoanFailure());
    }

    @And("Working Capital Loan has transactions:")
    public void workingCapitalLoanHasTransactions(final DataTable dataTable) throws InvocationTargetException, IllegalAccessException {
        final GetWorkingCapitalLoanTransactionsResponse getWorkingCapitalLoansLoanIdResponse = retrieveLoanTransactions(getCreatedLoanId());
        final List<GetWorkingCapitalLoanTransactionIdResponse> actualTransactions = getWorkingCapitalLoansLoanIdResponse.getContent();
        assertTable(GetWorkingCapitalLoanTransactionIdResponse.class, dataTable, actualTransactions);
    }

    @And("Working Capital Loan {string} transaction on {string} has the following charge paid-by data:")
    public void workingCapitalLoanTransactionHasChargePaidByData(final String transactionType, final String transactionDate,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final TransactionType resolvedType = resolveTransactionType(transactionType);
        final List<GetWorkingCapitalLoanTransactionIdResponse> matches = findMatchingTransactions(loanId, resolvedType, transactionDate,
                false);
        Assertions.assertFalse(matches.isEmpty(),
                String.format("No non-reversed %s transaction found on %s for loan %s", transactionType, transactionDate, loanId));
        final GetWorkingCapitalLoanTransactionIdResponse transaction = matches.get(0);

        final List<GetWorkingCapitalLoanChargePaidByData> actualPaidBy = transaction.getChargePaidByList() == null ? List.of()
                : transaction.getChargePaidByList();

        final List<List<String>> data = table.asLists();
        final List<String> headers = data.get(0);
        final List<List<String>> actualRows = actualPaidBy.stream().map(paidBy -> fetchChargePaidByValues(headers, paidBy)).toList();

        for (int i = 1; i < data.size(); i++) {
            final List<String> expectedValues = data.get(i);
            assertThat(actualRows.contains(expectedValues))
                    .as("%nNo matching charge paid-by row for %s transaction on %s of loan %s line %s.%nActual rows: %s%nExpected row: %s",
                            transactionType, transactionDate, loanId, i, actualRows, expectedValues)
                    .isTrue();
        }
        Assertions.assertEquals(data.size() - 1, actualPaidBy.size(),
                String.format("Charge paid-by row count mismatch for %s transaction on %s of loan %s. Actual: %s", transactionType,
                        transactionDate, loanId, actualPaidBy));
    }

    private List<String> fetchChargePaidByValues(final List<String> headers, final GetWorkingCapitalLoanChargePaidByData paidBy) {
        final List<String> values = new ArrayList<>();
        for (final String headerName : headers) {
            switch (headerName) {
                case "Charge Name" -> values.add(paidBy.getName());
                case "Amount" ->
                    values.add(paidBy.getAmount() == null ? null : new Utils.DoubleFormatter(paidBy.getAmount().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return values;
    }

    @Then("Admin successfully add discount with {string} amount on Working Capital loan account")
    public void adminSuccessfullyUpdateDiscountWithAmountOnWorkingCapitalLoanAccount(final String discountAmount) {
        addDiscountFeeWCLoanDisbursement(discountAmount);
    }

    @Then("In Working Capital Loan Transactions all transactions have non-blank external-id")
    public void workingCapitalLoanTransactionsHaveNonBlankExternalId() {
        final GetWorkingCapitalLoanTransactionsResponse loanResponse = retrieveLoanTransactions(getCreatedLoanId());
        final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = loanResponse.getContent();
        Assertions.assertNotNull(transactions, "WC loan transactions list must not be null");
        for (final GetWorkingCapitalLoanTransactionIdResponse txn : transactions) {
            assertThat(txn.getExternalId()).as("WC transaction id=%s type=%s date=%s must have a non-blank externalId", txn.getId(),
                    txn.getType() == null ? null : txn.getType().getValue(), txn.getTransactionDate()).isNotBlank();
        }
    }

    @Then("Add discount with {string} amount on Working Capital loan account failed due to date diff from disbursement date")
    public void updateDiscountWithAmountOnWorkingCapitalLoanAccountFailedDueToDateDiffFromDisbursementDate(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountDiffDateFromDisburseFailure();
        addDiscountFeeFailedCheck(discountAmount, errorMessage);
    }

    @Then("Add discount with {string} amount on Working Capital loan account failed due to already added discount before disbursement")
    public void addDiscountWithAmountOnWorkingCapitalLoanAccountFailedDueToAlreadyAddedDiscountBeforeDisbursement(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountAlreadySetBeforeDisburseFailure();
        addDiscountFeeFailedCheck(discountAmount, errorMessage);
    }

    @Then("Add discount with {string} amount on Working Capital loan account failed due to override disallowed by product")
    public void updateDiscountWithAmountOnWorkingCapitalLoanAccountFailedDueToOverrideDisallowedByProduct(String discountAmount) {
        String errorMessage = ErrorMessageHelper.overrideDisallowedByProductFailure();
        addDiscountFeeFailedCheck(discountAmount, errorMessage);
    }

    @Then("Add discount with {string} amount on Working Capital loan account failed due to exceed discount amount")
    public void updateDiscountWithAmountOnWorkingCapitalLoanAccountFailedDueToExceedDiscountAmount(String discountAmount) {
        String errorMessage = ErrorMessageHelper.discountExceedProductDiscountFailure();
        addDiscountFeeFailedCheck(discountAmount, errorMessage);
    }

    @When("Admin update Working Capital period payment rate with {string} value")
    public void adminAddWorkingCapitalPeriodPaymentRate(String periodPaymentRate) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PutWorkingCapitalLoansLoanIdRateRequest rateChangeRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUpdateRateRequest().periodPaymentRate(new BigDecimal(periodPaymentRate));

        final CommandProcessingResult rateChangeResponse = ok(
                () -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateById(loanId, rateChangeRequest));
        final Long rateChangeId = rateChangeResponse.getResourceId();

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_RATE_CHANGE_ID, rateChangeId);
        assertThat(rateChangeResponse.getChanges()).isNotNull();
        checkWorkingCapitalPeriodPaymentRate(loanId, periodPaymentRate);
    }

    @When("Admin update Working Capital period payment rate with {string} value by externalId")
    public void adminAddWorkingCapitalPeriodPaymentRateByExternalId(String periodPaymentRate) {
        final Long loanId = getCreatedLoanId();
        final String externalId = retrieveLoanExternalId(loanId);

        PutWorkingCapitalLoansLoanIdRateRequest rateChangeRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUpdateRateRequest().periodPaymentRate(new BigDecimal(periodPaymentRate));

        final CommandProcessingResult rateChangeResponse = ok(
                () -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateByExternalId(externalId, rateChangeRequest));
        final Long rateChangeId = rateChangeResponse.getResourceId();

        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_RATE_CHANGE_ID, rateChangeId);
        assertThat(rateChangeResponse.getChanges()).isNotNull();
        checkWorkingCapitalPeriodPaymentRate(loanId, periodPaymentRate);
    }

    @When("Admin update Working Capital period payment rate failed with {string} value on non active loan")
    public void adminAddWorkingCapitalPeriodPaymentRateNonActiveLoanFailure(final String periodPaymentRate) {
        String errorMessage = ErrorMessageHelper.periodPaymentRateOnNonActiveLoanFailure();
        updatePeriodPaymentRateFailed(periodPaymentRate, errorMessage);
    }

    @When("Admin update Working Capital period payment rate failed with {string} value with {} error message")
    public void adminAddWorkingCapitalPeriodPaymentRateInvalidDataFailure(final String periodPaymentRate, final String errorMessage) {
        updatePeriodPaymentRateFailed(periodPaymentRate, errorMessage);
    }

    @When("Admin update Working Capital period payment rate failed with {string} value cause unable to calculate EIR")
    public void adminAddWorkingCapitalPeriodPaymentRateCauseUnableCalculateEIrFailure(final String periodPaymentRate) {
        String errorMessage = ErrorMessageHelper.workingCapitalInputValuesCauseUnableCalculateEIrFailure();
        updatePeriodPaymentRateFailed(periodPaymentRate, errorMessage, 403);
    }

    @When("Working Capital Loan Period Payment Rate changes history contains the following data:")
    public void adminChecksWorkingCapitalPeriodPaymentRateChangesHistory(DataTable table) {
        PostWorkingCapitalLoansResponse loanCreateResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanCreateResponse.getLoanId();
        String resourceId = String.valueOf(loanId);

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> rateChangesResponse = ok(
                () -> fineractClient.workingCapitalLoans().getWorkingCapitalLoanRateChangeHistoryById(loanId));

        List<List<String>> data = table.asLists();
        List<String> header = table.row(0);
        checkPeriodPaymentRateChangeHistory(data, rateChangesResponse, header, resourceId);
    }

    @When("Working Capital Loan Period Payment Rate changes history by externalId contains the following data:")
    public void adminChecksWorkingCapitalPeriodPaymentRateChangesHistoryByExternalId(DataTable table) {
        final Long loanId = getCreatedLoanId();
        String resourceId = String.valueOf(loanId);
        final String externalId = retrieveLoanExternalId(loanId);

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> rateChangesResponse = ok(
                () -> fineractClient.workingCapitalLoans().getWorkingCapitalLoanRateChangeHistoryByExternalId(externalId));

        List<List<String>> data = table.asLists();
        List<String> header = table.row(0);
        checkPeriodPaymentRateChangeHistory(data, rateChangesResponse, header, resourceId);
    }

    // ====================================
    // Private Helper Methods
    // ====================================

    // Loan Lifecycle Helpers
    private void createWorkingCapitalLoanAccount(final List<String> loanData) {
        final String loanProduct = loanData.getFirst();
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

    public void creatingAWorkingCapitalLoanWithInvalidDataResultAnError(final DataTable table, final String errorMessage) {
        final List<List<String>> data = table.asLists();
        final List<String> loanData = data.get(1);

        final String loanProduct = loanData.getFirst();
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansRequest = buildCreateLoanRequest(clientId, loanProductId, loanData);

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, 400);
        assertValidationError(exception, errorMessage);

        log.info("Verified working capital loan creation failed with error message '{}'", errorMessage);
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

    public void addDiscountFeeFailedCheck(String discountAmount, String errorMessage) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse);
        Assertions.assertNotNull(loanResponse.getLoanId());
        long loanId = loanResponse.getLoanId();
        PostWorkingCapitalLoansLoanIdResponse lastDisbursementResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);
        Assertions.assertNotNull(lastDisbursementResponse);

        final PostWorkingCapitalLoanTransactionsRequest updateDiscountRequest = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDisbursementResponse.getResourceId())
                .transactionAmount(new BigDecimal(discountAmount));

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "discountFee", updateDiscountRequest));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void addDiscountFeeAdjustmentFailedCheck(final String adjustmentAmount, final String transactionDateOrNull,
            final String errorMessage) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Assertions.assertNotNull(loanResponse);
        Assertions.assertNotNull(loanResponse.getLoanId());
        final long loanId = loanResponse.getLoanId();
        final PostWorkingCapitalLoanTransactionsResponse lastDiscountResponse = testContext()
                .get(TestContextKey.WORKING_CAPITAL_LOAN_DISCOUNT_FEE_RESPONSE);
        Assertions.assertNotNull(lastDiscountResponse);
        final PostWorkingCapitalLoanTransactionsRequest adjustmentRequest = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().relatedResourceId(lastDiscountResponse.getResourceId())
                .transactionAmount(new BigDecimal(adjustmentAmount));
        if (transactionDateOrNull != null) {
            adjustmentRequest.transactionDate(transactionDateOrNull);
        }
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "discountFeeAdjustment", adjustmentRequest));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(400);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    private void executeDiscountFeeAdjustmentById(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId, "discountFeeAdjustment",
                request));
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
        if ("WCLP_DELINQUENCY".equals(loanProductName) || "WCLP_BREACH".equals(loanProductName)) {
            final PostWorkingCapitalLoanProductsResponse response = testContext()
                    .get(TestContextKey.WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE);
            if (response != null) {
                return response.getResourceId();
            }
        }
        final DefaultWorkingCapitalLoanProduct product = DefaultWorkingCapitalLoanProduct.valueOf(loanProductName);
        return workingCapitalLoanProductResolver.resolve(product);
    }

    private BigDecimal extractPrincipalFromModifyTable(final DataTable table) {
        final Map<String, String> data = table.asMaps().getFirst();
        return new BigDecimal(data.get("principalAmount"));
    }

    private List<String> fetchValuesOfWorkingCapitalLoan(final List<String> header, final GetWorkingCapitalLoansLoanIdResponse response) {
        final List<String> actualValues = new ArrayList<>();
        for (final String headerName : header) {
            switch (headerName) {
                case "product.name" -> actualValues.add(response.getLoanProductName());
                case "submittedOnDate" ->
                    actualValues.add(response.getTimeline() == null || response.getTimeline().getSubmittedOnDate() == null ? null
                            : response.getTimeline().getSubmittedOnDate().toString());
                case "expectedDisbursementDate" ->
                    actualValues.add(response.getDisbursementDetails() == null || response.getDisbursementDetails().isEmpty() ? null
                            : response.getDisbursementDetails().getFirst().getExpectedDisbursementDate().toString());
                case "status" -> actualValues.add(response.getStatus() == null ? null : response.getStatus().getValue());
                case "proposedPrincipal" -> actualValues.add(response.getProposedPrincipal() == null ? null
                        : new Utils.DoubleFormatter(response.getProposedPrincipal().doubleValue()).format());
                case "principal" -> actualValues.add(response.getBalance() == null || response.getBalance().getPrincipal() == null ? null
                        : new Utils.DoubleFormatter(response.getBalance().getPrincipal().doubleValue()).format());
                case "approvedPrincipal" -> actualValues.add(response.getApprovedPrincipal() == null ? "0"
                        : new Utils.DoubleFormatter(response.getApprovedPrincipal().doubleValue()).format());
                case "totalPaymentVolume" -> actualValues.add(response.getTotalPaymentVolume() == null ? null
                        : new Utils.DoubleFormatter(response.getTotalPaymentVolume().doubleValue()).format());
                case "periodPaymentRate" -> actualValues.add(response.getPaymentRate() == null ? null
                        : new Utils.DoubleFormatter(response.getPaymentRate().doubleValue()).format());
                case "discount" -> actualValues.add(response.getDiscountFee() == null ? "null"
                        : new Utils.DoubleFormatter(response.getDiscountFee().doubleValue()).format());
                case "discountProposed" -> actualValues.add(response.getProposedDiscountFee() == null ? "null"
                        : new Utils.DoubleFormatter(response.getProposedDiscountFee().doubleValue()).format());
                case "discountApproved" -> actualValues.add(response.getApprovedDiscountFee() == null ? "null"
                        : new Utils.DoubleFormatter(response.getApprovedDiscountFee().doubleValue()).format());
                case "totalPaidPrincipal" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getPrincipalPaid() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getPrincipalPaid().doubleValue()).format());
                case "overpaymentAmount" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getOverpaymentAmount() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getOverpaymentAmount().doubleValue()).format());
                case "realizedIncome" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getRealizedIncomeFromDiscountFee() == null
                            ? null
                            : new Utils.DoubleFormatter(response.getBalance().getRealizedIncomeFromDiscountFee().doubleValue()).format());
                case "unrealizedIncome" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getUnrealizedIncomeFromDiscountFee() == null
                            ? null
                            : new Utils.DoubleFormatter(response.getBalance().getUnrealizedIncomeFromDiscountFee().doubleValue()).format());
                case "totalDiscountFee" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getTotalDiscountFee() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getTotalDiscountFee().doubleValue()).format());
                case "breachStartDate" ->
                    actualValues.add(response.getBreachStartDate() == null ? "null" : response.getBreachStartDate().toString());
                case "delinquencyStartDate" ->
                    actualValues.add(response.getDelinquencyStartDate() == null ? "null" : response.getDelinquencyStartDate().toString());
                case "totalDiscountFeeAdjustment" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getTotalDiscountFeeAdjustment() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getTotalDiscountFeeAdjustment().doubleValue()).format());
                case "breachPastDueAmount" ->
                    actualValues.add(response.getBalance() == null || response.getBalance().getBreachPastDueAmount() == null ? null
                            : new Utils.DoubleFormatter(response.getBalance().getBreachPastDueAmount().doubleValue()).format());
                case "chargedOff" -> actualValues.add(String.valueOf(response.getChargedOff()));
                case "chargedOffOnDate" ->
                    actualValues.add(response.getChargedOffOnDate() == null ? "null" : response.getChargedOffOnDate().toString());
                case "chargeOffReason.name" ->
                    actualValues.add(response.getChargeOffReason() == null ? "null" : response.getChargeOffReason().getName());
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
        final String totalPaymentVolume = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);
        final String discount = loanData.get(6);

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principalAmount(new BigDecimal(principal))//
                .totalPaymentVolume(new BigDecimal(totalPaymentVolume))//
                .periodPaymentRate(new BigDecimal(periodPaymentRate))//
                .discount(discount != null && !discount.isEmpty() ? new BigDecimal(discount) : null);//
    }

    private PostWorkingCapitalLoansRequest buildCreateLoanBaseRequest(final Long clientId, final Long productId,
            final List<String> loanData) {
        final String submittedOnDate = loanData.get(1);
        final String expectedDisbursementDate = loanData.get(2);
        final String principal = loanData.get(3);
        final String totalPaymentVolume = loanData.get(4);
        final String periodPaymentRate = loanData.get(5);

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .expectedDisbursementDate(expectedDisbursementDate)//
                .principalAmount(new BigDecimal(principal))//
                .totalPaymentVolume(new BigDecimal(totalPaymentVolume))//
                .periodPaymentRate(new BigDecimal(periodPaymentRate));//
    }

    private PutWorkingCapitalLoansLoanIdRequest buildModifyLoanRequest(final List<String> loanData) {
        final String submittedOnDate = loanData.getFirst();
        final String expectedDisbursementDate = loanData.get(1);
        final String principal = loanData.get(2);
        final String totalPaymentVolume = loanData.get(3);
        final String periodPaymentRate = loanData.get(4);
        final String discount = loanData.get(5);

        return workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .submittedOnDate(submittedOnDate != null && !submittedOnDate.isEmpty() ? submittedOnDate : null)
                .expectedDisbursementDate(
                        expectedDisbursementDate != null && !expectedDisbursementDate.isEmpty() ? expectedDisbursementDate : null)
                .principalAmount(principal != null && !principal.isEmpty() ? new BigDecimal(principal) : null)
                .totalPaymentVolume(totalPaymentVolume != null && !totalPaymentVolume.isEmpty() ? new BigDecimal(totalPaymentVolume) : null)
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
        final String expectedHttpCode = data.get(1).getFirst();
        final String expectedErrorMessage = data.get(1).get(1);

        log.debug("Checking for Http code: {} and error message: \"{}\"", expectedHttpCode, expectedErrorMessage);

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
        final Map<String, String> row = table.asMaps().getFirst();
        submitLoanAndStore(buildGraceDaysLoanRequest(row));
    }

    @When("Admin creates a working capital loan with grace days override and the following data:")
    public void createLoanWithGraceDaysOverride(final DataTable table) {
        final Map<String, String> row = table.asMaps().getFirst();
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
                .totalPaymentVolume(new BigDecimal(row.get("totalPaymentVolume"))) //
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
        final Map<String, String> row = table.asMaps().getFirst();
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
                .totalPaymentVolume(new BigDecimal("100")) //
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
                .totalPaymentVolume(new BigDecimal("100")) //
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

    @Then("Admin creates working capital loan with with breach on {string} date")
    public void createLoanWithBreachOverrideAllowedWithBreachData(String submittedOnDate) {
        final Long breachId = createBreachAndGetId();

        final PostWorkingCapitalLoansRequest loansRequest = createWorkingCapitalLoanAccountDefaultRequest(submittedOnDate)
                .breachId(breachId);
        createWorkingCapitalLoanAccount(loansRequest);
    }

    public void checkCreateWCLoanAccountBreachData(Long breachId) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse loanAccountResponse = retrieveLoanDetails(loanId);
        assert loanAccountResponse.getBreach() != null;
        assertThat(loanAccountResponse.getBreach().getId()).isEqualTo(breachId);
        assertThat(loanAccountResponse.getNearBreach()).isNull();
    }

    public void checkCreateWCLoanAccountBreachNearBreachData(Long breachId, Long nearBreachId) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        final GetWorkingCapitalLoansLoanIdResponse loanAccountResponse = retrieveLoanDetails(loanId);
        assert loanAccountResponse.getBreach() != null;
        assert loanAccountResponse.getNearBreach() != null;
        assertThat(loanAccountResponse.getBreach().getId()).isEqualTo(breachId);
        assertThat(loanAccountResponse.getNearBreach().getId()).isEqualTo(nearBreachId);
    }

    public PostWorkingCapitalLoansRequest createWorkingCapitalLoanAccountDefaultRequest(String submittedOnDate) {
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(DefaultWorkingCapitalLoanProduct.WCLP.name());

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId).productId(loanProductId) //
                .submittedOnDate(submittedOnDate) //
                .expectedDisbursementDate(submittedOnDate) //
                .principalAmount(new BigDecimal("100")) //
                .totalPaymentVolume(new BigDecimal("100")) //
                .periodPaymentRate(new BigDecimal("1")) //
                .discount(BigDecimal.ZERO);
    }

    public PostWorkingCapitalLoansRequest createWorkingCapitalLoanAccountDefaultRequest(String loanProduct, String submittedOnDate) {
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);

        return workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId).productId(loanProductId) //
                .submittedOnDate(submittedOnDate) //
                .expectedDisbursementDate(submittedOnDate) //
                .principalAmount(new BigDecimal("100")) //
                .totalPaymentVolume(new BigDecimal("100")) //
                .periodPaymentRate(new BigDecimal("1")) //
                .discount(BigDecimal.ZERO);
    }

    public PostWorkingCapitalLoansRequest createWorkingCapitalLoanAccountWithBreachNearBreachRequest(final List<String> loanData,
            Long breachId, Long nearBreachId) {
        final String loanProduct = loanData.getFirst();
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        final PostWorkingCapitalLoansRequest loansBaseRequest = buildCreateLoanBaseRequest(clientId, loanProductId, loanData);
        final PostWorkingCapitalLoansRequest loansRequest = loansBaseRequest.breachId(breachId).nearBreachId(nearBreachId);
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);
        return loansRequest;
    }

    public PostWorkingCapitalLoansRequest createWorkingCapitalLoanAccountWithBaseRequest(final List<String> loanData) {
        final String loanProduct = loanData.getFirst();
        final Long clientId = extractClientId();
        final Long loanProductId = resolveLoanProductId(loanProduct);
        return buildCreateLoanBaseRequest(clientId, loanProductId, loanData);
    }

    public void createWorkingCapitalLoanAccountWithBreachNearBreachData(final List<String> loanData, Long breachId, Long nearBreachId) {
        final PostWorkingCapitalLoansRequest loansBaseRequest = createWorkingCapitalLoanAccountWithBaseRequest(loanData);
        final PostWorkingCapitalLoansRequest loansRequest = loansBaseRequest.breachId(breachId).nearBreachId(nearBreachId);
        testContext().set(TestContextKey.LOAN_CREATE_REQUEST, loansRequest);
        createWorkingCapitalLoanAccount(loansRequest);
    }

    public void createWorkingCapitalLoanAccount(PostWorkingCapitalLoansRequest loansRequest) {
        final PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        assertThat(response).isNotNull();
        assertThat(response.getLoanId()).isNotNull();
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);
        trackLoanIdIfEnabled(response.getLoanId());
        log.info("Working Capital Loan created with breach and bear breach with ID: {}", response.getLoanId());
    }

    public void verifyCreateWorkingCapitalLoanAccountFailure(PostWorkingCapitalLoansRequest loansRequest, int statusCode, String message) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(loansRequest));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, exception);

        assertHttpStatus(exception, statusCode);
        assertValidationError(exception, message);
    }

    private Long createBreachAndGetId() {
        final CommandProcessingResult breachResponse = ok(() -> fineractClient.workingCapitalBreaches()
                .createWorkingCapitalBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalBreachRequest()));
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachResponse.getResourceId());
        return breachResponse.getResourceId();
    }

    private Long createBreachOverrideAndGetId() {
        final CommandProcessingResult breachResponse = ok(() -> fineractClient.workingCapitalBreaches()
                .createWorkingCapitalBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalBreachRequest()));
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE, breachResponse.getResourceId());
        return breachResponse.getResourceId();
    }

    private Long createBreachAndGetId(int breachFrequency, String breachFrequencyType) {
        return createBreachOverrideAndGetId(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachFrequency, breachFrequencyType);
    }

    private Long createBreachOverrideAndGetId(int breachFrequency, String breachFrequencyType) {
        return createBreachOverrideAndGetId(TestContextKey.WORKING_CAPITAL_BREACH_ID_OVERRIDE, breachFrequency, breachFrequencyType);
    }

    private Long createBreachOverrideAndGetId(String breachIdLink, int breachFrequency, String breachFrequencyType) {
        final CommandProcessingResult breachResponse = ok(
                () -> fineractClient.workingCapitalBreaches().createWorkingCapitalBreach(workingCapitalProductRequestFactory
                        .defaultWorkingCapitalBreachRequest().breachFrequency(breachFrequency).breachFrequencyType(breachFrequencyType)));
        testContext().set(breachIdLink, breachResponse.getResourceId());
        return breachResponse.getResourceId();
    }

    private Long createNearBreachAndGetId() {
        final CommandProcessingResult nearBreachResponse = ok(() -> fineractClient.workingCapitalNearBreaches()
                .createWorkingCapitalNearBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalNearBreachRequest()));
        testContext().set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID, nearBreachResponse.getResourceId());
        return nearBreachResponse.getResourceId();
    }

    private Long createNearBreachOverrideAndGetId() {
        final CommandProcessingResult nearBreachResponse = ok(() -> fineractClient.workingCapitalNearBreaches()
                .createWorkingCapitalNearBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalNearBreachRequest()));
        testContext().set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_OVERRIDE, nearBreachResponse.getResourceId());
        return nearBreachResponse.getResourceId();
    }

    private Long createNearBreachAndGetId(int nearBreachFrequency, String nearBreachFrequencyType) {
        return createNearBreachAndGetId(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID, nearBreachFrequency, nearBreachFrequencyType);
    }

    private Long createNearBreachOverrideAndGetId(int nearBreachFrequency, String nearBreachFrequencyType) {
        return createNearBreachAndGetId(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID_OVERRIDE, nearBreachFrequency,
                nearBreachFrequencyType);
    }

    private Long createNearBreachAndGetId(String nearBreachIdLink, int nearBreachFrequency, String nearBreachFrequencyType) {
        final CommandProcessingResult nearBreachResponse = ok(() -> fineractClient.workingCapitalNearBreaches()
                .createWorkingCapitalNearBreach(workingCapitalProductRequestFactory.defaultWorkingCapitalNearBreachRequest()
                        .nearBreachFrequency(nearBreachFrequency).nearBreachFrequencyType(nearBreachFrequencyType)));
        testContext().set(nearBreachIdLink, nearBreachResponse.getResourceId());
        return nearBreachResponse.getResourceId();
    }

    private Long getBreachIdFromWCLP(Long loanProductId) {
        GetWorkingCapitalLoanProductsProductIdResponse loanProductResponse = fineractClient.workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProduct(loanProductId, Map.of());
        Long breachId = loanProductResponse.getBreach().getId();
        testContext().set(TestContextKey.WORKING_CAPITAL_BREACH_ID, breachId);
        return breachId;
    }

    private Long getNearBreachIdFromWCLP(Long loanProductId) {
        GetWorkingCapitalLoanProductsProductIdResponse loanProductResponse = fineractClient.workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProduct(loanProductId, Map.of());
        Long nearBreachId = loanProductResponse.getNearBreach().getId();
        testContext().set(TestContextKey.WORKING_CAPITAL_NEAR_BREACH_ID, nearBreachId);
        return nearBreachId;
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

    public void verifyModifyWorkingCapitalLoanAccountFailure(PutWorkingCapitalLoansLoanIdRequest modifyRequest, int statusCode,
            String message) {
        final CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(getCreatedLoanId(), modifyRequest, ""));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, exception);

        assertHttpStatus(exception, statusCode);
        assertValidationError(exception, message);
    }

    @Then("Customer makes repayment on {string} with {double} transaction amount on Working Capital loan")
    public void makeWorkingCapitalLoanRepayment(final String transactionDate, final double transactionAmount) {
        makeWorkingCapitalLoanRepaymentLike("REPAYMENT", transactionDate, transactionAmount);
    }

    @Then("Customer makes {string} transaction on {string} with {double} transaction amount on Working Capital loan")
    public void makeWorkingCapitalLoanRepaymentLike(final String transactionTypeInput, final String transactionDate,
            final double transactionAmount) {
        final Long loanId = getCreatedLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        final String transactionTypeValue = transactionType.getValue();
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount, null);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentLikeById(loanId, transactionTypeValue,
                repaymentRequest);
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
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentLikeById(loanId, "repayment", repaymentRequest);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    @Then("Admin closes the Working Capital loan with a full repayment on {string}")
    public void closeWorkingCapitalLoanWithFullRepayment(final String transactionDate) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoansLoanIdResponse loanDetails = ok(
                () -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        Assertions.assertNotNull(loanDetails.getBalance());
        Assertions.assertNotNull(loanDetails.getBalance().getTotalOutstanding());
        final BigDecimal totalOutstanding = loanDetails.getBalance().getTotalOutstanding();
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().transactionDate(transactionDate).transactionAmount(totalOutstanding);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentLikeById(loanId, "repayment", repaymentRequest);
        Assertions.assertNotNull(loanDetails.getBalance());
        validateRepaymentResponse(response, totalOutstanding.doubleValue(), transactionDate, loanId);
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

    @Then("Customer fails to make credit balance refund on {string} with {double} EUR transaction amount backdated outcomes with error message")
    public void creditBalanceRefundWCLoanFailureBackdated(final String transactionDate, final double transactionAmount) {
        String errorMessage = ErrorMessageHelper.creditBalanceRefundBackdatedForbiddenFailure();
        creditBalanceRefundWCLoanFailure(transactionDate, transactionAmount, 400, errorMessage);
    }

    @Then("Customer makes {string} transaction on {string} with {double} transaction amount on Working Capital loan with the following payment details:")
    public void makeWorkingCapitalLoanTransactionLikeWithPaymentDetails(final String transactionTypeInput, final String transactionDate,
            final double transactionAmount, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final String command = TransactionType.valueOf(transactionTypeInput).getValue();
        final PostWorkingCapitalLoanTransactionsPaymentDetailRequest paymentDetails = buildPaymentDetailsFromTable(table);
        final PostWorkingCapitalLoanTransactionsRequest request = buildRepaymentRequest(transactionDate, transactionAmount, paymentDetails);
        final PostWorkingCapitalLoanTransactionsResponse response = executeRepaymentLikeById(loanId, command, request);
        validateRepaymentResponse(response, transactionAmount, transactionDate, loanId);
    }

    @Then("Working Capital loan transaction with type {string} has payment type {string}")
    public void workingCapitalLoanTransactionHasPaymentType(final String transactionTypeInput, final String expectedPaymentTypeName) {
        final String expectedTransactionType = TransactionType.valueOf(transactionTypeInput).getValue();
        final String expectedTypeCode = "loanTransactionType." + expectedTransactionType;
        final Long loanId = getCreatedLoanId();
        final String lastTransactionType = testContext().get(WC_LAST_TRANSACTION_TYPE);
        final String lastTransactionDate = testContext().get(WC_LAST_TRANSACTION_DATE);
        final BigDecimal lastTransactionAmount = testContext().get(WC_LAST_TRANSACTION_AMOUNT);
        Assertions.assertNotNull(lastTransactionType,
                String.format("WC transaction type must be present before asserting payment type for %s", transactionTypeInput));
        Assertions.assertNotNull(lastTransactionDate,
                String.format("WC transaction date must be present before asserting payment type for %s", transactionTypeInput));
        Assertions.assertNotNull(lastTransactionAmount,
                String.format("WC transaction amount must be present before asserting payment type for %s", transactionTypeInput));
        assertThat(lastTransactionType).as("last WC transaction type").isEqualTo(expectedTransactionType);

        final List<GetWorkingCapitalLoanTransactionIdResponse> transactions = retrieveLoanTransactions(loanId).getContent();
        Assertions.assertNotNull(transactions, "WC loan transactions list must not be null");
        final List<GetWorkingCapitalLoanTransactionIdResponse> matchingTransactions = transactions.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getReversed()) && t.getType() != null && expectedTypeCode.equals(t.getType().getCode()))
                .filter(t -> t.getTransactionDate() != null && lastTransactionDate.equals(DATE_FORMATTER.format(t.getTransactionDate())))
                .filter(t -> t.getTransactionAmount() != null && t.getTransactionAmount().compareTo(lastTransactionAmount) == 0).toList();
        assertThat(matchingTransactions)
                .as("active %s WC transaction on %s with amount %s", transactionTypeInput, lastTransactionDate, lastTransactionAmount)
                .hasSize(1);
        final Long transactionId = matchingTransactions.getFirst().getId();
        Assertions.assertNotNull(transactionId, String.format("transaction id must be present on %s transaction", transactionTypeInput));

        final GetWorkingCapitalLoanTransactionIdResponse txn = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionById(loanId, transactionId));
        Assertions.assertNotNull(txn.getType(), String.format("transaction type must be present on %s transaction", transactionTypeInput));
        assertThat(txn.getType().getCode()).as("transaction type code").isEqualTo(expectedTypeCode);
        Assertions.assertNotNull(txn.getPaymentDetailData(),
                String.format("paymentDetailData must be present on %s transaction", transactionTypeInput));
        Assertions.assertNotNull(txn.getPaymentDetailData().getPaymentType(),
                String.format("paymentType must be present on %s transaction", transactionTypeInput));
        assertThat(txn.getPaymentDetailData().getPaymentType().getName()).as("payment type name on %s transaction", transactionTypeInput)
                .isEqualTo(expectedPaymentTypeName);
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
        rememberLastWorkingCapitalTransaction("creditBalanceRefund", cbrRequest.getTransactionDate(), cbrRequest.getTransactionAmount());
        final int after = countJournalEntriesForLoan(loanId);
        testContext().set(WC_CBR_JOURNAL_ENTRIES_BEFORE, before);
        testContext().set(WC_CBR_JOURNAL_ENTRIES_AFTER, after);
        return response;
    }

    public void creditBalanceRefundWCLoanFailure(final String transactionDate, final double transactionAmount, int errorCode,
            String errorMessage) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest cbrRequest = buildCreditBalanceRefundRequest(transactionDate, transactionAmount,
                null);
        CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", cbrRequest));
        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(errorCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
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

    private PostWorkingCapitalLoanTransactionsResponse executeRepaymentLikeById(final Long loanId, final String transactionType,
            final PostWorkingCapitalLoanTransactionsRequest repaymentRequest) {
        log.debug("Making {} for loan ID: {}, transactionDate: {}, transactionAmount: {}", transactionType, loanId,
                repaymentRequest.getTransactionDate(), repaymentRequest.getTransactionAmount());

        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, transactionType, repaymentRequest));
        rememberLastWorkingCapitalTransaction(transactionType, repaymentRequest.getTransactionDate(),
                repaymentRequest.getTransactionAmount());
        return response;
    }

    private PostWorkingCapitalLoanTransactionsResponse executeRepaymentByExternalId(final String loanExternalId,
            final PostWorkingCapitalLoanTransactionsRequest repaymentRequest) {
        log.debug("Making repayment for loan externalId: {}, transactionDate: {}, transactionAmount: {}", loanExternalId,
                repaymentRequest.getTransactionDate(), repaymentRequest.getTransactionAmount());

        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionByExternalId(loanExternalId, "repayment", repaymentRequest));
        rememberLastWorkingCapitalTransaction("repayment", repaymentRequest.getTransactionDate(), repaymentRequest.getTransactionAmount());
        return response;
    }

    private void rememberLastWorkingCapitalTransaction(final String transactionType, final String transactionDate,
            final BigDecimal transactionAmount) {
        testContext().set(WC_LAST_TRANSACTION_TYPE, transactionType);
        testContext().set(WC_LAST_TRANSACTION_DATE, transactionDate);
        testContext().set(WC_LAST_TRANSACTION_AMOUNT, transactionAmount);
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
                    "Wrong value in line %s of amortization schedule: \n actual=%s,\n expected=%s", i, matchingPeriods.stream()
                            .map(period -> fetchValuesOfWcAmortizationSchedule(headers, period)).collect(Collectors.toList()),
                    expectedValues).isTrue();
        }

        assertThat(linesActual)
                .as("Wrong number of lines in WC amortization schedule: \n actual=%s,\n expected=%s", linesActual, linesExpected)
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
            case "expectedPaymentAmount" -> asText(period.getExpectedPaymentAmount());
            case "expectedBalance" -> asText(period.getExpectedBalance());
            case "actualBalance" -> asText(period.getActualBalance());
            case "expectedAmortizationAmount" -> asText(period.getExpectedAmortizationAmount());
            case "actualPaymentAmount" -> asText(period.getActualPaymentAmount());
            case "actualAmortizationAmount" -> asText(period.getActualAmortizationAmount());
            case "expectedDiscountFeeBalance" -> asText(period.getExpectedDiscountFeeBalance());
            case "actualDiscountFeeBalance" -> asText(period.getActualDiscountFeeBalance());
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
        final List<String> headers = rows.getFirst();
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

    @Then("Initiating a {string} transaction on {string} with {double} transaction amount on Working Capital loan results an error with the following data:")
    public void initiateTransactionResultsAnErrorWithDetails(final String transactionTypeInput, final String transactionDate,
            final double transactionAmount, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        final String transactionTypeValue = transactionType.getValue();
        final PostWorkingCapitalLoanTransactionsRequest transactionRequest = buildRepaymentRequest(transactionDate, transactionAmount,
                null);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, transactionTypeValue, transactionRequest));

        if (table != null) {
            verifyErrorResponse(exception, table);
        }

        log.debug("Verified working capital loan {} transaction failed with expected error for loan {}", transactionTypeValue, loanId);
    }

    @Then("Initiating a repayment on {string} with {double} transaction amount on Working Capital loan results an error with the following data:")
    public void initiateRepaymentResultsAnErrorWithDetails(final String transactionDate, final double transactionAmount,
            final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest repaymentRequest = buildRepaymentRequest(transactionDate, transactionAmount, null);

        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "repayment", repaymentRequest));

        if (table != null) {
            verifyErrorResponse(exception, table);
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
            verifyErrorResponse(exception, table);
        }
        log.debug("Verified working capital loan credit balance refund failed with expected error for loan {}", loanId);
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
            assertBalanceFieldEquals(rows.get(i).getFirst(), rows.get(i).get(1));
        }
    }

    private void assertBalanceFieldEquals(final String field, final String expected) {
        final GetBalance balance = retrieveLoanDetails(getCreatedLoanId()).getBalance();
        assertNotNull(balance, "Balance payload should not be null");
        final BigDecimal actual = switch (field) {
            case "overpaymentAmount" -> balance.getOverpaymentAmount();
            case "principalOutstanding" -> balance.getPrincipalOutstanding();
            case "totalPaidPrincipal" -> balance.getPrincipalPaid();
            case "realizedIncome" -> balance.getRealizedIncomeFromDiscountFee();
            case "unrealizedIncome" -> balance.getUnrealizedIncomeFromDiscountFee();
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
        final GetWorkingCapitalLoanTransactionsResponse loan = retrieveLoanTransactions(getCreatedLoanId());
        final GetWorkingCapitalLoanTransactionIdResponse cbr = loan.getContent().stream()
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
            verifyErrorResponse(exception, table);
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
            verifyErrorResponse(exception, table);
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
            verifyErrorResponse(exception, table);
        }
    }

    private GetWorkingCapitalLoanTransactionIdResponse latestCreditBalanceRefundTransaction() {
        final GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = retrieveLoanTransactions(getCreatedLoanId());
        return loanTransactionsResponse.getContent().stream()
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

    private <T> void assertTable(Class<T> tClass, DataTable dataTable, List<T> actualTransactions)
            throws InvocationTargetException, IllegalAccessException {
        List<List<String>> table = dataTable.asLists();
        List<String> header = table.getFirst();
        List<List<String>> expectedTransactions = table.subList(1, table.size());
        assertTable(tClass, header, expectedTransactions, actualTransactions);
    }

    private <T> void assertTable(Class<T> tClass, List<String> header, List<List<String>> expectedRows, List<T> actualRows)
            throws InvocationTargetException, IllegalAccessException {
        // expected and actual list of transactions are empty
        if (expectedRows.isEmpty() && (actualRows == null || actualRows.isEmpty())) {
            return;
        }
        Assertions.assertNotNull(actualRows);
        Assertions.assertEquals(expectedRows.size(), actualRows.size());
        List<Method> methods = header.stream()
                .map(fieldName -> Arrays.stream(tClass.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase("get" + fieldName))
                        .findAny().orElseThrow(() -> new RuntimeException(new NoSuchMethodException("No such Method: "))))
                .toList();
        for (int i = 0; i < expectedRows.size(); i++) {
            T actualValues = actualRows.get(i);
            List<String> expectedValues = expectedRows.get(i);
            for (int iM = 0; iM < methods.size(); iM++) {
                Object actual = methods.get(iM).invoke(actualValues);
                String expected = expectedValues.get(iM);
                String message = "Line " + (i + 1) + " has miss match on field: " + header.get(iM);
                if (actual instanceof BigDecimal) {
                    Assertions.assertEquals(Double.parseDouble(expected), ((BigDecimal) actual).doubleValue(), message);
                } else if (actual instanceof LoanTransactionEnumData) {
                    Assertions.assertEquals(expected, ((LoanTransactionEnumData) actual).getValue(), message);
                } else if (actual instanceof LocalDate) {
                    Assertions.assertEquals(expected, FORMATTER.format((LocalDate) actual), message);
                } else {
                    Assertions.assertEquals(expectedValues.get(iM), actual == null ? null : actual.toString(), message);
                }
            }
        }
    }

    @And("Working Capital Loan has a {string} transaction with date {string} which has classification code value {string}")
    public void workingCapitalLoanTransactionHasClassification(String transactionType, String transactionDate,
            String expectedClassification) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        TransactionType resolvedTransactionType = resolveTransactionType(transactionType);
        List<GetWorkingCapitalLoanTransactionIdResponse> transactionsMatch = findMatchingTransactions(loanId, resolvedTransactionType,
                transactionDate, false);
        GetWorkingCapitalLoanTransactionIdResponse transaction = transactionsMatch.stream().findFirst().orElseThrow(
                () -> new IllegalStateException(String.format("No %s transaction found on %s", transactionType, transactionDate)));

        // Get detailed transaction information including classification
        GetWorkingCapitalLoanTransactionIdResponse transactionDetails = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .retrieveWorkingCapitalLoanTransactionById(loanId, transaction.getId()));

        assertThat(transactionDetails.getClassification()).as(String.format("%s transaction should have classification", transactionType))
                .isNotNull();
        assertThat(transactionDetails.getClassification().getName()).as("Classification name should match expected value")
                .isEqualTo(expectedClassification);
    }

    @Then("Working Capital Loan Transactions tab has a {string} transaction with date {string} which has the following Journal entries:")
    public void verifyWorkingCapitalLoanTransactionJournalEntries(final String transactionType, final String transactionDate,
            final DataTable table) {
        verifyTransactionsJournalEntries(transactionType, transactionDate, false, null, table);
    }

    @Then("Working Capital Loan Transactions tab has {int} {string} transactions with date {string} which have the following Journal entries:")
    public void verifyMultipleWorkingCapitalLoanTransactionsJournalEntries(final int expectedCount, final String transactionType,
            final String transactionDate, final DataTable table) {
        verifyTransactionsJournalEntries(transactionType, transactionDate, false, expectedCount, table);
    }

    @Then("Working Capital Loan Transactions tab has a reversed {string} transaction with date {string} which has the following Journal entries:")
    public void verifyReversedWorkingCapitalLoanTransactionJournalEntries(final String transactionType, final String transactionDate,
            final DataTable table) {
        verifyTransactionsJournalEntries(transactionType, transactionDate, true, null, table);
    }

    @When("Customer undo {string}th working capital transaction made on {string}")
    public void undoNthTransaction(String nthItemStr, String transactionDate) throws IOException {
        final GetWorkingCapitalLoanTransactionsResponse getWorkingCapitalLoansLoanIdResponse = retrieveLoanTransactions(getCreatedLoanId());
        final List<GetWorkingCapitalLoanTransactionIdResponse> actualTransactions = getWorkingCapitalLoansLoanIdResponse.getContent();

        int nthItem = Integer.parseInt(nthItemStr) - 1;

        GetWorkingCapitalLoanTransactionIdResponse transactionIdResponse = actualTransactions.stream()
                .filter(t -> transactionDate.equals(FORMATTER.format(t.getTransactionDate()))).toList().get(nthItem);

        String reversalExternalId = Utils.randomStringGenerator("wcl-reversal-ext-id", 8);
        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest()
                .reversalExternalId(reversalExternalId);

        ExecuteWorkingCapitalLoanTransactionCommandResponse undo = ok(
                () -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(
                        getCreatedLoanId(), transactionIdResponse.getId(), "undo", request));
        Assertions.assertNotNull(undo);

        // testContext().set(TestContextKey.LOAN_TRANSACTION_UNDO_RESPONSE, transactionUndoResponse);

    }

    private void verifyTransactionsJournalEntries(final String transactionType, final String transactionDate, final boolean reversed,
            final Integer expectedCount, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final TransactionType resolvedTransactionType = resolveTransactionType(transactionType);
        final List<GetWorkingCapitalLoanTransactionIdResponse> transactionsMatch = findMatchingTransactions(loanId, resolvedTransactionType,
                transactionDate, reversed);
        if (expectedCount != null) {
            assertThat(transactionsMatch.size()).as("The number of transactions does not match the expected count! Expected: "
                    + expectedCount + ", Actual: " + transactionsMatch.size()).isEqualTo(expectedCount);
        }
        verifyJournalEntries(transactionsMatch, loanId, table);
    }

    private TransactionType resolveTransactionType(String transactionType) {
        return TransactionType.valueOf(transactionType.toUpperCase(Locale.ROOT).replace(' ', '_'));
    }

    private List<GetWorkingCapitalLoanTransactionIdResponse> findMatchingTransactions(Long loanId, TransactionType transactionType,
            String transactionDate, boolean reversed) {
        GetWorkingCapitalLoanTransactionsResponse loanTransactionsResponse = retrieveLoanTransactions(loanId);

        String expectedCode = "loanTransactionType." + transactionType.getValue();
        return loanTransactionsResponse.getContent().stream()
                .filter(t -> t.getType() != null && transactionDate.equals(DATE_FORMATTER.format(t.getTransactionDate()))
                        && expectedCode.equals(t.getType().getCode())
                        && (reversed ? Boolean.TRUE.equals(t.getReversed()) : !Boolean.TRUE.equals(t.getReversed())))
                .collect(Collectors.toList());
    }

    private void verifyJournalEntries(List<GetWorkingCapitalLoanTransactionIdResponse> transactions, Long loanId, DataTable table) {
        List<List<JournalEntryTransactionItem>> journalLinesActualList = getWorkingCapitalJournalLinesActualList(transactions);
        log.debug("journalLinesActualList: {}", journalLinesActualList);
        journalEntriesStepDef.checkJournalEntryData(journalLinesActualList, loanId, table);
    }

    private List<List<JournalEntryTransactionItem>> getWorkingCapitalJournalLinesActualList(
            List<GetWorkingCapitalLoanTransactionIdResponse> transactions) {
        log.debug("Processing {} working capital loan transactions for journal entries", transactions.size());
        return transactions.stream().map(this::retrieveJournalEntriesForTransaction).collect(Collectors.toList());
    }

    private List<JournalEntryTransactionItem> retrieveJournalEntriesForTransaction(GetWorkingCapitalLoanTransactionIdResponse transaction) {
        String transactionId = "WC" + transaction.getId();
        log.debug("Retrieving journal entries for working capital transaction: {}", transactionId);

        JournalEntriesApi.RetrieveAllJournalEntriesQueryParams params = new JournalEntriesApi.RetrieveAllJournalEntriesQueryParams()
                .transactionId(transactionId).runningBalance(true);

        GetJournalEntriesTransactionIdResponse journalEntryDataResponse = ok(
                () -> fineractClient.journalEntries().retrieveAllJournalEntries(params));

        return journalEntryDataResponse != null && journalEntryDataResponse.getPageItems() != null ? journalEntryDataResponse.getPageItems()
                : List.of();
    }

    @When("Customer undo {string}th {string} transaction made on {string} on Working Capital loan")
    public void undoWorkingCapitalLoanTransaction(String nthItemStr, String transactionType, String transactionDate) throws IOException {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse response = retrieveLoanTransactions(loanId);
        final List<GetWorkingCapitalLoanTransactionIdResponse> actualTransactions = response.getContent();

        final TransactionType resolvedType = resolveTransactionType(transactionType);
        final String expectedCode = "loanTransactionType." + resolvedType.getValue();
        int nthItem = Integer.parseInt(nthItemStr) - 1;

        GetWorkingCapitalLoanTransactionIdResponse target = actualTransactions.stream()
                .filter(t -> t.getType() != null && expectedCode.equals(t.getType().getCode())
                        && transactionDate.equals(FORMATTER.format(t.getTransactionDate())) && !Boolean.TRUE.equals(t.getReversed()))
                .toList().get(nthItem);

        String reversalExternalId = Utils.randomStringGenerator("wcl-reversal-ext-id", 8);
        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest()
                .reversalExternalId(reversalExternalId);

        ExecuteWorkingCapitalLoanTransactionCommandResponse undo = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, target.getId(), "undo", request));
        Assertions.assertNotNull(undo);
    }

    @When("Customer tries to undo {string}th {string} transaction made on {string} on Working Capital loan and gets error:")
    public void undoWorkingCapitalLoanTransactionExpectError(String nthItemStr, String transactionType, String transactionDate,
            DataTable table) throws IOException {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse response = retrieveLoanTransactions(loanId);
        final List<GetWorkingCapitalLoanTransactionIdResponse> actualTransactions = response.getContent();

        final TransactionType resolvedType = resolveTransactionType(transactionType);
        final String expectedCode = "loanTransactionType." + resolvedType.getValue();
        int nthItem = Integer.parseInt(nthItemStr) - 1;

        GetWorkingCapitalLoanTransactionIdResponse target = actualTransactions.stream().filter(t -> t.getType() != null
                && expectedCode.equals(t.getType().getCode()) && transactionDate.equals(FORMATTER.format(t.getTransactionDate()))).toList()
                .get(nthItem);

        final Map<String, String> expectedData = table.asMaps().get(0);
        final int expectedHttpCode = Integer.parseInt(expectedData.get("httpCode"));
        final String expectedErrorMessage = expectedData.get("errorMessage").trim();

        ExecuteWorkingCapitalLoanTransactionCommandRequest request = new ExecuteWorkingCapitalLoanTransactionCommandRequest();

        CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, target.getId(), "undo", request));
        assertHttpStatus(exception, expectedHttpCode);
        assertValidationError(exception, expectedErrorMessage);
    }

    public void updatePeriodPaymentRateFailed(String periodPaymentRate, String errorMessage) {
        /*
         * final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
         * long loanId = loanResponse.getLoanId();
         *
         * PutWorkingCapitalLoansLoanIdRateRequest rateChangeRequest = workingCapitalLoanRequestFactory
         * .defaultWorkingCapitalLoanUpdateRateRequest().periodPaymentRate(new BigDecimal(periodPaymentRate));
         *
         * CallFailedRuntimeException exception = fail( () ->
         * fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateById(loanId, rateChangeRequest));
         *
         * assertThat(exception.getStatus()).as(errorMessage).isEqualTo(400);
         * assertThat(exception.getDeveloperMessage()).contains(errorMessage);
         */
        updatePeriodPaymentRateFailed(periodPaymentRate, errorMessage, 400);
    }

    public void updatePeriodPaymentRateFailed(String periodPaymentRate, String errorMessage, int responseCode) {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        PutWorkingCapitalLoansLoanIdRateRequest rateChangeRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanUpdateRateRequest().periodPaymentRate(new BigDecimal(periodPaymentRate));

        CallFailedRuntimeException exception = fail(
                () -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateById(loanId, rateChangeRequest));

        assertThat(exception.getStatus()).as(errorMessage).isEqualTo(responseCode);
        assertThat(exception.getDeveloperMessage()).contains(errorMessage);
    }

    public void checkWorkingCapitalPeriodPaymentRate(Long loanId, String periodPaymentRate) {
        final GetWorkingCapitalLoansLoanIdResponse loanDetailsResponse = retrieveLoanDetails(loanId);
        assert loanDetailsResponse.getPaymentRate() != null;
        assertThat(loanDetailsResponse.getPaymentRate().compareTo(new BigDecimal(periodPaymentRate))).isZero();
    }

    public void checkPeriodPaymentRateChangeHistory(List<List<String>> data,
            List<WorkingCapitalLoanPeriodPaymentRateChangeData> rateChanges, List<String> header, String resourceId) {
        checkPeriodPaymentRatesTabRows(data, rateChanges, header, resourceId);
        assertThat(rateChanges.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(resourceId, rateChanges.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    public void checkPeriodPaymentRatesTabRows(List<List<String>> data, List<WorkingCapitalLoanPeriodPaymentRateChangeData> rateChanges,
            List<String> header, String resourceId) {
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String transactionDateExpected = expectedValues.getFirst();
            List<List<String>> actualValuesList = rateChanges.stream()//
                    .filter(rate -> transactionDateExpected.equals(FORMATTER.format(rate.getEffectiveDate())))//
                    .map(rate -> fetchValuesOfRateChangesHistory(header, rate))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
    }

    private List<String> fetchValuesOfRateChangesHistory(List<String> header,
            WorkingCapitalLoanPeriodPaymentRateChangeData rateChangeData) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Effective Date" -> actualValues
                        .add(rateChangeData.getEffectiveDate() == null ? null : FORMATTER.format(rateChangeData.getEffectiveDate()));
                case "Previous Rate" -> actualValues.add(rateChangeData.getPreviousRate() == null ? null
                        : new Utils.DoubleFormatter(rateChangeData.getPreviousRate().doubleValue()).format());
                case "New Rate" -> actualValues.add(rateChangeData.getNewRate() == null ? null
                        : new Utils.DoubleFormatter(rateChangeData.getNewRate().doubleValue()).format());
                case "Reversed" ->
                    actualValues.add(rateChangeData.getReversed() == null ? null : String.valueOf(rateChangeData.getReversed()));
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private GetWorkingCapitalLoanTransactionsResponse retrieveLoanTransactions(Long loanId) {
        return fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId);
    }

    private Long getClassificationCodeValueId(String codeName, String codeValueName) {
        // Check if code value already exists
        List<GetCodeValuesDataResponse> existingCodeValues = fineractClient.codeValues().retrieveAllCodeValuesByCodeName(codeName);
        // Try to find existing code value with the same name
        for (GetCodeValuesDataResponse codeValue : existingCodeValues) {
            if (codeValueName.equals(codeValue.getName())) {
                log.debug("Reusing existing code value: {}", codeValueName);
                return codeValue.getId();
            }
        }

        throw new IllegalStateException(String.format("Code [%s] with code value [%s] cannot be found", codeName, codeValueName));
    }

    @When("Admin charges off the Working Capital loan on {string}")
    public void chargeOffWCLoan(final String transactionDate) {
        chargeOffWCLoan(transactionDate, null, null, null);
    }

    @Then("Charging off the Working Capital loan on {string} results an error with the following data:")
    public void chargeOffWCLoanResultsAnError(final String transactionDate, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest request = buildChargeOffRequest(transactionDate, null, null, null);
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request));
        if (table != null) {
            verifyErrorResponse(exception, table);
        }
        log.debug("Verified charge-off on {} fails with expected error for loan {}", transactionDate, loanId);
    }

    @When("Admin sets the fraud flag of the Working Capital loan to {word}")
    public void setWorkingCapitalLoanFraudFlag(final String fraudFlag) {
        final Long loanId = getCreatedLoanId();
        final MarkWorkingCapitalLoanAsFraudRequest request = new MarkWorkingCapitalLoanAsFraudRequest()
                .fraud(Boolean.parseBoolean(fraudFlag));
        ok(() -> fineractClient.workingCapitalLoans().markWorkingCapitalLoanAsFraudById(loanId, request));
        log.info("Set fraud flag={} on Working Capital loan {}", fraudFlag, loanId);
    }

    @When("Admin charges off the Working Capital loan on {string} with charge-off reason {string}")
    public void chargeOffWCLoanWithReason(final String transactionDate, final String chargeOffReasonName) {
        chargeOffWCLoan(transactionDate, chargeOffReasonName, null, null);
    }

    @When("Admin charges off the Working Capital loan on {string} with charge-off reason {string} and note {string}")
    public void chargeOffWCLoanWithReasonAndNote(final String transactionDate, final String chargeOffReasonName, final String note) {
        chargeOffWCLoan(transactionDate, chargeOffReasonName, note, null);
    }

    @When("Admin charges off the Working Capital loan on {string} with a random externalId")
    public void chargeOffWCLoanWithRandomExternalId(final String transactionDate) {
        final String randomExternalId = Utils.randomStringGenerator("chargeOffExt_", 10);
        chargeOffWCLoan(transactionDate, null, null, randomExternalId);
    }

    private void chargeOffWCLoan(final String transactionDate, final String chargeOffReasonName, final String note,
            final String externalId) {
        final Long loanId = getCreatedLoanId();
        final Long chargeOffReasonId = chargeOffReasonName != null ? resolveChargeOffReasonId(chargeOffReasonName) : null;
        final PostWorkingCapitalLoanTransactionsRequest request = buildChargeOffRequest(transactionDate, chargeOffReasonId, note,
                externalId);
        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request));
        assertNotNull(response.getResourceId(), "Charge-off transaction ID should not be null");
        log.info("Charged off working capital loan {} on {}", loanId, transactionDate);
        testContext().set(WC_LAST_TRANSACTION_TYPE, TransactionType.CHARGE_OFF.getValue());
        testContext().set(WC_LAST_TRANSACTION_DATE, transactionDate);
        testContext().set(WC_LAST_TRANSACTION_AMOUNT, null);
    }

    @Then("Initiating a charge-off on the Working Capital loan on {string} results an error with the following data:")
    public void chargeOffWCLoanFailureWithTable(final String transactionDate, final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest request = buildChargeOffRequest(transactionDate, null, null, null);
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request));
        verifyErrorResponse(exception, table);
        log.info("Verified charge-off failed with expected error for loan {}", loanId);
    }

    @When("Admin undoes the charge-off on the Working Capital loan")
    public void undoChargeOffWCLoan() {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest request = buildUndoChargeOffRequest();
        final PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "undoChargeOff", request));
        assertNotNull(response.getResourceId(), "Undo charge-off transaction ID should not be null");
        log.info("Undid charge-off on working capital loan {}", loanId);
    }

    @Then("Initiating an undo of the charge-off on the Working Capital loan results an error with the following data:")
    public void undoChargeOffWCLoanFailureWithTable(final DataTable table) {
        final Long loanId = getCreatedLoanId();
        final PostWorkingCapitalLoanTransactionsRequest request = buildUndoChargeOffRequest();
        final CallFailedRuntimeException exception = fail(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "undoChargeOff", request));
        verifyErrorResponse(exception, table);
        log.info("Verified undo charge-off failed with expected error for loan {}", loanId);
    }

    private PostWorkingCapitalLoanTransactionsRequest buildUndoChargeOffRequest() {
        return new PostWorkingCapitalLoanTransactionsRequest();
    }

    private PostWorkingCapitalLoanTransactionsRequest buildChargeOffRequest(final String transactionDate, final Long chargeOffReasonId,
            final String note, final String externalId) {
        final PostWorkingCapitalLoanTransactionsRequest request = workingCapitalProductRequestFactory
                .defaultWorkingCapitalLoanRepaymentRequest().transactionDate(transactionDate).note(note);
        if (chargeOffReasonId != null) {
            request.chargeOffReasonId(chargeOffReasonId);
        }
        if (externalId != null) {
            request.externalId(externalId);
        }
        return request;
    }

    @Then("Working Capital Loan transaction of type {string} on {string} has a non-blank externalId")
    public void verifyWorkingCapitalLoanTransactionExternalIdNonBlank(final String transactionType, final String transactionDate) {
        final Long loanId = getCreatedLoanId();
        final GetWorkingCapitalLoanTransactionsResponse response = retrieveLoanTransactions(loanId);
        final LocalDate expectedLocalDate = LocalDate.parse(transactionDate, FORMATTER);
        final GetWorkingCapitalLoanTransactionIdResponse transaction = response.getContent().stream() //
                .filter(t -> t.getType() != null && transactionType.equals(t.getType().getValue())) //
                .filter(t -> expectedLocalDate.equals(t.getTransactionDate())) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException(
                        String.format("Transaction of type %s on %s not found", transactionType, transactionDate)));
        assertThat(transaction.getExternalId()).as("Transaction externalId should not be blank").isNotBlank();
        log.info("Verified transaction of type {} on {} has a non-blank externalId", transactionType, transactionDate);
    }

    private Long resolveChargeOffReasonId(final String chargeOffReasonName) {
        final List<GetCodeValuesDataResponse> codeValues = fineractClient.codeValues()
                .retrieveAllCodeValuesByCodeName(CodeNames.CHARGE_OFF.getValue());
        return codeValues.stream().filter(v -> chargeOffReasonName.equals(v.getName())).map(GetCodeValuesDataResponse::getId).findFirst()
                .orElseThrow(() -> new IllegalStateException("Charge-off reason not found: " + chargeOffReasonName));
    }
}
