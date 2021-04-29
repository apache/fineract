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

import com.google.gson.JsonObject;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
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
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
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
 * Client Loan Integration Test for testing loan repayments with semi monthly schedule.
 */
@SuppressWarnings("rawtypes")
public class LoanWithSemiMonthlyRepaymentScheduleTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanWithSemiMonthlyRepaymentScheduleTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ResponseSpecification generalResponseSpec;

    private static final String LP_PRINCIPAL = "10000.00";
    private static final String LP_INTEREST_RATE_PERIOD = "0.777";
    private static final String FIRST_SEMI_DATE = "15 November 2020";
    private static final String SECOND_SEMI_DATE = "30 November 2020";
    private static final String LOAN_APPROVAL_DATE = "01 November 2020";
    private static final String LOAN_DISBURSEMENT_DATE = "01 November 2020";

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

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN PRODUCT WITH SEMI MONTH REPAYMENT
        JsonObject loanProductConfigurableAttributes = new JsonObject();
        loanProductConfigurableAttributes = createLoanProductConfigurableAttributes(loanProductConfigurableAttributes, true);
        final Integer loanProductID = createLoanProductWithSemiMonthlyRepayment(LoanProductTestBuilder.RBI_INDIA_STRATEGY, LP_PRINCIPAL,
                FIRST_SEMI_DATE, SECOND_SEMI_DATE, LP_INTEREST_RATE_PERIOD, null, null, loanProductConfigurableAttributes);

        // APPLY FOR LOAN WITH SEMI MONTHREPAYMENT
        final Integer loanID = applyForLoanApplicationWithSemiMonthlyRepayment(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A FIRST TRANCHE
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec,
                loanID);
        HashMap lastInstallment = loanRepaymnetSchedule.get(24);
        Map<String, Object> expectedvalues = new HashMap<>(3);
        Calendar date = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        date.set(2021, Calendar.OCTOBER, 30);
        expectedvalues.put("dueDate", getDateAsArray(date, 0));
        expectedvalues.put("principalDue", "416.59");
        expectedvalues.put("interestDue", "50.0");

        // VALIDATE REPAYMENT SCHEDULE
        verifyLoanRepaymentSchedule(lastInstallment, expectedvalues);

        // VALIDATE THE NET DISBURSAL AMOUNT
        assertEquals(Float.valueOf("10000.0"), JsonPath.from(loanDetails).get("netDisbursalAmount"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoanRepaymentPerSemiMonthWithOverpaymentAndUnderpayment() {

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE LOAN PRODUCT WITH SEMI MONTH REPAYMENT
        JsonObject loanProductConfigurableAttributes = new JsonObject();
        loanProductConfigurableAttributes = createLoanProductConfigurableAttributes(loanProductConfigurableAttributes, true);
        final Integer loanProductID = createLoanProductWithSemiMonthlyRepayment(LoanProductTestBuilder.RBI_INDIA_STRATEGY, LP_PRINCIPAL,
                FIRST_SEMI_DATE, SECOND_SEMI_DATE, LP_INTEREST_RATE_PERIOD, null, null, loanProductConfigurableAttributes);

        // APPLY FOR LOAN WITH SEMI MONTHREPAYMENT
        final Integer loanID = applyForLoanApplicationWithSemiMonthlyRepayment(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE LOAN
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec,
                loanID);

        // MAKE AND VALIDATE AN OVERPAYMENT((PRINCIPAL DUE + INTEREST = 416.67+50=466.67) + (100 OVERPAYMENT) = 566.67)
        this.loanTransactionHelper.makeRepayment(FIRST_SEMI_DATE, Float.valueOf("566.67"), loanID);
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);

        final HashMap firstInstallment = loanRepaymnetSchedule.get(1);
        assertEquals(Float.valueOf("50"), firstInstallment.get("interestDue"));
        assertEquals(Float.valueOf("50"), firstInstallment.get("interestPaid"));
        assertEquals(Float.valueOf("416.67"), firstInstallment.get("principalDue"));
        assertEquals(Float.valueOf("416.67"), firstInstallment.get("principalPaid"));

        // Verify overpayment carried over to next installment
        HashMap secondInstallment = loanRepaymnetSchedule.get(2);
        assertEquals(Float.valueOf("50"), secondInstallment.get("interestDue"));
        assertEquals(Float.valueOf("50"), secondInstallment.get("interestPaid"));
        assertEquals(Float.valueOf("416.67"), secondInstallment.get("principalDue"));
        assertEquals(Float.valueOf("50"), secondInstallment.get("principalPaid"));
        assertEquals(Float.valueOf("366.67"), secondInstallment.get("principalOutstanding"));

        // MAKE AND VALIDATE AN UNDERPAYMENT(150 UNDERPAYMENT)
        this.loanTransactionHelper.makeRepayment(SECOND_SEMI_DATE, Float.valueOf("266.67"), loanID);
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);

        secondInstallment = loanRepaymnetSchedule.get(2);
        assertEquals(Float.valueOf("50"), secondInstallment.get("interestDue"));
        assertEquals(Float.valueOf("50"), secondInstallment.get("interestPaid"));
        assertEquals(Float.valueOf("416.67"), secondInstallment.get("principalDue"));
        assertEquals(Float.valueOf("316.67"), secondInstallment.get("principalPaid"));
        assertEquals(Float.valueOf("100.0"), secondInstallment.get("principalOutstanding"));

        // MAKE AND VALIDATE A STANDARD PAYMENT(466.67 PAYMENT)
        this.loanTransactionHelper.makeRepayment(SECOND_SEMI_DATE, Float.valueOf("466.67"), loanID);
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);

        final HashMap thirdInstallment = loanRepaymnetSchedule.get(3);
        assertEquals(Float.valueOf("50"), thirdInstallment.get("interestDue"));
        assertEquals(Float.valueOf("50"), thirdInstallment.get("interestPaid"));
        assertEquals(Float.valueOf("416.67"), thirdInstallment.get("principalDue"));
        assertEquals(Float.valueOf("316.67"), thirdInstallment.get("principalPaid"));
        assertEquals(Float.valueOf("100.0"), thirdInstallment.get("principalOutstanding"));

        // ASSERT OUTSTANDING PRINCIPAL FROM 2nd AND 3rd ARE THESAME
        assertEquals(secondInstallment.get("principalOutstanding"), thirdInstallment.get("principalOutstanding"));

        // PERFORM ASSERTIONS
        loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec, loanID);
        HashMap lastInstallment = loanRepaymnetSchedule.get(24);
        Map<String, Object> expectedvalues = new HashMap<>(3);
        Calendar date = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        date.set(2021, Calendar.OCTOBER, 30);
        expectedvalues.put("dueDate", getDateAsArray(date, 0));
        expectedvalues.put("principalDue", "416.59");
        expectedvalues.put("interestDue", "50.0");

        // VALIDATE REPAYMENT SCHEDULE
        verifyLoanRepaymentSchedule(lastInstallment, expectedvalues);

        // VALIDATE THE NET DISBURSAL AMOUNT
        assertEquals(Float.valueOf("10000.0"), JsonPath.from(loanDetails).get("netDisbursalAmount"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoanRepaymentPerSemiMonthWithCharges() {

        // CREATE CLIENT
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2014");
        LOG.info("---------------------------------CLIENT CREATED WITH ID--------------------------------------------------- {}", clientID);

        // CREATE AND VALIDATE LOAN CHARGE
        final String chargeJsonString = ChargesHelper.getLoanDisbursementJSON(1, "100", 0);
        final Integer chargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec, chargeJsonString);
        Assertions.assertNotNull(chargeId, "Could not create charge");

        // CREATE LOAN PRODUCT WITH SEMI MONTH REPAYMENT
        JsonObject loanProductConfigurableAttributes = new JsonObject();
        loanProductConfigurableAttributes = createLoanProductConfigurableAttributes(loanProductConfigurableAttributes, true);
        final Integer loanProductID = createLoanProductWithSemiMonthlyRepayment(LoanProductTestBuilder.RBI_INDIA_STRATEGY, LP_PRINCIPAL,
                FIRST_SEMI_DATE, SECOND_SEMI_DATE, LP_INTEREST_RATE_PERIOD, null, chargeId.toString(), loanProductConfigurableAttributes);

        // APPLY FOR LOAN WITH SEMI MONTHREPAYMENT
        final Integer loanID = applyForLoanApplicationWithSemiMonthlyRepayment(clientID, loanProductID, LOAN_DISBURSEMENT_DATE);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        // VALIDATE THE LOAN STATUS
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, loanID);

        // VALIDATE THE LOAN IS APPROVED
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        // DISBURSE A FIRST TRANCHE
        String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);

        ArrayList<HashMap> loanRepaymnetSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, generalResponseSpec,
                loanID);
        HashMap lastInstallment = loanRepaymnetSchedule.get(24);
        Map<String, Object> expectedvalues = new HashMap<>(3);
        Calendar date = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        date.set(2021, Calendar.OCTOBER, 30);
        expectedvalues.put("dueDate", getDateAsArray(date, 0));
        expectedvalues.put("principalDue", "416.59");
        expectedvalues.put("interestDue", "50.0");

        // VALIDATE REPAYMENT SCHEDULE
        verifyLoanRepaymentSchedule(lastInstallment, expectedvalues);

        // VALIDATE THE NET DISBURSAL AMOUNT
        assertEquals(Float.valueOf("10000.0"), JsonPath.from(loanDetails).get("netDisbursalAmount"));
    }

    private void verifyLoanRepaymentSchedule(final HashMap lastInstallment, final Map<String, Object> expectedvalues) {

        assertEquals(expectedvalues.get("dueDate"), lastInstallment.get("dueDate"));
        assertEquals(String.valueOf(expectedvalues.get("principalDue")), String.valueOf(lastInstallment.get("principalDue")));
        assertEquals(String.valueOf(expectedvalues.get("interestDue")), String.valueOf(lastInstallment.get("interestDue")));

    }

    private Integer createLoanProductWithSemiMonthlyRepayment(final String repaymentStrategy, final String principal,
            final String firstSemiDate, final String secondSemiDate, final String interestRatePerPeriod, final Account[] accounts,
            final String chargeId, final JsonObject loanProductConfigurableAttributes) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder().withTranches(false).withPrincipal(principal)
                .withNumberOfRepayments("12").withRepaymentAfterEvery("2").withinterestRatePerPeriod("2")
                .withInterestTypeAsDecliningBalance().withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(repaymentStrategy)
                .withinterestRatePerPeriod(interestRatePerPeriod).withRepaymentTypeAsSemiMonth(firstSemiDate, secondSemiDate)
                .withLoanProductConfiguration(loanProductConfigurableAttributes);
        final String loanProductJSON = builder.build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private JsonObject createLoanProductConfigurableAttributes(final JsonObject attributes, final boolean state) {
        attributes.addProperty("graceOnPrincipalAndInterestPayment", state);
        attributes.addProperty("transactionProcessingStrategyId", state);
        attributes.addProperty("interestCalculationPeriodType", state);
        attributes.addProperty("graceOnArrearsAgeing", state);
        attributes.addProperty("inArrearsTolerance", state);
        attributes.addProperty("amortizationType", state);
        attributes.addProperty("repaymentEvery", state);
        attributes.addProperty("interestType", state);
        return attributes;
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
