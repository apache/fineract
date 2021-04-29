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
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanRescheduleOnDecliningBalanceLoanTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRescheduleOnDecliningBalanceLoanTest.class);
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
    private final String dateString = "4 September 2014";

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
     * Creates the client, loan product, and loan entities
     **/
    private void createRequiredEntities() {
        this.createClientEntity();
        this.createLoanProductEntity();
        this.createLoanEntity();
        this.enableConfig();
    }

    /**
     * Creates the client, loan product, and loan entities
     **/
    private void createRequiredEntitiesWithRecalculationEnabled() {
        this.createClientEntity();
        this.createLoanProductWithInterestRecalculation();
        this.createLoanEntity();
        this.enableConfig();
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);

        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, this.clientId);
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount)
                .withNumberOfRepayments(numberOfRepayments).withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsYear().withInterestTypeAsDecliningBalance().withInterestCalculationPeriodTypeAsDays()
                .build(null);

        this.loanProductId = this.loanTransactionHelper.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", this.loanProductId);
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

    /**
     * submit a new loan application, approve and disburse the loan
     **/
    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeAsDays().withInterestRatePerPeriod(interestRatePerPeriod)
                .withInterestTypeAsDecliningBalance().withSubmittedOnDate(dateString).withExpectedDisbursementDate(dateString)
                .withPrincipalGrace("2").withInterestGrace("2").build(this.clientId.toString(), this.loanProductId.toString(), null);

        this.loanId = this.loanTransactionHelper.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", this.loanId);

        this.approveLoanApplication();
        this.disburseLoan();
    }

    /**
     * approve the loan application
     **/
    private void approveLoanApplication() {

        if (this.loanId != null) {
            this.loanTransactionHelper.approveLoan(this.dateString, this.loanId);
            LOG.info("Successfully approved loan (ID: {} )", this.loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan() {

        if (this.loanId != null) {
            String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, this.loanId);
            this.loanTransactionHelper.disburseLoan(this.dateString, this.loanId,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LOG.info("Successfully disbursed loan (ID: {} )", this.loanId);
        }
    }

    /**
     * enables the configuration `is-interest-to-be-appropriated-equally-when-greater-than-emi`
     **/
    private void enableConfig() {
        GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec, "34", true);
    }

    /**
     * disables the configuration `is-interest-to-be-appropriated-equally-when-greater-than-emi`
     **/
    private void disableConfig() {
        GlobalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec, "34", false);
    }

    @Test
    public void testCreateLoanRescheduleRequestWithInterestAppropriation() {
        // create all required entities
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForInterestAppropriation();

    }

    /**
     * create new loan reschedule request
     **/
    private void createAndApproveLoanRescheduleRequestForInterestAppropriation() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateRescheduleFromDate("04 January 2015").updateAdjustedDueDate("04 October 2015")
                .updateRecalculateInterest(true).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        final String aproveRequestJSON = new LoanRescheduleRequestTestBuilder().getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, aproveRequestJSON);
        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec, loanId,
                "repaymentSchedule");
        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");

        HashMap period = (HashMap) periods.get(5);
        Float totalDueForPeriod = (Float) period.get("totalDueForPeriod");

        final HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(requestSpec, generalResponseSpec, loanId);
        final Float totalExpectedRepayment = (Float) loanSummary.get("totalExpectedRepayment");

        assertEquals(12186, totalDueForPeriod.intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(123682, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

    }

    @Test
    public void testCreateLoanRescheduleRequestWithRecalculationEnabled() {
        // create all required entities
        this.createRequiredEntitiesWithRecalculationEnabled();
        this.createAndApproveLoanRescheduleRequestWithRecalculationEnabled();
    }

    /**
     * create new loan reschedule request with recalculation enabled in Loan product
     **/

    private void createAndApproveLoanRescheduleRequestWithRecalculationEnabled() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR LOAN WITH RECALCULATION------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateRescheduleFromDate("04 January 2015").updateAdjustedDueDate("04 October 2015")
                .updateRecalculateInterest(true).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        final String aproveRequestJSON = new LoanRescheduleRequestTestBuilder().getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, aproveRequestJSON);
        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec, loanId,
                "repaymentSchedule");
        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");

        HashMap period = (HashMap) periods.get(5);
        Float totalDueForPeriod = (Float) period.get("totalDueForPeriod");

        final HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(requestSpec, generalResponseSpec, loanId);
        final Float totalExpectedRepayment = (Float) loanSummary.get("totalExpectedRepayment");

        assertEquals(12326, totalDueForPeriod.intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(131512, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

    }

    @Test
    public void testCreateLoanRescheduleRequestForInterestAppropriationAndFixedEMI() {
        // create all required entities
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForInterestAppropriationAndFixedEMI();
    }

    /**
     * create new loan reschedule request with combination of date change, interest appropriation and fixed emi
     **/
    private void createAndApproveLoanRescheduleRequestForInterestAppropriationAndFixedEMI() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateRescheduleFromDate("04 January 2015").updateAdjustedDueDate("04 July 2015").updateEMI("5000")
                .updateEmiChangeEndDate("4 September 2015").updateRecalculateInterest(true).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        final String aproveRequestJSON = new LoanRescheduleRequestTestBuilder().getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, aproveRequestJSON);
        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec, loanId,
                "repaymentSchedule");
        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");

        HashMap period = (HashMap) periods.get(5);
        Float totalFixedDueForPeriod = (Float) period.get("totalDueForPeriod");

        HashMap period2 = (HashMap) periods.get(8);
        Float totalDueForPeriod = (Float) period2.get("totalDueForPeriod");

        final HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(requestSpec, generalResponseSpec, loanId);
        final Float totalExpectedRepayment = (Float) loanSummary.get("totalExpectedRepayment");

        assertEquals(5000, totalFixedDueForPeriod.intValue(), "EXPECTED FIXED REPAYMENT is NOK");

        assertEquals(15316, totalDueForPeriod.intValue(), "EXPECTED REPAYMENT is NOK");
        assertEquals(120806, totalExpectedRepayment.intValue(), "TOTAL EXPECTED REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

    }

    @Test
    public void testCreateLoanRescheduleRequestWithMultpleInterestAppropriation() {
        // create all required entities
        this.createRequiredEntities();
        this.createAndApproveLoanRescheduleRequestForInterestAppropriation();

        this.createAndApproveLoanRescheduleRequestForSecondInterestAppropriation();

    }

    /**
     * create new loan reschedule request
     **/
    private void createAndApproveLoanRescheduleRequestForSecondInterestAppropriation() {
        LOG.info(
                "---------------------------------CREATING LOAN RESCHEDULE REQUEST FOR INTEREST APPROPRIATTION-------------------------------------");

        final String requestJSON = new LoanRescheduleRequestTestBuilder().updateGraceOnPrincipal(null).updateGraceOnInterest(null)
                .updateExtraTerms(null).updateRescheduleFromDate("04 December 2015").updateAdjustedDueDate("04 June 2016")
                .updateRecalculateInterest(true).build(this.loanId.toString());

        this.loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
        this.loanRescheduleRequestHelper.verifyCreationOfLoanRescheduleRequest(this.loanRescheduleRequestId);

        LOG.info("Successfully created loan reschedule request (ID: {} )", this.loanRescheduleRequestId);

        final String aproveRequestJSON = new LoanRescheduleRequestTestBuilder().getApproveLoanRescheduleRequestJSON();
        this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(this.loanRescheduleRequestId, aproveRequestJSON);
        final HashMap response = (HashMap) this.loanRescheduleRequestHelper.getLoanRescheduleRequest(loanRescheduleRequestId, "statusEnum");
        assertTrue((Boolean) response.get("approved"));

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);

        final Map repaymentSchedule = (Map) this.loanTransactionHelper.getLoanDetail(requestSpec, generalResponseSpec, loanId,
                "repaymentSchedule");
        final ArrayList periods = (ArrayList) repaymentSchedule.get("periods");

        HashMap period = (HashMap) periods.get(7);
        Float totalDueForPeriod = (Float) period.get("totalDueForPeriod");

        final HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(requestSpec, generalResponseSpec, loanId);
        final Float totalExpectedRepayment = (Float) loanSummary.get("totalExpectedRepayment");

        assertEquals(12187, totalDueForPeriod.intValue(), "EXPECTED REPAYMENT in Second Reschedule is NOK");
        assertEquals(130750, totalExpectedRepayment.intValue(), "TOTAL EXPECTED in Second Reschedule REPAYMENT is NOK");

        LOG.info("Successfully approved loan reschedule request (ID: {})", this.loanRescheduleRequestId);
    }
}
