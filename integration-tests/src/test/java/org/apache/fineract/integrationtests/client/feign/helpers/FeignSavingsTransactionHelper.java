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

import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.services.SavingsAccountTransactionsApi.SearchSavingsAccountTransactionsQueryParams;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PostSavingsAccountBulkReversalTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.client.models.SavingsAccountTransactionsSearchResponse;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;

public class FeignSavingsTransactionHelper {

    private final FineractFeignClient fineractClient;

    public FeignSavingsTransactionHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostSavingsAccountTransactionsResponse deposit(Long savingsId, PostSavingsAccountTransactionsRequest request) {
        return ok(() -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "deposit"));
    }

    public PostSavingsAccountTransactionsResponse deposit(Long savingsId, String amount, String transactionDate) {
        return deposit(savingsId, SavingsRequestBuilders.deposit(amount, transactionDate));
    }

    public PostSavingsAccountTransactionsResponse withdraw(Long savingsId, PostSavingsAccountTransactionsRequest request) {
        return ok(() -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "withdrawal"));
    }

    public PostSavingsAccountTransactionsResponse withdraw(Long savingsId, String amount, String transactionDate) {
        return withdraw(savingsId, SavingsRequestBuilders.withdrawal(amount, transactionDate));
    }

    /** The server only accepts this command while the force-withdrawal global configuration is enabled. */
    public PostSavingsAccountTransactionsResponse forceWithdraw(Long savingsId, PostSavingsAccountTransactionsRequest request) {
        return ok(
                () -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "force-withdrawal"));
    }

    public PostSavingsAccountTransactionsResponse forceWithdraw(Long savingsId, String amount, String transactionDate) {
        return forceWithdraw(savingsId, SavingsRequestBuilders.withdrawal(amount, transactionDate));
    }

    public CallFailedRuntimeException withdrawExpectingError(Long savingsId, String amount, String transactionDate) {
        PostSavingsAccountTransactionsRequest request = SavingsRequestBuilders.withdrawal(amount, transactionDate);
        return fail(() -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "withdrawal"));
    }

    public PostSavingsAccountTransactionsResponse postInterestAsOn(Long savingsId, String transactionDate) {
        PostSavingsAccountTransactionsRequest request = SavingsRequestBuilders.postInterestAsOn(transactionDate);
        return ok(
                () -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "postInterestAsOn"));
    }

    public PostSavingsAccountTransactionsResponse holdAmount(Long savingsId, String amount, String transactionDate, String reasonForBlock) {
        PostSavingsAccountTransactionsRequest request = SavingsRequestBuilders.holdAmount(amount, transactionDate, reasonForBlock);
        return ok(() -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "holdAmount"));
    }

    public CommandProcessingResult reverseTransaction(Long savingsId, Long transactionId) {
        return adjustTransaction(savingsId, transactionId, "reverse");
    }

    public CommandProcessingResult undoTransaction(Long savingsId, Long transactionId) {
        return adjustTransaction(savingsId, transactionId, "undo");
    }

    public CommandProcessingResult releaseAmount(Long savingsId, Long holdTransactionId) {
        return adjustTransaction(savingsId, holdTransactionId, "releaseAmount");
    }

    /** All three commands are driven by the query parameter alone and read no payload, hence the empty body. */
    private CommandProcessingResult adjustTransaction(Long savingsId, Long transactionId, String command) {
        PostSavingsAccountBulkReversalTransactionsRequest request = new PostSavingsAccountBulkReversalTransactionsRequest();
        return ok(() -> fineractClient.savingsAccountTransactions().adjustSavingsAccountTransaction(savingsId, transactionId, request,
                command));
    }

    public SavingsAccountTransactionData getTransaction(Long savingsId, Long transactionId) {
        return ok(() -> fineractClient.savingsAccountTransactions().retrieveOneSavingsAccountTransaction(savingsId, transactionId));
    }

    public List<SavingsAccountTransactionData> getTransactions(Long savingsId) {
        SavingsAccountData savings = ok(
                () -> fineractClient.savingsAccount().retrieveSavingsAccount(savingsId, Map.of("associations", "transactions")));
        return savings.getTransactions();
    }

    public List<SavingsAccountTransactionData> getAccrualTransactions(Long savingsId) {
        return getTransactions(savingsId).stream().filter(FeignSavingsTransactionHelper::isAccrual).toList();
    }

    /** Reversed accruals are included, matching what the account's own accrual total reflects. */
    public BigDecimal getTotalAccrualAmount(Long savingsId) {
        return getAccrualTransactions(savingsId).stream().map(SavingsAccountTransactionData::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isAccrual(SavingsAccountTransactionData transaction) {
        return transaction.getTransactionType() != null && Boolean.TRUE.equals(transaction.getTransactionType().getAccrual());
    }

    public SavingsAccountTransactionsSearchResponse searchTransactions(Long savingsId,
            SearchSavingsAccountTransactionsQueryParams queryParams) {
        return ok(() -> fineractClient.savingsAccountTransactions().searchSavingsAccountTransactions(savingsId, queryParams));
    }

    /** The error decoder only builds a typed error when the response has a body, so a bodiless rejection needs both. */
    public int searchTransactionsExpectingErrorStatus(Long savingsId, SearchSavingsAccountTransactionsQueryParams queryParams) {
        try {
            return fail(() -> fineractClient.savingsAccountTransactions().searchSavingsAccountTransactions(savingsId, queryParams))
                    .getStatus();
        } catch (FeignException e) {
            return e.status();
        }
    }
}
