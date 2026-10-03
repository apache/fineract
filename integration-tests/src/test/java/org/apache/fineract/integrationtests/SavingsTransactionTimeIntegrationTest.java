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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.GetAccountTransfersPageItems;
import org.apache.fineract.client.models.PostAccountTransfersResponse;
import org.apache.fineract.client.models.PostSavingsAccountBulkReversalTransactionsRequest;
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
    private static final String TRANSACTION_TIME = "02:00:00+05:30";
    private static final String ADJUSTED_TRANSACTION_TIME = "23:30:00-04:00";
    private static final DateTimeFormatter TRANSACTION_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ssXXX");

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
        assertEquals(expectedTransactionTime(TRANSACTION_TIME),
                savingsTransactionHelper.getTransaction(fromSavingsId, deposit.getResourceId()).getTransactionTime());

        final CommandProcessingResult adjustment = savingsTransactionHelper.modifyTransaction(fromSavingsId, deposit.getResourceId(),
                new PostSavingsAccountBulkReversalTransactionsRequest().transactionDate(ACCOUNT_DATE)
                        .transactionTime(ADJUSTED_TRANSACTION_TIME).transactionAmount(new BigDecimal("1000")).paymentTypeId(1L)
                        .dateFormat("dd MMMM yyyy").locale("en"));
        assertNotNull(adjustment.getResourceId());
        assertEquals(expectedTransactionTime(ADJUSTED_TRANSACTION_TIME),
                savingsTransactionHelper.getTransaction(fromSavingsId, adjustment.getResourceId()).getTransactionTime());

        final PostAccountTransfersResponse transfer = accountTransferHelper.createAccountTransfer(
                AccountTransferRequestBuilders.transfer(ACCOUNT_DATE, fromClientId, fromSavingsId, PortfolioAccountType.SAVINGS, toClientId,
                        toSavingsId, PortfolioAccountType.SAVINGS, "250").transferTime(TRANSACTION_TIME));
        assertNotNull(transfer.getResourceId());

        final GetAccountTransfersPageItems retrievedTransfer = accountTransferHelper.getAccountTransfer(transfer.getResourceId());
        assertEquals(expectedTransactionTime(TRANSACTION_TIME), retrievedTransfer.getTransferTime());

        assertTransferTransactionTime(fromSavingsId, transfer.getResourceId());
        assertTransferTransactionTime(toSavingsId, transfer.getResourceId());

        final PostAccountTransfersResponse transferWithoutTime = accountTransferHelper
                .createAccountTransfer(AccountTransferRequestBuilders.transfer(ACCOUNT_DATE, fromClientId, fromSavingsId,
                        PortfolioAccountType.SAVINGS, toClientId, toSavingsId, PortfolioAccountType.SAVINGS, "100"));
        assertNotNull(transferWithoutTime.getResourceId());
        assertNull(accountTransferHelper.getAccountTransfer(transferWithoutTime.getResourceId()).getTransferTime());
    }

    private void assertTransferTransactionTime(final Long savingsId, final Long transferId) {
        final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        final SavingsAccountTransactionData transaction = transactions.stream()
                .filter(item -> item.getTransfer() != null && transferId.equals(item.getTransfer().getId())).findFirst().orElseThrow();

        assertEquals(expectedTransactionTime(TRANSACTION_TIME), transaction.getTransactionTime());
        assertEquals(expectedTransactionTime(TRANSACTION_TIME), transaction.getTransfer().getTransferTime());
    }

    private String expectedTransactionTime(String transactionTime) {
        final String composeFile = System.getenv("COMPOSE_FILE");
        if (composeFile != null && (composeFile.contains("mysql") || composeFile.contains("mariadb"))) {
            return OffsetTime.parse(transactionTime).withOffsetSameInstant(ZoneOffset.UTC).format(TRANSACTION_TIME_FORMATTER);
        }
        return transactionTime;
    }
}
