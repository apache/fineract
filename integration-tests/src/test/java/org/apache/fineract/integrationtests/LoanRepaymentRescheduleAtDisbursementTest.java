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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansDisbursementData;
import org.apache.fineract.client.models.PostLoansLoanIdDisbursementData;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Test;

public class LoanRepaymentRescheduleAtDisbursementTest extends FeignLoanTestBase {

    @Test
    public void testLoanRepaymentRescheduleAtDisbursement() {
        final String approvalAmount = "10000";
        final String approveDate = "01 March 2015";
        final String expectedDisbursementDate = "01 March 2015";
        final String disbursementDate = "01 March 2015";
        final String adjustRepaymentDate = "16 March 2015";

        Long clientId = createClient("01 January 2014");

        Long loanProductId = createLoanProduct(buildLoanProductRequest());

        List<PostLoansDisbursementData> createTranches = List.of(LoanRequestBuilders.applyTrancheDetail("01 March 2015", 5000.0),
                LoanRequestBuilders.applyTrancheDetail("01 May 2015", 5000.0));

        List<PostLoansLoanIdDisbursementData> approveTranches = List.of(LoanRequestBuilders.approveTrancheDetail("01 March 2015", 5000.0),
                LoanRequestBuilders.approveTrancheDetail("01 May 2015", 5000.0));

        Long loanId = applyForLoan(buildLoanApplication(clientId, loanProductId, disbursementDate, createTranches));

        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        approveLoan(loanId, LoanRequestBuilders.approveLoanWithTranches(Double.valueOf(approvalAmount), approveDate,
                expectedDisbursementDate, approveTranches));

        GetLoansLoanIdResponse approvedLoan = getLoanDetails(loanId);
        verifyLoanStatus(approvedLoan, LoanStatus.APPROVED);
        LoanTestValidators.verifyLoanStatus(approvedLoan, status -> Boolean.TRUE.equals(status.getWaitingForDisbursal()));

        disburseLoanWithRepaymentReschedule(loanId, disbursementDate, adjustRepaymentDate);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        GetLoansLoanIdRepaymentPeriod firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                .filter(period -> period.getPeriod() != null && period.getPeriod() == 1).findFirst().orElseThrow();

        LoanTestValidators.validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2015, 3, 16), 834.71, 0.0, 834.71, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 49.32, 0.0, 49.32, 0, 0);
        assertEquals(884.03, Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()));
    }

    private PostLoanProductsRequest buildLoanProductRequest() {
        return new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("12").withRepaymentAfterEvery("2")
                .withRepaymentTypeAsWeek().withinterestRatePerPeriod("2").withInterestRateFrequencyTypeAsMonths().withTranches(true)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withRepaymentStrategy(LoanProductTestBuilder.RBI_INDIA_STRATEGY)
                .withInterestTypeAsDecliningBalance()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).buildRequest(null);
    }

    private PostLoansRequest buildLoanApplication(Long clientId, Long loanProductId, String disbursementDate,
            List<PostLoansDisbursementData> tranches) {
        return new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .principal(new BigDecimal("10000.00"))//
                .loanTermFrequency(24)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .numberOfRepayments(12)//
                .repaymentEvery(2)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.WEEKS)//
                .interestRatePerPeriod(new BigDecimal("2"))//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .disbursementData(tranches)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .expectedDisbursementDate(disbursementDate)//
                .submittedOnDate(disbursementDate)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.RBI_INDIA_STRATEGY)//
                .charges(List.of())//
                .loanType("individual")//
                .maxOutstandingLoanBalance(new BigDecimal("36000"))//
                .collateral(List.of())//
                .locale("en_GB")//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }
}
