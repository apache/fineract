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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class LoanWithSemiMonthlyRepaymentScheduleTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanRepaymentRescheduleAtDisbursementTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ResponseSpecification generalResponseSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.generalResponseSpec = new ResponseSpecBuilder().build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoanRepaymentPerSemiMonth() {
        final String approveDate = "01 November 2020";
        final String disbursementDate = "01 November 2020";
        final String firstSemiDate = "15 November 2020";
        final String secondSemiDate = "15 November 2020";

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN MULTIDISBURSAL PRODUCT WITH SEMI MONTH REPAYMENT
        final Integer loanProductID = createLoanProductWithSemiMonthlyRepayment(LoanProductTestBuilder.RBI_INDIA_STRATEGY, firstSemiDate,
                secondSemiDate, null, null);

        // APPLY FOR LOAN WITH SEMI MONTHREPAYMENT
        final Integer loanID = applyForLoanApplicationWithSemiMonthlyRepayment(clientID, loanProductID, disbursementDate);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(approveDate, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A FIRST TRANCHE
        this.loanTransactionHelper.disburseLoan(disbursementDate, loanID);
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec,
                loanID);
        HashMap lastInstallment = loanRepaymnetSchedule.get(24);
        Map<String, Object> expectedvalues = new HashMap<>(3);
        Calendar date = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        date.set(2020, Calendar.OCTOBER, 30);
        expectedvalues.put("dueDate", getDateAsArray(date, 0));
        expectedvalues.put("principalDue", "454.7");
        expectedvalues.put("interestDue", "3.53");

        // VALIDATE REPAYMENT SCHEDULE
        verifyLoanRepaymentSchedule(lastInstallment, expectedvalues);

    }

    private void verifyLoanRepaymentSchedule(final HashMap lastInstallment, final Map<String, Object> expectedvalues) {

        assertEquals(expectedvalues.get("dueDate"), lastInstallment.get("dueDate"));
        assertEquals(String.valueOf(expectedvalues.get("principalDue")), String.valueOf(lastInstallment.get("principalDue")));
        assertEquals(String.valueOf(expectedvalues.get("interestDue")), String.valueOf(lastInstallment.get("interestDue")));

    }

    private Integer createLoanProductWithSemiMonthlyRepayment(final String repaymentStrategy, final String firstSemiDate,
            final String secondSemiDate, final Account[] accounts, final String chargeId) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withPrincipal("10000.00").withNumberOfRepayments("12")
                .withRepaymentAfterEvery("2").withRepaymentTypeAsSemiMonth(firstSemiDate, secondSemiDate).withinterestRatePerPeriod("2")
                .withInterestRateFrequencyTypeAsMonths().withTranches(true).withRepaymentStrategy(repaymentStrategy);
        final String loanProductJSON = builder.build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplicationWithSemiMonthlyRepayment(final Integer clientID, final Integer loanProductID,
            final String disbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("10000.00") //
                .withLoanTermFrequency("12") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("24") //
                .withRepaymentEveryAfter("2") //
                .withRepaymentFrequencyTypeAsSemiMonthly() //
                .withInterestRatePerPeriod("2") //
                .withExpectedDisbursementDate(disbursementDate) //
                .withSubmittedOnDate(disbursementDate) //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private List getDateAsArray(Calendar date, int addPeriod) {
        return getDateAsArray(date, addPeriod, Calendar.DAY_OF_MONTH);
    }

    private List getDateAsArray(Calendar date, int addvalue, int type) {
        date.add(type, addvalue);
        return new ArrayList<>(Arrays.asList(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH)));
    }

}
