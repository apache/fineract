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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanDownPaymentTransactionChargebackTest extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);

    @Test
    public void loanDownPaymentTransactionChargebackTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(false);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    downPaymentTransaction_1.getResourceId(), "50.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("800.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // verify journal entries for chargeback transaction
            verifyTRJournalEntries(chargebackTransactionId, //
                    credit(fundSource, 50.0), //
                    debit(loansReceivableAccount, 50.0) //
            );
        });
    }

    @Test
    public void loanDownPaymentTransactionChargebackForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 3, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(45);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // make down payment
            final PostLoansLoanIdTransactionsResponse downPaymentTransaction_1 = loanTransactionHelper.makeLoanDownPayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("01 March 2023").locale("en")
                            .transactionAmount(250.0));
            assertNotNull(downPaymentTransaction_1);

            // chargeback down payment transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId.intValue(),
                    downPaymentTransaction_1.getResourceId(), "50.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId.intValue(), downPaymentTransaction_1.getResourceId(), 1, Double.valueOf("750.00"));
            reviewLoanTransactionRelations(loanId.intValue(), chargebackTransactionId, 0, Double.valueOf("800.00"));

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(0, null, "01 March 2023"), //
                    installment(250.0, true, "01 March 2023"), //
                    installment(300.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023")//
            );

            // verify journal entries for chargeback transaction
            verifyTRJournalEntries(chargebackTransactionId, //
                    credit(fundSource, 50.0), //
                    debit(loansReceivableAccount, 50.0) //
            );
        });
    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithEnableDownPayment(boolean isAdvancedPaymentStrategy) {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = isAdvancedPaymentStrategy
                ? createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                : createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(3);
        product.setRepaymentEvery(15);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(false);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();

    }

    private void reviewLoanTransactionRelations(final Integer loanId, final Long transactionId, final Integer expectedSize,
            final Double outstandingBalance) {

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper.getLoanTransaction(loanId,
                transactionId.intValue());
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());
        assertEquals(expectedSize, getLoansTransactionResponse.getTransactionRelations().size());
        // Outstanding amount
        assertEquals(outstandingBalance, getLoansTransactionResponse.getOutstandingLoanBalance());
    }
}
