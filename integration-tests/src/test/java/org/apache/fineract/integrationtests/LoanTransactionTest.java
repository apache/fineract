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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.TransactionType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanTransactionTest extends BaseLoanIntegrationTest {

    @Test
    public void testGetLoanTransactionsFiltering() {
        final PostClientsResponse client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final PostLoanProductsResponse loanProductsResponse = loanProductHelper.createLoanProduct(create4IProgressive());

        final String loanExternalIdStr = UUID.randomUUID().toString();

        runAt("20 December 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProductsResponse.getResourceId(), "20 December 2024",
                    430.0, 7.0, 6, (request) -> request.externalId(loanExternalIdStr));

            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(430), "20 December 2024");
        });
        runAt("21 December 2024", () -> {
            executeInlineCOB(loanIdRef.get());
        });
        runAt("20 January 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "20 January 2025", 82.20);
        });
        runAt("20 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "20 February 2025", 82.20);
        });
        runAt("23 February 2025", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);

            final GetLoansLoanIdTransactionsResponse allLoanTransactionsPage = loanTransactionHelper.getLoanTransactions(loanId);
            Assertions.assertEquals(67L, allLoanTransactionsPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse nonAccrualLoanTransactionsPage = loanTransactionHelper.getLoanTransactions(loanId,
                    List.of(TransactionType.ACCRUAL));
            Assertions.assertEquals(3L, nonAccrualLoanTransactionsPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse allLoanTransactionsByExternalIdPage = loanTransactionHelper
                    .getLoanTransactionsByExternalId(loanExternalIdStr);
            Assertions.assertEquals(67L, allLoanTransactionsByExternalIdPage.getTotalElements());

            final GetLoansLoanIdTransactionsResponse nonAccrualLoanTransactionsByExternalIdPage = loanTransactionHelper
                    .getLoanTransactionsByExternalId(loanExternalIdStr, List.of(TransactionType.ACCRUAL));
            Assertions.assertEquals(3L, nonAccrualLoanTransactionsByExternalIdPage.getTotalElements());
        });
    }

}
