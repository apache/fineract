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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class ClientLoanMultipleDisbursementsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanMultipleDisbursementsIntegrationTest.class);

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

    private Integer createLoanProduct(final boolean multiDisburseLoan) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("12,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
            builder = builder.withMaxTrancheCount("30");
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplicationWithTranches(final Integer clientID, final Integer loanProductID, final String savingsId,
            String principal, List<HashMap> tranches, String submitDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(submitDate) //
                .withTranches(tranches) //
                .withSubmittedOnDate(submitDate) //
                .build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private HashMap createTrancheDetail(final String date, final String amount) {
        HashMap detail = new HashMap();
        detail.put("expectedDisbursementDate", date);
        detail.put("principal", amount);

        return detail;
    }

    /***
     * Test case to verify repayment schedule shows all disbursals for tranche loans
     */
    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroTest() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Integer loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        LOG.info("-----------------------------------10 Tranches--------------------------------------");
        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        tranches.add(createTrancheDetail("03 January 2021", "4"));
        tranches.add(createTrancheDetail("04 January 2021", "8"));
        tranches.add(createTrancheDetail("05 January 2021", "16"));
        tranches.add(createTrancheDetail("06 January 2021", "32"));
        tranches.add(createTrancheDetail("07 January 2021", "64"));
        tranches.add(createTrancheDetail("08 January 2021", "128"));
        tranches.add(createTrancheDetail("09 January 2021", "256"));
        tranches.add(createTrancheDetail("10 January 2021", "512"));
        String submitDate = "01 January 2021";

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, savingsId, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("01 January 2021", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE 8 LOANS -------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("12 January 2021", loanID, "1");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("12 January 2021", loanID, "2");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("12 January 2021", loanID, "4");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("13 January 2021", loanID, "8");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("14 January 2021", loanID, "16");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("14 January 2021", loanID, "32");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("15 January 2021", loanID, "64");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("15 January 2021", loanID, "128");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 9;
        final int expectedDisbursals = 8;
        final BigDecimal val255 = BigDecimal.valueOf(255.0);
        final BigDecimal expectedTotalPrincipalDisbursed = val255;
        final BigDecimal expectedPrincipalDue = val255;
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking nine lines in schedule");

        int disbursalCount = 0;
        BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
        // First 8 lines should be disbursals
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            final Integer period = (Integer) loanSchedule.get(i).get("period");
            final BigDecimal principalDisbursed = BigDecimal
                    .valueOf(Double.parseDouble(loanSchedule.get(i).get("principalDisbursed").toString()));

            if (period == null) {
                disbursalCount += 1;
                totalPrincipalDisbursed = totalPrincipalDisbursed.add(principalDisbursed);
            }
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for eight disbursals");
        assertEquals(expectedTotalPrincipalDisbursed, totalPrincipalDisbursed, "Checking Principal Disburse is 255");

        final BigDecimal principalDue = BigDecimal.valueOf(Double.parseDouble(loanSchedule.get(8).get("principalDue").toString()));
        assertEquals(expectedPrincipalDue, principalDue, "Checking Principal Due is 255");

        final BigDecimal principalLoanBalanceOutstanding = BigDecimal
                .valueOf(Double.parseDouble(loanSchedule.get(8).get("principalLoanBalanceOutstanding").toString()));
        assertEquals(expectedPrincipalLoanBalanceOutstanding, principalLoanBalanceOutstanding,
                "Checking Principal Loan Balance Outstanding is zero");

    }

    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroButLoanGotReopenedTest() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Integer loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        LOG.info("-----------------------------------2 Tranches--------------------------------------");
        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        String submitDate = "01 January 2021";

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, savingsId, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("01 January 2021", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info(
                "-------------------------------DISBURSE 1, repay fully, disburse again LOANS -------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithTransactionAmount("12 January 2021", loanID, "1");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        HashMap repaymentDetails = this.loanTransactionHelper.makeRepayment("13 January 2021", 1.0f, loanID);
        loanStatusHashMap = this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithTransactionAmount("14 January 2021", loanID, "2");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 3;
        final int expectedDisbursals = 2;
        final BigDecimal expectedTotalPrincipalDisbursed = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalDue = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalPaid = BigDecimal.valueOf(1.0);
        final BigDecimal expectedPrincipalOutstanding = BigDecimal.valueOf(2.0);
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking 3 lines in schedule");

        int disbursalCount = 0;
        BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
        // First 8 lines should be disbursals
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            final Integer period = (Integer) loanSchedule.get(i).get("period");
            final BigDecimal principalDisbursed = BigDecimal
                    .valueOf(Double.parseDouble(loanSchedule.get(i).get("principalDisbursed").toString()));

            if (period == null) {
                disbursalCount += 1;
                totalPrincipalDisbursed = totalPrincipalDisbursed.add(principalDisbursed);
            }
            // LOG.info(loanSchedule.get(i).toString());
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for 2 disbursals");
        assertEquals(expectedTotalPrincipalDisbursed, totalPrincipalDisbursed, "Checking Principal Disburse is 3");

        final BigDecimal principalDue = BigDecimal.valueOf(Double.parseDouble(loanSchedule.get(2).get("principalDue").toString()));
        assertEquals(expectedPrincipalDue, principalDue, "Checking Principal Due is 3");
        final BigDecimal principalPaid = BigDecimal.valueOf(Double.parseDouble(loanSchedule.get(2).get("principalPaid").toString()));
        assertEquals(expectedPrincipalPaid, principalPaid, "Checking Principal Paid is 1");
        final BigDecimal principalOutstanding = BigDecimal
                .valueOf(Double.parseDouble(loanSchedule.get(2).get("principalOutstanding").toString()));
        assertEquals(expectedPrincipalOutstanding, principalOutstanding, "Checking Principal Due is 2");

        final BigDecimal principalLoanBalanceOutstanding = BigDecimal
                .valueOf(Double.parseDouble(loanSchedule.get(2).get("principalLoanBalanceOutstanding").toString()));
        assertEquals(expectedPrincipalLoanBalanceOutstanding, principalLoanBalanceOutstanding,
                "Checking Principal Loan Balance Outstanding is zero");

    }

    @Test
    public void checkThatAllMultiDisbursalsAppearOnLoanScheduleAndOutStandingBalanceIsZeroButLoanGotReopenedFromOverPaidTest() {
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        /***
         * Create loan product with allowing multiple disbursals
         */
        boolean allowMultipleDisbursals = true;
        final Integer loanProductID = createLoanProduct(allowMultipleDisbursals);
        Assertions.assertNotNull(loanProductID);

        /***
         * Apply for loan application and verify loan status
         */
        final String savingsId = null;
        final String principal = "12,000.00";

        LOG.info("-----------------------------------2 Tranches--------------------------------------");
        List<HashMap> tranches = new ArrayList<>();
        tranches.add(createTrancheDetail("01 January 2021", "1"));
        tranches.add(createTrancheDetail("02 January 2021", "2"));
        String submitDate = "01 January 2021";

        final Integer loanID = applyForLoanApplicationWithTranches(clientID, loanProductID, savingsId, principal, tranches, submitDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan("01 January 2021", loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info(
                "-------------------------------DISBURSE 1, repay fully, disburse again LOANS -------------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithTransactionAmount("12 January 2021", loanID, "1");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        HashMap repaymentDetails = this.loanTransactionHelper.makeRepayment("13 January 2021", 2.0f, loanID);
        loanStatusHashMap = this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanID, "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithTransactionAmount("14 January 2021", loanID, "2");
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec, loanID);
        final int loanScheduleLineCount = loanSchedule.size();
        final int expectedLoanScheduleLineCount = 3;
        final int expectedDisbursals = 2;
        final BigDecimal expectedTotalPrincipalDisbursed = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalDue = BigDecimal.valueOf(3.0);
        final BigDecimal expectedPrincipalPaid = BigDecimal.valueOf(2.0);
        final BigDecimal expectedPrincipalOutstanding = BigDecimal.valueOf(1.0);
        final BigDecimal expectedPrincipalLoanBalanceOutstanding = BigDecimal.valueOf(0.0);

        assertEquals(expectedLoanScheduleLineCount, loanScheduleLineCount, "Checking nine lines in schedule");

        int disbursalCount = 0;
        BigDecimal totalPrincipalDisbursed = BigDecimal.ZERO;
        // First 8 lines should be disbursals
        for (int i = 0; i < loanScheduleLineCount - 1; i++) {
            final Integer period = (Integer) loanSchedule.get(i).get("period");
            final BigDecimal principalDisbursed = BigDecimal
                    .valueOf(Double.parseDouble(loanSchedule.get(i).get("principalDisbursed").toString()));

            if (period == null) {
                disbursalCount += 1;
                totalPrincipalDisbursed = totalPrincipalDisbursed.add(principalDisbursed);
            }
            // LOG.info(loanSchedule.get(i).toString());
        }
        assertEquals(expectedDisbursals, disbursalCount, "Checking for 2 disbursals");
        assertEquals(expectedTotalPrincipalDisbursed, totalPrincipalDisbursed, "Checking Principal Disburse is 3");

        final BigDecimal principalDue = BigDecimal.valueOf(Double.parseDouble(loanSchedule.get(2).get("principalDue").toString()));
        assertEquals(expectedPrincipalDue, principalDue, "Checking Principal Due is 3");
        final BigDecimal principalPaid = BigDecimal.valueOf(Double.parseDouble(loanSchedule.get(2).get("principalPaid").toString()));
        assertEquals(expectedPrincipalPaid, principalPaid, "Checking Principal Paid is 1");
        final BigDecimal principalOutstanding = BigDecimal
                .valueOf(Double.parseDouble(loanSchedule.get(2).get("principalOutstanding").toString()));
        assertEquals(expectedPrincipalOutstanding, principalOutstanding, "Checking Principal Due is 2");

        final BigDecimal principalLoanBalanceOutstanding = BigDecimal
                .valueOf(Double.parseDouble(loanSchedule.get(2).get("principalLoanBalanceOutstanding").toString()));
        assertEquals(expectedPrincipalLoanBalanceOutstanding, principalLoanBalanceOutstanding,
                "Checking Principal Loan Balance Outstanding is zero");

    }

}
