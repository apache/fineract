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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Test;

public class LoanRepaymentScheduleForChargesAfterMaturityTest extends BaseLoanIntegrationTest {

    private final LoanRescheduleRequestHelper loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec,
            this.responseSpec);

    @Test
    public void loanNPlusOneInstallmentIsRetainedAfterLoanRescheduleTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepayments();

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 4, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(60);
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023")//
            );

            // add charge with due date after loan maturity date
            Long loanChargeId = addCharge(loanId, false, 50, "23 May 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "23 May 2023")//
            );

            // reschedule installment date
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms(null).updateNewInterestRate(null).updateRescheduleFromDate("15 April 2023")
                    .updateAdjustedDueDate("30 April 2023").updateSubmittedOnDate("03 March 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("03 March 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(250.0, false, "15 May 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "23 May 2023")//
            );

        });
    }

    @Test
    public void loanNPlusOneInstallmentIsAdjustedAfterRescheduleIfDateFallBeforeMaturityDateTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepayments();

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 4, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(60);
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023")//
            );

            // add charge with due date after loan maturity date but date which is with in installment date after
            // reschedule
            Long loanChargeId = addCharge(loanId, false, 50, "13 May 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "13 May 2023")//
            );

            // reschedule installment date
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms(null).updateNewInterestRate(null).updateRescheduleFromDate("15 April 2023")
                    .updateAdjustedDueDate("30 April 2023").updateSubmittedOnDate("03 March 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("03 March 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(250.0, 0.0, 50.0, 300.0, false, "15 May 2023")//
            );

        });
    }

    @Test
    public void loanNPlusOneInstallmentIsRetainedAfterLoanRescheduleForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithAdvancedPaymentAllocationStrategy();

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 4, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(60);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
                req.setLoanScheduleProcessingType(LoanScheduleType.PROGRESSIVE.toString());
                req.setLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023")//
            );

            // add charge with due date after loan maturity date
            Long loanChargeId = addCharge(loanId, false, 50, "23 May 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "23 May 2023")//
            );

            // reschedule installment date
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms(null).updateNewInterestRate(null).updateRescheduleFromDate("15 April 2023")
                    .updateAdjustedDueDate("30 April 2023").updateSubmittedOnDate("03 March 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("03 March 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(250.0, false, "15 May 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "23 May 2023")//
            );

        });
    }

    @Test
    public void loanNPlusOneInstallmentIsAdjustedAfterRescheduleIfDateFallBeforeMaturityDateForAdvancedPaymentAllocationTest() {
        runAt("03 March 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            // Create Loan Product
            Long loanProductId = createLoanProductWithMultiDisbursalAndRepaymentsWithAdvancedPaymentAllocationStrategy();

            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 March 2023", 1500.0, 4, req -> {
                req.setRepaymentEvery(15);
                req.setLoanTermFrequency(60);
                req.setTransactionProcessingStrategyCode("advanced-payment-allocation-strategy");
            });

            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 March 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023")//
            );

            // add charge with due date after loan maturity date but date which is with in installment date after
            // reschedule
            Long loanChargeId = addCharge(loanId, false, 50, "13 May 2023");

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "15 April 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(0.0, 0.0, 50.0, 50.0, false, "13 May 2023")//
            );

            // reschedule installment date
            String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnInterest(null).updateGraceOnPrincipal(null)
                    .updateExtraTerms(null).updateNewInterestRate(null).updateRescheduleFromDate("15 April 2023")
                    .updateAdjustedDueDate("30 April 2023").updateSubmittedOnDate("03 March 2023").updateRescheduleReasonId("1")
                    .build(loanId.toString());

            Integer loanRescheduleRequest = loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            requestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("03 March 2023")
                    .getApproveLoanRescheduleRequestJSON();
            Integer approveLoanRescheduleRequest = loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequest,
                    requestJSON);

            // verify repayment schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 March 2023"), //
                    installment(250.0, false, "16 March 2023"), //
                    installment(250.0, false, "31 March 2023"), //
                    installment(250.0, false, "30 April 2023"), //
                    installment(250.0, 0.0, 50.0, 300.0, false, "15 May 2023")//
            );

        });
    }

    private Long createLoanProductWithMultiDisbursalAndRepayments() {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(4);
        product.setRepaymentEvery(15);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();

    }

    private Long createLoanProductWithMultiDisbursalAndRepaymentsWithAdvancedPaymentAllocationStrategy() {
        boolean multiDisburseEnabled = true;
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation();
        product.setMultiDisburseLoan(multiDisburseEnabled);
        product.setNumberOfRepayments(4);
        product.setRepaymentEvery(15);

        if (!multiDisburseEnabled) {
            product.disallowExpectedDisbursements(null);
            product.setAllowApprovedDisbursedAmountsOverApplied(null);
            product.overAppliedCalculationType(null);
            product.overAppliedNumber(null);
        }

        PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
        GetLoanProductsProductIdResponse getLoanProductsProductIdResponse = loanProductHelper
                .retrieveLoanProductById(loanProductResponse.getResourceId());
        assertNotNull(getLoanProductsProductIdResponse);
        return loanProductResponse.getResourceId();
    }
}
