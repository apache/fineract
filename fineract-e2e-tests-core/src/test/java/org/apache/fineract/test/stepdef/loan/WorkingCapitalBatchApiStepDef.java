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

import com.google.gson.Gson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.Header;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import org.apache.fineract.test.data.workingcapitalproduct.WorkingCapitalLoanProductResolver;
import org.apache.fineract.test.factory.WorkingCapitalLoanRequestFactory;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@Slf4j
@RequiredArgsConstructor
public class WorkingCapitalBatchApiStepDef extends AbstractStepDef {

    private static final Header HEADER_JSON = new Header().name("Content-type").value("application/json");
    private static final String BATCH_API_METHOD_POST = "POST";
    private static final String BATCH_API_METHOD_PUT = "PUT";
    private static final String BATCH_API_METHOD_DELETE = "DELETE";
    private static final String BATCH_API_METHOD_GET = "GET";
    private static final String ENCLOSING_TRANSACTION = "enclosingTransaction";
    private static final String BODY_GET_REQUEST = "{}";
    private static final Gson GSON = new Gson();
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = org.apache.fineract.client.feign.ObjectMapperFactory
            .getShared();
    private static final String WCL_BASE_URL = "v1/working-capital-loans";
    private static final String WCL_EXTERNAL_ID_URL = WCL_BASE_URL + "/external-id/";
    private static final String COMMAND_APPROVE = "?command=approve";
    private static final String COMMAND_REJECT = "?command=reject";
    private static final String COMMAND_DISBURSE = "?command=disburse";
    private static final String COMMAND_DISCOUNT = "?command=discountFee";
    private static final String WCL_TRANSACTIONS_PATH = "/transactions";

    private final FineractFeignClient fineractFeignClient;
    private final WorkingCapitalLoanProductResolver workingCapitalLoanProductResolver;
    private final WorkingCapitalLoanRequestFactory workingCapitalLoanRequestFactory;

    // Individual operation steps implementation

    @When("Batch API creates a working capital loan with the following data:")
    public void batchApiCreateWCLoan(DataTable table) throws IOException {
        batchApiCreateWCLoanInternal(table, null);
    }

    @When("Batch API creates a working capital loan with external ID and the following data:")
    public void batchApiCreateWCLoanWithExternalId(DataTable table) throws IOException {
        final String externalId = UUID.randomUUID().toString();
        batchApiCreateWCLoanInternal(table, externalId);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_EXTERNAL_ID, externalId);
    }

    private void batchApiCreateWCLoanInternal(DataTable table, String externalId) throws IOException {
        final List<String> loanData = table.asLists().get(1);
        final Long clientId = extractClientId();
        final Long productId = workingCapitalLoanProductResolver.resolve(DefaultWorkingCapitalLoanProduct.valueOf(loanData.get(0)));

        final PostWorkingCapitalLoansRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(clientId)
                .productId(productId).submittedOnDate(loanData.get(1)).expectedDisbursementDate(loanData.get(2))
                .principalAmount(new BigDecimal(loanData.get(3))).totalPaymentVolume(new BigDecimal(loanData.get(4)))
                .periodPaymentRate(new BigDecimal(loanData.get(5))).discount(new BigDecimal(loanData.get(6)));
        if (externalId != null) {
            request.externalId(externalId);
        }

        final BatchRequest batchRequest = buildBatchRequest(1L, null, "v1/working-capital-loans", BATCH_API_METHOD_POST,
                GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);

        storeCreatedLoanIdFromResponse(responses.get(0));
    }

    private void storeCreatedLoanIdFromResponse(BatchResponse response) {
        final Long loanId = extractResourceId(response.getBody());
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_ID, loanId);
        testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_CREATE_RESPONSE, response);
        final PostWorkingCapitalLoansResponse loanResponse = new PostWorkingCapitalLoansResponse().loanId(loanId).resourceId(loanId);
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, loanResponse);
    }

    @When("Batch API modifies the working capital loan principal to {string} by loan ID")
    public void batchApiModifyWCLoanById(String principal) throws IOException {
        batchApiModifyWCLoanInternal(principal, resolveLoanUrlForGet());
    }

    @When("Batch API modifies the working capital loan principal to {string} by external ID")
    public void batchApiModifyWCLoanByExternalId(String principal) throws IOException {
        batchApiModifyWCLoanInternal(principal, resolveLoanUrlForGetByExternalId());
    }

    private void batchApiModifyWCLoanInternal(String principal, String relativeUrl) throws IOException {
        final PutWorkingCapitalLoansLoanIdRequest request = workingCapitalLoanRequestFactory.defaultModifyWorkingCapitalLoansRequest()
                .principalAmount(new BigDecimal(principal));
        final BatchRequest batchRequest = buildBatchRequest(1L, null, relativeUrl, BATCH_API_METHOD_PUT, GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API approves the working capital loan on {string} with {string} amount and expected disbursement date on {string}")
    public void batchApiApproveWCLoan(String approvedOnDate, String approvedAmount, String expectedDisbursementDate) throws IOException {
        batchApiApproveWCLoanInternal(approvedOnDate, approvedAmount, expectedDisbursementDate, resolveLoanUrl(COMMAND_APPROVE));
    }

    @When("Batch API approves the working capital loan by external ID on {string} with {string} amount and expected disbursement date on {string}")
    public void batchApiApproveWCLoanByExternalId(String approvedOnDate, String approvedAmount, String expectedDisbursementDate)
            throws IOException {
        batchApiApproveWCLoanInternal(approvedOnDate, approvedAmount, expectedDisbursementDate,
                resolveLoanUrlByExternalId(COMMAND_APPROVE));
    }

    private void batchApiApproveWCLoanInternal(String approvedOnDate, String approvedAmount, String expectedDisbursementDate,
            String relativeUrl) throws IOException {
        final PostWorkingCapitalLoansLoanIdRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanApproveRequest()
                .approvedOnDate(approvedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .approvedLoanAmount(new BigDecimal(approvedAmount));
        final BatchRequest batchRequest = buildBatchRequest(1L, null, relativeUrl, BATCH_API_METHOD_POST, GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
        final Long loanId = getCreatedWCLoanId();
        final PostWorkingCapitalLoansLoanIdResponse approveResponse = new PostWorkingCapitalLoansLoanIdResponse().loanId(loanId)
                .resourceId(loanId);
        testContext().set(TestContextKey.LOAN_APPROVAL_RESPONSE, approveResponse);
    }

    @When("Batch API disburses the working capital loan on {string} with {string} EUR transaction amount")
    public void batchApiDisburseWCLoan(String actualDisbursementDate, String transactionAmount) throws IOException {
        batchApiDisburseWCLoanInternal(actualDisbursementDate, transactionAmount, resolveLoanUrl(COMMAND_DISBURSE));
    }

    @When("Batch API disburses the working capital loan by external ID on {string} with {string} EUR transaction amount")
    public void batchApiDisburseWCLoanByExternalId(String actualDisbursementDate, String transactionAmount) throws IOException {
        batchApiDisburseWCLoanInternal(actualDisbursementDate, transactionAmount, resolveLoanUrlByExternalId(COMMAND_DISBURSE));
    }

    private void batchApiDisburseWCLoanInternal(String actualDisbursementDate, String transactionAmount, String relativeUrl)
            throws IOException {
        final PostWorkingCapitalLoansLoanIdRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanDisburseRequest()
                .actualDisbursementDate(actualDisbursementDate).transactionAmount(new BigDecimal(transactionAmount));
        final BatchRequest batchRequest = buildBatchRequest(1L, null, relativeUrl, BATCH_API_METHOD_POST, GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);

        final Long resourceId = extractResourceId(responses.get(0).getBody());
        if (resourceId != null) {
            final PostWorkingCapitalLoansLoanIdResponse disburseResponse = new PostWorkingCapitalLoansLoanIdResponse()
                    .resourceId(resourceId);
            testContext().set(TestContextKey.LOAN_DISBURSE_RESPONSE, disburseResponse);
        }
    }

    @When("Batch API rejects the working capital loan on {string}")
    public void batchApiRejectWCLoan(String rejectedOnDate) throws IOException {
        final Long loanId = getCreatedWCLoanId();
        final PostWorkingCapitalLoansLoanIdRequest request = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoanRejectRequest()
                .rejectedOnDate(rejectedOnDate);
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveLoanUrl(COMMAND_REJECT), BATCH_API_METHOD_POST,
                GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
        final PostWorkingCapitalLoansLoanIdResponse rejectResponse = new PostWorkingCapitalLoansLoanIdResponse().loanId(loanId)
                .resourceId(loanId);
        testContext().set(TestContextKey.LOAN_REJECT_RESPONSE, rejectResponse);
    }

    @When("Batch API deletes the working capital loan by loan ID")
    public void batchApiDeleteWCLoanById() throws IOException {
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveLoanUrlForGet(), BATCH_API_METHOD_DELETE, BODY_GET_REQUEST);
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API adds discount fee with {string} amount on the working capital loan")
    public void batchApiAddDiscountFee(String amount) throws IOException {
        final Long transactionId = getDisburseTransactionId();
        final PostWorkingCapitalLoanTransactionsRequest request = new PostWorkingCapitalLoanTransactionsRequest()
                .transactionDate("01 January 2026").transactionAmount(new BigDecimal(amount)).relatedResourceId(transactionId)
                .locale(WorkingCapitalLoanRequestFactory.DEFAULT_LOCALE).dateFormat(WorkingCapitalLoanRequestFactory.DATE_FORMAT);
        final String url = resolveLoanUrlForGet() + WCL_TRANSACTIONS_PATH + COMMAND_DISCOUNT;
        final BatchRequest batchRequest = buildBatchRequest(1L, null, url, BATCH_API_METHOD_POST, GSON.toJson(request));
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API fetches working capital loan details by loan ID")
    public void batchApiGetWCLoanById() throws IOException {
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveLoanUrlForGet(), BATCH_API_METHOD_GET, BODY_GET_REQUEST);
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API fetches working capital loan details by external ID")
    public void batchApiGetWCLoanByExternalId() throws IOException {
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveLoanUrlForGetByExternalId(), BATCH_API_METHOD_GET,
                BODY_GET_REQUEST);
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API fetches working capital loan disbursement transaction by loan ID")
    public void batchApiGetWCLoanDisbursementTransaction() throws IOException {
        final Long transactionId = getDisburseTransactionId();
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveTransactionUrl(transactionId), BATCH_API_METHOD_GET,
                BODY_GET_REQUEST);
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    @When("Batch API fetches working capital loan disbursement transaction by external ID")
    public void batchApiGetWCLoanDisbursementTransactionByExternalId() throws IOException {
        final Long transactionId = getDisburseTransactionId();
        final BatchRequest batchRequest = buildBatchRequest(1L, null, resolveTransactionUrlByExternalId(transactionId),
                BATCH_API_METHOD_GET, BODY_GET_REQUEST);
        final List<BatchResponse> responses = handleBatchRequests(List.of(batchRequest), false);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    private Long getDisburseTransactionId() {
        final PostWorkingCapitalLoansLoanIdResponse disburseResponse = testContext().get(TestContextKey.LOAN_DISBURSE_RESPONSE);
        return disburseResponse != null ? disburseResponse.getResourceId() : null;
    }

    // Helper methods

    private List<BatchResponse> handleBatchRequests(List<BatchRequest> requests, boolean enclosingTransaction) throws IOException {
        final Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(ENCLOSING_TRANSACTION, enclosingTransaction);
        return fineractFeignClient.batch().handleBatchRequests(requests, queryParams);
    }

    private Long getCreatedWCLoanId() {
        final PostWorkingCapitalLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        return loanResponse.getLoanId();
    }

    private static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error deserializing JSON to object", e);
        }
    }

    private Long extractClientId() {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        return clientResponse != null ? clientResponse.getClientId() : null;
    }

    private Long extractResourceId(String jsonBody) {
        if (jsonBody == null || jsonBody.isEmpty()) {
            return null;
        }
        try {
            final Map<String, Object> result = GSON.fromJson(jsonBody, Map.class);
            final Object resourceId = result.get("resourceId");
            if (resourceId instanceof Double) {
                return ((Double) resourceId).longValue();
            } else if (resourceId instanceof Number) {
                return ((Number) resourceId).longValue();
            }
        } catch (Exception e) {
            log.warn("Failed to extract resourceId from: {}", jsonBody);
        }
        return null;
    }

    private String resolveLoanUrl(String command) {
        return WCL_BASE_URL + "/" + getCreatedWCLoanId() + command;
    }

    private String resolveLoanUrlByExternalId(String command) {
        final String externalId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_EXTERNAL_ID);
        return WCL_EXTERNAL_ID_URL + externalId + command;
    }

    private String resolveLoanUrlForGet() {
        return WCL_BASE_URL + "/" + getCreatedWCLoanId();
    }

    private String resolveLoanUrlForGetByExternalId() {
        final String externalId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_EXTERNAL_ID);
        return WCL_EXTERNAL_ID_URL + externalId;
    }

    private String resolveTransactionUrl(Long transactionId) {
        return WCL_BASE_URL + "/" + getCreatedWCLoanId() + WCL_TRANSACTIONS_PATH + "/" + transactionId;
    }

    private String resolveTransactionUrlByExternalId(Long transactionId) {
        final String externalId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_EXTERNAL_ID);
        return WCL_EXTERNAL_ID_URL + externalId + WCL_TRANSACTIONS_PATH + "/" + transactionId;
    }

    // Combined workflow steps

    @When("Batch API call with working capital steps: {string} runs with enclosingTransaction: {string}")
    public void batchApiWCCallWithSteps(String steps, String enclosingTransaction) throws IOException {
        batchApiWCCallWithStepsInternal(steps, enclosingTransaction, null);
    }

    @When("Batch API call with working capital steps: {string} runs with enclosingTransaction: {string} and loan data:")
    public void batchApiWCCallWithStepsAndLoanData(String steps, String enclosingTransaction, DataTable loanData) throws IOException {
        batchApiWCCallWithStepsInternal(steps, enclosingTransaction, loanData.asMaps().get(0));
    }

    private void batchApiWCCallWithStepsInternal(String steps, String enclosingTransaction, Map<String, String> loanData)
            throws IOException {
        final String[] stepArray = steps.split(", ", -1);
        final List<BatchRequest> requestList = new ArrayList<>();
        final String idempotencyKey = UUID.randomUUID().toString();

        long requestId = 1L;
        Long previousRequestId = null;

        for (final String step : stepArray) {
            final BatchRequest request = createWCStepRequest(requestId, previousRequestId, step.trim(), idempotencyKey, loanData);
            if (request != null) {
                requestList.add(request);
                previousRequestId = requestId;
                requestId++;
            }
        }

        final Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        final List<BatchResponse> responses = handleBatchRequests(requestList, isEnclosingTransaction);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);

        storeLoanIdFromBatchResponse(stepArray, responses);
    }

    private void storeLoanIdFromBatchResponse(String[] stepArray, List<BatchResponse> responses) {
        final List<String> stepList = List.of(stepArray);
        final int wcLoanIndex = stepList.indexOf("createWCLoan");
        if (wcLoanIndex >= 0 && wcLoanIndex < responses.size()) {
            final Long loanId = extractResourceId(responses.get(wcLoanIndex).getBody());
            if (loanId != null) {
                testContext().set(TestContextKey.WORKING_CAPITAL_LOAN_ID, loanId);
                testContext().set(TestContextKey.LOAN_CREATE_RESPONSE,
                        new PostWorkingCapitalLoansResponse().loanId(loanId).resourceId(loanId));
            }
        }
    }

    @When("Batch API call with working capital steps by external IDs: {string} runs with enclosingTransaction: {string}")
    public void batchApiWCCallWithStepsByExternalId(String steps, String enclosingTransaction) throws IOException {
        batchApiWCCallWithStepsByExternalIdInternal(steps, enclosingTransaction, null, null);
    }

    @When("Batch API call with working capital steps by external IDs: {string} runs with enclosingTransaction: {string} and loan data:")
    public void batchApiWCCallWithStepsByExternalIdAndLoanData(String steps, String enclosingTransaction, DataTable loanData)
            throws IOException {
        batchApiWCCallWithStepsByExternalIdInternal(steps, enclosingTransaction, loanData.asMaps().get(0), null);
    }

    private void batchApiWCCallWithStepsByExternalIdInternal(String steps, String enclosingTransaction, Map<String, String> loanData,
            String preCreatedClientExternalId) throws IOException {
        final String[] stepArray = steps.split(", ", -1);
        final List<BatchRequest> requestList = new ArrayList<>();
        final String idempotencyKey = UUID.randomUUID().toString();
        final String clientExternalId = preCreatedClientExternalId != null ? preCreatedClientExternalId : UUID.randomUUID().toString();
        final String loanExternalId = UUID.randomUUID().toString();

        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);

        long requestId = 1L;
        Long previousRequestId = null;

        for (final String step : stepArray) {
            final BatchRequest request = createWCStepRequestByExternalId(requestId, previousRequestId, step.trim(), idempotencyKey,
                    clientExternalId, loanExternalId, loanData);
            if (request != null) {
                requestList.add(request);
                previousRequestId = requestId;
                requestId++;
            }
        }

        final Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        final List<BatchResponse> responses = handleBatchRequests(requestList, isEnclosingTransaction);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);

        storeLoanIdFromBatchResponse(stepArray, responses);
    }

    @When("Batch API call with working capital steps: {string} runs with enclosingTransaction: {string}, with failed disburse step")
    public void batchApiWCCallWithFailedStep(String steps, String enclosingTransaction) throws IOException {
        batchApiWCCallWithFailedStepInternal(steps, enclosingTransaction, null);
    }

    @When("Batch API call with working capital steps: {string} runs with enclosingTransaction: {string}, with failed disburse step and loan data:")
    public void batchApiWCCallWithFailedStepAndLoanData(String steps, String enclosingTransaction, DataTable loanData) throws IOException {
        batchApiWCCallWithFailedStepInternal(steps, enclosingTransaction, loanData.asMaps().get(0));
    }

    private void batchApiWCCallWithFailedStepInternal(String steps, String enclosingTransaction, Map<String, String> loanData)
            throws IOException {
        final String[] stepArray = steps.split(", ", -1);
        final List<BatchRequest> requestList = new ArrayList<>();
        final String idempotencyKey = UUID.randomUUID().toString();

        long requestId = 1L;
        Long previousRequestId = null;

        for (final String step : stepArray) {
            final BatchRequest request;
            if ("disburseWCLoan".equals(step.trim())) {
                request = createFailedDisburseRequest(requestId, previousRequestId);
            } else {
                request = createWCStepRequest(requestId, previousRequestId, step.trim(), idempotencyKey, loanData);
            }
            if (request != null) {
                requestList.add(request);
                previousRequestId = requestId;
                requestId++;
            }
        }

        final Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        final List<BatchResponse> responses = handleBatchRequests(requestList, isEnclosingTransaction);
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, responses);
    }

    // Verification steps

    @Then("Working capital loan no longer exists")
    public void verifyWCLoanNoLongerExists() {
        final Long loanId = getCreatedWCLoanId();
        final CallFailedRuntimeException exception = fail(
                () -> fineractFeignClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
        assertThat(exception.getStatus()).as("Loan should not be found after deletion").isEqualTo(404);
    }

    @Then("Batch API response contains working capital loan with the correct data:")
    public void verifyBatchResponseWCLoanData(final DataTable table) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).isNotEmpty();

        final GetWorkingCapitalLoansLoanIdResponse loan = fromJson(responses.get(0).getBody(), GetWorkingCapitalLoansLoanIdResponse.class);

        final List<String> header = table.row(0);
        final List<String> expectedValues = table.row(1);
        final List<String> actualValues = extractLoanFieldValues(loan, header);

        assertThat(actualValues).as("Batch API working capital loan data should match expected values").isEqualTo(expectedValues);
    }

    private List<String> extractLoanFieldValues(GetWorkingCapitalLoansLoanIdResponse loan, List<String> fields) {
        final List<String> values = new ArrayList<>();
        for (final String field : fields) {
            switch (field) {
                case "product.name" -> values.add(loan.getLoanProductName());
                case "submittedOnDate" -> values.add(loan.getTimeline() == null || loan.getTimeline().getSubmittedOnDate() == null ? null
                        : loan.getTimeline().getSubmittedOnDate().toString());
                case "expectedDisbursementDate" ->
                    values.add(loan.getDisbursementDetails() == null || loan.getDisbursementDetails().isEmpty() ? null
                            : loan.getDisbursementDetails().getFirst().getExpectedDisbursementDate().toString());
                case "status" -> values.add(loan.getStatus() == null ? null : loan.getStatus().getValue());
                case "proposedPrincipal" -> values.add(loan.getProposedPrincipal() == null ? null
                        : new Utils.DoubleFormatter(loan.getProposedPrincipal().doubleValue()).format());
                case "principal" -> values.add(loan.getBalance() == null || loan.getBalance().getPrincipal() == null ? null
                        : new Utils.DoubleFormatter(loan.getBalance().getPrincipal().doubleValue()).format());
                case "approvedPrincipal" -> values.add(loan.getApprovedPrincipal() == null ? "0"
                        : new Utils.DoubleFormatter(loan.getApprovedPrincipal().doubleValue()).format());
                case "totalPaymentVolume" -> values.add(loan.getTotalPaymentVolume() == null ? null
                        : new Utils.DoubleFormatter(loan.getTotalPaymentVolume().doubleValue()).format());
                case "periodPaymentRate" -> values.add(
                        loan.getPaymentRate() == null ? null : new Utils.DoubleFormatter(loan.getPaymentRate().doubleValue()).format());
                case "discount" -> values.add(
                        loan.getDiscountFee() == null ? "null" : new Utils.DoubleFormatter(loan.getDiscountFee().doubleValue()).format());
                case "discountProposed" -> values.add(loan.getProposedDiscountFee() == null ? "null"
                        : new Utils.DoubleFormatter(loan.getProposedDiscountFee().doubleValue()).format());
                case "discountApproved" -> values.add(loan.getApprovedDiscountFee() == null ? "null"
                        : new Utils.DoubleFormatter(loan.getApprovedDiscountFee().doubleValue()).format());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", field));
            }
        }
        return values;
    }

    @Then("Batch API response contains working capital transaction with the correct data:")
    public void verifyBatchResponseWCTransactionData(final DataTable table) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).isNotEmpty();

        final GetWorkingCapitalLoanTransactionIdResponse txn = fromJson(responses.get(responses.size() - 1).getBody(),
                GetWorkingCapitalLoanTransactionIdResponse.class);

        final List<String> header = table.row(0);
        final List<String> expectedValues = table.row(1);
        final List<String> actualValues = new ArrayList<>();

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        for (final String field : header) {
            switch (field) {
                case "transactionDate" ->
                    actualValues.add(txn.getTransactionDate() == null ? null : formatter.format(txn.getTransactionDate()));
                case "type" -> actualValues.add(txn.getType() == null ? null : txn.getType().getValue());
                case "transactionAmount" -> actualValues.add(txn.getTransactionAmount() == null ? null
                        : new Utils.DoubleFormatter(txn.getTransactionAmount().doubleValue()).format());
                case "principalPortion" -> actualValues.add(txn.getPrincipalPortion() == null ? null
                        : new Utils.DoubleFormatter(txn.getPrincipalPortion().doubleValue()).format());
                case "feeChargesPortion" -> actualValues.add(txn.getFeeChargesPortion() == null ? null
                        : new Utils.DoubleFormatter(txn.getFeeChargesPortion().doubleValue()).format());
                case "penaltyChargesPortion" -> actualValues.add(txn.getPenaltyChargesPortion() == null ? null
                        : new Utils.DoubleFormatter(txn.getPenaltyChargesPortion().doubleValue()).format());
                case "reversed" -> actualValues.add(txn.getReversed() == null ? null : txn.getReversed().toString());
                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", field));
            }
        }

        assertThat(actualValues).as("Batch API working capital transaction data should match expected values").isEqualTo(expectedValues);
    }

    @Then("Batch API response contains {string} transaction")
    public void verifyBatchResponseContainsTransaction(String transactionType) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).isNotEmpty();

        final BatchResponse response = responses.get(0);
        final String body = response.getBody();
        assertThat(body).contains("\"type\":\"" + transactionType + "\"");
    }

    @Then("Batch API response contains transaction with external ID {string}")
    public void verifyBatchResponseContainsTransactionWithExternalId(String externalId) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).isNotEmpty();

        final BatchResponse response = responses.get(0);
        final String body = response.getBody();
        assertThat(body).contains("\"externalId\":\"" + externalId + "\"");
    }

    @Then("Batch API response contains working capital loan details")
    public void verifyBatchResponseContainsWCLoanDetails() {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).isNotEmpty();

        final BatchResponse response = responses.get(responses.size() - 1);
        assertThat(response.getStatusCode()).isEqualTo(200);
        final String body = response.getBody();
        assertThat(body).contains("\"id\":");
        assertThat(body).contains("\"principal\":");
    }

    @Then("Batch API response contains working capital loan details with the correct data:")
    public void verifyBatchResponseContainsWCLoanDetailsWithData(final DataTable table) {
        verifyBatchResponseContainsWCLoanDetails();

        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        final GetWorkingCapitalLoansLoanIdResponse loan = fromJson(responses.get(responses.size() - 1).getBody(),
                GetWorkingCapitalLoansLoanIdResponse.class);

        final List<String> header = table.row(0);
        final List<String> expectedValues = table.row(1);
        final List<String> actualValues = extractLoanFieldValues(loan, header);

        assertThat(actualValues).as("Batch API working capital loan details data should match expected values").isEqualTo(expectedValues);
    }

    @Then("Verify that WCL step {int} results 200")
    public void verifyStepResults200(int step) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        final BatchResponse response = responses.stream().filter(r -> r.getRequestId() == step).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Step %d is not found in batch responses", step)));
        assertThat(response.getStatusCode()).as("Step %d should result 200", step).isEqualTo(200);
    }

    @Then("Verify that WCL step {int} throws an error with error code {int}")
    public void verifyWCStepThrowsErrorWithCode(int step, int errorCode) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        final BatchResponse response = responses.stream().filter(r -> r.getRequestId() == step).findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Step %d is not found in batch responses", step)));
        assertThat(response.getStatusCode()).as("Step %d should result %d", step, errorCode).isEqualTo(errorCode);
    }

    @Then("Nr. {int} Working capital loan was created")
    public void verifyWCLoanCreated(int index) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).hasSizeGreaterThanOrEqualTo(index);

        final BatchResponse response = responses.get(index - 1);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Then("Nr. {int} Working capital loan was approved")
    public void verifyWCLoanApproved(int index) {
        final List<BatchResponse> responses = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        assertThat(responses).hasSizeGreaterThanOrEqualTo(index);

        final BatchResponse response = responses.get(index - 1);
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Then("Nr. {int} Working capital loan creation was rolled back")
    public void verifyWCLoanRolledBack(int index) {
        final Long loanId = testContext().get(TestContextKey.WORKING_CAPITAL_LOAN_ID);
        if (loanId != null) {
            try {
                ok(() -> fineractFeignClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
                log.info("Loan {} still exists after rollback check", loanId);
            } catch (Exception e) {
                log.info("Loan does not exist as expected after rollback");
            }
        }
    }

    // Step request helpers

    private BatchRequest createWCStepRequest(Long requestId, Long reference, String step, String idempotencyKey,
            Map<String, String> loanData) {
        return switch (step) {
            case "createClient" -> createClientBatchRequest(requestId, idempotencyKey, UUID.randomUUID().toString());
            case "createWCLoan" -> createWCLoanBatchRequest(requestId, reference, null, loanData);
            case "approveWCLoan" ->
                createApproveWCLoanBatchRequest(requestId, reference, "v1/working-capital-loans/$.resourceId?command=approve");
            case "disburseWCLoan" ->
                createDisburseWCLoanBatchRequest(requestId, reference, "v1/working-capital-loans/$.resourceId?command=disburse");
            case "getWCLoanDetails" ->
                buildBatchRequest(requestId, reference, "v1/working-capital-loans/$.resourceId", BATCH_API_METHOD_GET, BODY_GET_REQUEST);
            case "getDisbursementTransaction" -> buildBatchRequest(requestId, reference,
                    "v1/working-capital-loans/$.loanId/transactions/$.resourceId", BATCH_API_METHOD_GET, BODY_GET_REQUEST);
            case "addDiscountFee" ->
                createDiscountFeeBatchRequest(requestId, reference, "v1/working-capital-loans/$.loanId/transactions?command=discountFee");
            default -> null;
        };
    }

    private BatchRequest createWCStepRequestByExternalId(Long requestId, Long reference, String step, String idempotencyKey,
            String clientExternalId, String loanExternalId, Map<String, String> loanData) {
        return switch (step) {
            case "createClient" -> createClientBatchRequest(requestId, idempotencyKey, clientExternalId);
            case "createWCLoan" -> createWCLoanBatchRequest(requestId, reference, loanExternalId, loanData);
            case "approveWCLoan" -> createApproveWCLoanBatchRequest(requestId, reference,
                    "v1/working-capital-loans/external-id/" + loanExternalId + "?command=approve");
            case "disburseWCLoan" -> createDisburseWCLoanBatchRequest(requestId, reference,
                    "v1/working-capital-loans/external-id/" + loanExternalId + "?command=disburse");
            case "getWCLoanDetails" -> buildBatchRequest(requestId, reference, "v1/working-capital-loans/external-id/" + loanExternalId,
                    BATCH_API_METHOD_GET, BODY_GET_REQUEST);
            case "getDisbursementTransaction" -> buildBatchRequest(requestId, reference,
                    "v1/working-capital-loans/$.loanId/transactions/$.resourceId", BATCH_API_METHOD_GET, BODY_GET_REQUEST);
            default -> null;
        };
    }

    private BatchRequest createClientBatchRequest(Long requestId, String idempotencyKey, String clientExternalId) {
        final Set<Header> headers = new HashSet<>();
        headers.add(HEADER_JSON);
        headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));
        final BatchRequest request = new BatchRequest();
        request.requestId(requestId);
        request.relativeUrl("clients");
        request.method(BATCH_API_METHOD_POST);
        request.headers(headers);
        request.body("{\"officeId\":1,\"firstname\":\"Test\",\"lastname\":\"Client\",\"externalId\":\"" + clientExternalId
                + "\",\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"active\":false,\"submittedOnDate\":\"01 January 2026\"}");
        return request;
    }

    private BatchRequest createWCLoanBatchRequest(Long requestId, Long reference, String loanExternalId, Map<String, String> loanData) {
        final Long productId = workingCapitalLoanProductResolver.resolve(DefaultWorkingCapitalLoanProduct.WCLP);
        final String submittedOnDate = loanData != null ? loanData.get("submittedOnDate") : "01 January 2026";
        final String expectedDisbursementDate = loanData != null ? loanData.get("expectedDisbursementDate") : "01 January 2026";
        final String principalAmount = loanData != null ? loanData.get("principalAmount") : "9000";
        final String totalPaymentVolume = loanData != null ? loanData.get("totalPaymentVolume") : "100000";
        final String periodPaymentRate = loanData != null ? loanData.get("periodPaymentRate") : "18";
        final String discount = loanData != null ? loanData.get("discount") : "0";
        final PostWorkingCapitalLoansRequest wcRequest = workingCapitalLoanRequestFactory.defaultWorkingCapitalLoansRequest(1L)
                .productId(productId).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .principalAmount(new BigDecimal(principalAmount)).totalPaymentVolume(new BigDecimal(totalPaymentVolume))
                .periodPaymentRate(new BigDecimal(periodPaymentRate)).discount(new BigDecimal(discount));
        if (loanExternalId != null) {
            wcRequest.externalId(loanExternalId);
        }
        final String body;
        if (reference != null) {
            body = GSON.toJson(wcRequest).replace("\"clientId\":1", "\"clientId\":\"$.clientId\"");
        } else {
            final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
            body = GSON.toJson(wcRequest).replace("\"clientId\":1", "\"clientId\":" + clientResponse.getClientId());
        }
        return buildBatchRequest(requestId, reference, "v1/working-capital-loans", BATCH_API_METHOD_POST, body);
    }

    private BatchRequest createApproveWCLoanBatchRequest(Long requestId, Long reference, String relativeUrl) {
        final PostWorkingCapitalLoansLoanIdRequest approveRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanApproveRequest().approvedOnDate("01 January 2026").expectedDisbursementDate("01 January 2026")
                .approvedLoanAmount(new BigDecimal("9000"));
        return buildBatchRequest(requestId, reference, relativeUrl, BATCH_API_METHOD_POST, GSON.toJson(approveRequest));
    }

    private BatchRequest createDisburseWCLoanBatchRequest(Long requestId, Long reference, String relativeUrl) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest().actualDisbursementDate("01 January 2026")
                .transactionAmount(new BigDecimal("9000"));
        return buildBatchRequest(requestId, reference, relativeUrl, BATCH_API_METHOD_POST, GSON.toJson(disburseRequest));
    }

    private BatchRequest createDiscountFeeBatchRequest(Long requestId, Long reference, String relativeUrl) {
        final PostWorkingCapitalLoanTransactionsRequest discountRequest = new PostWorkingCapitalLoanTransactionsRequest()
                .transactionDate("01 January 2026").transactionAmount(new BigDecimal("100"))
                .locale(WorkingCapitalLoanRequestFactory.DEFAULT_LOCALE).dateFormat(WorkingCapitalLoanRequestFactory.DATE_FORMAT);
        final String serialized = GSON.toJson(discountRequest);
        final String body = serialized.substring(0, serialized.lastIndexOf('}')) + ",\"relatedResourceId\":\"$.resourceId\"}";
        return buildBatchRequest(requestId, reference, relativeUrl, BATCH_API_METHOD_POST, body);
    }

    private BatchRequest createFailedDisburseRequest(Long requestId, Long reference) {
        final PostWorkingCapitalLoansLoanIdRequest disburseRequest = workingCapitalLoanRequestFactory
                .defaultWorkingCapitalLoanDisburseRequest().actualDisbursementDate("01 January 2030")
                .transactionAmount(new BigDecimal("999999999"));
        return buildBatchRequest(requestId, reference, "v1/working-capital-loans/$.resourceId?command=disburse", BATCH_API_METHOD_POST,
                GSON.toJson(disburseRequest));
    }

    private BatchRequest buildBatchRequest(Long requestId, Long reference, String relativeUrl, String method, String body) {
        final BatchRequest request = new BatchRequest();
        request.requestId(requestId);
        request.relativeUrl(relativeUrl);
        request.method(method);
        if (reference != null) {
            request.reference(reference);
        }
        request.headers(Set.of(HEADER_JSON));
        request.body(body);
        return request;
    }
}
