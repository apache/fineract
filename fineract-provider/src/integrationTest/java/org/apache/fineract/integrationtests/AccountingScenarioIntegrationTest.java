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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountHelper;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes", "unchecked", "static-access" })
public class AccountingScenarioIntegrationTest {

    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;

    private final String DATE_OF_JOINING = "01 January 2011";

    private final Float LP_PRINCIPAL = 10000.0f;
    private final String LP_REPAYMENTS = "5";
    private final String LP_REPAYMENT_PERIOD = "2";
    private final String LP_INTEREST_RATE = "1";
    private final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private final String LOAN_APPLICATION_SUBMISSION_DATE = "3 March 2011";
    private final String TRANSACTION_DATE = "01 March 2013";
    private final String LOAN_TERM_FREQUENCY = "10";
    private final String INDIVIDUAL_LOAN = "individual";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String DEPOSIT_AMOUNT = "7000";
    public static final String WITHDRAWAL_AMOUNT = "3000";
    public static final String WITHDRAWAL_AMOUNT_ADJUSTED = "2000";

    Float SP_BALANCE = new Float(MINIMUM_OPENING_BALANCE);
    Float SP_DEPOSIT_AMOUNT = new Float(DEPOSIT_AMOUNT);
    Float SP_WITHDRAWAL_AMOUNT = new Float(WITHDRAWAL_AMOUNT);
    Float SP_WITHDRAWAL_AMOUNT_ADJUSTED = new Float(WITHDRAWAL_AMOUNT_ADJUSTED);

    private final String REPAYMENT_DATE[] = { "", "04 May 2011", "04 July 2011", "04 September 2011", "04 November 2011", "04 January 2012" };
    private final Float REPAYMENT_AMOUNT[] = { .0f, 2200.0f, 3000.0f, 900.0f, 2000.0f, 2500.0f };

    private final Float AMOUNT_TO_BE_WAIVE = 400.0f;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private FixedDepositProductHelper fixedDepositProductHelper;
    private FixedDepositAccountHelper fixedDepositAccountHelper;
    private RecurringDepositProductHelper recurringDepositProductHelper;
    private RecurringDepositAccountHelper recurringDepositAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec, this.responseSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void checkUpfrontAccrualAccountingFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithUpfrontAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);
        System.out.println("CHECKING INCOME: ******************************************");
        final JournalEntry incomeJournalEntry = new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, this.EXPECTED_DISBURSAL_DATE, incomeJournalEntry);

        // MAKE 1
        System.out.println("Repayment 1 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[1], this.REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = this.LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_INTEREST + FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[1], assetAccountFirstEntry);
        System.out.println("Repayment 1 Done......");

        // REPAYMENT 2
        System.out.println("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[2], this.REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = this.REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[2], assetAccountSecondEntry);
        System.out.println("Repayment 2 Done ......");

        // WAIVE INTEREST
        System.out.println("Waive Interest  ......");
        this.loanTransactionHelper.waiveInterest(this.REPAYMENT_DATE[4], this.AMOUNT_TO_BE_WAIVE.toString(), loanID);

        final JournalEntry waivedEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], waivedEntry);

        final JournalEntry expenseJournalEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, this.REPAYMENT_DATE[4], expenseJournalEntry);
        System.out.println("Waive Interest Done......");

        // REPAYMENT 3
        System.out.println("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[3], this.REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[3], assetAccountThirdEntry);
        System.out.println("Repayment 3 Done ......");

        // REPAYMENT 4
        System.out.println("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[4], this.REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], assetAccountFourthEntry);
        System.out.println("Repayment 4 Done  ......");

        // Repayment 5
        System.out.println("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[5], this.REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[5], assetAccountFifthEntry);
        System.out.println("Repayment 5 Done  ......");
    }

    private Integer createLoanProductWithUpfrontAccrualAccountingEnabled(final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleUpfrontAccrual(accounts)
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString())
                .withLoanTermFrequency(this.LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(this.LP_REPAYMENTS)
                .withRepaymentEveryAfter(this.LP_REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate(this.EXPECTED_DISBURSAL_DATE).withSubmittedOnDate(this.LOAN_APPLICATION_SUBMISSION_DATE)
                .withLoanType(this.INDIVIDUAL_LOAN).build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    @Test
    public void checkAccountingWithSavingsFlow() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

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

        // First Transaction-Deposit
        this.savingsAccountHelper.depositToSavingsAccount(savingsID, DEPOSIT_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Float balance = SP_BALANCE + SP_DEPOSIT_AMOUNT;
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals("Verifying Balance after Deposit", balance, summary.get("accountBalance"));

        System.out.println("----------------------Verifying Journal Entry after the Transaction Deposit----------------------------");
        final JournalEntry[] assetAccountFirstTransactionEntry = { new JournalEntry(this.SP_DEPOSIT_AMOUNT,
                JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liabililityAccountFirstTransactionEntry = { new JournalEntry(this.SP_DEPOSIT_AMOUNT,
                JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.TRANSACTION_DATE, assetAccountFirstTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, this.TRANSACTION_DATE,
                liabililityAccountFirstTransactionEntry);

        // Second Transaction-Withdrawal
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        balance -= SP_WITHDRAWAL_AMOUNT;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals("Verifying Balance after Withdrawal", balance, summary.get("accountBalance"));

        System.out.println("-------------------Verifying Journal Entry after the Transaction Withdrawal----------------------");
        final JournalEntry[] assetAccountSecondTransactionEntry = { new JournalEntry(this.SP_WITHDRAWAL_AMOUNT,
                JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] liabililityAccountSecondTransactionEntry = { new JournalEntry(this.SP_WITHDRAWAL_AMOUNT,
                JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.TRANSACTION_DATE, assetAccountSecondTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, this.TRANSACTION_DATE,
                liabililityAccountSecondTransactionEntry);

        // Third Transaction-Add Charges for Withdrawal Fee
        final Integer withdrawalChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assert.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsID, withdrawalChargeId, false);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsID);
        Assert.assertEquals(1, chargesPendingState.size());
        HashMap savingsChargeForPay = chargesPendingState.get(0);
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsID, (Integer) savingsChargeForPay.get("id"));
        Float chargeAmount = (Float) paidCharge.get("amount");

        // Withdrawal after adding Charge of type Withdrawal Fee
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT_ADJUSTED,
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        balance = balance - SP_WITHDRAWAL_AMOUNT_ADJUSTED - chargeAmount;

        final JournalEntry[] liabililityAccountThirdTransactionEntry = {
                new JournalEntry(chargeAmount, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.SP_WITHDRAWAL_AMOUNT_ADJUSTED, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] assetAccountThirdTransactionEntry = { new JournalEntry(this.SP_WITHDRAWAL_AMOUNT_ADJUSTED,
                JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] incomeAccountThirdTransactionEntry = { new JournalEntry(chargeAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.TRANSACTION_DATE, assetAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, this.TRANSACTION_DATE,
                liabililityAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, this.TRANSACTION_DATE, incomeAccountThirdTransactionEntry);

        // Verifying Balance after applying Charge for Withdrawal Fee
        assertEquals("Verifying Balance", balance, summary.get("accountBalance"));
    }

    @Test
    public void testFixedDepositAccountingFlow() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, FixedDepositTest.WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);

        Float depositAmount = (Float) accountSummary.get("totalDeposits");

        // Checking initial Journal entries after Activation.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        // Checking initial Journal entries after Interest Posting.
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

    }

    @Test
    public void testRecurringDepositAccountingFlow() {
        this.recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assert.assertNotNull(recurringDepositProductId);

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, RecurringDepositTest.WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(
                this.requestSpec, this.responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = this.recurringDepositAccountHelper.getRecurringDepositAccountById(this.requestSpec,
                this.responseSpec, recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("mandatoryRecommendedDepositAmount");

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                depositAmount, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assert.assertNotNull(depositTransactionId);

        // Checking initial Journal entries after Activation.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE,
                liablilityAccountInitialEntry);

        Integer interestPostingTransactionId = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assert.assertNotNull(interestPostingTransactionId);

        HashMap accountSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        // Checking initial Journal entries after Interest Posting.
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

    }

    public static Integer createSavingsProduct(final String minOpenningBalance, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        final String fixedDepositProductJSON = fixedDepositProductHelper //
                .withPeriodRangeChart()//
                .withAccountingRuleAsCashBased(accounts).build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType) {
        System.out.println("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate).build(clientID, productID, penalInterestType);
        return this.fixedDepositAccountHelper
                .applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec, this.responseSpec);
    }

    private Integer createRecurringDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        System.out.println("------------------------------CREATING NEW RECURRING DEPOSIT PRODUCT ---------------------------------------");
        RecurringDepositProductHelper recurringDepositProductHelper = new RecurringDepositProductHelper(this.requestSpec, this.responseSpec);
        final String recurringDepositProductJSON = recurringDepositProductHelper //
                .withPeriodRangeChart()//
                .withAccountingRuleAsCashBased(accounts).build(validFrom, validTo);
        return RecurringDepositProductHelper.createRecurringDepositProduct(recurringDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForRecurringDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType, final String expectedFirstDepositOnDate) {
        System.out.println("--------------------------------APPLYING FOR RECURRING DEPOSIT ACCOUNT --------------------------------");
        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(this.requestSpec, this.responseSpec)
                //
                .withSubmittedOnDate(submittedOnDate).withExpectedFirstDepositOnDate(expectedFirstDepositOnDate)
                .build(clientID, productID, penalInterestType);
        return this.recurringDepositAccountHelper.applyRecurringDepositApplication(recurringDepositApplicationJSON, this.requestSpec,
                this.responseSpec);
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // MAKE 1
        System.out.println("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[1]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[1], this.REPAYMENT_AMOUNT[1], loanID);
        float expected_value = this.LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_INTEREST + FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[1], assetAccountFirstEntry);
        System.out.println("Repayment 1 Done......");

        // REPAYMENT 2
        System.out.println("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[2], this.REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = this.REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[2]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[3]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[2], assetAccountSecondEntry);
        System.out.println("Repayment 2 Done ......");

        // WAIVE INTEREST
        System.out.println("Waive Interest  ......");
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[4]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[5]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.waiveInterest(this.REPAYMENT_DATE[4], this.AMOUNT_TO_BE_WAIVE.toString(), loanID);

        final JournalEntry waivedEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], waivedEntry);

        final JournalEntry expenseJournalEntry = new JournalEntry(this.AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, this.REPAYMENT_DATE[4], expenseJournalEntry);
        System.out.println("Waive Interest Done......");

        // REPAYMENT 3
        System.out.println("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[3], this.REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[3], assetAccountThirdEntry);
        System.out.println("Repayment 3 Done ......");

        // REPAYMENT 4
        System.out.println("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[4], this.REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], assetAccountFourthEntry);
        System.out.println("Repayment 4 Done  ......");

        // Repayment 5
        System.out.println("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[5], this.REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[5], assetAccountFifthEntry);
        System.out.println("Repayment 5 Done  ......");
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow_OVER_PAYMENT() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        final String jobName = "Add Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // MAKE 1
        System.out.println("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(this.REPAYMENT_DATE[1]), FIRST_INTEREST,
                FEE_PORTION, PENALTY_PORTION, loanID);
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[1], 15000f, loanID);
        float expected_value = this.LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountEntry = { new JournalEntry(15000f, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(11000f, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[1], assetAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, this.REPAYMENT_DATE[1], new JournalEntry(4000f,
                JournalEntry.TransactionType.CREDIT));
        System.out.println("Repayment  Done......");

    }

    @Test
    public void checkPeriodicAccrualAccountingTillCurrentDateFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(FEE_PORTION), false));
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance();
        final String currentDate = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.DATE, -4);

        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.MONTH, 2);
        final String FIRST_REPAYMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate = Calendar.getInstance();
        todayDate.add(Calendar.DATE, -2);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate),
                        dateFormat.format(todayDate.getTime()), String.valueOf(PENALTY_PORTION)));
        todayDate.add(Calendar.DATE, 1);
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flat), dateFormat.format(todayDate.getTime()), String.valueOf(FEE_PORTION)));

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);

        final String jobName = "Add Periodic Accrual Transactions";
        try {
            this.schedulerJobHelper.executeJob(jobName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        // MAKE 1
        List fromDateList = (List) loanSchedule.get(1).get("fromDate");
        LocalDate fromDateLocal = LocalDate.now();
        fromDateLocal = fromDateLocal.withYear((int) fromDateList.get(0));
        fromDateLocal = fromDateLocal.withMonthOfYear((int) fromDateList.get(1));
        fromDateLocal = fromDateLocal.withDayOfMonth((int) fromDateList.get(2));

        List dueDateList = (List) loanSchedule.get(1).get("dueDate");
        LocalDate dueDateLocal = LocalDate.now();
        dueDateLocal = dueDateLocal.withYear((int) dueDateList.get(0));
        dueDateLocal = dueDateLocal.withMonthOfYear((int) dueDateList.get(1));
        dueDateLocal = dueDateLocal.withDayOfMonth((int) dueDateList.get(2));

        int totalDaysInPeriod = Days.daysBetween(fromDateLocal, dueDateLocal).getDays();

        float totalInterest = (float) loanSchedule.get(1).get("interestOriginalDue");
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float INTEREST_4_DAYS = totalInterest / totalDaysInPeriod * 4;
        INTEREST_4_DAYS = new Float(numberFormat.format(INTEREST_4_DAYS));

        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(currentDate), INTEREST_4_DAYS, FEE_PORTION,
                PENALTY_PORTION, loanID);

    }

    @Test
    public void checkPeriodicAccrualAccountingAPIFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        final float NEXT_FEE_PORTION = 55.0f;
        final float NEXT_PENALTY_PORTION = 105.0f;

        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(FEE_PORTION), false));
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        Integer flatNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper.getLoanSpecifiedDueDateJSON(
                ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_FEE_PORTION), false));
        Integer flatSpecifiedDueDateNext = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(NEXT_PENALTY_PORTION), true));

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance();
        final String currentDate = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.DATE, -4);

        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.MONTH, 2);
        final String FIRST_REPAYMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate = Calendar.getInstance();
        todayDate.add(Calendar.DATE, -2);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSEMENT_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDate),
                        dateFormat.format(todayDate.getTime()), String.valueOf(PENALTY_PORTION)));
        todayDate.add(Calendar.DATE, 1);
        String runOndate = dateFormat.format(todayDate.getTime());

        this.loanTransactionHelper
                .addChargesForLoan(
                        loanID,
                        LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), runOndate,
                                String.valueOf(FEE_PORTION)));

        todayDate.add(Calendar.DATE, 1);
        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatSpecifiedDueDateNext),
                        dateFormat.format(todayDate.getTime()), String.valueOf(NEXT_PENALTY_PORTION)));

        this.loanTransactionHelper.addChargesForLoan(
                loanID,
                LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flatNext),
                        dateFormat.format(todayDate.getTime()), String.valueOf(NEXT_FEE_PORTION)));

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);

        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);

        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(this.requestSpec, this.responseSpec,
                loanID);
        // MAKE 1
        List fromDateList = (List) loanSchedule.get(1).get("fromDate");
        LocalDate fromDateLocal = LocalDate.now();
        fromDateLocal = fromDateLocal.withYear((int) fromDateList.get(0));
        fromDateLocal = fromDateLocal.withMonthOfYear((int) fromDateList.get(1));
        fromDateLocal = fromDateLocal.withDayOfMonth((int) fromDateList.get(2));

        List dueDateList = (List) loanSchedule.get(1).get("dueDate");
        LocalDate dueDateLocal = LocalDate.now();
        dueDateLocal = dueDateLocal.withYear((int) dueDateList.get(0));
        dueDateLocal = dueDateLocal.withMonthOfYear((int) dueDateList.get(1));
        dueDateLocal = dueDateLocal.withDayOfMonth((int) dueDateList.get(2));

        int totalDaysInPeriod = Days.daysBetween(fromDateLocal, dueDateLocal).getDays();

        float totalInterest = (float) loanSchedule.get(1).get("interestOriginalDue");
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float INTEREST_3_DAYS = totalInterest / totalDaysInPeriod * 3;
        INTEREST_3_DAYS = new Float(numberFormat.format(INTEREST_3_DAYS));
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), INTEREST_3_DAYS, FEE_PORTION,
                PENALTY_PORTION, loanID);

        runOndate = dateFormat.format(todayDate.getTime());

        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);
        float interestPerDay = (totalInterest / totalDaysInPeriod * 4) - INTEREST_3_DAYS;
        interestPerDay = new Float(numberFormat.format(interestPerDay));
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), interestPerDay, NEXT_FEE_PORTION,
                NEXT_PENALTY_PORTION, loanID);

    }

    private Integer createLoanProductWithPeriodicAccrualAccountingEnabled(final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .withDaysInMonth("30").withDaysInYear("365").build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    @Test
    public void checkCashBasedAccountingFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithCashBasedAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, this.DATE_OF_JOINING);
        final Integer loanID = applyForLoanApplication(clientID, loanProductID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(this.EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        System.out.println("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(this.LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        // MAKE 1
        System.out.println("Repayment 1 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[1], this.REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = this.LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[1], assetAccountFirstEntry);
        System.out.println("CHECKING INCOME: ******************************************");
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, this.REPAYMENT_DATE[1], new JournalEntry(FIRST_INTEREST,
                JournalEntry.TransactionType.CREDIT));
        System.out.println("Repayment 1 Done......");

        // REPAYMENT 2
        System.out.println("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[2], this.REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = this.REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[2], assetAccountSecondEntry);
        System.out.println("CHECKING INCOME: ******************************************");
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, this.REPAYMENT_DATE[2], new JournalEntry(
                SECOND_AND_THIRD_INTEREST, JournalEntry.TransactionType.CREDIT));
        System.out.println("Repayment 2 Done ......");

        // WAIVE INTEREST
        System.out.println("Waive Interest  ......");
        Integer transactionId = this.loanTransactionHelper.waiveInterestAndReturnTransactionId(this.REPAYMENT_DATE[4],
                this.AMOUNT_TO_BE_WAIVE.toString(), loanID);
        // waive of fees and interest are not considered in cash based
        // accounting,
        this.journalEntryHelper.ensureNoAccountingTransactionsWithTransactionId("L" + transactionId);

        // REPAYMENT 3
        System.out.println("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[3], this.REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[3], assetAccountThirdEntry);
        System.out.println("Repayment 3 Done ......");

        // REPAYMENT 4
        System.out.println("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[4], this.REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[4], assetAccountFourthEntry);
        System.out.println("Repayment 4 Done  ......");

        // Repayment 5
        System.out.println("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(this.REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(this.REPAYMENT_DATE[5], this.REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, this.REPAYMENT_DATE[5], assetAccountFifthEntry);
        System.out.println("Repayment 5 Done  ......");
    }

    private Integer createLoanProductWithCashBasedAccountingEnabled(final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(this.LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(this.LP_REPAYMENT_PERIOD).withNumberOfRepayments(this.LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(this.LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsCashBased(accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private LocalDate getDateAsLocalDate(String dateAsString) {
        LocalDate date = null;
        try {
            DateFormat df = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
            date = new LocalDate(df.parse(dateAsString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}
