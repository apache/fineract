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
package org.apache.fineract.integrationtests.loan;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.junit.jupiter.api.Test;

public class LoanApiIntegrationTest extends FeignLoanTestBase {

    private static final String LOAN_DATE = "01 January 2023";

    private PostLoanProductsRequest commonProduct(int numberOfRepayments, int repaymentEvery) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(numberOfRepayments)
                .repaymentEvery(repaymentEvery).installmentAmountInMultiplesOf(null)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE).interestRatePerPeriod(10.0)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE)
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)
                .isInterestRecalculationEnabled(true).recalculationRestFrequencyInterval(1)
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.DAILY)
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)
                .allowPartialPeriodInterestCalculation(false).disallowExpectedDisbursements(false)
                .allowApprovedDisbursedAmountsOverApplied(false).overAppliedNumber(null).overAppliedCalculationType(null)
                .multiDisburseLoan(null);
    }

    private PostLoansRequest commonApplication(Long clientId, Long loanProductId, int numberOfRepayments, int repaymentEvery,
            double amount) {
        return applyLoanRequest(clientId, loanProductId, LOAN_DATE, amount, numberOfRepayments).repaymentEvery(repaymentEvery)
                .interestRatePerPeriod(BigDecimal.valueOf(10.0)).loanTermFrequency(numberOfRepayments)
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS).interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);
    }

    @Test
    public void test_retrieveLoansByClientId_Works() {
        AtomicLong createdLoanId = new AtomicLong();
        AtomicLong createdLoanId2 = new AtomicLong();
        Long clientId = createClient();
        Long clientId2 = createClient();

        int numberOfRepayments = 3;
        int repaymentEvery = 1;
        double amount = 5000.0;

        // Create Client
        runAt(LOAN_DATE, () -> {
            Long loanProductId = createLoanProduct(commonProduct(numberOfRepayments, repaymentEvery));
            Long loanProductId2 = createLoanProduct(commonProduct(numberOfRepayments, repaymentEvery));

            // Create Loan Products
            Long loanId = approveLoan(applyForLoan(commonApplication(clientId, loanProductId, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();
            Long loanId2 = approveLoan(
                    applyForLoan(commonApplication(clientId2, loanProductId2, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();

            createdLoanId.getAndSet(loanId);
            createdLoanId2.getAndSet(loanId2);

            disburseLoan(loanId, BigDecimal.valueOf(amount), LOAN_DATE);
            // disburse Loan
            disburseLoan(loanId2, BigDecimal.valueOf(amount), LOAN_DATE);
        });

        runAt("01 February 2023", () -> {
            long loanId = createdLoanId.get();
            GetLoansResponse loansLoanIdResponse = retrieveAllLoans(null, null, clientId);
            assertThat(loansLoanIdResponse.getPageItems()).isNotNull();
            assertThat(loansLoanIdResponse.getPageItems().size()).isEqualTo(1);
            Long loanIdFromResponse = loansLoanIdResponse.getPageItems().iterator().next().getId();
            assertThat(loanIdFromResponse).isEqualTo(loanId);
        });
    }

    @Test
    public void test_retrieveLoansWithSummary_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        int numberOfRepayments = 3;
        int repaymentEvery = 1;
        // Create Client
        double amount = 5000.0;

        runAt(LOAN_DATE, () -> {
            Long clientId = createClient();
            // Create Loan Product
            Long loanProductId = createLoanProduct(commonProduct(numberOfRepayments, repaymentEvery));

            // Apply and Approve Loan
            Long loanId = approveLoan(applyForLoan(commonApplication(clientId, loanProductId, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();
            createdLoanId.getAndSet(loanId);

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(amount), LOAN_DATE);
        });

        runAt("01 February 2023", () -> {
            long loanId = createdLoanId.get();
            GetLoansLoanIdResponse loanResponse = getLoanDetails(loanId);
            GetLoansResponse loansLoanIdResponse = retrieveAllLoans(loanResponse.getAccountNo(), "summary", null);
            BigDecimal totalUnpaidPayableDueInterest = loansLoanIdResponse.getPageItems().iterator().next().getSummary()
                    .getTotalUnpaidPayableDueInterest();
            assertThat(totalUnpaidPayableDueInterest).isEqualByComparingTo(BigDecimal.valueOf(509.59));
        });
    }

    @Test
    public void test_retrieveLoansWithSummaryForMultipleLoans_Works() {
        AtomicLong createdClientId = new AtomicLong();

        int numberOfRepayments = 3;
        int repaymentEvery = 1;
        // Create Client
        double amount = 5000.0;

        runAt(LOAN_DATE, () -> {
            Long clientId = createClient();
            createdClientId.getAndSet(clientId);
            Long loanProductId = createLoanProduct(commonProduct(numberOfRepayments, repaymentEvery));

            // Create Loan Product
            Long loanId = approveLoan(applyForLoan(commonApplication(clientId, loanProductId, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();
            Long loanId2 = approveLoan(applyForLoan(commonApplication(clientId, loanProductId, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();

            disburseLoan(loanId, BigDecimal.valueOf(amount), LOAN_DATE);
            disburseLoan(loanId2, BigDecimal.valueOf(amount), LOAN_DATE);
        });

        runAt("01 February 2023", () -> {
            GetLoansResponse loansLoanIdResponse = retrieveAllLoans(null, "summary", createdClientId.get());
            loansLoanIdResponse.getPageItems().forEach(r -> {
                BigDecimal totalUnpaidPayableDueInterest = r.getSummary().getTotalUnpaidPayableDueInterest();
                assertThat(totalUnpaidPayableDueInterest).isEqualByComparingTo(BigDecimal.valueOf(509.59));
            });
        });
    }

    @Test
    public void test_retrieveLoansWithSummaryWithoutDisbursement_Works() {
        AtomicLong createdLoanId = new AtomicLong();

        int numberOfRepayments = 3;
        int repaymentEvery = 1;
        // Create Client
        double amount = 5000.0;

        runAt(LOAN_DATE, () -> {
            Long clientId = createClient();
            // Create Loan Product
            Long loanProductId = createLoanProduct(commonProduct(numberOfRepayments, repaymentEvery));

            // Apply and Approve Loan
            Long loanId = approveLoan(applyForLoan(commonApplication(clientId, loanProductId, numberOfRepayments, repaymentEvery, amount)),
                    approveLoanRequest(amount, LOAN_DATE)).getLoanId();
            createdLoanId.getAndSet(loanId);
        });

        runAt("01 February 2023", () -> {
            long loanId = createdLoanId.get();
            GetLoansLoanIdResponse loanResponse = getLoanDetails(loanId);
            GetLoansResponse loansLoanIdResponse = retrieveAllLoans(loanResponse.getAccountNo(), "summary", null);
            assertThat(loansLoanIdResponse.getPageItems()).isNotNull();
            assertThat(loansLoanIdResponse.getPageItems().iterator().next().getSummary()).isNull();
        });
    }
}
