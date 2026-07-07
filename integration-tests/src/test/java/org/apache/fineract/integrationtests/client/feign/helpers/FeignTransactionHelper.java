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
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;

public class FeignTransactionHelper {

    private final FineractFeignClient fineractClient;

    public FeignTransactionHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public void executeInlineCOB(Long loanId) {
        InlineJobRequest request = new InlineJobRequest().loanIds(List.of(loanId));
        ok(() -> fineractClient.inlineJob().executeInlineJob("LOAN_COB", request));
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
}
