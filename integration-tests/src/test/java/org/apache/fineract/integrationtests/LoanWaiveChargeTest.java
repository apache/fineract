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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Streams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoanWaiveChargeTest extends BaseLoanIntegrationTest {

    private static Stream<Arguments> processingStrategy() {
        return Stream.of(Arguments.of(Named.of("originalStrategy", false)), //
                Arguments.of(Named.of("advancedStrategy", true)));
    }

    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void test_LoanPaidByDateIsCorrect_WhenNPlusOneInstallmentCharge_IsWaived(boolean advancedPaymentStrategy) {
        double amount = 1000.0;
        AtomicLong appliedLoanId = new AtomicLong();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product;
            if (advancedPaymentStrategy) {
                product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation();
            } else {
                product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
            }

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, 1);
            if (advancedPaymentStrategy) {
                applicationRequest = applicationRequest
                        .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
            }

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();
            appliedLoanId.set(loanId);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 January 2023");

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 0.0, 1000.0, false, "31 January 2023"));
        });
        runAt("02 February 2023", () -> {
            Long loanId = appliedLoanId.get();

            // create charge
            double chargeAmount = 100.0;
            PostChargesResponse chargeResult = createCharge(chargeAmount);
            Long chargeId = chargeResult.getResourceId();

            // add charge after maturity
            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "01 February 2023", chargeAmount);
            Long loanChargeId = loanChargeResult.getResourceId();

            // verify N+1 installment in schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 0.0, 1000.0, false, "31 January 2023"), //
                    installment(0.0, 0.0, 100.0, 100.0, false, "01 February 2023") //
            );

            // waive charge
            waiveLoanCharge(loanId, loanChargeId, 2);
        });
        runAt("03 February 2023", () -> {
            Long loanId = appliedLoanId.get();

            // repay loan
            addRepaymentForLoan(loanId, amount, "03 February 2023");

            // verify maturity
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            // verify N+1 installment completion
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 0.0, 0.0, true, "31 January 2023"), //
                    installment(0.0, 0.0, 100.0, 0.0, true, "01 February 2023") //
            );

            // verify obligationsMetOnDate for N+1 installment
            LocalDate obligationsMetOnDate = Streams.findLast(loanDetails.getRepaymentSchedule().getPeriods().stream()).get()
                    .getObligationsMetOnDate();
            LocalDate expected = LocalDate.of(2023, 2, 1);
            assertEquals(expected, obligationsMetOnDate);
        });
    }

    @ParameterizedTest
    @MethodSource("processingStrategy")
    public void accrualIsCalculatedWhenThereIsWaivedChargeAndLoanIsClosed(boolean advancedPaymentStrategy) {
        double amount = 1000.0;
        AtomicLong appliedLoanId = new AtomicLong();
        String LoanCoBJobName = "Loan COB";

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product;
            if (advancedPaymentStrategy) {
                product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation();
            } else {
                product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
            }

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, 1);
            if (advancedPaymentStrategy) {
                applicationRequest = applicationRequest
                        .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY);
            }

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();
            appliedLoanId.set(loanId);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 January 2023");

            // verify schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 0.0, 1000.0, false, "31 January 2023"));
        });

        runAt("10 January 2023", () -> {
            Long loanId = appliedLoanId.get();

            // create charge
            double chargeAmount = 10.0;
            PostChargesResponse chargeResult = createCharge(chargeAmount);
            Long chargeId = chargeResult.getResourceId();

            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "09 January 2023", chargeAmount);
            loanChargeResult.getResourceId();
            this.schedulerJobHelper.executeAndAwaitJob(LoanCoBJobName);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 10.0, 1010.0, false, "31 January 2023") //

            );
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0));
        });
        runAt("11 January 2023", () -> {
            Long loanId = appliedLoanId.get();

            // create charge
            double chargeAmount = 9.0;
            PostChargesResponse chargeResult = createCharge(chargeAmount);
            Long chargeId = chargeResult.getResourceId();

            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "10 January 2023", chargeAmount);
            Long loanChargeId = loanChargeResult.getResourceId();
            this.schedulerJobHelper.executeAndAwaitJob(LoanCoBJobName);
            // waive charge
            waiveLoanCharge(loanId, loanChargeId, 1);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0), //
                    transaction(9.0, "Accrual", "10 January 2023", 0.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0), //
                    transaction(9.0, "Waive loan charges", "10 January 2023", 1000.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0) //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 19.0, 1010.0, false, "31 January 2023") //
            );
        });

        runAt("12 January 2023", () -> {
            Long loanId = appliedLoanId.get();

            // create charge
            double chargeAmount = 8.0;
            PostChargesResponse chargeResult = createCharge(chargeAmount);
            Long chargeId = chargeResult.getResourceId();

            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "11 January 2023", chargeAmount);
            loanChargeResult.getResourceId();

            loanTransactionHelper.makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("12 January 2023")
                    .dateFormat("dd MMMM yyyy").locale("en").transactionAmount(1018.0));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 10.0, 0.0, 0.0, 0.0), //
                    transaction(9.0, "Accrual", "10 January 2023", 0.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0), //
                    transaction(9.0, "Waive loan charges", "10 January 2023", 1000.0, 0.0, 0.0, 9.0, 0.0, 0.0, 0.0), //
                    transaction(1018.0, "Repayment", "12 January 2023", 0.0, 1000.0, 0.0, 18.0, 0.0, 0.0, 0.0), //
                    transaction(8.0, "Accrual", "12 January 2023", 0.0, 0.0, 0.0, 8.0, 0.0, 0.0, 0.0) //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(1000.0, 0.0, 27.0, 0.0, true, "31 January 2023") //
            );
        });

    }
}
