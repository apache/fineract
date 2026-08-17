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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BuyDownFeeAmortizationDetails;
import org.apache.fineract.client.models.CapitalizedIncomeDetails;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.LoanCapitalizedIncomeData;
import org.apache.fineract.client.models.LoanScheduleData;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesResponse;
import org.apache.fineract.client.models.TransactionType;

public class FeignTransactionHelper {

    private final FineractFeignClient fineractClient;

    public FeignTransactionHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public void executeInlineCOB(Long loanId) {
        executeInlineCOB(List.of(loanId));
    }

    public void executeInlineCOB(List<Long> loanIds) {
        ok(() -> fineractClient.inlineJob().executeInlineJob("LOAN_COB", new InlineJobRequest().loanIds(loanIds)));
    }

    public PostLoansLoanIdTransactionsResponse addCapitalizedIncome(Long loanId, String transactionDate, double amount) {
        return ok(
                () -> fineractClient.loanTransactions()
                        .handleCommandsLoanTransaction(loanId, new PostLoansLoanIdTransactionsRequest().transactionAmount(amount)
                                .transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en"),
                                Map.of("command", "capitalizedIncome")));
    }

    public PostLoansLoanIdTransactionsResponse addCapitalizedIncome(Long loanId, String transactionDate, double amount,
            Long classificationId) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate)
                        .dateFormat("dd MMMM yyyy").locale("en").classificationId(classificationId),
                Map.of("command", "capitalizedIncome")));
    }

    public PostLoansLoanIdTransactionsResponse capitalizedIncomeAdjustment(Long loanId, Long capitalizedIncomeTransactionId,
            String transactionDate, double amount) {
        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en");
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, capitalizedIncomeTransactionId, request,
                "capitalizedIncomeAdjustment"));
    }

    public List<CapitalizedIncomeDetails> fetchCapitalizedIncomeDetails(Long loanId) {
        return ok(() -> fineractClient.loanCapitalizedIncome().fetchCapitalizedIncomeDetails(loanId));
    }

    public LoanCapitalizedIncomeData fetchLoanCapitalizedIncomeData(Long loanId) {
        return ok(() -> fineractClient.loanCapitalizedIncome().fetchLoanCapitalizedIncomeData(loanId));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanBuyDownFee(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "buyDownFee")));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanBuyDownFee(Long loanId, String date, double amount) {
        return makeLoanBuyDownFee(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate(date)
                .locale("en").transactionAmount(amount));
    }

    public PostLoansLoanIdTransactionsResponse buyDownFeeAdjustment(Long loanId, Long buyDownFeeTransactionId, String transactionDate,
            double amount) {
        return buyDownFeeAdjustment(loanId, buyDownFeeTransactionId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).transactionDate(transactionDate).dateFormat("dd MMMM yyyy").locale("en"));
    }

    public PostLoansLoanIdTransactionsResponse buyDownFeeAdjustment(Long loanId, Long buyDownFeeTransactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, buyDownFeeTransactionId, request,
                "buyDownFeeAdjustment"));
    }

    public List<BuyDownFeeAmortizationDetails> fetchBuyDownFeeAmortizationDetails(Long loanId) {
        return ok(() -> fineractClient.loanBuyDownFees().retrieveLoanBuyDownFeeAmortizationDetails(loanId));
    }

    public GetLoansLoanIdTransactionsTemplateResponse getPrepaymentAmount(Long loanId, String transactionDate, String dateFormat) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransaction(loanId, "prepayLoan", dateFormat, transactionDate,
                "en", null));
    }

    public PostLoansLoanIdTransactionsResponse addRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "repayment")));
    }

    public PostLoansLoanIdTransactionsResponse addInterestWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "waiveInterest")));
    }

    public PostLoansLoanIdTransactionsResponse chargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "charge-off")));
    }

    public PostLoansLoanIdTransactionsResponse undoChargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "undo-charge-off")));
    }

    public PostLoansLoanIdTransactionsResponse addChargeback(Long loanId, Long transactionId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsTransactionIdRequest chargebackRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()//
                .transactionDate(request.getTransactionDate())//
                .transactionAmount(request.getTransactionAmount())//
                .locale(request.getLocale())//
                .dateFormat(request.getDateFormat());

        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, chargebackRequest,
                Map.of("command", "chargeback")));
    }

    public PostLoansLoanIdTransactionsResponse reAge(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "reAge")));
    }

    public PostLoansLoanIdTransactionsResponse undoReAge(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "undoReAge")));
    }

    public GetLoansLoanIdTransactionsTemplateResponse getReAgeTemplate(Long loanId) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransaction(loanId, "reAge", null, null, null, null));
    }

    public LoanScheduleData previewReAgeSchedule(Long loanId, Map<String, Object> queryParams) {
        final Map<String, Object> params = queryParams == null ? new HashMap<>() : new HashMap<>(queryParams);
        params.put("command", "reAge");
        return ok(() -> fineractClient.loanTransactions().previewReAgeLoanSchedule(loanId, params));
    }

    public PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(Long loanId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByTransactionExternalId(loanId, transactionExternalId,
                request, "chargeback"));
    }

    public PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(Long loanId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, request,
                Map.of("command", "chargeback")));
    }

    public PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanExternalId(loanExternalId, transactionId, request,
                "chargeback"));
    }

    public PostLoansLoanIdTransactionsResponse applyChargebackTransaction(Long loanId, Long transactionId, Double amount,
            Long paymentTypeId) {
        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).paymentTypeId(paymentTypeId).locale("en");
        return chargebackLoanTransaction(loanId, transactionId, request);
    }

    public PostLoansLoanIdTransactionsResponse makeLoanDownPayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "downPayment"));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanDownPayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "downPayment"));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransaction(Long loanId, Long transactionId) {
        final Long resolvedTransactionId = transactionId;
        return ok(() -> fineractClient.loanTransactions().retrieveOneLoanTransaction(loanId, resolvedTransactionId, ""));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(Long loanId, Long transactionId) {
        return getLoanTransaction(loanId, transactionId);
    }

    public PostLoansLoanIdTransactionsResponse makeMerchantIssuedRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "merchantIssuedRefund")));
    }

    public PostLoansLoanIdTransactionsResponse makePayoutRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "payoutRefund")));
    }

    public PostLoansLoanIdTransactionsResponse makeGoodwillCredit(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "goodwillCredit")));
    }

    public PostLoansLoanIdTransactionsResponse makeInterestPaymentWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "interestPaymentWaiver")));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanRepayment(Long loanId, String command, String date, Double amount) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, new PostLoansLoanIdTransactionsRequest()
                .transactionAmount(amount).transactionDate(date).dateFormat("dd MMMM yyyy").locale("en"), Map.of("command", command)));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "repayment")));
    }

    public PostLoansLoanIdTransactionsResponse makeLoanRepayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "repayment"));
    }

    public PostLoansLoanIdTransactionsResponse chargeOffLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "charge-off")));
    }

    public PostLoansLoanIdTransactionsResponse chargeOffLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "charge-off"));
    }

    public PostLoansLoanIdTransactionsResponse makeMerchantIssuedRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "merchantIssuedRefund"));
    }

    public PostLoansLoanIdTransactionsResponse makeCreditBalanceRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "creditBalanceRefund"));
    }

    public PostLoansLoanIdTransactionsResponse reverseLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanExternalId(loanExternalId, transactionId, request,
                Map.of("command", "undo")));
    }

    public PostLoansLoanIdTransactionsResponse reverseLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanAndTransactionExternalId(loanExternalId,
                transactionExternalId, request, Map.of("command", "undo")));
    }

    public PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanAndTransactionExternalId(loanExternalId,
                transactionExternalId, request, "chargeback"));
    }

    public void undoRepayment(Long loanId, Long transactionId, String transactionDate) {
        reverseLoanTransaction(loanId, transactionId, transactionDate);
    }

    public PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, request, Map.of("command", "undo")));
    }

    public PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, Long transactionId, String transactionDate) {
        return reverseLoanTransaction(loanId, transactionId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                .dateFormat("dd MMMM yyyy").transactionDate(transactionDate).transactionAmount(0.0).locale("en"));
    }

    public PostLoansLoanIdTransactionsResponse adjustLoanTransaction(Long loanId, Long transactionId, String transactionDate) {
        return adjustLoanTransaction(loanId, transactionId, transactionDate, 0.0);
    }

    public PostLoansLoanIdTransactionsResponse adjustLoanTransaction(Long loanId, Long transactionId, String transactionDate,
            double transactionAmount) {
        return ok(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, transactionId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                        .dateFormat("dd MMMM yyyy").transactionDate(transactionDate).transactionAmount(transactionAmount).locale("en"),
                        "adjust"));
    }

    public CallFailedRuntimeException adjustLoanTransactionExpectingError(Long loanId, Long transactionId, String transactionDate,
            double transactionAmount) {
        return fail(() -> fineractClient.loanTransactions()
                .adjustLoanTransaction(loanId, transactionId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                        .dateFormat("dd MMMM yyyy").transactionDate(transactionDate).transactionAmount(transactionAmount).locale("en"),
                        "adjust"));
    }

    public PostLoansLoanIdTransactionsResponse makeCreditBalanceRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "creditBalanceRefund")));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(Long loanId, String transactionExternalId) {
        return ok(() -> fineractClient.loanTransactions().retrieveOneLoanTransactionByExternalId(loanId, transactionExternalId, ""));
    }

    public PostLoansLoanIdTransactionsResponse createManualInterestRefund(Long loanId, Long targetTransactionId, String transactionDate,
            Double amount, String externalId) {
        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).dateFormat("dd MMMM yyyy").locale("en");
        if (externalId != null) {
            request.externalId(externalId);
        }
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, targetTransactionId, request,
                Map.of("command", "interest-refund")));
    }

    public PostLoansLoanIdTransactionsResponse reAmortize(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "reAmortize")));
    }

    public PostLoansLoanIdTransactionsResponse undoReAmortize(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "undoReAmortize")));
    }

    public PostLoansLoanIdTransactionsResponse writeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "writeoff")));
    }

    public PostLoansLoanIdTransactionsResponse writeOff(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request, "writeoff"));
    }

    public PostLoansLoanIdTransactionsResponse closeRescheduledLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "close-rescheduled")));
    }

    public PostLoansLoanIdTransactionsResponse makeRefundByCash(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "refundByCash"));
    }

    public PostLoansLoanIdTransactionsResponse makeRefundByCash(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "refundByCash"));
    }

    public GetLoansLoanIdTransactionsResponse getLoanTransactions(Long loanId) {
        return ok(() -> fineractClient.loanTransactions().retrieveAllLoanTransactionsUniversal(loanId, Collections.emptyMap()));
    }

    public GetLoansLoanIdTransactionsResponse getLoanTransactions(Long loanId, List<TransactionType> excludedTransactionTypes) {
        Map<String, Object> queryParams = new HashMap<>();
        if (excludedTransactionTypes != null && !excludedTransactionTypes.isEmpty()) {
            queryParams.put("excludedTypes", excludedTransactionTypes);
        }
        return ok(() -> fineractClient.loanTransactions().retrieveAllLoanTransactionsUniversal(loanId, queryParams));
    }

    public GetLoansLoanIdTransactionsResponse getLoanTransactionsByExternalId(String loanExternalId) {
        return ok(() -> fineractClient.loanTransactions().retrieveAllLoanTransactionsByExternalIdUniversal(loanExternalId,
                Collections.emptyMap()));
    }

    public GetLoansLoanIdTransactionsResponse getLoanTransactionsByExternalId(String loanExternalId,
            List<TransactionType> excludedTransactionTypes) {
        Map<String, Object> queryParams = new HashMap<>();
        if (excludedTransactionTypes != null && !excludedTransactionTypes.isEmpty()) {
            queryParams.put("excludedTypes", excludedTransactionTypes);
        }
        return ok(() -> fineractClient.loanTransactions().retrieveAllLoanTransactionsByExternalIdUniversal(loanExternalId, queryParams));
    }

    public GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(Long loanId, String command, String dateFormat,
            String transactionDate, String locale) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransaction(loanId, command, dateFormat, transactionDate,
                locale, null));
    }

    public GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(Long loanId, String command, String dateFormat,
            String transactionDate, String locale, Long transactionId) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransaction(loanId, command, dateFormat, transactionDate,
                locale, transactionId));
    }

    public PostLoansLoanIdTransactionsResponse executeLoanTransaction(Long loanId, PostLoansLoanIdTransactionsRequest request,
            String command) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, command));
    }

    public void makeRepayment(String date, float amount, int loanId) {
        makeLoanRepayment((long) loanId, "Repayment", date, (double) amount);
    }

    public void makeRefundByCash(String date, float amount, int loanId) {
        ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction((long) loanId, new PostLoansLoanIdTransactionsRequest()
                .transactionAmount((double) amount).transactionDate(date).dateFormat("dd MMMM yyyy").locale("en"), "refundByCash"));
    }

    public GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(String loanExternalId, String command, String dateFormat,
            String transactionDate, String locale) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransactionByLoanExternalId(loanExternalId, command,
                dateFormat, transactionDate, locale, null));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(String loanExternalId, Long transactionId) {
        return ok(() -> fineractClient.loanTransactions().retrieveOneLoanTransactionByLoanExternalId(loanExternalId, transactionId,
                (String) null));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(String loanExternalId, String transactionExternalId) {
        return ok(() -> fineractClient.loanTransactions().retrieveOneLoanTransactionByLoanExternalIdAndTransactionExternalId(loanExternalId,
                transactionExternalId, (String) null));
    }

    public PostLoansLoanIdTransactionsResponse makePayoutRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "payoutRefund"));
    }

    public PostLoansLoanIdTransactionsResponse makeGoodwillCredit(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "goodwillCredit"));
    }

    public PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByTransactionExternalId(loanId, transactionExternalId,
                request, "undo"));
    }

    public PostLoansLoanIdTransactionsResponse closeLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request, "close"));
    }

    public PostLoansLoanIdTransactionsResponse closeRescheduledLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "close-rescheduled"));
    }

    public PostLoansLoanIdTransactionsResponse forecloseLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "foreclosure"));
    }

    public PutChargeTransactionChangesResponse undoWaiveLoanCharge(Long loanId, Long transactionId,
            PutChargeTransactionChangesRequest request) {
        return ok(() -> fineractClient.loanTransactions().undoWaiveChargeLoanTransaction(loanId, transactionId, request));
    }

    public PutChargeTransactionChangesResponse undoWaiveLoanCharge(Long loanId, String transactionExternalId,
            PutChargeTransactionChangesRequest request) {
        return ok(() -> fineractClient.loanTransactions().undoWaiveChargeLoanTransactionByTransactionExternalId(loanId,
                transactionExternalId, request));
    }

    public PutChargeTransactionChangesResponse undoWaiveLoanCharge(String loanExternalId, Long transactionId,
            PutChargeTransactionChangesRequest request) {
        return ok(() -> fineractClient.loanTransactions().undoWaiveChargeLoanTransactionByLoanExternalId(loanExternalId, transactionId,
                request));
    }

    public PutChargeTransactionChangesResponse undoWaiveLoanCharge(String loanExternalId, String transactionExternalId,
            PutChargeTransactionChangesRequest request) {
        return ok(() -> fineractClient.loanTransactions().undoWaiveChargeLoanTransactionByLoanAndTransactionExternalId(loanExternalId,
                transactionExternalId, request));
    }

    public PostLoansLoanIdTransactionsResponse makeChargeRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "chargeRefund"));
    }

    public PostLoansLoanIdTransactionsResponse makeChargeRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "chargeRefund"));
    }

    public PostLoansLoanIdTransactionsResponse makeWaiveInterest(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "waiveinterest"));
    }

    public PostLoansLoanIdTransactionsResponse makeWaiveInterest(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "waiveinterest"));
    }

    public PostLoansLoanIdTransactionsResponse makeUndoWriteoff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "undowriteoff"));
    }

    public PostLoansLoanIdTransactionsResponse makeUndoWriteoff(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "undowriteoff"));
    }

    public PostLoansLoanIdTransactionsResponse makeRecoveryPayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, "recoverypayment"));
    }

    public PostLoansLoanIdTransactionsResponse makeRecoveryPayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request,
                "recoverypayment"));
    }

    public PostLoansLoanIdTransactionsResponse adjustLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanAndTransactionExternalId(loanExternalId,
                transactionExternalId, request, "adjust"));
    }

    public PostLoansLoanIdTransactionsResponse adjustLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransactionByLoanExternalId(loanExternalId, transactionId, request,
                "adjust"));
    }
}
