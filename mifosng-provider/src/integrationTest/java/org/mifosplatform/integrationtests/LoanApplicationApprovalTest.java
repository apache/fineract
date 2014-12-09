/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import org.mifosplatform.integrationtests.common.ClientHelper;
import org.mifosplatform.integrationtests.common.CommonConstants;
import org.mifosplatform.integrationtests.common.Utils;
import org.mifosplatform.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanProductTestBuilder;
import org.mifosplatform.integrationtests.common.loans.LoanStatusChecker;
import org.mifosplatform.integrationtests.common.loans.LoanTransactionHelper;

@SuppressWarnings("rawtypes")
public class LoanApplicationApprovalTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecForStatusCode403;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForStatusCode403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    /*
     *  Positive test case: Approved amount non zero is less than proposed amount 
     * 
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

        loanStatusHashMap = this.loanTransactionHelper.approveLoanWithApproveAmount(approveDate, approvalAmount, loanID);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    /*
     *  Negative test case: Approved amount non zero is greater than proposed amount 
     * 
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
        
        assertEquals("error.msg.loan.approval.amount.can't.be.greater.than.loan.amount.demanded", error
                .get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        final String loanApplication = new LoanApplicationTestBuilder().withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("2").withExpectedDisbursementDate("04 April 2012")
                .withSubmittedOnDate("02 April 2012").build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }
}
