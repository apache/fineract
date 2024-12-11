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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.test.stepdef.datatable.DatatablesStepDef.DATATABLE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.fineract.avro.loan.v1.LoanSchedulePeriodDataV1;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetUsersUserIdResponse;
import org.apache.fineract.client.models.Header;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostUsersResponse;
import org.apache.fineract.client.services.BatchApiApi;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.client.services.UsersApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.data.ChargeProductType;
import org.apache.fineract.test.data.LoanRescheduleErrorMessage;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.loan.LoanRescheduledDueAdjustScheduleEvent;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

@Slf4j
public class BatchApiStepDef extends AbstractStepDef {

    private static final Gson GSON = new JSON().getGson();
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String DEFAULT_LOCALE = "en";
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_1 = 1L;
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_2 = 2L;
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_3 = 3L;
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_4 = 4L;
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_5 = 5L;
    private static final Long BATCH_API_SAMPLE_REQUEST_ID_6 = 6L;
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_CLIENTS = "clients";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOANS = "loans";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOAN_RESCHEDULE = "rescheduleloans";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOAN_RESCHEDULE_APPROVE = "rescheduleloans/$.resourceId?command=approve";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOANS_CHARGES = "loans/$.loanId/charges";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOANS_APPROVE = "loans/$.loanId?command=approve";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOANS_DISBURSE = "loans/$.loanId?command=disburse";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_LOANS_REPAYMENT = "loans/$.loanId/transactions?command=repayment";
    private static final String BATCH_API_RELATIVE_URL_LOANS_APPLY_EXTERNAL_ID = "loans/external-id/$.resourceExternalId?command=approve";
    private static final String BATCH_API_RELATIVE_URL_GET_LOAN_DETAILS_EXTERNAL_ID = "loans/external-id/$.resourceExternalId";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES_QUERY = "/query?columnFilter=loan_id&valueFilter=0&resultColumns=loan_id";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES_UPDATE = "/$.[0].loan_id";
    private static final String BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES = "datatables/";
    private static final String BATCH_API_METHOD_POST = "POST";
    private static final String BATCH_API_METHOD_GET = "GET";
    private static final String BATCH_API_METHOD_PUT = "PUT";
    private static final Header HEADER = new Header().name("Content-type").value("text/html");
    private static final Header HEADER_JSON = new Header().name("Content-type").value("application/json");
    private static final String BODY_GET_REQUEST = "{}";
    private static final Long CHARGE_ID_NFS_FEE = ChargeProductType.LOAN_NSF_FEE.value;
    private static final String ERROR_DEVELOPER_MESSAGE = "The requested resource is not available.";
    private static final Integer ERROR_HTTP_404 = 404;
    private static final String ERROR_DEVELOPER_MESSAGE_CLIENT = "Client with identifier null does not exist";
    private static final String ERROR_DEVELOPER_MESSAGE_LOAN_EXTERNAL = "Loan with external identifier {externalId} does not exist";
    private static final String PWD_USER_WITH_ROLE = "1234567890Aa!";

    @Autowired
    private BatchApiApi batchApiApi;

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ClientRequestFactory clientRequestFactory;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private LoanRequestFactory loanRequestFactory;

    @Autowired
    private UsersApi usersApi;

    @When("Batch API sample call ran")
    public void runSampleBatchApiCall() throws IOException {
        List<BatchRequest> requestList = new ArrayList<>();
        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);

        // request 1 - create client
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();
        String bodyClientsRequest = GSON.toJson(clientsRequest);

        BatchRequest batchRequest1 = new BatchRequest();
        batchRequest1.requestId(BATCH_API_SAMPLE_REQUEST_ID_1);
        batchRequest1.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_CLIENTS);
        batchRequest1.method(BATCH_API_METHOD_POST);
        batchRequest1.headers(headers);
        batchRequest1.body(bodyClientsRequest);

        // request 2 - create Loan
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(1L);
        String bodyLoansRequest = GSON.toJson(loansRequest);
        String bodyLoansRequestMod = bodyLoansRequest.replace("\"clientId\":1", "\"clientId\":\"$.clientId\"");

        BatchRequest batchRequest2 = new BatchRequest();
        batchRequest2.requestId(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest2.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS);
        batchRequest2.method(BATCH_API_METHOD_POST);
        batchRequest2.headers(headers);
        batchRequest2.reference(BATCH_API_SAMPLE_REQUEST_ID_1);
        batchRequest2.body(bodyLoansRequestMod);

        // request 3 - charge Loan
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String dateOfCharge = formatter.format(Utils.now().minusMonths(1L).plusDays(1L));

        PostLoansLoanIdChargesRequest loanIdChargesRequest = new PostLoansLoanIdChargesRequest();
        loanIdChargesRequest.chargeId(CHARGE_ID_NFS_FEE);
        loanIdChargesRequest.amount(25D);
        loanIdChargesRequest.dueDate(dateOfCharge);
        loanIdChargesRequest.dateFormat(DATE_FORMAT);
        loanIdChargesRequest.locale(DEFAULT_LOCALE);
        String bodyLoanIdChargesRequest = GSON.toJson(loanIdChargesRequest);

        BatchRequest batchRequest3 = new BatchRequest();
        batchRequest3.requestId(BATCH_API_SAMPLE_REQUEST_ID_3);
        batchRequest3.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_CHARGES);
        batchRequest3.method(BATCH_API_METHOD_POST);
        batchRequest3.headers(headers);
        batchRequest3.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest3.body(bodyLoanIdChargesRequest);

        // request 4 - get charge data
        BatchRequest batchRequest4 = new BatchRequest();
        batchRequest4.requestId(BATCH_API_SAMPLE_REQUEST_ID_4);
        batchRequest4.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_CHARGES);
        batchRequest4.method(BATCH_API_METHOD_GET);
        batchRequest4.headers(headers);
        batchRequest4.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest4.body(BODY_GET_REQUEST);

        // build Batch Api request
        requestList.add(batchRequest1);
        requestList.add(batchRequest2);
        requestList.add(batchRequest3);
        requestList.add(batchRequest4);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, false).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
    }

    @When("Batch API call runs with idempotency key")
    public void runBatchAPIWithIdempotencyKey() throws IOException {
        List<BatchRequest> requestList = new ArrayList<>();
        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);

        // request 1 - create client
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();
        String bodyClientsRequest = GSON.toJson(clientsRequest);

        BatchRequest batchRequest1 = new BatchRequest();
        batchRequest1.requestId(BATCH_API_SAMPLE_REQUEST_ID_1);
        batchRequest1.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_CLIENTS);
        batchRequest1.method(BATCH_API_METHOD_POST);
        batchRequest1.headers(headers);
        batchRequest1.body(bodyClientsRequest);

        // request 2 - create Loan
        PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(1L);
        String bodyLoansRequest = GSON.toJson(loansRequest);
        String bodyLoansRequestMod = bodyLoansRequest.replace("\"clientId\":1", "\"clientId\":\"$.clientId\"");

        BatchRequest batchRequest2 = new BatchRequest();
        batchRequest2.requestId(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest2.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS);
        batchRequest2.method(BATCH_API_METHOD_POST);
        batchRequest2.headers(headers);
        batchRequest2.reference(BATCH_API_SAMPLE_REQUEST_ID_1);
        batchRequest2.body(bodyLoansRequestMod);

        // request 3 - approve Loan
        PostLoansLoanIdRequest loanApproveRequest = LoanRequestFactory.defaultLoanApproveRequest();
        String bodyLoanApproveRequest = GSON.toJson(loanApproveRequest);

        BatchRequest batchRequest3 = new BatchRequest();
        batchRequest3.requestId(BATCH_API_SAMPLE_REQUEST_ID_3);
        batchRequest3.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_APPROVE);
        batchRequest3.method(BATCH_API_METHOD_POST);
        batchRequest3.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest3.headers(headers);
        batchRequest3.body(bodyLoanApproveRequest);

        // request 4 - disburse Loan
        PostLoansLoanIdRequest loanDisburseRequest = LoanRequestFactory.defaultLoanDisburseRequest();
        String bodyLoanDisburseRequest = GSON.toJson(loanDisburseRequest);

        BatchRequest batchRequest4 = new BatchRequest();
        batchRequest4.requestId(BATCH_API_SAMPLE_REQUEST_ID_4);
        batchRequest4.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_DISBURSE);
        batchRequest4.method(BATCH_API_METHOD_POST);
        batchRequest4.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest4.headers(headers);
        batchRequest4.body(bodyLoanDisburseRequest);

        // request 5 - repayment with idempotency key
        PostLoansLoanIdTransactionsRequest loanRepaymentRequest1 = LoanRequestFactory.defaultRepaymentRequest();
        String bodyLoanRepaymentRequest1 = GSON.toJson(loanRepaymentRequest1);

        String idempotencyKey = UUID.randomUUID().toString();
        headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));

        BatchRequest batchRequest5 = new BatchRequest();
        batchRequest5.requestId(BATCH_API_SAMPLE_REQUEST_ID_5);
        batchRequest5.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_REPAYMENT);
        batchRequest5.method(BATCH_API_METHOD_POST);
        batchRequest5.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest5.headers(headers);
        batchRequest5.body(bodyLoanRepaymentRequest1);

        // request 6 - repayment with same idempotency key
        PostLoansLoanIdTransactionsRequest loanRepaymentRequest2 = LoanRequestFactory.defaultRepaymentRequest();
        String bodyLoanRepaymentRequest2 = GSON.toJson(loanRepaymentRequest2);

        BatchRequest batchRequest6 = new BatchRequest();
        batchRequest6.requestId(BATCH_API_SAMPLE_REQUEST_ID_6);
        batchRequest6.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS_REPAYMENT);
        batchRequest6.method(BATCH_API_METHOD_POST);
        batchRequest6.reference(BATCH_API_SAMPLE_REQUEST_ID_2);
        batchRequest6.headers(headers);
        batchRequest6.body(bodyLoanRepaymentRequest2);

        // build Batch Api request
        requestList.add(batchRequest1);
        requestList.add(batchRequest2);
        requestList.add(batchRequest3);
        requestList.add(batchRequest4);
        requestList.add(batchRequest5);
        requestList.add(batchRequest6);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, false).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
    }

    @When("Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: {string}")
    public void runBatchApiClientLoanApproveLoanDetails(String enclosingTransaction) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String clientExternalId = UUID.randomUUID().toString();
        String loanExternalId = UUID.randomUUID().toString();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createClient(1L, idempotencyKey, clientExternalId));
        requestList.add(createLoan(2L, 1L, idempotencyKey, loanExternalId));
        requestList.add(approveLoanByExternalId(3L, 2L, idempotencyKey));
        requestList.add(getLoanDetailsByExternalId(4L, 2L, idempotencyKey));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);
    }

    @When("Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: {string}, with failed approve step")
    public void runBatchApiClientLoanApproveLoanDetailsApproveFails(String enclosingTransaction) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String clientExternalId = UUID.randomUUID().toString();
        String loanExternalId = UUID.randomUUID().toString();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createClient(1L, idempotencyKey, clientExternalId));
        requestList.add(createLoan(2L, 1L, idempotencyKey, loanExternalId));
        requestList.add(approveLoanByExternalIdFail(3L, 2L, idempotencyKey, "approve-fail"));
        requestList.add(getLoanDetailsByExternalId(4L, 2L, idempotencyKey));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);
    }

    @When("Batch API call with steps done twice: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: {string}")
    public void runBatchApiTwiceClientLoanApproveLoanDetails(String enclosingTransaction) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String clientExternalId = UUID.randomUUID().toString();
        String loanExternalId = UUID.randomUUID().toString();
        String idempotencyKey2 = UUID.randomUUID().toString();
        String clientExternalId2 = UUID.randomUUID().toString();
        String loanExternalId2 = UUID.randomUUID().toString();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createClient(1L, idempotencyKey, clientExternalId));
        requestList.add(createLoan(2L, 1L, idempotencyKey, loanExternalId));
        requestList.add(approveLoanByExternalId(3L, 2L, idempotencyKey));
        requestList.add(getLoanDetailsByExternalId(4L, 2L, idempotencyKey));
        requestList.add(createClient(5L, idempotencyKey2, clientExternalId2));
        requestList.add(createLoan(6L, 5L, idempotencyKey2, loanExternalId2));
        requestList.add(approveLoanByExternalId(7L, 6L, idempotencyKey2));
        requestList.add(getLoanDetailsByExternalId(8L, 6L, idempotencyKey2));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY_2, idempotencyKey2);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID_2, clientExternalId2);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID_2, loanExternalId2);
    }

    @When("Batch API call with steps done twice: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: {string}, with failed approve step in second tree")
    public void runBatchApiTwiceClientLoanApproveLoanDetailsSecondApproveFails(String enclosingTransaction) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String clientExternalId = UUID.randomUUID().toString();
        String loanExternalId = UUID.randomUUID().toString();
        String idempotencyKey2 = UUID.randomUUID().toString();
        String clientExternalId2 = UUID.randomUUID().toString();
        String loanExternalId2 = UUID.randomUUID().toString();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createClient(1L, idempotencyKey, clientExternalId));
        requestList.add(createLoan(2L, 1L, idempotencyKey, loanExternalId));
        requestList.add(approveLoanByExternalId(3L, 2L, idempotencyKey));
        requestList.add(getLoanDetailsByExternalId(4L, 2L, idempotencyKey));
        requestList.add(createClient(5L, idempotencyKey2, clientExternalId2));
        requestList.add(createLoan(6L, 5L, idempotencyKey2, loanExternalId2));
        requestList.add(approveLoanByExternalIdFail(7L, 6L, idempotencyKey2, "approve-fail"));
        requestList.add(getLoanDetailsByExternalId(8L, 6L, idempotencyKey2));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();

        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY_2, idempotencyKey2);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID_2, clientExternalId2);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID_2, loanExternalId2);
    }

    @When("Batch API call with steps: createClient, createLoan, approveLoan, getLoanDetails runs with enclosingTransaction: {string}, and approveLoan is doubled")
    public void runBatchApiClientLoanApproveLoanDetailsApproveDoubled(String enclosingTransaction) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        String clientExternalId = UUID.randomUUID().toString();
        String loanExternalId = UUID.randomUUID().toString();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createClient(1L, idempotencyKey, clientExternalId));
        requestList.add(createLoan(2L, 1L, idempotencyKey, loanExternalId));
        requestList.add(approveLoanByExternalId(3L, 2L, idempotencyKey));
        requestList.add(approveLoanByExternalId(4L, 2L, idempotencyKey));
        requestList.add(getLoanDetailsByExternalId(5L, 2L, idempotencyKey));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        testContext().set(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID, clientExternalId);
        testContext().set(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID, loanExternalId);
    }

    @When("Batch API call with steps: rescheduleLoan from {string} to {string} submitted on date: {string}, approveReschedule on date: {string} runs with enclosingTransaction: {string}")
    public void runBatchApiCreateAndApproveLoanReschedule(String fromDateStr, String toDateStr, String submittedOnDate,
            String approvedOnDate, String enclosingTransaction) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String idempotencyKey = UUID.randomUUID().toString();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createLoanReschedule(1L, loanId, fromDateStr, toDateStr, submittedOnDate, idempotencyKey, null));
        requestList.add(approveLoanReschedule(2L, idempotencyKey, approvedOnDate, 1L));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        eventAssertion.assertEvent(LoanRescheduledDueAdjustScheduleEvent.class, loanId).extractingData(loanAccountDataV1 -> {
            LoanSchedulePeriodDataV1 period = loanAccountDataV1.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> formatter.format(LocalDate.parse(p.getDueDate())).equals(toDateStr)).findFirst().orElse(null);
            String dueDate = "";
            if (period != null) {
                dueDate = formatter.format(LocalDate.parse(period.getDueDate()));
            }
            assertThat(dueDate).as(ErrorMessageHelper.wrongDataInLastPaymentAmount(dueDate, toDateStr)).isEqualTo(toDateStr);
            return null;
        });
    }

    @When("Batch API call with created user and with steps: rescheduleLoan from {string} to {string} submitted on date: {string}, approveReschedule on date: {string} runs with enclosingTransaction: {string}")
    public void runBatchApiCreateAndApproveLoanRescheduleWithGivenUser(String fromDateStr, String toDateStr, String submittedOnDate,
            String approvedOnDate, String enclosingTransaction) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        String idempotencyKey = UUID.randomUUID().toString();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        Map<String, String> headerMap = new HashMap<>();

        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + PWD_USER_WITH_ROLE;
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(createLoanReschedule(1L, loanId, fromDateStr, toDateStr, submittedOnDate, idempotencyKey, null));
        requestList.add(approveLoanReschedule(2L, idempotencyKey, approvedOnDate, 1L));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction, headerMap)
                .execute();

        if (batchResponseList.errorBody() != null) {
            log.debug("ERROR: {}", batchResponseList.errorBody().string());

        }
        if (batchResponseList.body() != null) {
            log.debug("Body: {}", batchResponseList.body());
        }

        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
        eventAssertion.assertEvent(LoanRescheduledDueAdjustScheduleEvent.class, loanId).extractingData(loanAccountDataV1 -> {
            Optional<LoanSchedulePeriodDataV1> period = loanAccountDataV1.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> formatter.format(LocalDate.parse(p.getDueDate())).equals(toDateStr)).findFirst();
            String dueDate = "";
            if (period.isPresent()) {
                dueDate = formatter.format(LocalDate.parse(period.get().getDueDate()));
            }
            assertThat(dueDate).as(ErrorMessageHelper.wrongDataInLastPaymentAmount(dueDate, toDateStr)).isEqualTo(toDateStr);
            return null;
        });
    }

    @When("Batch API call with created user and the following data results a {int} error and a {string} error message:")
    public void runBatchApiCreateAndApproveLoanRescheduleWithGivenUserLockedByCobError(int errorCodeExpected, String errorMessageType,
            DataTable table) throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        LoanRescheduleErrorMessage loanRescheduleErrorMessage = LoanRescheduleErrorMessage.valueOf(errorMessageType);
        String errorMessageExpected = loanRescheduleErrorMessage.getValue(loanId);

        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);
        String fromDateStr = transferData.get(0);
        String submittedOnDate = transferData.get(1);
        String toDateStr = transferData.get(2);
        String approvedOnDate = transferData.get(3);
        String enclosingTransaction = transferData.get(4);

        Map<String, String> headerMap = new HashMap<>();

        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + PWD_USER_WITH_ROLE;
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        List<BatchRequest> requestList = new ArrayList<>();
        requestList.add(createLoanReschedule(1L, loanId, fromDateStr, toDateStr, submittedOnDate, idempotencyKey, null));
        requestList.add(approveLoanReschedule(2L, idempotencyKey, approvedOnDate, 1L));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction, headerMap)
                .execute();
        String errorToString = batchResponseList.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorToString, ErrorResponse.class);
        String errorMessageActual = errorResponse.getDeveloperMessage();
        Integer errorCodeActual = errorResponse.getHttpStatusCode();

        assertThat(errorCodeActual).as(ErrorMessageHelper.wrongErrorCode(errorCodeActual, errorCodeExpected)).isEqualTo(errorCodeExpected);
        assertThat(errorMessageActual).as(ErrorMessageHelper.wrongErrorMessage(errorMessageActual, errorMessageExpected))
                .isEqualTo(errorMessageExpected);

        log.debug("ERROR CODE: {}", errorCodeActual);
        log.debug("ERROR MESSAGE: {}", errorMessageActual);
    }

    @When("Batch API call with created user and the following data results a {int} statuscode WITHOUT error message:")
    public void runBatchApiCreateAndApproveLoanRescheduleWithGivenUserLockedByCobWithoutError(int httpCodeExpected, DataTable table)
            throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        Response<PostLoansResponse> loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.body().getLoanId();

        List<List<String>> data = table.asLists();
        List<String> transferData = data.get(1);
        String fromDateStr = transferData.get(0);
        String submittedOnDate = transferData.get(1);
        String toDateStr = transferData.get(2);
        String approvedOnDate = transferData.get(3);
        String enclosingTransaction = transferData.get(4);

        Map<String, String> headerMap = new HashMap<>();

        Response<PostUsersResponse> createUserResponse = testContext().get(TestContextKey.CREATED_SIMPLE_USER_RESPONSE);
        Long createdUserId = createUserResponse.body().getResourceId();
        Response<GetUsersUserIdResponse> user = usersApi.retrieveOne31(createdUserId).execute();
        ErrorHelper.checkSuccessfulApiCall(user);
        String authorizationString = user.body().getUsername() + ":" + PWD_USER_WITH_ROLE;
        Base64 base64 = new Base64();
        headerMap.put("Authorization",
                "Basic " + new String(base64.encode(authorizationString.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        List<BatchRequest> requestList = new ArrayList<>();
        requestList.add(createLoanReschedule(1L, loanId, fromDateStr, toDateStr, submittedOnDate, idempotencyKey, null));
        requestList.add(approveLoanReschedule(2L, idempotencyKey, approvedOnDate, 1L));

        Boolean isEnclosingTransaction = Boolean.valueOf(enclosingTransaction);
        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, isEnclosingTransaction, headerMap)
                .execute();
        BatchResponse lastBatchResponse = batchResponseList.body().get(batchResponseList.body().size() - 1);
        assertThat(httpCodeExpected).isEqualTo(lastBatchResponse.getStatusCode());
        // No error
        assertThat(batchResponseList.errorBody()).isEqualTo(null);
    }

    @When("Batch API call with steps: queryDatatable, updateDatatable runs, with empty queryDatatable response")
    public void runBatchApiQueryDatatableUpdateDatatable() throws IOException {
        String idempotencyKey = UUID.randomUUID().toString();
        List<BatchRequest> requestList = new ArrayList<>();

        requestList.add(queryDatatable(1L));
        requestList.add(updateDatatable(2L, 1L));

        Response<List<BatchResponse>> batchResponseList = batchApiApi.handleBatchRequests(requestList, false).execute();
        testContext().set(TestContextKey.BATCH_API_CALL_RESPONSE, batchResponseList);
        testContext().set(TestContextKey.BATCH_API_CALL_IDEMPOTENCY_KEY, idempotencyKey);
    }

    private BatchRequest createLoanReschedule(Long requestId, Long loanId, String fromDateStr, String toDateStr, String submittedOnDate,
            String idempotencyKey, Long referenceId) {
        PostCreateRescheduleLoansRequest rescheduleLoansRequest = LoanRequestFactory.defaultLoanRescheduleCreateRequest(loanId, fromDateStr,
                toDateStr);
        rescheduleLoansRequest.setSubmittedOnDate(submittedOnDate);
        String bodyLoanRescheduleRequest = GSON.toJson(rescheduleLoansRequest);

        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);
        if (idempotencyKey != null) {
            headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));
        }
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOAN_RESCHEDULE);
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.headers(headers);
        batchRequest.reference(referenceId);
        batchRequest.body(bodyLoanRescheduleRequest);

        return batchRequest;
    }

    private BatchRequest approveLoanReschedule(Long requestId, String idempotencyKey, String approvedOnDate, Long referenceId) {
        PostUpdateRescheduleLoansRequest rescheduleLoansRequest = LoanRequestFactory.defaultLoanRescheduleUpdateRequest();
        rescheduleLoansRequest.setApprovedOnDate(approvedOnDate);
        String bodyLoanRescheduleRequest = GSON.toJson(rescheduleLoansRequest);

        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);
        if (idempotencyKey != null) {
            headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));
        }
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOAN_RESCHEDULE_APPROVE);
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.headers(headers);
        batchRequest.reference(referenceId);
        batchRequest.body(bodyLoanRescheduleRequest);

        return batchRequest;
    }

    @Then("Admin checks that all steps result 200OK")
    public void adminChecksThatAllStepsResultOK() {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        ErrorHelper.checkSuccessfulBatchApiCall(batchResponseList);
    }

    @Then("Verify that step Nr. {int} results {int}")
    public void checkGivenStepResult(int nr, int resultStatusCode) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse stepResponse = batchResponseList.body().stream().filter(r -> r.getRequestId() == nr).findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Request id %s not found", nr)));

        assertThat(stepResponse.getStatusCode()).as(ErrorMessageHelper.wrongStatusCode(stepResponse.getStatusCode(), resultStatusCode))
                .isEqualTo(resultStatusCode);
    }

    @Then("Verify that step {int} throws an error with error code {int}")
    public void errorCodeInStep(int step, int errorCode) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse response = batchResponseList.body().stream().filter(r -> r.getRequestId() == step).findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("Step %s is not found", step)));
        ErrorResponse errorResponse = GSON.fromJson(response.getBody(), ErrorResponse.class);

        String developerMessageActual = errorResponse.getDeveloperMessage();
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();

        String developerMessageExpected = ERROR_DEVELOPER_MESSAGE;
        Integer httpStatusCodeExpected = ERROR_HTTP_404;

        assertThat(response.getStatusCode()).as(ErrorMessageHelper.wrongStatusCode(response.getStatusCode(), errorCode))
                .isEqualTo(errorCode);
        assertThat(developerMessageActual).as(ErrorMessageHelper.wrongErrorMessage(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);
        assertThat(httpStatusCodeActual).as(ErrorMessageHelper.wrongStatusCode(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
    }

    @Then("Admin checks that all steps result 200OK for Batch API idempotency request")
    public void adminChecksThatAllStepsResultOKIdempotency() {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        ErrorHelper.checkSuccessfulBatchApiCall(batchResponseList);
    }

    @Then("Batch API response has boolean value in header {string}: {string} in segment with requestId {int}")
    public void batchAPITransactionHeaderCheckBoolean(String headerKeyExpected, String headerValueExpected, int requestId) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse batchResponse = batchResponseList.body().get(requestId - 1);

        Set<Header> headers = batchResponse.getHeaders();
        List<Header> headersList = new ArrayList<>(Objects.requireNonNull(headers));
        String headerValueActual = getHeaderValueByHeaderKey(headersList, headerKeyExpected);

        assertThat(headerValueActual)
                .as(ErrorMessageHelper.wrongValueInResponseHeader(headerKeyExpected, headerValueActual, headerValueExpected))
                .isEqualTo(headerValueExpected);
    }

    @Then("Batch API response has no {string} field in segment with requestId {int}")
    public void batchAPITransactionHeaderCheckNoField(String headerKeyExpected, int requestId) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse batchResponse = batchResponseList.body().get(requestId - 1);

        Set<Header> headers = batchResponse.getHeaders();
        List<Header> headersList = new ArrayList<>(Objects.requireNonNull(headers));
        boolean hasHeaderKey = false;
        for (Header header : headersList) {
            if (headerKeyExpected.equals(header.getName())) {
                hasHeaderKey = true;
            }
        }

        assertThat(hasHeaderKey).isFalse();
    }

    @Then("Batch API response has {double} EUR value for transaction amount in segment with requestId {int}")
    public void batchAPITransactionAmountCheck(double transactionAmountExpected, int requestId) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse batchResponse = batchResponseList.body().get(requestId - 1);

        PostLoansLoanIdTransactionsResponse loanTransactionResponse = GSON.fromJson(batchResponse.getBody(),
                PostLoansLoanIdTransactionsResponse.class);
        Double transactionAmountActual = Double
                .valueOf(Objects.requireNonNull(Objects.requireNonNull(loanTransactionResponse.getChanges()).getTransactionAmount()));

        assertThat(transactionAmountActual)
                .as(ErrorMessageHelper.wrongAmountInTransactionsAmount(transactionAmountActual, transactionAmountExpected))
                .isEqualTo(transactionAmountExpected);
    }

    @Then("Batch API response has the same clientId and loanId in segment with requestId {int} as in segment with requestId {int}")
    public void batchAPIClientIdLoanIdCheck(int requestIdSecondTransaction, int requestIdFirstTransaction) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse batchResponseFirstTransaction = batchResponseList.body().get(requestIdFirstTransaction - 1);
        BatchResponse batchResponseSecondTransaction = batchResponseList.body().get(requestIdSecondTransaction - 1);

        PostLoansLoanIdTransactionsResponse loanTransactionResponseFirst = GSON.fromJson(batchResponseFirstTransaction.getBody(),
                PostLoansLoanIdTransactionsResponse.class);
        PostLoansLoanIdTransactionsResponse loanTransactionResponseSecond = GSON.fromJson(batchResponseSecondTransaction.getBody(),
                PostLoansLoanIdTransactionsResponse.class);

        Long clientIdFirstTransaction = loanTransactionResponseFirst.getClientId();
        Long clientIdSecondTransaction = loanTransactionResponseSecond.getClientId();

        Long loanIdFirstTransaction = loanTransactionResponseFirst.getLoanId();
        Long loanIdSecondTransaction = loanTransactionResponseSecond.getLoanId();

        assertThat(clientIdSecondTransaction)
                .as(ErrorMessageHelper.wrongClientIdInTransactionResponse(clientIdSecondTransaction, clientIdFirstTransaction))
                .isEqualTo(clientIdFirstTransaction);
        assertThat(loanIdSecondTransaction)
                .as(ErrorMessageHelper.wrongLoanIdInTransactionResponse(loanIdSecondTransaction, loanIdFirstTransaction))
                .isEqualTo(loanIdFirstTransaction);
    }

    @Then("Batch API response has the same idempotency key in segment with requestId {int} as in segment with requestId {int}")
    public void batchAPIIdempotencyKeyCheck(int requestIdSecondTransaction, int requestIdFirstTransaction) {
        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse batchResponseFirstTransaction = batchResponseList.body().get(requestIdFirstTransaction - 1);
        BatchResponse batchResponseSecondTransaction = batchResponseList.body().get(requestIdSecondTransaction - 1);

        Set<Header> headersFirstTransaction = batchResponseFirstTransaction.getHeaders();
        List<Header> headersListFirstTransaction = new ArrayList<>(Objects.requireNonNull(headersFirstTransaction));
        Set<Header> headersSecondTransaction = batchResponseSecondTransaction.getHeaders();
        List<Header> headersListSecondTransaction = new ArrayList<>(Objects.requireNonNull(headersSecondTransaction));

        String idempotencyKey = "Idempotency-Key";
        String idempotencyValueFirstTransaction = getHeaderValueByHeaderKey(headersListFirstTransaction, idempotencyKey);
        String idempotencyValueSecondTransaction = getHeaderValueByHeaderKey(headersListSecondTransaction, idempotencyKey);

        assertThat(idempotencyValueSecondTransaction)
                .as(ErrorMessageHelper.idempotencyKeyNoMatch(idempotencyValueSecondTransaction, idempotencyValueFirstTransaction))
                .isEqualTo(idempotencyValueFirstTransaction);
    }

    @Then("Loan has {int} {string} transactions on Transactions tab after Batch API run")
    public void checkNrOfTransactionsBatchApi(int nrOfTransactionsExpected, String transactionTypeInput) throws IOException {
        TransactionType transactionType = TransactionType.valueOf(transactionTypeInput);
        String transactionTypeValue = transactionType.getValue();

        Response<List<BatchResponse>> batchResponseList = testContext().get(TestContextKey.BATCH_API_CALL_RESPONSE);
        BatchResponse lastBatchResponse = batchResponseList.body().get(batchResponseList.body().size() - 1);
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = GSON.fromJson(lastBatchResponse.getBody(),
                PostLoansLoanIdTransactionsResponse.class);
        Long loanId = loanTransactionResponse.getLoanId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();

        List<GetLoansLoanIdTransactions> transactions = loanDetails.body().getTransactions();
        List<String> transactionsMatched = new ArrayList<>();

        transactions.forEach(t -> {
            String transactionTypeValueActual = t.getType().getCode();
            String transactionTypeValueExpected = "loanTransactionType." + transactionTypeValue;

            if (transactionTypeValueActual.equals(transactionTypeValueExpected)) {
                transactionsMatched.add(transactionTypeValueActual);
            }
        });

        int nrOfTransactionsActual = transactionsMatched.size();
        assertThat(nrOfTransactionsActual)
                .as(ErrorMessageHelper.wrongNrOfTransactions(transactionTypeInput, nrOfTransactionsActual, nrOfTransactionsExpected))
                .isEqualTo(nrOfTransactionsExpected);
    }

    @Then("Nr. {int} Client was created")
    public void givenClientCreated(int nr) throws IOException {
        String clientExternalId = "";
        if (nr == 1) {
            clientExternalId = testContext().get(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID);
        } else if (nr == 2) {
            clientExternalId = testContext().get(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID_2);
        } else {
            throw new IllegalStateException(String.format("Nr. %s client external ID not found", nr));
        }

        Response<GetClientsClientIdResponse> response = clientApi.retrieveOne12(clientExternalId, false).execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        assertThat(response.body().getId()).as(ErrorMessageHelper.idNull()).isNotNull();
    }

    @Then("Nr. {int} Loan was created")
    public void givenLoanCreated(int nr) throws IOException {
        String loanExternalId = "";
        if (nr == 1) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID);
        } else if (nr == 2) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID_2);
        } else {
            throw new IllegalStateException(String.format("Nr. %s loan external ID not found", nr));
        }

        Response<GetLoansLoanIdResponse> response = loansApi.retrieveLoan1(loanExternalId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(response);
        assertThat(response.body().getId()).as(ErrorMessageHelper.idNull()).isNotNull();
    }

    @Then("Nr. {int} Loan was approved")
    public void givenLoanApproved(int nr) throws IOException {
        String loanExternalId = "";
        if (nr == 1) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID);
        } else if (nr == 2) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID_2);
        } else {
            throw new IllegalStateException(String.format("Nr. %s loan external ID not found", nr));
        }

        Response<GetLoansLoanIdResponse> response = loansApi.retrieveLoan1(loanExternalId, false, "", "", "").execute();
        ErrorHelper.checkSuccessfulApiCall(response);

        GetLoansLoanIdStatus status = response.body().getStatus();
        Integer statusIdActual = status.getId();
        Integer statusIdExpected = LoanStatus.APPROVED.value;

        String resourceId = String.valueOf(response.body().getId());
        assertThat(statusIdActual).as(ErrorMessageHelper.wrongLoanStatus(resourceId, statusIdActual, statusIdExpected))
                .isEqualTo(statusIdExpected);
    }

    @Then("Nr. {int} Client creation was rolled back")
    public void clientNotCreated(int nr) throws IOException {
        String clientExternalId = "";
        if (nr == 1) {
            clientExternalId = testContext().get(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID);
        } else if (nr == 2) {
            clientExternalId = testContext().get(TestContextKey.BATCH_API_CALL_CLIENT_EXTERNAL_ID_2);
        } else {
            throw new IllegalStateException(String.format("Nr. %s client external id mot found", nr));
        }

        Response<GetClientsClientIdResponse> response = clientApi.retrieveOne12(clientExternalId, false).execute();
        ErrorResponse errorResponse = GSON.fromJson(response.errorBody().string(), ErrorResponse.class);
        String developerMessageActual = errorResponse.getDeveloperMessage();
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        String errorsDeveloperMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        String developerMessageExpected = ERROR_DEVELOPER_MESSAGE;
        Integer httpStatusCodeExpected = ERROR_HTTP_404;
        String errorsDeveloperMessageExpected = ERROR_DEVELOPER_MESSAGE_CLIENT;

        assertThat(developerMessageActual).as(ErrorMessageHelper.wrongErrorMessage(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);
        assertThat(httpStatusCodeActual).as(ErrorMessageHelper.wrongStatusCode(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(errorsDeveloperMessageActual)
                .as(ErrorMessageHelper.wrongErrorMessage(errorsDeveloperMessageActual, errorsDeveloperMessageExpected))
                .isEqualTo(errorsDeveloperMessageExpected);
    }

    @Then("Nr. {int} Loan creation was rolled back")
    public void loanNotCreated(int nr) throws IOException {
        String loanExternalId = "";
        if (nr == 1) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID);
        } else if (nr == 2) {
            loanExternalId = testContext().get(TestContextKey.BATCH_API_CALL_LOAN_EXTERNAL_ID_2);
        } else {
            throw new IllegalStateException(String.format("Nr. %s loan external id mot found", nr));
        }

        Response<GetLoansLoanIdResponse> response = loansApi.retrieveLoan1(loanExternalId, false, "", "", "").execute();

        ErrorResponse errorResponse = GSON.fromJson(response.errorBody().string(), ErrorResponse.class);
        String developerMessageActual = errorResponse.getDeveloperMessage();
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        String errorsDeveloperMessageActual = errorResponse.getErrors().get(0).getDeveloperMessage();

        String developerMessageExpected = ERROR_DEVELOPER_MESSAGE;
        Integer httpStatusCodeExpected = ERROR_HTTP_404;
        String errorsDeveloperMessageExpected = ERROR_DEVELOPER_MESSAGE_LOAN_EXTERNAL.replace("{externalId}", loanExternalId);

        assertThat(developerMessageActual).as(ErrorMessageHelper.wrongErrorMessage(developerMessageActual, developerMessageExpected))
                .isEqualTo(developerMessageExpected);
        assertThat(httpStatusCodeActual).as(ErrorMessageHelper.wrongStatusCode(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(errorsDeveloperMessageActual)
                .as(ErrorMessageHelper.wrongErrorMessage(errorsDeveloperMessageActual, errorsDeveloperMessageExpected))
                .isEqualTo(errorsDeveloperMessageExpected);
    }

    private String getHeaderValueByHeaderKey(List<Header> headersList, String headerKey) {
        for (Header header : headersList) {
            if (Objects.requireNonNull(header.getName()).equals(headerKey)) {
                return header.getValue();
            }
        }
        throw new Error(ErrorMessageHelper.noHeaderKeyFound(headersList, headerKey));
    }

    private BatchRequest createClient(Long requestId, String idempotencyKey, String clientExternalId) {
        PostClientsRequest clientsRequest = clientExternalId == null ? clientRequestFactory.defaultClientCreationRequest()
                : clientRequestFactory.defaultClientCreationRequest().externalId(clientExternalId);
        String bodyClientsRequest = GSON.toJson(clientsRequest);

        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);
        if (idempotencyKey != null) {
            headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));
        }

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_CLIENTS);
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.headers(headers);
        batchRequest.body(bodyClientsRequest);

        return batchRequest;
    }

    private BatchRequest createLoan(Long requestId, Long referenceId, String idempotencyKey, String loanExternalId) {
        PostLoansRequest loansRequest = loanExternalId == null ? loanRequestFactory.defaultLoansRequest(1L)
                : loanRequestFactory.defaultLoansRequest(1L).externalId(loanExternalId);
        String bodyLoansRequest = GSON.toJson(loansRequest);
        String bodyLoansRequestMod = bodyLoansRequest.replace("\"clientId\":1", "\"clientId\":\"$.clientId\"");

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_LOANS);
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.headers(setHeaders(idempotencyKey));
        batchRequest.reference(referenceId);
        batchRequest.body(bodyLoansRequestMod);

        return batchRequest;
    }

    private BatchRequest queryDatatable(Long requestId) {
        String datatableName = testContext().get(DATATABLE_NAME);

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES + datatableName + BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES_QUERY);
        batchRequest.method(BATCH_API_METHOD_GET);
        batchRequest.headers(Set.of(HEADER_JSON));
        batchRequest.body("{}");

        return batchRequest;
    }

    private BatchRequest updateDatatable(Long requestId, Long referenceId) {
        String datatableName = testContext().get(DATATABLE_NAME);

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest
                .relativeUrl(BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES + datatableName + BATCH_API_SAMPLE_RELATIVE_URL_DATATABLES_UPDATE);
        batchRequest.method(BATCH_API_METHOD_PUT);
        batchRequest.headers(Set.of(HEADER_JSON));
        batchRequest.reference(referenceId);
        batchRequest.body("{\"loan_id\": \"345\"}");

        return batchRequest;
    }

    private BatchRequest approveLoanByExternalId(Long requestId, Long referenceId, String idempotencyKey) {
        PostLoansLoanIdRequest loanApproveRequest = LoanRequestFactory.defaultLoanApproveRequest();
        String bodyLoanApproveRequest = GSON.toJson(loanApproveRequest);

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_RELATIVE_URL_LOANS_APPLY_EXTERNAL_ID);
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.reference(referenceId);
        batchRequest.headers(setHeaders(idempotencyKey));
        batchRequest.body(bodyLoanApproveRequest);

        return batchRequest;
    }

    private BatchRequest approveLoanByExternalIdFail(Long requestId, Long referenceId, String idempotencyKey, String loanExternalId) {
        PostLoansLoanIdRequest loanApproveRequest = LoanRequestFactory.defaultLoanApproveRequest();
        String bodyLoanApproveRequest = GSON.toJson(loanApproveRequest);

        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_RELATIVE_URL_LOANS_APPLY_EXTERNAL_ID.replace("$.resourceExternalId", loanExternalId));
        batchRequest.method(BATCH_API_METHOD_POST);
        batchRequest.reference(referenceId);
        batchRequest.headers(setHeaders(idempotencyKey));
        batchRequest.body(bodyLoanApproveRequest);

        return batchRequest;
    }

    private BatchRequest getLoanDetailsByExternalId(Long requestId, Long referenceId, String idempotencyKey) {
        BatchRequest batchRequest = new BatchRequest();
        batchRequest.requestId(requestId);
        batchRequest.relativeUrl(BATCH_API_RELATIVE_URL_GET_LOAN_DETAILS_EXTERNAL_ID);
        batchRequest.method(BATCH_API_METHOD_GET);
        batchRequest.headers(setHeaders(idempotencyKey));
        batchRequest.reference(referenceId);
        batchRequest.body(BODY_GET_REQUEST);

        return batchRequest;
    }

    private Set<Header> setHeaders(String idempotencyKey) {
        Set<Header> headers = new HashSet<>();
        headers.add(HEADER);
        if (idempotencyKey != null) {
            headers.add(new Header().name("Idempotency-Key").value(idempotencyKey));
        }

        return headers;
    }
}
