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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanChargebackWithCreditAllocationsIntegrationTests extends BaseLoanIntegrationTest {

    @Test
    public void createLoanWithCreditAllocationAndChargebackPenaltyFeeInterestAndPrincipal() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProduct(chargebackAllocation("PENALTY", "FEE", "INTEREST", "PRINCIPAL"));

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Add Charges
            Long feeId = addCharge(loanId, false, 50, "15 January 2023");
            Long penaltyId = addCharge(loanId, true, 20, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 383.0, false, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Update Business Date
            updateBusinessDate("20 January 2023");

            // Add Repayment
            Long repaymentTransaction = addRepaymentForLoan(loanId, 383.0, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Add Chargeback
            addChargebackForLoan(loanId, repaymentTransaction, 100.0); // 20 penalty + 50 fee + 0 interest + 30
                                                                       // principal

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "20 January 2023", 1037.0, 30.0, 0.0, 50.0, 20.0, 0.0, 0.0) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(343.0, 0, 50, 20, 30.0, false, "01 February 2023"), // TODO: we still need to add the
                                                                                    // fee and the penalty to the
                                                                                    // outstanding
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );
        });
    }

    @Test
    public void createLoanWithCreditAllocationAndChargebackPenaltyFeeInterestAndPrincipalOnNPlusOneInstallment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProduct(chargebackAllocation("PENALTY", "FEE", "INTEREST", "PRINCIPAL"));

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Update Business Date + and make a full repayment for the first installment
            updateBusinessDate("20 January 2023");
            addRepaymentForLoan(loanId, 313.0, "20 January 2023");

            // Update Business Date + and make a full repayment for the second installment
            updateBusinessDate("20 February 2023");
            addRepaymentForLoan(loanId, 313.0, "20 February 2023");

            // Update Business Date + and make a full repayment for the third installment
            updateBusinessDate("20 March 2023");
            addRepaymentForLoan(loanId, 313.0, "20 March 2023");

            // Add some charges Update Business Date + and make a full repayment for the fourth installment
            updateBusinessDate("20 April 2023");
            addCharge(loanId, false, 50, "20 April 2023");
            addCharge(loanId, true, 20, "20 April 2023");
            Long repaymentTransaction = addRepaymentForLoan(loanId, 381.0, "20 April 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 April 2023"), //
                    installment(311.0, 0, 50, 20, 0.0, true, "01 May 2023") //
            );

            // Let's move over the maturity date and chargeback some money
            updateBusinessDate("02 May 2023");

            // Add Chargeback, 20 penalty + 50 fee + 0 interest + 30 principal
            addChargebackForLoan(loanId, repaymentTransaction, 100.0);

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(313.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(313.0, "Repayment", "20 February 2023", 624.0, 313.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(313.0, "Repayment", "20 March 2023", 311.0, 313.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(381.0, "Repayment", "20 April 2023", 0.0, 311.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(70.0, "Accrual", "20 April 2023", 0.0, 0.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "02 May 2023", 100.0, 30.0, 0.0, 50.0, 20.0, 0.0, 0.0) //
            );

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 0.0, true, "01 April 2023"), //
                    installment(311.0, 0, 50, 20, 0.0, true, "01 May 2023"), //
                    installment(30.0, 0, 0, 0, 30.0, false, "02 May 2023") // TODO: fee and penalty must be added here
                                                                           // after chargeback
            );
        });
    }

    @Test
    public void createLoanWithCreditAllocationAndChargebackReverseReplayWithBackdatedPayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProduct(chargebackAllocation("PENALTY", "FEE", "INTEREST", "PRINCIPAL"));

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Add Charges
            Long feeId = addCharge(loanId, false, 50, "15 January 2023");
            Long penaltyId = addCharge(loanId, true, 20, "15 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 383.0, false, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Update Business Date
            updateBusinessDate("20 January 2023");

            // Add Repayment
            Long repaymentTransaction = addRepaymentForLoan(loanId, 383.0, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            updateBusinessDate("21 January 2023");

            // Add Chargeback20 penalty + 50 fee + 0 interest + 30 principal
            addChargebackForLoan(loanId, repaymentTransaction, 100.0);

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "21 January 2023", 1037.0, 30.0, 0.0, 50.0, 20.0, 0.0, 0.0) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(343.0, 0, 50, 20, 30.0, false, "01 February 2023"), // TODO: we still need to add the
                    // fee and the penalty to the
                    // outstanding
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // let's add a backdated repayment on 19th of January to trigger reverse replaying the chargeback, that will
            // pay both the charges earlier.
            addRepaymentForLoan(loanId, 200.0, "19 January 2023");

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(200.0, "Repayment", "19 January 2023", 1120.0, 130.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 737.0, 383.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "21 January 2023", 837.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
        });
    }

    @Test
    public void createLoanWithCreditAllocationAndOnlyTheChargebackReverseReplayedWithBackdatedPayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProduct(chargebackAllocation("PENALTY", "FEE", "INTEREST", "PRINCIPAL"));

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Add Charges
            Long feeId = addCharge(loanId, false, 50, "15 January 2023");
            Long penaltyId = addCharge(loanId, true, 20, "15 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 383.0, false, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Update Business Date
            updateBusinessDate("20 January 2023");

            // Add Repayment
            Long repaymentTransaction = addRepaymentForLoan(loanId, 383.0, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            updateBusinessDate("22 January 2023");

            // Add Chargeback20 penalty + 50 fee + 0 interest + 30 principal
            addChargebackForLoan(loanId, repaymentTransaction, 100.0);

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "22 January 2023", 1037.0, 30.0, 0.0, 50.0, 20.0, 0.0, 0.0) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(343.0, 0, 50, 20, 30.0, false, "01 February 2023"), // TODO: we still need to add the
                    // fee and the penalty to the
                    // outstanding
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // let's add a backdated repayment on 21th of January that will reverse replay the chargeback transaction
            // but will leave the
            // original repayment from 20th of January unchanged.
            addRepaymentForLoan(loanId, 200.0, "21 January 2023");

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(200.0, "Repayment", "21 January 2023", 737.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "22 January 2023", 837.0, 30.0, 0.0, 50.0, 20.0, 0.0, 0.0) //
            );
        });
    }

    @Test
    public void createLoanWithCreditAllocationAndChargebackPrincipalInterestFeePenalty() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProduct(chargebackAllocation("PRINCIPAL", "INTEREST", "FEE", "PENALTY"));

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId);

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Add Charges
            Long feeId = addCharge(loanId, false, 50, "15 January 2023");
            Long penaltyId = addCharge(loanId, true, 20, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 383.0, false, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Update Business Date
            updateBusinessDate("20 January 2023");

            // Add Repayment
            Long repaymentTransaction = addRepaymentForLoan(loanId, 383.0, "20 January 2023");

            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(313.0, 0, 50, 20, 0.0, true, "01 February 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );

            // Add Chargeback
            addChargebackForLoan(loanId, repaymentTransaction, 100.0); // 100 principal, 0 interest, 0 fee 0 penalty

            verifyTransactions(loanId, //
                    transaction(1250.0, "Disbursement", "01 January 2023", 1250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(383.0, "Repayment", "20 January 2023", 937.0, 313.0, 0.0, 50.0, 20.0, 0.0, 0.0), //
                    transaction(100.0, "Chargeback", "20 January 2023", 1037.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 January 2023"), //
                    installment(413.0, 0, 50, 20, 100.0, false, "01 February 2023"), // TODO: we still need to add the
                    // fee and the penalty to the
                    // outstanding
                    installment(313.0, 0, 0, 0, 313.0, false, "01 March 2023"), //
                    installment(313.0, 0, 0, 0, 313.0, false, "01 April 2023"), //
                    installment(311.0, 0, 0, 0, 311.0, false, "01 May 2023") //
            );
        });
    }

    @Nullable
    private Long applyAndApproveLoan(Long clientId, Long loanProductId) {
        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", 1250.0, 4)//
                .repaymentEvery(1)//
                .loanTermFrequency(4)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy");

        PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

        PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                approveLoanRequest(1250.0, "01 January 2023"));

        Long loanId = approvedLoanResult.getLoanId();
        return loanId;
    }

    public Long createLoanProduct(CreditAllocationData... creditAllocationData) {
        PostLoanProductsRequest postLoanProductsRequest = loanProductWithAdvancedPaymentAllocationWith4Installments(creditAllocationData);
        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(postLoanProductsRequest);
        return loanProductResponse.getResourceId();
    }

    private PostLoanProductsRequest loanProductWithAdvancedPaymentAllocationWith4Installments(
            CreditAllocationData... creditAllocationData) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue())//
                .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()) //
                .loanScheduleProcessingType(LoanScheduleProcessingType.VERTICAL.toString()) //
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy")
                .paymentAllocation(List.of(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))
                .creditAllocation(Arrays.asList(creditAllocationData));
    }

    private AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private CreditAllocationData chargebackAllocation(String... allocationRules) {
        CreditAllocationData creditAllocationData = new CreditAllocationData();
        creditAllocationData.setTransactionType("CHARGEBACK");
        creditAllocationData.setCreditAllocationOrder(createCreditAllocationOrders(allocationRules));
        return creditAllocationData;
    }

    public List<CreditAllocationOrder> createCreditAllocationOrders(String... allocationRule) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(allocationRule).map(allocation -> {
            CreditAllocationOrder creditAllocationOrder = new CreditAllocationOrder();
            creditAllocationOrder.setCreditAllocationRule(allocation);
            creditAllocationOrder.setOrder(integer.getAndIncrement());
            return creditAllocationOrder;
        }).toList();
    }

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

}
