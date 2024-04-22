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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoanInstallmentMultiplesOfTest extends BaseLoanIntegrationTest {

    private static Stream<Arguments> interestTypes() {
        return Stream.of(Arguments.of(Named.of("DECLINING_BALANCE", InterestType.DECLINING_BALANCE)), //
                Arguments.of(Named.of("FLAT", InterestType.FLAT)));
    }

    @ParameterizedTest
    @MethodSource("interestTypes")
    public void test_LoanRepaymentScheduleIsEquallyDistributed_WhenNoInterest_ButInterestTypeIs(int interestType) {
        runAt("01 January 2023", () -> {
            int amortizationType = AmortizationType.EQUAL_INSTALLMENTS;

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    interestType, amortizationType);

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, 4)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(4)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(interestType)//
                    .amortizationType(amortizationType);

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(312.0, false, "01 February 2023"), //
                    installment(312.0, false, "01 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(312.0, false, "01 February 2023"), //
                    installment(312.0, false, "01 March 2023"), //
                    installment(312.0, false, "01 April 2023"), //
                    installment(314.0, false, "01 May 2023") //
            );
        });
    }

    @Test
    public void test_LoanRepaymentScheduleIsEquallyDistributed_WhenInterestIsPresent_AndInterestTypeIsFlat() {
        runAt("01 January 2023", () -> {
            int amortizationType = AmortizationType.EQUAL_INSTALLMENTS;
            int interestType = InterestType.FLAT;

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    interestType, amortizationType)//
                    .maxInterestRatePerPeriod(12.0)//
                    .minInterestRatePerPeriod(12.0)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(InterestRateFrequencyType.YEARS).interestCalculationPeriodType(SAME_AS_REPAYMENT_PERIOD);

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;
            int numberOfRepayments = 3;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(interestType)//
                    .amortizationType(amortizationType)//
                    .interestCalculationPeriodType(SAME_AS_REPAYMENT_PERIOD)//
                    .interestRatePerPeriod(BigDecimal.valueOf(12));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(416.5, 12.5, 429.0, false, "01 February 2023"), //
                    installment(416.5, 12.5, 429.0, false, "01 March 2023"), //
                    installment(417.0, 12.5, 429.5, false, "01 April 2023") //
            );

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250, null, "01 January 2023"), //
                    installment(416.5, 12.5, 429.0, false, "01 February 2023"), //
                    installment(416.5, 12.5, 429.0, false, "01 March 2023"), //
                    installment(417.0, 12.5, 429.5, false, "01 April 2023") //
            );
        });
    }

    @Test
    public void test_LoanRepaymentScheduleIsEquallyDistributed_WhenInterestIsPresent_AndInterestTypeIsFlat_AndMultiplesOfIs20() {
        runAt("01 January 2023", () -> {
            int amortizationType = AmortizationType.EQUAL_INSTALLMENTS;
            int interestType = InterestType.FLAT;

            // Create Client
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            // Create Loan Product
            PostLoanProductsRequest product = create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
                    interestType, amortizationType)//
                    .maxInterestRatePerPeriod(12.0)//
                    .minInterestRatePerPeriod(12.0)//
                    .interestRatePerPeriod(12.0)//
                    .installmentAmountInMultiplesOf(20).interestRateFrequencyType(InterestRateFrequencyType.YEARS)
                    .interestCalculationPeriodType(SAME_AS_REPAYMENT_PERIOD);

            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            Long loanProductId = loanProductResponse.getResourceId();

            // Apply and Approve Loan
            double amount = 1250.0;
            int numberOfRepayments = 3;

            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "01 January 2023", amount, numberOfRepayments)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestType(interestType)//
                    .amortizationType(amortizationType)//
                    .interestCalculationPeriodType(SAME_AS_REPAYMENT_PERIOD)//
                    .interestRatePerPeriod(BigDecimal.valueOf(12));

            PostLoansResponse postLoansResponse = loanTransactionHelper.applyLoan(applicationRequest);

            PostLoansLoanIdResponse approvedLoanResult = loanTransactionHelper.approveLoan(postLoansResponse.getResourceId(),
                    approveLoanRequest(amount, "01 January 2023"));

            Long loanId = approvedLoanResult.getLoanId();

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(427.5, 12.5, 440.0, false, "01 February 2023"), //
                    installment(427.5, 12.5, 440.0, false, "01 March 2023"), //
                    installment(395.0, 12.5, 407.5, false, "01 April 2023") //
            );

            // disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1250.0), "01 January 2023");

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1250.0, null, "01 January 2023"), //
                    installment(427.5, 12.5, 440.0, false, "01 February 2023"), //
                    installment(427.5, 12.5, 440.0, false, "01 March 2023"), //
                    installment(395.0, 12.5, 407.5, false, "01 April 2023") //
            );
        });
    }
}
