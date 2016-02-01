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

import java.util.HashMap;
import java.util.List;

import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.accounting.Account.AccountType;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.savings.AccountTransferHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * JUnit Test Cases for Account Transfer for.
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class AccountTransferTest {

    public static final String MINIMUM_OPENING_BALANCE = "30000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String ACCOUNT_TRANSFER_AMOUNT = "15000.0";
    public static final String ACCOUNT_TRANSFER_AMOUNT_ADJUST = "3000.0";
    public static final String FROM_LOAN_ACCOUNT_TYPE = "1";
    public static final String FROM_SAVINGS_ACCOUNT_TYPE = "2";
    public static final String TO_LOAN_ACCOUNT_TYPE = "1";
    public static final String TO_SAVINGS_ACCOUNT_TYPE = "2";

    public static final String LOAN_APPROVAL_DATE = "01 March 2013";
    public static final String LOAN_APPROVAL_DATE_PLUS_ONE = "02 March 2013";
    public static final String LOAN_DISBURSAL_DATE = "01 March 2013";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private AccountTransferHelper accountTransferHelper;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;

    Float TRANSFER_AMOUNT = new Float(ACCOUNT_TRANSFER_AMOUNT);
    Float TRANSFER_AMOUNT_ADJUST = new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST);

    private FinancialActivityAccountHelper financialActivityAccountHelper;
    private Integer financialActivityAccountId;
    private Account liabilityTransferAccount;

    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.financialActivityAccountHelper = new FinancialActivityAccountHelper(this.requestSpec);

        List<HashMap> financialActivities = this.financialActivityAccountHelper.getAllFinancialActivityAccounts(this.responseSpec);
        if (financialActivities.isEmpty()) {
            /** Setup liability transfer account **/
            /** Create a Liability and an Asset Transfer Account **/
            liabilityTransferAccount = accountHelper.createLiabilityAccount();
            Assert.assertNotNull(liabilityTransferAccount);

            /*** Create A Financial Activity to Account Mapping **/
            financialActivityAccountId = (Integer) financialActivityAccountHelper.createFinancialActivityAccount(
                    FinancialActivityAccountsTest.liabilityTransferFinancialActivityId, liabilityTransferAccount.getAccountID(),
                    responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
            Assert.assertNotNull(financialActivityAccountId);
        } else {
            for (HashMap financialActivity : financialActivities) {
                HashMap financialActivityData = (HashMap) financialActivity.get("financialActivityData");
                if (financialActivityData.get("id").equals(FinancialActivityAccountsTest.liabilityTransferFinancialActivityId)) {
                    HashMap glAccountData = (HashMap) financialActivity.get("glAccountData");
                    liabilityTransferAccount = new Account((Integer) glAccountData.get("id"), AccountType.LIABILITY);
                    financialActivityAccountId = (Integer) financialActivity.get("id");
                    break;
                }
            }
        }
    }

    /**
     * Delete the Liability transfer account
     */
    @After
    public void tearDown() {
        Integer deletedFinancialActivityAccountId = financialActivityAccountHelper.deleteFinancialActivityAccount(
                financialActivityAccountId, responseSpec, CommonConstants.RESPONSE_RESOURCE_ID);
        Assert.assertNotNull(deletedFinancialActivityAccountId);
        Assert.assertEquals(financialActivityAccountId, deletedFinancialActivityAccountId);
    }

    @Test
    public void testFromSavingsToSavingsAccountTransfer() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        OfficeHelper officeHelper = new OfficeHelper(this.requestSpec, this.responseSpec);
        Integer toOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(toOfficeId);

        // Creating Savings Account to which fund to be Transferred
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(toOfficeId));
        Assert.assertNotNull(toClientID);

        final Integer toSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, assetAccount,
                incomeAccount, expenseAccount, liabilityAccount);
        Assert.assertNotNull(toSavingsProductID);

        final Integer toSavingsID = this.savingsAccountHelper.applyForSavingsApplication(toClientID, toSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(toSavingsProductID);

        HashMap toSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, toSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(toSavingsStatusHashMap);

        final HashMap toSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        Integer fromOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(fromOfficeId);

        // Creating Savings Account from which the Fund has to be Transferred
        final Integer fromClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(fromOfficeId));
        Assert.assertNotNull(fromClientID);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                assetAccount, incomeAccount, expenseAccount, liabilityAccount);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientID, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap fromSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);
        Float toSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, fromSavingsID, fromClientID, toSavingsID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT);

        fromSavingsBalance -= new Float(ACCOUNT_TRANSFER_AMOUNT);
        toSavingsBalance += new Float(ACCOUNT_TRANSFER_AMOUNT);

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsID);
        assertEquals("Verifying To Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));
        final JournalEntry[] office1LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT),
                JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] office2LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT),
                JournalEntry.TransactionType.DEBIT) };

        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(fromOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office1LiabilityEntries);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(toOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office2LiabilityEntries);

    }

    @Test
    public void testFromSavingsToLoanAccountTransfer() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Account loanAssetAccount = this.accountHelper.createAssetAccount();
        final Account loanIncomeAccount = this.accountHelper.createIncomeAccount();
        final Account loanExpenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        OfficeHelper officeHelper = new OfficeHelper(this.requestSpec, this.responseSpec);
        Integer toOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(toOfficeId);

        // Creating Loan Account to which fund to be Transferred
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(toOfficeId));
        Assert.assertNotNull(toClientID);

        Account toTransferAccount = accountHelper.createLiabilityAccount();
        Assert.assertNotNull(toTransferAccount);

        final Integer toLoanProductID = createLoanProduct(loanAssetAccount, loanIncomeAccount, loanExpenseAccount, overpaymentAccount);
        Assert.assertNotNull(toLoanProductID);

        final Integer toLoanID = applyForLoanApplication(toClientID, toLoanProductID);
        Assert.assertNotNull(toLoanID);

        HashMap toLoanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, toLoanID);
        LoanStatusChecker.verifyLoanIsPending(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsApproved(toLoanStatusHashMap);

        toLoanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSAL_DATE, toLoanID);
        LoanStatusChecker.verifyLoanIsActive(toLoanStatusHashMap);

        Integer fromOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(fromOfficeId);

        // Creating Savings Account from which the Fund has to be Transferred
        final Integer fromClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(fromOfficeId));
        Assert.assertNotNull(fromClientID);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                assetAccount, incomeAccount, expenseAccount, liabilityAccount);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientID, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap fromSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, fromSavingsID, toClientID, toLoanID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_LOAN_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT_ADJUST);

        fromSavingsBalance -= TRANSFER_AMOUNT_ADJUST;

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        HashMap toLoanSummaryAfter = this.loanTransactionHelper.getLoanSummary(requestSpec, responseSpec, toLoanID);
        assertEquals("Verifying To Loan Repayment Amount after Account Transfer", TRANSFER_AMOUNT_ADJUST,
                toLoanSummaryAfter.get("totalRepayment"));

        final JournalEntry[] office1LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST),
                JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] office2LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST),
                JournalEntry.TransactionType.DEBIT) };

        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(fromOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office1LiabilityEntries);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(toOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office2LiabilityEntries);
    }

    @Test
    public void testFromLoanToSavingsAccountTransfer() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Account loanAssetAccount = this.accountHelper.createAssetAccount();
        final Account loanIncomeAccount = this.accountHelper.createIncomeAccount();
        final Account loanExpenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountTransferHelper = new AccountTransferHelper(this.requestSpec, this.responseSpec);

        OfficeHelper officeHelper = new OfficeHelper(this.requestSpec, this.responseSpec);
        Integer toOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(toOfficeId);

        // Creating Loan Account to which fund to be Transferred
        final Integer toClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(toOfficeId));
        Assert.assertNotNull(toClientID);

        final Integer toSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE, assetAccount,
                incomeAccount, expenseAccount, liabilityAccount);
        Assert.assertNotNull(toSavingsProductID);

        final Integer toSavingsID = this.savingsAccountHelper.applyForSavingsApplication(toClientID, toSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(toSavingsID);

        HashMap toSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, toSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(toSavingsStatusHashMap);

        toSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(toSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(toSavingsStatusHashMap);

        Integer fromOfficeId = officeHelper.createOffice("01 January 2011");
        Assert.assertNotNull(fromOfficeId);

        // Creating Savings Account from which the Fund has to be Transferred
        final Integer fromClientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2011",
                String.valueOf(fromOfficeId));
        Assert.assertNotNull(fromClientID);

        final Integer loanProductID = createLoanProduct(loanAssetAccount, loanIncomeAccount, loanExpenseAccount, overpaymentAccount);
        Assert.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(fromClientID, loanProductID);
        Assert.assertNotNull(loanID);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(LOAN_APPROVAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.disburseLoan(LOAN_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final Integer fromSavingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, MINIMUM_OPENING_BALANCE,
                assetAccount, incomeAccount, expenseAccount, liabilityAccount);
        Assert.assertNotNull(fromSavingsProductID);

        final Integer fromSavingsID = this.savingsAccountHelper.applyForSavingsApplication(fromClientID, fromSavingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL);
        Assert.assertNotNull(fromSavingsID);

        HashMap fromSavingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec, fromSavingsID);
        SavingsStatusChecker.verifySavingsIsPending(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.approveSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsApproved(fromSavingsStatusHashMap);

        fromSavingsStatusHashMap = this.savingsAccountHelper.activateSavings(fromSavingsID);
        SavingsStatusChecker.verifySavingsIsActive(fromSavingsStatusHashMap);

        final HashMap toSavingsSummaryBefore = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        Float fromSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, fromSavingsID, fromClientID, loanID, FROM_SAVINGS_ACCOUNT_TYPE,
                TO_LOAN_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT);

        fromSavingsBalance -= TRANSFER_AMOUNT;

        HashMap fromSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(fromSavingsID);

        // Verifying fromSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", fromSavingsBalance,
                fromSavingsSummaryAfter.get("accountBalance"));

        Float toSavingsBalance = new Float(MINIMUM_OPENING_BALANCE);

        this.accountTransferHelper.accountTransfer(fromClientID, loanID, toClientID, toSavingsID, FROM_LOAN_ACCOUNT_TYPE,
                TO_SAVINGS_ACCOUNT_TYPE, ACCOUNT_TRANSFER_AMOUNT_ADJUST);

        toSavingsBalance += TRANSFER_AMOUNT_ADJUST;

        HashMap toSavingsSummaryAfter = this.savingsAccountHelper.getSavingsSummary(toSavingsID);

        // Verifying toSavings Account Balance after Account Transfer
        assertEquals("Verifying From Savings Account Balance after Account Transfer", toSavingsBalance,
                toSavingsSummaryAfter.get("accountBalance"));

        final JournalEntry[] office1LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST),
                JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] office2LiabilityEntries = { new JournalEntry(new Float(ACCOUNT_TRANSFER_AMOUNT_ADJUST),
                JournalEntry.TransactionType.DEBIT) };

        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(fromOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office1LiabilityEntries);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(toOfficeId, liabilityTransferAccount,
                AccountTransferHelper.ACCOUNT_TRANSFER_DATE, office2LiabilityEntries);

    }

    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, final Account... accounts) {
        System.out.println("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        final String savingsProductJSON = savingsProductHelper //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createLoanProduct(final Account... accounts) {
        System.out.println("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("8,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withAccountingRuleAsCashBased(accounts)//
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID) {
        System.out.println("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal("8,000.00") //
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
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }
}