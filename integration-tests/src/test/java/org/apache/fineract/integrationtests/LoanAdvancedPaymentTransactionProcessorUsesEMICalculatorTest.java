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

import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.junit.jupiter.api.Test;

public class LoanAdvancedPaymentTransactionProcessorUsesEMICalculatorTest extends BaseLoanIntegrationTest {

    @Test
    public void testRepaymentScheduleAfterBackdatedRepaymentForAdvancedPaymentTransactionProcessorWithEMICalculator() {
        runAt("01 January 2023", () -> {
            int amortizationType = AmortizationType.EQUAL_INSTALLMENTS;
            int interestType = InterestType.DECLINING_BALANCE;

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .interestType(interestType)//
                    .maxInterestRatePerPeriod(12.0)//
                    .minInterestRatePerPeriod(0.0)//
                    .interestRatePerPeriod(9.99)//
                    .amortizationType(amortizationType).interestRateFrequencyType(InterestRateFrequencyType.YEARS)
                    .interestCalculationPeriodType(0).repaymentEvery(15).repaymentFrequencyType(0L).daysInMonthType(30).daysInYearType(360)
                    .allowPartialPeriodInterestCalcualtion(false);

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1000.0;
            int numberOfRepayments = 4;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(15)//
                    .loanTermFrequency(60)//
                    .repaymentFrequencyType(RepaymentFrequencyType.DAYS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.DAYS)//
                    .interestType(interestType)//
                    .amortizationType(amortizationType)//
                    .interestCalculationPeriodType(0)//
                    .interestRatePerPeriod(BigDecimal.valueOf(9.99)).transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // disburse

            disburseLoan(loanId, BigDecimal.valueOf(100.00), "01 January 2023");

            // verify repayment periods installment amounts

            GetLoansLoanIdResponse loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            assertNotNull(loanResponse.getRepaymentSchedule());
            assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());

            //
            GetLoansLoanIdRepaymentPeriod period_1 = loanResponse.getRepaymentSchedule().getPeriods().get(1);
            assertEquals(24.58, period_1.getPrincipalOutstanding());
            assertEquals(0.42, period_1.getInterestOutstanding());
            assertEquals(25.0, period_1.getTotalInstallmentAmountForPeriod());

            GetLoansLoanIdRepaymentPeriod period_2 = loanResponse.getRepaymentSchedule().getPeriods().get(2);
            assertEquals(24.69, period_2.getPrincipalOutstanding());
            assertEquals(0.31, period_2.getInterestOutstanding());
            assertEquals(25.0, period_2.getTotalInstallmentAmountForPeriod());

            GetLoansLoanIdRepaymentPeriod period_3 = loanResponse.getRepaymentSchedule().getPeriods().get(3);
            assertEquals(24.79, period_3.getPrincipalOutstanding());
            assertEquals(0.21, period_3.getInterestOutstanding());
            assertEquals(25.0, period_3.getTotalInstallmentAmountForPeriod());

            GetLoansLoanIdRepaymentPeriod period_4 = loanResponse.getRepaymentSchedule().getPeriods().get(4);
            assertEquals(25.94, period_4.getPrincipalOutstanding());
            assertEquals(0.11, period_4.getInterestOutstanding());
            assertEquals(26.05, period_4.getTotalInstallmentAmountForPeriod());

            updateBusinessDate("16 January 2023");

            // make repayment

            addRepaymentForLoan(loanId, 35.0, "16 January 2023");

            // verify repayment periods installment amounts

            loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            assertNotNull(loanResponse.getRepaymentSchedule());
            assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());

            //
            period_1 = loanResponse.getRepaymentSchedule().getPeriods().get(1);
            assertEquals(0.0, period_1.getPrincipalOutstanding());
            assertEquals(0.0, period_1.getInterestOutstanding());
            assertEquals(25.0, period_1.getTotalInstallmentAmountForPeriod());

            period_2 = loanResponse.getRepaymentSchedule().getPeriods().get(2);
            assertEquals(14.69, period_2.getPrincipalOutstanding());
            assertEquals(0.31, period_2.getInterestOutstanding());
            assertEquals(25.0, period_2.getTotalInstallmentAmountForPeriod());

            period_3 = loanResponse.getRepaymentSchedule().getPeriods().get(3);
            assertEquals(24.79, period_3.getPrincipalOutstanding());
            assertEquals(0.21, period_3.getInterestOutstanding());
            assertEquals(25.0, period_3.getTotalInstallmentAmountForPeriod());

            period_4 = loanResponse.getRepaymentSchedule().getPeriods().get(4);
            assertEquals(25.94, period_4.getPrincipalOutstanding());
            assertEquals(0.11, period_4.getInterestOutstanding());
            assertEquals(26.05, period_4.getTotalInstallmentAmountForPeriod());

            // make backdated repayment to trigger transaction reprocess

            addRepaymentForLoan(loanId, 30.0, "12 January 2023");

            // verify repayment periods installment amounts

            loanResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId.intValue());
            assertNotNull(loanResponse.getRepaymentSchedule());
            assertNotNull(loanResponse.getRepaymentSchedule().getPeriods());

            //
            period_1 = loanResponse.getRepaymentSchedule().getPeriods().get(1);
            assertEquals(0.0, period_1.getPrincipalOutstanding());
            assertEquals(0.0, period_1.getInterestOutstanding());
            assertEquals(25.0, period_1.getTotalInstallmentAmountForPeriod());

            period_2 = loanResponse.getRepaymentSchedule().getPeriods().get(2);
            assertEquals(0.0, period_2.getPrincipalOutstanding());
            assertEquals(0.0, period_2.getInterestOutstanding());
            assertEquals(25.0, period_2.getTotalInstallmentAmountForPeriod());

            period_3 = loanResponse.getRepaymentSchedule().getPeriods().get(3);
            assertEquals(9.79, period_3.getPrincipalOutstanding());
            assertEquals(0.21, period_3.getInterestOutstanding());
            assertEquals(25.0, period_3.getTotalInstallmentAmountForPeriod());

            period_4 = loanResponse.getRepaymentSchedule().getPeriods().get(4);
            assertEquals(25.94, period_4.getPrincipalOutstanding());
            assertEquals(0.11, period_4.getInterestOutstanding());
            assertEquals(26.05, period_4.getTotalInstallmentAmountForPeriod());

        });

    }
}
