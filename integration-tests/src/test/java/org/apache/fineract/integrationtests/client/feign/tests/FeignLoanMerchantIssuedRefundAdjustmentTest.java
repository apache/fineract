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
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.SupportedInterestRefundTypesItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Adjusting a Merchant Issued Refund replaces it with a new Merchant Issued Refund and replays the loan, unless the
 * product calculates an Interest Refund for it.
 */
public class FeignLoanMerchantIssuedRefundAdjustmentTest extends FeignLoanTransactionAdjustmentTestBase {

    private static final String MERCHANT_ISSUED_REFUND = "Merchant Issued Refund";

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("loanVariantsAndChanges")
    void adjustMerchantIssuedRefund(LoanVariant variant, String change, double originalAmount, String originalDate, double newAmount,
            String newDate) {
        runAt(BUSINESS_DATE, () -> verifyAdjustmentMatchesDirectPosting(variant, MERCHANT_ISSUED_REFUND, this::postRefund, noPreparation(),
                originalAmount, originalDate, newAmount, newDate, twinMode(variant)));
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustMerchantIssuedRefundToOverpayLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, MERCHANT_ISSUED_REFUND, this::postRefund, repayDownTo(100.0), 30.0,
                    LATER_DATE, 200.0, LATER_DATE, twinMode(variant));
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getOverpaid);
        });
    }

    @ParameterizedTest
    @EnumSource(LoanVariant.class)
    void adjustMerchantIssuedRefundToReopenOverpaidLoan(LoanVariant variant) {
        runAt(BUSINESS_DATE, () -> {
            Long loanId = verifyAdjustmentMatchesDirectPosting(variant, MERCHANT_ISSUED_REFUND, this::postRefund, repayDownTo(100.0), 200.0,
                    LATER_DATE, 30.0, LATER_DATE, twinMode(variant));
            verifyLoanStatus(getLoanDetails(loanId), GetLoansLoanIdStatus::getActive);
        });
    }

    @Test
    void adjustRejectedWhenInterestRefundEnabled() {
        runAt(BUSINESS_DATE, () -> verifyAdjustRejectedWhenInterestRefundEnabled(SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND,
                MERCHANT_ISSUED_REFUND, this::postRefund));
    }

    @Test
    void adjustAllowedWhenInterestRefundRequestedOnTransactionButNotEnabled() {
        runAt(BUSINESS_DATE, () -> verifyAdjustAllowedWhenInterestRefundRequestedOnTransaction(MERCHANT_ISSUED_REFUND,
                (loanId, date, amount) -> postRefund(loanId, refundRequest(date, amount).interestRefundCalculation(true))));
    }

    @Test
    void adjustAllowedWhenInterestRefundEnabledOnlyForOtherType() {
        runAt(BUSINESS_DATE, () -> verifyAdjustAllowedWhenInterestRefundEnabledForOtherType(SupportedInterestRefundTypesItem.PAYOUT_REFUND,
                MERCHANT_ISSUED_REFUND, this::postRefund));
    }

    @Test
    void adjustedMerchantIssuedRefundPostsToFundSourceAccount() {
        runAt(BUSINESS_DATE, () -> {
            Long clientId = createClient(DISBURSEMENT_DATE);
            Long productId = createProduct(
                    customizeProduct(fourInstallmentsProgressiveWithAdvancedAllocation(), product -> product.interestRatePerPeriod(0.0)));
            Long loanId = applyAndApproveProgressiveLoan(clientId, productId, DISBURSEMENT_DATE, PRINCIPAL, 0.0, 4, null);
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), DISBURSEMENT_DATE);

            Long originalId = postRefund(loanId, LATER_DATE, 100.0);
            Long newId = adjustTransaction(loanId, originalId, LATER_DATE, 60.0);

            assertReplacedWithSameType(loanId, originalId, newId, MERCHANT_ISSUED_REFUND, 60.0, LATER_DATE);
            verifyTRJournalEntries(originalId, //
                    journalEntry(100.0, getAccounts().getFundSource(), "DEBIT"), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getFundSource(), "CREDIT"), //
                    journalEntry(100.0, getAccounts().getLoansReceivableAccount(), "DEBIT"));
            verifyTRJournalEntries(newId, //
                    journalEntry(60.0, getAccounts().getFundSource(), "DEBIT"), //
                    journalEntry(60.0, getAccounts().getLoansReceivableAccount(), "CREDIT"));
        });
    }

    private static TwinMode twinMode(LoanVariant variant) {
        return variant.isCumulativeWithInterestRecalculation() ? TwinMode.REPAYMENT : TwinMode.DIRECT;
    }

    private Long postRefund(Long loanId, String date, double amount) {
        return postRefund(loanId, refundRequest(date, amount));
    }

    private Long postRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return makeMerchantIssuedRefund(loanId, request).getResourceId();
    }

    private static PostLoansLoanIdTransactionsRequest refundRequest(String date, double amount) {
        return new PostLoansLoanIdTransactionsRequest().transactionAmount(amount).transactionDate(date).dateFormat(DATETIME_PATTERN)
                .locale("en");
    }
}
