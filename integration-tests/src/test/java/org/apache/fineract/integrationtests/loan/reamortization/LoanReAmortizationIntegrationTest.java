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
package org.apache.fineract.integrationtests.loan.reamortization;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Test;

public class LoanReAmortizationIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void test_LoanReAmortizeTransaction_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 1;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()); //

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS);

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(1250.0, false, "01 February 2023") //
            );

            createdLoanId.set(loanId);
        });

        runAt("02 February 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-amortize transaction
            reAmortizeLoan(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(1250.0, "Re-amortize", "02 February 2023") //
            );

            // TODO: verify installments when schedule generation is implemented
        });
    }

    @Test
    public void test_LoanUndoReAmortizeTransaction_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 1;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()); //

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS);

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(1250.0, false, "01 February 2023") //
            );

            createdLoanId.set(loanId);
        });

        runAt("02 February 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-amortize transaction
            reAmortizeLoan(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(1250.0, "Re-amortize", "02 February 2023") //
            );
        });

        runAt("03 February 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-amortize transaction
            undoReAmortizeLoan(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    reversedTransaction(1250.0, "Re-amortize", "02 February 2023") //
            );

            // TODO: verify installments when schedule generation is implemented
        });
    }
}
