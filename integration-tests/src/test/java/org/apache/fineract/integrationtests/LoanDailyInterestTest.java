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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.InterestCalculationPeriodType.DAILY;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.ChargeData;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.Test;

public class LoanDailyInterestTest extends BaseLoanIntegrationTest {

    @Test
    public void test_LoanInterestIsCalculatedFor30Days_WhenInterestIsDaily_AndDaysInMonthIs30_AndRepaymentStartsIn5Days_AndInterestIsChargedFromDisbursementDate_AndChargeIsPresent() {
        runAt("01 August 2024", () -> {
            int amortizationType = AmortizationType.EQUAL_INSTALLMENTS;
            int interestType = InterestType.DECLINING_BALANCE;

            double chargeAmount = 28_161.24;
            double amount = 532_770.0;
            int numberOfRepayments = 60;

            Long chargeId = createDisbursementFlatCharge(chargeAmount);

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    interestType, amortizationType)//
                    .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                    .installmentAmountInMultiplesOf(null)//
                    .maxPrincipal(amount).minPrincipal(amount).principal(amount).minNumberOfRepayments(numberOfRepayments)
                    .numberOfRepayments(numberOfRepayments).maxNumberOfRepayments(numberOfRepayments).maxInterestRatePerPeriod(12.0)//
                    .minInterestRatePerPeriod(12.0)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(DAILY)//
                    .allowPartialPeriodInterestCalcualtion(false)//
                    .isInterestRecalculationEnabled(true)//
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    .recalculationRestFrequencyInterval(1) //
                    .recalculationCompoundingFrequencyType(1) //
                    .preClosureInterestCalculationStrategy(1)//
                    .daysInMonthType(DaysInMonthType.DAYS_30)//
                    .daysInYearType(DaysInYearType.DAYS_360)//
                    .rescheduleStrategyMethod(RescheduleStrategyMethod.REDUCE_EMI_AMOUNT)//
                    .interestRecalculationCompoundingMethod(0)//
                    .charges(List.of(new ChargeData().id(chargeId)));//

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 August 2024", amount, numberOfRepayments)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(interestType)//
                    .amortizationType(amortizationType)//
                    .interestCalculationPeriodType(DAILY)//
                    .repaymentsStartingFromDate("05 August 2024")//
                    .interestChargedFromDate("01 August 2024").interestRatePerPeriod(BigDecimal.valueOf(12))//
                    .charges(List.of(new PostLoansRequestChargeData().chargeId(chargeId).amount(BigDecimal.valueOf(chargeAmount))));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 August 2024"));

            Long loanId = approvedLoanResult.getLoanId();

            // Verify Repayment Schedule
            verifyRepaymentSchedulePartially(loanId, //
                    installment(532_770.0, null, "01 August 2024"), //
                    installment(11_140.81, 710.36, 11_851.17, false, "05 August 2024"), //
                    installment(6_634.88, 5_216.29, 11_851.17, false, "05 September 2024"), //
                    installment(6_701.23, 5_149.94, 11_851.17, false, "05 October 2024") //
            );

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(532_770.0), "01 August 2024");

            // Verify Repayment Schedule
            verifyRepaymentSchedulePartially(loanId, //
                    installment(532_770.0, null, "01 August 2024"), //
                    installment(11_140.81, 710.36, 11_851.17, false, "05 August 2024"), //
                    installment(6_634.88, 5_216.29, 11_851.17, false, "05 September 2024"), //
                    installment(6_701.23, 5_149.94, 11_851.17, false, "05 October 2024") //
            );
        });
    }
}
