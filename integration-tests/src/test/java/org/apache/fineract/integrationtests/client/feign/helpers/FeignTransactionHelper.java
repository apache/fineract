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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.LoanScheduleData;
import org.apache.fineract.client.models.LoanTransactionData;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;

public class FeignTransactionHelper {

    private final FineractFeignClient fineractClient;
    private final InternalLoanReAgeApi internalLoanReAgeApi;

    public FeignTransactionHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.internalLoanReAgeApi = fineractClient.create(InternalLoanReAgeApi.class);
    }

    public void executeInlineCOB(Long loanId) {
        executeInlineCOB(List.of(loanId));
    }

    public void executeInlineCOB(List<Long> loanIds) {
        RequestSpecification requestSpec = restAssuredRequestSpec();
        ResponseSpecification responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        new InlineLoanCOBHelper(requestSpec, responseSpec).executeInlineCOB(loanIds);
    }

    private static RequestSpecification restAssuredRequestSpec() {
        Utils.initializeRESTAssured();
        return new RequestSpecBuilder().setContentType(ContentType.JSON)
                .addHeader("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey())
                .addHeader("Fineract-Platform-TenantId", Utils.DEFAULT_TENANT).build();
    }

    public void addCapitalizedIncome(Long loanId, String transactionDate, double amount) {
        ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId,
                new PostLoansLoanIdTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate)
                        .dateFormat("dd MMMM yyyy").locale("en"),
                Map.of("command", "capitalizedIncome")));
    }

    public GetLoansLoanIdTransactionsTemplateResponse getPrepaymentAmount(Long loanId, String transactionDate, String dateFormat) {
        return ok(() -> fineractClient.loanTransactions().retrieveTemplateLoanTransaction(loanId, "prepayLoan", dateFormat, transactionDate,
                "en", null));
    }

    public Long addRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "repayment")));
        return response.getResourceId();
    }

    public Long addInterestWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "waiveInterest")));
        return response.getResourceId();
    }

    public Long chargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "charge-off")));
        return response.getResourceId();
    }

    public PostLoansLoanIdTransactionsResponse undoChargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request,
                Map.of("command", "undo-charge-off")));
    }

    public Long addChargeback(Long loanId, Long transactionId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsTransactionIdRequest chargebackRequest = new PostLoansLoanIdTransactionsTransactionIdRequest()//
                .transactionDate(request.getTransactionDate())//
                .transactionAmount(request.getTransactionAmount())//
                .locale(request.getLocale())//
                .dateFormat(request.getDateFormat());

        PostLoansLoanIdTransactionsResponse response = ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId,
                transactionId, chargebackRequest, Map.of("command", "chargeback")));
        return response.getResourceId();
    }

    public Long reAge(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "reAge")));
        return response.getResourceId();
    }

    public Long undoReAge(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "undoReAge")));
        return response.getResourceId();
    }

    public LoanTransactionData getReAgeTemplate(Long loanId) {
        return ok(() -> internalLoanReAgeApi.retrieveReAgeTemplate(loanId));
    }

    public LoanScheduleData previewReAgeSchedule(Long loanId, Map<String, Object> queryParams) {
        return ok(() -> fineractClient.loanTransactions().previewReAgeSchedule(loanId, queryParams));
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

    public Long applyChargebackTransaction(Long loanId, Long transactionId, Double amount, Long paymentTypeId) {
        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest()
                .transactionAmount(amount).paymentTypeId(paymentTypeId).locale("en");
        return chargebackLoanTransaction(loanId, transactionId, request).getResourceId();
    }

    public PostLoansLoanIdTransactionsResponse makeLoanDownPayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, "downPayment"));
    }

    public GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransaction(Long loanId, Long transactionId) {
        final Long resolvedTransactionId = transactionId;
        return ok(() -> fineractClient.loanTransactions().retrieveTransaction(loanId, resolvedTransactionId, ""));
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
        return ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId,
                new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy").transactionDate(transactionDate)
                        .transactionAmount(0.0).locale("en"),
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
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "undoReAmortize")));
    }

    public PostLoansLoanIdTransactionsResponse writeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransaction(loanId, request, Map.of("command", "writeoff")));
    }

    public PostLoansLoanIdTransactionsResponse writeOff(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().handleCommandsLoanTransactionByLoanExternalId(loanExternalId, request, "writeoff"));
    }

    public PostLoansLoanIdTransactionsResponse closeRescheduledLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, Map.of("command", "close-rescheduled")));
    }

    public PostLoansLoanIdTransactionsResponse makeRefundByCash(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, "refundByCash"));
    }

    public PostLoansLoanIdTransactionsResponse makeRefundByCash(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return ok(() -> fineractClient.loanTransactions().executeLoanTransactionByLoanExternalId(loanExternalId, request, "refundByCash"));
    }
}
