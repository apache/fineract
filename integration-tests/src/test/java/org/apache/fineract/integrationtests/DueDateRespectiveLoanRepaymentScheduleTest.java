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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class DueDateRespectiveLoanRepaymentScheduleTest {

    private static final Logger LOG = LoggerFactory.getLogger(DueDateRespectiveLoanRepaymentScheduleTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private BusinessDateHelper businessDateHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private InlineLoanCOBHelper inlineLoanCOBHelper;
    private AccountHelper accountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);
        this.businessDateHelper = new BusinessDateHelper();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
    }

    // Scenario1:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
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
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

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
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
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
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

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
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a partial repayment
    // 3. Adding a charge
    // 3.1 No reverse-replay
    // 4. Adding a full repayment
    // 4.1 Paying first the in advance principal portion, and after the in advance charges
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
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

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

    // Scenario4:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan with 3 installments
    // 2. Adding a charge but not due
    // 2. Adding a repayment which fully pays 1st installment and partially the next
    // 4.1 Paying first the in advance principal portion of #1 installment, and after the in advance charges of #1
    // installment and in advance principal in #2 installment
    @Test
    public void scenario4() {
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
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "3", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "90", "30", "3", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "20 January 2023", "50"));

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("10 January 2023", Float.parseFloat("500.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(550.0, response.getSummary().getTotalOutstanding());
            assertEquals(550.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(116.67, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(216.66, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, response.getTransactions().get(1).getAmount());
            assertEquals(450.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(50.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(550.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario5:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan with 3 installments
    // 2. Adding a charge but not due
    // 2. Adding a repayment which fully pays 1st installment and partially the next
    // 4.1 Paying first the in advance principal portion of #1 installment, and after the in advance charges of #1
    // installment and in advance principal in #2 installment
    // 5 Overpay the loan
    @Test
    public void scenario5() {
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
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "3", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "90", "30", "3", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "20 January 2023", "50"));

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("10 January 2023", Float.parseFloat("500.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(550.0, response.getSummary().getTotalOutstanding());
            assertEquals(550.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(116.67, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(216.66, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(500.0, response.getTransactions().get(1).getAmount());
            assertEquals(450.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(50.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(550.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Integer secondRepaymentId = (Integer) loanTransactionHelper.makeRepayment("17 January 2023", Float.parseFloat("650.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);

            int repaymentOrderNo;
            int accrualOrderNo;

            assertEquals(0.0, response.getSummary().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(50.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(333.33, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getFeeChargesOutstanding());
            assertEquals(333.34, response.getRepaymentSchedule().getPeriods().get(3).getPrincipalDue());
            assertEquals(333.34, response.getRepaymentSchedule().getPeriods().get(3).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(3).getPrincipalOutstanding());
            assertEquals(100.0, response.getTotalOverpaid());
            assertTrue(response.getStatus().getOverpaid());

            int secondRepaymentIndex;
            // The repayment and accrual order is not consistent
            if (response.getTransactions().get(2).getType().getRepayment()) {
                secondRepaymentIndex = 2;
            } else {
                secondRepaymentIndex = 3;
            }

            assertEquals(secondRepaymentId, response.getTransactions().get(secondRepaymentIndex).getId().intValue());
            assertNull(response.getTransactions().get(secondRepaymentIndex).getReversedOnDate());
            assertTrue(response.getTransactions().get(secondRepaymentIndex).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(secondRepaymentIndex).getType().getRepayment());
            assertEquals(650.0, response.getTransactions().get(secondRepaymentIndex).getAmount());
            assertEquals(550.0, response.getTransactions().get(secondRepaymentIndex).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(secondRepaymentIndex).getPenaltyChargesPortion());
            assertEquals(100.0, response.getTransactions().get(secondRepaymentIndex).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(secondRepaymentIndex).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(secondRepaymentIndex).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(secondRepaymentIndex).getOutstandingLoanBalance());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario6:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Adding a snooze fee
    // 3. Do partial repayment
    // 3.1 Repay principal fully
    // 3.2 Repay fee partially
    @Test
    public void scenario6() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", false));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "25 January 2023", "20"));
            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("01 March 2023", Float.parseFloat("1010.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(10.0, response.getSummary().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario7:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Full repayment
    // 3. Reverse repayment
    // 3.1 Add NSF Fee
    // 4. Partial repayment
    @Test
    public void scenario7() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.01.28").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "15", true));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            Integer firstRepaymentId = (Integer) loanTransactionHelper.makeRepayment("25 January 2023", Float.parseFloat("1000.00"), loanID)
                    .get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(0.0, response.getSummary().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getClosedObligationsMet());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1000.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(0, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = loanTransactionHelper.reverseLoanTransaction((long) loanID,
                    (long) firstRepaymentId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("28 January 2023").transactionAmount(0.0).locale("en"));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.01.31").dateFormat("yyyy.MM.dd").locale("en"));

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(1000.0, response.getSummary().getTotalOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertEquals(LocalDate.of(2023, 1, 28), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1000.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(0, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "28 January 2023", "15"));
            Integer secondRepayment = (Integer) loanTransactionHelper.makeRepayment("31 January 2023", Float.parseFloat("1010.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(5.0, response.getSummary().getTotalOutstanding());
            assertEquals(5.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(995.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(5.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId().intValue());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(2).getAmount());
            assertEquals(995.0, response.getTransactions().get(2).getPrincipalPortion());
            assertEquals(15.0, response.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(5.0, response.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(2).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario8:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Partial repayment
    // 4. Reverse repayment
    // 4.1 NSF Fee added
    // 4. Partial repayment
    @Test
    public void scenario8() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.15").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", false));
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "15", true));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            final String requestJSON = new LoanRescheduleRequestTestBuilder().updateRescheduleFromDate("31 January 2023")
                    .updateAdjustedDueDate("01 March 2023").updateSubmittedOnDate("25 January 2023").updateGraceOnPrincipal(null)
                    .updateGraceOnInterest(null).updateExtraTerms(null).build(loanID.toString());
            final HashMap<String, String> map = new HashMap<>();
            map.put("locale", "en");
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("approvedOnDate", "25 January 2023");
            final String aproveRequestJSON = new Gson().toJson(map);

            Integer loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, aproveRequestJSON);

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "01 March 2023", "20"));
            Integer firstRepaymentId = (Integer) loanTransactionHelper
                    .makeRepayment("10 February 2023", Float.parseFloat("1010.00"), loanID).get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(10.0, response.getSummary().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = loanTransactionHelper.reverseLoanTransaction((long) loanID,
                    (long) firstRepaymentId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("15 February 2023").transactionAmount(0.0).locale("en"));

            Integer secondChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "15 February 2023", "15"));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(1035.0, response.getSummary().getTotalOutstanding());
            assertEquals(1035.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertEquals(LocalDate.of(2023, 2, 15), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Integer secondRepayment = (Integer) loanTransactionHelper.makeRepayment("01 March 2023", Float.parseFloat("15.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(1020.0, response.getSummary().getTotalOutstanding());
            assertEquals(1020.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId().intValue());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(15.0, response.getTransactions().get(2).getAmount());
            assertEquals(0.0, response.getTransactions().get(2).getPrincipalPortion());
            assertEquals(15.0, response.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(1000.0, response.getTransactions().get(2).getOutstandingLoanBalance());
            assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(2).getLoanChargePaidByList().size());

            Integer thirdRepayment = (Integer) loanTransactionHelper.makeRepayment("01 March 2023", Float.parseFloat("1000.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(20.0, response.getSummary().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(thirdRepayment, response.getTransactions().get(3).getId().intValue());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(1000.0, response.getTransactions().get(3).getAmount());
            assertEquals(1000.0, response.getTransactions().get(3).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(3).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(0, response.getTransactions().get(3).getLoanChargePaidByList().size());

            Integer forthRepayment = (Integer) loanTransactionHelper.makeRepayment("01 March 2023", Float.parseFloat("10.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(10.0, response.getSummary().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(forthRepayment, response.getTransactions().get(4).getId().intValue());
            assertNull(response.getTransactions().get(4).getReversedOnDate());
            assertTrue(response.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(4).getType().getRepayment());
            assertEquals(10.0, response.getTransactions().get(4).getAmount());
            assertEquals(0.0, response.getTransactions().get(4).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(4).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(4).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(4).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(4).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(4).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(4).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario9:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Partial repayment
    // 4. Reverse repayment
    // 4.1 NSF Fee added
    // 4. Partial repayment
    // 5. Reverse repayment
    // 5.1 NSF Fee added
    // 6. Partial repayment
    @Test
    public void scenario9() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.02.15").dateFormat("yyyy.MM.dd").locale("en"));

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", false));
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "15", true));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "01 January 2023", "01 January 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("01 January 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("01 January 2023", loanID, "1000");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            final String requestJSON = new LoanRescheduleRequestTestBuilder().updateRescheduleFromDate("31 January 2023")
                    .updateAdjustedDueDate("01 March 2023").updateSubmittedOnDate("25 January 2023").updateGraceOnPrincipal(null)
                    .updateGraceOnInterest(null).updateExtraTerms(null).build(loanID.toString());
            final HashMap<String, String> map = new HashMap<>();
            map.put("locale", "en");
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("approvedOnDate", "25 January 2023");
            final String aproveRequestJSON = new Gson().toJson(map);

            Integer loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, aproveRequestJSON);

            Integer firstChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "01 March 2023", "20"));
            Integer firstRepaymentId = (Integer) loanTransactionHelper
                    .makeRepayment("10 February 2023", Float.parseFloat("1010.00"), loanID).get("resourceId");

            GetLoansLoanIdResponse response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(10.0, response.getSummary().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertNull(response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            PostLoansLoanIdTransactionsResponse reverseRepayment = loanTransactionHelper.reverseLoanTransaction((long) loanID,
                    (long) firstRepaymentId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("15 February 2023").transactionAmount(0.0).locale("en"));

            Integer secondChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "15 February 2023", "15"));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.01").dateFormat("yyyy.MM.dd").locale("en"));

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(1035.0, response.getSummary().getTotalOutstanding());
            assertEquals(1035.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(firstRepaymentId, response.getTransactions().get(1).getId().intValue());
            assertEquals(LocalDate.of(2023, 2, 15), response.getTransactions().get(1).getReversedOnDate());
            assertTrue(response.getTransactions().get(1).getManuallyReversed());
            assertTrue(response.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(1).getType().getRepayment());
            assertEquals(1010.0, response.getTransactions().get(1).getAmount());
            assertEquals(1000.0, response.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(1).getInterestPortion());
            assertEquals(10.0, response.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(1).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(1).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(1).getLoanChargePaidByList().size());

            Integer secondRepayment = (Integer) loanTransactionHelper.makeRepayment("01 March 2023", Float.parseFloat("1030.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(5.0, response.getSummary().getTotalOutstanding());
            assertEquals(5.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(5.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(secondRepayment, response.getTransactions().get(2).getId().intValue());
            assertNull(response.getTransactions().get(2).getReversedOnDate());
            assertTrue(response.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(2).getType().getRepayment());
            assertEquals(1030.0, response.getTransactions().get(2).getAmount());
            assertEquals(1000.0, response.getTransactions().get(2).getPrincipalPortion());
            assertEquals(15.0, response.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(2).getInterestPortion());
            assertEquals(15.0, response.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(2).getOutstandingLoanBalance());
            if (secondChargeId.equals(response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId().intValue())) {
                assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId().intValue());
                assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(1).getChargeId().intValue());
            } else {
                assertEquals(secondChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(1).getChargeId().intValue());
                assertEquals(firstChargeId, response.getTransactions().get(2).getLoanChargePaidByList().get(0).getChargeId().intValue());
            }
            assertEquals(2, response.getTransactions().get(2).getLoanChargePaidByList().size());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.07").dateFormat("yyyy.MM.dd").locale("en"));

            PostLoansLoanIdTransactionsResponse secondReverseRepayment = loanTransactionHelper.reverseLoanTransaction((long) loanID,
                    (long) secondRepayment, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("07 March 2023").transactionAmount(0.0).locale("en"));

            Integer thirdChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "07 March 2023", "15"));

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.03.08").dateFormat("yyyy.MM.dd").locale("en"));

            Integer thirdRepayment = (Integer) loanTransactionHelper.makeRepayment("08 March 2023", Float.parseFloat("15.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(1035.0, response.getSummary().getTotalOutstanding());
            assertEquals(1035.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());

            assertEquals(thirdRepayment, response.getTransactions().get(3).getId().intValue());
            assertNull(response.getTransactions().get(3).getReversedOnDate());
            assertTrue(response.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(3).getType().getRepayment());
            assertEquals(15.0, response.getTransactions().get(3).getAmount());
            assertEquals(0.0, response.getTransactions().get(3).getPrincipalPortion());
            assertEquals(15.0, response.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(3).getInterestPortion());
            assertEquals(0.0, response.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(1000.0, response.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(secondChargeId, response.getTransactions().get(3).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(3).getLoanChargePaidByList().size());

            Integer forthRepayment = (Integer) loanTransactionHelper.makeRepayment("08 March 2023", Float.parseFloat("1015.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(20.0, response.getSummary().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(5.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());

            assertEquals(forthRepayment, response.getTransactions().get(4).getId().intValue());
            assertNull(response.getTransactions().get(4).getReversedOnDate());
            assertTrue(response.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(4).getType().getRepayment());
            assertEquals(1015.0, response.getTransactions().get(4).getAmount());
            assertEquals(1000.0, response.getTransactions().get(4).getPrincipalPortion());
            assertEquals(0.0, response.getTransactions().get(4).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(4).getInterestPortion());
            assertEquals(15.0, response.getTransactions().get(4).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(4).getOutstandingLoanBalance());
            assertEquals(firstChargeId, response.getTransactions().get(4).getLoanChargePaidByList().get(0).getChargeId().intValue());
            assertEquals(1, response.getTransactions().get(4).getLoanChargePaidByList().size());

            Integer fifthRepayment = (Integer) loanTransactionHelper.makeRepayment("08 March 2023", Float.parseFloat("10.00"), loanID)
                    .get("resourceId");

            response = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(10.0, response.getSummary().getTotalOutstanding());
            assertEquals(10.0, response.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(20.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(1000.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(response.getStatus().getActive());

            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getFeeChargesOutstanding());
            assertEquals(15.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesDue());
            assertEquals(5.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesPaid());
            assertEquals(10.0, response.getRepaymentSchedule().getPeriods().get(2).getPenaltyChargesOutstanding());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalDue());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalPaid());
            assertEquals(0.0, response.getRepaymentSchedule().getPeriods().get(2).getPrincipalOutstanding());

            assertEquals(fifthRepayment, response.getTransactions().get(5).getId().intValue());
            assertNull(response.getTransactions().get(5).getReversedOnDate());
            assertTrue(response.getTransactions().get(5).getTransactionRelations().isEmpty());
            assertTrue(response.getTransactions().get(5).getType().getRepayment());
            assertEquals(10.0, response.getTransactions().get(5).getAmount());
            assertEquals(0.0, response.getTransactions().get(5).getPrincipalPortion());
            assertEquals(5.0, response.getTransactions().get(5).getPenaltyChargesPortion());
            assertEquals(0.0, response.getTransactions().get(5).getOverpaymentPortion());
            assertEquals(0.0, response.getTransactions().get(5).getInterestPortion());
            assertEquals(5.0, response.getTransactions().get(5).getFeeChargesPortion());
            assertEquals(0.0, response.getTransactions().get(5).getOutstandingLoanBalance());
            if (firstChargeId.equals(response.getTransactions().get(5).getLoanChargePaidByList().get(0).getChargeId().intValue())) {
                assertEquals(thirdChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(1).getChargeId().intValue());
            } else {
                assertEquals(firstChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(1).getChargeId().intValue());
                assertEquals(thirdChargeId, response.getTransactions().get(5).getLoanChargePaidByList().get(0).getChargeId().intValue());
            }
            assertEquals(2, response.getTransactions().get(5).getLoanChargePaidByList().size());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    // Scenario10:
    // DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Merchant issued refund (partial)
    // 4. Charge adjustment (same day)
    // 5. Merchant issued refund (rest)
    @Test
    public void scenario10() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.05.14").dateFormat("yyyy.MM.dd").locale("en"));
            GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(requestSpec, responseSpec, "submitted-date");

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "3.65", false));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_INTEREST_PRINCIPAL_FEE_IN_ADVANCE_PENALTY_INTEREST_PRINCIPAL_FEE_STRATEGY,
                    "14 May 2023", "14 May 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("14 May 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("14 May 2023", loanID, "127.95");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.11").dateFormat("yyyy.MM.dd").locale("en"));

            final String requestJSON = new LoanRescheduleRequestTestBuilder().updateRescheduleFromDate("13 June 2023")
                    .updateAdjustedDueDate("13 July 2023").updateSubmittedOnDate("11 June 2023").updateGraceOnPrincipal(null)
                    .updateGraceOnInterest(null).updateExtraTerms(null).build(loanID.toString());

            final HashMap<String, String> map = new HashMap<>();
            map.put("locale", "en");
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("approvedOnDate", "11 June 2023");
            final String aproveRequestJSON = new Gson().toJson(map);

            Integer loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, aproveRequestJSON);
            Integer penalty1LoanChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "13 July 2023", "3.65"));
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.12").dateFormat("yyyy.MM.dd").locale("en"));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(131.6, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(131.6, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(0).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(0).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertEquals(127.95, loanDetails.getTransactions().get(0).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getInterestPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getFeeChargesPortion());
            assertEquals(127.95, loanDetails.getTransactions().get(0).getOutstandingLoanBalance());

            assertNull(loanDetails.getTransactions().get(1).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertEquals(3.65, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getInterestPortion());
            assertEquals(3.65, loanDetails.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getOutstandingLoanBalance());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.17").dateFormat("yyyy.MM.dd").locale("en"));
            PostLoansLoanIdTransactionsResponse merchantIssuedRefund1 = loanTransactionHelper.makeMerchantIssuedRefund(Long.valueOf(loanID),
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(6.6, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(6.6, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(125.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(2).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(2).getType().getMerchantIssuedRefund());
            assertEquals(125.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(125.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(2.95, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());

            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = this.loanTransactionHelper.chargeAdjustment((long) loanID,
                    (long) penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(3.65));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(2.95, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.70, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(3).getReversedOnDate());
            assertFalse(loanDetails.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertEquals((long) penalty1LoanChargeId,
                    loanDetails.getTransactions().get(3).getTransactionRelations().iterator().next().getToLoanCharge());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeAdjustment());
            assertEquals(3.65, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(2.95, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getInterestPortion());
            assertEquals(0.7, loanDetails.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund2 = loanTransactionHelper.makeMerchantIssuedRefund(Long.valueOf(loanID),
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(2.95));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(0.0, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertNull(loanDetails.getTransactions().get(4).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(4).getType().getMerchantIssuedRefund());
            assertEquals(2.95, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getInterestPortion());
            assertEquals(2.95, loanDetails.getTransactions().get(4).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOutstandingLoanBalance());

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(requestSpec, responseSpec, "due-date");
        }
    }

    // Scenario11:
    // DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY
    // 1. Disburse the loan
    // 2. Snooze fee
    // 3. Merchant issued refund (partial)
    // 4. Charge adjustment (same day)
    // 5. Merchant issued refund (rest)
    @Test
    public void scenario11() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.05.14").dateFormat("yyyy.MM.dd").locale("en"));
            GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(requestSpec, responseSpec, "submitted-date");

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            Integer fee = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "3.65", false));
            final Integer loanProductID = createLoanProductWithNoAccountingNoInterest("1000", "30", "1", "0",
                    LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2023");

            final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1000", "30", "30", "1", "0",
                    LoanApplicationTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY,
                    "14 May 2023", "14 May 2023");

            HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.approveLoan("14 May 2023", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

            loanStatusHashMap = loanTransactionHelper.disburseLoanWithTransactionAmount("14 May 2023", loanID, "127.95");
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.11").dateFormat("yyyy.MM.dd").locale("en"));

            final String requestJSON = new LoanRescheduleRequestTestBuilder().updateRescheduleFromDate("13 June 2023")
                    .updateAdjustedDueDate("13 July 2023").updateSubmittedOnDate("11 June 2023").updateGraceOnPrincipal(null)
                    .updateGraceOnInterest(null).updateExtraTerms(null).build(loanID.toString());

            final HashMap<String, String> map = new HashMap<>();
            map.put("locale", "en");
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("approvedOnDate", "11 June 2023");
            final String aproveRequestJSON = new Gson().toJson(map);

            Integer loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, aproveRequestJSON);
            Integer penalty1LoanChargeId = loanTransactionHelper.addChargesForLoan(loanID,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(fee), "13 July 2023", "3.65"));
            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.12").dateFormat("yyyy.MM.dd").locale("en"));
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanID.longValue()));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(131.6, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(131.6, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(0).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(0).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertEquals(127.95, loanDetails.getTransactions().get(0).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getInterestPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(0).getFeeChargesPortion());
            assertEquals(127.95, loanDetails.getTransactions().get(0).getOutstandingLoanBalance());

            assertNull(loanDetails.getTransactions().get(1).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(1).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(1).getType().getAccrual());
            assertEquals(3.65, loanDetails.getTransactions().get(1).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getInterestPortion());
            assertEquals(3.65, loanDetails.getTransactions().get(1).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(1).getOutstandingLoanBalance());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.06.17").dateFormat("yyyy.MM.dd").locale("en"));

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund1 = loanTransactionHelper.makeMerchantIssuedRefund(Long.valueOf(loanID),
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(6.6, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(6.6, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(125.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(2).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(2).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(2).getType().getMerchantIssuedRefund());
            assertEquals(125.0, loanDetails.getTransactions().get(2).getAmount());
            assertEquals(125.0, loanDetails.getTransactions().get(2).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getInterestPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(2).getFeeChargesPortion());
            assertEquals(2.95, loanDetails.getTransactions().get(2).getOutstandingLoanBalance());

            PostLoansLoanIdChargesChargeIdResponse chargeAdjustmentResponse = this.loanTransactionHelper.chargeAdjustment((long) loanID,
                    (long) penalty1LoanChargeId, new PostLoansLoanIdChargesChargeIdRequest().amount(3.65));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(2.95, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(0.70, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(2.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getActive());

            assertNull(loanDetails.getTransactions().get(3).getReversedOnDate());
            assertFalse(loanDetails.getTransactions().get(3).getTransactionRelations().isEmpty());
            assertEquals((long) penalty1LoanChargeId,
                    loanDetails.getTransactions().get(3).getTransactionRelations().iterator().next().getToLoanCharge());
            assertTrue(loanDetails.getTransactions().get(3).getType().getChargeAdjustment());
            assertEquals(3.65, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(2.95, loanDetails.getTransactions().get(3).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getInterestPortion());
            assertEquals(0.7, loanDetails.getTransactions().get(3).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());

            PostLoansLoanIdTransactionsResponse merchantIssuedRefund2 = loanTransactionHelper.makeMerchantIssuedRefund(Long.valueOf(loanID),
                    new PostLoansLoanIdTransactionsRequest().locale("en").dateFormat("dd MMMM yyyy").transactionDate("17 June 2023")
                            .transactionAmount(2.95));

            loanDetails = loanTransactionHelper.getLoanDetails((long) loanID);
            assertEquals(0.0, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getTotalOutstanding());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesDue());
            assertEquals(3.65, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getFeeChargesOutstanding());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesDue());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPenaltyChargesOutstanding());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalDue());
            assertEquals(127.95, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalPaid());
            assertEquals(0.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPrincipalOutstanding());
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            assertNull(loanDetails.getTransactions().get(4).getReversedOnDate());
            assertTrue(loanDetails.getTransactions().get(4).getTransactionRelations().isEmpty());
            assertTrue(loanDetails.getTransactions().get(4).getType().getMerchantIssuedRefund());
            assertEquals(2.95, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getPrincipalPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getPenaltyChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOverpaymentPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getInterestPortion());
            assertEquals(2.95, loanDetails.getTransactions().get(4).getFeeChargesPortion());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOutstandingLoanBalance());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
            GlobalConfigurationHelper.updateChargeAccrualDateConfiguration(requestSpec, responseSpec, "due-date");
        }
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, final String principal,
            final String loanTermFrequency, final String repaymentAfterEvery, final String numberOfRepayments, final String interestRate,
            final String repaymentStrategy, final String expectedDisbursementDate, final String submittedOnDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principal)
                .withLoanTermFrequency(loanTermFrequency).withLoanTermFrequencyAsDays().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter(repaymentAfterEvery).withRepaymentFrequencyTypeAsDays().withInterestRatePerPeriod(interestRate)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments().withRepaymentStrategy(repaymentStrategy)
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(expectedDisbursementDate)
                .withSubmittedOnDate(submittedOnDate).withLoanType("individual").build(clientID.toString(), loanProductID.toString(), null);
        return loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer createLoanProductWithNoAccountingNoInterest(final String principal, final String repaymentAfterEvery,
            final String numberOfRepayments, final String interestRate, final String repaymentStrategy, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(principal).withRepaymentTypeAsDays()
                .withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withinterestRatePerPeriod(interestRate).withInterestRateFrequencyTypeAsMonths().withRepaymentStrategy(repaymentStrategy)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
