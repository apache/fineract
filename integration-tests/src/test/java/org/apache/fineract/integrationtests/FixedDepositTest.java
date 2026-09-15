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
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdSummary;
import org.apache.fineract.client.models.GetFixedDepositAccountsAccountIdTransactionsResponse;
import org.apache.fineract.client.models.GetFixedDepositAccountsStatus;
import org.apache.fineract.client.models.GetInterestRateChartsChartSlabs;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
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
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.apache.fineract.portfolio.savings.service.FixedDepositAccountInterestCalculationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@Slf4j
public class FixedDepositTest extends FeignDepositTestBase {

    private FixedDepositAccountInterestCalculationServiceImpl fixedDepositAccountInterestCalculationServiceImpl;

    public static final int WHOLE_TERM = DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM;
    public static final int TILL_PREMATURE_WITHDRAWAL = DepositTestData.PreClosurePenalInterestOnType.TILL_PREMATURE_WITHDRAWAL;
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
    private static final int ACCRUAL = SavingsTestData.AccountingRule.ACCRUAL_PERIODIC;

    public static final BigDecimal MINIMUM_OPENING_BALANCE = new BigDecimal("1000.0");
    public static final int CLOSURE_TYPE_WITHDRAW_DEPOSIT = DepositTestData.AccountClosureType.WITHDRAW_DEPOSIT;
    public static final int CLOSURE_TYPE_TRANSFER_TO_SAVINGS = DepositTestData.AccountClosureType.TRANSFER_TO_SAVINGS;
    public static final int CLOSURE_TYPE_REINVEST = DepositTestData.AccountClosureType.REINVEST;
    public static final int CLOSURE_TYPE_REINVEST_PRINCIPAL_ONLY = DepositTestData.AccountClosureType.REINVEST_PRINCIPAL_ONLY;
    public static final Integer DAILY_COMPOUNDING_INTERVAL = 0;
    public static final Integer MONTHLY_INTERVAL = 1;
    public static final Integer QUARTERLY_INTERVAL = 3;
    public static final Integer BIANNULLY_INTERVAL = 6;
    public static final Integer ANNUL_INTERVAL = 12;

    // TODO Given the difference in calculation methods in test vs application,
    // the exact values
    // returned may differ enough to cause differences in rounding. Given this,
    // we only compare that the result is within THRESHOLD of the expected amount.
    // A proper solution would be to implement the exact interest
    // calculation in this test,
    // and then to compare the exact results
    public static final BigDecimal THRESHOLD = BigDecimal.ONE;

    private MockedStatic<MoneyHelper> moneyHelperStatic;

    @BeforeEach
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(Utils.TENANT_TIME_ZONE));
    }

    /***
     * Test case for Fixed Deposit Account Interest Calculation
     */
    @Test
    public void testFixedDepositInterestCalculationWithWrongCompoundingPeriod() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("principalAmount", 100);
        jsonObject.addProperty("annualInterestRate", 5);
        jsonObject.addProperty("tenureInMonths", 12);
        jsonObject.addProperty("interestPostingPeriodInMonths", 3);
        jsonObject.addProperty("interestCompoundingPeriodInMonths", 7);
        JsonParser parser = new JsonParser();
        String apiRequestBodyAsJson = jsonObject.toString();
        JsonElement element = parser.parse(apiRequestBodyAsJson);
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(12, RoundingMode.UP));
        fixedDepositAccountInterestCalculationServiceImpl = new FixedDepositAccountInterestCalculationServiceImpl(
                new DepositAccountDataValidator(new FromJsonHelper(), null), new FromJsonHelper());
        try {
            HashMap h = fixedDepositAccountInterestCalculationServiceImpl
                    .calculateInterest(new JsonQuery(apiRequestBodyAsJson, element, new FromJsonHelper()));
            fail("The function must throw an exception when called with invalid Compounding period");
        } catch (PlatformApiDataValidationException e) {
            assertEquals("Validation errors exist.", e.getMessage());
        } finally {
            moneyHelperStatic.close();
        }
    }

    @Test
    public void testFixedDepositInterestCalculationWithWrongCompoundingPeriod2() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("principalAmount", 100);
        jsonObject.addProperty("annualInterestRate", 5);
        jsonObject.addProperty("tenureInMonths", 15);
        jsonObject.addProperty("interestPostingPeriodInMonths", 3);
        jsonObject.addProperty("interestCompoundingPeriodInMonths", 6);
        JsonParser parser = new JsonParser();
        String apiRequestBodyAsJson = jsonObject.toString();
        JsonElement element = parser.parse(apiRequestBodyAsJson);
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(12, RoundingMode.UP));
        fixedDepositAccountInterestCalculationServiceImpl = new FixedDepositAccountInterestCalculationServiceImpl(
                new DepositAccountDataValidator(new FromJsonHelper(), null), new FromJsonHelper());
        try {
            HashMap h = fixedDepositAccountInterestCalculationServiceImpl
                    .calculateInterest(new JsonQuery(apiRequestBodyAsJson, element, new FromJsonHelper()));
            fail("The function must throw an exception when called with invalid Compounding period");
        } catch (PlatformApiDataValidationException e) {
            assertEquals("Validation errors exist.", e.getMessage());
        } finally {
            moneyHelperStatic.close();
        }
    }

    @Test
    public void testFixedDepositInterestCalculationWithValidInput() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("principalAmount", 10000);
        jsonObject.addProperty("annualInterestRate", 5);
        jsonObject.addProperty("tenureInMonths", 12);
        jsonObject.addProperty("interestPostingPeriodInMonths", 6);
        jsonObject.addProperty("interestCompoundingPeriodInMonths", 6);
        JsonParser parser = new JsonParser();
        String apiRequestBodyAsJson = jsonObject.toString();
        JsonElement element = parser.parse(apiRequestBodyAsJson);
        moneyHelperStatic = Mockito.mockStatic(MoneyHelper.class);
        moneyHelperStatic.when(() -> MoneyHelper.getMathContext()).thenReturn(new MathContext(12, RoundingMode.UP));
        fixedDepositAccountInterestCalculationServiceImpl = new FixedDepositAccountInterestCalculationServiceImpl(
                new DepositAccountDataValidator(new FromJsonHelper(), null), new FromJsonHelper());
        BigDecimal expectedResult = new BigDecimal("10506.250000");
        BigDecimal actualResult = new BigDecimal(fixedDepositAccountInterestCalculationServiceImpl
                .calculateInterest(new JsonQuery(apiRequestBodyAsJson, element, new FromJsonHelper())).get("maturityAmount").toString());
        assertEquals(expectedResult, actualResult);
        moneyHelperStatic.close();
    }

    /***
     * Test case for Fixed Deposit Product with default attributes
     */
    @Test
    public void testFixedDepositProductCreation() {
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

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> fixedDepositProductHelper.createProduct(withAccounting(DepositRequestBuilders.fixedDepositProduct(), CASH_BASED,
                        assetAccount, liabilityAccount, incomeAccount, expenseAccount)));
        assertEquals(400, exception.getStatus(), "A fixed deposit product without an interest chart must be rejected");
    }

    /***
     * Test case for Fixed Deposit Account premature closure with transaction type withdrawal and Cash Based accounting
     * enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeWithdrawal() {
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

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = daysInMonth - currentDate + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdSummary accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal depositAmount = accountSummary.getTotalDeposits();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(fixedDepositHelper.calculateInterest(fixedDepositAccountId));
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal totalInterestPosted = accountSummary.getTotalInterestPosted();

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        fixedDepositHelper.calculatePrematureAmount(fixedDepositAccountId, CLOSED_ON_DATE);

        Long prematureClosureTransactionId = fixedDepositHelper
                .prematureClose(fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId();
        Assertions.assertNotNull(prematureClosureTransactionId);

        DepositTestValidators.verifyFixedDepositAccountIsPrematureClosed(statusOf(fixedDepositAccountId));

        BigDecimal maturityAmount = fixedDepositHelper.getAccount(fixedDepositAccountId).getMaturityAmount();
        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, CLOSED_ON_DATE, credit(assetAccount, maturityAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, debit(liabilityAccount, maturityAmount));
    }

    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeWithdrawal_WITH_HOLD_TAX() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account liabilityAccountForTax = accountHelper.createLiabilityAccount();

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

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = daysInMonth - currentDate + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        final Long taxGroupId = createTaxGroup("10", liabilityAccountForTax);
        Long fixedDepositProductId = createFixedDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, taxGroupId, CASH_BASED, assetAccount,
                liabilityAccount, incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdSummary accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal depositAmount = accountSummary.getTotalDeposits();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(fixedDepositHelper.calculateInterest(fixedDepositAccountId));
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal totalInterestPosted = accountSummary.getTotalInterestPosted();
        Assertions.assertNull(accountSummary.getTotalWithholdTax());

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        fixedDepositHelper.calculatePrematureAmount(fixedDepositAccountId, CLOSED_ON_DATE);

        Long prematureClosureTransactionId = fixedDepositHelper
                .prematureClose(fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId();
        Assertions.assertNotNull(prematureClosureTransactionId);

        DepositTestValidators.verifyFixedDepositAccountIsPrematureClosed(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdResponse accountDetails = fixedDepositHelper.getAccount(fixedDepositAccountId);
        BigDecimal maturityAmount = accountDetails.getMaturityAmount();

        Assertions.assertNotNull(accountDetails.getSummary().getTotalWithholdTax());
        BigDecimal withHoldTax = accountDetails.getSummary().getTotalWithholdTax();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, CLOSED_ON_DATE, credit(assetAccount, maturityAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE, debit(liabilityAccount, maturityAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccountForTax, CLOSED_ON_DATE,
                credit(liabilityAccountForTax, withHoldTax));
    }

    @Test
    public void testFixedDepositAccountClosureTypeWithdrawal_WITH_HOLD_TAX() {
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
        final String CLOSED_ON_DATE = Utils.dateFormatter.format(todaysDate.plusMonths(14));

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        final Long taxGroupId = createTaxGroup("10", liabilityAccountForTax);
        Long fixedDepositProductId = createFixedDepositProductWithWithHoldTax(VALID_FROM, VALID_TO, taxGroupId, CASH_BASED, assetAccount,
                liabilityAccount, incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdSummary accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal depositAmount = accountSummary.getTotalDeposits();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(fixedDepositHelper.calculateInterest(fixedDepositAccountId));
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        Assertions.assertNull(accountSummary.getTotalWithholdTax());

        schedulerHelper.executeAndAwaitJob("Update Deposit Accounts Maturity details");

        GetFixedDepositAccountsAccountIdResponse accountDetails = fixedDepositHelper.getAccount(fixedDepositAccountId);
        Assertions.assertNotNull(accountDetails.getSummary().getTotalWithholdTax());
        BigDecimal withHoldTax = accountDetails.getSummary().getTotalWithholdTax();
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccountForTax, CLOSED_ON_DATE,
                credit(liabilityAccountForTax, withHoldTax));

        DepositTestValidators.verifyFixedDepositAccountIsMatured(statusOf(fixedDepositAccountId));
    }

    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart() {
        testFixedDepositAccountForInterestRate("period", new BigDecimal("10000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart_AMOUNT_VARIATION() {
        testFixedDepositAccountForInterestRate("period", new BigDecimal("2000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testFixedDepositAccountWithPeriodInterestRateChart_PERIOD_VARIATION() {
        testFixedDepositAccountForInterestRate("period", new BigDecimal("10000"), 18, new BigDecimal("7.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart() {
        testFixedDepositAccountForInterestRate("amount", new BigDecimal("10000"), 12, new BigDecimal("7.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart_AMOUNT_VARIATION() {
        testFixedDepositAccountForInterestRate("amount", new BigDecimal("5000"), 12, new BigDecimal("5.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountInterestRateChart_PERIOD_VARIATION() {
        testFixedDepositAccountForInterestRate("amount", new BigDecimal("10000"), 26, new BigDecimal("7.0"));
    }

    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart() {
        testFixedDepositAccountForInterestRate("period_amount", new BigDecimal("10000"), 12, new BigDecimal("7.0"));
    }

    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart_AMOUNT_VARIATION() {
        testFixedDepositAccountForInterestRate("period_amount", new BigDecimal("5000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testFixedDepositAccountWithPeriodAndAmountInterestRateChart_PERIOD_VARIATION() {
        testFixedDepositAccountForInterestRate("period_amount", new BigDecimal("10000"), 20, new BigDecimal("9.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart() {
        testFixedDepositAccountForInterestRate("amount_period", new BigDecimal("10000"), 12, new BigDecimal("8.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart_AMOUNT_VARIATION() {
        testFixedDepositAccountForInterestRate("amount_period", new BigDecimal("5000"), 12, new BigDecimal("6.0"));
    }

    @Test
    public void testFixedDepositAccountWithAmountAndPeriodInterestRateChart_PERIOD_VARIATION() {
        testFixedDepositAccountForInterestRate("amount_period", new BigDecimal("10000"), 6, new BigDecimal("7.0"));
    }

    private void testFixedDepositAccountForInterestRate(final String chartToUse, final BigDecimal depositAmount, final int depositPeriod,
            final BigDecimal interestRate) {
        final String VALID_FROM = "01 March 2014";
        final String VALID_TO = "01 March 2016";
        final String SUBMITTED_ON_DATE = "01 March 2015";
        final String APPROVED_ON_DATE = "01 March 2015";
        final String ACTIVATION_DATE = "01 March 2015";

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE, chartToUse);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM,
                depositAmount, depositPeriod);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        BigDecimal actualRate = fixedDepositHelper.getAccount(fixedDepositAccountId).getNominalAnnualInterestRate();
        assertEquals(0, interestRate.compareTo(actualRate),
                () -> "Expected nominal annual interest rate " + interestRate + " but was " + actualRate);
    }

    /***
     * Test case for FD Account premature closure with transaction transfers to savings account and Cash Based
     * accounting enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeTransferToSavings() {
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

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = daysInMonth - currentDate + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
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

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdSummary accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal depositAmount = accountSummary.getTotalDeposits();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(fixedDepositHelper.calculateInterest(fixedDepositAccountId));
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal totalInterestPosted = accountSummary.getTotalInterestPosted();

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        BigDecimal balanceBefore = savingsHelper.getSavingsDetails(savingsId).getSummary().getAccountBalance();

        Account financialAccount = getMappedLiabilityFinancialAccount();

        fixedDepositHelper.calculatePrematureAmount(fixedDepositAccountId, CLOSED_ON_DATE);

        Long prematureClosureTransactionId = fixedDepositHelper
                .prematureClose(fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_TRANSFER_TO_SAVINGS, savingsId).getResourceId();
        Assertions.assertNotNull(prematureClosureTransactionId);

        DepositTestValidators.verifyFixedDepositAccountIsPrematureClosed(statusOf(fixedDepositAccountId));

        BigDecimal prematurityAmount = fixedDepositHelper.getAccount(fixedDepositAccountId).getMaturityAmount();

        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, CLOSED_ON_DATE,
                credit(liabilityAccount, prematurityAmount), debit(liabilityAccount, prematurityAmount));
        journalEntryHelper.checkJournalEntryForAssetAccount(financialAccount, CLOSED_ON_DATE, debit(financialAccount, prematurityAmount),
                credit(financialAccount, prematurityAmount));

        BigDecimal balanceAfter = savingsHelper.getSavingsDetails(savingsId).getSummary().getAccountBalance();
        BigDecimal expectedSavingsBalance = balanceBefore.add(prematurityAmount);

        assertEquals(0, expectedSavingsBalance.compareTo(balanceAfter), () -> "Verifying Savings Account Balance after Premature Closure: "
                + "expected " + expectedSavingsBalance + " but was " + balanceAfter);
    }

    /***
     * Test case for Fixed Deposit Account premature closure with transaction type ReInvest and Cash Based accounting
     * enabled
     */
    @Test
    public void testFixedDepositAccountWithPrematureClosureTypeReinvest() {
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

        Integer currentDate = Integer.valueOf(currentDateFormat.format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        Integer numberOfDaysLeft = daysInMonth - currentDate + 1;
        todaysDate.add(Calendar.DATE, numberOfDaysLeft);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());
        final String CLOSED_ON_DATE = dateFormat.format(Calendar.getInstance().getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdSummary accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal depositAmount = accountSummary.getTotalDeposits();

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, depositAmount));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE, credit(liabilityAccount, depositAmount));

        Assertions.assertNotNull(fixedDepositHelper.calculateInterest(fixedDepositAccountId));
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        accountSummary = fixedDepositHelper.getSummary(fixedDepositAccountId);
        BigDecimal totalInterestPosted = accountSummary.getTotalInterestPosted();

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPosted));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPosted));

        fixedDepositHelper.calculatePrematureAmount(fixedDepositAccountId, CLOSED_ON_DATE);

        CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                () -> fixedDepositHelper.prematureClose(fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_REINVEST, null));
        assertEquals("validation.msg.fixeddepositaccount.onAccountClosureId.reinvest.not.allowed",
                FeignErrors.errorGlobalisationCode(exception));
    }

    @Test
    public void testFixedDepositAccountUpdation() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        String submittedOnDate = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        fixedDepositProductHelper.getAllProducts();
        fixedDepositProductHelper.getProduct(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, submittedOnDate, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        todaysDate.add(Calendar.DATE, -1);
        submittedOnDate = dateFormat.format(todaysDate.getTime());
        PostFixedDepositAccountsRequest updated = DepositRequestBuilders.fixedDepositAccount(clientId, fixedDepositProductId,
                submittedOnDate, WHOLE_TERM);
        var changes = fixedDepositHelper.updateApplication(fixedDepositAccountId, DepositRequestBuilders.asUpdate(updated)).getChanges();
        Assertions.assertNotNull(changes.getSubmittedOnDate(), "The update must report submittedOnDate as changed");
    }

    @Test
    public void testFixedDepositAccountUndoApproval() {
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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.undoApproval(fixedDepositAccountId);
        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));
    }

    @Test
    public void testFixedDepositAccountRejectedAndClosed() {
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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.reject(fixedDepositAccountId, REJECTED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsRejected(statusOf(fixedDepositAccountId));
        DepositTestValidators.verifyFixedDepositAccountIsClosed(statusOf(fixedDepositAccountId));
    }

    @Test
    public void testFixedDepositAccountWithdrawnByClientAndClosed() {
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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.withdrawApplication(fixedDepositAccountId, WITHDRAWN_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsWithdrawn(statusOf(fixedDepositAccountId));
        DepositTestValidators.verifyFixedDepositAccountIsClosed(statusOf(fixedDepositAccountId));
    }

    @Test
    public void testFixedDepositAccountIsDeleted() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

        Calendar todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -3);
        final String VALID_FROM = dateFormat.format(todaysDate.getTime());
        todaysDate.add(Calendar.YEAR, 10);
        final String VALID_TO = dateFormat.format(todaysDate.getTime());

        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        Long deletedId = fixedDepositHelper.deleteApplication(fixedDepositAccountId).getResourceId();
        Assertions.assertNotNull(deletedId);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_360_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_360, MONTHLY, MONTHLY, MONTHLY_INTERVAL, MONTHLY_INTERVAL, true);
    }

    @Test
    public void testMaturityAmountForMonthlyCompoundingAndMonthlyPosting_With_365_Days() {
        verifyMaturityAmountFromMonthStart(DAYS_365, MONTHLY, MONTHLY, MONTHLY_INTERVAL, MONTHLY_INTERVAL, false);
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
    public void testMaturityAmountForDailyCompoundingAndAnnuallyPosting_With_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, DAILY, ANNUALLY, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);
    }

    @Test
    public void testMaturityAmountDailyCompoundingAndAnnuallyPostingWith_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, DAILY, ANNUALLY, DAILY_COMPOUNDING_INTERVAL, ANNUL_INTERVAL);
    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, BI_ANNUALLY, BI_ANNUALLY, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);
    }

    @Test
    public void testFixedDepositWithBi_AnnualCompoundingAndPosting_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, BI_ANNUALLY, BI_ANNUALLY, BIANNULLY_INTERVAL, BIANNULLY_INTERVAL);
    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_365_Days() {
        verifyMaturityAmountFromYearStart(DAYS_365, QUARTERLY, QUARTERLY, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);
    }

    @Test
    public void testFixedDepositWithQuarterlyCompoundingAndQuarterlyPosting_360_Days() {
        verifyMaturityAmountFromYearStart(DAYS_360, QUARTERLY, QUARTERLY, QUARTERLY_INTERVAL, QUARTERLY_INTERVAL);
    }

    /**
     * Projects the maturity amount for a deposit opened on the first of last month, and compares it to the server's
     * within {@link #THRESHOLD}.
     *
     * @param reconfigure
     *            whether the interest configuration is re-sent as an update after the account is applied for; the
     *            365-day monthly case relies on the product default instead.
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
     * Projects the maturity amount for a deposit opened on the first of January of this year, and compares it to the
     * server's within {@link #THRESHOLD}.
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

        Long fixedDepositProductId = createFixedDepositProduct(validFrom, validTo, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, submittedOnDate, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        if (reconfigure) {
            updateInterestCalculationConfig(clientId, fixedDepositProductId, fixedDepositAccountId, submittedOnDate, daysInYearType,
                    WHOLE_TERM, INTEREST_CALCULATION_USING_DAILY_BALANCE, compoundingPeriodType, postingPeriodType);
        }

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, submittedOnDate);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdResponse account = fixedDepositHelper.getAccount(fixedDepositAccountId);
        BigDecimal principal = account.getDepositAmount();
        BigDecimal maturityAmount = account.getMaturityAmount();
        Integer depositPeriod = account.getDepositPeriod();
        Long daysInYear = account.getInterestCalculationDaysInYearType().getId();

        Set<GetInterestRateChartsChartSlabs> chartSlabs = interestRateChartHelper.getChartSlabsByProduct(fixedDepositProductId);
        BigDecimal interestRate = DepositInterestCalculator.interestRateFor(chartSlabs, depositPeriod);
        double interestPerDay = interestRate.doubleValue() / 100 / daysInYear;

        float projected = DepositInterestCalculator.principalAfterCompoundingInterest(openedOn, principal.floatValue(), depositPeriod,
                interestPerDay, compoundingInterval, postingInterval);

        assertWithinThreshold(BigDecimal.valueOf(projected), maturityAmount, "Verifying Maturity amount for Fixed Deposit Account");
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_360() {
        verifyPrematureClosureWithPenalInterest(DAYS_360, WHOLE_TERM, true);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestForWholeTerm_With_365() {
        verifyPrematureClosureWithPenalInterest(DAYS_365, WHOLE_TERM, false);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_365_Days() {
        verifyPrematureClosureWithPenalInterest(DAYS_365, TILL_PREMATURE_WITHDRAWAL, false);
    }

    @Test
    public void testPrematureClosureAmountWithPenalInterestTillPrematureWithdrawal_With_360_Days() {
        verifyPrematureClosureWithPenalInterest(DAYS_360, TILL_PREMATURE_WITHDRAWAL, true);
    }

    /**
     * Projects the pre-closure amount independently of the server and compares within {@link #THRESHOLD}.
     * <p>
     * With the penalty applied till premature withdrawal the rate comes from the slab covering the months actually
     * held, not the slab covering the full deposit period.
     */
    private void verifyPrematureClosureWithPenalInterest(final int daysInYearType, final int penalInterestType, final boolean reconfigure) {
        DateTimeFormatter dateFormat = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
        final boolean tillWithdrawal = penalInterestType == TILL_PREMATURE_WITHDRAWAL;

        LocalDate anchor = tillWithdrawal ? Utils.getLocalDateOfTenant().minusDays(32) : Utils.getLocalDateOfTenant();
        LocalDate chartDate = anchor.minusMonths(3);
        final String VALID_FROM = dateFormat.format(chartDate);
        final String VALID_TO = dateFormat.format(chartDate.plusYears(10));

        LocalDate activationDate = anchor.minusMonths(1).minusDays(1);
        final String SUBMITTED_ON_DATE = dateFormat.format(activationDate);
        final String APPROVED_ON_DATE = dateFormat.format(activationDate);
        final String ACTIVATION_DATE = dateFormat.format(activationDate);

        LocalDate closingDate = tillWithdrawal || daysInYearType == DAYS_360 ? Utils.getLocalDateOfTenant()
                : activationDate.plusMonths(1).plusDays(1);
        final String CLOSED_ON_DATE = dateFormat.format(closingDate);

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, NONE);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, penalInterestType);
        Assertions.assertNotNull(fixedDepositAccountId);

        if (reconfigure && !tillWithdrawal) {
            updateInterestCalculationConfig(clientId, fixedDepositProductId, fixedDepositAccountId, SUBMITTED_ON_DATE, daysInYearType,
                    penalInterestType, INTEREST_CALCULATION_USING_DAILY_BALANCE, MONTHLY, MONTHLY);
        }

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        GetFixedDepositAccountsAccountIdResponse account = fixedDepositHelper.getAccount(fixedDepositAccountId);
        BigDecimal principal = account.getDepositAmount();
        Integer depositPeriod = account.getDepositPeriod();
        Long daysInYear = account.getInterestCalculationDaysInYearType().getId();
        BigDecimal preClosurePenalInterestRate = account.getPreClosurePenalInterest();

        Set<GetInterestRateChartsChartSlabs> chartSlabs = interestRateChartHelper.getChartSlabsByProduct(fixedDepositProductId);
        int ratePeriod = tillWithdrawal ? Math.toIntExact(ChronoUnit.MONTHS.between(activationDate, closingDate)) : depositPeriod;
        BigDecimal interestRate = DepositInterestCalculator.interestRateFor(chartSlabs, ratePeriod).subtract(preClosurePenalInterestRate);
        double interestPerDay = interestRate.doubleValue() / 100 / daysInYear;

        long daysBetween = DAYS.between(activationDate, closingDate);
        BigDecimal totalInterest = BigDecimal.valueOf(interestPerDay * principal.doubleValue() * daysBetween);
        BigDecimal expectedPrematureAmount = principal.add(totalInterest);

        if (daysInYearType == DAYS_360 && !tillWithdrawal) {
            fixedDepositHelper.postInterest(fixedDepositAccountId);
        }

        fixedDepositHelper.calculatePrematureAmount(fixedDepositAccountId, CLOSED_ON_DATE);

        Long prematureClosureTransactionId = fixedDepositHelper
                .prematureClose(fixedDepositAccountId, CLOSED_ON_DATE, CLOSURE_TYPE_WITHDRAW_DEPOSIT, null).getResourceId();
        Assertions.assertNotNull(prematureClosureTransactionId);

        DepositTestValidators.verifyFixedDepositAccountIsPrematureClosed(statusOf(fixedDepositAccountId));

        BigDecimal maturityAmount = fixedDepositHelper.getAccount(fixedDepositAccountId).getMaturityAmount();
        assertWithinThreshold(expectedPrematureAmount, maturityAmount, "Verifying Pre-Closure maturity amount");
    }

    @Test
    public void testFixedDepositAccountWithRolloverMaturityAmount() {
        verifyRolloverMaturityInstruction(DepositTestData.AccountClosureType.REINVEST);
    }

    /***
     * Test case for Fixed Deposit Account rollover with maturity instruction as re invest principal only
     */
    @Test
    public void testFixedDepositAccountWithRolloverPrincipal() {
        verifyRolloverMaturityInstruction(DepositTestData.AccountClosureType.REINVEST_PRINCIPAL_ONLY);
    }

    private void verifyRolloverMaturityInstruction(final int maturityInstructionId) {
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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM,
                maturityInstructionId);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));
    }

    @Test
    public void testCloseFixedDepositForCLOSURE_TYPE_REINVEST_withConfigurationMaturityInstruction() {
        try {
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();

            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

            Calendar todaysDate = Calendar.getInstance();
            int currentYear = todaysDate.get(Calendar.YEAR);
            todaysDate.set(currentYear, Calendar.JANUARY, 1);
            final String VALID_FROM = dateFormat.format(todaysDate.getTime());
            todaysDate.set(currentYear + 1, Calendar.MARCH, 1);
            final String VALID_TO = dateFormat.format(todaysDate.getTime());

            todaysDate = Calendar.getInstance();
            todaysDate.set(currentYear, Calendar.JANUARY, 1);
            final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
            final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), LocalDate.of(currentYear + 1, 3, 1).toString());

            Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, ACCRUAL, assetAccount, liabilityAccount,
                    incomeAccount, expenseAccount);
            Assertions.assertNotNull(fixedDepositProductId);

            Long clientId = clientHelper.createClient();
            Assertions.assertNotNull(clientId);

            Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM,
                    CLOSURE_TYPE_REINVEST);
            Assertions.assertNotNull(fixedDepositAccountId);

            fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
            fixedDepositHelper.activate(fixedDepositAccountId, APPROVED_ON_DATE);

            schedulerHelper.executeAndAwaitJob("Update Deposit Accounts Maturity details");

            DepositTestValidators.verifyFixedDepositAccountIsClosed(statusOf(fixedDepositAccountId));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testCloseFixedDepositForCLOSURE_TYPE_REINVEST() {
        testClosureTypeReinvestVariants(CLOSURE_TYPE_REINVEST);
    }

    @Test
    public void testCloseFixedDepositForCLOSURE_TYPE_REINVEST_PRINCIPAL_ONLY() {
        testClosureTypeReinvestVariants(CLOSURE_TYPE_REINVEST_PRINCIPAL_ONLY);
    }

    public void testClosureTypeReinvestVariants(int reInvest) {
        try {
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();

            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);

            Calendar todaysDate = Calendar.getInstance();
            int currentYear = todaysDate.get(Calendar.YEAR);
            todaysDate.set(currentYear, Calendar.JANUARY, 1);
            final String VALID_FROM = dateFormat.format(todaysDate.getTime());
            todaysDate.set(currentYear + 1, Calendar.JANUARY, 1);
            final String VALID_TO = dateFormat.format(todaysDate.getTime());

            todaysDate = Calendar.getInstance();
            todaysDate.set(currentYear, Calendar.JANUARY, 1);
            final String SUBMITTED_ON_DATE = dateFormat.format(todaysDate.getTime());
            final String APPROVED_ON_DATE = dateFormat.format(todaysDate.getTime());

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            businessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE.name(), LocalDate.of(currentYear + 1, 1, 1).toString());

            Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, ACCRUAL, assetAccount, liabilityAccount,
                    incomeAccount, expenseAccount);
            Assertions.assertNotNull(fixedDepositProductId);

            Long clientId = clientHelper.createClient();
            Assertions.assertNotNull(clientId);

            Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM,
                    new BigDecimal("10000"), 12);
            Assertions.assertNotNull(fixedDepositAccountId);

            fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
            fixedDepositHelper.activate(fixedDepositAccountId, APPROVED_ON_DATE);

            schedulerHelper.executeAndAwaitJob("Update Deposit Accounts Maturity details");

            DepositTestValidators.verifyFixedDepositAccountIsMatured(statusOf(fixedDepositAccountId));

            todaysDate.set(currentYear + 1, Calendar.JANUARY, 1);
            final String CLOSED_ON_DATE = dateFormat.format(todaysDate.getTime());

            fixedDepositHelper.close(fixedDepositAccountId, CLOSED_ON_DATE, reInvest, null);

            DepositTestValidators.verifyFixedDepositAccountIsClosed(statusOf(fixedDepositAccountId));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testFixedDepositAccountUndoTransaction() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();

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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        fixedDepositHelper.calculateInterest(fixedDepositAccountId);
        Assertions.assertNotNull(fixedDepositHelper.postInterest(fixedDepositAccountId).getResourceId());

        // Fineract posts interest at the last day of the posting period, which for a MONTHLY product is the end of
        // the month the account was activated in.
        todaysDate = Calendar.getInstance();
        todaysDate.add(Calendar.MONTH, -1);
        Integer currentDay = Integer.valueOf(new SimpleDateFormat("dd", Locale.US).format(todaysDate.getTime()));
        Integer daysInMonth = todaysDate.getActualMaximum(Calendar.DATE);
        todaysDate.add(Calendar.DATE, daysInMonth - currentDay + 1);
        final String INTEREST_POSTED_DATE = dateFormat.format(todaysDate.getTime());

        BigDecimal totalInterestPostedBeforeUndo = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalInterestPosted();
        Assertions.assertNotNull(totalInterestPostedBeforeUndo);
        Assertions.assertTrue(totalInterestPostedBeforeUndo.compareTo(BigDecimal.ZERO) > 0, "Expected interest > 0 before undo");

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                debit(expenseAccount, totalInterestPostedBeforeUndo));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                credit(liabilityAccount, totalInterestPostedBeforeUndo));

        Long interestTransactionId = findTransactionId(fixedDepositAccountId, true);
        Assertions.assertNotNull(interestTransactionId);

        Assertions.assertNotNull(fixedDepositHelper.undoTransaction(fixedDepositAccountId, interestTransactionId));

        assertTransactionIsReversed(fixedDepositAccountId, interestTransactionId, "Interest posting");

        BigDecimal totalInterestPostedAfterUndo = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalInterestPosted();
        BigDecimal afterUndo = totalInterestPostedAfterUndo == null ? BigDecimal.ZERO : totalInterestPostedAfterUndo;
        assertEquals(0, BigDecimal.ZERO.compareTo(afterUndo), () -> "totalInterestPosted must be zero after undo but was " + afterUndo);

        journalEntryHelper.checkJournalEntryForAssetAccount(expenseAccount, INTEREST_POSTED_DATE,
                credit(expenseAccount, totalInterestPostedBeforeUndo));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, INTEREST_POSTED_DATE,
                debit(liabilityAccount, totalInterestPostedBeforeUndo));
    }

    @Test
    public void testFixedDepositAccountAdjustTransaction() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();

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

        Long clientId = clientHelper.createClient();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, CASH_BASED, assetAccount, liabilityAccount,
                incomeAccount, expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        Long fixedDepositAccountId = applyForFixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, WHOLE_TERM);
        Assertions.assertNotNull(fixedDepositAccountId);

        DepositTestValidators.verifyFixedDepositIsPending(statusOf(fixedDepositAccountId));

        fixedDepositHelper.approve(fixedDepositAccountId, APPROVED_ON_DATE);
        DepositTestValidators.verifyFixedDepositIsApproved(statusOf(fixedDepositAccountId));

        fixedDepositHelper.activate(fixedDepositAccountId, ACTIVATION_DATE);
        DepositTestValidators.verifyFixedDepositIsActive(statusOf(fixedDepositAccountId));

        Long depositTransactionId = findTransactionId(fixedDepositAccountId, false);
        Assertions.assertNotNull(depositTransactionId);

        BigDecimal totalDepositsBeforeAdjust = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalDeposits();
        Assertions.assertNotNull(totalDepositsBeforeAdjust);
        Assertions.assertTrue(totalDepositsBeforeAdjust.compareTo(BigDecimal.ZERO) > 0, "Expected totalDeposits > 0 before adjust");

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, debit(assetAccount, totalDepositsBeforeAdjust));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE,
                credit(liabilityAccount, totalDepositsBeforeAdjust));

        final BigDecimal newDepositAmount = new BigDecimal("200000.0");
        Assertions.assertNotNull(
                fixedDepositHelper.adjustTransaction(fixedDepositAccountId, depositTransactionId, ACTIVATION_DATE, newDepositAmount));

        assertTransactionIsReversed(fixedDepositAccountId, depositTransactionId, "Original deposit");

        BigDecimal totalDepositsAfterAdjust = fixedDepositHelper.getSummary(fixedDepositAccountId).getTotalDeposits();
        Assertions.assertNotNull(totalDepositsAfterAdjust);
        assertEquals(0, newDepositAmount.compareTo(totalDepositsAfterAdjust),
                () -> "totalDeposits must reflect new amount after adjust: expected " + newDepositAmount + " but was "
                        + totalDepositsAfterAdjust);

        journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, ACTIVATION_DATE, credit(assetAccount, totalDepositsBeforeAdjust));
        journalEntryHelper.checkJournalEntryForLiabilityAccount(liabilityAccount, ACTIVATION_DATE,
                debit(liabilityAccount, totalDepositsBeforeAdjust));
    }

    @AfterEach
    public void tearDown() {
        for (GetFinancialActivityAccountsResponse mapping : financialActivityAccountHelper.getAllMappings()) {
            Long deletedId = financialActivityAccountHelper.deleteMapping(mapping.getId()).getResourceId();
            Assertions.assertNotNull(deletedId);
            assertEquals(mapping.getId(), deletedId);
        }
    }

    private Long createFixedDepositProduct(final String validFrom, final String validTo, final int accountingRule, Account... accounts) {
        PostFixedDepositProductsRequest request = withAccounting(DepositRequestBuilders.fixedDepositProduct(), accountingRule, accounts);
        return fixedDepositProductHelper
                .createProduct(DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.periodRangeChartSlabs()))
                .getResourceId();
    }

    private Long createFixedDepositProductWithWithHoldTax(final String validFrom, final String validTo, final Long taxGroupId,
            final int accountingRule, Account... accounts) {
        PostFixedDepositProductsRequest request = withAccounting(DepositRequestBuilders.fixedDepositProduct(), accountingRule, accounts)
                .withHoldTax(true).taxGroupId(taxGroupId);
        return fixedDepositProductHelper
                .createProduct(DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.periodRangeChartSlabs()))
                .getResourceId();
    }

    private Long createFixedDepositProduct(final String validFrom, final String validTo, final int accountingRule,
            final String chartToBePicked, Account... accounts) {
        PostFixedDepositProductsRequest request = withAccounting(DepositRequestBuilders.fixedDepositProduct(), accountingRule, accounts);
        return fixedDepositProductHelper.createProduct(DepositRequestBuilders.withChart(request, validFrom, validTo,
                DepositTestData.chartSlabsFor(chartToBePicked), DepositTestData.isPrimaryGroupingByAmount(chartToBePicked)))
                .getResourceId();
    }

    private PostFixedDepositProductsRequest withAccounting(PostFixedDepositProductsRequest request, final int accountingRule,
            Account... accounts) {
        if (accountingRule == CASH_BASED) {
            return DepositRequestBuilders.withCashBasedAccounting(request, accounts[0], accounts[1], accounts[2], accounts[3]);
        }
        if (accountingRule == ACCRUAL) {
            return DepositRequestBuilders.withAccrualAccounting(request, accounts[0], accounts[1], accounts[2], accounts[3]);
        }
        return request.accountingRule(NONE);
    }

    private Long applyForFixedDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final int penalInterestType) {
        return fixedDepositHelper
                .submitApplication(DepositRequestBuilders.fixedDepositAccount(clientId, productId, submittedOnDate, penalInterestType))
                .getSavingsId();
    }

    private Long applyForFixedDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final int penalInterestType, final int maturityInstructionId) {
        return fixedDepositHelper.submitApplication(DepositRequestBuilders
                .fixedDepositAccount(clientId, productId, submittedOnDate, penalInterestType).maturityInstructionId(maturityInstructionId))
                .getSavingsId();
    }

    private Long applyForFixedDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final int penalInterestType, final BigDecimal depositAmount, final int depositPeriod) {
        return fixedDepositHelper
                .submitApplication(DepositRequestBuilders.fixedDepositAccount(clientId, productId, submittedOnDate, penalInterestType)
                        .depositAmount(depositAmount).depositPeriod(depositPeriod))
                .getSavingsId();
    }

    /** Re-sends the whole account body with the interest configuration overridden, as the update endpoint requires. */
    private void updateInterestCalculationConfig(final Long clientId, final Long productId, final Long accountId,
            final String submittedOnDate, final int daysInYearType, final int penalInterestType, final int interestCalculationType,
            final int compoundingPeriodType, final int postingPeriodType) {
        PostFixedDepositAccountsRequest request = DepositRequestBuilders
                .fixedDepositAccount(clientId, productId, submittedOnDate, penalInterestType)//
                .interestCalculationDaysInYearType(daysInYearType)//
                .interestCalculationType(interestCalculationType)//
                .interestCompoundingPeriodType(compoundingPeriodType)//
                .interestPostingPeriodType(postingPeriodType);
        fixedDepositHelper.updateApplication(accountId, DepositRequestBuilders.asUpdate(request));
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

    private GetFixedDepositAccountsStatus statusOf(final Long fixedDepositAccountId) {
        return fixedDepositHelper.getAccount(fixedDepositAccountId).getStatus();
    }

    private Long findTransactionId(final Long accountId, final boolean interestPosting) {
        for (GetFixedDepositAccountsAccountIdTransactionsResponse transaction : fixedDepositHelper.getTransactions(accountId)) {
            if (transaction.getTransactionType() == null) {
                continue;
            }
            Boolean matches = interestPosting ? transaction.getTransactionType().getInterestPosting()
                    : transaction.getTransactionType().getDeposit();
            if (Boolean.TRUE.equals(matches)) {
                return transaction.getId();
            }
        }
        return null;
    }

    private void assertTransactionIsReversed(final Long accountId, final Long transactionId, final String description) {
        List<GetFixedDepositAccountsAccountIdTransactionsResponse> transactions = fixedDepositHelper.getTransactions(accountId);
        boolean found = false;
        for (GetFixedDepositAccountsAccountIdTransactionsResponse transaction : transactions) {
            if (transactionId.equals(transaction.getId())) {
                Assertions.assertTrue(Boolean.TRUE.equals(transaction.getReversed()), description + " transaction must be marked reversed");
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, description + " transaction must still be present");
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
