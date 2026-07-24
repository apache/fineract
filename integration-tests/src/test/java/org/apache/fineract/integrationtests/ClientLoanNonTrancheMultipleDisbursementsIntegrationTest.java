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

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Application Repayments Schedule, loan charges, penalties, loan
 * repayments and verifying accounting transactions
 */
public class ClientLoanNonTrancheMultipleDisbursementsIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanNonTrancheMultipleDisbursementsIntegrationTest.class);

    private static final String APPLIED_FOR_PRINCIPAL = "12,000.0";

    private Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient();
    }

    private Long createLoanProduct(final boolean isInterestRecalculationEnabled) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal(APPLIED_FOR_PRINCIPAL) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withMultiDisburse() //
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true) //
                .withMaxTrancheCount("30") //
                .withDisallowExpectedDisbursements(true);
        if (isInterestRecalculationEnabled) {
            final String interestRecalculationCompoundingMethod = LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE;
            final String rescheduleStrategyMethod = LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS;
            final String recalculationRestFrequencyType = LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY;
            final String recalculationRestFrequencyInterval = "0";
            final String preCloseInterestCalculationStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;
            final String recalculationCompoundingFrequencyType = null;
            final String recalculationCompoundingFrequencyInterval = null;
            final Integer recalculationCompoundingFrequencyOnDayType = null;
            final Integer recalculationCompoundingFrequencyDayOfWeekType = null;
            final Integer recalculationRestFrequencyOnDayType = null;
            final Integer recalculationRestFrequencyDayOfWeekType = null;
            builder = builder
                    .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                            preCloseInterestCalculationStrategy)
                    .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                            recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                    .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                            recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                            recalculationCompoundingFrequencyDayOfWeekType);
        }
        final String loanProductJSON = builder.build(null);
        return createLoanProductFromJson(loanProductJSON);
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductID, String principal, String submitDate,
            String repaymentsNo) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency(repaymentsNo) //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments(repaymentsNo) //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(submitDate) //
                .withTranches(null) //
                .withSubmittedOnDate(submitDate) //
                .build(clientId.toString(), loanProductID.toString(), null);
        return applyForLoanFromJson(loanApplicationJSON);
    }

    /***
     * Defensive Test case to ensure that the first disbursal for a non-tranche multi-disbursal loan creates a schedule
     */
    @Test
    public void checkThatNonTrancheMultiDisbursalsCreateAScheduleOnFirstDisbursalTest() {
        /***
         * Create loan product allowing non-tranche multiple disbursals with interest recalculation
         */
        boolean isInterestRecalculationEnabled = true;
        final Long loanProductID = createLoanProduct(isInterestRecalculationEnabled);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        String submitDate = "01 January 2021";
        Integer repaymentsNo = 3;
        final Long loanID = applyForLoanApplication(clientId, loanProductID, APPLIED_FOR_PRINCIPAL, submitDate, repaymentsNo.toString());
        Assertions.assertNotNull(loanID);
        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        final Double approved = 9000.00;
        approveLoan(loanID, approveLoanRequest(approved, submitDate));
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        LOG.info("-------------------------------DISBURSE non-tranch multi-disbursal loan       ----------");
        disburseLoanWithAmount(loanID, submitDate, approved);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        Assertions.assertNotNull(loanDetails);

        List<GetLoansLoanIdRepaymentPeriod> schedule = loanDetails.getRepaymentSchedule().getPeriods();
        // count repayment periods only (period != null)
        long repaymentPeriodCount = schedule.stream().filter(p -> p.getPeriod() != null).count();
        Assertions.assertEquals(repaymentsNo.longValue(), repaymentPeriodCount);

        Assertions.assertTrue(BigDecimal.valueOf(approved).compareTo(loanDetails.getSummary().getPrincipalDisbursed()) == 0);
        Assertions.assertTrue(BigDecimal.valueOf(approved).compareTo(loanDetails.getSummary().getPrincipalOutstanding()) == 0);

        LOG.info("------------------------------- 2nd DISBURSE non-tranch multi-disbursal loan       ----------");
        final Double anotherDisbursalAmount = 900.00;
        disburseLoan(loanID, submitDate, anotherDisbursalAmount);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        loanDetails = getLoanDetails(loanID);
        schedule = loanDetails.getRepaymentSchedule().getPeriods();
        repaymentPeriodCount = schedule.stream().filter(p -> p.getPeriod() != null).count();
        Assertions.assertEquals(repaymentsNo.longValue(), repaymentPeriodCount);

        Double disbursedSum = approved + anotherDisbursalAmount;
        Assertions.assertTrue(BigDecimal.valueOf(disbursedSum).compareTo(loanDetails.getSummary().getPrincipalDisbursed()) == 0);
        Assertions.assertTrue(BigDecimal.valueOf(disbursedSum).compareTo(loanDetails.getSummary().getPrincipalOutstanding()) == 0);

        LOG.info("------------------------------- 3rd DISBURSE non-tranch multi-disbursal loan       ----------");
        final Double thirdDisbursalAmount = 500.00;
        String thirdDisbursalDate = "03 February 2021";
        disburseLoan(loanID, thirdDisbursalDate, thirdDisbursalAmount);
        verifyLoanStatus(loanID, LoanStatus.ACTIVE);

        loanDetails = getLoanDetails(loanID);
        schedule = loanDetails.getRepaymentSchedule().getPeriods();
        repaymentPeriodCount = schedule.stream().filter(p -> p.getPeriod() != null).count();
        Assertions.assertEquals(repaymentsNo.longValue(), repaymentPeriodCount);

        disbursedSum = disbursedSum + thirdDisbursalAmount;
        Assertions.assertTrue(BigDecimal.valueOf(disbursedSum).compareTo(loanDetails.getSummary().getPrincipalDisbursed()) == 0);
        Assertions.assertTrue(BigDecimal.valueOf(disbursedSum).compareTo(loanDetails.getSummary().getPrincipalOutstanding()) == 0);
    }

    @Test
    public void checkThatNonTrancheMultiDisbursalsCreateAScheduleOnSubmitAndApprovalTest() {
        /***
         * Create loan product allowing non-tranche multiple disbursals with interest recalculation
         */
        boolean isInterestRecalculationEnabled = true;
        final Long loanProductID = createLoanProduct(isInterestRecalculationEnabled);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        String submitDate = "01 January 2022";
        Integer repaymentsNo = 3;
        final Long loanID = applyForLoanApplication(clientId, loanProductID, APPLIED_FOR_PRINCIPAL, submitDate, repaymentsNo.toString());
        Assertions.assertNotNull(loanID);
        verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID);
        List<GetLoansLoanIdRepaymentPeriod> schedule = loanDetails.getRepaymentSchedule().getPeriods();
        long repaymentPeriodCount = schedule.stream().filter(p -> p.getPeriod() != null).count();
        Assertions.assertEquals(repaymentsNo.longValue(), repaymentPeriodCount);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        final Double approved = 9000.00;
        approveLoan(loanID, approveLoanRequest(approved, submitDate));
        verifyLoanStatus(loanID, LoanStatus.APPROVED);

        loanDetails = getLoanDetails(loanID);
        schedule = loanDetails.getRepaymentSchedule().getPeriods();
        repaymentPeriodCount = schedule.stream().filter(p -> p.getPeriod() != null).count();
        Assertions.assertEquals(repaymentsNo.longValue(), repaymentPeriodCount);
    }
}
