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
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked" })
public class LoanSpecificDueDateChargeAfterMaturityTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String DEPOSIT_AMOUNT = "7000";
    private static final Logger LOG = LoggerFactory.getLogger(LoanSpecificDueDateChargeAfterMaturityTest.class);
    private static final String DATE_OF_JOINING = "01 January 2011";
    private static final Float LP_PRINCIPAL = 10000.0f;
    private static final String LP_REPAYMENTS = "1";
    private static final String LP_REPAYMENT_PERIOD = "1";
    private static final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "03 March 2011";
    private static final String LOAN_TERM_FREQUENCY = "1";
    private static final String INDIVIDUAL_LOAN = "individual";
    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

    public static Integer createSavingsProduct(final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(requestSpec, responseSpec);
    }

    @Test
    public void onlyNonInterestBearingLoanCanAcceptChargeAfterMaturity() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingWithInterestRecalculationEnabled(assetAccount,
                incomeAccount, expenseAccount, overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "1");

        final float PENALTY_PORTION = 100.0f;

        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LocalDate targetDate = LocalDate.of(2011, 3, 4);
        final String loanDisbursementDate = dateFormatter.format(targetDate);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        targetDate = LocalDate.of(2011, 4, 5);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

        final String ADD_CHARGES_URL = "/fineract-provider/api/v1/loans/" + loanID + "/charges?" + Utils.TENANT_IDENTIFIER;
        ResponseSpecification response403Spec = new ResponseSpecBuilder().expectStatusCode(403).build();
        final HashMap response = Utils.performServerPost(requestSpec, response403Spec, ADD_CHARGES_URL,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), penaltyCharge1AddedDate,
                        String.valueOf(PENALTY_PORTION)),
                "");
        assertEquals("validation.msg.domain.rule.violation", response.get("userMessageGlobalisationCode"));

    }

    @Test
    public void checkPeriodicAccrualAccountingAPIFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "0");

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        final float NEXT_FEE_PORTION = 55.0f;
        final float NEXT_PENALTY_PORTION = 105.0f;

        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(FEE_PORTION), false));
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        Integer flatNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_FEE_PORTION), false));
        Integer flatSpecifiedDueDateNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_PENALTY_PORTION), true));

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LocalDate targetDate = LocalDate.of(2011, 3, 4);
        final String loanDisbursementDate = dateFormatter.format(targetDate);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(10000.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(10000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));
        targetDate = LocalDate.of(2011, 4, 5);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);
        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), penaltyCharge1AddedDate,
                        String.valueOf(PENALTY_PORTION)));

        this.loanTransactionHelper.noAccrualTransactionForRepayment(loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 5), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        String runOnDateStr = penaltyCharge1AddedDate;
        LocalDate runOnDate = targetDate;
        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOnDateStr);

        this.loanTransactionHelper.checkAccrualTransactionForRepayment(runOnDate, 0.0f, 0.0f, PENALTY_PORTION, loanID);

        targetDate = LocalDate.of(2011, 4, 6);
        final String feeCharge1AddedDate = dateFormatter.format(targetDate);
        Integer fee1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), feeCharge1AddedDate, String.valueOf(FEE_PORTION)));

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(FEE_PORTION, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION + FEE_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION + FEE_PORTION, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 6), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        targetDate = LocalDate.of(2011, 4, 7);
        final String penaltyCharge2AddedDate = dateFormatter.format(targetDate);
        Integer penalty2LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDateNext),
                        penaltyCharge2AddedDate, String.valueOf(NEXT_PENALTY_PORTION)));

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(FEE_PORTION, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 7), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        targetDate = LocalDate.of(2011, 4, 8);
        final String feeCharge2AddedDate = dateFormatter.format(targetDate);
        Integer fee2LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatNext), feeCharge2AddedDate, String.valueOf(NEXT_FEE_PORTION)));

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(FEE_PORTION + NEXT_FEE_PORTION, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(FEE_PORTION + NEXT_FEE_PORTION, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION + NEXT_PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION + NEXT_FEE_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION + FEE_PORTION + NEXT_PENALTY_PORTION + NEXT_FEE_PORTION,
                loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 8), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        runOnDateStr = penaltyCharge2AddedDate;
        runOnDate = LocalDate.of(2011, 4, 7);
        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOnDateStr);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(runOnDate, 0.0f, FEE_PORTION, NEXT_PENALTY_PORTION, loanID);
    }

    @Test
    public void reopenClosedLoan() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingNoInterest(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, "0");

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        final float NEXT_FEE_PORTION = 55.0f;
        final float NEXT_PENALTY_PORTION = 105.0f;

        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(FEE_PORTION), false));
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        Integer flatNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_FEE_PORTION), false));
        Integer flatSpecifiedDueDateNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_PENALTY_PORTION), true));

        HashMap<String, Object> loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LocalDate targetDate = LocalDate.of(2011, 3, 4);
        final String loanDisbursementDate = dateFormatter.format(targetDate);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(loanDisbursementDate, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(2, loanSchedule.size());
        assertEquals(0, loanSchedule.get(1).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("feeChargesOutstanding"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesDue"));
        assertEquals(0, loanSchedule.get(1).get("penaltyChargesOutstanding"));
        assertEquals(10000.0f, loanSchedule.get(1).get("totalDueForPeriod"));
        assertEquals(10000.0f, loanSchedule.get(1).get("totalOutstandingForPeriod"));

        targetDate = LocalDate.of(2011, 3, 10);
        String repaymentDateStr = dateFormatter.format(targetDate);
        loanTransactionHelper.makeRepayment(repaymentDateStr, 10000.0f, loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        targetDate = LocalDate.of(2011, 4, 13);
        final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);
        Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), penaltyCharge1AddedDate,
                        String.valueOf(PENALTY_PORTION)));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 13), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        targetDate = LocalDate.of(2011, 4, 14);
        String runOnDateStr = dateFormatter.format(targetDate);
        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOnDateStr);

        // Transaction date will be the due date of the instalment (in case of N+1 scenario)
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(LocalDate.of(2011, 4, 13), 0.0f, 0.0f, PENALTY_PORTION, loanID);

        loanTransactionHelper.waiveChargesForLoan(loanID, penalty1LoanChargeId,
                LoanTransactionHelper.getWaiveChargeJSON(String.valueOf(2)));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(0.0f, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesWaived"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(0.0f, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalWaivedForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 13), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        targetDate = LocalDate.of(2011, 4, 14);
        String penaltyCharge2AddedDate = dateFormatter.format(targetDate);
        Integer penalty2LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate), penaltyCharge2AddedDate,
                        String.valueOf(PENALTY_PORTION)));

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesWaived"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalWaivedForPeriod"));
        assertEquals(LocalDate.of(2011, 4, 14), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        targetDate = LocalDate.of(2011, 4, 15);
        repaymentDateStr = dateFormatter.format(targetDate);
        loanTransactionHelper.makeRepayment(repaymentDateStr, PENALTY_PORTION, loanID);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(0.0f, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesWaived"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesPaid"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(0.0f, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalWaivedForPeriod"));
        // Might need to change if refund should update the due date of N+1 instalment
        assertEquals(LocalDate.of(2011, 4, 14), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        loanTransactionHelper.loanChargeRefund(penalty2LoanChargeId, null, PENALTY_PORTION, null, loanID, "resourceId");

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        assertEquals(3, loanSchedule.size());
        assertEquals(0, loanSchedule.get(2).get("feeChargesDue"));
        assertEquals(0, loanSchedule.get(2).get("feeChargesOutstanding"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesDue"));
        assertEquals(0.0f, loanSchedule.get(2).get("penaltyChargesOutstanding"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesWaived"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("penaltyChargesPaid"));
        assertEquals(PENALTY_PORTION + PENALTY_PORTION, loanSchedule.get(2).get("totalDueForPeriod"));
        assertEquals(0.0f, loanSchedule.get(2).get("totalOutstandingForPeriod"));
        assertEquals(PENALTY_PORTION, loanSchedule.get(2).get("totalWaivedForPeriod"));
        // Might need to change if refund should update the due date of N+1 instalment
        assertEquals(LocalDate.of(2011, 4, 14), LocalDate.of((int) ((List) loanSchedule.get(2).get("dueDate")).get(0),
                (int) ((List) loanSchedule.get(2).get("dueDate")).get(1), (int) ((List) loanSchedule.get(2).get("dueDate")).get(2)));
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String interestRate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(LP_PRINCIPAL.toString())
                .withLoanTermFrequency(LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(LP_REPAYMENTS)
                .withRepaymentEveryAfter(LP_REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE)
                .withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE).withLoanType(INDIVIDUAL_LOAN)
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingWithInterestRecalculationEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("10000.0").withNumberOfRepayments("1")
                .withinterestRatePerPeriod("1").withInterestRateFrequencyTypeAsYear().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsDays()
                .withInterestRecalculationDetails(LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE,
                        LoanProductTestBuilder.RECALCULATION_STRATEGY_REDUCE_NUMBER_OF_INSTALLMENTS,
                        LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE)
                .withInterestRecalculationRestFrequencyDetails(LoanProductTestBuilder.RECALCULATION_FREQUENCY_TYPE_DAILY, "0", null, null)
                .withInterestRecalculationCompoundingFrequencyDetails(null, null, null, null).withMoratorium("0", "0")
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withAccountingRulePeriodicAccrual(accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer createLoanProductWithPeriodicAccrualAccountingNoInterest(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment()
                .withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

}
