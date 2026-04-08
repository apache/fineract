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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.WorkingDaysHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRescheduleWithAdvancePaymentTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleWithAdvancePaymentTest.class);
    private ResponseSpecification responseSpec;
    private ResponseSpecification generalResponseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private Integer clientId;
    private Integer loanProductId;
    private Integer loanId;
    private Integer loanRescheduleRequestId;
    private final String loanPrincipalAmount = "100000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";

    @BeforeEach
    public void initialize() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);

        this.generalResponseSpec = new ResponseSpecBuilder().build();

    }

    @AfterEach
    public void tearDown() {
        disableConfig();
    }

    /**
     * enables the configuration `is-interest-to-be-recovered-first-when-greater-than-emi`
     **/
    private void enableConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    /**
     * disables the configuration `is-interest-to-be-recovered-first-when-greater-than-emi`
     **/
    private void disableConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.IS_INTEREST_TO_BE_RECOVERED_FIRST_WHEN_GREATER_THAN_EMI,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    /**
     * enables the configuration `is-principal-compounding-disabled-for-overdue-loans`
     **/
    private void enablePrincipalCompoundingConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(true));
    }

    /**
     * disables the configuration `is-principal-compounding-disabled-for-overdue-loans`
     **/
    private void disablePrincipalCompoundingConfig() {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    /**
     * approve the loan application
     **/
    private void approveLoanApplication(String approveDate) {

        if (this.loanId != null) {
            this.loanTransactionHelper.approveLoan(approveDate, this.loanId);
            LOG.info("Successfully approved loan (ID: {} )", this.loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan(String disburseDate) {

        if (this.loanId != null) {
            String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, this.loanId);
            this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(disburseDate, this.loanId,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
        }
    }

    /* FINERACT-1450 */
    @Test
    public void testRescheduleAfterLatePayment() {
        this.enableConfig();
        this.enablePrincipalCompoundingConfig();
        WorkingDaysHelper.updateWorkingDaysWeekDays(this.requestSpec, this.responseSpec);
        // create all required entities
        this.createRequiredEntitiesWithLatePayment();
        this.createApproveLoanRescheduleRequestAfterLatePayment();
        WorkingDaysHelper.updateWorkingDays(this.requestSpec, this.responseSpec);
        this.disablePrincipalCompoundingConfig();
        this.disableConfig();
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, this.clientId);
    }

    private void createRequiredEntitiesWithLatePayment() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculation();
        this.createLoanEntityWithEntitiesForTestResceduleWithLatePayment();
    }

    private void createLoanProductWithInterestRecalculation() {
        LOG.info(
                "---------------------------------CREATING LOAN PRODUCT WITH RECALULATION ENABLED ------------------------------------------");

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

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount)
                .withNumberOfRepayments(numberOfRepayments).withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType)
                .build(null);

        this.loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private void createLoanEntityWithEntitiesForTestResceduleWithLatePayment() {
        String firstRepaymentDate = "14 June 2021";
        String submittedDate = "10 May 2021";

        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod("12").withInterestTypeAsDecliningBalance().withSubmittedOnDate(submittedDate)
                .withExpectedDisbursementDate(submittedDate).withFirstRepaymentDate(firstRepaymentDate)
                .withRepaymentStrategy(LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY)
                .withinterestChargedFromDate(submittedDate).build(this.clientId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        this.approveLoanApplication(submittedDate);
        this.disburseLoan(submittedDate);
    }

    private void createApproveLoanRescheduleRequestAfterLatePayment() {
        LOG.info("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("14 June 2021", Float.parseFloat("1331.58"), loanId);

        LOG.info("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("15 July 2021", Float.parseFloat("1331.58"), loanId);

        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR LOAN WITH RECALCULATION------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateRescheduleFromDate("16 August 2021").updateAdjustedDueDate("31 August 2021")
                .updateRecalculateInterest(false).updateSubmittedOnDate("16 August 2022").build(this.loanId.toString());
        LOG.info("Reschedule request : {}", requestJSON);
        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        final String aproveRequestJSON = new LoanRescheduleRequestTestBuilder().updateSubmittedOnDate("16 August 2022")
                .getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, aproveRequestJSON);

        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetailExcludeFutureSchedule(requestSpec, generalResponseSpec,
                this.loanId, "repaymentSchedule");
        LOG.info("Repayment Schedule for id {} : {}", this.loanId, repaymentSchedule);
        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");

        HashMap period = (HashMap) periods.get(4);
        LOG.info("period  {}", period);

        assertEquals(new ArrayList<>(Arrays.asList(2021, 8, 31)), period.get("dueDate"), "Checking for Due Date for 1st Month");

    }

    /* FINERACT-1449 */
    @Test
    public void testMultipleAdvancePaymentWithReschedule() {
        this.enableConfig();
        this.enablePrincipalCompoundingConfig();
        WorkingDaysHelper.updateWorkingDaysWeekDays(this.requestSpec, this.responseSpec);
        // create all required entities
        this.createRequiredEntitiesForTestMultipleAdvancePaymentWithReschedule();
        this.doMultipleAdvancePaymentsAndVerifySchedule();
        WorkingDaysHelper.updateWorkingDays(this.requestSpec, this.responseSpec);
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

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount)
                .withNumberOfRepayments(numberOfRepayments).withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(interestRecalculationCompoundingMethod, rescheduleStrategyMethod,
                        preCloseInterestCalculationStrategy)
                .withInterestRecalculationRestFrequencyDetails(recalculationRestFrequencyType, recalculationRestFrequencyInterval,
                        recalculationRestFrequencyOnDayType, recalculationRestFrequencyDayOfWeekType)
                .withInterestRecalculationCompoundingFrequencyDetails(recalculationCompoundingFrequencyType,
                        recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyOnDayType,
                        recalculationCompoundingFrequencyDayOfWeekType)
                .withInstallmentAmountInMultiplesOf("10").build(null);

        this.loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
    }

    private void createLoanEntityForTestMultipleAdvancePaymentWithReschedule() {
        String firstRepaymentDate = "03 January 2022";
        String submittedDate = "29 November 2021";

        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("15000").withLoanTermFrequency("12")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("12").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeAsDays()
                .withInterestRatePerPeriod("12").withInterestTypeAsDecliningBalance().withSubmittedOnDate(submittedDate)
                .withExpectedDisbursementDate(submittedDate).withFirstRepaymentDate(firstRepaymentDate)
                .withRepaymentStrategy(LoanApplicationTestBuilder.INTEREST_PRINCIPAL_PENALTIES_FEES_ORDER_STRATEGY)
                .withinterestChargedFromDate(submittedDate).build(this.clientId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        this.approveLoanApplication(submittedDate);
        this.disburseLoan(submittedDate);
    }

    private void doMultipleAdvancePaymentsAndVerifySchedule() {

        LOG.info("-------------Make Advance repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("02 December 2021", Float.parseFloat("1"), this.loanId);

        LOG.info("-------------Make Advance repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("03 December 2021", Float.parseFloat("1"), this.loanId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetailExcludeFutureSchedule(requestSpec, generalResponseSpec,
                this.loanId, "repaymentSchedule");

        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");
        HashMap period = (HashMap) periods.get(3);
        LOG.info("period  {}", period);

        assertEquals(new ArrayList<>(Arrays.asList(2022, 1, 3)), period.get("dueDate"), "Checking for Due Date for 1st Month");
        assertEquals(period.get("principalDue"), Float.parseFloat("1177.12"));
        assertEquals(period.get("interestDue"), Float.parseFloat("152.88"));
    }
}
