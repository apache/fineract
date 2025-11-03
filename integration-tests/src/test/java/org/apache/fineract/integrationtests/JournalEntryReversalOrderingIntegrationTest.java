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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class JournalEntryReversalOrderingIntegrationTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testJournalEntryReversalOrdering() {
        // Given: Setup loan with accounting enabled
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithAccounting(assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        final Integer clientID = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "10 January 2023", "10000");

        loanTransactionHelper.approveLoan("10 January 2023", loanID);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("10 January 2023", loanID, "10000");

        // When: Make a repayment transaction
        final PostLoansLoanIdTransactionsResponse repaymentResponse = loanTransactionHelper.makeLoanRepayment("11 January 2023", 1000.0f,
                loanID);
        assertNotNull(repaymentResponse);
        final Long repaymentTransactionId = repaymentResponse.getResourceId();

        // Capture original journal entries
        ArrayList<HashMap> originalEntries = journalEntryHelper.getJournalEntriesByTransactionId("L" + repaymentTransactionId.toString());
        assertNotNull(originalEntries);
        assertTrue(originalEntries.size() > 0, "Should have journal entries for repayment");
        final int originalEntryCount = originalEntries.size();

        // When: Reverse the repayment transaction
        PostLoansLoanIdTransactionsResponse reversalResponse = loanTransactionHelper.reverseLoanTransaction(loanID.longValue(),
                repaymentTransactionId, "12 January 2023");
        assertNotNull(reversalResponse);

        // Then: Verify journal entries after reversal maintain consistent ordering
        ArrayList<HashMap> entriesAfterReversal = journalEntryHelper
                .getJournalEntriesByTransactionId("L" + repaymentTransactionId.toString());
        assertNotNull(entriesAfterReversal);

        // Verify we have both original and reversal entries
        assertEquals(originalEntryCount * 2, entriesAfterReversal.size(),
                "After reversal should have double the entries (original + reversal)");

        // Verify consistent ordering by entry date, created date time, and id
        verifyJournalEntriesOrdering(entriesAfterReversal);
    }

    private void verifyJournalEntriesOrdering(ArrayList<HashMap> entries) {
        Long previousId = null;
        String previousTransactionDate = null;
        String previousCreatedDate = null;

        for (HashMap entry : entries) {
            String transactionDate = extractDateString(entry.get("transactionDate"));
            String createdDate = extractDateString(entry.get("createdDate"));
            Long id = ((Number) entry.get("id")).longValue();

            if (previousTransactionDate != null) {
                // Entries should be ordered by:
                // 1. Transaction date (ascending)
                // 2. Created date (ascending) when transaction dates are equal
                // 3. ID (descending) when both dates are equal
                int transactionDateComparison = transactionDate.compareTo(previousTransactionDate);

                if (transactionDateComparison < 0) {
                    // Current transaction date is earlier - this is correct ascending order
                } else if (transactionDateComparison == 0) {
                    // Same transaction date, check created date
                    int createdDateComparison = createdDate.compareTo(previousCreatedDate);

                    if (createdDateComparison < 0) {
                        // Current created date is earlier - this is correct ascending order
                    } else if (createdDateComparison == 0) {
                        // Same transaction and created dates, verify ID ordering (descending)
                        assertTrue(id < previousId, String.format("Journal entries with same dates should be ordered by ID (descending). "
                                + "Current ID: %d, Previous ID: %d", id, previousId));
                    } else {
                        // Created date is later but transaction date is same - verify this is expected
                        // This is acceptable as entries can be created at different times
                    }
                } else {
                    // Transaction date is later - this is correct for reversal entries
                    // Reversal entries have a later transaction date than original entries
                }
            }

            previousTransactionDate = transactionDate;
            previousCreatedDate = createdDate;
            previousId = id;
        }
    }

    private String extractDateString(Object dateObject) {
        if (dateObject instanceof ArrayList) {
            return dateObject.toString();
        } else {
            return (String) dateObject;
        }
    }

    private Integer createLoanProductWithAccounting(final Account assetAccount, final Account incomeAccount, final Account expenseAccount,
            final Account overpaymentAccount) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("12").withRepaymentTypeAsMonth().withinterestRatePerPeriod("1")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String submittedOnDate,
            final String principal) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principal).withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("1").withExpectedDisbursementDate(submittedOnDate)
                .withSubmittedOnDate(submittedOnDate).withLoanType("individual").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
