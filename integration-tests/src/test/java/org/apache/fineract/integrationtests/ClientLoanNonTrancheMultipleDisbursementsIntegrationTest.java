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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Application Repayments Schedule, loan charges, penalties, loan
 * repayments and verifying accounting transactions
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class ClientLoanNonTrancheMultipleDisbursementsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanNonTrancheMultipleDisbursementsIntegrationTest.class);

    private static final String APPLIED_FOR_PRINCIPAL = "12,000.0";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    private Integer createLoanProduct(final boolean isInterestRecalculationEnabled) {
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
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String savingsId, String principal,
            String submitDate, String repaymentsNo) {
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
                .build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    /***
     * Defensive Test case to ensure that the first disbursal for a non-tranche multi-disbursal loan creates a schedule
     */
    @Test
    public void checkThatNonTrancheMultiDisbursalsCreateAScheduleOnFirstDisbursalTest() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product allowing non-tranche multiple disbursals with interest recalculation
         */
        boolean isInterestRecalculationEnabled = true;
        final Integer loanProductID = createLoanProduct(isInterestRecalculationEnabled);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        String submitDate = "01 January 2021";
        Integer repaymentsNo = 3;
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, savingsId, APPLIED_FOR_PRINCIPAL, submitDate,
                repaymentsNo.toString());
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        final Float approved = 9000.00f;
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(submitDate, null, approved.toString(), loanID, null);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);

        LOG.info("-------------------------------DISBURSE non-tranch multi-disbursal loan       ----------");
        final String netDisbursedAmt = null;
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithTransactionAmount(submitDate, loanID, approved.toString());

        GetLoansLoanIdResponse getLoansLoanIdResponse = this.loanTransactionHelper.getLoan(requestSpec, responseSpec, loanID);
        Assertions.assertNotNull(getLoansLoanIdResponse);

        this.loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        Integer loanScheduleLineCount = loanSchedule.size() - 1;
        Assertions.assertEquals(repaymentsNo, loanScheduleLineCount);

        HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        Assertions.assertEquals(approved, loanSummary.get("principalDisbursed"));
        Assertions.assertEquals(approved, loanSummary.get("principalOutstanding"));

        LOG.info("------------------------------- 2nd DISBURSE non-tranch multi-disbursal loan       ----------");
        final Float anotherDisbursalAmount = 900.00f;
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(submitDate, loanID,
                anotherDisbursalAmount.toString(), netDisbursedAmt);

        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanScheduleLineCount = loanSchedule.size() - 2;
        Assertions.assertEquals(repaymentsNo, loanScheduleLineCount);

        loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        Float disbursedSum = approved + anotherDisbursalAmount;
        Assertions.assertEquals(disbursedSum, loanSummary.get("principalDisbursed"));
        Assertions.assertEquals(disbursedSum, loanSummary.get("principalOutstanding"));

        LOG.info("------------------------------- 3rd DISBURSE non-tranch multi-disbursal loan       ----------");
        final Float thirdDisbursalAmount = 500.00f;
        String thirdDisbursalDate = "03 February 2021";
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(thirdDisbursalDate, loanID,
                thirdDisbursalAmount.toString(), netDisbursedAmt);

        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanScheduleLineCount = loanSchedule.size() - 3;
        Assertions.assertEquals(repaymentsNo, loanScheduleLineCount);

        loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        disbursedSum = disbursedSum + thirdDisbursalAmount;
        Assertions.assertEquals(disbursedSum, loanSummary.get("principalDisbursed"));
        Assertions.assertEquals(disbursedSum, loanSummary.get("principalOutstanding"));

    }

    @Test
    public void checkThatNonTrancheMultiDisbursalsCreateAScheduleOnSubmitAndApprovalTest() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product allowing non-tranche multiple disbursals with interest recalculation
         */
        boolean isInterestRecalculationEnabled = true;
        final Integer loanProductID = createLoanProduct(isInterestRecalculationEnabled);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        String submitDate = "01 January 2022";
        Integer repaymentsNo = 3;
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, savingsId, APPLIED_FOR_PRINCIPAL, submitDate,
                repaymentsNo.toString());
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        Integer loanScheduleLineCount = loanSchedule.size() - 1; // exclude disbursement line
        Assertions.assertEquals(repaymentsNo, loanScheduleLineCount);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        final Float approved = 9000.00f;
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(submitDate, null, approved.toString(), loanID, null);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        loanScheduleLineCount = loanSchedule.size() - 1;
        Assertions.assertEquals(repaymentsNo, loanScheduleLineCount);

    }
}
