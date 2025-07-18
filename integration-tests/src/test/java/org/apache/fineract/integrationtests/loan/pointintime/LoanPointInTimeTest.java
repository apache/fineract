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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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
                    .allowPartialPeriodInterestCalcualtion(false)//
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
                    .allowPartialPeriodInterestCalcualtion(false)//
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
                    .allowPartialPeriodInterestCalcualtion(false)//
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
                    .allowPartialPeriodInterestCalcualtion(false)//
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
    public void test_LoanPointInTimeDataWorks_ForArrealDataCalculation() {
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
                    .allowPartialPeriodInterestCalcualtion(false)//
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
            verifyArreals(pointInTimeData, true, "2023-02-01");

            // repay 500
            addRepaymentForLoan(loanId, 2500.0, "01 February 2023");

            LoanPointInTimeData pointInTimeDataAfterRepay = getPointInTimeData(loanId, "10 February 2023");
            verifyOutstanding(pointInTimeDataAfterRepay, outstanding(2500.0, 0.0, 0.0, 0.0, 2500.0));
            verifyArreals(pointInTimeDataAfterRepay, false, null);

            // verify transactions
            verifyTransactions(loanId, //
                    transaction(5000.0, "Disbursement", "01 January 2023"), //
                    transaction(125.0, "Repayment (at time of disbursement)", "01 January 2023"), //
                    transaction(2500.0, "Repayment", "01 February 2023") //
            );
        });
    }
}
