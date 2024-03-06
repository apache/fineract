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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetRecurringDepositProductsProductIdResponse;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
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
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountHelper;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositAccountStatusChecker;
import org.apache.fineract.integrationtests.common.recurringdeposit.RecurringDepositProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.shares.ShareAccountHelper;
import org.apache.fineract.integrationtests.common.shares.ShareAccountTransactionHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductHelper;
import org.apache.fineract.integrationtests.common.shares.ShareProductTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class AccountingScenarioIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AccountingScenarioIntegrationTest.class);
    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec;

    private static final String DATE_OF_JOINING = "01 January 2011";

    private static final Float LP_PRINCIPAL = 10000.0f;
    private static final String LP_REPAYMENTS = "5";
    private static final String LP_REPAYMENT_PERIOD = "2";
    private static final String LP_INTEREST_RATE = "1";
    private static final String EXPECTED_DISBURSAL_DATE = "04 March 2011";
    private static final String LOAN_APPLICATION_SUBMISSION_DATE = "03 March 2011";
    private static final String TRANSACTION_DATE = "01 March 2013";
    private static final String LOAN_TERM_FREQUENCY = "10";
    private static final String INDIVIDUAL_LOAN = "individual";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String DEPOSIT_AMOUNT = "7000";
    public static final String WITHDRAWAL_AMOUNT = "3000";
    public static final String WITHDRAWAL_AMOUNT_ADJUSTED = "2000";

    static Float SP_BALANCE = Float.valueOf(MINIMUM_OPENING_BALANCE);
    static Float SP_DEPOSIT_AMOUNT = Float.valueOf(DEPOSIT_AMOUNT);
    static Float SP_WITHDRAWAL_AMOUNT = Float.valueOf(WITHDRAWAL_AMOUNT);
    static Float SP_WITHDRAWAL_AMOUNT_ADJUSTED = Float.valueOf(WITHDRAWAL_AMOUNT_ADJUSTED);

    private static final String[] REPAYMENT_DATE = { "", "04 May 2011", "04 July 2011", "04 September 2011", "04 November 2011",
            "04 January 2012" };
    private static final Float[] REPAYMENT_AMOUNT = { .0f, 2200.0f, 3000.0f, 900.0f, 2000.0f, 2500.0f };

    private static final Float AMOUNT_TO_BE_WAIVE = 400.0f;
    private LoanTransactionHelper loanTransactionHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private FixedDepositAccountHelper fixedDepositAccountHelper;
    private RecurringDepositAccountHelper recurringDepositAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;

    private TimeZone tenantTimeZone;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(requestSpec, responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);

        this.tenantTimeZone = TimeZone.getTimeZone(Utils.TENANT_TIME_ZONE);
    }

    @Test
    public void checkUpfrontAccrualAccountingFlow() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithUpfrontAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(EXPECTED_DISBURSAL_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);
        LOG.info("CHECKING INCOME: ******************************************");
        final JournalEntry incomeJournalEntry = new JournalEntry(TOTAL_INTEREST, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, EXPECTED_DISBURSAL_DATE, incomeJournalEntry);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_INTEREST + FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], assetAccountFirstEntry);
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], assetAccountSecondEntry);
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        this.loanTransactionHelper.waiveInterest(REPAYMENT_DATE[4], AMOUNT_TO_BE_WAIVE.toString(), loanID);

        final JournalEntry waivedEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], waivedEntry);

        final JournalEntry expenseJournalEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, REPAYMENT_DATE[4], expenseJournalEntry);
        LOG.info("Waive Interest Done......");

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], assetAccountThirdEntry);
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], assetAccountFourthEntry);
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], assetAccountFifthEntry);
        LOG.info("Repayment 5 Done  ......");
    }

    private Integer createLoanProductWithUpfrontAccrualAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleUpfrontAccrual(accounts)
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(LP_PRINCIPAL.toString())
                .withLoanTermFrequency(LOAN_TERM_FREQUENCY).withLoanTermFrequencyAsMonths().withNumberOfRepayments(LP_REPAYMENTS)
                .withRepaymentEveryAfter(LP_REPAYMENT_PERIOD).withRepaymentFrequencyTypeAsMonths()
                .withInterestRatePerPeriod(LP_INTEREST_RATE).withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(EXPECTED_DISBURSAL_DATE)
                .withSubmittedOnDate(LOAN_APPLICATION_SUBMISSION_DATE).withLoanType(INDIVIDUAL_LOAN).withCollaterals(collaterals)
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    @Test
    public void checkAccountingWithSavingsFlow() {

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer savingsProductID = createSavingsProduct(MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);
        final Integer savingsID = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(requestSpec, responseSpec, savingsID);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Checking initial Account entries.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(SP_BALANCE, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(SP_BALANCE, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, liablilityAccountInitialEntry);

        // First Transaction-Deposit
        this.savingsAccountHelper.depositToSavingsAccount(savingsID, DEPOSIT_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Float balance = SP_BALANCE + SP_DEPOSIT_AMOUNT;
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance after Deposit");

        LOG.info("----------------------Verifying Journal Entry after the Transaction Deposit----------------------------");
        final JournalEntry[] assetAccountFirstTransactionEntry = {
                new JournalEntry(SP_DEPOSIT_AMOUNT, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liabililityAccountFirstTransactionEntry = {
                new JournalEntry(SP_DEPOSIT_AMOUNT, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountFirstTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountFirstTransactionEntry);

        // Second Transaction-Withdrawal
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        balance -= SP_WITHDRAWAL_AMOUNT;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance after Withdrawal");

        LOG.info("-------------------Verifying Journal Entry after the Transaction Withdrawal----------------------");
        final JournalEntry[] assetAccountSecondTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT, JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] liabililityAccountSecondTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT, JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountSecondTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountSecondTransactionEntry);

        // Third Transaction-Add Charges for Withdrawal Fee
        final Integer withdrawalChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assertions.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsID, withdrawalChargeId, false);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsID);
        assertEquals(1, chargesPendingState.size());
        HashMap savingsChargeForPay = chargesPendingState.get(0);
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsID, (Integer) savingsChargeForPay.get("id"));
        Float chargeAmount = (Float) paidCharge.get("amount");

        // Withdrawal after adding Charge of type Withdrawal Fee
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT_ADJUSTED, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        balance = balance - SP_WITHDRAWAL_AMOUNT_ADJUSTED - chargeAmount;

        final JournalEntry[] liabililityAccountThirdTransactionEntry = { new JournalEntry(chargeAmount, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SP_WITHDRAWAL_AMOUNT_ADJUSTED, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] assetAccountThirdTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT_ADJUSTED, JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] incomeAccountThirdTransactionEntry = { new JournalEntry(chargeAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, TRANSACTION_DATE, incomeAccountThirdTransactionEntry);

        // Verifying Balance after applying Charge for Withdrawal Fee
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance");
    }

    @Test
    public void checkAccountingWithSavingsFlowUsingAccrualAccounting() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer savingsProductID = createSavingsProductWithAccrualAccounting(MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount,
                expenseAccount, liabilityAccount);
        final GetSavingsProductsProductIdResponse savingsProductsResponse = SavingsProductHelper.getSavingsProductById(requestSpec,
                responseSpec, savingsProductID);
        Assertions.assertNotNull(savingsProductsResponse);
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getSavingsControlAccount());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getInterestPayableAccount());

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);
        final Integer savingsID = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);

        HashMap savingsStatusHashMap = SavingsStatusChecker.getStatusOfSavings(requestSpec, responseSpec, savingsID);
        SavingsStatusChecker.verifySavingsIsPending(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.approveSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);

        savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsID);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        // Checking initial Account entries.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(SP_BALANCE, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(SP_BALANCE, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE, liablilityAccountInitialEntry);

        // First Transaction-Deposit
        this.savingsAccountHelper.depositToSavingsAccount(savingsID, DEPOSIT_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        Float balance = SP_BALANCE + SP_DEPOSIT_AMOUNT;
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance after Deposit");

        LOG.info("----------------------Verifying Journal Entry after the Transaction Deposit----------------------------");
        final JournalEntry[] assetAccountFirstTransactionEntry = {
                new JournalEntry(SP_DEPOSIT_AMOUNT, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liabililityAccountFirstTransactionEntry = {
                new JournalEntry(SP_DEPOSIT_AMOUNT, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountFirstTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountFirstTransactionEntry);

        // Second Transaction-Withdrawal
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        balance -= SP_WITHDRAWAL_AMOUNT;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance after Withdrawal");

        LOG.info("-------------------Verifying Journal Entry after the Transaction Withdrawal----------------------");
        final JournalEntry[] assetAccountSecondTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT, JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] liabililityAccountSecondTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT, JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountSecondTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountSecondTransactionEntry);

        // Third Transaction-Add Charges for Withdrawal Fee
        final Integer withdrawalChargeId = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assertions.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsID, withdrawalChargeId, false);
        ArrayList<HashMap> chargesPendingState = this.savingsAccountHelper.getSavingsCharges(savingsID);
        Assertions.assertEquals(1, chargesPendingState.size());
        HashMap savingsChargeForPay = chargesPendingState.get(0);
        HashMap paidCharge = this.savingsAccountHelper.getSavingsCharge(savingsID, (Integer) savingsChargeForPay.get("id"));
        Float chargeAmount = (Float) paidCharge.get("amount");

        // Withdrawal after adding Charge of type Withdrawal Fee
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsID, WITHDRAWAL_AMOUNT_ADJUSTED, SavingsAccountHelper.TRANSACTION_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsID);
        balance = balance - SP_WITHDRAWAL_AMOUNT_ADJUSTED - chargeAmount;

        final JournalEntry[] liabililityAccountThirdTransactionEntry = { new JournalEntry(chargeAmount, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SP_WITHDRAWAL_AMOUNT_ADJUSTED, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] assetAccountThirdTransactionEntry = {
                new JournalEntry(SP_WITHDRAWAL_AMOUNT_ADJUSTED, JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] incomeAccountThirdTransactionEntry = { new JournalEntry(chargeAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, TRANSACTION_DATE, assetAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, TRANSACTION_DATE,
                liabililityAccountThirdTransactionEntry);
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, TRANSACTION_DATE, incomeAccountThirdTransactionEntry);

        // Verifying Balance after applying Charge for Withdrawal Fee
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance");
    }

    @Test
    public void testFixedDepositAccountingFlow() {
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.fixedDepositAccountHelper = new FixedDepositAccountHelper(requestSpec, responseSpec);

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(3);
        final String VALID_FROM = dateFormat.format(todaysDate);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = dateFormat.format(todaysDate);

        todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate);
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate);

        todaysDate = todaysDate.plusMonths(1).withDayOfMonth(1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientId);
        // Assertions.assertNotNull(clientId);

        Integer fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Integer fixedDepositAccountId = applyForFixedDepositApplication(clientId.toString(), fixedDepositProductId.toString(), VALID_FROM,
                VALID_TO, SUBMITTED_ON_DATE, FixedDepositTest.WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        HashMap fixedDepositAccountStatusHashMap = FixedDepositAccountStatusChecker.getStatusOfFixedDepositAccount(requestSpec,
                responseSpec, fixedDepositAccountId.toString());
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
        Assertions.assertNotNull(transactionIdForPostInterest);

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
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.recurringDepositAccountHelper = new RecurringDepositAccountHelper(requestSpec, responseSpec);

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        LocalDate todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(3);
        final String VALID_FROM = dateFormat.format(todaysDate);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = dateFormat.format(todaysDate);

        todaysDate = Utils.getLocalDateOfTenant();
        todaysDate = todaysDate.minusMonths(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate);
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate);
        final String ACTIVATION_DATE = dateFormat.format(todaysDate);
        final String EXPECTED_FIRST_DEPOSIT_ON_DATE = dateFormat.format(todaysDate);

        todaysDate = todaysDate.plusMonths(1).withDayOfMonth(1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate);

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientId);

        Integer recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);
        final GetRecurringDepositProductsProductIdResponse recurringDepositProductsProduct = RecurringDepositProductHelper
                .getRecurringDepositProductById(requestSpec, responseSpec, recurringDepositProductId);
        Assertions.assertNotNull(recurringDepositProductsProduct);
        Assertions.assertNotNull(recurringDepositProductsProduct.getAccountingMappings());
        Assertions.assertNotNull(recurringDepositProductsProduct.getAccountingMappings().getSavingsControlAccount());
        Assertions.assertNull(recurringDepositProductsProduct.getAccountingMappings().getInterestPayableAccount());

        Integer recurringDepositAccountId = applyForRecurringDepositApplication(clientId.toString(), recurringDepositProductId.toString(),
                VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, RecurringDepositTest.WHOLE_TERM, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assertions.assertNotNull(recurringDepositAccountId);

        HashMap recurringDepositAccountStatusHashMap = RecurringDepositAccountStatusChecker.getStatusOfRecurringDepositAccount(requestSpec,
                responseSpec, recurringDepositAccountId.toString());
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsPending(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.approveRecurringDeposit(recurringDepositAccountId,
                APPROVED_ON_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsApproved(recurringDepositAccountStatusHashMap);

        recurringDepositAccountStatusHashMap = this.recurringDepositAccountHelper.activateRecurringDeposit(recurringDepositAccountId,
                ACTIVATION_DATE);
        RecurringDepositAccountStatusChecker.verifyRecurringDepositIsActive(recurringDepositAccountStatusHashMap);

        HashMap recurringDepositAccountData = RecurringDepositAccountHelper.getRecurringDepositAccountById(requestSpec, responseSpec,
                recurringDepositAccountId);
        Float depositAmount = (Float) recurringDepositAccountData.get("mandatoryRecommendedDepositAmount");

        Integer depositTransactionId = this.recurringDepositAccountHelper.depositToRecurringDepositAccount(recurringDepositAccountId,
                depositAmount, EXPECTED_FIRST_DEPOSIT_ON_DATE);
        Assertions.assertNotNull(depositTransactionId);

        // Checking initial Journal entries after Activation.
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountInitialEntry = { new JournalEntry(depositAmount, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE, assetAccountInitialEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, EXPECTED_FIRST_DEPOSIT_ON_DATE,
                liablilityAccountInitialEntry);

        Integer interestPostingTransactionId = this.recurringDepositAccountHelper
                .postInterestForRecurringDeposit(recurringDepositAccountId);
        Assertions.assertNotNull(interestPostingTransactionId);

        HashMap accountSummary = this.recurringDepositAccountHelper.getRecurringDepositSummary(recurringDepositAccountId);
        Float totalInterestPosted = (Float) accountSummary.get("totalInterestPosted");

        // Checking initial Journal entries after Interest Posting.
        final JournalEntry[] expenseAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liablilityAccountEntry = { new JournalEntry(totalInterestPosted, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE, expenseAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE, liablilityAccountEntry);

    }

    public static Integer createSavingsProduct(final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsCashBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createFixedDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        LOG.info("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        FixedDepositProductHelper fixedDepositProductHelper = new FixedDepositProductHelper(requestSpec, responseSpec);
        final String fixedDepositProductJSON = fixedDepositProductHelper //
                .withPeriodRangeChart()//
                .withAccountingRuleAsCashBased(accounts).build(validFrom, validTo);
        return FixedDepositProductHelper.createFixedDepositProduct(fixedDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForFixedDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType) {
        LOG.info("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        final String fixedDepositApplicationJSON = new FixedDepositAccountHelper(requestSpec, responseSpec) //
                .withSubmittedOnDate(submittedOnDate).build(clientID, productID, penalInterestType);
        return FixedDepositAccountHelper.applyFixedDepositApplicationGetId(fixedDepositApplicationJSON, requestSpec, responseSpec);
    }

    private Integer createRecurringDepositProduct(final String validFrom, final String validTo, Account... accounts) {
        LOG.info("------------------------------CREATING NEW RECURRING DEPOSIT PRODUCT ---------------------------------------");
        RecurringDepositProductHelper recurringDepositProductHelper = new RecurringDepositProductHelper(requestSpec, responseSpec);
        final String recurringDepositProductJSON = recurringDepositProductHelper //
                .withPeriodRangeChart()//
                .withAccountingRuleAsCashBased(accounts).build(validFrom, validTo);
        return RecurringDepositProductHelper.createRecurringDepositProduct(recurringDepositProductJSON, requestSpec, responseSpec);
    }

    private Integer applyForRecurringDepositApplication(final String clientID, final String productID, final String validFrom,
            final String validTo, final String submittedOnDate, final String penalInterestType, final String expectedFirstDepositOnDate) {
        LOG.info("--------------------------------APPLYING FOR RECURRING DEPOSIT ACCOUNT --------------------------------");
        final String recurringDepositApplicationJSON = new RecurringDepositAccountHelper(requestSpec, responseSpec)
                //
                .withSubmittedOnDate(submittedOnDate).withExpectedFirstDepositOnDate(expectedFirstDepositOnDate)
                .build(clientID, productID, penalInterestType);
        return RecurringDepositAccountHelper.applyRecurringDepositApplication(recurringDepositApplicationJSON, requestSpec, responseSpec);
    }

    public static Integer createSavingsProductWithAccrualAccounting(final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsAccrualBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow() throws InterruptedException, ParseException {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(EXPECTED_DISBURSAL_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        final String jobName = "Add Accrual Transactions";

        this.schedulerJobHelper.executeAndAwaitJob(jobName);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[1]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_INTEREST + FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], assetAccountFirstEntry);
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[2]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[3]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_AND_THIRD_INTEREST + SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], assetAccountSecondEntry);
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[4]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[5]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.waiveInterest(REPAYMENT_DATE[4], AMOUNT_TO_BE_WAIVE.toString(), loanID);

        final JournalEntry waivedEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.CREDIT);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], waivedEntry);

        final JournalEntry expenseJournalEntry = new JournalEntry(AMOUNT_TO_BE_WAIVE, JournalEntry.TransactionType.DEBIT);
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, REPAYMENT_DATE[4], expenseJournalEntry);
        LOG.info("Waive Interest Done......");

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], assetAccountThirdEntry);
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], assetAccountFourthEntry);
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], assetAccountFifthEntry);
        LOG.info("Repayment 5 Done  ......");
    }

    @Test
    public void checkPeriodicAccrualAccountingFlow_OVER_PAYMENT() throws InterruptedException, ParseException {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(EXPECTED_DISBURSAL_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        final String jobName = "Add Accrual Transactions";

        this.schedulerJobHelper.executeAndAwaitJob(jobName);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        final float FEE_PORTION = 0.0f;
        final float PENALTY_PORTION = 0.0f;
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(REPAYMENT_DATE[1]), FIRST_INTEREST, FEE_PORTION,
                PENALTY_PORTION, loanID);
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[1], 15000f, loanID);
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountEntry = { new JournalEntry(15000f, JournalEntry.TransactionType.DEBIT),
                new JournalEntry(11000f, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], assetAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, REPAYMENT_DATE[1],
                new JournalEntry(4000f, JournalEntry.TransactionType.CREDIT));
        LOG.info("Repayment  Done......");

    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    @Test
    public void checkPeriodicAccrualAccountingTillCurrentDateFlow() throws InterruptedException, ParseException {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

        final float FEE_PORTION = 50.0f;
        final float PENALTY_PORTION = 100.0f;
        Integer flat = ChargesHelper.createCharges(requestSpec, responseSpec,
                ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(FEE_PORTION), false));
        Integer flatSpecifiedDueDate = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, String.valueOf(PENALTY_PORTION), true));

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

        final LocalDate localDate = LocalDate.now(this.tenantTimeZone.toZoneId());
        final ZonedDateTime currentDate = ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, this.tenantTimeZone.toZoneId());
        ZonedDateTime zonedDate = currentDate.minusDays(4);
        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(zonedDate);

        zonedDate = currentDate.minusDays(2);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), dateFormat.format(zonedDate), String.valueOf(PENALTY_PORTION)));
        zonedDate = zonedDate.plusDays(1);
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), dateFormat.format(zonedDate), String.valueOf(FEE_PORTION)));

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);

        final String jobName = "Add Periodic Accrual Transactions";

        this.schedulerJobHelper.executeAndAwaitJob(jobName);

        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        // MAKE 1
        List fromDateList = (List) loanSchedule.get(1).get("fromDate");
        LocalDate fromDateLocal = LocalDate.now(Utils.getZoneIdOfTenant());
        fromDateLocal = fromDateLocal.withYear((int) fromDateList.get(0));
        fromDateLocal = fromDateLocal.withMonth((int) fromDateList.get(1));
        fromDateLocal = fromDateLocal.withDayOfMonth((int) fromDateList.get(2));

        List dueDateList = (List) loanSchedule.get(1).get("dueDate");
        LocalDate dueDateLocal = LocalDate.now(Utils.getZoneIdOfTenant());
        dueDateLocal = dueDateLocal.withYear((int) dueDateList.get(0));
        dueDateLocal = dueDateLocal.withMonth((int) dueDateList.get(1));
        dueDateLocal = dueDateLocal.withDayOfMonth((int) dueDateList.get(2));

        int totalDaysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(fromDateLocal, dueDateLocal));

        float totalInterest = (float) loanSchedule.get(1).get("interestOriginalDue");
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float interest4Days = totalInterest / totalDaysInPeriod * 4;
        interest4Days = Float.parseFloat(numberFormat.format(interest4Days));

        this.loanTransactionHelper.checkAccrualTransactionForRepayment(currentDate.toLocalDate(), interest4Days, FEE_PORTION,
                PENALTY_PORTION, loanID);

    }

    @Test
    public void checkPeriodicAccrualAccountingAPIFlow() throws ParseException {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

        final Integer loanProductID = createLoanProductWithPeriodicAccrualAccountingEnabled(assetAccount, incomeAccount, expenseAccount,
                overpaymentAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

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

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance(this.tenantTimeZone);

        todayDate.add(Calendar.DATE, -4);

        final String LOAN_DISBURSEMENT_DATE = dateFormat.format(todayDate.getTime());

        todayDate.add(Calendar.MONTH, 2);

        todayDate = Calendar.getInstance(this.tenantTimeZone);
        todayDate.add(Calendar.DATE, -2);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(LOAN_DISBURSEMENT_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDate), dateFormat.format(todayDate.getTime()), String.valueOf(PENALTY_PORTION)));
        todayDate.add(Calendar.DATE, 1);
        String runOndate = dateFormat.format(todayDate.getTime());

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper
                .getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(flat), runOndate, String.valueOf(FEE_PORTION)));

        todayDate.add(Calendar.DATE, 1);
        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatSpecifiedDueDateNext), dateFormat.format(todayDate.getTime()), String.valueOf(NEXT_PENALTY_PORTION)));

        this.loanTransactionHelper.addChargesForLoan(loanID, LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(
                String.valueOf(flatNext), dateFormat.format(todayDate.getTime()), String.valueOf(NEXT_FEE_PORTION)));

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, LOAN_DISBURSEMENT_DATE, assetAccountInitialEntry);

        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);

        final ArrayList<HashMap> loanSchedule = this.loanTransactionHelper.getLoanRepaymentSchedule(requestSpec, responseSpec, loanID);
        // MAKE 1
        List fromDateList = (List) loanSchedule.get(1).get("fromDate");
        LocalDate fromDateLocal = LocalDate.now(Utils.getZoneIdOfTenant());
        fromDateLocal = fromDateLocal.withYear((int) fromDateList.get(0));
        fromDateLocal = fromDateLocal.withMonth((int) fromDateList.get(1));
        fromDateLocal = fromDateLocal.withDayOfMonth((int) fromDateList.get(2));

        List dueDateList = (List) loanSchedule.get(1).get("dueDate");
        LocalDate dueDateLocal = LocalDate.now(Utils.getZoneIdOfTenant());
        dueDateLocal = dueDateLocal.withYear((int) dueDateList.get(0));
        dueDateLocal = dueDateLocal.withMonth((int) dueDateList.get(1));
        dueDateLocal = dueDateLocal.withDayOfMonth((int) dueDateList.get(2));

        int totalDaysInPeriod = Math.toIntExact(ChronoUnit.DAYS.between(fromDateLocal, dueDateLocal));

        float totalInterest = (float) loanSchedule.get(1).get("interestOriginalDue");
        DecimalFormat numberFormat = new DecimalFormat("#.00", new DecimalFormatSymbols(Locale.US));
        float interest3Days = totalInterest / totalDaysInPeriod * 3;
        interest3Days = Float.parseFloat(numberFormat.format(interest3Days));
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), interest3Days, FEE_PORTION,
                PENALTY_PORTION, loanID);

        runOndate = dateFormat.format(todayDate.getTime());

        this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(runOndate);
        float interestPerDay = (totalInterest / totalDaysInPeriod * 4) - interest3Days;
        interestPerDay = Float.parseFloat(numberFormat.format(interestPerDay));
        this.loanTransactionHelper.checkAccrualTransactionForRepayment(getDateAsLocalDate(runOndate), interestPerDay, NEXT_FEE_PORTION,
                NEXT_PENALTY_PORTION, loanID);

    }

    private Integer createLoanProductWithPeriodicAccrualAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
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

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);

        List<HashMap> collaterals = new ArrayList<>();
        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);
        addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, collaterals);

        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(requestSpec, responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        loanStatusHashMap = this.loanTransactionHelper.approveLoan(EXPECTED_DISBURSAL_DATE, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        String loanDetails = this.loanTransactionHelper.getLoanDetails(requestSpec, responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(EXPECTED_DISBURSAL_DATE, loanID,
                JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        // CHECK ACCOUNT ENTRIES
        LOG.info("Entries ......");
        final float PRINCIPAL_VALUE_FOR_EACH_PERIOD = 2000.0f;
        final float TOTAL_INTEREST = 1000.0f;
        final JournalEntry[] assetAccountInitialEntry = { new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.CREDIT),
                new JournalEntry(LP_PRINCIPAL, JournalEntry.TransactionType.DEBIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, EXPECTED_DISBURSAL_DATE, assetAccountInitialEntry);

        // MAKE 1
        LOG.info("Repayment 1 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[1], REPAYMENT_AMOUNT[1], loanID);
        final float FIRST_INTEREST = 200.0f;
        final float FIRST_PRINCIPAL = 2000.0f;
        float expected_value = LP_PRINCIPAL - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(1, expected_value, loanID);
        final JournalEntry[] assetAccountFirstEntry = { new JournalEntry(REPAYMENT_AMOUNT[1], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(FIRST_PRINCIPAL, JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[1], assetAccountFirstEntry);
        LOG.info("CHECKING INCOME: ******************************************");
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, REPAYMENT_DATE[1],
                new JournalEntry(FIRST_INTEREST, JournalEntry.TransactionType.CREDIT));
        LOG.info("Repayment 1 Done......");

        // REPAYMENT 2
        LOG.info("Repayment 2 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[2], REPAYMENT_AMOUNT[2], loanID);
        final float SECOND_AND_THIRD_INTEREST = 400.0f;
        final float SECOND_PRINCIPAL = REPAYMENT_AMOUNT[2] - SECOND_AND_THIRD_INTEREST;
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(2, expected_value, loanID);
        final JournalEntry[] assetAccountSecondEntry = { new JournalEntry(REPAYMENT_AMOUNT[2], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(SECOND_PRINCIPAL, JournalEntry.TransactionType.CREDIT), };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[2], assetAccountSecondEntry);
        LOG.info("CHECKING INCOME: ******************************************");
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, REPAYMENT_DATE[2],
                new JournalEntry(SECOND_AND_THIRD_INTEREST, JournalEntry.TransactionType.CREDIT));
        LOG.info("Repayment 2 Done ......");

        // WAIVE INTEREST
        LOG.info("Waive Interest  ......");
        Integer transactionId = this.loanTransactionHelper.waiveInterestAndReturnTransactionId(REPAYMENT_DATE[4],
                AMOUNT_TO_BE_WAIVE.toString(), loanID);
        // waive of fees and interest are not considered in cash based
        // accounting,
        this.journalEntryHelper.ensureNoAccountingTransactionsWithTransactionId("L" + transactionId);

        // REPAYMENT 3
        LOG.info("Repayment 3 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[3], REPAYMENT_AMOUNT[3], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        final JournalEntry[] assetAccountThirdEntry = { new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[3], JournalEntry.TransactionType.CREDIT) };
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(3, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[3], assetAccountThirdEntry);
        LOG.info("Repayment 3 Done ......");

        // REPAYMENT 4
        LOG.info("Repayment 4 ......");
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[4], REPAYMENT_AMOUNT[4], loanID);
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(4, expected_value, loanID);
        final JournalEntry[] assetAccountFourthEntry = { new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[4], JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[4], assetAccountFourthEntry);
        LOG.info("Repayment 4 Done  ......");

        // Repayment 5
        LOG.info("Repayment 5 ......");
        final JournalEntry[] assetAccountFifthEntry = { new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.DEBIT),
                new JournalEntry(REPAYMENT_AMOUNT[5], JournalEntry.TransactionType.CREDIT) };
        expected_value = expected_value - PRINCIPAL_VALUE_FOR_EACH_PERIOD;
        this.loanTransactionHelper.makeRepayment(REPAYMENT_DATE[5], REPAYMENT_AMOUNT[5], loanID);
        this.loanTransactionHelper.verifyRepaymentScheduleEntryFor(5, expected_value, loanID);
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, REPAYMENT_DATE[5], assetAccountFifthEntry);
        LOG.info("Repayment 5 Done  ......");
    }

    private Integer createLoanProductWithCashBasedAccountingEnabled(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal(LP_PRINCIPAL.toString()).withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery(LP_REPAYMENT_PERIOD).withNumberOfRepayments(LP_REPAYMENTS).withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod(LP_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRuleAsCashBased(accounts).build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private LocalDate getDateAsLocalDate(String dateAsString) {
        return LocalDate.parse(dateAsString, Utils.dateFormatter);
    }

    @Test
    public void checkAccountingWithSharingFlow() {

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account equityAccount = this.accountHelper.createEquityAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer shareProductID = createSharesProduct(assetAccount, incomeAccount, equityAccount, liabilityAccount);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec, DATE_OF_JOINING);
        Assertions.assertNotNull(clientID);
        final Integer savingsAccountId = SavingsAccountHelper.openSavingsAccount(requestSpec, responseSpec, clientID, "1000");
        Assertions.assertNotNull(savingsAccountId);
        final Integer shareAccountId = createShareAccount(clientID, shareProductID, savingsAccountId);
        Assertions.assertNotNull(shareAccountId);
        final Map<String, Object> shareAccountData = ShareAccountTransactionHelper.retrieveShareAccount(shareAccountId, requestSpec,
                responseSpec);
        Assertions.assertNotNull(shareAccountData);
        // Approve share Account
        final Map<String, Object> approveMap = new HashMap<>();
        approveMap.put("note", "Share Account Approval Note");
        approveMap.put("dateFormat", "dd MMMM yyyy");
        approveMap.put("approvedDate", "01 Jan 2016");
        approveMap.put("locale", "en");
        final String approve = new Gson().toJson(approveMap);
        ShareAccountTransactionHelper.postCommand("approve", shareAccountId, approve, requestSpec, responseSpec);
        // Activate Share Account
        final Map<String, Object> activateMap = new HashMap<>();
        activateMap.put("dateFormat", "dd MMMM yyyy");
        activateMap.put("activatedDate", "01 Jan 2016");
        activateMap.put("locale", "en");
        final String activateJson = new Gson().toJson(activateMap);
        ShareAccountTransactionHelper.postCommand("activate", shareAccountId, activateJson, requestSpec, responseSpec);

        // Checking sharing entries.
        final JournalEntry[] assetAccountEntry = { new JournalEntry(Float.parseFloat("200"), JournalEntry.TransactionType.DEBIT) };
        final JournalEntry[] liabilityAccountEntry = { new JournalEntry(Float.parseFloat("200"), JournalEntry.TransactionType.CREDIT) };
        final JournalEntry[] checkJournalEntryForEquityAccount = {
                new JournalEntry(Float.parseFloat("200"), JournalEntry.TransactionType.CREDIT) };
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "01 Jan 2016", assetAccountEntry);
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, "01 Jan 2016", liabilityAccountEntry);
        this.journalEntryHelper.checkJournalEntryForEquityAccount(equityAccount, "01 Jan 2016", checkJournalEntryForEquityAccount);

        final String transactionId = this.journalEntryHelper.getJournalEntryTransactionIdByAccount(assetAccount, "01 Jan 2016",
                assetAccountEntry);
        Assertions.assertNotEquals("", transactionId);

        final GetJournalEntriesTransactionIdResponse journalEntriesTransactionIdResponse = this.journalEntryHelper
                .getJournalEntries(transactionId);
        Assertions.assertNotNull(journalEntriesTransactionIdResponse);
    }

    public static Integer createSharesProduct(final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SHARE PRODUCT ---------------------------------------");
        final String shareProductJSON = new ShareProductHelper().withCashBasedAccounting(accounts).build();
        return ShareProductTransactionHelper.createShareProduct(shareProductJSON, requestSpec, responseSpec);
    }

    private Integer createShareAccount(final Integer clientId, final Integer productId, final Integer savingsAccountId) {
        final String shareAccountJSON = new ShareAccountHelper().withClientId(String.valueOf(clientId))
                .withProductId(String.valueOf(productId)).withExternalId("External1").withSavingsAccountId(String.valueOf(savingsAccountId))
                .withSubmittedDate("01 Jan 2016").withApplicationDate("01 Jan 2016").withRequestedShares("100").build();
        return ShareAccountTransactionHelper.createShareAccount(shareAccountJSON, requestSpec, responseSpec);
    }

}
