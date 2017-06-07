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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.TaxComponentHelper;
import org.apache.fineract.integrationtests.common.TaxGroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.Account.AccountType;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountHelper;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.fixeddeposit.FixedDepositProductHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "unchecked", "rawtypes", "static-access" })
public class FixedDepositTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private FixedDepositProductHelper fixedDepositProductHelper;
    private FixedDepositAccountHelper fixedDepositAccountHelper;
    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private JournalEntryHelper journalEntryHelper;
    private FinancialActivityAccountHelper financialActivityAccountHelper;

    public static final String WHOLE_TERM = "1";
    public static final String TILL_PREMATURE_WITHDRAWAL = "2";
    private static final String DAILY = "1";
    private static final String MONTHLY = "4";
    private static final String QUARTERLY = "5";
    private static final String BI_ANNUALLY = "6";
    private static final String ANNUALLY = "7";
    private static final String INTEREST_CALCULATION_USING_DAILY_BALANCE = "1";
    private static final String INTEREST_CALCULATION_USING_AVERAGE_DAILY_BALANCE = "2";
    private static final String DAYS_360 = "360";
    private static final String DAYS_365 = "365";

    private static final String NONE = "1";
    private static final String CASH_BASED = "2";

    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String CLOSURE_TYPE_WITHDRAW_DEPOSIT = "100";
    public static final String CLOSURE_TYPE_TRANSFER_TO_SAVINGS = "200";
    public static final String CLOSURE_TYPE_REINVEST = "300";
    public static final Integer DAILY_COMPOUNDING_INTERVAL = 0;
    public static final Integer MONTHLY_INTERVAL = 1;
    public static final Integer QUARTERLY_INTERVAL = 3;
    public static final Integer BIANNULLY_INTERVAL = 6;
    public static final Integer ANNUL_INTERVAL = 12;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.financialActivityAccountHelper = new FinancialActivityAccountHelper(this.requestSpec);
    }

    /***
     * Test case for Fixed Deposit Account premature closure with transaction
     * type withdrawal and Cash Based accounting enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeWithdrawal() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create GL Accounts for product account mapping
         */
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create FD product with CashBased accounting enabled
         */
        final String accountingRule = CASH_BASED;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assert.assertNotNull(fixedDepositProductId);

        /***
         * Apply for FD account with created product and verify status
         */
        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        /***
         * Approve the FD account and verify whether account is approved
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        /***
         * Activate the FD Account and verify whether account is activated
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);

        Float depositAmount = (Float) accountSummary.get("totalDeposits");

        /***
         * Verify journal entries posted for initial deposit transaction which
         * happened at activation time
         */
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        /***
         * Update interest earned of FD account
         */
        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        /***
         * Post interest and verify the account summary
         */
        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        /***
         * Verify journal entries transactions for interest posting transaction
         */
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

        /***
         * Preclose the FD account verify whether account is preClosed
         */
        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

     
        /***
         * Verify journal entry transactions for preclosure transaction
         */
        HashMap accountDetails = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float maturityAmount = Float.valueOf(accountDetails.get("maturityAmount").toString());
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, CLOSED_ON_DATE, new JournalEntry(maturityAmount,
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, new JournalEntry(maturityAmount,
                JournalEntry.TransactionType.DEBIT));

    }
    
    
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeWithdrawal_WITH_HOLD_TAX() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create GL Accounts for product account mapping
         */
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
        final Account liabilityAccountForTax = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create FD product with CashBased accounting enabled
         */
        final String accountingRule = CASH_BASED;
        final Integer taxGroupId = createTaxGroup("10", liabilityAccountForTax);
        Integer fixedDepositProductId = createFixedDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, String.valueOf(taxGroupId),
                accountingRule, assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        Assert.assertNotNull(fixedDepositProductId);

        /***
         * Apply for FD account with created product and verify status
         */
        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(),
                SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        /***
         * Approve the FD account and verify whether account is approved
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        /***
         * Activate the FD Account and verify whether account is activated
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);

        Float depositAmount = (Float) accountSummary.get("totalDeposits");

        /***
         * Verify journal entries posted for initial deposit transaction which
         * happened at activation time
         */
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        /***
         * Update interest earned of FD account
         */
        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        /***
         * Post interest and verify the account summary
         */
        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");
        Assert.assertNull(accountSummary.get("totalWithholdTax"));

        /***
         * Verify journal entries transactions for interest posting transaction
         */
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

        /***
         * Preclose the FD account verify whether account is preClosed
         */
        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        /***
         * Verify journal entry transactions for preclosure transaction
         */
        HashMap accountDetails = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float maturityAmount = Float.valueOf(accountDetails.get("maturityAmount").toString());
        
        HashMap summary = (HashMap) accountDetails.get("summary");
        Assert.assertNotNull(summary.get("totalWithholdTax"));
        Float withHoldTax = (Float) summary.get("totalWithholdTax");
        
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, CLOSED_ON_DATE, new JournalEntry(maturityAmount,
                JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, new JournalEntry(maturityAmount,
                JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccountForTax, CLOSED_ON_DATE, new JournalEntry(withHoldTax,
                JournalEntry.TransactionType.CREDIT));

    }
    
    
    @Test
    public void testFixedDepositAccountClosureTypeWithdrawal_WITH_HOLD_TAX() throws InterruptedException {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create GL Accounts for product account mapping
         */
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
        final Account liabilityAccountForTax = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -20);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -20);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        Calendar closedOn = Calendar.getInstance();
        closedOn.add(Calendar.MONTH, -6);
        final String CLOSED_ON_DATE = dateFormat.format(closedOn.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create FD product with CashBased accounting enabled
         */
        final String accountingRule = CASH_BASED;
        final Integer taxGroupId = createTaxGroup("10", liabilityAccountForTax);
        Integer fixedDepositProductId = createFixedDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, String.valueOf(taxGroupId),
                accountingRule, assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        Assert.assertNotNull(fixedDepositProductId);

        /***
         * Apply for FD account with created product and verify status
         */
        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(),
                SUBMITTED_ON_DATE, WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        /***
         * Approve the FD account and verify whether account is approved
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        /***
         * Activate the FD Account and verify whether account is activated
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);

        Float depositAmount = (Float) accountSummary.get("totalDeposits");

        /***
         * Verify journal entries posted for initial deposit transaction which
         * happened at activation time
         */
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        /***
         * Update interest earned of FD account
         */
        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        /***
         * Post interest and verify the account summary
         */
        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");
        Assert.assertNull(accountSummary.get("totalWithholdTax"));

        /***
         * FD account verify whether account is matured
         */
        
        SchedulerJobHelper schedulerJobHelper =  new SchedulerJobHelper(requestSpec, responseSpec);
        String JobName = "Update Deposit Accounts Maturity details";
        schedulerJobHelper.executeJob(JobName);
        
        HashMap accountDetails = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        
        HashMap summary = (HashMap) accountDetails.get("summary");
        Assert.assertNotNull(summary.get("totalWithholdTax"));
        Float withHoldTax = (Float) summary.get("totalWithholdTax");
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccountForTax, CLOSED_ON_DATE, new JournalEntry(withHoldTax,
                JournalEntry.TransactionType.CREDIT));
        

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsMatured(fixedDepositAccountStatusHashMap);

    }

    
    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart() {
        final String chartToUse = "period";
        final String depositAmount = "10000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(6.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart_AMOUNT_VARIATION() {
        final String chartToUse = "period";
        final String depositAmount = "2000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(6.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart_PERIOD_VARIATION() {
        final String chartToUse = "period";
        final String depositAmount = "10000";
        final String depositPeriod = "18";
        final Float interestRate = new Float(7.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart() {
        final String chartToUse = "amount";
        final String depositAmount = "10000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(7.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart_AMOUNT_VARIATION() {
        final String chartToUse = "amount";
        final String depositAmount = "5000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(5.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart_PERIOD_VARIATION() {
        final String chartToUse = "amount";
        final String depositAmount = "10000";
        final String depositPeriod = "26";
        final Float interestRate = new Float(7.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }

    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart() {
        final String chartToUse = "period_amount";
        final String depositAmount = "10000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(7.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart_AMOUNT_VARIATION() {
        final String chartToUse = "period_amount";
        final String depositAmount = "5000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(6.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart_PERIOD_VARIATION() {
        final String chartToUse = "period_amount";
        final String depositAmount = "10000";
        final String depositPeriod = "20";
        final Float interestRate = new Float(9.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart() {
        final String chartToUse = "amount_period";
        final String depositAmount = "10000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(8.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart_AMOUNT_VARIATION() {
        final String chartToUse = "amount_period";
        final String depositAmount = "5000";
        final String depositPeriod = "12";
        final Float interestRate = new Float(6.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }
    
    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart_PERIOD_VARIATION() {
        final String chartToUse = "amount_period";
        final String depositAmount = "10000";
        final String depositPeriod = "6";
        final Float interestRate = new Float(7.0);
        testFixedDepositAccountForInterestRate(chartToUse, depositAmount, depositPeriod, interestRate);
    }

    private void testFixedDepositAccountForInterestRate(final String chartToUse, final String depositAmount, final String depositPeriod,
            final Float interestRate) {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        final String VALID_FROM = "01 March 2014";
        final String VALID_TO = "01 March 2016";

        final String SUBMITTED_ON_DATE = "01 March 2015";
        final String APPROVED_ON_DATE = "01 March 2015";
        final String ACTIVATION_DATE = "01 March 2015";

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create FD product with CashBased accounting enabled
         */
        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule, chartToUse);
        Assert.assertNotNull(fixedDepositProductId);

        /***
         * Apply for FD account with created product and verify status
         */
        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM, depositAmount, depositPeriod);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        /***
         * Approve the FD account and verify whether account is approved
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        /***
         * Activate the FD Account and verify whether account is activated
         */
        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap accountSummary = this.fixedDepositAccountHelper.getFixedDepositDetails(fixedDepositAccountId);

        Assert.assertEquals(interestRate, accountSummary.get("nominalAnnualInterestRate"));
    }

    /***
     * Test case for FD Account premature closure with transaction transfers to
     * savings account and Cash Based accounting enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeTransferToSavings() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        /***
         * Create GL Accounts for product account mapping
         */
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create Savings product with CashBased accounting enabled
         */
        final String accountingRule = CASH_BASED;
        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, accountingRule,
                assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        Assert.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientId, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(savingsProductID);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, savingsId);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        /***
         * Create FD product with CashBased accounting enabled
         */
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
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

        /***
         * Verify journal entries posted for initial deposit transaction which
         * happened at activation time
         */
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        /***
         * Update interest earned of FD account
         */
        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        /***
         * Post interest and verify the account summary
         */
        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        /***
         * Verify journal entries transactions for interest posting transaction
         */
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

        HashMap savingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balanceBefore = (Float) savingsSummaryBefore.get("accountBalance");

        /***
         * Retrieve mapped financial account for liability transfer
         */
        Account financialAccount = getMappedLiabilityFinancialAccount();

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        /***
         * Preclose the account and verify journal entries
         */
        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_TRANSFER_TO_SAVINGS, savingsId, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float prematurityAmount = (Float) fixedDepositData.get("maturityAmount");

        /***
         * Verify journal entry transactions for preclosure transaction As this
         * transaction is an account transfer you should get financial account
         * mapping details and verify amounts
         */
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, new JournalEntry(prematurityAmount,
                JournalEntry.TransactionType.CREDIT), new JournalEntry(prematurityAmount, JournalEntry.TransactionType.DEBIT));

        this.journalEntryHelper.checkJournalEntryForAssetAccount(financialAccount, CLOSED_ON_DATE, new JournalEntry(prematurityAmount,
                JournalEntry.TransactionType.DEBIT), new JournalEntry(prematurityAmount, JournalEntry.TransactionType.CREDIT));

        HashMap savingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balanceAfter = (Float) savingsSummaryAfter.get("accountBalance");
        Float expectedSavingsBalance = balanceBefore + prematurityAmount;

        Assert.assertEquals("Verifying Savings Account Balance after Premature Closure", expectedSavingsBalance, balanceAfter);

    }

    /***
     * Test case for Fixed Deposit Account premature closure with transaction
     * type ReInvest and Cash Based accounting enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeReinvest() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        FixedDepositAccountHelper fixedDepositAccountHelperValidationError = new FixedDepositAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());

        /***
         * Create GL Accounts for product account mapping
         */
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
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
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = (daysInMonth - currentDate) + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        /***
         * Create FD product with CashBased accounting enabled
         */
        final String accountingRule = CASH_BASED;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
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

        /***
         * Verify journal entries posted for initial deposit transaction which
         * happened at activation time
         */
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, liablilityAccountInitialEntry);

        fixedDepositAccountId = this.fixedDepositAccountHelper.calculateInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(fixedDepositAccountId);

        Integer transactionIdForPostInterest = this.fixedDepositAccountHelper.postInterestForFixedDeposit(fixedDepositAccountId);
        Assert.assertNotNull(transactionIdForPostInterest);

        accountSummary = this.fixedDepositAccountHelper.getFixedDepositSummary(fixedDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        /***
         * Verify journal entries transactions for interest posting transaction
         */
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        ArrayList<HashMap> errorResponse = (ArrayList<HashMap>) fixedDepositAccountHelperValidationError.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_REINVEST, null, CommonConstants.RESPONSE_ERROR);

        assertEquals("validation.msg.fixeddepositaccount.onAccountClosureId.reinvest.not.allowed",
                errorResponse.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @Test
    public void testFixedDepositAccountUpdation() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        ArrayList<HashMap> allFixedDepositProductsData = this.fixedDepositProductHelper.retrieveAllFixedDepositProducts(this.requestSpec,
                this.responseSpec);
        HashMap fixedDepositProductData = this.fixedDepositProductHelper.retrieveFixedDepositProductById(this.requestSpec,
                this.responseSpec, fixedDepositProductId.toString());

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        todaysDate.add(Calendar.DATE, -1);
        SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateFixedDepositAccount(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), VALID_FROM, VALID_TO, WHOLE_TERM, SUBMITTED_ON_DATE);
        Assert.assertTrue(modificationsHashMap.containsKey("submittedOnDate"));

    }

    @Test
    public void testFixedDepositAccountUndoApproval() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.undoApproval(fixedDepositAccountId);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountRejectedAndClosed() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String REJECTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.rejectApplication(fixedDepositAccountId, REJECTED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsRejected(fixedDepositAccountStatusHashMap);
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsClosed(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountWithdrawnByClientAndClosed() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String WITHDRAWN_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.withdrawApplication(fixedDepositAccountId, WITHDRAWN_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsWithdrawn(fixedDepositAccountStatusHashMap);
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsClosed(fixedDepositAccountStatusHashMap);
    }

    @Test
    public void testFixedDepositAccountIsDeleted() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountId = (Integer) this.fixedDepositAccountHelper.deleteFixedDepositApplication(fixedDepositAccountId, "resourceId");
        Assert.assertNotNull(fixedDepositAccountId);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);
        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);
        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, MONTHLY_INTERVAL, MONTHLY_INTERVAL);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_365() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        todaysDate.add(Calendar.DAY_OF_MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_360() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        todaysDate.add(Calendar.DAY_OF_MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");

        principal = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", principal, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        todaysDate.add(Calendar.DAY_OF_MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                TILL_PREMATURE_WITHDRAWAL);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        activationDate.add(Calendar.DAY_OF_MONTH, -1);
        DateTime startDate = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime endDate = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(startDate, endDate).getMonths();

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        todaysDate.add(Calendar.DAY_OF_MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                TILL_PREMATURE_WITHDRAWAL);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                TILL_PREMATURE_WITHDRAWAL, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Float preClosurePenalInterestRate = (Float) fixedDepositAccountData.get("preClosurePenalInterest");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Calendar activationDate = Calendar.getInstance();
        activationDate.add(Calendar.MONTH, -1);
        activationDate.add(Calendar.DAY_OF_MONTH, -1);
        DateTime startDate = new DateTime(activationDate.getTime());

        Calendar prematureClosureDate = Calendar.getInstance();
        DateTime endDate = new DateTime(prematureClosureDate.getTime());

        Integer depositedPeriod = Months.monthsBetween(startDate, endDate).getMonths();

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositedPeriod);
        interestRate -= preClosurePenalInterestRate;
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        todaysDate.add(Calendar.MONTH, -1);
        todaysDate.add(Calendar.DAY_OF_MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        daysInMonth = (daysInMonth - currentDate) + 1;
        Float interestPerMonth = (float) (interestPerDay * principal * daysInMonth);
        principal += interestPerMonth;
        todaysDate.add(Calendar.DATE, daysInMonth);
        System.out.println(monthDayFormat.format(todaysDate.getTime()));

        interestPerMonth = (float) (interestPerDay * principal * currentDate);
        System.out.println("IPM = " + interestPerMonth);
        principal += interestPerMonth;
        System.out.println("principal = " + principal);

        HashMap fixedDepositPrematureData = this.fixedDepositAccountHelper.calculatePrematureAmountForFixedDeposit(fixedDepositAccountId,
                CLOSED_ON_DATE);

        Integer prematureClosureTransactionId = (Integer) this.fixedDepositAccountHelper.prematureCloseForFixedDeposit(
                fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(prematureClosureTransactionId);

        fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositAccountIsPrematureClosed(fixedDepositAccountStatusHashMap);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        principal = new Float(decimalFormat.format(principal));
        maturityAmount = new Float(decimalFormat.format(maturityAmount));
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        //todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        System.out.println("Submitted Date:" + SUBMITTED_ON_DATE);
        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, MONTHLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Float maturityAmount = (Float) fixedDepositAccountData.get("maturityAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL);

        principal = new BigDecimal(principal).setScale(0, BigDecimal.ROUND_FLOOR).floatValue();
        maturityAmount = new BigDecimal(maturityAmount).setScale(0, BigDecimal.ROUND_FLOOR).floatValue();
        System.out.println(principal);
        Assert.assertEquals("Verifying Maturity amount for Fixed Deposit Account", principal, maturityAmount);

    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();

        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testMaturityAmountDailyCompoundingAndAnnuallyPostingWith_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();

        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, DAILY, ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.activateFixedDeposit(fixedDepositAccountId, ACTIVATION_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsActive(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, BI_ANNUALLY, BI_ANNUALLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);

    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_365_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_365,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);
    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_360_Days() {
        this.fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat monthDayFormat = new SimpleDateFormat("dd MMM", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = new Integer(currentMonthFormat.format(todaysDate.getTime()));
        Integer numberOfMonths = 12 - currentMonth;
        todaysDate.add(Calendar.MONTH, numberOfMonths);
        Integer currentDate = new Integer(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer daysLeft = daysInMonth - currentDate;
        todaysDate.add(Calendar.DATE, (daysLeft + 1));
        daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        System.out.println(dateFormat.format(todaysDate.getTime()));
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());

        final String VALID_TO = null;
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String ACTIVATION_DATE = dateFormat.format(todaysDate.getTime());
        final String MONTH_DAY = monthDayFormat.format(todaysDate.getTime());

        Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(clientId);

        final String accountingRule = NONE;
        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, accountingRule);
        Assert.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), SUBMITTED_ON_DATE,
                WHOLE_TERM);
        Assert.assertNotNull(fixedDepositAccountId);

        HashMap modificationsHashMap = this.fixedDepositAccountHelper.updateInterestCalculationConfigForFixedDeposit(clientId.toString(),
                fixedDepositProductId.toString(), fixedDepositAccountId.toString(), SUBMITTED_ON_DATE, VALID_FROM, VALID_TO, DAYS_360,
                WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, QUARTERLY, QUARTERLY);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(this.requestSpec,
                this.responseSpec, fixedDepositAccountId.toString());
        FixedDepositAccountStatusChecker.verifyFixedDepositIsPending(fixedDepositAccountStatusHashMap);

        fixedDepositAccountStatusHashMap = this.fixedDepositAccountHelper.approveFixedDeposit(fixedDepositAccountId, APPROVED_ON_DATE);
        FixedDepositAccountStatusChecker.verifyFixedDepositIsApproved(fixedDepositAccountStatusHashMap);

        HashMap fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        Float principal = (Float) fixedDepositAccountData.get("depositAmount");
        Integer depositPeriod = (Integer) fixedDepositAccountData.get("depositPeriod");
        HashMap daysInYearMap = (HashMap) fixedDepositAccountData.get("interestCalculationDaysInYearType");
        Integer daysInYear = (Integer) daysInYearMap.get("id");
        ArrayList<ArrayList<HashMap>> interestRateChartData = this.fixedDepositProductHelper.getInterestRateChartSlabsByProductId(
                this.requestSpec, this.responseSpec, fixedDepositProductId);

        Float interestRate = this.fixedDepositAccountHelper.getInterestRate(interestRateChartData, depositPeriod);
        double interestRateInFraction = (interestRate / 100);
        double perDay = (double) 1 / (daysInYear);
        System.out.println("per day = " + perDay);
        double interestPerDay = interestRateInFraction * perDay;

        principal = this.fixedDepositAccountHelper.getPrincipalAfterCompoundingInterest(todaysDate, principal, depositPeriod,
                interestPerDay, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);

        fixedDepositAccountData = this.fixedDepositAccountHelper.getFixedDepositAccountById(this.requestSpec, this.responseSpec,
                fixedDepositAccountId);
        DecimalFormat decimalFormat = new DecimalFormat("", new DecimalFormatSymbols(Locale.US));
        decimalFormat.applyPattern(".");
        Float expectedPrematureAmount = new Float(decimalFormat.format(principal));
        Float maturityAmount = new Float(decimalFormat.format(fixedDepositAccountData.get("maturityAmount")));

        Assert.assertEquals("Verifying Pre-Closure maturity amount", expectedPrematureAmount, maturityAmount);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo, final String accountingRule,
            Account... accounts) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        if (accountingRule.equals(CASH_BASED)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsCashBased(accounts);
        } else if (accountingRule.equals(NONE)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsNone();
        }
        final String fixedDepositProductJSON = fixedDepositProductHelper.withPeriodRangeChart() //
                .build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    
    private Integer createFixedDepositProductWithWithHoldTax(final String validFrom, final String validTo, final String taxGroupId,
            final String accountingRule, Account... accounts) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        if (accountingRule.equals(CASH_BASED)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsCashBased(accounts);
        } else if (accountingRule.equals(NONE)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsNone();
        }
        final String fixedDepositProductJSON = fixedDepositProductHelper.withPeriodRangeChart() //
                .withWithHoldTax(taxGroupId)//
                .build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }
    
    private Integer createFixedDepositProduct(final String validFrom, final String validTo, final String accountingRule,
            final String chartToBePicked, Account... accounts) {
        System.out.println("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(this.requestSpec, this.responseSpec);
        if (accountingRule.equals(CASH_BASED)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsCashBased(accounts);
        } else if (accountingRule.equals(NONE)) {
            fixedDepositProductHelper = fixedDepositProductHelper.withAccountingRuleAsNone();
        }
        switch (chartToBePicked) {
            case "period":
                fixedDepositProductHelper = fixedDepositProductHelper.withPeriodRangeChart();
            break;
            case "amount":
                fixedDepositProductHelper = fixedDepositProductHelper.withAmountRangeChart();
            break;
            case "period_amount":
                fixedDepositProductHelper = fixedDepositProductHelper.withPeriodAndAmountRangeChart();
            break;
            case "amount_period":
                fixedDepositProductHelper = fixedDepositProductHelper.withAmountAndPeriodRangeChart();
            break;
            default:
            break;
        }

        final String fixedDepositProductJSON = fixedDepositProductHelper //
                .build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String submittedOnDate,
            final String penalInterestType) {
        System.out.println("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec) //
                .withSubmittedOnDate(submittedOnDate).build(clientID, productID, penalInterestType);
        return this.fixedDepositAccountHelper
                .applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec, this.responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String submittedOnDate,
            final String penalInterestType, final String depositAmount, final String depositPeriod) {
        System.out.println("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(this.requestSpec, this.responseSpec)
                //
                .withSubmittedOnDate(submittedOnDate).withDepositPeriod(depositPeriod).withDepositAmount(depositAmount)
                .build(clientID, productID, penalInterestType);
        return this.fixedDepositAccountHelper
                .applyFixedDepositApplication(fixedDepositApplicationJSON, this.requestSpec, this.responseSpec);
    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, final String accountingRule, Account... accounts) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");

        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        if (accountingRule.equals(CASH_BASED)) {
            savingsProductHelper = savingsProductHelper.withAccountingRuleAsCashBased(accounts);
        } else if (accountingRule.equals(NONE)) {
            savingsProductHelper = savingsProductHelper.withAccountingRuleAsNone();
        }

        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Account getMappedLiabilityFinancialAccount() {
        final Integer liabilityTransferFinancialActivityId = FINANCIAL_ACTIVITY.LIABILITY_TRANSFER.getValue();
        List<HashMap> financialActivities = this.financialActivityAccountHelper.getAllFinancialActivityAccounts(this.responseSpec);
        final Account financialAccount;
        /***
         * if no financial activities are defined for account transfers, create
         * liability financial accounting mappings
         */
        if (financialActivities.isEmpty()) {
            financialAccount = createLiabilityFinancialAccountTransferType(liabilityTransferFinancialActivityId);
        } else {
            /***
             * extract mapped liability financial account
             */
            Account mappedLiabilityAccount = null;
            for (HashMap financialActivity : financialActivities) {
                HashMap financialActivityData = (HashMap) financialActivity.get("financialActivityData");
                if (financialActivityData.get("id").equals(liabilityTransferFinancialActivityId)) {
                    HashMap glAccountData = (HashMap) financialActivity.get("glAccountData");
                    mappedLiabilityAccount = new Account((Integer) glAccountData.get("id"), AccountType.LIABILITY);
                    break;
                }
            }
            /***
             * If liability transfer is not defined create liability transfer
             */
            if (mappedLiabilityAccount == null) {
                mappedLiabilityAccount = createLiabilityFinancialAccountTransferType(liabilityTransferFinancialActivityId);
            }
            financialAccount = mappedLiabilityAccount;
        }
        return financialAccount;
    }

    private Account createLiabilityFinancialAccountTransferType(final Integer liabilityTransferFinancialActivityId) {
        /***
         * Create and verify financial account transfer type is created
         */
        final Account liabilityAccountForMapping = this.accountHelper.createLiabilityAccount();
        Integer financialActivityAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                liabilityTransferFinancialActivityId, liabilityAccountForMapping.getAccountID(), this.responseSpec,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(financialActivityAccountId);
        assertFinancialActivityAccountCreation(financialActivityAccountId, liabilityTransferFinancialActivityId, liabilityAccountForMapping);
        return liabilityAccountForMapping;
    }

    private void assertFinancialActivityAccountCreation(Integer financialActivityAccountId, Integer financialActivityId, Account glAccount) {
        HashMap mappingDetails = this.financialActivityAccountHelper.getFinancialActivityAccount(financialActivityAccountId,
                this.responseSpec);
        Assert.assertEquals(financialActivityId, ((HashMap) mappingDetails.get("financialActivityData")).get("id"));
        Assert.assertEquals(glAccount.getAccountID(), ((HashMap) mappingDetails.get("glAccountData")).get("id"));
    }
    
    private Integer createTaxGroup(final String percentage, final Account liabilityAccountForTax){
        final Integer liabilityAccountId = liabilityAccountForTax.getAccountID();
        final Integer taxComponentId = TaxComponentHelper.createTaxComponent(this.requestSpec, this.responseSpec, percentage, liabilityAccountId);
        return TaxGroupHelper.createTaxGroup(this.requestSpec, this.responseSpec, Arrays.asList(taxComponentId));
    }

    /**
     * Delete the Liability transfer account
     */
    @After
    public void tearDown() {
        List<HashMap> financialActivities = this.financialActivityAccountHelper.getAllFinancialActivityAccounts(this.responseSpec);
        for (HashMap financialActivity : financialActivities) {
            Integer financialActivityAccountId = (Integer) financialActivity.get("id");
            Integer deletedFinancialActivityAccountId = this.financialActivityAccountHelper.deleteFinancialActivityAccount(
                    financialActivityAccountId, this.responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
            Assert.assertNotNull(deletedFinancialActivityAccountId);
            Assert.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
        }
    }
}