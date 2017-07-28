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
import java.util.List;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
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
public class LoanDisbursalDateValidationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ResponseSpecification responseForbiddenError;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.responseForbiddenError = new ResponseSpecBuilder().expectStatusCode(403).build();
    }

    @Test
    public void LoanApplicationValidateDisbursalDate() {

        final String proposedAmount = "5000";
        final String approveDate = "1 March 2014";
        final String disbursalDate = "02 March 2014";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        System.out.println("---------------------------------CLIENT CREATED WITH ID---------------------------------------------------"
                + clientID);

        // CREATE LOAN  PRODUCT
        final Integer loanProductID = this.loanTransactionHelper.getLoanProductId(new LoanProductTestBuilder()
                .withSyncExpectedWithDisbursementDate(true).build(null));
        System.out.println("----------------------------------LOAN PRODUCT CREATED WITH ID-------------------------------------------"
                + loanProductID);

        // APPLY FOR LOAN 
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, proposedAmount);
        System.out.println("-----------------------------------LOAN CREATED WITH LOANID-------------------------------------------------"
                + loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        System.out.println("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(approveDate, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A LOAN
        @SuppressWarnings("unchecked")
        List<HashMap> disbursalError = (List<HashMap>) this.loanTransactionHelper.disburseLoan(disbursalDate, loanID, this.responseForbiddenError);
        
        Assert.assertEquals("error.msg.actual.disbursement.date.does.not.match.with.expected.disbursal.date",
        	disbursalError.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));     
       
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String proposedAmount) {
        final String loanApplication = new LoanApplicationTestBuilder()
        		.withPrincipal(proposedAmount).withLoanTermFrequency("5")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("5")
                .withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod("2")
                .withExpectedDisbursementDate("1 March 2014")
                .withSubmittedOnDate("26 February 2014").
                build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplication);
    }

    

}
