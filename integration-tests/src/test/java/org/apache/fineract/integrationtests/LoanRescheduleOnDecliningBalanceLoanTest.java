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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRescheduleOnDecliningBalanceLoanTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleOnDecliningBalanceLoanTest.class);

    private Long clientId;
    private Long loanProductId;
    private Long loanId;
    private Long loanRescheduleRequestId;
    private final double loanPrincipalAmount = 100000.00;
    private final int numberOfRepayments = 12;
    private final double interestRatePerPeriod = 18;
    private final String dateString = "4 September 2014";

    @AfterEach
    public void tearDown() {
        disableConfig();
    }

    private void createRequiredEntities() {
        this.createClientEntity();
        this.createLoanProductEntity();
        this.createLoanEntity();
        this.enableConfig();
    }

    private void createRequiredEntitiesNoInterest() {
        this.createClientEntity();
        this.createLoanProductEntityNoInterest();
        this.createLoanEntityNoInterest();
        this.enableConfig();
    }

    private void createRequiredEntitiesWithRecalculationEnabled() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculation();
        this.createLoanEntity();
        this.enableConfig();
    }

    private void createClientEntity() {
        this.clientId = createClient();
    }

    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        PostLoanProductsRequest product = decliningBalanceTwelveMonthProduct().principal(loanPrincipalAmount)
                .interestRatePerPeriod(interestRatePerPeriod);

        this.loanProductId = createLoanProduct(product);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private void createLoanProductEntityNoInterest() {
        LOG.info("-------------------------------- - CREATING LOAN PRODUCT ------------------------------------------");

        PostLoanProductsRequest product = decliningBalanceTwelveMonthProduct().principal(loanPrincipalAmount).interestRatePerPeriod(0.0);

        this.loanProductId = createLoanProduct(product);
        LOG.info("Successfully created loan product(ID:{}) ", this.loanProductId);
    }

    private void createLoanProductWithInterestRecalculation() {
        LOG.info(
                "---------------------------------CREATING LOAN PRODUCT WITH RECALULATION ENABLED ------------------------------------------");

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(String.valueOf((int) loanPrincipalAmount))
                .withNumberOfRepayments(String.valueOf(numberOfRepayments))
                .withinterestRatePerPeriod(String.valueOf((int) interestRatePerPeriod)).withInterestRateFrequencyTypeAsYear()
                .withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).build(null);

        this.loanProductId = createLoanProductFromJson(loanProductJSON);
        assertTrue(Boolean.TRUE.equals(retrieveLoanProduct(this.loanProductId).getIsInterestRecalculationEnabled()));
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private PostLoanProductsRequest decliningBalanceTwelveMonthProduct() {
        return new PostLoanProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("12 month declining balance product")//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .principal(loanPrincipalAmount)//
                .numberOfRepayments(numberOfRepayments)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L)//
                .interestRatePerPeriod(interestRatePerPeriod)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)//
                .loanScheduleType(LoanScheduleType.CUMULATIVE.toString())//
                .daysInYearType(LoanTestData.DaysInYearType.ACTUAL)//
                .daysInMonthType(LoanTestData.DaysInMonthType.ACTUAL)//
                .isInterestRecalculationEnabled(false)//
                .accountingRule(1)//
                .multiDisburseLoan(false)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
    }

    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        PostLoansRequest applyRequest = decliningBalanceLoanApplication(this.clientId, this.loanProductId, interestRatePerPeriod);
        this.loanId = applyForLoan(applyRequest);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        approveAndDisburseLoan(this.loanId, loanPrincipalAmount, dateString);
    }

    private void createLoanEntityNoInterest() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        PostLoansRequest applyRequest = decliningBalanceLoanApplication(this.clientId, this.loanProductId, 0.0);
        this.loanId = applyForLoan(applyRequest);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        approveAndDisburseLoan(this.loanId, loanPrincipalAmount, dateString);
    }

    private PostLoansRequest decliningBalanceLoanApplication(Long clientId, Long loanProductId, double interestRate) {
        return applyLoanRequest(clientId, loanProductId, dateString, loanPrincipalAmount, numberOfRepayments, req -> {
            req.interestRatePerPeriod(BigDecimal.valueOf(interestRate));
            req.graceOnPrincipalPayment(2);
            req.graceOnInterestPayment(2);
            req.repaymentEvery(1);
            req.repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.loanTermFrequency(numberOfRepayments);
            req.loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);
            req.interestType(LoanTestData.InterestType.DECLINING_BALANCE);
            req.transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY);
        });
    }

    private void approveAndDisburseLoan(Long loanId, double amount, String date) {
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, date));
        disburseLoan(loanId,
                new PostLoansLoanIdRequest().actualDisbursementDate(date).transactionAmount(getLoanDetails(loanId).getNetDisbursalAmount())
                        .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE));
        LOG.info("Successfully disbursed loan (ID: {} )", loanId);
    }

    private void enableConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    private void disableConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void testCreateLoanRescheduleRequestWithInterestAppropriation() {
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForRecoverInterestInterestFirst();
    }

    @Test
    public void testCreateLoanRescheduleRequestFailIfLoanIsChargedOff() {
        this.createRequiredEntitiesNoInterest();
        this.createLoanRescheduleRequestWhichFailsAsLoanIdChargedOff();
    }

    private void createLoanRescheduleRequestWhichFailsAsLoanIdChargedOff() {
        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithRecalculateInterest(this.loanId, dateString,
                "04 January 2015", "04 October 2015");

        chargeOffLoan(this.loanId, "04 January 2015");

        Map<?, ?> response = loanHelper.createRescheduleRequestWithFullResponse(rescheduleRequest, 403);
        assertEquals("error.msg.loan.is.charged.off",
                ((Map<?, ?>) ((List<?>) response.get("errors")).get(0)).get("userMessageGlobalisationCode"));

        undoChargeOffLoan(this.loanId);
        closeRescheduledLoan(this.loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN)
                .transactionDate("04 January 2015").locale(LoanTestData.LOCALE));
    }

    private void createAndApproveLoanRescheduleRequestForRecoverInterestInterestFirst() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithRecalculateInterest(this.loanId, dateString,
                "04 January 2015", "04 October 2015");

        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));
        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(5);
        BigDecimal totalExpectedRepayment = getLoanDetails(this.loanId).getSummary().getTotalExpectedRepayment();

        assertEquals(10831, period.getTotalDueForPeriod().intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(125184, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleRequestWithRecalculationEnabled() {
        this.createRequiredEntitiesWithRecalculationEnabled();
        this.createAndApproveLoanRescheduleRequestWithRecalculationEnabled();
    }

    private void createAndApproveLoanRescheduleRequestWithRecalculationEnabled() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR LOAN WITH RECALCULATION------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithRecalculateInterest(this.loanId, dateString,
                "04 January 2015", "04 October 2015");

        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));
        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(5);
        BigDecimal totalExpectedRepayment = getLoanDetails(this.loanId).getSummary().getTotalExpectedRepayment();

        assertEquals(10831, period.getTotalDueForPeriod().intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(131512, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleRequestForInterestAppropriationAndFixedEMI() {
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForRecoverInterestFirstAndFixedEMI();
    }

    private void createAndApproveLoanRescheduleRequestForRecoverInterestFirstAndFixedEMI() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithFixedEmiAndRecalculateInterest(this.loanId,
                dateString, "04 January 2015", "04 July 2015", BigDecimal.valueOf(5000), "4 September 2015");

        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));
        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(5);
        GetLoansLoanIdRepaymentPeriod period2 = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(8);
        BigDecimal totalExpectedRepayment = getLoanDetails(this.loanId).getSummary().getTotalExpectedRepayment();

        assertEquals(5000, period.getTotalDueForPeriod().intValue(), "EXPECTED FIXED REPAYMENT is NOK");
        assertEquals(15417, period2.getTotalDueForPeriod().intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(121412, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanRescheduleRequestWithMultpleInterestAppropriation() {
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForRecoverInterestInterestFirst();
        this.createAndApproveLoanRescheduleRequestForSecondInterestAppropriation();
    }

    private void createAndApproveLoanRescheduleRequestForSecondInterestAppropriation() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithRecalculateInterest(this.loanId, dateString,
                "04 December 2015", "04 June 2016");

        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));
        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(7);
        BigDecimal totalExpectedRepayment = getLoanDetails(this.loanId).getSummary().getTotalExpectedRepayment();

        assertEquals(10831, period.getTotalDueForPeriod().intValue(), "EXPECTED REPAYMENT in Second Reschedule is NOK");
        assertEquals(133470, totalExpectedRepayment.intValue(), "TOTAL EXPECTED in Second Reschedule REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }

    @Test
    public void testCreateLoanInterestGreaterThanEMIFromGapWithRecalculationEnabledAndPrincipalCompoundingOff() {
        this.enableConfig();
        this.enablePrincipalCompoundingConfig();
        this.createRequiredEntitiesWithRecalculationEnabledWithPrincipalCompoundingOff();
        this.createApproveLoanRescheduleRequestWithRecalculationEnabledWithPrincipalCompoundingOff();
        this.disablePrincipalCompoundingConfig();
        this.disableConfig();
    }

    private void createRequiredEntitiesWithRecalculationEnabledWithPrincipalCompoundingOff() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculation();
        this.createLoanEntityWithScheduleGapWithInterestGreaterThanEMIAndPrincipalCompoundingOff();
    }

    private void enablePrincipalCompoundingConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    private void disablePrincipalCompoundingConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    private void createLoanEntityWithScheduleGapWithInterestGreaterThanEMIAndPrincipalCompoundingOff() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15000").withLoanTermFrequency("24")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("24").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod("25").withInterestTypeAsDecliningBalance().withSubmittedOnDate(this.dateString)
                .withExpectedDisbursementDate(this.dateString).withFirstRepaymentDate("01 January 2015")
                .withinterestChargedFromDate(this.dateString).build(this.clientId.toString(), this.loanProductId.toString(), null);

        this.loanId = applyForLoanFromJson(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        approveAndDisburseLoan(this.loanId, 15000.0, this.dateString);
    }

    private void createApproveLoanRescheduleRequestWithRecalculationEnabledWithPrincipalCompoundingOff() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR LOAN WITH RECALCULATION------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleWithRecalculateInterest(this.loanId, dateString,
                "01 March 2015", "01 July 2015");

        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule(dateString));
        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(5);
        BigDecimal totalExpectedRepayment = getLoanDetails(this.loanId).getSummary().getTotalExpectedRepayment();

        assertEquals(798, period.getTotalDueForPeriod().intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(22567, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }
}
