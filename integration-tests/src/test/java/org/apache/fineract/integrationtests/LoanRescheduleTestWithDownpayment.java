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

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanRescheduleTestWithDownpayment extends BaseLoanIntegrationTest {

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE = new BigDecimal(25);
    private final ClientHelper clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);

    private final LoanRescheduleRequestHelper loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec,
            this.responseSpec);

    @Test
    public void testRescheduleWithDownpayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith25PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1500.0, 2);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1500.0, null, "01 January 2023"), //
                    installment(375.0, false, "01 January 2023"), //
                    installment(563.0, false, "31 January 2023"), //
                    installment(562.0, false, "02 March 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(250.0, "Down Payment", "01 January 2023"), //
                    transaction(1000.0, "Disbursement", "01 January 2023") //
            );

            // verify journal entries
            verifyJournalEntries(loanId, journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(250.0, suspenseClearingAccount, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, suspenseClearingAccount, "CREDIT") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(375.0, false, "31 January 2023"), //
                    installment(375.0, false, "02 March 2023") //
            );

            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms(null).updateNewInterestRate(null).updateRescheduleFromDate("31 January 2023")
                    .updateAdjustedDueDate("15 February 2023").updateSubmittedOnDate("01 January 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("01 January 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, true, "01 January 2023"), //
                    installment(375.0, false, "15 February 2023"), //
                    installment(375.0, false, "17 March 2023") //
            );
        });
    }

    private Long createLoanProductWith25PctDownPayment(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0, getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

}
