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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProgressiveLoanDisbursementAfterMaturityTest extends FeignLoanTestBase {

    @Test
    public void testSecondDisbursementAfterOriginalMaturityDate() {
        final Long clientId = createClient();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        // Create loan product with specific configurations for this test
        final Long loanProductId = createLoanProduct(create4IProgressive().multiDisburseLoan(true).maxTrancheCount(10)
                .disallowExpectedDisbursements(true).allowApprovedDisbursedAmountsOverApplied(true).overAppliedCalculationType("percentage")
                .overAppliedNumber(100).enableDownPayment(true).disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25.0))
                .enableAutoRepaymentForDownPayment(true)
                .addSupportedInterestRefundTypesItem(LoanTestData.SupportedInterestRefundTypesItem.MERCHANT_ISSUED_REFUND)
                .addSupportedInterestRefundTypesItem(LoanTestData.SupportedInterestRefundTypesItem.PAYOUT_REFUND)
                .paymentAllocation(List.of(
                        LoanRequestBuilders.paymentAllocation("DEFAULT", LoanTestData.FuturePaymentAllocationRule.NEXT_INSTALLMENT),
                        LoanRequestBuilders.paymentAllocation("DOWN_PAYMENT", LoanTestData.FuturePaymentAllocationRule.NEXT_INSTALLMENT),
                        LoanRequestBuilders.paymentAllocation("MERCHANT_ISSUED_REFUND",
                                LoanTestData.FuturePaymentAllocationRule.LAST_INSTALLMENT),
                        LoanRequestBuilders.paymentAllocation("PAYOUT_REFUND",
                                LoanTestData.FuturePaymentAllocationRule.LAST_INSTALLMENT))));

        runAt("14 March 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "14 March 2024", 1000.0, 0.0, 3, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(487.58), "14 March 2024");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);

            verifyTransactions(loanId, transaction(487.58, "Disbursement", "14 March 2024"),
                    transaction(121.90, "Down Payment", "14 March 2024"));

            assertEquals(0, BigDecimal.valueOf(365.68).compareTo(loanDetails.getSummary().getPrincipalOutstanding()));
        });

        // Step 4: Create first merchant issued refund on 24 March 2024 for €201.39
        runAt("24 March 2024", () -> {
            Long loanId = loanIdRef.get();

            PostLoansLoanIdTransactionsResponse mirResponse = makeLoanRepayment(loanId, "MerchantIssuedRefund", "24 March 2024", 201.39);
            Assertions.assertNotNull(mirResponse);
            Assertions.assertNotNull(mirResponse.getResourceId());

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);

            // Verify remaining balance
            assertEquals(0, BigDecimal.valueOf(164.29).compareTo(loanDetails.getSummary().getPrincipalOutstanding()));

            log.info("First MIR applied. Outstanding: €{}", loanDetails.getSummary().getPrincipalOutstanding());
        });

        // Step 5: Create second merchant issued refund on 24 March 2024 for €286.19 to overpay
        runAt("24 March 2024", () -> {
            Long loanId = loanIdRef.get();

            PostLoansLoanIdTransactionsResponse mirResponse = makeLoanRepayment(loanId, "MerchantIssuedRefund", "24 March 2024", 286.19);
            Assertions.assertNotNull(mirResponse);
            Assertions.assertNotNull(mirResponse.getResourceId());

            // After second MIR, the loan should be overpaid
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, LoanStatus.OVERPAID);

            // Verify overpaid amount
            assertEquals(0, BigDecimal.valueOf(121.90).compareTo(loanDetails.getTotalOverpaid()));
        });

        // Step 6: Create credit balance refund on 25 March 2024 to close the loan
        runAt("25 March 2024", () -> {
            Long loanId = loanIdRef.get();

            makeLoanRepayment(loanId, "CreditBalanceRefund", "25 March 2024", 121.90);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, LoanStatus.CLOSED_OBLIGATIONS_MET);

            assertEquals(0, BigDecimal.ZERO.compareTo(loanDetails.getSummary().getPrincipalOutstanding()));
        });

        runAt("1 April 2025", () -> {
            Long loanId = loanIdRef.get();

            try {
                // Attempt second disbursement after original maturity date
                disburseLoan(loanId, BigDecimal.valueOf(312.69), "1 April 2025");

                // If disbursement succeeds, verify the loan is active again
                GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);

                // Verify second disbursement and automatic downpayment
                verifyTransactions(loanId, transaction(487.58, "Disbursement", "14 March 2024"),
                        transaction(121.90, "Down Payment", "14 March 2024"),
                        transaction(201.39, "Merchant Issued Refund", "24 March 2024"),
                        transaction(286.19, "Merchant Issued Refund", "24 March 2024"),
                        transaction(121.90, "Credit Balance Refund", "25 March 2024"), transaction(312.69, "Disbursement", "01 April 2025"),
                        transaction(78.17, "Down Payment", "01 April 2025")); // 25% of 312.69

                // Verify outstanding balance after second disbursement
                BigDecimal expectedOutstanding = BigDecimal.valueOf(312.69).subtract(BigDecimal.valueOf(78.17));
                assertEquals(0, expectedOutstanding.compareTo(loanDetails.getSummary().getPrincipalOutstanding()));

            } catch (Exception e) {
                log.error("Second disbursement failed after maturity date: {}", e.getMessage());
                Assertions.fail("Second disbursement should be allowed after original maturity date: " + e.getMessage());
            }
        });
    }
}
