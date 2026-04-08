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
package org.apache.fineract.integrationtests.loan.pointintime;

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.LoanPointInTimeData;
import org.apache.fineract.client.models.LoanProductChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.BaseLoanIntegrationTest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.junit.jupiter.api.Test;

public class LoanPointInTimeTest extends BaseLoanIntegrationTest {

    @Test
    public void test_LoanPointInTimeDataWorks_ForPrincipalOutstandingCalculation() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create charges
            double charge1Amount = 1.0;
            double charge2Amount = 1.5;
            Long charge1Id = createDisbursementPercentageCharge(charge1Amount);
            Long charge2Id = createDisbursementPercentageCharge(charge2Amount);

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null)//
                    .charges(List.of(new LoanProductChargeData().id(charge1Id), new LoanProductChargeData().id(charge2Id)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .charges(List.of(//
                            new PostLoansRequestChargeData().chargeId(charge1Id).amount(BigDecimal.valueOf(charge1Amount)), //
                            new PostLoansRequestChargeData().chargeId(charge2Id).amount(BigDecimal.valueOf(charge2Amount))//
            ));//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );
        });

        runAt("01 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023") //
            );
        });

        runAt("09 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "09 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            pointInTimeData = getPointInTimeData(loanId, "07 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            pointInTimeData = getPointInTimeData(loanId, "09 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4000.0, 0.0, 0.0, 0.0, 4000.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023") //
            );
        });

        runAt("01 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 March 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4000.0, 0.0, 0.0, 0.0, 4000.0));

            pointInTimeData = getPointInTimeData(loanId, "01 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(3500.0, 0.0, 0.0, 0.0, 3500.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023") //
            );
        });
    }

    @Test
    public void test_LoanPointInTimeDataWorks_ForAllOutstandingCalculation_WhenLoanIsCumulative_AndInterestIsEnabled() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestRatePerPeriod(10.0)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null);//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .interestRatePerPeriod(BigDecimal.valueOf(10.0))//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY);//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            Long chargeId = createCharge(100.0).getResourceId();
            addLoanCharge(loanId, chargeId, "02 February 2023", 100.0);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023") //
            );
        });

        runAt("01 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 250.0, "01 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 767.70, 0.0, 0.0, 5767.70));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023") //
            );
        });

        runAt("09 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "09 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 767.70, 0.0, 0.0, 5767.70));

            pointInTimeData = getPointInTimeData(loanId, "07 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 800.22, 100.0, 0.0, 5900.22));

            pointInTimeData = getPointInTimeData(loanId, "09 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4759.59, 551.47, 100.0, 0.0, 5411.06));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023") //
            );
        });

        runAt("01 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 March 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 767.70, 0.0, 0.0, 5767.70));

            pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4759.59, 556.02, 100.0, 0.0, 5415.61));

            pointInTimeData = getPointInTimeData(loanId, "01 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(4259.59, 642.46, 100.0, 0.0, 5002.05));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023") //
            );
        });

        runAt("05 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay full loan
            addRepaymentForLoan(loanId, 5032.52, "05 March 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 767.70, 0.0, 0.0, 5767.70));

            pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4759.59, 556.02, 100.0, 0.0, 5415.61));

            pointInTimeData = getPointInTimeData(loanId, "01 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(4259.59, 642.46, 100.0, 0.0, 5002.05));

            pointInTimeData = getPointInTimeData(loanId, "05 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(0.0, 0.0, 0.0, 0.0, 0.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023"), //
                    transaction(5032.52, "Repayment", "05 March 2023"), //
                    transaction(1110.08, "Accrual", "05 March 2023") //
            );
        });
    }

    @Test
    public void test_LoanPointInTimeDataWorks_ForAllOutstandingCalculation_WhenLoanIsProgressive_AndInterestIsEnabled() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create Loan Product
            PostLoanProductsRequest product = create4IProgressive() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestRatePerPeriod(10.0)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .currencyCode("USD").multiDisburseLoan(null);//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).interestRatePerPeriod(BigDecimal.valueOf(10.0))//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY);//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            Long chargeId = createCharge(100.0).getResourceId();
            addLoanCharge(loanId, chargeId, "02 February 2023", 100.0);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023") //
            );
        });

        runAt("01 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 250.0, "01 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4750.0, 83.56, 0.0, 0.0, 4833.56));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023") //
            );
        });

        runAt("09 February 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "09 February 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4750.0, 83.56, 0.0, 0.0, 4833.56));

            pointInTimeData = getPointInTimeData(loanId, "07 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4750.0, 86.10, 100.0, 0.0, 4936.10));

            pointInTimeData = getPointInTimeData(loanId, "09 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4250.0, 86.93, 100.0, 0.0, 4436.93));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023") //
            );
        });

        runAt("01 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 March 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4750.0, 83.56, 0.0, 0.0, 4833.56));

            pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4250.0, 87.20, 100.0, 0.0, 4437.20));

            pointInTimeData = getPointInTimeData(loanId, "01 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(3750.0, 92.36, 100.0, 0.0, 3942.36));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023") //
            );
        });

        runAt("05 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay full loan
            addRepaymentForLoan(loanId, 3942.36, "05 March 2023");

            // check point in time data
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "01 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4750.0, 83.56, 0.0, 0.0, 4833.56));

            pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(4250.0, 87.20, 100.0, 0.0, 4437.20));

            pointInTimeData = getPointInTimeData(loanId, "01 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(3750.0, 92.36, 100.0, 0.0, 3942.36));

            pointInTimeData = getPointInTimeData(loanId, "05 March 2023");
            verifyOutstanding(pointInTimeData, outstanding(0.0, 0.0, 0.0, 0.0, 0.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(250.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "09 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023"), //
                    transaction(3942.36, "Repayment", "05 March 2023"), //
                    transaction(182.31, "Accrual", "05 March 2023") //
            );
        });
    }

    @Test
    public void test_LoansPointInTimeDataWorks_ForPrincipalOutstandingCalculation() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        AtomicReference<Long> aLoanId2 = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create charges
            double charge1Amount = 1.0;
            double charge2Amount = 1.5;
            Long charge1Id = createDisbursementPercentageCharge(charge1Amount);
            Long charge2Id = createDisbursementPercentageCharge(charge2Amount);

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null)//
                    .charges(List.of(new LoanProductChargeData().id(charge1Id), new LoanProductChargeData().id(charge2Id)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .charges(List.of(//
                            new PostLoansRequestChargeData().chargeId(charge1Id).amount(BigDecimal.valueOf(charge1Amount)), //
                            new PostLoansRequestChargeData().chargeId(charge2Id).amount(BigDecimal.valueOf(charge2Amount))//
            ));//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);
            PostLoansResponse postLoansResponse2 = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            PostLoansLoanIdResponse approvedLoanResult2 = loanTransactionHelper.approveLoan(postLoansResponse2.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            aLoanId2.getAndSet(approvedLoanResult2.getLoanId());
            Long loanId = aLoanId.get();
            Long loanId2 = aLoanId2.get();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");
            disburseLoan(loanId2, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );
            verifyTransactions(loanId2, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );
        });

        runAt("01 February 2023", () -> {
            Long loanId = aLoanId.get();
            Long loanId2 = aLoanId2.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 February 2023");
            addRepaymentForLoan(loanId2, 500.0, "01 February 2023");

            // check point in time data
            List<LoanPointInTimeData> pointInTimeData = getPointInTimeData(List.of(loanId, loanId2), "01 February 2023");
            verifyOutstanding(pointInTimeData.get(0), outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));
            verifyOutstanding(pointInTimeData.get(1), outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023") //
            );
            verifyTransactions(loanId2, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023") //
            );
        });

        runAt("01 March 2023", () -> {
            Long loanId = aLoanId.get();
            Long loanId2 = aLoanId2.get();

            // repay 500
            addRepaymentForLoan(loanId, 500.0, "01 March 2023");
            addRepaymentForLoan(loanId2, 500.0, "01 March 2023");

            // check point in time data
            List<LoanPointInTimeData> pointInTimeData = getPointInTimeData(List.of(loanId, loanId2), "01 February 2023");
            verifyOutstanding(pointInTimeData.get(0), outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));
            verifyOutstanding(pointInTimeData.get(1), outstanding(4500.0, 0.0, 0.0, 0.0, 4500.0));

            pointInTimeData = getPointInTimeData(List.of(loanId, loanId2), "01 March 2023");
            verifyOutstanding(pointInTimeData.get(0), outstanding(4000.0, 0.0, 0.0, 0.0, 4000.0));
            verifyOutstanding(pointInTimeData.get(1), outstanding(4000.0, 0.0, 0.0, 0.0, 4000.0));

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023") //
            );
            verifyTransactions(loanId2, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(500.0, "Repayment", "01 February 2023"), //
                    transaction(500.0, "Repayment", "01 March 2023") //
            );
        });
    }

    @Test
    public void test_LoanPointInTimeDataWorks_ForArrearsDataCalculation() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create charges
            double charge1Amount = 1.0;
            double charge2Amount = 1.5;
            Long charge1Id = createDisbursementPercentageCharge(charge1Amount);
            Long charge2Id = createDisbursementPercentageCharge(charge2Amount);

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null)//
                    .charges(List.of(new LoanProductChargeData().id(charge1Id), new LoanProductChargeData().id(charge2Id)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .charges(List.of(//
                            new PostLoansRequestChargeData().chargeId(charge1Id).amount(BigDecimal.valueOf(charge1Amount)), //
                            new PostLoansRequestChargeData().chargeId(charge2Id).amount(BigDecimal.valueOf(charge2Amount))//
            ));//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );
        });

        runAt("05 February 2023", () -> {
            Long loanId = aLoanId.get();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeData, outstanding(5000.0, 0.0, 0.0, 0.0, 5000.0));
            verifyArrears(pointInTimeData, true, "2023-02-01");

            // repay 500
            addRepaymentForLoan(loanId, 2500.0, "01 February 2023");

            LoanPointInTimeData pointInTimeDataAfterRepay = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeDataAfterRepay, outstanding(2500.0, 0.0, 0.0, 0.0, 2500.0));
            verifyArrears(pointInTimeDataAfterRepay, false, null);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(2500.0, "Repayment", "01 February 2023") //
            );
        });
    }

    @Test
    public void test_LoanPointInTimeDataWorks_ForArrearsDataCalculation_ForFutureDate_WithInterest() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();

        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            // Create charges
            double charge1Amount = 1.0;
            double charge2Amount = 1.5;
            Long charge1Id = createDisbursementPercentageCharge(charge1Amount);
            Long charge2Id = createDisbursementPercentageCharge(charge2Amount);

            // Create Loan Product
            double interestRatePerPeriod = 10.0;
            PostLoanProductsRequest product = createOnePeriod30DaysPeriodicAccrualProduct(interestRatePerPeriod) //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(repaymentEvery) //
                    .installmentAmountInMultiplesOf(null) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()) //
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyInterval(1)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .allowPartialPeriodInterestCalculation(false)//
                    .disallowExpectedDisbursements(false)//
                    .allowApprovedDisbursedAmountsOverApplied(false)//
                    .overAppliedNumber(null)//
                    .overAppliedCalculationType(null)//
                    .multiDisburseLoan(null)//
                    .charges(List.of(new LoanProductChargeData().id(charge1Id), new LoanProductChargeData().id(charge2Id)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 5000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(repaymentEvery)//
                    .interestRatePerPeriod(BigDecimal.valueOf(interestRatePerPeriod)).loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(InterestType.DECLINING_BALANCE)//
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)//
                    .charges(List.of(//
                            new PostLoansRequestChargeData().chargeId(charge1Id).amount(BigDecimal.valueOf(charge1Amount)), //
                            new PostLoansRequestChargeData().chargeId(charge2Id).amount(BigDecimal.valueOf(charge2Amount))//
            ));//

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(5000.0), "01 January 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023") //
            );
        });

        runAt("05 March 2023", () -> {
            Long loanId = aLoanId.get();

            // repay
            addRepaymentForLoan(loanId, 5897.89, "05 March 2023");

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(5897.89, "Repayment", "05 March 2023"), //
                    transaction(897.89, "Accrual", "05 March 2023") //
            );
        });

        runAt("05 June 2023", () -> {
            Long loanId = aLoanId.get();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "05 June 2023");

            verifyOutstanding(pointInTimeData, outstanding(0.0, 0.0, 0.0, 0.0, 0.0));
            verifyArrears(pointInTimeData, false, null);
            assertThat(pointInTimeData.getArrears().getPrincipalOverdue()).isZero();
            assertThat(pointInTimeData.getArrears().getFeeOverdue()).isZero();
            assertThat(pointInTimeData.getArrears().getInterestOverdue()).isZero();
            assertThat(pointInTimeData.getArrears().getPenaltyOverdue()).isZero();
            assertThat(pointInTimeData.getArrears().getTotalOverdue()).isZero();
        });
    }

    @Test
    public void test_LoanPointInTimeData_InstallmentFeeAllocation() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 100.0;

        runAt("01 October 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 6;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.DECLINING_BALANCE)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)
                    .isInterestRecalculationEnabled(true).recalculationRestFrequencyInterval(1)
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT).allowPartialPeriodInterestCalculation(false)
                    .disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null)
                    .overAppliedCalculationType(null).multiDisburseLoan(null)
                    .charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 6000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2025", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.DECLINING_BALANCE).interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(installmentFeeChargeId)
                            .amount(BigDecimal.valueOf(installmentFeeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2025"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2025");

            verifyTransactions(loanId, transaction(6000.0, "Disbursement", "01 October 2025"));
        });

        runAt("01 November 2025", () -> {
            Long loanId = aLoanId.get();

            addRepaymentForLoan(loanId, 1100.0, "01 November 2025");

            verifyTransactions(loanId, transaction(6000.0, "Disbursement", "01 October 2025"),
                    transaction(1100.0, "Repayment", "01 November 2025"));
        });

        runAt("01 December 2025", () -> {
            Long loanId = aLoanId.get();

            addRepaymentForLoan(loanId, 1100.0, "01 December 2025");

            verifyTransactions(loanId, transaction(6000.0, "Disbursement", "01 October 2025"),
                    transaction(1100.0, "Repayment", "01 November 2025"), transaction(1100.0, "Repayment", "01 December 2025"));
        });

        runAt("01 January 2026", () -> {
            Long loanId = aLoanId.get();

            addRepaymentForLoan(loanId, 1100.0, "01 January 2026");

            verifyTransactions(loanId, transaction(6000.0, "Disbursement", "01 October 2025"),
                    transaction(1100.0, "Repayment", "01 November 2025"), transaction(1100.0, "Repayment", "01 December 2025"),
                    transaction(1100.0, "Repayment", "01 January 2026"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal regularApiFeeChargesPaid = loanDetails.getSummary().getFeeChargesPaid();
            BigDecimal regularApiFeeChargesCharged = loanDetails.getSummary().getFeeChargesCharged();

            assertThat(regularApiFeeChargesCharged).isEqualByComparingTo(BigDecimal.valueOf(600.0));
            assertThat(regularApiFeeChargesPaid).isEqualByComparingTo(BigDecimal.valueOf(300.0));
        });

        runAt("08 January 2026", () -> {
            Long loanId = aLoanId.get();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "08 January 2026");

            assertThat(pointInTimeData.getFee().getFeeChargesCharged())
                    .as("Point-in-time feeChargesCharged should only include fees for installments due by the requested date")
                    .isEqualByComparingTo(BigDecimal.valueOf(300.0));

            assertThat(pointInTimeData.getFee().getFeeChargesPaid())
                    .as("Point-in-time feeChargesPaid should reflect paid installment fees up to the requested date")
                    .isEqualByComparingTo(BigDecimal.valueOf(300.0));

            assertThat(pointInTimeData.getFee().getFeeChargesOutstanding())
                    .as("Point-in-time feeChargesOutstanding should be 0 since all due fees are paid")
                    .isEqualByComparingTo(BigDecimal.ZERO);

            verifyOutstanding(pointInTimeData, outstanding(3000.0, 0.0, 0.0, 0.0, 3000.0));
        });
    }

    @Test
    public void test_LoanPointInTimeData_ClosedLoanWithInstallmentFees() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 25.0;

        runAt("01 October 2023", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 2;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.FLAT)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .isInterestRecalculationEnabled(false).disallowExpectedDisbursements(false)
                    .allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null).overAppliedCalculationType(null)
                    .multiDisburseLoan(null).charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 2000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2023", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.FLAT).interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(installmentFeeChargeId)
                            .amount(BigDecimal.valueOf(installmentFeeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2023"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2023");

            verifyTransactions(loanId, transaction(2000.0, "Disbursement", "01 October 2023"));
        });

        runAt("01 November 2023", () -> {
            Long loanId = aLoanId.get();

            // First repayment: 1000 principal + 25 fee = 1025
            addRepaymentForLoan(loanId, 1025.0, "01 November 2023");

            verifyTransactions(loanId, transaction(2000.0, "Disbursement", "01 October 2023"),
                    transaction(1025.0, "Repayment", "01 November 2023"));
        });

        runAt("01 December 2023", () -> {
            Long loanId = aLoanId.get();

            // Second repayment: 1000 principal + 25 fee = 1025 (loan should close)
            addRepaymentForLoan(loanId, 1025.0, "01 December 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            // Verify loan is closed
            assertThat(loanDetails.getStatus().getCode()).isEqualTo("loanStatusType.closed.obligations.met");

            // Verify fee charges
            BigDecimal feeChargesCharged = loanDetails.getSummary().getFeeChargesCharged();
            BigDecimal feeChargesPaid = loanDetails.getSummary().getFeeChargesPaid();

            assertThat(feeChargesCharged).as("Total fee charges charged should be 50 (2 installments * 25)")
                    .isEqualByComparingTo(BigDecimal.valueOf(50.0));
            assertThat(feeChargesPaid).as("Total fee charges paid should be 50").isEqualByComparingTo(BigDecimal.valueOf(50.0));

            // With periodic accrual accounting, an accrual transaction is created for the installment fees
            verifyTransactions(loanId, transaction(2000.0, "Disbursement", "01 October 2023"),
                    transaction(1025.0, "Repayment", "01 November 2023"), transaction(50.0, "Accrual", "01 December 2023"),
                    transaction(1025.0, "Repayment", "01 December 2023"));
        });

        runAt("15 December 2023", () -> {
            Long loanId = aLoanId.get();

            // Query point-in-time data for a date AFTER the loan was closed
            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "15 December 2023");

            // For a closed loan, all installment fees should be included (50 total)
            assertThat(pointInTimeData.getFee().getFeeChargesCharged())
                    .as("Point-in-time feeChargesCharged for closed loan should be 50.0 (2 installments * 25)")
                    .isEqualByComparingTo(BigDecimal.valueOf(50.0));

            assertThat(pointInTimeData.getFee().getFeeChargesPaid()).as("Point-in-time feeChargesPaid for closed loan should be 50.0")
                    .isEqualByComparingTo(BigDecimal.valueOf(50.0));

            assertThat(pointInTimeData.getFee().getFeeChargesOutstanding())
                    .as("Point-in-time feeChargesOutstanding for closed loan should be 0").isEqualByComparingTo(BigDecimal.ZERO);

            // Total outstanding should be 0 for a closed loan
            verifyOutstanding(pointInTimeData, outstanding(0.0, 0.0, 0.0, 0.0, 0.0));
        });
    }

    @Test
    public void test_LoanPointInTimeData_TotalExpectedAmountsConsistency() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 100.0;

        runAt("01 October 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 6;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.DECLINING_BALANCE)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)
                    .interestRecalculationCompoundingMethod(InterestRecalculationCompoundingMethod.NONE)
                    .isInterestRecalculationEnabled(true).recalculationRestFrequencyInterval(1)
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY)
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT).allowPartialPeriodInterestCalculation(false)
                    .disallowExpectedDisbursements(false).allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null)
                    .overAppliedCalculationType(null).multiDisburseLoan(null)
                    .charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 6000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2025", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.DECLINING_BALANCE).interestCalculationPeriodType(InterestCalculationPeriodType.DAILY)
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(installmentFeeChargeId)
                            .amount(BigDecimal.valueOf(installmentFeeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2025"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2025");
        });

        runAt("01 November 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 November 2025");
        });

        runAt("01 December 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 December 2025");
        });

        runAt("01 January 2026", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 January 2026");

            // Verify regular API values (full schedule)
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal regularApiFeeChargesCharged = loanDetails.getSummary().getFeeChargesCharged();
            assertThat(regularApiFeeChargesCharged).as("Regular API should show full fees (600)")
                    .isEqualByComparingTo(BigDecimal.valueOf(600.0));
        });

        runAt("08 January 2026", () -> {
            Long loanId = aLoanId.get();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "08 January 2026");

            // Fee charges should only include fees for installments due by the requested date (3 installments * 100 =
            // 300)
            BigDecimal expectedFeeCharged = BigDecimal.valueOf(300.0);
            assertThat(pointInTimeData.getFee().getFeeChargesCharged())
                    .as("Point-in-time feeChargesCharged should be 300 (3 installments * 100)").isEqualByComparingTo(expectedFeeCharged);

            // Verify that totalExpectedRepayment is consistent with adjusted fees
            // totalExpectedRepayment = principal + interest + fees + penalties
            // For this loan: 6000 (principal) + 0 (interest) + 300 (adjusted fees) + 0 (penalties) = 6300
            BigDecimal expectedTotalExpectedRepayment = BigDecimal.valueOf(6300.0);
            assertThat(pointInTimeData.getTotal().getTotalExpectedRepayment())
                    .as("Point-in-time totalExpectedRepayment should be consistent with adjusted feeChargesCharged (6000 + 300 = 6300)")
                    .isEqualByComparingTo(expectedTotalExpectedRepayment);

            // Verify that totalExpectedCostOfLoan is consistent with adjusted fees
            // totalExpectedCostOfLoan = interest + fees + penalties
            // For this loan: 0 (interest) + 300 (adjusted fees) + 0 (penalties) = 300
            BigDecimal expectedTotalExpectedCostOfLoan = BigDecimal.valueOf(300.0);
            assertThat(pointInTimeData.getTotal().getTotalExpectedCostOfLoan())
                    .as("Point-in-time totalExpectedCostOfLoan should be consistent with adjusted feeChargesCharged (0 + 300 = 300)")
                    .isEqualByComparingTo(expectedTotalExpectedCostOfLoan);
        });
    }

    @Test
    public void test_LoanPointInTimeData_InterestFieldsRemainCoherent() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 50.0;
        double interestRatePerPeriod = 12.0;

        runAt("01 October 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 4;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysPeriodicAccrualProduct(interestRatePerPeriod)
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.FLAT)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .isInterestRecalculationEnabled(false).disallowExpectedDisbursements(false)
                    .allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null).overAppliedCalculationType(null)
                    .multiDisburseLoan(null).charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 4000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2025", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.FLAT).interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .interestRatePerPeriod(BigDecimal.valueOf(interestRatePerPeriod)).charges(List.of(new PostLoansRequestChargeData()
                            .chargeId(installmentFeeChargeId).amount(BigDecimal.valueOf(installmentFeeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2025"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2025");
        });

        runAt("01 November 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1170.0, "01 November 2025");
        });

        runAt("01 December 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1170.0, "01 December 2025");
        });

        runAt("08 December 2025", () -> {
            Long loanId = aLoanId.get();

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal regularApiInterestCharged = loanDetails.getSummary().getInterestCharged();
            BigDecimal regularApiInterestPaid = loanDetails.getSummary().getInterestPaid();
            BigDecimal regularApiInterestOutstanding = loanDetails.getSummary().getInterestOutstanding();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "08 December 2025");

            assertThat(pointInTimeData.getInterest().getInterestCharged())
                    .as("Point-in-time interestCharged should remain unchanged from regular API")
                    .isEqualByComparingTo(regularApiInterestCharged);

            assertThat(pointInTimeData.getInterest().getInterestPaid())
                    .as("Point-in-time interestPaid should remain unchanged from regular API").isEqualByComparingTo(regularApiInterestPaid);

            assertThat(pointInTimeData.getInterest().getInterestOutstanding())
                    .as("Point-in-time interestOutstanding should remain unchanged from regular API")
                    .isEqualByComparingTo(regularApiInterestOutstanding);

            assertThat(pointInTimeData.getFee().getFeeChargesCharged())
                    .as("Point-in-time feeChargesCharged should only include fees for 2 installments due by Dec 8")
                    .isEqualByComparingTo(BigDecimal.valueOf(100.0));
        });
    }

    @Test
    public void test_LoanPointInTimeData_PenaltyFieldsRemainCoherent() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 50.0;
        double installmentPenaltyAmount = 25.0;

        runAt("01 October 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 4;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);
            Long installmentPenaltyChargeId = createInstallmentPenaltyCharge(installmentPenaltyAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.FLAT)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .isInterestRecalculationEnabled(false).disallowExpectedDisbursements(false)
                    .allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null).overAppliedCalculationType(null)
                    .multiDisburseLoan(null).charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId),
                            new LoanProductChargeData().id(installmentPenaltyChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 4000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2025", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.FLAT).interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .charges(List.of(
                            new PostLoansRequestChargeData().chargeId(installmentFeeChargeId)
                                    .amount(BigDecimal.valueOf(installmentFeeAmount)),
                            new PostLoansRequestChargeData().chargeId(installmentPenaltyChargeId)
                                    .amount(BigDecimal.valueOf(installmentPenaltyAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2025"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2025");
        });

        runAt("01 November 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1075.0, "01 November 2025");
        });

        runAt("01 December 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1075.0, "01 December 2025");
        });

        runAt("08 December 2025", () -> {
            Long loanId = aLoanId.get();

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            BigDecimal regularApiPenaltyCharged = loanDetails.getSummary().getPenaltyChargesCharged();
            BigDecimal regularApiPenaltyPaid = loanDetails.getSummary().getPenaltyChargesPaid();
            BigDecimal regularApiPenaltyOutstanding = loanDetails.getSummary().getPenaltyChargesOutstanding();

            LoanPointInTimeData pointInTimeData = getPointInTimeData(loanId, "08 December 2025");

            assertThat(pointInTimeData.getPenalty().getPenaltyChargesCharged())
                    .as("Point-in-time penaltyChargesCharged should remain unchanged from regular API")
                    .isEqualByComparingTo(regularApiPenaltyCharged);

            assertThat(pointInTimeData.getPenalty().getPenaltyChargesPaid())
                    .as("Point-in-time penaltyChargesPaid should remain unchanged from regular API")
                    .isEqualByComparingTo(regularApiPenaltyPaid);

            assertThat(pointInTimeData.getPenalty().getPenaltyChargesOutstanding())
                    .as("Point-in-time penaltyChargesOutstanding should remain unchanged from regular API")
                    .isEqualByComparingTo(regularApiPenaltyOutstanding);

            assertThat(pointInTimeData.getFee().getFeeChargesCharged())
                    .as("Point-in-time feeChargesCharged should only include fees for 2 installments due by Dec 8")
                    .isEqualByComparingTo(BigDecimal.valueOf(100.0));
        });
    }

    @Test
    public void test_LoanPointInTimeData_MatchesRegularApiWhenNoFiltering() {
        AtomicReference<Long> aLoanId = new AtomicReference<>();
        double installmentFeeAmount = 100.0;

        runAt("01 October 2025", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            int numberOfRepayments = 3;
            int repaymentEvery = 1;

            Long installmentFeeChargeId = createInstallmentFeeCharge(installmentFeeAmount);

            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                    .numberOfRepayments(numberOfRepayments).repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS.longValue()).interestType(InterestType.FLAT)
                    .interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .isInterestRecalculationEnabled(false).disallowExpectedDisbursements(false)
                    .allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null).overAppliedCalculationType(null)
                    .multiDisburseLoan(null).charges(List.of(new LoanProductChargeData().id(installmentFeeChargeId)));

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            double amount = 3000.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 October 2025", amount, numberOfRepayments)
                    .repaymentEvery(repaymentEvery).loanTermFrequency(numberOfRepayments)
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                    .interestType(InterestType.FLAT).interestCalculationPeriodType(InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(installmentFeeChargeId)
                            .amount(BigDecimal.valueOf(installmentFeeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 October 2025"));

            aLoanId.getAndSet(approvedLoanResult.getLoanId());
            Long loanId = aLoanId.get();

            disburseLoan(loanId, BigDecimal.valueOf(amount), "01 October 2025");
        });

        runAt("01 November 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 November 2025");
        });

        runAt("01 December 2025", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 December 2025");
        });

        runAt("01 January 2026", () -> {
            Long loanId = aLoanId.get();
            addRepaymentForLoan(loanId, 1100.0, "01 January 2026");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            assertThat(loanDetails.getStatus().getCode()).isEqualTo("loanStatusType.closed.obligations.met");
        });

        runAt("15 January 2026", () -> {
            Long loanId = aLoanId.get();

            GetLoansLoanIdResponse regularApi = loanTransactionHelper.getLoanDetails(loanId);
            LoanPointInTimeData pointInTimeApi = getPointInTimeData(loanId, "15 January 2026");

            assertThat(pointInTimeApi.getFee().getFeeChargesCharged())
                    .as("Closed loan: point-in-time feeChargesCharged should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getFeeChargesCharged());

            assertThat(pointInTimeApi.getFee().getFeeChargesPaid()).as("Closed loan: point-in-time feeChargesPaid should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getFeeChargesPaid());

            assertThat(pointInTimeApi.getFee().getFeeChargesOutstanding())
                    .as("Closed loan: point-in-time feeChargesOutstanding should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getFeeChargesOutstanding());

            assertThat(pointInTimeApi.getTotal().getTotalExpectedRepayment())
                    .as("Closed loan: point-in-time totalExpectedRepayment should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getTotalExpectedRepayment());

            assertThat(pointInTimeApi.getTotal().getTotalExpectedCostOfLoan())
                    .as("Closed loan: point-in-time totalExpectedCostOfLoan should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getTotalExpectedCostOfLoan());

            assertThat(pointInTimeApi.getPrincipal().getPrincipalOutstanding())
                    .as("Closed loan: point-in-time principalOutstanding should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getPrincipalOutstanding());

            assertThat(pointInTimeApi.getTotal().getTotalOutstanding())
                    .as("Closed loan: point-in-time totalOutstanding should match regular API")
                    .isEqualByComparingTo(regularApi.getSummary().getTotalOutstanding());
        });
    }

    private Long createInstallmentFeeCharge(double amount) {
        Integer chargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(amount), false));
        assertNotNull(chargeId);
        return chargeId.longValue();
    }

    private Long createInstallmentPenaltyCharge(double amount) {
        Integer chargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanInstallmentJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(amount), true));
        assertNotNull(chargeId);
        return chargeId.longValue();
    }
}
