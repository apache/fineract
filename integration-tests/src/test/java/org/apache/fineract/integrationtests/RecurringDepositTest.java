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

import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetInterestRateChartsChartSlabs;
import org.apache.fineract.client.models.GetRecurringDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetRecurringDepositAccountsStatus;
import org.apache.fineract.client.models.GetRecurringDepositAccountsSummary;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRequest;
import org.apache.fineract.client.models.PostRecurringDepositProductsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.integrationtests.client.feign.FeignDepositTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.DepositInterestCalculator;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestValidators;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.Account.AccountType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class RecurringDepositTest extends FeignDepositTestBase {

    public static final int WHOLE_TERM = DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM;
    private static final int TILL_PREMATURE_WITHDRAWAL = DepositTestData.PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL;
    private static final int DAILY = SavingsTestData.InterestCompoundingPeriodType.DAILY;
    private static final int MONTHLY = SavingsTestData.InterestCompoundingPeriodType.MONTHLY;
    private static final int QUARTERLY = SavingsTestData.InterestCompoundingPeriodType.QUARTERLY;
    private static final int BI_ANNUALLY = SavingsTestData.InterestCompoundingPeriodType.BI_ANNUAL;
    private static final int ANNUALLY = SavingsTestData.InterestCompoundingPeriodType.ANNUAL;
    private static final int INTEREST_CALCULATION_USING_DAILY_BALANCE = SavingsTestData.InterestCalculationType.DAILY_BALANCE;
    private static final int DAYS_360 = SavingsTestData.InterestCalculationDaysInYearType.DAYS_360;
    private static final int DAYS_365 = SavingsTestData.InterestCalculationDaysInYearType.DAYS_365;
    private static final int NONE = SavingsTestData.AccountingRule.NONE;
    private static final int CASH_BASED = SavingsTestData.AccountingRule.CASH_BASED;

    public static final BigDecimal MINIMUM_OPENING_BALANCE = new BigDecimal("1000.0");
    public static final int CLOSURE_TYPE_WITHDRAW_DEPOSIT = DepositTestData.AccountClosureType.WITHDRAW_DEPOSIT;
    public static final int CLOSURE_TYPE_TRANSFER_TO_SAVINGS = DepositTestData.AccountClosureType.TRANSFER_TO_SAVINGS;
    public static final int CLOSURE_TYPE_REINVEST = DepositTestData.AccountClosureType.REINVEST;
    public static final Integer DAILY_COMPOUNDING_INTERVAL = 0;
    public static final Integer MONTHLY_INTERVAL = 1;
    public static final Integer QUARTERLY_INTERVAL = 3;
    public static final Integer BIANNULLY_INTERVAL = 6;
    public static final Integer ANNUL_INTERVAL = 12;

    public static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("2000.0");

    // TODO Given the difference in calculation methods in test vs application,
    // the exact values
    // returned may differ enough to cause differences in rounding. Given this,
    // we only compare that the result is within THRESHOLD of the expected amount.
    // A proper solution would be to implement the exact interest
    // calculation in this test,
    // and then to compare the exact results
    public static final BigDecimal THRESHOLD = BigDecimal.ONE;

    @BeforeEach
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(Utils.TENANT_TIME_ZONE));
    }

    /***
     * Test case for Recurring Deposit Account premature closure with transaction type withdrawal and Cash Based
     * accounting enabled
     */
    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeWithdrawal() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

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
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDate + 1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();

        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, depositAmount).getResourceId());

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, expectedFirstDepositOnDate, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, expectedFirstDepositOnDate,
                credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(recurringDepositHelper.calculateInterest(recurringDepositAccountId));
        Assertions.assertNotNull(recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId());

        BigDecimal totalInterestPosted = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalInterestPosted();

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        recurringDepositHelper.calculatePrematureAmount(recurringDepositAccountId, CLOSED_ON_DATE);

        Assertions.assertNotNull(recurringDepositHelper
                .prematureClose(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId());

        DepositTestValidators.verifyRecurringDepositAccountIsPrematureClosed(statusOf(recurringDepositAccountId));

        BigDecimal maturityAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMaturityAmount();
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, CLOSED_ON_DATE, credit(assetAccount, maturityAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, debit(liabilityAccount, maturityAmount));
    }

    /***
     * Test case for Recurring Deposit Account premature closure with transaction transfers to savings account and Cash
     * Based accounting enabled
     */
    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeTransferToSavings() {
        verifyPrematureClosureToSavings(false, null);
    }

    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeTransferToSavings_WITH_HOLD_TAX() {
        verifyPrematureClosureToSavings(true, null);
    }

    private void verifyPrematureClosureToSavings(final boolean withHoldTax, final String unusedMarker) {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account liabilityAccountForTax = withHoldTax ? accountHelper.createLiabilityAccount() : null;

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
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDate + 1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(savingsProductId);

        PostSavingsAccountsResponse savingsApplication = savingsHelper.submitApplication(clientId, savingsProductId, SUBMITTED_ON_DATE);
        final Long savingsId = savingsApplication.getSavingsId();
        Assertions.assertNotNull(savingsId);

        SavingsTestValidators.verifySavingsIsPending(savingsHelper.getSavingsDetails(savingsId).getStatus());
        savingsHelper.approveSavings(savingsId, APPROVED_ON_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsDetails(savingsId).getStatus());
        savingsHelper.activateSavings(savingsId, ACTIVATION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsDetails(savingsId).getStatus());

        Long recurringDepositProductId;
        if (withHoldTax) {
            final Long taxGroupId = createTaxGroup("10", liabilityAccountForTax);
            recurringDepositProductId = createRecurringDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, taxGroupId, CASH_BASED,
                    assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        } else {
            recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                    incomeAccount, expenseAccount);
        }
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();

        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, depositAmount).getResourceId());

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, expectedFirstDepositOnDate, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, expectedFirstDepositOnDate,
                credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(recurringDepositHelper.calculateInterest(recurringDepositAccountId));
        Assertions.assertNotNull(recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId());

        BigDecimal totalInterestPosted = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalInterestPosted();

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        BigDecimal balanceBefore = savingsHelper.getSavingsDetails(savingsId).getSummary().getAccountBalance();
        Account financialAccount = getMappedLiabilityFinancialAccount();

        recurringDepositHelper.calculatePrematureAmount(recurringDepositAccountId, CLOSED_ON_DATE);

        Assertions.assertNotNull(recurringDepositHelper
                .prematureClose(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_TRANSFER_TO_SAVINGS, savingsId).getResourceId());

        DepositTestValidators.verifyRecurringDepositAccountIsPrematureClosed(statusOf(recurringDepositAccountId));

        BigDecimal prematurityAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMaturityAmount();

        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE,
                credit(liabilityAccount, prematurityAmount), debit(liabilityAccount, prematurityAmount));
        journalEntryHelper.checkJournalEntryForAssetAccount(financialAccount, CLOSED_ON_DATE, debit(financialAccount, prematurityAmount),
                credit(financialAccount, prematurityAmount));

        BigDecimal balanceAfter = savingsHelper.getSavingsDetails(savingsId).getSummary().getAccountBalance();
        BigDecimal expectedSavingsBalance = balanceBefore.add(prematurityAmount);
        assertEquals(0, expectedSavingsBalance.compareTo(balanceAfter), () -> "Verifying Savings Account Balance after Premature Closure: "
                + "expected " + expectedSavingsBalance + " but was " + balanceAfter);

        if (withHoldTax) {
            BigDecimal withHoldTaxAmount = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalWithholdTax();
            Assertions.assertNotNull(withHoldTaxAmount);
        }
    }

    @Test
    public void testRecurringDepositAccountWithClosureTypeTransferToSavings_WITH_HOLD_TAX() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account liabilityAccountForTax = accountHelper.createLiabilityAccount();

        LocalDate todaysDate = Utils.getLocalDateOfTenant().minusMonths(20);
        final String VALID_FROM = Utils.dateFormatter.format(todaysDate);
        final String VALID_TO = Utils.dateFormatter.format(todaysDate.plusYears(10));

        todaysDate = Utils.getLocalDateOfTenant().minusMonths(20);
        final String SUBMITTED_ON_DATE = Utils.dateFormatter.format(todaysDate);
        final String APPROVED_ON_DATE = Utils.dateFormatter.format(todaysDate);
        final String ACTIVATION_DATE = Utils.dateFormatter.format(todaysDate);
        final String expectedFirstDepositOnDate = Utils.dateFormatter.format(todaysDate);

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createSavingsProduct(MINIMUM_OPENING_BALANCE, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        PostSavingsAccountsResponse savingsApplication = savingsHelper.submitApplication(clientId, savingsProductId, SUBMITTED_ON_DATE);
        final Long savingsId = savingsApplication.getSavingsId();
        savingsHelper.approveSavings(savingsId, APPROVED_ON_DATE);
        savingsHelper.activateSavings(savingsId, ACTIVATION_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsDetails(savingsId).getStatus());

        final Long taxGroupId = createTaxGroup("10", liabilityAccountForTax);
        Long recurringDepositProductId = createRecurringDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, taxGroupId, CASH_BASED,
                assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();
        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, depositAmount).getResourceId());

        Assertions.assertNotNull(recurringDepositHelper.calculateInterest(recurringDepositAccountId));
        Assertions.assertNotNull(recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId());

        schedulerHelper.executeAndAwaitJob("Update Deposit Accounts Maturity details");

        GetRecurringDepositAccountsSummary summary = recurringDepositHelper.getSummary(recurringDepositAccountId);
        Assertions.assertNotNull(summary.getTotalWithholdTax());

        DepositTestValidators.verifyRecurringDepositAccountIsMatured(statusOf(recurringDepositAccountId));
    }

    /***
     * Test case for Recurring Deposit Account premature closure with transaction type ReInvest and Cash Based
     * accounting enabled
     */
    @Test
    public void testRecurringDepositAccountWithPrematureClosureTypeReinvest() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

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
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDate + 1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();
        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, depositAmount).getResourceId());

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, expectedFirstDepositOnDate, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, expectedFirstDepositOnDate,
                credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(recurringDepositHelper.calculateInterest(recurringDepositAccountId));
        Assertions.assertNotNull(recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId());

        BigDecimal totalInterestPosted = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalInterestPosted();
        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        recurringDepositHelper.calculatePrematureAmount(recurringDepositAccountId, CLOSED_ON_DATE);

        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> recurringDepositHelper.prematureClose(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_REINVEST, null));
        assertEquals("validation.msg.recurringdepositaccount.onAccountClosureId.reinvest.not.allowed",
                FeignErrors.errorGlobalisationCode(exception));
    }

    @Test
    public void testRecurringDepositAccountUpdation() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        String submittedOnDate = dateFormat.format(todaysDate.getTime());
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        recurringDepositProductHelper.getAllProducts();
        recurringDepositProductHelper.getProduct(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, submittedOnDate,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        todaysDate.add(Calendar.DATE, -1);
        submittedOnDate = dateFormat.format(todaysDate.getTime());
        PostRecurringDepositAccountsRequest updated = DepositRequestBuilders.recurringDepositAccount(clientId, recurringDepositProductId,
                submittedOnDate, expectedFirstDepositOnDate, WHOLE_TERM);
        var changes = recurringDepositHelper.updateApplication(recurringDepositAccountId, DepositRequestBuilders.asUpdate(updated))
                .getChanges();
        Assertions.assertNotNull(changes.getSubmittedOnDate(), "The update must report submittedOnDate as changed");
    }

    @Test
    public void testRecurringDepositAccountUndoApproval() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.undoApproval(recurringDepositAccountId);
        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));
    }

    @Test
    public void testRecurringDepositAccountRejectedAndClosed() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String REJECTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.reject(recurringDepositAccountId, REJECTED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsRejected(statusOf(recurringDepositAccountId));
        DepositTestValidators.verifyRecurringDepositAccountIsClosed(statusOf(recurringDepositAccountId));
    }

    @Test
    public void testRecurringDepositAccountWithdrawnByClientAndClosed() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String WITHDRAWN_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.withdrawApplication(recurringDepositAccountId, WITHDRAWN_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsWithdrawn(statusOf(recurringDepositAccountId));
        DepositTestValidators.verifyRecurringDepositAccountIsClosed(statusOf(recurringDepositAccountId));
    }

    @Test
    public void testRecurringDepositAccountIsDeleted() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        Assertions.assertNotNull(recurringDepositHelper.deleteApplication(recurringDepositAccountId).getResourceId());
    }

    @Test
    public void testUpdateAndUndoTransactionForRecurringDepositAccount() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

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
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String DEPOSIT_DATE = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal balanceBefore = recurringDepositHelper.getSummary(recurringDepositAccountId).getAccountBalance();

        Long transactionIdForDeposit = recurringDepositHelper.deposit(recurringDepositAccountId, DEPOSIT_DATE, DEPOSIT_AMOUNT)
                .getResourceId();
        Assertions.assertNotNull(transactionIdForDeposit);

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, DEPOSIT_DATE, debit(assetAccount, DEPOSIT_AMOUNT));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, DEPOSIT_DATE, credit(liabilityAccount, DEPOSIT_AMOUNT));

        BigDecimal expectedBalanceAfter = balanceBefore.add(DEPOSIT_AMOUNT);
        BigDecimal balanceAfter = recurringDepositHelper.getSummary(recurringDepositAccountId).getAccountBalance();
        assertBalance(expectedBalanceAfter, balanceAfter, "Verifying account balance after deposit");

        BigDecimal updatedTransactionAmount = DEPOSIT_AMOUNT.subtract(new BigDecimal("1000.0"));
        Long updateTransactionId = recurringDepositHelper.updateTransaction(recurringDepositAccountId, transactionIdForDeposit,
                DEPOSIT_DATE, updatedTransactionAmount);
        Assertions.assertNotNull(updateTransactionId);

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, DEPOSIT_DATE, debit(assetAccount, updatedTransactionAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, DEPOSIT_DATE,
                credit(liabilityAccount, updatedTransactionAmount));

        expectedBalanceAfter = DEPOSIT_AMOUNT.subtract(updatedTransactionAmount);
        balanceAfter = recurringDepositHelper.getSummary(recurringDepositAccountId).getAccountBalance();
        assertBalance(expectedBalanceAfter, balanceAfter, "Verifying account balance after updating Transaction");

        Long undoTransactionId = recurringDepositHelper.undoTransaction(recurringDepositAccountId, updateTransactionId, DEPOSIT_DATE,
                BigDecimal.ZERO);
        Assertions.assertNotNull(undoTransactionId);

        expectedBalanceAfter = expectedBalanceAfter.subtract(updatedTransactionAmount);
        balanceAfter = recurringDepositHelper.getSummary(recurringDepositAccountId).getAccountBalance();
        assertBalance(expectedBalanceAfter, balanceAfter, "Verifying account balance after Undo Transaction");
    }

    @Test
    public void testPostInterestForRecurringDeposit() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();

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
        final String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDate + 1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal depositAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMandatoryRecommendedDepositAmount();
        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, depositAmount).getResourceId());

        Assertions.assertNotNull(recurringDepositHelper.calculateInterest(recurringDepositAccountId));
        Assertions.assertNotNull(recurringDepositHelper.postInterest(recurringDepositAccountId).getResourceId());

        BigDecimal totalInterestPosted = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalInterestPosted();
        Assertions.assertNotNull(totalInterestPosted);
        Assertions.assertTrue(totalInterestPosted.compareTo(BigDecimal.ZERO) > 0, "Expected interest to be posted");

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_365_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_365, MONTHLY, MONTHLY, MONTHLY_INTERVAL, MONTHLY_INTERVAL, false);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_360_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_360, MONTHLY, MONTHLY, MONTHLY_INTERVAL, MONTHLY_INTERVAL, true);
    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_365_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_365, DAILY, MONTHLY, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL, true);
    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndMonthlyPosting_With_360_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_360, DAILY, MONTHLY, DAILY_COMPOUNDING_INTERVAL, MONTHLY_INTERVAL, true);
    }

    @Test
    public void testRecurringDepositWithBi_AnnualCompoundingAndPosting_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, BI_ANNUALLY, BI_ANNUALLY, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);
    }

    @Test
    public void testRecurringDepositWithBi_AnnualCompoundingAndPosting_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, BI_ANNUALLY, BI_ANNUALLY, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);
    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, DAILY, ANNUALLY, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);
    }

    @Test
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, DAILY, ANNUALLY, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);
    }

    @Test
    public void testRecurringDepositQuarterlyCompoundingAndQuarterlyPosting_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, QUARTERLY, QUARTERLY, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);
    }

    @Test
    public void testRecurringDepositQuarterlyCompoundingAndQuarterlyPosting_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, QUARTERLY, QUARTERLY, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);
    }

    /**
     * Projects the maturity amount for an account opened on the first of last month.
     *
     * @param reconfigure
     *            whether the interest configuration is re-sent as an update; the 365-day monthly case relies on the
     *            product default instead.
     */
    private void verifyMaturityAmountFromMonthStart(final int daysInYearType, final int compoundingPeriodType, final int postingPeriodType,
            final int compoundingInterval, final int postingInterval, final boolean reconfigure) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar chartDate = Calendar.getInstance();
        chartDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(chartDate.getTime());
        chartDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(chartDate.getTime());

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.DATE, -(currentDate - 1));
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());

        verifyMaturityAmount(VALID_FROM, VALID_TO, SUBMITTED_ON_DATE, todaysDate, daysInYearType, compoundingPeriodType, postingPeriodType,
                compoundingInterval, postingInterval, reconfigure);
    }

    /**
     * Projects the maturity amount for an account opened on the first of January of this year.
     * <p>
     * The longer posting periods need a whole number of periods to have elapsed, so these anchor on a year boundary
     * rather than a month one, and their chart is left open-ended.
     */
    private void verifyMaturityAmountFromYearStart(final int daysInYearType, final int compoundingPeriodType, final int postingPeriodType,
            final int compoundingInterval, final int postingInterval) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        DateFormat currentMonthFormat = new SimpleDateFormat("MM");
        DateFormat currentDateFormat = new SimpleDateFormat("dd");

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.YEAR, -1);
        Integer currentMonth = Integer.valueOf(currentMonthFormat.format(todaysDate.getTime()));
        todaysDate.add(Calendar.MONTH, 12 - currentMonth);
        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDate + 1);

        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());

        verifyMaturityAmount(VALID_FROM, null, SUBMITTED_ON_DATE, todaysDate, daysInYearType, compoundingPeriodType, postingPeriodType,
                compoundingInterval, postingInterval, true);
    }

    private void verifyMaturityAmount(final String validFrom, final String validTo, final String submittedOnDate, final Calendar openedOn,
            final int daysInYearType, final int compoundingPeriodType, final int postingPeriodType, final int compoundingInterval,
            final int postingInterval, final boolean reconfigure) {
        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(validFrom, validTo, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, submittedOnDate,
                WHOLE_TERM, submittedOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        if (reconfigure) {
            updateInterestCalculationConfig(clientId, recurringDepositProductId, recurringDepositAccountId, submittedOnDate,
                    submittedOnDate, daysInYearType, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, compoundingPeriodType,
                    postingPeriodType);
        }

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, submittedOnDate);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        GetRecurringDepositAccountsAccountIdResponse account = recurringDepositHelper.getAccount(recurringDepositAccountId);
        BigDecimal depositAmount = account.getMandatoryRecommendedDepositAmount();
        BigDecimal maturityAmount = account.getMaturityAmount();
        Integer depositPeriod = account.getDepositPeriod();
        Long daysInYear = account.getInterestCalculationDaysInYearType().getId();

        BigDecimal principal = recurringDepositHelper.getSummary(recurringDepositAccountId).getAccountBalance();

        Set<GetInterestRateChartsChartSlabs> chartSlabs = interestRateChartHelper.getChartSlabsByProduct(recurringDepositProductId);
        BigDecimal interestRate = DepositInterestCalculator.interestRateFor(chartSlabs, depositPeriod);
        double interestPerDay = interestRate.doubleValue() / 100 / daysInYear;

        float projected = DepositInterestCalculator.principalAfterCompoundingInterest(openedOn, principal.floatValue(),
                depositAmount.floatValue(), depositPeriod, interestPerDay, compoundingInterval, postingInterval);

        assertWithinThreshold(BigDecimal.valueOf(projected), maturityAmount, "Verifying Maturity amount for Recurring Deposit Account");
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_365_Days() {
        verifyPrematureClosureWithPenalInterestWholeTerm(DAYS_365, false);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_360_Days() {
        verifyPrematureClosureWithPenalInterestWholeTerm(DAYS_360, true);
    }

    /**
     * The whole-term pre-closure pair deposits twice: once on activation and once at the start of the following month,
     * so the projection accrues over two part-months rather than one.
     */
    private void verifyPrematureClosureWithPenalInterestWholeTerm(final int daysInYearType, final boolean reconfigure) {
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
        String expectedFirstDepositOnDate = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.MONTH, 1);
        final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, expectedFirstDepositOnDate);
        Assertions.assertNotNull(recurringDepositAccountId);

        if (reconfigure) {
            updateInterestCalculationConfig(clientId, recurringDepositProductId, recurringDepositAccountId, SUBMITTED_ON_DATE,
                    expectedFirstDepositOnDate, daysInYearType, WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);
        }

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));
        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));
        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        GetRecurringDepositAccountsAccountIdResponse account = recurringDepositHelper.getAccount(recurringDepositAccountId);
        BigDecimal depositAmount = account.getMandatoryRecommendedDepositAmount();
        Integer depositPeriod = account.getDepositPeriod();
        Long daysInYear = account.getInterestCalculationDaysInYearType().getId();
        BigDecimal preClosurePenalInterestRate = account.getPreClosurePenalInterest();

        Set<GetInterestRateChartsChartSlabs> chartSlabs = interestRateChartHelper.getChartSlabsByProduct(recurringDepositProductId);

        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, DEPOSIT_AMOUNT).getResourceId());

        BigDecimal principal = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalDeposits();

        BigDecimal interestRate = DepositInterestCalculator.interestRateFor(chartSlabs, depositPeriod)
                .subtract(preClosurePenalInterestRate);
        double interestPerDay = interestRate.doubleValue() / 100 / daysInYear;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Integer currentDate = Integer.valueOf(currentDateFormat.format(calendar.getTime()));
        Integer daysInMonth = calendar.getActualMaximum(Calendar.DATE) - currentDate + 1;
        BigDecimal interestPerMonth = BigDecimal.valueOf(interestPerDay * principal.doubleValue() * daysInMonth);
        principal = principal.add(interestPerMonth).add(depositAmount);
        calendar.add(Calendar.DATE, daysInMonth);

        expectedFirstDepositOnDate = dateFormat.format(calendar.getTime());
        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, expectedFirstDepositOnDate, DEPOSIT_AMOUNT).getResourceId());

        interestPerMonth = BigDecimal.valueOf(interestPerDay * principal.doubleValue() * (currentDate - 1));
        principal = principal.add(interestPerMonth);

        recurringDepositHelper.calculatePrematureAmount(recurringDepositAccountId, CLOSED_ON_DATE);

        Assertions.assertNotNull(recurringDepositHelper
                .prematureClose(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId());

        DepositTestValidators.verifyRecurringDepositAccountIsPrematureClosed(statusOf(recurringDepositAccountId));

        BigDecimal maturityAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMaturityAmount();
        assertWithinThreshold(principal, maturityAmount, "Verifying Pre-Closure maturity amount");
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_365_Days() {
        verifyPrematureClosureTillWithdrawal(DAYS_365, false);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_360_Days() {
        verifyPrematureClosureTillWithdrawal(DAYS_360, true);
    }

    /**
     * With the penalty applied till premature withdrawal the rate comes from the slab covering the months actually
     * held, not the slab covering the full deposit period.
     */
    private void verifyPrematureClosureTillWithdrawal(final int daysInYearType, final boolean reconfigure) {
        DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();

        LocalDate chartDate = Utils.getLocalDateOfTenant().minusMonths(3);
        final String VALID_FROM = dateFormat.format(chartDate);
        final String VALID_TO = dateFormat.format(chartDate.plusYears(10));

        LocalDate activationDate = Utils.getLocalDateOfTenant().minusMonths(1).minusDays(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(activationDate);
        final String APPROVED_ON_DATE = dateFormat.format(activationDate);
        final String ACTIVATION_DATE = dateFormat.format(activationDate);

        LocalDate closingDate = Utils.getLocalDateOfTenant();
        final String CLOSED_ON_DATE = dateFormat.format(closingDate);

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                TILL_PREMATURE_WITHDRAWAL, SUBMITTED_ON_DATE);
        Assertions.assertNotNull(recurringDepositAccountId);

        if (reconfigure) {
            updateInterestCalculationConfig(clientId, recurringDepositProductId, recurringDepositAccountId, SUBMITTED_ON_DATE,
                    SUBMITTED_ON_DATE, daysInYearType, TILL_PREMATURE_WITHDRAWAL, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY,
                    MONTHLY);
        }

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));
        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));
        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        GetRecurringDepositAccountsAccountIdResponse account = recurringDepositHelper.getAccount(recurringDepositAccountId);
        Long daysInYear = account.getInterestCalculationDaysInYearType().getId();
        BigDecimal preClosurePenalInterestRate = account.getPreClosurePenalInterest();

        Assertions.assertNotNull(
                recurringDepositHelper.deposit(recurringDepositAccountId, SUBMITTED_ON_DATE, DEPOSIT_AMOUNT).getResourceId());

        BigDecimal principal = recurringDepositHelper.getSummary(recurringDepositAccountId).getTotalDeposits();

        Set<GetInterestRateChartsChartSlabs> chartSlabs = interestRateChartHelper.getChartSlabsByProduct(recurringDepositProductId);
        int monthsHeld = Math.toIntExact(ChronoUnit.MONTHS.between(activationDate, closingDate));
        BigDecimal interestRate = DepositInterestCalculator.interestRateFor(chartSlabs, monthsHeld).subtract(preClosurePenalInterestRate);
        double interestPerDay = interestRate.doubleValue() / 100 / daysInYear;

        long daysBetween = DAYS.between(activationDate, closingDate);
        BigDecimal totalInterest = BigDecimal.valueOf(interestPerDay * principal.doubleValue() * daysBetween);
        BigDecimal expectedPrematureAmount = principal.add(totalInterest);

        recurringDepositHelper.calculatePrematureAmount(recurringDepositAccountId, CLOSED_ON_DATE);

        Assertions.assertNotNull(recurringDepositHelper
                .prematureClose(recurringDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId());

        DepositTestValidators.verifyRecurringDepositAccountIsPrematureClosed(statusOf(recurringDepositAccountId));

        BigDecimal maturityAmount = recurringDepositHelper.getAccount(recurringDepositAccountId).getMaturityAmount();
        assertWithinThreshold(expectedPrematureAmount, maturityAmount, "Verifying Pre-Closure maturity amount");
    }

    @Test
    public void testRecurringDepositAccountWithPeriodInterestRateChart() {
        verifyInterestRateForChart("period", new BigDecimal("1000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testRecurringDepositAccountWithPeriodInterestRateChart_AMOUNT_VARIATION() {
        verifyInterestRateForChart("period", new BigDecimal("10000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testRecurringDepositAccountWithPeriodInterestRateChart_PERIOD_VARIATION() {
        verifyInterestRateForChart("period", new BigDecimal("1000"), 18, new BigDecimal("7.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountInterestRateChart() {
        verifyInterestRateForChart("amount", new BigDecimal("1000"), 12, new BigDecimal("8.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountInterestRateChart_AMOUNT_VARIATION() {
        verifyInterestRateForChart("amount", new BigDecimal("500"), 12, new BigDecimal("7.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountInterestRateChart_PERIOD_VARIATION() {
        verifyInterestRateForChart("amount", new BigDecimal("500"), 10, new BigDecimal("5.0"));
    }

    @Test
    public void testRecurringDepositAccountWithPeriodAndAmountInterestRateChart() {
        verifyInterestRateForChart("period_amount", new BigDecimal("1000"), 12, new BigDecimal("7.0"));
    }

    @Test
    public void testRecurringDepositAccountWithPeriodAndAmountInterestRateChart_AMOUNT_VARIATION() {
        verifyInterestRateForChart("period_amount", new BigDecimal("400"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testRecurringDepositAccountWithPeriodAndAmountInterestRateChart_PERIOD_VARIATION() {
        verifyInterestRateForChart("period_amount", new BigDecimal("1000"), 14, new BigDecimal("8.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountAndPeriodInterestRateChart() {
        verifyInterestRateForChart("amount_period", new BigDecimal("1000"), 12, new BigDecimal("8.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountAndPeriodInterestRateChart_AMOUNT_VARIATION() {
        verifyInterestRateForChart("amount_period", new BigDecimal("100"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testRecurringDepositAccountWithAmountAndPeriodInterestRateChart_PERIOD_VARIATION() {
        verifyInterestRateForChart("amount_period", new BigDecimal("1000"), 6, new BigDecimal("7.0"));
    }

    private void verifyInterestRateForChart(final String chartToUse, final BigDecimal depositAmount, final int depositPeriod,
            final BigDecimal interestRate) {
        final String VALID_FROM = "01 March 2014";
        final String VALID_TO = "01 March 2016";
        final String SUBMITTED_ON_DATE = "01 March 2015";
        final String APPROVED_ON_DATE = "01 March 2015";
        final String ACTIVATION_DATE = "01 March 2015";

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long recurringDepositProductId = createRecurringDepositProduct(VALID_FROM, VALID_TO, NONE, chartToUse);
        Assertions.assertNotNull(recurringDepositProductId);

        Long recurringDepositAccountId = applyForRecurringDepositApplication(clientId, recurringDepositProductId, SUBMITTED_ON_DATE,
                WHOLE_TERM, SUBMITTED_ON_DATE, depositAmount, depositPeriod);
        Assertions.assertNotNull(recurringDepositAccountId);

        DepositTestValidators.verifyRecurringDepositIsPending(statusOf(recurringDepositAccountId));

        recurringDepositHelper.approve(recurringDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyRecurringDepositIsApproved(statusOf(recurringDepositAccountId));

        recurringDepositHelper.activate(recurringDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyRecurringDepositIsActive(statusOf(recurringDepositAccountId));

        BigDecimal actualRate = recurringDepositHelper.getAccount(recurringDepositAccountId).getNominalAnnualInterestRate();
        assertEquals(0, interestRate.compareTo(actualRate),
                () -> "Expected nominal annual interest rate " + interestRate + " but was " + actualRate);
    }

    @AfterEach
    public void tearDown() {
        for (GetFinancialActivityAccountsResponse mapping : financialActivityAccountHelper.getAllMappings()) {
            Long deletedId = financialActivityAccountHelper.deleteMapping(mapping.getId()).getResourceId();
            Assertions.assertNotNull(deletedId);
            assertEquals(mapping.getId(), deletedId);
        }
    }

    private Long createRecurringDepositProduct(final String validFrom, final String validTo, final int accountingRule,
            Account... accounts) {
        PostRecurringDepositProductsRequest request = withAccounting(DepositRequestBuilders.recurringDepositProduct(), accountingRule,
                accounts);
        return recurringDepositProductHelper
                .createProduct(
                        DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.recurringChartSlabsFor("period")))
                .getResourceId();
    }

    private Long createRecurringDepositProductWithWithHoldTax(final String validFrom, final String validTo, final Long taxGroupId,
            final int accountingRule, Account... accounts) {
        PostRecurringDepositProductsRequest request = withAccounting(DepositRequestBuilders.recurringDepositProduct(), accountingRule,
                accounts).withHoldTax(true).taxGroupId(taxGroupId);
        return recurringDepositProductHelper
                .createProduct(
                        DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.recurringChartSlabsFor("period")))
                .getResourceId();
    }

    private Long createRecurringDepositProduct(final String validFrom, final String validTo, final int accountingRule,
            final String chartToBePicked, Account... accounts) {
        PostRecurringDepositProductsRequest request = withAccounting(DepositRequestBuilders.recurringDepositProduct(), accountingRule,
                accounts);
        return recurringDepositProductHelper.createProduct(DepositRequestBuilders.withChart(request, validFrom, validTo,
                DepositTestData.recurringChartSlabsFor(chartToBePicked), DepositTestData.isPrimaryGroupingByAmount(chartToBePicked)))
                .getResourceId();
    }

    private PostRecurringDepositProductsRequest withAccounting(PostRecurringDepositProductsRequest request, final int accountingRule,
            Account... accounts) {
        if (accountingRule == CASH_BASED) {
            return DepositRequestBuilders.withCashBasedAccounting(request, accounts[0], accounts[1], accounts[2], accounts[3]);
        }
        return request.accountingRule(NONE);
    }

    private Long applyForRecurringDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final int penalInterestType, final String expectedFirstDepositOnDate) {
        return recurringDepositHelper.submitApplication(DepositRequestBuilders.recurringDepositAccount(clientId, productId, submittedOnDate,
                expectedFirstDepositOnDate, penalInterestType)).getSavingsId();
    }

    private Long applyForRecurringDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final int penalInterestType, final String expectedFirstDepositOnDate, final BigDecimal depositAmount, final int depositPeriod) {
        return recurringDepositHelper.submitApplication(DepositRequestBuilders
                .recurringDepositAccount(clientId, productId, submittedOnDate, expectedFirstDepositOnDate, penalInterestType)
                .mandatoryRecommendedDepositAmount(depositAmount).depositPeriod(depositPeriod)).getSavingsId();
    }

    /** Re-sends the whole account body with the interest configuration overridden, as the update endpoint requires. */
    private void updateInterestCalculationConfig(final Long clientId, final Long productId, final Long accountId,
            final String submittedOnDate, final String expectedFirstDepositOnDate, final int daysInYearType, final int penalInterestType,
            final int interestCalculationType, final int compoundingPeriodType, final int postingPeriodType) {
        PostRecurringDepositAccountsRequest request = DepositRequestBuilders
                .recurringDepositAccount(clientId, productId, submittedOnDate, expectedFirstDepositOnDate, penalInterestType)//
                .interestCalculationDaysInYearType(daysInYearType)//
                .interestCalculationType(interestCalculationType)//
                .interestCompoundingPeriodType(compoundingPeriodType)//
                .interestPostingPeriodType(postingPeriodType);
        recurringDepositHelper.updateApplication(accountId, DepositRequestBuilders.asUpdate(request));
    }

    private Long createSavingsProduct(final BigDecimal minOpeningBalance, final int accountingRule, Account... accounts) {
        var request = SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .minRequiredOpeningBalance(minOpeningBalance);
        if (accountingRule == CASH_BASED) {
            request = SavingsRequestBuilders.withAccrualAccountingMappings(request, accounts[0], accounts[1], accounts[2], accounts[3])
                    .accountingRule(SavingsTestData.AccountingRule.CASH_BASED);
        }
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private Account getMappedLiabilityFinancialAccount() {
        final Integer liabilityTransferFinancialActivityId = FinancialActivity.LIABILITY_TRANSFER.getValue();
        for (GetFinancialActivityAccountsResponse mapping : financialActivityAccountHelper.getAllMappings()) {
            if (liabilityTransferFinancialActivityId.equals(mapping.getFinancialActivityData().getId())) {
                return new Account(mapping.getGlAccountData().getId().intValue(), AccountType.LIABILITY);
            }
        }
        return createLiabilityFinancialAccountTransferType(liabilityTransferFinancialActivityId);
    }

    private Account createLiabilityFinancialAccountTransferType(final Integer liabilityTransferFinancialActivityId) {
        final Account liabilityAccountForMapping = accountHelper.createLiabilityAccount();
        Long financialActivityAccountId = financialActivityAccountHelper
                .createMapping(liabilityTransferFinancialActivityId, liabilityAccountForMapping).getResourceId();
        Assertions.assertNotNull(financialActivityAccountId);

        GetFinancialActivityAccountsResponse mapping = financialActivityAccountHelper.getMapping(financialActivityAccountId);
        assertEquals(liabilityTransferFinancialActivityId, mapping.getFinancialActivityData().getId());
        assertEquals(Long.valueOf(liabilityAccountForMapping.getAccountID()), mapping.getGlAccountData().getId());
        return liabilityAccountForMapping;
    }

    private Long createTaxGroup(final String percentage, final Account liabilityAccountForTax) {
        final PostTaxesComponentsRequest componentRequest = new PostTaxesComponentsRequest()
                .name(Utils.randomStringGenerator("Tax_component_Name_", 5)).percentage(Float.parseFloat(percentage))
                .startDate("01 January 2013").dateFormat("dd MMMM yyyy").locale("en").creditAccountType(2)
                .creditAccountId(liabilityAccountForTax.getAccountID().longValue());
        final var componentResponse = taxComponentHelper.createTaxComponent(componentRequest);
        final PostTaxesGroupRequest groupRequest = new PostTaxesGroupRequest().name(Utils.randomStringGenerator("Tax_group_Name_", 5))
                .dateFormat("dd MMMM yyyy").locale("en").taxComponents(Set.of(
                        new PostTaxesGroupTaxComponents().taxComponentId(componentResponse.getResourceId()).startDate("01 January 2013")));
        return taxGroupHelper.createTaxGroup(groupRequest).getResourceId();
    }

    private GetRecurringDepositAccountsStatus statusOf(final Long recurringDepositAccountId) {
        return recurringDepositHelper.getAccount(recurringDepositAccountId).getStatus();
    }

    private void assertBalance(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertEquals(0, expected.compareTo(actual), () -> message + ": expected " + expected + " but was " + actual);
    }

    /**
     * The projection accumulates in float over hundreds of days, so it is only good to about seven significant digits;
     * the message prints both values because a comparison result alone cannot be diagnosed.
     */
    private void assertWithinThreshold(final BigDecimal expected, final BigDecimal actual, final String message) {
        Assertions.assertTrue(expected.subtract(actual).abs().compareTo(THRESHOLD) < 0,
                () -> message + ": expected " + expected + " but was " + actual + " (tolerance " + THRESHOLD + ")");
    }

    private LoanTestData.Journal debit(final Account account, final BigDecimal amount) {
        return LoanTestData.Journal.debit(account.getAccountID().longValue(), amount.doubleValue());
    }

    private LoanTestData.Journal credit(final Account account, final BigDecimal amount) {
        return LoanTestData.Journal.credit(account.getAccountID().longValue(), amount.doubleValue());
    }
}
