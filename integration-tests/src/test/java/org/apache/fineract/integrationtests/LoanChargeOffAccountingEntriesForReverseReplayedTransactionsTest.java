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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanChargeOffAccountingEntriesForReverseReplayedTransactionsTest extends FeignLoanTestBase {

    @Test
    public void testJournalEntriesForChargeOffLoanWithMultipleReverseReplay() {
        runAt("24 May 2024", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            Long delinquencyBucketId = DelinquencyBucketsHelper.createBucket(List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(3)//
                    .repaymentEvery(15)//
                    .repaymentFrequencyType(RepaymentFrequencyType.DAYS.longValue())//
                    .disallowExpectedDisbursements(true)//
                    .multiDisburseLoan(true)//
                    .enableDownPayment(true)//
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0))//
                    .enableAutoRepaymentForDownPayment(true);

            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());

            Long loanProductId = createLoanProduct(loanProductsRequest);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "24 May 2024", 1000.0, 3);

            disburseLoan(loanId, BigDecimal.valueOf(200), "24 May 2024");

            verifyTransactions(loanId, //
                    transaction(50.0, "Down Payment", "24 May 2024"), //
                    transaction(200.0, "Disbursement", "24 May 2024") //
            );

            updateBusinessDate("25 May 2024");

            Long downPaymentTransactionId = getTransactionId(loanId, "Down Payment", "24 May 2024");
            reverseLoanTransaction(loanId, downPaymentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                            .transactionDate("25 May 2024").transactionAmount(0.0).locale(LoanTestData.LOCALE));

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true) //
            );

            updateBusinessDate("26 May 2024");
            Long chargeOffTransactionId = chargeOffLoan(loanId, "26 May 2024");

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(200.0, "Charge-off", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false) //
            );

            Long repaymentTransactionId = addRepaymentForLoan(loanId, 10.0, "25 May 2024");

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));

            String merchantIssuedRefundExternalId = UUID.randomUUID().toString();
            Long merchantIssuedRefundId = makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate("26 May 2024")
                            .locale(LoanTestData.LOCALE).transactionAmount(200.0).externalId(merchantIssuedRefundExternalId))
                    .getResourceId();

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 10.0, false));

            verifyLoanStatus(loanId, LoanStatus.OVERPAID);

            verifyTRJournalEntries(repaymentTransactionId, //
                    credit(accounts.getLoansReceivableAccount(), 10), //
                    debit(accounts.getFundSource(), 10) //
            );

            verifyTRJournalEntries(chargeOffTransactionId, //
                    credit(accounts.getLoansReceivableAccount(), 200), //
                    debit(accounts.getChargeOffExpenseAccount(), 200), //
                    debit(accounts.getLoansReceivableAccount(), 200), //
                    credit(accounts.getChargeOffExpenseAccount(), 200) //
            );

            verifyTRJournalEntries(merchantIssuedRefundId, //
                    credit(accounts.getChargeOffExpenseAccount(), 190), //
                    credit(accounts.getOverpaymentAccount(), 10), //
                    debit(accounts.getFundSource(), 200)

            );

            updateBusinessDate("27 May 2024");

            makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("27 May 2024")
                    .dateFormat(LoanTestData.DATETIME_PATTERN).transactionAmount(10.0).locale(LoanTestData.LOCALE));

            verifyLoanStatus(loanId, LoanStatus.CLOSED_OBLIGATIONS_MET);

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 10.0, false), //
                    transaction(10.0, "Credit Balance Refund", "27 May 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, false));

            reverseLoanTransaction(loanId, repaymentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                            .transactionDate("27 May 2024").transactionAmount(0.0).locale(LoanTestData.LOCALE));

            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(200.0, "Charge-off", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(10.0, "Credit Balance Refund", "27 May 2024", 10.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));

            verifyTRJournalEntries(repaymentTransactionId, //
                    credit(accounts.getLoansReceivableAccount(), 10), //
                    debit(accounts.getFundSource(), 10), //
                    debit(accounts.getLoansReceivableAccount(), 10), //
                    credit(accounts.getFundSource(), 10) //
            );

            Long replayedMerchantIssuedRefundId = getLoanTransactionDetails(loanId, merchantIssuedRefundExternalId).getId();

            verifyTRJournalEntries(replayedMerchantIssuedRefundId, credit(accounts.getChargeOffExpenseAccount(), 200), //
                    debit(accounts.getFundSource(), 200) //
            );

            verifyTRJournalEntries(merchantIssuedRefundId, //
                    credit(accounts.getChargeOffExpenseAccount(), 190), //
                    credit(accounts.getOverpaymentAccount(), 10), //
                    debit(accounts.getFundSource(), 200), //
                    debit(accounts.getChargeOffExpenseAccount(), 190), //
                    debit(accounts.getOverpaymentAccount(), 10), //
                    credit(accounts.getFundSource(), 200));

        });
    }
}
