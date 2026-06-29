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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Test;

public class LoanTransactionReverseReplayChargeOffTest extends FeignLoanTestBase {

    @Test
    public void loanTransactionReverseReplayWithChargeOff() {
        runAt("4 October 2022", () -> {
            final Account assetAccount = accountHelper.createAssetAccount("asset");
            final Account chargeOffFraudExpenseAccount = accountHelper.createExpenseAccount("fraudExpense");
            final Account chargeOffExpenseAccount = accountHelper.createExpenseAccount("chargeOffExpense");

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())
                    .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())
                    .loanPortfolioAccountId(assetAccount.getAccountID().longValue());
            Long loanProductId = createLoanProduct(loanProductsRequest);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "2 September 2022", 1000.0, 1,
                    postLoansRequest -> postLoansRequest.externalId(loanExternalIdStr));

            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "2 September 2022");

            String loanTransactionExternalIdStr = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse repaymentTransaction = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("03 October 2022")
                            .locale(LoanTestData.LOCALE).transactionAmount(10.0).externalId(loanTransactionExternalIdStr));

            changeLoanFraudState(loanId, true);

            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(
                    Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5), 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("4 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).externalId(transactionExternalId)
                            .chargeOffReasonId(chargeOffReasonId));

            updateBusinessDate("6 October 2022");

            reverseLoanTransaction(loanExternalIdStr, repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().transactionDate("6 October 2022").locale(LoanTestData.LOCALE)
                            .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(0.0));

            GetJournalEntriesTransactionIdResponse journalEntriesForChargeOffTransaction = getJournalEntries(
                    "L" + chargeOffTransaction.getResourceId());
            assertNotNull(journalEntriesForChargeOffTransaction);

            List<JournalEntryTransactionItem> assetAccountJournalEntries = journalEntriesForChargeOffTransaction.getPageItems().stream()
                    .filter(journalEntry -> assetAccount.getAccountID().longValue() == journalEntry.getGlAccountId()).toList();

            List<JournalEntryTransactionItem> expenseAccountJournalEntries = journalEntriesForChargeOffTransaction.getPageItems().stream()
                    .filter(journalEntry -> chargeOffFraudExpenseAccount.getAccountID().longValue() == journalEntry.getGlAccountId())
                    .toList();

            assertEquals(2, assetAccountJournalEntries.size());
            assertEquals(2, expenseAccountJournalEntries.size());
        });
    }
}
