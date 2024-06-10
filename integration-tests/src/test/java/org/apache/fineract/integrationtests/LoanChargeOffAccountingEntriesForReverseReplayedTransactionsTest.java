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
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanChargeOffAccountingEntriesForReverseReplayedTransactionsTest extends BaseLoanIntegrationTest {

    @Test
    public void testJournalEntriesForChargeOffLoanWithMultipleReverseReplay() {
        runAt("24 May 2024", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create DelinquencyBuckets
            Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, List.of(//
                    Pair.of(1, 10), //
                    Pair.of(11, 30), //
                    Pair.of(31, 60), //
                    Pair.of(61, null)//
            ));

            // create loan product
            PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(3)//
                    .repaymentEvery(15)//
                    .repaymentFrequencyType(RepaymentFrequencyType.DAYS.longValue())//
                    .disallowExpectedDisbursements(true)//
                    .multiDisburseLoan(true)//
                    .enableDownPayment(true)//
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0))//
                    .enableAutoRepaymentForDownPayment(true);

            loanProductsRequest.setDelinquencyBucketId(delinquencyBucketId.longValue());

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductResponse.getResourceId(), "24 May 2024", 1000.0, 3);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(200), "24 May 2024");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(50.0, "Down Payment", "24 May 2024"), //
                    transaction(200.0, "Disbursement", "24 May 2024") //
            );

            // set business date 25 May
            updateBusinessDate("25 May 2024");

            // reverse downpayment transaction
            Long downPaymentTransactionId = getTransactionId(loanId, "Down Payment", "24 May 2024");
            loanTransactionHelper.reverseLoanTransaction(loanId, downPaymentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("25 May 2024")
                            .transactionAmount(0.0).locale("en"));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true) //
            );

            // set business date 26 May
            updateBusinessDate("26 May 2024");
            // charge-off loan
            Long chargeOffTransactionId = chargeOffLoan(loanId, "26 May 2024");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(200.0, "Charge-off", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false) //
            );

            // make backdated repayment on 25 May
            Long repaymentTransactionId = addRepaymentForLoan(loanId, 10.0, "25 May 2024");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));

            // make refund on 26 May equal to disbursal amount
            String merchantIssuedRefundExternalId = UUID.randomUUID().toString();
            Long merchantIssuedRefundId = loanTransactionHelper.makeMerchantIssuedRefund(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("26 May 2024").locale("en")
                            .transactionAmount(200.0).externalId(merchantIssuedRefundExternalId))
                    .getResourceId();

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 10.0, false));

            // verify loan status is overpaid
            verifyLoanStatus(loanId, LoanStatus.OVERPAID);
            // verify journal entries

            // repayment
            verifyTRJournalEntries(repaymentTransactionId, //
                    credit(loansReceivableAccount, 10), //
                    debit(fundSource, 10) //
            );

            // charge off
            verifyTRJournalEntries(chargeOffTransactionId, //
                    credit(loansReceivableAccount, 200), //
                    debit(chargeOffExpenseAccount, 200), //
                    debit(loansReceivableAccount, 200), //
                    credit(chargeOffExpenseAccount, 200) //
            );

            // refund
            verifyTRJournalEntries(merchantIssuedRefundId, //
                    credit(chargeOffExpenseAccount, 190), //
                    credit(overpaymentAccount, 10), //
                    debit(fundSource, 200)

            );

            // set business date 27 May
            updateBusinessDate("27 May 2024");

            // CBR
            loanTransactionHelper.makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("27 May 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(10.0).locale("en"));

            // verify loan status is closed
            verifyLoanStatus(loanId, LoanStatus.CLOSED_OBLIGATIONS_MET);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(190.0, "Charge-off", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 190.0, 0.0, 0.0, 0.0, 0.0, 10.0, false), //
                    transaction(10.0, "Credit Balance Refund", "27 May 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 10.0, false));

            // reverse backdated repayment
            loanTransactionHelper.reverseLoanTransaction(loanId, repaymentTransactionId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("27 May 2024")
                            .transactionAmount(0.0).locale("en"));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(200.0, "Disbursement", "24 May 2024", 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(50.0, "Down Payment", "24 May 2024", 150.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(10.0, "Repayment", "25 May 2024", 190.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(200.0, "Charge-off", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(200.0, "Merchant Issued Refund", "26 May 2024", 0.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0, false), //
                    transaction(10.0, "Credit Balance Refund", "27 May 2024", 10.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));

            // verify journal entries

            // repayment
            verifyTRJournalEntries(repaymentTransactionId, //
                    credit(loansReceivableAccount, 10), //
                    debit(fundSource, 10), //
                    debit(loansReceivableAccount, 10), //
                    credit(fundSource, 10) //
            );

            // replayed merchant issued refund
            Long replayedMerchantIssuedRefundId = loanTransactionHelper.getLoanTransactionDetails(loanId, merchantIssuedRefundExternalId)
                    .getId();

            verifyTRJournalEntries(replayedMerchantIssuedRefundId, credit(chargeOffExpenseAccount, 200), //
                    debit(fundSource, 200) //
            );

            // verify journal entries for reversed refund
            verifyTRJournalEntries(merchantIssuedRefundId, //
                    credit(chargeOffExpenseAccount, 190), //
                    credit(overpaymentAccount, 10), //
                    debit(fundSource, 200), //
                    debit(chargeOffExpenseAccount, 190), //
                    debit(overpaymentAccount, 10), //
                    credit(fundSource, 200));

        });
    }
}
