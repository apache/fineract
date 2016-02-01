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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.LoanApplicationApprovalTest;
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

@SuppressWarnings("rawtypes")
public class LoanApplicationUndoLastTrancheTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanApplicationApprovalTest loanApplicationApprovalTest;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanApplicationApprovalTest = new LoanApplicationApprovalTest();
    }

    @Test
    public void LoanApplicationUndoLastTranche() {

        final String proposedAmount = "5000";
        final String approvalAmount = "2000";
        final String approveDate = "1 March 2014";
        final String expectedDisbursementDate = "1 March 2014";
        final String disbursalDate = "1 March 2014";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withInterestTypeAsDecliningBalance().withTranches(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // CREATE TRANCHES
        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("1 March 2014", "1000"));
        createTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("23 June 2014", "4000"));

        // APPROVE TRANCHES
        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("1 March 2014", "1000"));
        approveTranches.add(this.loanApplicationApprovalTest.createTrancheDetail("23 June 2014", "1000"));

        // APPLY FOR LOAN WITH TRANCHES
        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        this.loanTransactionHelper.disburseLoan(disbursalDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN IS ACTIVE STATUS
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        System.out.println("-------------Make repayment 1-----------");
        this.loanTransactionHelper.makeRepayment("01 April 2014", Float.valueOf("420"), loanID);
        System.out.println("-------------Make repayment 2-----------");
        this.loanTransactionHelper.makeRepayment("01 May 2014", Float.valueOf("412"), loanID);
        System.out.println("-------------Make repayment 3-----------");
        this.loanTransactionHelper.makeRepayment("01 June 2014", Float.valueOf("204"), loanID);
        // DISBURSE A SECOND TRANCHE
        this.loanTransactionHelper.disburseLoan("23 June 2014", loanID);
        // UNDO LAST TRANCHE
        Float disbursedAmount = this.loanTransactionHelper.undoLastDisbursal(loanID);
        validateDisbursedAmount(disbursedAmount);
    }

    private void validateDisbursedAmount(Float disbursedAmount) {
        Assert.assertEquals(Float.valueOf("1000.0"), disbursedAmount);

    }

    public Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, String principal,
            List<HashMap> tranches) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder()
        //
                .withPrincipal(principal)
                //
                .withLoanTermFrequency("5")
                //
                .withLoanTermFrequencyAsMonths()
                //
                .withNumberOfRepayments("5").withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withExpectedDisbursementDate("1 March 2014") //
                .withTranches(tranches) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate("1 March 2014") //
                .build(clientID.toString(), loanProductID.toString(), null);

        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

}
