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
import java.time.LocalDate;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.WorkingDaysHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRescheduleWithAdvancePaymentTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleWithAdvancePaymentTest.class);

    private Long clientId;
    private Long loanProductId;
    private Long loanId;
    private Long loanRescheduleRequestId;
    private final double loanPrincipalAmount = 100000.00;
    private final int numberOfRepayments = 12;
    private final double interestRatePerPeriod = 18;

    @AfterEach
    public void tearDown() {
        disableConfig();
    }

    /**
     * enables the configuration `is-interest-to-be-recovered-first-when-greater-than-emi`
     **/
    private void enableConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    /**
     * disables the configuration `is-interest-to-be-recovered-first-when-greater-than-emi`
     **/
    private void disableConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    /**
     * enables the configuration `is-principal-compounding-disabled-for-overdue-loans`
     **/
    private void enablePrincipalCompoundingConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    /**
     * disables the configuration `is-principal-compounding-disabled-for-overdue-loans`
     **/
    private void disablePrincipalCompoundingConfig() {
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    /* FINERACT-1450 */
    @Test
    public void testRescheduleAfterLatePayment() {
        this.enableConfig();
        this.enablePrincipalCompoundingConfig();
        WorkingDaysHelper.updateWorkingDaysWeekDays();
        // create all required entities
        this.createRequiredEntitiesWithLatePayment();
        this.createApproveLoanRescheduleRequestAfterLatePayment();
        WorkingDaysHelper.updateWorkingDays();
        this.disablePrincipalCompoundingConfig();
        this.disableConfig();
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = createClient();
    }

    private void createRequiredEntitiesWithLatePayment() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculation();
        this.createLoanEntityWithEntitiesForTestResceduleWithLatePayment();
    }

    private void createLoanProductWithInterestRecalculation() {
        LOG.info(
                "---------------------------------CREATING LOAN PRODUCT WITH RECALULATION ENABLED ------------------------------------------");

        PostLoanProductsRequest product = twelveMonthInterestRecalculationProduct()//
                .principal(loanPrincipalAmount)//
                .interestRatePerPeriod(interestRatePerPeriod);

        this.loanProductId = createLoanProduct(product);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private void createLoanEntityWithEntitiesForTestResceduleWithLatePayment() {
        String submittedDate = "2021-05-10";

        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        PostLoansRequest applyRequest = applyLoanRequest(this.clientId, this.loanProductId, submittedDate, 15000.0, 12, req -> {
            req.loanTermFrequency(12);
            req.loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.repaymentEvery(1);
            req.repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.interestRatePerPeriod(BigDecimal.valueOf(12));
            req.interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);
            req.interestType(LoanTestData.InterestType.DECLINING_BALANCE);
            req.amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS);
            req.transactionProcessingStrategyCode(LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY);
            req.dateFormat(LoanTestData.ISO_DATE_PATTERN);
            req.submittedOnDate(submittedDate);
            req.expectedDisbursementDate(submittedDate);
            req.repaymentsStartingFromDate(LocalDate.of(2021, 6, 14));
        });

        this.loanId = applyForLoan(applyRequest);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        approveLoan(this.loanId,
                new org.apache.fineract.client.models.PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(15000))
                        .approvedOnDate(submittedDate).dateFormat(LoanTestData.ISO_DATE_PATTERN).locale(LoanTestData.LOCALE));
        disburseLoan(this.loanId,
                new org.apache.fineract.client.models.PostLoansLoanIdRequest().actualDisbursementDate(submittedDate)
                        .transactionAmount(getLoanDetails(this.loanId).getNetDisbursalAmount()).dateFormat(LoanTestData.ISO_DATE_PATTERN)
                        .locale(LoanTestData.LOCALE));
        LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
    }

    private void createApproveLoanRescheduleRequestAfterLatePayment() {
        LOG.info("-------------Make repayment 1-----------");
        addRepaymentForLoan(this.loanId, 1331.58, "14 June 2021");

        LOG.info("-------------Make repayment 2-----------");
        addRepaymentForLoan(this.loanId, 1331.58, "15 July 2021");

        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR LOAN WITH RECALCULATION------------------------------------");

        PostCreateRescheduleLoansRequest rescheduleRequest = LoanRequestBuilders.rescheduleRequest(this.loanId, "16 August 2022",
                "16 August 2021", "31 August 2021");
        LOG.info("Reschedule request : {}", rescheduleRequest);
        this.loanRescheduleRequestId = createRescheduleRequest(rescheduleRequest);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        approveRescheduleRequest(this.loanRescheduleRequestId, LoanRequestBuilders.approveReschedule("16 August 2022"));

        assertTrue(loanHelper.readRescheduleRequest(this.loanRescheduleRequestId, "statusEnum").getStatusEnum().getApproved());

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(4);
        LOG.info("period  {}", period);

        assertEquals(LocalDate.of(2021, 8, 31), period.getDueDate(), "Checking for Due Date for 1st Month");

    }

    /* FINERACT-1449 */
    @Test
    public void testMultipleAdvancePaymentWithReschedule() {
        this.enableConfig();
        this.enablePrincipalCompoundingConfig();
        WorkingDaysHelper.updateWorkingDaysWeekDays();
        // create all required entities
        this.createRequiredEntitiesForTestMultipleAdvancePaymentWithReschedule();
        this.doMultipleAdvancePaymentsAndVerifySchedule();
        WorkingDaysHelper.updateWorkingDays();
        this.disablePrincipalCompoundingConfig();
        this.disableConfig();
    }

    private void createRequiredEntitiesForTestMultipleAdvancePaymentWithReschedule() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculationForTestMultipleAdvancePaymentWithReschedule();
        this.createLoanEntityForTestMultipleAdvancePaymentWithReschedule();
    }

    private void createLoanProductWithInterestRecalculationForTestMultipleAdvancePaymentWithReschedule() {
        LOG.info(
                "---------------------------------CREATING LOAN PRODUCT WITH RECALULATION ENABLED ------------------------------------------");

        PostLoanProductsRequest product = twelveMonthInterestRecalculationProduct()//
                .principal(loanPrincipalAmount)//
                .interestRatePerPeriod(interestRatePerPeriod)//
                .installmentAmountInMultiplesOf(10);

        this.loanProductId = createLoanProduct(product);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private void createLoanEntityForTestMultipleAdvancePaymentWithReschedule() {
        String submittedDate = "2021-11-29";

        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        PostLoansRequest applyRequest = applyLoanRequest(this.clientId, this.loanProductId, submittedDate, 15000.0, 12, req -> {
            req.loanTermFrequency(12);
            req.loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.repaymentEvery(1);
            req.repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS);
            req.interestRatePerPeriod(BigDecimal.valueOf(12));
            req.interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);
            req.interestType(LoanTestData.InterestType.DECLINING_BALANCE);
            req.amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS);
            req.transactionProcessingStrategyCode(LoanProductTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY);
            req.dateFormat(LoanTestData.ISO_DATE_PATTERN);
            req.submittedOnDate(submittedDate);
            req.expectedDisbursementDate(submittedDate);
            req.repaymentsStartingFromDate(LocalDate.of(2022, 1, 3));
        });

        this.loanId = applyForLoan(applyRequest);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        approveLoan(this.loanId,
                new org.apache.fineract.client.models.PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(15000))
                        .approvedOnDate(submittedDate).dateFormat(LoanTestData.ISO_DATE_PATTERN).locale(LoanTestData.LOCALE));
        disburseLoan(this.loanId,
                new org.apache.fineract.client.models.PostLoansLoanIdRequest().actualDisbursementDate(submittedDate)
                        .transactionAmount(getLoanDetails(this.loanId).getNetDisbursalAmount()).dateFormat(LoanTestData.ISO_DATE_PATTERN)
                        .locale(LoanTestData.LOCALE));
        LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
    }

    private void doMultipleAdvancePaymentsAndVerifySchedule() {

        LOG.info("-------------Make Advance repayment 1-----------");
        addRepaymentForLoan(this.loanId, 1.0, "02 December 2021");

        LOG.info("-------------Make Advance repayment 2-----------");
        addRepaymentForLoan(this.loanId, 1.0, "03 December 2021");

        GetLoansLoanIdRepaymentPeriod period = getLoanDetails(this.loanId).getRepaymentSchedule().getPeriods().get(3);
        LOG.info("period  {}", period);

        assertEquals(LocalDate.of(2022, 1, 3), period.getDueDate(), "Checking for Due Date for 1st Month");
        assertEquals(0, BigDecimal.valueOf(1177.13).compareTo(period.getPrincipalDue()));
        assertEquals(0, BigDecimal.valueOf(152.87).compareTo(period.getInterestDue()));
    }
}
