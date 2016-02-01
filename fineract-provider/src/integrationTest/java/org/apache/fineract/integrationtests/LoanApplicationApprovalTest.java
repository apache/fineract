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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class LoanApplicationApprovalTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForStatusCode403;
    private ResponseSpecification responseSpecForStatusCode400;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForStatusCode403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.responseSpecForStatusCode400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    /*
     * Positive test case: Approved amount non zero is less than proposed amount
     */
    @Test
    public void loanApplicationApprovedAmountLessThanProposedAmount() {

        final String proposedAmount = "8000";
        final String approvalAmount = "5000";
        final String approveDate = "20 September 2012";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, proposedAmount);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        final String expectedDisbursementDate = null;
        List<HashMap> approveTranches = null;
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    /*
     * Negative test case: Approved amount non zero is greater than proposed
     * amount
     */
    @Test
    public void loanApplicationApprovedAmountGreaterThanProposedAmount() {

        final String proposedAmount = "5000";
        final String approvalAmount = "9000";
        final String approveDate = "20 September 2011";

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder().build(null));
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, proposedAmount);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);

        @SuppressWarnings("unchecked")
        List<HashMap> error = (List<HashMap>) this.loanTransactionHelper.approveLoan(approveDate, approvalAmount, loanID,
                CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.loan.approval.amount.can't.be.greater.than.loan.amount.demanded",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    public HashMap createTrancheDetail(final String date, final String amount) {
        HashMap<String, Object> detail = new HashMap<>();
        detail.put("expectedDisbursementDate", date);
        detail.put("principal", amount);

        return detail;
    }

    @Test
    public void loanApplicationApprovalAndValidationForMultiDisburseLoans() {

        List<HashMap> createTranches = new ArrayList<>();
        createTranches.add(createTrancheDetail("1 March 2014", "1000"));
        createTranches.add(createTrancheDetail("23 March 2014", "4000"));

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withInterestTypeAsDecliningBalance().withTranches(true).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        this.trancheLoansApprovedAmountLesserThanProposedAmount(clientID, loanProductID, createTranches);
        this.trancheLoansApprovalValidation(clientID, loanProductID, createTranches);

    }

    private void trancheLoansApprovedAmountLesserThanProposedAmount(Integer clientID, Integer loanProductID, List<HashMap> createTranches) {
        final String proposedAmount = "5000";
        final String approvalAmount = "2000";
        final String approveDate = "1 March 2014";
        final String expectedDisbursementDate = "1 March 2014";

        List<HashMap> approveTranches = new ArrayList<>();
        approveTranches.add(createTrancheDetail("1 March 2014", "1000"));
        approveTranches.add(createTrancheDetail("23 March 2014", "1000"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, expectedDisbursementDate, approvalAmount,
                loanID, approveTranches);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
        System.out
                .println("-----------------------------------MULTI DISBURSAL LOAN APPROVED SUCCESSFULLY---------------------------------------");

    }

    private void trancheLoansApprovalValidation(Integer clientID, Integer loanProductID, List<HashMap> createTranches) {
        final String proposedAmount = "5000";
        final String approvalAmount1 = "10000";
        final String approvalAmount2 = "3000";
        final String approvalAmount3 = "400";
        final String approvalAmount4 = "200";

        final String approveDate = "1 March 2014";
        final String expectedDisbursementDate = "1 March 2014";

        List<HashMap> approveTranche1 = new ArrayList<>();
        approveTranche1.add(createTrancheDetail("1 March 2014", "5000"));
        approveTranche1.add(createTrancheDetail("23 March 2014", "5000"));

        List<HashMap> approveTranche2 = new ArrayList<>();
        approveTranche2.add(createTrancheDetail("1 March 2014", "1000"));
        approveTranche2.add(createTrancheDetail("23 March 2014", "1000"));
        approveTranche2.add(createTrancheDetail("23 March 2014", "1000"));

        List<HashMap> approveTranche3 = new ArrayList<>();
        approveTranche3.add(createTrancheDetail("1 March 2014", "100"));
        approveTranche3.add(createTrancheDetail("23 March 2014", "100"));
        approveTranche3.add(createTrancheDetail("24 March 2014", "100"));
        approveTranche3.add(createTrancheDetail("25 March 2014", "100"));

        List<HashMap> approveTranche4 = new ArrayList<>();
        approveTranche4.add(createTrancheDetail("1 March 2014", "100"));
        approveTranche4.add(createTrancheDetail("23 March 2014", "100"));
        approveTranche4.add(createTrancheDetail("24 March 2014", "100"));

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, proposedAmount, createTranches);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode400);

        /* Tranches with same expected disbursement date */
        List<HashMap<String, Object>> error = this.loanTransactionHelper.approveLoanForTranches(approveDate, expectedDisbursementDate,
                approvalAmount2, loanID, approveTranche2, CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.loan.expectedDisbursementDate.disbursement.date.must.be.unique.for.tranches",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /* Sum of tranches is greater than approved amount */
        error = this.loanTransactionHelper.approveLoanForTranches(approveDate, expectedDisbursementDate, approvalAmount4, loanID,
                approveTranche4, CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.loan.principal.sum.of.multi.disburse.amounts.must.be.equal.to.or.lesser.than.approved.principal",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpecForStatusCode403);

        /* Sum of tranches exceeds the proposed amount */
        error = this.loanTransactionHelper.approveLoanForTranches(approveDate, expectedDisbursementDate, approvalAmount1, loanID,
                approveTranche1, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.loan.approval.amount.can't.be.greater.than.loan.amount.demanded",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /* No. of tranches exceeds the max tranche count at product level */
        error = this.loanTransactionHelper.approveLoanForTranches(approveDate, expectedDisbursementDate, approvalAmount3, loanID,
                approveTranche3, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.disbursementData.exceeding.max.tranche.count", error.get(0)
                .get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        /* If tranches are not specified for a multi-disburse loan */
        /**
         * error =
         * this.loanTransactionHelper.approveLoanForTranches(approveDate,
         * expectedDisbursementDate, approvalAmount5, loanID, approveTranche5,
         * CommonConstants.RESPONSE_ERROR);
         * assertEquals("error.msg.disbursementData.required",
         * error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
         **/
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
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
