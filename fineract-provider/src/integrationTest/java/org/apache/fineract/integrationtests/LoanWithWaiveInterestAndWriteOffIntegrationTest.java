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

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Client Loan Integration Test for checking Loan Disbursement with Waive
 * Interest and Write-Off.
 */
@SuppressWarnings({ "rawtypes" })
public class LoanWithWaiveInterestAndWriteOffIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private final String LP_PRINCIPAL = "12,000.00", LP_REPAYMENTS = "2", LP_REPAYMENT_PERIOD = "6", LP_INTEREST_RATE = "1",
            PRINCIPAL = "4,500.00", LOAN_TERM_FREQUENCY = "18", NUMBER_OF_REPAYMENTS = "9", REPAYMENT_PERIOD = "2",
            DISBURSEMENT_DATE = "30 October 2010", LOAN_APPLICATION_SUBMISSION_DATE = "23 September 2010",
            EXPECTED_DISBURSAL_DATE = "28 October 2010", RATE_OF_INTEREST_PER_PERIOD = "2", DATE_OF_JOINING = "04 March 2009",
            INTEREST_VALUE_AMOUNT = "40.00";
    private LoanTransactionHelper loanTransactionHelper;

    @Before
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
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // CREATE LOAN PRODUCT
        final Integer loanProductID = createLoanProduct();
        // APPLY FOR LOAN
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // UNDO APPROVAL
        loanStatusHashMap = this.loanTransactionHelper.undoApproval(loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------RE-APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("1 October 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // PERFORM REPAYMENTS AND CHECK LOAN STATUS
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("1 January 2011", 540.0f, loanID);

        // UNDO DISBURSE LOAN
        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DIBURSE AGAIN
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // MAKE REPAYMENTS
        final float repayment_with_interest = 540.0f;
        final float repayment_without_interest = 500.0f;

        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("1 January 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.makeRepayment("1 March 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("1 May 2011", this.INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("1 May 2011", repayment_without_interest, loanID);
        this.loanTransactionHelper.makeRepayment("1 July 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("1 September 2011", this.INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("1 September 2011", repayment_without_interest, loanID);
        this.loanTransactionHelper.makeRepayment("1 November 2011", repayment_with_interest, loanID);
        this.loanTransactionHelper.waiveInterest("1 January 2012", this.INTEREST_VALUE_AMOUNT, loanID);
        this.loanTransactionHelper.makeRepayment("1 January 2012", repayment_without_interest, loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(7, 1000.0f, loanID);

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(this.loanTransactionHelper.writeOffLoan("1 March 2012", loanID));

    }

    @Test
    public void checkClientLoan_WRITTEN_OFF() {
        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        // CREATE LOAN PRODUCT
        final Integer loanProductID = createLoanProduct();
        // APPLY FOR LOAN
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("28 September 2010", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE
        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.DISBURSEMENT_DATE, loanID);
        System.out.println("DISBURSE " + loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // MAKE REPAYMENTS
        final float repayment_with_interest = 680.0f;

        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, 4000.0F, loanID);
        this.loanTransactionHelper.makeRepayment("1 January 2011", repayment_with_interest, loanID);

        HashMap toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, loanID);
        Assert.assertTrue("Checking for Principal paid ",
                new Float("500.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("principalPaid")))) == 0);
        Assert.assertTrue("Checking for interestPaid paid ",
                new Float("180.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("interestPaid")))) == 0);
        Assert.assertTrue("Checking for total paid ",
                new Float("680.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("totalRepayment")))) == 0);

        // WRITE OFF LOAN AND CHECK ACCOUNT IS CLOSED
        LoanStatusChecker.verifyLoanAccountIsClosed(this.loanTransactionHelper.writeOffLoan("1 January 2011", loanID));
        toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, loanID);
        Assert.assertTrue("Checking for Principal written off ",
                new Float("4000.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("principalWrittenOff")))) == 0);
        Assert.assertTrue("Checking for interestPaid written off ",
                new Float("1440.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("interestWrittenOff")))) == 0);
        Assert.assertTrue("Checking for total written off ",
                new Float("5440.0").compareTo(new Float(String.valueOf(toLoanSummaryAfter.get("totalWrittenOff")))) == 0);

    }

    private Integer createLoanProduct() {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(this.PRINCIPAL)
                .withLoanTermFrequency(this.LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths()
                .withNumberOfRepayments(this.NUMBER_OF_REPAYMENTS).withRepaymentEveryAfter(this.REPAYMENT_PERIOD)
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(this.RATE_OF_INTEREST_PER_PERIOD)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualInstallments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(this.EXPECTED_DISBURSAL_DATE)
                .withSubmittedOnDate(this.LOAN_APPLICATION_SUBMISSION_DATE).build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}
