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
package org.apache.fineract.integrationtests.client.feign.tests;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Adjusting a Goodwill Credit replaces it with a new Goodwill Credit and replays the loan.
 */
public class FeignLoanGoodwillCreditAdjustmentTest extends FeignLoanTransactionAdjustmentTestBase {

    private static final String GOODWILL_CREDIT = "Goodwill Credit";

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("loanVariantsAndChanges")
    void adjustGoodwillCredit(LoanVariant variant, String change, double originalAmount, String originalDate, double newAmount,
            String newDate) {
        runAt(BUSINESS_DATE, () -> verifyAdjustmentMatchesDirectPosting(variant, GOODWILL_CREDIT, this::postGoodwillCredit, noPreparation(),
                originalAmount, originalDate, newAmount, newDate));
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustGoodwillCreditToOverpayLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, GOODWILL_CREDIT, this::postGoodwillCredit, repayDownTo(100.0), 30.0,
                    LATER_DATE, 200.0, LATER_DATE);
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getOverpaid);
        });
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustGoodwillCreditToReopenOverpaidLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, GOODWILL_CREDIT, this::postGoodwillCredit, repayDownTo(100.0),
                    200.0, LATER_DATE, 30.0, LATER_DATE);
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
        });
    }

    @Test
    void adjustedGoodwillCreditPostsToGoodwillExpenseAccount() {
        runAt(BUSINESS_DATE, () -> {
            Long clientId = createClient(DISBURSEMENT_DATE);
            Long productId = createProduct(
                    customizeProduct(fourInstallmentsProgressiveWithAdvancedAllocation(), product -> product.interestRatePerPeriod(0.0)));
            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 0.0, 4, null);
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);

            Long originalId = postGoodwillCredit(loanId, LATER_DATE, 100.0);
            Long newId = adjustTransaction(loanId, originalId, LATER_DATE, 150.0);

            assertReplacedWithSameType(loanId, originalId, newId, GOODWILL_CREDIT, 150.0, LATER_DATE);
            verifyTRJournalEntries(originalId, //
                    journalEntry(100.0, getAccounts().getGoodwillExpenseAccount(), "DEBIT"), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getGoodwillExpenseAccount(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), "DEBIT"));
            verifyTRJournalEntries(newId, //
                    journalEntry(150.0, getAccounts().getGoodwillExpenseAccount(), "DEBIT"), //
                    journalEntry(150.0, getAccounts().getLoansReceivableAccount(), "CREDIT"));
        });
    }

    private Long postGoodwillCredit(Long loanId, String date, double amount) {
        return makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest().transactionAmount(amount).transactionDate(date)
                .dateFormat(DATETIME_PATTERN).locale("en")).getResourceId();
    }
}
