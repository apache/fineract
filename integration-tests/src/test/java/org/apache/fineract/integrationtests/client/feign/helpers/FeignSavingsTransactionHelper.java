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
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
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

    /**
     * Withdraws past the account balance using the {@code force-withdrawal} command, which the server only accepts
     * while the force-withdrawal global configuration is enabled.
     */
    public PostSavingsAccountTransactionsResponse forceWithdraw(Long savingsId, PostSavingsAccountTransactionsRequest request) {
        return ok(
                () -> fineractClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsId, request, "force-withdrawal"));
    }

    public PostSavingsAccountTransactionsResponse forceWithdraw(Long savingsId, String amount, String transactionDate) {
        return forceWithdraw(savingsId, SavingsRequestBuilders.withdrawal(amount, transactionDate));
    }

    public List<SavingsAccountTransactionData> getTransactions(Long savingsId) {
        SavingsAccountData savings = ok(
                () -> fineractClient.savingsAccount().retrieveSavingsAccount(savingsId, Map.of("associations", "transactions")));
        return savings.getTransactions();
    }

    public SavingsAccountTransactionsSearchResponse searchTransactions(Long savingsId, Map<String, Object> queryParams) {
        return ok(() -> fineractClient.savingsAccountTransactions().searchSavingsAccountTransactions(savingsId, queryParams));
    }

    /** The error decoder only builds a typed error when the response has a body, so a bodiless rejection needs both. */
    public int searchTransactionsExpectingErrorStatus(Long savingsId, Map<String, Object> queryParams) {
        try {
            return fail(() -> fineractClient.savingsAccountTransactions().searchSavingsAccountTransactions(savingsId, queryParams))
                    .getStatus();
        } catch (FeignException e) {
            return e.status();
        }
    }
}
