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
package org.apache.fineract.integrationtests.loan.reaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanReAgingIntegrationTest extends BaseLoanIntegrationTest {

    @Test
    public void test_LoanReAgeTransaction_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .enableDownPayment(true) //
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25)) //
                    .enableAutoRepaymentForDownPayment(true) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()); //

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
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
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, true, "01 January 2023"), //
                    installment(312.5, false, "01 February 2023"), //
                    installment(312.5, false, "01 March 2023"), //
                    installment(312.5, false, "01 April 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 1));
            createdLoanId.set(loanId);
        });

        runAt("11 April 2023", () -> {

            long loanId = createdLoanId.get();

            // create charge
            double chargeAmount = 10.0;
            PostChargesResponse chargeResult = createCharge(chargeAmount);
            Long chargeId = chargeResult.getResourceId();

            // add charge after maturity
            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "11 April 2023", chargeAmount);

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, true, "01 January 2023"), //
                    installment(312.5, false, "01 February 2023"), //
                    installment(312.5, false, "01 March 2023"), //
                    installment(312.5, false, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "11 April 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 1));
        });

        runAt("12 April 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-age transaction
            reAgeLoan(loanId, RepaymentFrequencyType.MONTHS_STRING, 1, "12 April 2023", 4);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    transaction(937.5, "Re-age", "12 April 2023") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, true, "01 January 2023"), //
                    installment(0, true, "01 February 2023"), //
                    installment(0, true, "01 March 2023"), //
                    installment(0, true, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "11 April 2023"), //
                    installment(234.38, false, "12 April 2023"), //
                    installment(234.38, false, "12 May 2023"), //
                    installment(234.38, false, "12 June 2023"), //
                    installment(234.36, false, "12 July 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 7, 12), LocalDate.of(2023, 7, 12));
        });

        runAt("13 April 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-age transaction
            undoReAgeLoan(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    reversedTransaction(937.5, "Re-age", "12 April 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, true, "01 January 2023"), //
                    installment(312.5, false, "01 February 2023"), //
                    installment(312.5, false, "01 March 2023"), //
                    installment(312.5, false, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "11 April 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 1));
        });
        String repaymentExternalId = UUID.randomUUID().toString();
        runAt("13 April 2023", () -> {
            long loanId = createdLoanId.get();

            loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("13 April 2023").locale("en").transactionAmount(100.0).externalId(repaymentExternalId));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    reversedTransaction(937.5, "Re-age", "12 April 2023"), //
                    transaction(100.0, "Repayment", "13 April 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 212.5, false, "01 February 2023"), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 March 2023"), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "11 April 2023") //
            );

            // create re-age transaction
            reAgeLoan(loanId, RepaymentFrequencyType.DAYS_STRING, 30, "13 April 2023", 3);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    reversedTransaction(937.5, "Re-age", "12 April 2023"), //
                    transaction(100.0, "Repayment", "13 April 2023"), //
                    transaction(837.5, "Re-age", "13 April 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023"), //
                    installment(100.0, 0, 0, 0, 0.0, true, "01 February 2023"), //
                    installment(0, 0, 0, 0, 0.0, true, "01 March 2023"), //
                    installment(0, 0, 0, 0, 0.0, true, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "11 April 2023"), //
                    installment(279.17, 0, 0, 0, 279.17, false, "13 April 2023"), //
                    installment(279.17, 0, 0, 0, 279.17, false, "13 May 2023"), //
                    installment(279.16, 0, 0, 0, 279.16, false, "12 June 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 6, 12), LocalDate.of(2023, 6, 12));
        });

        runAt("14 April 2023", () -> {
            long loanId = createdLoanId.get();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(100.0), "14 April 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    reversedTransaction(937.5, "Re-age", "12 April 2023"), //
                    transaction(100.0, "Repayment", "13 April 2023"), //
                    transaction(837.5, "Re-age", "13 April 2023"), //
                    transaction(100.0, "Disbursement", "14 April 2023"), //
                    transaction(25.0, "Down Payment", "14 April 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023"), //
                    installment(100.0, 0, 0, 0, 0.0, true, "01 February 2023"), //
                    installment(0, 0, 0, 0, 0.0, true, "01 March 2023"), //
                    installment(0, 0, 0, 0, 0.0, true, "01 April 2023"), //
                    installment(0.0, 0.0, 10.0, 0.0, true, "11 April 2023"), //
                    installment(279.17, 0, 0, 0, 264.17, false, "13 April 2023"), //
                    installment(100, null, "14 April 2023"), //
                    installment(25.0, 0, 0, 0, 25.0, false, "14 April 2023"), //
                    installment(316.67, 0, 0, 0, 316.67, false, "13 May 2023"), //
                    installment(316.66, 0, 0, 0, 316.66, false, "12 June 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 6, 12), LocalDate.of(2023, 6, 12));
        });
    }

    @Test
    public void test_LoanReAgeTransaction_WithChargeback_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .enableDownPayment(true) //
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25)) //
                    .enableAutoRepaymentForDownPayment(true) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()); //

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
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
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, true, "01 January 2023"), //
                    installment(312.5, false, "01 February 2023"), //
                    installment(312.5, false, "01 March 2023"), //
                    installment(312.5, false, "01 April 2023") //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 1));
            createdLoanId.set(loanId);
        });

        String repaymentExternalId = UUID.randomUUID().toString();
        runAt("01 February 2023", () -> {
            long loanId = createdLoanId.get();

            loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("01 February 2023").locale("en").transactionAmount(100.0).externalId(repaymentExternalId));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 February 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 212.5, false, "01 February 2023"), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 March 2023"), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 April 2023") //
            );
        });

        runAt("10 April 2023", () -> {
            long loanId = createdLoanId.get();

            // disburse Loan
            loanTransactionHelper.chargebackLoanTransaction(loanId, repaymentExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(100.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 February 2023"), //
                    transaction(100.0, "Chargeback", "10 April 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023", 937.5), //
                    installment(312.5, 0, 0, 0, 212.5, false, "01 February 2023", 625.0), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 March 2023", 312.5), //
                    installment(312.5, 0, 0, 0, 312.5, false, "01 April 2023", 0.0), //
                    installment(100.0, 0.0, 0.0, 0.0, 100.0, false, "10 April 2023", 0.0) //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 4, 1), LocalDate.of(2023, 4, 1));
        });

        runAt("12 April 2023", () -> {
            long loanId = createdLoanId.get();

            // create re-age transaction
            reAgeLoan(loanId, RepaymentFrequencyType.MONTHS_STRING, 1, "12 April 2023", 4);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    transaction(312.5, "Down Payment", "01 January 2023"), //
                    transaction(100.0, "Repayment", "01 February 2023"), //
                    transaction(100.0, "Chargeback", "10 April 2023"), //
                    transaction(937.5, "Re-age", "12 April 2023") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.5, 0, 0, 0, 0.0, true, "01 January 2023", 937.5), //
                    installment(100.0, 0, 0, 0, 0.0, true, "01 February 2023", 837.5), //
                    installment(0.0, 0, 0, 0, 0.0, true, "01 March 2023", 837.5), //
                    installment(0.0, 0, 0, 0, 0.0, true, "01 April 2023", 837.5), //
                    installment(0.0, 0.0, 0.0, 0.0, 0.0, true, "10 April 2023", 937.5), //
                    installment(234.38, 0.0, 0.0, 0.0, 234.38, false, "12 April 2023", 703.12), //
                    installment(234.38, 0.0, 0.0, 0.0, 234.38, false, "12 May 2023", 468.74), //
                    installment(234.38, 0.0, 0.0, 0.0, 234.38, false, "12 June 2023", 234.36), //
                    installment(234.36, 0.0, 0.0, 0.0, 234.36, false, "12 July 2023", 0.0) //
            );
            checkMaturityDates(loanId, LocalDate.of(2023, 7, 12), LocalDate.of(2023, 7, 12));
        });
    }
}
