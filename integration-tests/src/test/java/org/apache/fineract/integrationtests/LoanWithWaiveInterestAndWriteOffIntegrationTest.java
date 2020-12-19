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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Loan Integration Test for checking Loan Disbursement with Waive Interest and Write-Off.
 */
@SuppressWarnings({ "rawtypes" })
public class LoanWithWaiveInterestAndWriteOffIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanWithWaiveInterestAndWriteOffIntegrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private static final String LP_PRINCIPAL = "12,000.00";
    private static final String LP_REPAYMENTS = "2";
    private static final String LP_REPAYMENT_PERIOD = "6";
    private static final String LP_INTEREST_RATE = "1";
    private static final String PRINCIPAL = "4,500.00";
    private static final String LOAN_TERM_FREQUENCY = "18";
    private static final String NUMBER_OF_REPAYMENTS = "9";
    private static final String REPAYMENT_PERIOD = "2";
    private static final String DISBURSEMENT_DATE = "30 October 2010";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "23 September 2010";
    private static final String EXPECTED_DISBURSAL_DATE = "28 October 2010";
    private static final String RATE_OF_INTEREST_PER_PERIOD = "2";
    private static final String DATE_OF_JOINING = "04 March 2009";
    private static final String INTEREST_VALUE_AMOUNT = "40.00";
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkClientLoanCreateAndDisburseFlow() {
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, DATE_OF_JOINING);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // CREATE LOAN PRODUCT
        final Integer loanProductID = createLoanProduct();
        // APPLY FOR LOAN
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // UNDO APPROVAL
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------RE-APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("01 October 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap.toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // PERFORM REPAYMENTS AND CHECK LOAN STATUS
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("01 January 2011", 540.0f, loanID);

        // UNDO DISBURSE LOAN
        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DIBURSE AGAIN
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // MAKE REPAYMENTS
        final float repayment_with_interest = 540.0f;
        final float repayment_without_interest = 500.0f;

        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("01 January 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.makeRepayment("01 March 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("01 May 2011", INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("01 May 2011", repayment_without_interest, loanID);
        this.loanTransactionHelper.makeRepayment("01 July 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("01 September 2011", INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("01 September 2011", repayment_without_interest, loanID);
        this.loanTransactionHelper.makeRepayment("01 November 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("01 January 2012", INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("01 January 2012", repayment_without_interest, loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(7, 1000.0f, loanID);

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(this.loanTransactionHelper.writeOffLoan("01 March 2012", loanID));

    }

    @Test
    public void checkClientLoan_WRITTEN_OFF() {
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, DATE_OF_JOINING);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // CREATE LOAN PRODUCT
        final Integer loanProductID = createLoanProduct();
        // APPLY FOR LOAN
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LOG.info("DISBURSE {}", loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // MAKE REPAYMENTS
        final float repayment_with_interest = 680.0f;

        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("01 January 2011", repayment_with_interest, loanID);

        HashMap toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, loanID);
        Assertions.assertTrue(Float.valueOf("500.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("principalPaid")))) == 0,
                "Checking for Principal paid ");
        Assertions.assertTrue(Float.valueOf("180.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("interestPaid")))) == 0,
                "Checking for interestPaid paid ");
        Assertions.assertTrue(
                Float.valueOf("680.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("totalRepayment")))) == 0,
                "Checking for total paid ");

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(this.loanTransactionHelper.writeOffLoan("01 January 2011", loanID));
        toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, loanID);
        Assertions.assertTrue(
                Float.valueOf("4000.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("principalWrittenOff")))) == 0,
                "Checking for Principal written off ");
        Assertions.assertTrue(
                Float.valueOf("1440.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("interestWrittenOff")))) == 0,
                "Checking for interestPaid written off ");
        Assertions.assertTrue(
                Float.valueOf("5440.0").compareTo(Float.valueOf(String.valueOf(toLoanSummaryAfter.get("totalWrittenOff")))) == 0,
                "Checking for total written off ");

    }

    private Integer createLoanProduct() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(PRINCIPAL)
                .withLoanTermFrequency(LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(NUMBER_OF_REPAYMENTS)
                .withRepaymentEveryAfter(REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(RATE_OF_INTEREST_PER_PERIOD).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualInstallments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE)
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
