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

    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE_20 = new BigDecimal(20);
    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE_25 = new BigDecimal(25);
    public static final BigDecimal DOWN_PAYMENT_PERCENTAGE_33 = new BigDecimal(33);

    private final LoanRescheduleRequestHelper loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec,
            this.responseSpec);

    @Test
    public void testRescheduleWithDownPayment() {
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
                    journalEntry(250.0, fundSource, "DEBIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(1000.0, fundSource, "CREDIT") //
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

    @Test
    public void testRescheduleAddExtraInstallmentsWithDownPayment() {
        runAt("01 November 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith33PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "02 October 2023", 1000.0, 3);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, false, "02 October 2023"), //
                    installment(223.33, false, "01 November 2023"), //
                    installment(223.33, false, "01 December 2023"), //
                    installment(223.34, false, "31 December 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "02 October 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(330.0, "Down Payment", "02 October 2023"), //
                    transaction(1000.0, "Disbursement", "02 October 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, true, "02 October 2023"), //
                    installment(223.33, false, "01 November 2023"), //
                    installment(223.33, false, "01 December 2023"), //
                    installment(223.34, false, "31 December 2023") //
            );

            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms("2").updateNewInterestRate(null).updateRescheduleFromDate("01 November 2023")
                    .updateAdjustedDueDate(null).updateSubmittedOnDate("01 November 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("01 November 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, true, "02 October 2023"), //
                    installment(134.0, false, "01 November 2023"), //
                    installment(134.0, false, "01 December 2023"), //
                    installment(134.0, false, "31 December 2023"), //
                    installment(134.0, false, "30 January 2024"), //
                    installment(134.0, false, "29 February 2024") //
            );
        });
    }

    @Test
    public void testRescheduleAddExtraInstallmentsMultipleDisbursementWithDownPayment() {
        runAt("31 December 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith33PctDownPayment(true, true);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "02 October 2023", 1000.0, 3);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, false, "02 October 2023"), //
                    installment(223.33, false, "01 November 2023"), //
                    installment(223.33, false, "01 December 2023"), //
                    installment(223.34, false, "31 December 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "02 October 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(330.0, "Down Payment", "02 October 2023"), //
                    transaction(1000.0, "Disbursement", "02 October 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, true, "02 October 2023"), //
                    installment(223.33, false, "01 November 2023"), //
                    installment(223.33, false, "01 December 2023"), //
                    installment(223.34, false, "31 December 2023") //
            );

            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(200.00), "02 December 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(330.0, "Down Payment", "02 October 2023"), //
                    transaction(1000.0, "Disbursement", "02 October 2023"), //
                    transaction(200.0, "Disbursement", "02 December 2023"), //
                    transaction(66.0, "Down Payment", "02 December 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, true, "02 October 2023"), //
                    installment(268.0, false, "01 November 2023"), //
                    installment(268.0, false, "01 December 2023"), //
                    installment(200.0, null, "02 December 2023"), //
                    installment(66.0, false, "02 December 2023"), //
                    installment(268.0, false, "31 December 2023") //
            );

            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms("2").updateNewInterestRate(null).updateRescheduleFromDate("31 December 2023")
                    .updateAdjustedDueDate(null).updateSubmittedOnDate("31 December 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("31 December 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "02 October 2023"), //
                    installment(330.0, true, "02 October 2023"), //
                    installment(268, false, "01 November 2023"), //
                    installment(268, false, "01 December 2023"), //
                    installment(200.0, null, "02 December 2023"), //
                    installment(66.0, false, "02 December 2023"), //
                    installment(89.33, false, "31 December 2023"), //
                    installment(89.33, false, "30 January 2024"), //
                    installment(89.34, false, "29 February 2024") //
            );
        });
    }

    @Test
    public void testRescheduleAddExtraInstallmentsDisbursementWithDownPaymentWithInterest() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith20PctDownPaymentWithDecliningBalanceInterest(true, true, 5.0);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 15000.0, 6, postLoansRequest -> {
                postLoansRequest.interestRatePerPeriod(BigDecimal.valueOf(5));
                postLoansRequest.interestRatePerPeriod(BigDecimal.valueOf(5));
                postLoansRequest.repaymentEvery(1);
                postLoansRequest.loanTermFrequencyType(RepaymentFrequencyType.MONTHS);
                postLoansRequest.repaymentFrequencyType(RepaymentFrequencyType.MONTHS);
                postLoansRequest.loanTermFrequency(6);
            });

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(3000.00, false, "01 January 2023"), //
                    installment(1764.21, false, "01 February 2023"), //
                    installment(1852.42, false, "01 March 2023"), //
                    installment(1945.04, false, "01 April 2023"), //
                    installment(2042.29, false, "01 May 2023"), //
                    installment(2144.41, false, "01 June 2023"), //
                    installment(2251.63, false, "01 July 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(12000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(2400.0, "Down Payment", "01 January 2023"), //
                    transaction(12000.0, "Disbursement", "01 January 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(2400.00, true, "01 January 2023"), //
                    installment(1411.37, false, "01 February 2023"), //
                    installment(1481.94, false, "01 March 2023"), //
                    installment(1556.04, false, "01 April 2023"), //
                    installment(1633.84, false, "01 May 2023"), //
                    installment(1715.53, false, "01 June 2023"), //
                    installment(1801.28, false, "01 July 2023") //
            );

            updateBusinessDate("01 June 2023");
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms("2").updateNewInterestRate(null).updateRescheduleFromDate("01 June 2023").updateAdjustedDueDate(null)
                    .updateSubmittedOnDate("01 June 2023").updateRescheduleReasonId("1").build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("01 June 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(2400.00, true, "01 January 2023"), //
                    installment(1411.37, false, "01 February 2023"), //
                    installment(1481.94, false, "01 March 2023"), //
                    installment(1556.04, false, "01 April 2023"), //
                    installment(1633.84, false, "01 May 2023"), //
                    installment(815.94, false, "01 June 2023"), //
                    installment(856.74, false, "01 July 2023"), //
                    installment(899.57, false, "01 August 2023"), //
                    installment(944.56, false, "01 September 2023") //
            );
        });
    }

    @Test
    public void testRescheduleAddExtraInstallmentsMultipleDisbursementWithDownPaymentWithInterest() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            Long loanProductId = createLoanProductWith20PctDownPaymentWithDecliningBalanceInterest(true, true, 5.0);

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 15000.0, 6, postLoansRequest -> {
                postLoansRequest.interestRatePerPeriod(BigDecimal.valueOf(5));
                postLoansRequest.interestRatePerPeriod(BigDecimal.valueOf(5));
                postLoansRequest.repaymentEvery(1);
                postLoansRequest.loanTermFrequencyType(RepaymentFrequencyType.MONTHS);
                postLoansRequest.repaymentFrequencyType(RepaymentFrequencyType.MONTHS);
                postLoansRequest.loanTermFrequency(6);
            });

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(3000.00, false, "01 January 2023"), //
                    installment(1764.21, false, "01 February 2023"), //
                    installment(1852.42, false, "01 March 2023"), //
                    installment(1945.04, false, "01 April 2023"), //
                    installment(2042.29, false, "01 May 2023"), //
                    installment(2144.41, false, "01 June 2023"), //
                    installment(2251.63, false, "01 July 2023") //
            );

            // 1st Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(12000.00), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(2400.0, "Down Payment", "01 January 2023"), //
                    transaction(12000.0, "Disbursement", "01 January 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(2400.00, true, "01 January 2023"), //
                    installment(1411.37, false, "01 February 2023"), //
                    installment(1481.94, false, "01 March 2023"), //
                    installment(1556.04, false, "01 April 2023"), //
                    installment(1633.84, false, "01 May 2023"), //
                    installment(1715.53, false, "01 June 2023"), //
                    installment(1801.28, false, "01 July 2023") //
            );

            updateBusinessDate("02 May 2023");
            // 2nd Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(3000.00), "02 May 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(2400.0, "Down Payment", "01 January 2023"), //
                    transaction(12000.0, "Disbursement", "01 January 2023"), //
                    transaction(3000.00, "Disbursement", "02 May 2023"), //
                    transaction(600.0, "Down Payment", "02 May 2023") //
            );

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(2400.00, true, "01 January 2023"), //
                    installment(1884.21, false, "01 February 2023"), //
                    installment(1978.42, false, "01 March 2023"), //
                    installment(2077.34, false, "01 April 2023"), //
                    installment(2181.21, false, "01 May 2023"), //
                    installment(3000.0, null, "02 May 2023"), //
                    installment(600.0, false, "02 May 2023"), //
                    installment(2174.14, false, "01 June 2023"), //
                    installment(1704.68, false, "01 July 2023") //
            );

            updateBusinessDate("01 June 2023");
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms("2").updateNewInterestRate(null).updateRescheduleFromDate("01 June 2023").updateAdjustedDueDate(null)
                    .updateSubmittedOnDate("01 June 2023").updateRescheduleReasonId("1").build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("01 June 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(12000.0, null, "01 January 2023"), //
                    installment(2400.00, true, "01 January 2023"), //
                    installment(1884.21, false, "01 February 2023"), //
                    installment(1978.42, false, "01 March 2023"), //
                    installment(2077.34, false, "01 April 2023"), //
                    installment(2181.21, false, "01 May 2023"), //
                    installment(3000.0, null, "02 May 2023"), //
                    installment(600.0, false, "02 May 2023"), //
                    installment(903.80, false, "01 June 2023"), //
                    installment(945.12, false, "01 July 2023"), //
                    installment(992.37, false, "01 August 2023"), //
                    installment(1037.53, false, "01 September 2023")//
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
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE_25);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0,
                getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE_25));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

    private Long createLoanProductWith33PctDownPayment(boolean autoDownPaymentEnabled, boolean multiDisburseEnabled) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.repaymentEvery(1);
        product.repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue());
        product.installmentAmountInMultiplesOf(null);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE_33);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0,
                getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE_33));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

    private Long createLoanProductWith20PctDownPaymentWithDecliningBalanceInterest(boolean autoDownPaymentEnabled,
            boolean multiDisburseEnabled, double interestRate) {
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.repaymentEvery(1);
        product.repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue());
        product.installmentAmountInMultiplesOf(null);
        product.interestRatePerPeriod(interestRate);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        product.setEnableDownPayment(true);
        product.setDisbursedAmountPercentageForDownPayment(DOWN_PAYMENT_PERCENTAGE_20);
        product.setEnableAutoRepaymentForDownPayment(autoDownPaymentEnabled);

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());

        Long loanProductId = loanProductResponse.getResourceId();

        assertEquals(TRUE, getLoanProductsProductIdResponse.getEnableDownPayment());
        assertNotNull(getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment());
        assertEquals(0,
                getLoanProductsProductIdResponse.getDisbursedAmountPercentageForDownPayment().compareTo(DOWN_PAYMENT_PERCENTAGE_20));
        assertEquals(autoDownPaymentEnabled, getLoanProductsProductIdResponse.getEnableAutoRepaymentForDownPayment());
        assertEquals(multiDisburseEnabled, getLoanProductsProductIdResponse.getMultiDisburseLoan());
        return loanProductId;
    }

}
