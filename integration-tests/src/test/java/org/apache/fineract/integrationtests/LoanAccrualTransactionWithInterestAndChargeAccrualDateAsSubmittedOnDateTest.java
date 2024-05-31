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

import static org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY;

import java.math.BigDecimal;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.junit.jupiter.api.Test;

public class LoanAccrualTransactionWithInterestAndChargeAccrualDateAsSubmittedOnDateTest extends BaseLoanIntegrationTest {

    private SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);

    @Test
    public void accrualTransactionForInterestBearingLoan_WithoutCharges_SubmittedOnDateAsChargeAccrualDateWorksTest() {
        runAt("15 April 2024", () -> {

            try {
                // Configure Charge accrual date as submitted on date
                GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(this.requestSpec, this.responseSpec, "submitted-date");

                // Create Client
                Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

                // Create Loan Product
                PostLoanProductsRequest loanProductsRequest = createLoanProductWithInterestCalculation();
                PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

                // Apply and Approve Loan
                Long loanId = applyAndApproveLoanApplication(clientId, loanProductResponse.getResourceId(), "15 April 2024", 1000.0, 4);

                // Disburse Loan
                disburseLoan(loanId, BigDecimal.valueOf(500), "15 April 2024");

                // Verify Repayment Schedule and Due Dates
                verifyRepaymentSchedule(loanId, //
                        installment(500.0, null, "15 April 2024"), //
                        installment(114.41, 29.59, 0.0, 0.0, 144.0, false, "30 April 2024"), //
                        installment(121.18, 22.82, 0.0, 0.0, 144.0, false, "15 May 2024"), //
                        installment(128.35, 15.65, 0.0, 0.0, 144.0, false, "30 May 2024"), //
                        installment(136.06, 8.05, 0.0, 0.0, 144.11, false, "14 June 2024") //
                );

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
                );

                // update business date
                updateBusinessDate("25 April 2024");

                // run cob
                schedulerJobHelper.executeAndAwaitJob("Loan COB");

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                        transaction(17.75, "Accrual", "24 April 2024", 0.0, 0.0, 17.75, 0.0, 0.0, 0.0, 0.0) //
                );

                // update business date
                updateBusinessDate("26 April 2024");

                // disburse amount
                disburseLoan(loanId, BigDecimal.valueOf(500), "26 April 2024");

                // Verify Repayment Schedule and Due Dates
                verifyRepaymentSchedule(loanId, //
                        installment(500.0, null, "15 April 2024"), //
                        installment(500.0, null, "26 April 2024"), //
                        installment(250.52, 37.48, 0.0, 0.0, 288.0, false, "30 April 2024"), //
                        installment(243.65, 44.35, 0.0, 0.0, 288.0, false, "15 May 2024"), //
                        installment(258.07, 29.93, 0.0, 0.0, 288.0, false, "30 May 2024"), //
                        installment(247.76, 14.66, 0.0, 0.0, 262.42, false, "14 June 2024") //
                );

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                        transaction(22.49, "Accrual", "24 April 2024", 0.0, 0.0, 22.49, 0.0, 0.0, 0.0, 0.0), //
                        transaction(500.0, "Disbursement", "26 April 2024", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            } finally {
                GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(this.requestSpec, this.responseSpec, "due-date");
            }

        });

    }

    @Test
    public void accrualTransactionForInterestBearingLoan_WithCharges_SubmittedOnDateAsChargeAccrualDateWorksTest() {
        runAt("15 April 2024", () -> {

            try {
                // Configure Charge accrual date as submitted on date
                GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(this.requestSpec, this.responseSpec, "submitted-date");

                // Create Client
                Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

                // Create Loan Product
                PostLoanProductsRequest loanProductsRequest = createLoanProductWithInterestCalculation();
                PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(loanProductsRequest);

                // Apply and Approve Loan
                Long loanId = applyAndApproveLoanApplication(clientId, loanProductResponse.getResourceId(), "15 April 2024", 1000.0, 4);

                // Disburse Loan
                disburseLoan(loanId, BigDecimal.valueOf(500), "15 April 2024");

                // Verify Repayment Schedule and Due Dates
                verifyRepaymentSchedule(loanId, //
                        installment(500.0, null, "15 April 2024"), //
                        installment(114.41, 29.59, 0.0, 0.0, 144.0, false, "30 April 2024"), //
                        installment(121.18, 22.82, 0.0, 0.0, 144.0, false, "15 May 2024"), //
                        installment(128.35, 15.65, 0.0, 0.0, 144.0, false, "30 May 2024"), //
                        installment(136.06, 8.05, 0.0, 0.0, 144.11, false, "14 June 2024") //
                );

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
                );

                // update business date
                updateBusinessDate("24 April 2024");

                // add charge
                addCharge(loanId, false, 10.0, "29 April 2024");

                // Verify Repayment Schedule and Due Dates
                verifyRepaymentSchedule(loanId, //
                        installment(500.0, null, "15 April 2024"), //
                        installment(114.41, 29.59, 10.0, 0.0, 154.0, false, "30 April 2024"), //
                        installment(121.18, 22.82, 0.0, 0.0, 144.0, false, "15 May 2024"), //
                        installment(128.35, 15.65, 0.0, 0.0, 144.0, false, "30 May 2024"), //
                        installment(136.06, 8.05, 0.0, 0.0, 144.11, false, "14 June 2024") //
                );

                // update business date
                updateBusinessDate("25 April 2024");

                // run cob
                schedulerJobHelper.executeAndAwaitJob("Loan COB");

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                        transaction(27.75, "Accrual", "24 April 2024", 0.0, 0.0, 17.75, 10.0, 0.0, 0.0, 0.0) //
                );

                // update business date
                updateBusinessDate("26 April 2024");

                // disburse amount
                disburseLoan(loanId, BigDecimal.valueOf(500), "26 April 2024");

                // Verify Repayment Schedule and Due Dates
                verifyRepaymentSchedule(loanId, //
                        installment(500.0, null, "15 April 2024"), //
                        installment(500.0, null, "26 April 2024"), //
                        installment(250.52, 37.48, 10.0, 0.0, 298.0, false, "30 April 2024"), //
                        installment(243.65, 44.35, 0.0, 0.0, 288.0, false, "15 May 2024"), //
                        installment(258.07, 29.93, 0.0, 0.0, 288.0, false, "30 May 2024"), //
                        installment(247.76, 14.66, 0.0, 0.0, 262.42, false, "14 June 2024") //
                );

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                        transaction(32.49, "Accrual", "24 April 2024", 0.0, 0.0, 22.49, 10.0, 0.0, 0.0, 0.0), //
                        transaction(500.0, "Disbursement", "26 April 2024", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

                // run cob
                schedulerJobHelper.executeAndAwaitJob("Loan COB");

                verifyTransactions(loanId, //
                        transaction(500.0, "Disbursement", "15 April 2024", 500.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                        transaction(32.49, "Accrual", "24 April 2024", 0.0, 0.0, 22.49, 10.0, 0.0, 0.0, 0.0), //
                        transaction(2.50, "Accrual", "25 April 2024", 0.0, 0.0, 2.50, 0.0, 0.0, 0.0, 0.0), //
                        transaction(500.0, "Disbursement", "26 April 2024", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            } finally {
                GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(this.requestSpec, this.responseSpec, "due-date");
            }

        });

    }

    private Long applyAndApproveLoanApplication(Long clientId, Long productId, String disbursementDate, double amount,
            int numberOfRepayments) {
        PostLoansRequest postLoansRequest = new PostLoansRequest().clientId(clientId).productId(productId)
                .expectedDisbursementDate(disbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY)
                .locale("en").submittedOnDate(disbursementDate).amortizationType(AmortizationType.EQUAL_INSTALLMENTS)
                .interestRatePerPeriod(new BigDecimal(12.0))
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .interestType(InterestType.DECLINING_BALANCE).repaymentEvery(15).repaymentFrequencyType(RepaymentFrequencyType.DAYS)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(numberOfRepayments * 15).loanTermFrequencyType(0)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(amount)).principal(BigDecimal.valueOf(amount)).loanType("individual");
        PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(postLoansRequest);
        PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                approveLoanRequest(amount, disbursementDate));
        return approvedLoanResult.getLoanId();
    }

    private PostLoanProductsRequest createLoanProductWithInterestCalculation() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().multiDisburseLoan(true)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .principal(1000.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(15)//
                .repaymentFrequencyType(RepaymentFrequencyType.DAYS.longValue())//
                .interestType(InterestType.DECLINING_BALANCE)//
                .amortizationType(AmortizationType.EQUAL_INSTALLMENTS)//
                .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .interestRatePerPeriod(12.0) //
                .interestRateFrequencyType(InterestRateFrequencyType.MONTHS)//
                .isInterestRecalculationEnabled(true) //
                .interestRecalculationCompoundingMethod(0).rescheduleStrategyMethod(3).recalculationRestFrequencyType(1)
                .recalculationRestFrequencyInterval(1);
    }
}
