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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdDiscountRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;

public class FeignWorkingCapitalLoanHelper {

    private final FineractFeignClient fineractClient;

    public FeignWorkingCapitalLoanHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long submitApplication(PostWorkingCapitalLoansRequest request) {
        PostWorkingCapitalLoansResponse response = ok(
                () -> fineractClient.workingCapitalLoans().submitWorkingCapitalLoanApplication(request));
        return response.getResourceId();
    }

    public Long approve(Long loanId, PostWorkingCapitalLoansLoanIdRequest request) {
        PostWorkingCapitalLoansLoanIdResponse result = ok(
                () -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "approve", request));
        return result.getResourceId();
    }

    public Long disburse(Long loanId, PostWorkingCapitalLoansLoanIdRequest request) {
        PostWorkingCapitalLoansLoanIdResponse result = ok(
                () -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "disburse", request));
        return result.getResourceId();
    }

    public Long undoDisbursal(Long loanId, PostWorkingCapitalLoansLoanIdRequest request) {
        PostWorkingCapitalLoansLoanIdResponse result = ok(
                () -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "undodisbursal", request));
        return result.getResourceId();
    }

    public void undoApproval(Long loanId, PostWorkingCapitalLoansLoanIdRequest request) {
        ok(() -> fineractClient.workingCapitalLoans().stateTransitionWorkingCapitalLoanById(loanId, "undoapproval", request));
    }

    public void delete(Long loanId) {
        ok(() -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplication(loanId));
    }

    public GetWorkingCapitalLoansLoanIdResponse getLoanDetails(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
    }

    public Long updateDiscount(Long loanId, PutWorkingCapitalLoansLoanIdDiscountRequest request) {
        return ok(() -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanDiscountById(loanId, request)).getResourceId();
    }

    public CommandProcessingResult updateRate(Long loanId, PutWorkingCapitalLoansLoanIdRateRequest request) {
        return ok(() -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateById(loanId, request));
    }

    public CallFailedRuntimeException updateRateExpectingError(Long loanId, PutWorkingCapitalLoansLoanIdRateRequest request) {
        try {
            ok(() -> fineractClient.workingCapitalLoans().updateWorkingCapitalLoanRateById(loanId, request));
            throw new AssertionError("Expected rate update to fail but it succeeded");
        } catch (final CallFailedRuntimeException e) {
            return e;
        }
    }

    public List<WorkingCapitalLoanPeriodPaymentRateChangeData> getRateChangeHistory(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoans().getWorkingCapitalLoanRateChangeHistoryById(loanId));
    }

    public Long makeRepayment(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "repayment", request));
        return response.getResourceId();
    }

    public List<GetWorkingCapitalLoanTransactionIdResponse> getTransactions(Long loanId) {
        GetWorkingCapitalLoanTransactionsResponse response = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        return response.getContent() != null ? response.getContent() : List.of();
    }

    public void executeInlineWCCOB(Long loanId) {
        ok(() -> fineractClient.inlineJob().executeInlineJob("WC_LOAN_COB", new InlineJobRequest().addLoanIdsItem(loanId)));
    }

    public Long createGlobalCharge(ChargeRequest request) {
        PostChargesResponse response = ok(() -> fineractClient.charges().createCharge(request));
        return response.getResourceId();
    }

    public Long addCharge(Long loanId, PostLoansLoanIdChargesRequest request) {
        PostLoansLoanIdChargesResponse response = ok(() -> fineractClient.workingCapitalLoanCharges().createLoanCharge(loanId, request));
        return response.getResourceId();
    }

    public List<WorkingCapitalLoanChargeData> getCharges(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoanCharges().retrieveAllWorkingCapitalLoanChargesByLoanId(loanId));
    }

    public void adjustCharge(Long loanId, Long loanChargeId, PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request) {
        ok(() -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
    }

    /**
     * Best-effort cleanup: attempts undo disbursal, undo approval, and delete in sequence. Each step is independent —
     * failures are silently ignored so the remaining steps still run.
     */
    public void cleanupLoan(Long loanId) {
        if (loanId == null) {
            return;
        }
        final PostWorkingCapitalLoansLoanIdRequest emptyRequest = new PostWorkingCapitalLoansLoanIdRequest().locale("en")
                .dateFormat("dd MMMM yyyy");
        tryQuietly(() -> undoDisbursal(loanId, emptyRequest));
        tryQuietly(() -> undoApproval(loanId, emptyRequest));
        tryQuietly(() -> delete(loanId));
    }

    private static void tryQuietly(Runnable action) {
        try {
            action.run();
        } catch (final CallFailedRuntimeException ignored) {
            // best-effort cleanup
        }
    }
}
