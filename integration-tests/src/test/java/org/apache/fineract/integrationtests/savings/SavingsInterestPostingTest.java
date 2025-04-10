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
package org.apache.fineract.integrationtests.savings;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.savings.base.BaseSavingsIntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class SavingsInterestPostingTest extends BaseSavingsIntegrationTest {

    @Test
    public void testSavingsInterestPosting_Works_ForMultipleAccounts() {
        List<AccountWithDepositAmount> accounts = new ArrayList<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create product
            PostSavingsProductsRequest productsRequest = dailyInterestPostingProduct();
            PostSavingsProductsResponse product = createProduct(productsRequest);
            Long productId = product.getResourceId();

            // Create accounts
            IntStream.range(0, 200)//
                    .parallel().mapToObj(i -> applySavingsRequest(clientId, productId, "01 January 2023"))//
                    .map(this::applySavingsAccount) //
                    .mapToLong(PostSavingsAccountsResponse::getResourceId) //
                    .forEach(savingsId -> {
                        approveSavingsAccount(savingsId, "01 January 2023");
                        activateSavingsAccount(savingsId, "01 January 2023");
                        accounts.add(new AccountWithDepositAmount(savingsId));
                    });
        });

        runFromToInclusive("02 January 2023", "10 January 2023", (date) -> {
            SecureRandom rnd = new SecureRandom();
            accounts.parallelStream().forEach(account -> {
                long lAmount = rnd.nextLong(10_000_000L);
                BigDecimal amount = BigDecimal.valueOf(lAmount);
                deposit(account.savingsId, date, amount);
                account.addDeposit(amount);
            });
        });

        runAt("11 January 2023", () -> {
            String jobName = "Post Interest For Savings";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            accounts.parallelStream().forEach(account -> {
                SavingsAccountData accountData = getSavingsAccount(account.savingsId);
                BigDecimal accountBalance = accountData.getSummary().getAccountBalance();
                BigDecimal accountBalanceByTransactions = accountData.getTransactions().stream()
                        .map(SavingsAccountTransactionData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(accountBalance)
                        .withFailMessage("Account balance (%s) and calculated balance (%s) from transactions do not match", accountBalance,
                                accountBalanceByTransactions)
                        .isEqualByComparingTo(accountBalanceByTransactions);
                BigDecimal summedDepositAmount = accountData.getTransactions().stream().filter(tx -> tx.getTransactionType().getDeposit())
                        .map(SavingsAccountTransactionData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                assertThat(summedDepositAmount).withFailMessage(
                        "Calculated balance (%s) from deposit transactions and submitted deposit balance (%s) do not match", accountBalance,
                        summedDepositAmount).isEqualByComparingTo(account.depositedAmount);
            });
        });
    }

    static class AccountWithDepositAmount {

        public Long savingsId;
        public BigDecimal depositedAmount = BigDecimal.ZERO;

        AccountWithDepositAmount(Long savingsId) {
            this.savingsId = savingsId;
        }

        public void addDeposit(BigDecimal amount) {
            depositedAmount = depositedAmount.add(amount);
        }
    }
}
