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

import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;

public class FeignTransactionHelper {

    private final FineractFeignClient fineractClient;

    public FeignTransactionHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long addRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, Map.of("command", "repayment")));
        return response.getResourceId();
    }

    public Long addInterestWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, Map.of("command", "waiveInterest")));
        return response.getResourceId();
    }

    public Long chargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        PostLoansLoanIdTransactionsResponse response = ok(
                () -> fineractClient.loanTransactions().executeLoanTransaction(loanId, request, Map.of("command", "chargeOff")));
        return response.getResourceId();
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

    public void undoRepayment(Long loanId, Long transactionId, String transactionDate) {
        PostLoansLoanIdTransactionsTransactionIdRequest request = new PostLoansLoanIdTransactionsTransactionIdRequest();
        request.setTransactionDate(transactionDate);
        request.setTransactionAmount(0.0);
        request.setDateFormat("dd MMMM yyyy");
        request.setLocale("en");
        ok(() -> fineractClient.loanTransactions().adjustLoanTransaction(loanId, transactionId, request, Map.of("command", "undo")));
    }
}
