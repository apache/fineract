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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import org.apache.fineract.client.models.ExecuteWorkingCapitalLoanTransactionCommandResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.MarkWorkingCapitalLoanAsFraudRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansBreachActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoansResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdDiscountRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.client.models.WorkingCapitalLoanChargeData;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
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

    public Long modifyApplication(Long loanId, PutWorkingCapitalLoansLoanIdRequest request) {
        PutWorkingCapitalLoansLoanIdResponse response = ok(
                () -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, request, (String) null));
        return response.getResourceId();
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

    public Long undoTransaction(Long loanId, Long transactionId, ExecuteWorkingCapitalLoanTransactionCommandRequest request) {
        ExecuteWorkingCapitalLoanTransactionCommandResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId, transactionId, "undo", request));
        return response.getResourceId();
    }

    public void undoTransaction(Long loanId, Long transactionId) {
        undoTransaction(loanId, transactionId, new ExecuteWorkingCapitalLoanTransactionCommandRequest());
    }

    public Long makeGoodwillCredit(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "goodwillCredit", request));
        return response.getResourceId();
    }

    public Long makeDiscountFee(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "discountFee", request));
        return response.getResourceId();
    }

    public Long makeDiscountFeeAdjustment(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "discountFeeAdjustment", request));
        return response.getResourceId();
    }

    public Long payoutRefund(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "payoutRefund", request));
        return response.getResourceId();
    }

    public Long chargeOff(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "chargeOff", request));
        return response.getResourceId();
    }

    public Long undoChargeOff(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "undoChargeOff", request));
        return response.getResourceId();
    }

    public CallFailedRuntimeException chargeOffExpectingFailure(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        return fail(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId, "chargeOff",
                request));
    }

    public GetWorkingCapitalLoanTransactionIdResponse getTransaction(Long loanId, Long transactionId) {
        return ok(() -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionById(loanId, transactionId));
    }

    public Long creditBalanceRefund(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        PostWorkingCapitalLoanTransactionsResponse response = ok(() -> fineractClient.workingCapitalLoanTransactions()
                .executeWorkingCapitalLoanTransactionById(loanId, "creditBalanceRefund", request));
        return response.getResourceId();
    }

    public CallFailedRuntimeException creditBalanceRefundExpectingFailure(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        return fail(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId,
                "creditBalanceRefund", request));
    }

    /**
     * Reverses (undoes) a working capital loan transaction via the transaction-command {@code undo} endpoint. Used to
     * back out a prior repayment so the CBR-aware reprocessing can re-derive balances.
     */
    public void reverseTransaction(Long loanId, Long transactionId, ExecuteWorkingCapitalLoanTransactionCommandRequest request) {
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                transactionId, "undo", request));
    }

    public ProjectedAmortizationScheduleData getAmortizationSchedule(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoans().retrieveAmortizationSchedule(loanId));
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

    public List<WorkingCapitalLoanBreachScheduleData> getBreachSchedule(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoanBreachSchedule().retrieveBreachSchedule(loanId));
    }

    public Long adjustCharge(Long loanId, Long loanChargeId, PostWorkingCapitalLoansLoanIdChargesChargeIdRequest request) {
        PostWorkingCapitalLoansLoanIdChargesChargeIdResponse response = ok(
                () -> fineractClient.workingCapitalLoanCharges().adjustLoanCharge(loanId, loanChargeId, request, "adjustment"));
        return response.getSubResourceId();
    }

    public void undoLoanTransaction(Long loanId, Long transactionId, ExecuteWorkingCapitalLoanTransactionCommandRequest request) {
        ok(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionCommandByLoanIdTransactionId(loanId,
                transactionId, "undo", request));
    }

    public CallFailedRuntimeException createDelinquencyActionExpectingFailure(Long loanId,
            PostWorkingCapitalLoansDelinquencyActionRequest request) {
        return fail(() -> fineractClient.workingCapitalLoanDelinquencyActions().createDelinquencyAction(loanId, request));
    }

    public CallFailedRuntimeException createBreachActionExpectingFailure(Long loanId, PostWorkingCapitalLoansBreachActionRequest request) {
        return fail(() -> fineractClient.workingCapitalLoanBreachActions().createBreachAction(loanId, request));
    }

    public CallFailedRuntimeException markAsFraudExpectingFailure(Long loanId, MarkWorkingCapitalLoanAsFraudRequest request) {
        return fail(() -> fineractClient.workingCapitalLoans().markWorkingCapitalLoanAsFraudById(loanId, request));
    }

    public CallFailedRuntimeException deleteApplicationExpectingFailure(Long loanId) {
        return fail(() -> fineractClient.workingCapitalLoans().deleteWorkingCapitalLoanApplication(loanId));
    }

    public CallFailedRuntimeException modifyApplicationExpectingFailure(Long loanId, PutWorkingCapitalLoansLoanIdRequest request) {
        return fail(() -> fineractClient.workingCapitalLoans().modifyWorkingCapitalLoanApplicationById(loanId, request,
                Map.<String, Object>of()));
    }

    public CallFailedRuntimeException undoWriteOffExpectingFailure(Long loanId, PostWorkingCapitalLoanTransactionsRequest request) {
        return fail(() -> fineractClient.workingCapitalLoanTransactions().executeWorkingCapitalLoanTransactionById(loanId, "undoWriteOff",
                request));
    }

    /**
     * Extracts every {@code userMessageGlobalisationCode} from a Fineract validation error body, in response order.
     * Fineract's {@code DataValidatorBuilder} accumulates all failures for a request and reports them together, so a
     * single rejection frequently carries more than one code; asserting on the exact list is what makes an error-code
     * regression guard discriminating rather than "some 4xx happened".
     */
    public static List<String> errorCodesOf(final CallFailedRuntimeException exception) {
        final String body = exception.getResponseBody();
        assertNotNull(body, "expected an error response body");
        final JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonObject()) {
            throw new AssertionError("expected a JSON object error body but got: " + body);
        }
        final List<String> codes = new ArrayList<>();
        final JsonObject rootObject = root.getAsJsonObject();
        final JsonElement errors = rootObject.get("errors");
        if (errors != null && errors.isJsonArray()) {
            final JsonArray errorArray = errors.getAsJsonArray();
            for (final JsonElement error : errorArray) {
                final JsonElement code = error.getAsJsonObject().get("userMessageGlobalisationCode");
                if (code != null && !code.isJsonNull()) {
                    codes.add(code.getAsString());
                }
            }
        }
        if (codes.isEmpty()) {
            final JsonElement topLevelCode = rootObject.get("userMessageGlobalisationCode");
            if (topLevelCode != null && !topLevelCode.isJsonNull()) {
                codes.add(topLevelCode.getAsString());
            }
        }
        return codes;
    }

    public List<WorkingCapitalLoanDelinquencyRangeScheduleData> getDelinquencyRangeSchedule(Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoanDelinquencyRangeSchedule().retrieveDelinquencyRangeSchedule(loanId));
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

    public static GetWorkingCapitalLoanTransactionIdResponse findTransaction(
            final List<GetWorkingCapitalLoanTransactionIdResponse> transactions, final LocalDate transactionDate, final BigDecimal amount) {
        return transactions.stream().filter(txn -> transactionDate.equals(txn.getTransactionDate()))
                .filter(txn -> txn.getTransactionAmount() != null && amount.compareTo(txn.getTransactionAmount()) == 0).findFirst()
                .orElseThrow(() -> new AssertionError("Transaction not found on " + transactionDate + " with amount " + amount));
    }

    public static WorkingCapitalLoanChargeData findCharge(final List<WorkingCapitalLoanChargeData> charges, final Long loanChargeId) {
        return charges.stream().filter(charge -> loanChargeId.equals(charge.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("Loan charge not found with id " + loanChargeId));
    }

    public static void assertEqualBigDecimal(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message + " — value was null");
        assertEquals(0, expected.compareTo(actual), message + " — expected: " + expected + " but was: " + actual);
    }
}
