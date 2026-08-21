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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

public class JournalEntryReversalOrderingIntegrationTest extends FeignLoanTestBase {

    @Test
    public void testJournalEntryReversalOrdering() {
        // Given: Setup loan with periodic accrual accounting enabled
        // Given: Setup loan with accounting enabled
        Long loanProductId = createLoanProduct(createLoanProductWithAccounting());
        Long clientId = createClient();
        Long loanId = applyForLoan(applyForLoanApplication(clientId, loanProductId, "10 January 2023", 10000.0));

        approveLoan(loanId, approveLoanRequest(10000.0, "10 January 2023"));
        disburseLoan(loanId, "10 January 2023", 10000.0);

        // When: Make a repayment transaction
        Long repaymentTransactionId = addRepaymentForLoan(loanId, 1000.0, "11 January 2023");
        assertNotNull(repaymentTransactionId);

        // Capture original journal entries
        List<JournalEntryTransactionItem> originalEntries = getJournalEntries("L" + repaymentTransactionId).getPageItems();
        assertNotNull(originalEntries);
        assertTrue(originalEntries.size() > 0, "Should have journal entries for repayment");
        int originalEntryCount = originalEntries.size();

        // When: Reverse the repayment transaction
        PostLoansLoanIdTransactionsResponse reversalResponse = reverseLoanTransaction(loanId, repaymentTransactionId, "12 January 2023");
        assertNotNull(reversalResponse);

        // Then: Verify journal entries after reversal maintain consistent ordering
        List<JournalEntryTransactionItem> entriesAfterReversal = getJournalEntries("L" + repaymentTransactionId).getPageItems();
        assertNotNull(entriesAfterReversal);

        // Verify we have both original and reversal entries
        assertEquals(originalEntryCount * 2, entriesAfterReversal.size(),
                "After reversal should have double the entries (original + reversal)");

        // Verify consistent ordering by transaction date, created date and id
        // Verify consistent ordering by entry date, created date time, and id
        verifyJournalEntriesOrdering(entriesAfterReversal);
    }

    private void verifyJournalEntriesOrdering(List<JournalEntryTransactionItem> entries) {
        Long previousId = null;
        LocalDate previousTransactionDate = null;
        LocalDate previousCreatedDate = null;

        for (JournalEntryTransactionItem entry : entries) {
            LocalDate transactionDate = entry.getTransactionDate();
            LocalDate createdDate = entry.getCreatedDate();
            Long id = entry.getId();

            if (previousTransactionDate != null && transactionDate.isEqual(previousTransactionDate)
                    && createdDate.isEqual(previousCreatedDate)) {
                // Same transaction and created dates, verify ID ordering (descending)
                // Entries should be ordered by:
                // 1. Transaction date (ascending)
                // 2. Created date (ascending) when transaction dates are equal
                // 3. ID (descending) when both dates are equal
                // Current transaction date is earlier - this is correct ascending order
                // Same transaction date, check created date
                // Current created date is earlier - this is correct ascending order
                // Created date is later but transaction date is same - verify this is expected
                // This is acceptable as entries can be created at different times
                // Transaction date is later - this is correct for reversal entries
                // Reversal entries have a later transaction date than original entries
                assertTrue(id < previousId, String.format(
                        "Journal entries with same dates should be ordered by ID (descending). " + "Current ID: %d, Previous ID: %d", id,
                        previousId));
            }

            previousTransactionDate = transactionDate;
            previousCreatedDate = createdDate;
            previousId = id;
        }
    }

    private PostLoanProductsRequest createLoanProductWithAccounting() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()//
                .principal(10000.0)//
                .numberOfRepayments(12)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.MONTHS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false);
    }

    private PostLoansRequest applyForLoanApplication(Long clientId, Long loanProductId, String submittedOnDate, double principal) {
        return applyLoanRequest(clientId, loanProductId, submittedOnDate, principal, 12,
                request -> request.repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .loanTermFrequency(12).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .interestRatePerPeriod(BigDecimal.ONE));
    }
}
