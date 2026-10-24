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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.junit.jupiter.api.Test;

/**
 * Two transfers in opposite directions (A to B and B to A) used to deadlock, because each one updated its own source
 * account first and then waited for the other account. The database aborted one of them, and the caller got a conflict.
 * Both accounts are now locked in ascending id order, so crossing transfers queue up behind each other.
 */
class AccountTransferConcurrentTest extends FeignSavingsTestBase {

    private static final String ACTIVATION_DATE = "01 January 2013";
    private static final String TRANSFER_DATE = "01 March 2013";
    private static final BigDecimal OPENING_BALANCE = BigDecimal.valueOf(1000);
    private static final BigDecimal TRANSFER_AMOUNT = BigDecimal.TEN;
    private static final int TRANSFERS_PER_DIRECTION = 10;
    private static final int ROUNDS = 3;

    @Test
    void crossingTransfersBetweenTwoAccountsAllSucceed() throws Exception {
        runAt("01 March 2013", () -> {
            final Long productId = createDefaultSavingsProduct().getResourceId();
            final Long clientA = createClient();
            final Long clientB = createClient();
            final Long savingsA = createApproveActivateSavings(clientA, productId, ACTIVATION_DATE);
            final Long savingsB = createApproveActivateSavings(clientB, productId, ACTIVATION_DATE);
            deposit(savingsA, OPENING_BALANCE.toPlainString(), ACTIVATION_DATE);
            deposit(savingsB, OPENING_BALANCE.toPlainString(), ACTIVATION_DATE);

            for (int round = 0; round < ROUNDS; round++) {
                transferConcurrently(clientA, savingsA, clientB, savingsB);
            }

            // The same number of transfers went each way, so both balances are back where they started.
            assertBalance(savingsA, OPENING_BALANCE);
            assertBalance(savingsB, OPENING_BALANCE);
        });
    }

    private void transferConcurrently(final Long clientA, final Long savingsA, final Long clientB, final Long savingsB) {
        final int total = 2 * TRANSFERS_PER_DIRECTION;
        final ExecutorService executor = Executors.newFixedThreadPool(total);
        final CountDownLatch start = new CountDownLatch(1);
        try {
            final List<Future<Long>> results = new ArrayList<>();
            for (int i = 0; i < TRANSFERS_PER_DIRECTION; i++) {
                results.add(executor.submit(() -> transfer(start, clientA, savingsA, clientB, savingsB)));
                results.add(executor.submit(() -> transfer(start, clientB, savingsB, clientA, savingsA)));
            }
            start.countDown();
            for (final Future<Long> result : results) {
                // get() rethrows the failure of a transfer, such as the conflict a deadlock victim gets.
                assertNotNull(result.get(2, TimeUnit.MINUTES));
            }
        } catch (final Exception e) {
            throw new AssertionError("A crossing transfer failed", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private Long transfer(final CountDownLatch start, final Long fromClientId, final Long fromSavingsId, final Long toClientId,
            final Long toSavingsId) throws InterruptedException {
        start.await();
        final AccountTransferRequest request = AccountTransferRequestBuilders.transfer(TRANSFER_DATE, fromClientId, fromSavingsId,
                PortfolioAccountType.SAVINGS, toClientId, toSavingsId, PortfolioAccountType.SAVINGS, TRANSFER_AMOUNT.toPlainString());
        return accountTransferHelper.createAccountTransfer(request).getResourceId();
    }

    private void assertBalance(final Long savingsId, final BigDecimal expectedBalance) {
        assertEquals(0, expectedBalance.compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));
    }
}
