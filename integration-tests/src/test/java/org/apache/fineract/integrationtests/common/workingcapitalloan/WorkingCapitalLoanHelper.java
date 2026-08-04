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
package org.apache.fineract.integrationtests.common.workingcapitalloan;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.apache.fineract.client.feign.services.WorkingCapitalLoanTransactionsApi;
import org.apache.fineract.client.feign.services.WorkingCapitalLoansApi;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.feign.util.FeignCalls;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansPagedResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansTemplateResponse;
import org.apache.fineract.client.models.MarkWorkingCapitalLoanAsFraudRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;

/**
 * Integration-test helper for Working Capital Loans using the generated Feign client.
 */
public class WorkingCapitalLoanHelper {

    public WorkingCapitalLoanHelper() {}

    private static WorkingCapitalLoansApi api() {
        return FineractFeignClientHelper.getFineractFeignClient().workingCapitalLoans();
    }

    private static WorkingCapitalLoanTransactionsApi transactionsApi() {
        return FineractFeignClientHelper.getFineractFeignClient().create(WorkingCapitalLoanTransactionsApi.class);
    }

    public Long submit(final PostWorkingCapitalLoansRequest request) {
        PostWorkingCapitalLoansResponse response = FeignCalls.ok(() -> api().submitWorkingCapitalLoanApplication(request));
        return response.getResourceId();
    }

    public Long modifyById(final Long loanId, final PutWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.ok(() -> api().modifyWorkingCapitalLoanApplicationById(loanId, request, Map.of())).getResourceId();
    }

    public Long modifyByExternalId(final String externalId, final PutWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.ok(() -> api().modifyWorkingCapitalLoanApplicationByExternalId(externalId, request, Map.of())).getResourceId();
    }

    public Long deleteById(final Long loanId) {
        return FeignCalls.ok(() -> api().deleteWorkingCapitalLoanApplication(loanId)).getResourceId();
    }

    public Long deleteByExternalId(final String externalId) {
        return FeignCalls.ok(() -> api().deleteWorkingCapitalLoanApplicationByExternalId(externalId)).getResourceId();
    }

    public GetWorkingCapitalLoansLoanIdResponse retrieveById(final Long loanId) {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanById(loanId));
    }

    public void markAsFraudById(final Long loanId, final MarkWorkingCapitalLoanAsFraudRequest request) {
        FeignCalls.ok(() -> api().markWorkingCapitalLoanAsFraudById(loanId, request));
    }

    public CallFailedRuntimeException markAsFraudByIdExpectingFailure(final Long loanId,
            final MarkWorkingCapitalLoanAsFraudRequest request) {
        return FeignCalls.fail(() -> api().markWorkingCapitalLoanAsFraudById(loanId, request));
    }

    public void markAsFraudByExternalId(final String externalId, final MarkWorkingCapitalLoanAsFraudRequest request) {
        FeignCalls.ok(() -> api().markWorkingCapitalLoanAsFraudByExternalId(externalId, request));
    }

    public CallFailedRuntimeException markAsFraudByExternalIdExpectingFailure(final String externalId,
            final MarkWorkingCapitalLoanAsFraudRequest request) {
        return FeignCalls.fail(() -> api().markWorkingCapitalLoanAsFraudByExternalId(externalId, request));
    }

    public GetWorkingCapitalLoansLoanIdResponse retrieveByExternalId(final String externalId) {
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanByExternalId(externalId));
    }

    public ProjectedAmortizationScheduleData retrieveAmortizationScheduleByLoanIdRaw(final Long loanId) {
        return FeignCalls.ok(() -> api().retrieveAmortizationSchedule(loanId));
    }

    public GetWorkingCapitalLoansPagedResponse retrieveAllPagedRaw(final Map<String, Object> queryParams) {
        Map<String, Object> params = queryParams != null ? queryParams : Map.of();
        return FeignCalls.ok(() -> api().retrieveAllWorkingCapitalLoans(params));
    }

    public GetWorkingCapitalLoansTemplateResponse retrieveTemplateRaw(final Map<String, Object> queryParams) {
        Map<String, Object> params = queryParams != null ? queryParams : Map.of();
        return FeignCalls.ok(() -> api().retrieveWorkingCapitalLoanTemplate(params));
    }

    public void approveById(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "approve", request));
    }

    public void rejectById(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "reject", request));
    }

    public void undoApprovalById(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "undoapproval", request));
    }

    public void approveByExternalId(final String externalId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanByExternalId(externalId, "approve", request));
    }

    public void rejectByExternalId(final String externalId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanByExternalId(externalId, "reject", request));
    }

    public void undoApprovalByExternalId(final String externalId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanByExternalId(externalId, "undoapproval", request));
    }

    public void disburseById(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "disburse", request));
    }

    public void disburseByExternalId(final String externalId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanByExternalId(externalId, "disburse", request));
    }

    public void undoDisbursalById(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        FeignCalls.ok(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "undodisbursal", request));
    }

    public Long makeRepaymentByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "repayment", request))
                .getResourceId();
    }

    public void undoTransactionByLoanId(final Long loanId, final Long transactionId) {
        FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, transactionId,
                "undo", new ExecuteWorkingCapitalLoanTransactionCommandRequest()));
    }

    public void makeRepaymentByLoanExternalId(final String loanExternalId, final PostWorkingCapitalLoanTransactionsRequest request) {
        FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionByExternalId(loanExternalId, request,
                Map.of("command", "repayment")));
    }

    public void undoTransactionById(final Long loanId, final Long transactionId) {
        FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, transactionId,
                "undo", new ExecuteWorkingCapitalLoanTransactionCommandRequest()));
    }

    public Long makeCreditBalanceRefundByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", request))
                .getResourceId();
    }

    public Long makeGoodwillCreditByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "goodwillCredit", request))
                .getResourceId();
    }

    public Long makePayoutRefundByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "payoutRefund", request))
                .getResourceId();
    }

    public CallFailedRuntimeException runCreditBalanceRefundByLoanIdExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.fail(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", request));
    }

    public Long chargeOffByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request))
                .getResourceId();
    }

    public Long undoChargeOffByLoanId(final Long loanId, final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.ok(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "undoChargeOff", request))
                .getResourceId();
    }

    public CallFailedRuntimeException runChargeOffByLoanIdExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.fail(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request));
    }

    public CallFailedRuntimeException runUndoChargeOffByLoanIdExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.fail(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "undoChargeOff", request));
    }

    public GetWorkingCapitalLoanTransactionsResponse retrieveTransactionsByLoanId(final Long loanId) {
        return FeignCalls.ok(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionsById(loanId));
    }

    public GetWorkingCapitalLoanTransactionsResponse retrieveTransactionsByLoanExternalId(final String loanExternalId) {
        return FeignCalls.ok(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionsByExternalId(loanExternalId));
    }

    public GetWorkingCapitalLoanTransactionIdResponse retrieveTransactionByLoanIdAndTransactionId(final Long loanId,
            final Long transactionId) {
        return FeignCalls.ok(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionById(loanId, transactionId));
    }

    public CallFailedRuntimeException runRetrieveTransactionByLoanIdAndTransactionIdExpectingFailure(final Long loanId,
            final Long transactionId) {
        return FeignCalls.fail(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionById(loanId, transactionId));
    }

    public GetWorkingCapitalLoanTransactionIdResponse retrieveTransactionByLoanIdAndTransactionExternalId(final Long loanId,
            final String externalTransactionId) {
        return FeignCalls
                .ok(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionByExternalTransactionId(loanId, externalTransactionId));
    }

    public GetWorkingCapitalLoanTransactionIdResponse retrieveTransactionByExternalLoanIdAndTransactionId(final String loanExternalId,
            final Long transactionId) {
        return FeignCalls.ok(() -> transactionsApi().retrieveWorkingCapitalLoanTransactionByExternalLoanIdAndTransactionId(loanExternalId,
                transactionId));
    }

    public GetWorkingCapitalLoanTransactionIdResponse retrieveTransactionByExternalLoanIdAndTransactionExternalId(
            final String loanExternalId, final String externalTransactionId) {
        return FeignCalls.ok(() -> transactionsApi()
                .retrieveWorkingCapitalLoanTransactionByExternalLoanIdAndExternalTransactionId(loanExternalId, externalTransactionId));
    }

    public CallFailedRuntimeException runApproveExpectingFailure(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "approve", request));
    }

    public CallFailedRuntimeException runRejectExpectingFailure(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "reject", request));
    }

    public CallFailedRuntimeException runUndoApprovalExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "undoapproval", request));
    }

    public CallFailedRuntimeException runDisburseExpectingFailure(final Long loanId, final PostWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "disburse", request));
    }

    public CallFailedRuntimeException runUndoDisbursalExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().stateTransitionWorkingCapitalLoanById(loanId, "undodisbursal", request));
    }

    public CallFailedRuntimeException runRepaymentByLoanIdExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoanTransactionsRequest request) {
        return FeignCalls.fail(() -> transactionsApi().executeWorkingCapitalLoanTransactionById(loanId, "repayment", request));
    }

    /**
     * For validation tests: run submit expecting failure.
     */
    public CallFailedRuntimeException runSubmitExpectingFailure(final PostWorkingCapitalLoansRequest request) {
        return FeignCalls.fail(() -> api().submitWorkingCapitalLoanApplication(request));
    }

    /**
     * For validation tests: run modify expecting failure.
     */
    public CallFailedRuntimeException runModifyExpectingFailure(final Long loanId, final PutWorkingCapitalLoansLoanIdRequest request) {
        return FeignCalls.fail(() -> api().modifyWorkingCapitalLoanApplicationById(loanId, request, Map.of()));
    }

    public CallFailedRuntimeException runUpdateRateExpectingFailure(final Long loanId,
            final PutWorkingCapitalLoansLoanIdRateRequest request) {
        return FeignCalls.fail(() -> api().updateWorkingCapitalLoanRateById(loanId, request));
    }

    public GetWorkingCapitalLoansLoanIdResponse retrieveLoan(final Long loanId) {
        final GetWorkingCapitalLoansLoanIdResponse response = retrieveById(loanId);
        assertNotNull(response);
        return response;
    }
}
