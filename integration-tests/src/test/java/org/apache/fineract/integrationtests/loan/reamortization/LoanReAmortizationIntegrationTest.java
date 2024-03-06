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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanReAmortizationIntegrationTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void test_LoanReAmortizeTransaction_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 2;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
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
                    transaction(1250.0, "Disbursement", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(625.0, false, "01 February 2023"), //
                    installment(625.0, false, "01 March 2023") //
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
                    transaction(625.0, "Re-amortize", "02 February 2023") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(0.0, true, "01 February 2023"), //
                    installment(1250.0, false, "01 March 2023") //
            );
        });
    }

    @Test
    public void test_LoanUndoReAmortizeTransaction_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 2;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
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
                    transaction(1250.0, "Disbursement", "01 January 2023") //
            );

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(625.0, false, "01 February 2023"), //
                    installment(625.0, false, "01 March 2023") //
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
                    transaction(625.0, "Re-amortize", "02 February 2023") //
            );
        });

        runAt("03 February 2023", () -> {
            long loanId = createdLoanId.get();

            // undo re-amortize transaction
            undoReAmortizeLoan(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023"), //
                    reversedTransaction(625.0, "Re-amortize", "02 February 2023") //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(625.0, false, "01 February 2023"), //
                    installment(625.0, false, "01 March 2023") //
            );
        });
    }

    @Test
    public void reAmortizeLoanRepaymentScheduleTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("05 January 2023");
            addCharge(loanId, false, 10.0, "05 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, 0.0, 10.0, 135.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("25 January 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "16 January 2023"), //
                    installment(187.5, false, "31 January 2023"), //
                    installment(187.5, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void completePastDueReAmortizationTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("01 February 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void partiallyPaidReAmortizationTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("17 January 2023");
            addRepaymentForLoan(loanId, 50.0, "17 January 2023");

            updateBusinessDate("30 January 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(50.0, true, "16 January 2023"), //
                    installment(162.5, false, "31 January 2023"), //
                    installment(162.5, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void reAmortizationOnSameDayOfInstallmentTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("05 January 2023");
            addCharge(loanId, false, 10.0, "05 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, 0.0, 10.0, 135.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("31 January 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void reAmortizationNPlusOneInstallmentTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("01 February 2023");
            addCharge(loanId, false, 10.0, "27 February 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023"), //
                    installment(0.0, 0.0, 10.0, false, "27 February 2023") //
            );
        });
    }

    @Test
    public void reAmortizationBackdatedRepaymentAndReplayTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("01 February 2023");

            reAmortizeLoan(loanId);

            updateBusinessDate("01 February 2023");
            addRepaymentForLoan(loanId, 125.0, "15 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(250.0, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void reAmortizationUndoRepaymentAndReplayTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("15 January 2023");
            Long repaymentTransactionId = addRepaymentForLoan(loanId, 125.0, "15 January 2023");

            updateBusinessDate("01 February 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(250.0, false, "15 February 2023")//
            );

            loanTransactionHelper.reverseRepayment(loanId.intValue(), repaymentTransactionId.intValue(), "01 February 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void reverseReAmortizationTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );

            updateBusinessDate("01 February 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, true, "16 January 2023"), //
                    installment(0.0, true, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023")//
            );

            updateBusinessDate("02 February 2023");

            undoReAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023")//
            );
        });
    }

    @Test
    public void reAmortizationDivisionTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(4, 15, BigDecimal.valueOf(20));

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 500.0, 4, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(60);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(100.0, true, "01 January 2023"), //
                    installment(100.0, false, "16 January 2023"), //
                    installment(100.0, false, "31 January 2023"), //
                    installment(100.0, false, "15 February 2023"), //
                    installment(100.0, false, "02 March 2023")//
            );

            updateBusinessDate("17 January 2023");

            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(100.0, true, "01 January 2023"), //
                    installment(0.0, true, "16 January 2023"), //
                    installment(133.33, false, "31 January 2023"), //
                    installment(133.33, false, "15 February 2023"), //
                    installment(133.34, false, "02 March 2023")//
            );
        });
    }

    @Test
    public void secondDisbursementAfterReAmortizationTest() {
        runAt("01 January 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(3, 15);

            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(125.0, false, "16 January 2023"), //
                    installment(125.0, false, "31 January 2023"), //
                    installment(125.0, false, "15 February 2023") //
            );

            updateBusinessDate("16 January 2023");
            addCharge(loanId, false, 10.0, "16 January 2023");

            updateBusinessDate("25 January 2023");
            reAmortizeLoan(loanId);

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, 0.0, 10.0, 10.0, false, "16 January 2023"), //
                    installment(187.5, false, "31 January 2023"), //
                    installment(187.5, false, "15 February 2023") //
            );

            updateBusinessDate("26 January 2023");

            disburseLoan(loanId, BigDecimal.valueOf(500.00), "26 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(125.0, true, "01 January 2023"), //
                    installment(0.0, 0.0, 10.0, 0.0, true, "16 January 2023"), //
                    installment(500.0, null, "26 January 2023"), //
                    installment(125.0, false, "26 January 2023"), //
                    installment(375.0, false, "31 January 2023"), //
                    installment(375.0, false, "15 February 2023") //
            );
        });
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(int numberOfInstallments, int repaymentEvery) {
        return createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(numberOfInstallments, repaymentEvery,
                DOWN_PAYMENT_PERCENTAGE);
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(int numberOfInstallments, int repaymentEvery,
            BigDecimal downPaymentPercentage) {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(numberOfInstallments);
        product.setRepaymentEvery(repaymentEvery);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(downPaymentPercentage);
        product.setEnableAutoRepaymentForDownPayment(true);
        product.setInstallmentAmountInMultiplesOf(null);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();
    }
}
