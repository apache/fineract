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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DueDateRespectiveLoanRepaymentScheduleTest {

    private static final Logger LOG = LoggerFactory.getLogger(DueDateRespectiveLoanRepaymentScheduleTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private BusinessDateHelper businessDateHelper;
    private LoanTransactionHelper loanTransactionHelper;

    private AccountHelper accountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.businessDateHelper = new BusinessDateHelper();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    // Scenario1:
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4 Adding a partial repayment
    // 4.1 Paying only principal portion
    // 4.2 Adding a partial repayment
    // 4.3 Paying only charge portion
    @Test
    public void scenario1() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.01").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", true));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "1", "1", "0",
                    LoanProductTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "1", "1", "1", "0",
                    LoanApplicationTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("10 January 2023", Float.parseFloat("500.00"), loanID)
                    .get("resourceId");
            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "20 January 2023", "50"));
            Integer secondRepaymentId = (Integer) loanTransactionHelper.makeRepayment("17 January 2023", Float.parseFloat("450.00"), loanID)
                    .get("resourceId");

            Integer thirdRepaymentId = (Integer) loanTransactionHelper.makeRepayment("21 January 2023", Float.parseFloat("50.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(50.0, response.getSummary().getTotalOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(950.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, response.getTransactions().get(1).getAmount());
            assertEquals(500.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(500.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(secondRepaymentId, response.getTransactions().get(2).getId().intValue());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(450.0, response.getTransactions().get(2).getAmount());
            assertEquals(450.0, response.getTransactions().get(2).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(50.0, response.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(thirdRepaymentId, response.getTransactions().get(3).getId().intValue());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(50.0, response.getTransactions().get(3).getAmount());
            assertEquals(0.0, response.getTransactions().get(3).getPrincipalPortion());
            assertEquals(50.0, response.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(3).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(50.0, response.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario2:
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4. Adding a partial repayment
    // 4.1 Paying only principal portion
    // 5. Adding a charge
    // 5.1 No any reverse-replay
    // 6. Adding a partial repayment
    // 6.1 Paying the 1st charge
    // 6.2 Paying secondly the in advance principal
    // 6.3 Not paying the 2nd charge (id: #5)
    @Test
    public void scenario2() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.01").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", true));

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "1", "1", "0",
                    LoanProductTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "1", "1", "1", "0",
                    LoanApplicationTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("10 January 2023", Float.parseFloat("500.00"), loanID)
                    .get("resourceId");
            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "20 January 2023", "50"));
            Integer secondRepaymentId = (Integer) loanTransactionHelper.makeRepayment("17 January 2023", Float.parseFloat("100.00"), loanID)
                    .get("resourceId");

            Integer secondChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "23 January 2023", "10"));

            Integer thirdRepaymentId = (Integer) loanTransactionHelper.makeRepayment("20 January 2023", Float.parseFloat("100.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(360.0, response.getSummary().getTotalOutstanding());
            assertEquals(360.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(650.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(350.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, response.getTransactions().get(1).getAmount());
            assertEquals(500.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(500.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(secondRepaymentId, response.getTransactions().get(2).getId().intValue());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(100.0, response.getTransactions().get(2).getAmount());
            assertEquals(100.0, response.getTransactions().get(2).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(400.0, response.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(thirdRepaymentId, response.getTransactions().get(3).getId().intValue());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(100.0, response.getTransactions().get(3).getAmount());
            assertEquals(50.0, response.getTransactions().get(3).getPrincipalPortion());
            assertEquals(50.0, response.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(3).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(350.0, response.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario3:
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4. Adding a full repayment
    // 4.1 Paying first the in advanced principal portion, and after the in advanced charges
    @Test
    public void scenario3() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.01").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "50", false));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "1", "1", "0",
                    LoanProductTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "1", "1", "1", "0",
                    LoanApplicationTestBuilder.DUE_DATE_RESPECTIVE_STRATEGY, "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("10 January 2023", Float.parseFloat("500.00"), loanID)
                    .get("resourceId");
            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "20 January 2023", "50"));
            Integer secondRepaymentId = (Integer) loanTransactionHelper.makeRepayment("17 January 2023", Float.parseFloat("550.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(0.0, response.getSummary().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getClosedObligationsMet());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, response.getTransactions().get(1).getAmount());
            assertEquals(500.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(500.0, response.getTransactions().get(1).getOutstandingLoanBalance());

            int repaymentOrderNo;
            int accrualOrderNo;

            if (response.getTransactions().get(2).getType().getAccrual()) {
                accrualOrderNo = 2;
                repaymentOrderNo = 3;
            } else {
                accrualOrderNo = 3;
                repaymentOrderNo = 2;
            }

            assertNull(response.getTransactions().get(accrualOrderNo).getReversedOnDate());
            assertTrue(response.getTransactions().get(accrualOrderNo).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(accrualOrderNo).getType().getAccrual());
            assertEquals(50.0, response.getTransactions().get(accrualOrderNo).getAmount());
            assertEquals(0.0, response.getTransactions().get(accrualOrderNo).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(accrualOrderNo).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(accrualOrderNo).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(accrualOrderNo).getInterestPortion());
            assertEquals(50.0, response.getTransactions().get(accrualOrderNo).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(accrualOrderNo).getOutstandingLoanBalance());
            assertEquals(firstChargeId,
                    response.getTransactions().get(accrualOrderNo).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(accrualOrderNo).getLoanChargePaidByList().size());

            assertEquals(secondRepaymentId, response.getTransactions().get(repaymentOrderNo).getId().intValue());
            assertNull(response.getTransactions().get(repaymentOrderNo).getReversedOnDate());
            assertTrue(response.getTransactions().get(repaymentOrderNo).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(repaymentOrderNo).getType().getRepayment());
            assertEquals(550.0, response.getTransactions().get(repaymentOrderNo).getAmount());
            assertEquals(500.0, response.getTransactions().get(repaymentOrderNo).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(repaymentOrderNo).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(repaymentOrderNo).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(repaymentOrderNo).getInterestPortion());
            assertEquals(50.0, response.getTransactions().get(repaymentOrderNo).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(repaymentOrderNo).getOutstandingLoanBalance());
            assertEquals(firstChargeId,
                    response.getTransactions().get(repaymentOrderNo).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(repaymentOrderNo).getLoanChargePaidByList().size());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String principal,
            final String loanTermFrequency, final String repaymentAfterEvery, final String numberOfRepayments, final String interestRate,
            final String repaymentStrategy, final String expectedDisbursementDate, final String submittedOnDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principal)
                .withLoanTermFrequency(loanTermFrequency).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter(repaymentAfterEvery).withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments().withRepaymentStrategy(repaymentStrategy)
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(expectedDisbursementDate)
                .withSubmittedOnDate(submittedOnDate).withLoanType("individual").build(clientID.toString(), loanProductID.toString(), null);
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer createLoanProductWithNoAccountingNoInterest(final String principal, final String repaymentAfterEvery,
            final String numberOfRepayments, final String interestRate, final String repaymentStrategy, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(principal).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(interestRate).withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(repaymentStrategy)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
