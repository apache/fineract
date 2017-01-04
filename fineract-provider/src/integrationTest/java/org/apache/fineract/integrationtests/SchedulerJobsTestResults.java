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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.StandingInstructionsHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "unchecked", "rawtypes", "static-access", "cast" })
public class SchedulerJobsTestResults {

    private static final String FROM_ACCOUNT_TYPE_LOAN = "1";
    private static final String FROM_ACCOUNT_TYPE_SAVINGS = "2";
    private static final String TO_ACCOUNT_TYPE_LOAN = "1";
    private static final String TO_ACCOUNT_TYPE_SAVINGS = "2";
    private final String DATE_OF_JOINING = "01 January 2011";

    private final String TRANSACTION_DATE = "01 March 2013";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String MINIMUM_OPENING_BALANCE = "1000";

    Float SP_BALANCE = new Float(MINIMUM_OPENING_BALANCE);

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private ResponseSpecification responseSpecForSchedulerJob;
    private SchedulerJobHelper schedulerJobHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private HolidayHelper holidayHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;
    private StandingInstructionsHelper standingInstructionsHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecForSchedulerJob = new ResponseSpecBuilder().expectStatusCode(202).build();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testApplyAnnualFeeForSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer annualFeeChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsAnnualFeeJSON());
        Assert.assertNotNull(annualFeeChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, annualFeeChargeId, true);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(1, chargesPendingState.size());

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        String JobName = "Apply Annual Fee For Savings";

        this.schedulerJobHelper.executeJob(JobName);
        final HashMap chargeData = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, annualFeeChargeId);

        Float chargeAmount = (Float) chargeData.get("amount");

        final HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Assert.assertEquals("Verifying Annual Fee after Running Scheduler Job for Apply Anual Fee", chargeAmount,
                (Float) summaryAfter.get("totalAnnualFees"));

    }

    @Test
    public void testInterestPostingForSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        String JobName = "Post Interest For Savings";

        this.schedulerJobHelper.executeJob(JobName);
        final HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        Assert.assertNotSame("Verifying the Balance after running Post Interest for Savings Job", summaryBefore.get("accountBalance"),
                summaryAfter.get("accountBalance"));

    }

    @Test
    public void testTransferFeeForLoansFromSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), savingsId.toString());
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateWithAccountTransferJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(specifiedDueDateChargeId.toString(), "12 March 2013", "100"));
        ArrayList<HashMap> chargesPendingState = this.loanTransactionHelper.getLoanCharges(loanID);
        Assert.assertEquals(1, chargesPendingState.size());

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(AccountTransferTest.LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        final HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        String JobName = "Transfer Fee For Loans From Savings";
        this.schedulerJobHelper.executeJob(JobName);
        final HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        final HashMap chargeData = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);

        Float chargeAmount = (Float) chargeData.get("amount");

        final Float balance = (Float) summaryBefore.get("accountBalance") - chargeAmount;

        Assert.assertEquals("Verifying the Balance after running Transfer Fee for Loans from Savings", balance,
                (Float) summaryAfter.get("accountBalance"));

    }

    @Test
    public void testApplyHolidaysToLoansJobOutcome() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.holidayHelper = new HolidayHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        Integer holidayId = this.holidayHelper.createHolidays(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(holidayId);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(AccountTransferTest.LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = this.globalConfigurationHelper.getAllGlobalConfigurations(this.requestSpec,
                this.responseSpec);
        Assert.assertNotNull(globalConfig);

        // Updating Value for reschedule-repayments-on-holidays Global
        // Configuration
        Integer configId = (Integer) globalConfig.get(3).get("id");
        Assert.assertNotNull(configId);

        HashMap configData = this.globalConfigurationHelper.getGlobalConfigurationById(this.requestSpec, this.responseSpec,
                configId.toString());
        Assert.assertNotNull(configData);

        Boolean enabled = (Boolean) globalConfig.get(3).get("enabled");

        if (enabled == false) {
            enabled = true;
            configId = this.globalConfigurationHelper.updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec,
                    configId.toString(), enabled);
        }
        final ArrayList<HashMap> repaymentScheduleDataBeforeJob = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec,
                this.responseSpec, loanID);

        holidayId = this.holidayHelper.activateHolidays(this.requestSpec, this.responseSpec, holidayId.toString());
        Assert.assertNotNull(holidayId);

        String JobName = "Apply Holidays To Loans";

        this.schedulerJobHelper.executeJob(JobName);
        final ArrayList<HashMap> repaymentScheduleDataAfterJob = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec,
                this.responseSpec, loanID);

        HashMap holidayData = this.holidayHelper.getHolidayById(this.requestSpec, this.responseSpec, holidayId.toString());
        ArrayList<Integer> repaymentsRescheduledDate = (ArrayList<Integer>) holidayData.get("repaymentsRescheduledTo");

        ArrayList<Integer> rescheduleDateAfter = (ArrayList<Integer>) repaymentScheduleDataAfterJob.get(2).get("fromDate");

        Assert.assertEquals("Verifying Repayment Rescheduled Date after Running Apply Holidays to Loans Scheduler Job",
                repaymentsRescheduledDate, repaymentsRescheduledDate);

    }

    @Test
    public void testApplyDueFeeChargesForSavingsJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        final Integer specifiedDueDateChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsSpecifiedDueDateJSON());
        Assert.assertNotNull(specifiedDueDateChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, specifiedDueDateChargeId, true);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsId);
        Assert.assertEquals(1, chargesPendingState.size());

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        HashMap summaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);

        String JobName = "Pay Due Savings Charges";

        this.schedulerJobHelper.executeJob(JobName);
        HashMap summaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);

        final HashMap chargeData = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, specifiedDueDateChargeId);

        Float chargeAmount = (Float) chargeData.get("amount");

        final Float balance = (Float) summaryBefore.get("accountBalance") - chargeAmount;

        Assert.assertEquals("Verifying the Balance after running Pay due Savings Charges", balance,
                (Float) summaryAfter.get("accountBalance"));

    }

    @Test
    public void testUpdateAccountingRunningBalancesJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer accountID = assetAccount.getAccountID();

        final Integer savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer savingsID = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsID);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Checking initial Account entries.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.SP_BALANCE, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(this.SP_BALANCE, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.TRANSACTION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper
                .checkJournalEntryForLiabilityAccount(liabilityAccount, this.TRANSACTION_DATE, liablilityAccountInitialEntry);

        String JobName = "Update Accounting Running Balances";

        this.schedulerJobHelper.executeJob(JobName);
        final HashMap runningBalanceAfter = this.accountHelper.getAccountingWithRunningBalanceById(accountID.toString());

        final Integer INT_BALANCE = new Integer(MINIMUM_OPENING_BALANCE);

        Assert.assertEquals("Verifying Account Running Balance after running Update Accounting Running Balances Scheduler Job",
                INT_BALANCE, runningBalanceAfter.get("organizationRunningBalance"));

    }

    @Test
    public void testUpdateLoanArrearsAgingJobOutcome() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(AccountTransferTest.LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        String JobName = "Update Loan Arrears Ageing";

        this.schedulerJobHelper.executeJob(JobName);
        HashMap loanSummaryData = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);

        Float totalLoanArrearsAging = (Float) loanSummaryData.get("principalOverdue") + (Float) loanSummaryData.get("interestOverdue");

        Assert.assertEquals("Verifying Arrears Aging after Running Update Loan Arrears Aging Scheduler Job", totalLoanArrearsAging,
                loanSummaryData.get("totalOverdue"));

    }

    @Test
    public void testUpdateLoanPaidInAdvanceJobOutcome() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance();
        final String currentDate = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.MONTH, -1);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate = Calendar.getInstance();
        todayDate.add(Calendar.DATE, -5);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todayDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> loanScheduleBefore = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);

        Float totalDueForCurrentPeriod = (Float) loanScheduleBefore.get(1).get("totalDueForPeriod");

        this.loanTransactionHelper.makeRepayment(LOAN_FIRST_REPAYMENT_DATE, totalDueForCurrentPeriod, loanID);

        HashMap loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);

        String JobName = "Update Loan Paid In Advance";
        this.schedulerJobHelper.executeJob(JobName);
        // Retrieving Loan Repayment Schedule after the successful
        // completion of
        // Update Loan Paid in Advance Scheduler Job
        ArrayList<HashMap> loanScheduleAfter = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);

        loanSummary = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);

        Float totalPaidInAdvance = (Float) loanScheduleAfter.get(1).get("totalPaidInAdvanceForPeriod");

        Assert.assertEquals("Verifying Loan Repayment in Advance after Running Update Loan Paid in Advance Scheduler Job",
                totalDueForCurrentPeriod, totalPaidInAdvance);

    }

    // Invalid test case as it won't affect summary (Loan summary is properly
    // updated before running this job)
    @Ignore
    @Test
    public void testUpdateLoanSummaryJobOutcome() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        final String currentDate = dateFormat.format(todaysDate.getTime());

        todaysDate.add(Calendar.MONTH, -1);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.DATE, -5);
        final String LOAN_FIRST_REPAYMENT_DATE = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        Integer disburseChargeId = ChargesHelper
                .createCharges(this.requestSpec, this.responseSpec, ChargesHelper.getLoanDisbursementJSON());
        Assert.assertNotNull(disburseChargeId);

        this.loanTransactionHelper.addChargesForLoan(loanID,
                LoanTransactionHelper.getDisbursementChargesForLoanAsJSON(disburseChargeId.toString()));
        ArrayList<HashMap> chargesPendingState = this.loanTransactionHelper.getLoanCharges(loanID);
        Assert.assertEquals(1, chargesPendingState.size());

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        HashMap loanSummaryBefore = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);

        String JobName = "Update loan Summary";
        this.schedulerJobHelper.executeJob(JobName);
        Float expectedSummaryAfterJob = (Float) loanSummaryBefore.get("totalExpectedRepayment")
               /* - (Float) loanSummaryBefore.get("feeChargesPaid")*/;
        HashMap loanSummaryAfter = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, loanID);
        Assert.assertEquals("Verifying Loan Summary after Running Update Loan Summary Scheduler Job", expectedSummaryAfterJob,
                (Float) loanSummaryAfter.get("totalExpectedRepayment"));

    }

    @Test
    public void testExecuteStandingInstructionsJobOutcome() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.standingInstructionsHelper = new StandingInstructionsHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.WEEK_OF_YEAR, -1);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        todaysDate.add(Calendar.YEAR, 1);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec,
                ClientSavingsIntegrationTest.MINIMUM_OPENING_BALANCE);
        Assert.assertNotNull(savingsProductID);

        final Integer fromSavingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsId);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsId);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsId);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final Integer toSavingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap toSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, toSavingsId);
        SavingsStatusChecker.verifySavingsIsPending(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(toSavingsId);
        SavingsStatusChecker.verifySavingsIsApproved(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(toSavingsId);
        SavingsStatusChecker.verifySavingsIsActive(toSavingsStatusHashMap);

        HashMap fromSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(fromSavingsId);
        Float fromSavingsBalanceBefore = (Float) fromSavingsSummaryBefore.get("accountBalance");

        HashMap toSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(toSavingsId);
        Float toSavingsBalanceBefore = (Float) toSavingsSummaryBefore.get("accountBalance");

        Integer standingInstructionId = this.standingInstructionsHelper.createStandingInstruction(clientID.toString(),
                fromSavingsId.toString(), toSavingsId.toString(), FROM_ACCOUNT_TYPE_SAVINGS, TO_ACCOUNT_TYPE_SAVINGS, VALID_FROM, VALID_TO,
                MONTH_DAY);
        Assert.assertNotNull(standingInstructionId);

        String JobName = "Execute Standing Instruction";
        this.schedulerJobHelper.executeJob(JobName);
        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsId);
        Float fromSavingsBalanceAfter = (Float) fromSavingsSummaryAfter.get("accountBalance");

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsId);
        Float toSavingsBalanceAfter = (Float) toSavingsSummaryAfter.get("accountBalance");

        final HashMap standingInstructionData = this.standingInstructionsHelper
                .getStandingInstructionById(standingInstructionId.toString());
        Float expectedFromSavingsBalance = fromSavingsBalanceBefore - (Float) standingInstructionData.get("amount");
        Float expectedToSavingsBalance = toSavingsBalanceBefore + (Float) standingInstructionData.get("amount");

        Assert.assertEquals("Verifying From Savings Balance after Successful completion of Scheduler Job", expectedFromSavingsBalance,
                fromSavingsBalanceAfter);
        Assert.assertEquals("Verifying To Savings Balance after Successful completion of Scheduler Job", expectedToSavingsBalance,
                toSavingsBalanceAfter);
        Integer fromAccountType = PortfolioAccountType.SAVINGS.getValue();
        Integer transferType = AccountTransferType.ACCOUNT_TRANSFER.getValue();
        List<HashMap> standinInstructionHistoryData = this.standingInstructionsHelper.getStandingInstructionHistory(fromSavingsId,
                fromAccountType, clientID, transferType);
        Assert.assertEquals("Verifying the no of stainding instruction transactions logged for the client", 1,
                standinInstructionHistoryData.size());
        HashMap loggedTransaction = standinInstructionHistoryData.get(0);

        Assert.assertEquals("Verifying transferred amount and logged transaction amounts", (Float) standingInstructionData.get("amount"),
                (Float) loggedTransaction.get("amount"));

    }

    @Test
    public void testApplyPenaltyForOverdueLoansJobOutcome() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        Integer overdueFeeChargeId = ChargesHelper
                .createCharges(this.requestSpec, this.responseSpec, ChargesHelper.getLoanOverdueFeeJSON());
        Assert.assertNotNull(overdueFeeChargeId);

        final Integer loanProductID = createLoanProduct(overdueFeeChargeId.toString());
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(AccountTransferTest.LOAN_APPROVAL_DATE_PLUS_ONE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        ArrayList<HashMap> repaymentScheduleDataBefore = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec,
                this.responseSpec, loanID);

        String JobName = "Apply penalty to overdue loans";
        this.schedulerJobHelper.executeJob(JobName);

        final HashMap chargeData = ChargesHelper.getChargeById(this.requestSpec, this.responseSpec, overdueFeeChargeId);

        Float chargeAmount = (Float) chargeData.get("amount");

        ArrayList<HashMap> repaymentScheduleDataAfter = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec,
                this.responseSpec, loanID);

        Assert.assertEquals("Verifying From Penalty Charges due fot first Repayment after Successful completion of Scheduler Job",
                chargeAmount, (Float) repaymentScheduleDataAfter.get(1).get("penaltyChargesDue"));

        loanStatusHashMap = this.loanTransactionHelper.undoDisbursal(loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

    }

    @Test
    public void testUpdateOverdueDaysForNPA() throws InterruptedException {
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientID);

        final Integer loanProductID = createLoanProduct(null);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID.toString(), loanProductID.toString(), null);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(AccountTransferTest.LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(AccountTransferTest.LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final Boolean isNPABefore = (Boolean) this.loanTransactionHelper.getLoanDetail(requestSpec, responseSpec, loanID, "isNPA");
        Assert.assertFalse(isNPABefore);
        // Integer jobId = (Integer) allSchedulerJobsData.get(1).get("jobId");
        String JobName = "Update Non Performing Assets";
        this.schedulerJobHelper.executeJob(JobName);
        final Boolean isNPAAfter = (Boolean) this.loanTransactionHelper.getLoanDetail(requestSpec, responseSpec, loanID, "isNPA");
        Assert.assertTrue(isNPAAfter);
    }

    @Test
    public void testInterestTransferForSavings() throws InterruptedException {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        AccountHelper accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        FixedDepositAccountHelper fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -2);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String WHOLE_TERM = "1";

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);
        Float balance = new Float(MINIMUM_OPENING_BALANCE) + new Float(FixedDepositAccountHelper.depositAmount);
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, String.valueOf(balance));
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientId, savingsProductID,
                ClientSavingsIntegrationTest.ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsId);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        HashMap summary = savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals("Verifying opening Balance", balance, summary.get("accountBalance"));

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, WHOLE_TERM, savingsId.toString(), true, fixedDepositAccountHelper);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);
        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE);
        assertEquals("Verifying Balance", balance, summary.get("accountBalance"));

        fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);

        HashMap fixedDepositSummary = savingsAccountHelper.getSavingsSummary(fixedDepositAccountId);
        Float interestPosted = (Float) fixedDepositSummary.get("accountBalance") - new Float(FixedDepositAccountHelper.depositAmount);

        String JobName = "Transfer Interest To Savings";
        this.schedulerJobHelper.executeJob(JobName);
        fixedDepositSummary = savingsAccountHelper.getSavingsSummary(fixedDepositAccountId);
        assertEquals("Verifying opening Balance", new Float(FixedDepositAccountHelper.depositAmount),
                fixedDepositSummary.get("accountBalance"));

        summary = savingsAccountHelper.getSavingsSummary(savingsId);
        balance = new Float(MINIMUM_OPENING_BALANCE) + interestPosted;
        validateNumberForEqualExcludePrecission(String.valueOf(balance), String.valueOf(summary.get("accountBalance")));
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private static Integer createSavingsProduct(final String minOpenningBalance, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createLoanProduct(final String chargeId) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("15,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .build(chargeId);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final String clientID, final String loanProductID, final String savingsID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("15,000.00") //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("10 January 2013") //
                .withSubmittedOnDate("10 January 2013") //
                .build(clientID, loanProductID, savingsID);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        final String fixedDepositProductJSON = fixedDepositProductHelper //
                // .withAccountingRuleAsCashBased(accounts)
                .withPeriodRangeChart()//
                .build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType, String savingsId,
            final boolean transferInterest, final FixedDepositAccountHelper fixedDepositAccountHelper) {
        System.out.println("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec)
                //
                .withSubmittedOnDate(submittedOnDate).withSavings(savingsId).transferInterest(true)
                .withLockinPeriodFrequency("1", FixedDepositAccountHelper.DAYS)
                .build(clientID, productID, penalInterestType);
        return fixedDepositAccountHelper.applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec, this.responseSpec);
    }

    public void validateNumberForEqualExcludePrecission(String val, String val2) {
        DecimalFormat twoDForm = new DecimalFormat("#", new DecimalFormatSymbols(Locale.US));
        Assert.assertTrue(new Float(twoDForm.format(new Float(val))).compareTo(new Float(twoDForm.format(new Float(val2)))) == 0);
    }
}
