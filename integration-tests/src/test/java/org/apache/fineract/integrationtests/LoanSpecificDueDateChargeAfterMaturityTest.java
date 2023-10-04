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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanSpecificDueDateChargeAfterMaturityTest {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    private static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private static final String DEPOSIT_AMOUNT = "7000";
    private static final Logger LOG = LoggerFactory.getLogger(LoanSpecificDueDateChargeAfterMaturityTest.class);
    private static final String DATE_OF_JOINING = "01 January 2011";
    private static final Float LP_PRINCIPAL = 10000.0f;
    private static final String LP_REPAYMENTS = "1";
    private static final String LP_REPAYMENT_PERIOD = "1";
    private static final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "03 March 2011";
    private static final String LOAN_TERM_FREQUENCY = "1";
    private static final String INDIVIDUAL_LOAN = "individual";
    private static final BusinessDateHelper BUSINESS_DATE_HELPER = new BusinessDateHelper();
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern(DATETIME_PATTERN).toFormatter();
    private static final ChargesHelper CHARGES_HELPER = new ChargesHelper();
    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;

    public static Integer createSavingsProduct(final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @BeforeAll
    public static void setupCommon() {
        Utils.initializeRESTAssured();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(requestSpec, responseSpec);
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();
        commonLoanProductId = createLoanProduct("500", "15", "4", assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
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
        final String loanDisbursementDate = DATE_FORMATTER.format(targetDate);

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
        final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
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
        final String feeCharge1AddedDate = DATE_FORMATTER.format(targetDate);
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
        final String penaltyCharge2AddedDate = DATE_FORMATTER.format(targetDate);
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
        final String feeCharge2AddedDate = DATE_FORMATTER.format(targetDate);
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
        final String loanDisbursementDate = DATE_FORMATTER.format(targetDate);

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
        String repaymentDateStr = DATE_FORMATTER.format(targetDate);
        loanTransactionHelper.makeRepayment(repaymentDateStr, 10000.0f, loanID);

        loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        targetDate = LocalDate.of(2011, 4, 13);
        final String penaltyCharge1AddedDate = DATE_FORMATTER.format(targetDate);
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
        String runOnDateStr = DATE_FORMATTER.format(targetDate);
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
        String penaltyCharge2AddedDate = DATE_FORMATTER.format(targetDate);
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
        repaymentDateStr = DATE_FORMATTER.format(targetDate);
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

    @Test
    public void addChargeAfterLoanMaturity() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("01 September 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            PostChargesResponse penaltyCharge = CHARGES_HELPER.createCharges(new PostChargesRequest().penalty(true).amount(10.0)
                    .chargeCalculationType(ChargeCalculationType.FLAT.getValue())
                    .chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue()).chargePaymentMode(ChargePaymentMode.REGULAR.getValue())
                    .currencyCode("USD").name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5))
                    .chargeAppliesTo(1).locale("en").active(true));

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId, 1000L, 30, 30, 1, 0,
                    "01 September 2023", "01 September 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 September 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 0.0, 1000.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 0, 1000.0, 0.0, 0.0, 1000.0);
            assertTrue(loanDetails.getStatus().getActive());

            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("01 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            PostLoansLoanIdTransactionsResponse repaymentTransaction = loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("01 October 2023").locale("en")
                            .transactionAmount(1000.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            BUSINESS_DATE_HELPER.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("04 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("04 October 2023").locale("en").transactionAmount(1000.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, 1000.0);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 1000.0, 0.0, 1000.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.reverseLoanTransaction(loanResponse.getLoanId(), repaymentTransaction.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("04 October 2023")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 1000.0);
            validateLoanTransaction(loanDetails, 2, 1000.0, 1000.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            PostLoansLoanIdChargesResponse penaltyLoanChargeResult = loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                            .amount(10.0).dueDate("04 October 2023"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 10.0, 1000.0, 0.0, 1000.0, null);
            validateRepaymentPeriod(loanDetails, 1, 1000.0, 1000.0, 0.0, 0.0, 1000.0);
            validateRepaymentPeriod(loanDetails, 2, 0.0, 0.0, 0.0, 10.0, 0.0, 10.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
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

    private Integer createLoanProductWithPeriodicAccrualAccountingNoInterest(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment()
                .withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final Long principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final int interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanTransactionHelper.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(
                        LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                .locale("en").submittedOnDate(submittedOnDate).amortizationType(1).interestRatePerPeriod(interestRate)
                .interestCalculationPeriodType(1).interestType(0).repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery)
                .repaymentFrequencyType(0).numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency)
                .loanTermFrequencyType(0).principal(BigDecimal.valueOf(principal)).loanType("individual"));
    }

    private static void validateLoanTransaction(GetLoansLoanIdResponse loanDetails, int index, double transactionAmount,
            double principalPortion, double overPaidPortion, double loanBalance) {
        assertEquals(transactionAmount, loanDetails.getTransactions().get(index).getAmount());
        assertEquals(principalPortion, loanDetails.getTransactions().get(index).getPrincipalPortion());
        assertEquals(overPaidPortion, loanDetails.getTransactions().get(index).getOverpaymentPortion());
        assertEquals(loanBalance, loanDetails.getTransactions().get(index).getOutstandingLoanBalance());
    }

    private static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, int index, double principalDue, double principalPaid,
            double principalOutstanding, double paidInAdvance, double paidLate) {
        assertEquals(principalDue, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalDue());
        assertEquals(principalPaid, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalPaid());
        assertEquals(principalOutstanding, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalOutstanding());
        assertEquals(paidInAdvance, loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidInAdvanceForPeriod());
        assertEquals(paidLate, loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidLateForPeriod());
    }

    private static void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, int index, double principalDue, double principalPaid,
            double principalOutstanding, double penaltyDue, double penaltyPaid, double penaltyOutstanding, double paidInAdvance,
            double paidLate) {
        assertEquals(principalDue, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalDue());
        assertEquals(principalPaid, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalPaid());
        assertEquals(principalOutstanding, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPrincipalOutstanding());
        assertEquals(penaltyDue, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesDue());
        assertEquals(penaltyPaid, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesPaid());
        assertEquals(penaltyOutstanding, loanDetails.getRepaymentSchedule().getPeriods().get(index).getPenaltyChargesOutstanding());
        assertEquals(paidInAdvance, loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidInAdvanceForPeriod());
        assertEquals(paidLate, loanDetails.getRepaymentSchedule().getPeriods().get(index).getTotalPaidLateForPeriod());
    }

    private static void validateLoanSummaryBalances(GetLoansLoanIdResponse loanDetails, Double totalOutstanding, Double totalRepayment,
            Double principalOutstanding, Double principalPaid, Double totalOverpaid) {
        assertEquals(totalOutstanding, loanDetails.getSummary().getTotalOutstanding());
        assertEquals(totalRepayment, loanDetails.getSummary().getTotalRepayment());
        assertEquals(principalOutstanding, loanDetails.getSummary().getPrincipalOutstanding());
        assertEquals(principalPaid, loanDetails.getSummary().getPrincipalPaid());
        assertEquals(totalOverpaid, loanDetails.getTotalOverpaid());
    }
}
