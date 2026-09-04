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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.apache.fineract.client.models.GetAccountTransfersPageItems;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.junit.jupiter.api.Test;

public class SavingsTransactionTimeIntegrationTest extends FeignSavingsTestBase {

    private static final String ACCOUNT_DATE = "01 March 2013";
    private static final String TRANSACTION_TIME = "14:30:00+05:30";
    private static final String EXPECTED_UTC_TIME = "09:00:00Z";

    @Test
    public void shouldPersistAndReturnTransactionTimeForSavingsTransactionsAndAccountTransfers() {
        final Long productId = createDefaultSavingsProduct().getResourceId();
        assertNotNull(productId);

        final Long fromClientId = createClient(ACCOUNT_DATE);
        final Long fromSavingsId = createApproveActivateSavings(fromClientId, productId, ACCOUNT_DATE);
        final Long toClientId = createClient(ACCOUNT_DATE);
        final Long toSavingsId = createApproveActivateSavings(toClientId, productId, ACCOUNT_DATE);

        final PostSavingsAccountTransactionsRequest depositRequest = SavingsRequestBuilders.deposit("1000", ACCOUNT_DATE)
                .transactionTime(TRANSACTION_TIME);
        final PostSavingsAccountTransactionsResponse deposit = savingsTransactionHelper.deposit(fromSavingsId, depositRequest);
        assertNotNull(deposit.getResourceId());
        assertEquals(EXPECTED_UTC_TIME,
                savingsTransactionHelper.getTransaction(fromSavingsId, deposit.getResourceId()).getTransactionTime());

        final PostAccountTransfersResponse transfer = accountTransferHelper.createAccountTransfer(
                AccountTransferRequestBuilders.transfer(ACCOUNT_DATE, fromClientId, fromSavingsId, PortfolioAccountType.SAVINGS, toClientId,
                        toSavingsId, PortfolioAccountType.SAVINGS, "250").transferTime(TRANSACTION_TIME));
        assertNotNull(transfer.getResourceId());

        final GetAccountTransfersPageItems retrievedTransfer = accountTransferHelper.getAccountTransfer(transfer.getResourceId());
        assertEquals(EXPECTED_UTC_TIME, retrievedTransfer.getTransferTime());

        assertTransferTransactionTime(fromSavingsId, transfer.getResourceId());
        assertTransferTransactionTime(toSavingsId, transfer.getResourceId());
    }

    private void assertTransferTransactionTime(final Long savingsId, final Long transferId) {
        final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        final SavingsAccountTransactionData transaction = transactions.stream()
                .filter(item -> item.getTransfer() != null && transferId.equals(item.getTransfer().getId())).findFirst().orElseThrow();

        assertEquals(EXPECTED_UTC_TIME, transaction.getTransactionTime());
        assertEquals(EXPECTED_UTC_TIME, transaction.getTransfer().getTransferTime());
    }
}
