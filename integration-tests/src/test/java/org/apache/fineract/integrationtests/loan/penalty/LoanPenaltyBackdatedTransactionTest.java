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
package org.apache.fineract.integrationtests.loan.penalty;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class LoanPenaltyBackdatedTransactionTest extends FeignLoanTestBase {

    // PeriodFrequencyType.DAYS
    private static final String FEE_FREQUENCY_DAYS = "0";

    @BeforeEach
    public void before() {
        PutGlobalConfigurationsRequest request = new PutGlobalConfigurationsRequest().value(0L).enabled(true);
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD, request);
    }

    @AfterEach
    public void after() {
        // go back to defaults
        PutGlobalConfigurationsRequest request = new PutGlobalConfigurationsRequest().value(2L).enabled(true);
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.PENALTY_WAIT_PERIOD, request);
    }

    @Test
    public void test_PenaltyRecalculationWorksForBackdatedTx_WhenCumulative_1() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            Long loanId = createBackdatedPenaltyLoan();
            aLoanId.set(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023") //
            );
        });

        runAt("09 January 2023", () -> {
            Long loanId = aLoanId.get();
            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // repay 1k
            addRepaymentForLoan(loanId, 1000.0, "07 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // reverse accruals
            deactivateOverdueLoanCharges(loanId, "07 January 2023");

            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0, true), //
                    transaction(30.33, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 30.33, 0.0, 0.0) //
            );
        });
    }

    @Test
    public void test_PenaltyRecalculationWorksForBackdatedTx_WhenCumulative_2() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            Long loanId = createBackdatedPenaltyLoan();
            aLoanId.set(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023") //
            );
        });

        runAt("09 January 2023", () -> {
            Long loanId = aLoanId.get();

            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // repay 1k
            addRepaymentForLoan(loanId, 1000.0, "07 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // reverse accruals
            deactivateOverdueLoanCharges(loanId, "05 January 2023");

            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0, true), //
                    transaction(6.83, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 6.83, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4006.83, 993.17, 0.0, 0.0, 6.83, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0, true), //
                    transaction(20.49, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 20.49, 0.0, 0.0) //
            );
        });
    }

    @Test
    public void test_PenaltyRecalculationWorksForBackdatedTx_WhenCumulative_3() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            Long loanId = createBackdatedPenaltyLoan();
            // Create Client
            aLoanId.set(loanId);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023") //
            );
        });

        runAt("09 January 2023", () -> {
            Long loanId = aLoanId.get();

            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // repay 1k
            addRepaymentForLoan(loanId, 1000.0, "07 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0) //
            );

            // reverse accruals
            deactivateOverdueLoanCharges(loanId, "07 January 2023");

            // run accrual posting
            schedulerHelper.executeAndAwaitJob("Apply penalty to overdue loans");
            schedulerHelper.executeAndAwaitJob("Add Accrual Transactions");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0, true), //
                    transaction(30.33, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 30.33, 0.0, 0.0) //
            );
        });

        runAt("10 January 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 1k
            addRepaymentForLoan(loanId, 1000.0, "10 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023", 5000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(16.67, "Accrual", "05 January 2023", 0.0, 0.0, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "07 January 2023", 4016.67, 983.33, 0.0, 0.0, 16.67, 0.0, 0.0), //
                    transaction(50.01, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 50.01, 0.0, 0.0, true), //
                    transaction(30.33, "Accrual", "09 January 2023", 0.0, 0.0, 0.0, 0.0, 30.33, 0.0, 0.0), //
                    transaction(1000.0, "Repayment", "10 January 2023", 3047.0, 969.67, 0.0, 0.0, 30.33, 0.0, 0.0) //
            );
        });
    }

    private Long createBackdatedPenaltyLoan() {
        // Create Client
        Long clientId = createClient();

        int numberOfRepayments = 3;
        int repaymentEvery = 4;

        // Create charges
        double chargeAmount = 1.0;
        Long chargeId = createOverduePenaltyPercentageCharge(chargeAmount, FEE_FREQUENCY_DAYS, 1);

        // Create Loan Product
        PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                .graceOnArrearsAgeing(0).numberOfRepayments(numberOfRepayments) //
                .repaymentEvery(repaymentEvery) //
                .installmentAmountInMultiplesOf(null) //
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS_L) //
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE)//
                .isInterestRecalculationEnabled(true)//
                .recalculationRestFrequencyInterval(1)//
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.DAILY)//
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                .allowPartialPeriodInterestCalculation(false)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedNumber(null)//
                .overAppliedCalculationType(null)//
                .multiDisburseLoan(null)//
                .charges(List.of(new LoanProductChargeData().id(chargeId)));//

        Long loanProductId = createLoanProduct(product);

        // Apply and Approve Loan
        double amount = 5000.0;

        PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                .repaymentEvery(repaymentEvery)//
                .loanTermFrequency(numberOfRepayments * repaymentEvery)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.DAYS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);

        Long loanId = applyForLoan(applicationRequest);
        approveLoan(loanId, approveLoanRequest(amount, "01 January 2023"));

        // disburse Loan
        disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");
        return loanId;
    }

    private Long createOverduePenaltyPercentageCharge(double percentageAmount, String feeFrequency, int feeInterval) {
        ChargeRequest chargeRequest = ChargeRequestBuilders.loanOverdueFee(percentageAmount)//
                .chargeCalculationType(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST.getValue())//
                .feeFrequency(feeFrequency)//
                .feeInterval(String.valueOf(feeInterval));
        return chargesHelper.createCharge(chargeRequest).getResourceId();
    }
}
