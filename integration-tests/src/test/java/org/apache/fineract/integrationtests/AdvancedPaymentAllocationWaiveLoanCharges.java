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

import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class AdvancedPaymentAllocationWaiveLoanCharges extends BaseLoanIntegrationTest {

    @Test
    public void testAddFeeAndWaiveAdvancedPaymentAllocationNoBackdated() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()));
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");
            // Add Penalty
            Long loanChargeId = addCharge(loanId, false, 50, "01 January 2023");
            // When Waive Created Penalty
            loanTransactionHelper.waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, waiveTransaction.getLoanChargePaidByList().get(0).getAmount());
        });
    }

    @Test
    public void testAddPenaltyAndWaiveAdvancedPaymentAllocationNoBackDated() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()));
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");
            // Add Penalty
            Long loanChargeId = addCharge(loanId, true, 50, "01 January 2023");
            // When Waive Created Penalty
            loanTransactionHelper.waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, waiveTransaction.getLoanChargePaidByList().get(0).getAmount());
        });
    }

    @Test
    public void testAddPenaltyAndWaiveAdvancedPaymentAllocationAndBackdatedRepayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())); // Disburse
                                                                                                            // Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // set business date to
            updateBusinessDate("05 January 2023");

            // Add Penalty
            Long loanChargeId = addCharge(loanId, true, 50, "05 January 2023");

            // When Waive Created Penalty
            loanTransactionHelper.waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "05 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, waiveTransaction.getLoanChargePaidByList().get(0).getAmount());

            addRepaymentForLoan(loanId, 200.0, "03 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(200, "Repayment", "03 January 2023", 800.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "05 January 2023", 800.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );
        });
    }

    private AdvancedPaymentData createDefaultPaymentAllocationWithMixedGrouping() {
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

    protected Long createLoanProductWithAdvancedAllocation() {
        PostLoanProductsRequest req = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).loanScheduleType(LoanScheduleType.PROGRESSIVE.name())
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
        req.addPaymentAllocationItem(createDefaultPaymentAllocationWithMixedGrouping());
        PostLoanProductsResponse loanProduct = loanTransactionHelper.createLoanProduct(req);
        return loanProduct.getResourceId();
    }

}
