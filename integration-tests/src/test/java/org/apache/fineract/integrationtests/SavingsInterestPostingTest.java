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

import static org.apache.fineract.integrationtests.common.BusinessDateHelper.runAt;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith({ SavingsTestLifecycleExtension.class })
public class SavingsInterestPostingTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsInterestPostingTest.class);
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private GlobalConfigurationHelper globalConfigurationHelper;
    private SavingsProductHelper productHelper;
    private JournalEntryHelper journalEntryHelper;

    private static final String ACCRUALS_JOB_NAME = "Add Accrual Transactions For Savings";
    private static final String POST_INTEREST_JOB_NAME = "Post Interest For Savings";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @AfterEach
    public void cleanupAfterTest() {
        cleanupSavingsAccountsFromDuplicatePreventionTest();
    }

    @Test
    public void testPostInterestWithOverdraftProduct() {
        runAt("12 March 2025", () -> {
            final String amount = "10000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(2025, 2, 1);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            savingsAccountHelper.approveSavingsOnDate(accountId, startDateString);
            savingsAccountHelper.activateSavings(accountId, startDateString);
            savingsAccountHelper.depositToSavingsAccount(accountId, amount, startDateString, CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate marchDate = LocalDate.of(2025, 3, 2);

            schedulerJobHelper.executeAndAwaitJob(ACCRUALS_JOB_NAME);
            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            long days = ChronoUnit.DAYS.between(startDate, marchDate.minusDays(1));
            BigDecimal expected = calcInterestPosting(productHelper, amount, days);

            List<HashMap> txs = getInterestTransactions(accountId);
            Assertions.assertEquals(expected, BigDecimal.valueOf(((Double) txs.get(0).get("amount"))), "ERROR in expected");

            long interestCount = countInterestOnDate(accountId, marchDate.minusDays(1));
            long overdraftCount = countOverdraftOnDate(accountId, marchDate.minusDays(1));
            Assertions.assertEquals(1L, interestCount, "Expected exactly one INTEREST posting on posting date");
            Assertions.assertEquals(0L, overdraftCount, "Expected NO OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftInterestWithOverdraftProduct() {
        runAt("12 March 2025", () -> {
            final String amount = "10000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(2025, 2, 1);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            savingsAccountHelper.approveSavingsOnDate(accountId, startDateString);
            savingsAccountHelper.activateSavings(accountId, startDateString);
            savingsAccountHelper.withdrawalFromSavingsAccount(accountId, amount, startDateString, CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate marchDate = LocalDate.of(2025, 3, 2);

            schedulerJobHelper.executeAndAwaitJob(ACCRUALS_JOB_NAME);
            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            long days = ChronoUnit.DAYS.between(startDate, marchDate.minusDays(1));
            BigDecimal expected = calcOverdraftPosting(productHelper, amount, days);

            List<HashMap> txs = getInterestTransactions(accountId);
            Assertions.assertEquals(expected, BigDecimal.valueOf(((Double) txs.get(0).get("amount"))));

            BigDecimal runningBalance = BigDecimal.valueOf(((Double) txs.get(0).get("runningBalance")));
            Assertions.assertTrue(MathUtil.isLessThanZero(runningBalance), "Running balance is not less than zero");

            long interestCount = countInterestOnDate(accountId, marchDate.minusDays(1));
            long overdraftCount = countOverdraftOnDate(accountId, marchDate.minusDays(1));
            Assertions.assertEquals(0L, interestCount, "Expected NO INTEREST posting on posting date");
            Assertions.assertEquals(1L, overdraftCount, "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftAndInterestPosting_WithOverdraftProduct_WhitBalanceLessZero() {
        runAt("12 March 2025", () -> {
            final String amountDeposit = "10000";
            final String amountWithdrawal = "20000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(2025, 2, 1);
            final String startStr = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startStr);
            savingsAccountHelper.approveSavingsOnDate(accountId, startStr);
            savingsAccountHelper.activateSavings(accountId, startStr);
            savingsAccountHelper.depositToSavingsAccount(accountId, amountDeposit, startStr, CommonConstants.RESPONSE_RESOURCE_ID);

            final LocalDate withdrawalDate = LocalDate.of(2025, 2, 16);
            final String withdrawalStr = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(withdrawalDate);
            savingsAccountHelper.withdrawalFromSavingsAccount(accountId, amountWithdrawal, withdrawalStr,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate marchDate = LocalDate.of(2025, 3, 2);

            schedulerJobHelper.executeAndAwaitJob(ACCRUALS_JOB_NAME);
            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            List<HashMap> txs = getInterestTransactions(accountId);
            for (HashMap tx : txs) {
                BigDecimal amt = BigDecimal.valueOf(((Double) tx.get("amount")));
                @SuppressWarnings("unchecked")
                Map<String, Object> typeMap = (Map<String, Object>) tx.get("transactionType");
                SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) typeMap.get("id")).intValue());

                if (type.isInterestPosting()) {
                    long days = ChronoUnit.DAYS.between(startDate, withdrawalDate);
                    BigDecimal expected = calcInterestPosting(productHelper, amountDeposit, days);
                    Assertions.assertEquals(expected, amt);
                } else {
                    long days = ChronoUnit.DAYS.between(withdrawalDate, marchDate.minusDays(1));
                    BigDecimal overdraftBase = new BigDecimal(amountWithdrawal).subtract(new BigDecimal(amountDeposit));
                    BigDecimal expected = calcOverdraftPosting(productHelper, overdraftBase.toString(), days);
                    Assertions.assertEquals(expected, amt);
                }
            }

            Assertions.assertEquals(1L, countInterestOnDate(accountId, marchDate.minusDays(1)),
                    "Expected exactly one INTEREST posting on posting date");
            Assertions.assertEquals(1L, countOverdraftOnDate(accountId, marchDate.minusDays(1)),
                    "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftAndInterestPosting_WithOverdraftProduct_WhitBalanceGreaterZero() {
        runAt("12 March 2025", () -> {
            final String amountDeposit = "20000";
            final String amountWithdrawal = "10000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(2025, 2, 1);
            final String startStr = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startStr);
            savingsAccountHelper.approveSavingsOnDate(accountId, startStr);
            savingsAccountHelper.activateSavings(accountId, startStr);
            savingsAccountHelper.withdrawalFromSavingsAccount(accountId, amountWithdrawal, startStr, CommonConstants.RESPONSE_RESOURCE_ID);

            final LocalDate depositDate = LocalDate.of(2025, 2, 16);
            final String depositStr = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(depositDate);
            savingsAccountHelper.depositToSavingsAccount(accountId, amountDeposit, depositStr, CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate marchDate = LocalDate.of(2025, 3, 2);

            schedulerJobHelper.executeAndAwaitJob(ACCRUALS_JOB_NAME);
            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            List<HashMap> txs = getInterestTransactions(accountId);
            for (HashMap tx : txs) {
                BigDecimal amt = BigDecimal.valueOf(((Double) tx.get("amount")));
                @SuppressWarnings("unchecked")
                Map<String, Object> typeMap = (Map<String, Object>) tx.get("transactionType");
                SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) typeMap.get("id")).intValue());

                if (type.isOverDraftInterestPosting()) {
                    long days = ChronoUnit.DAYS.between(startDate, depositDate);
                    BigDecimal expected = calcOverdraftPosting(productHelper, amountWithdrawal, days);
                    Assertions.assertEquals(expected, amt);
                } else {
                    long days = ChronoUnit.DAYS.between(depositDate, marchDate.minusDays(1));
                    BigDecimal positiveBase = new BigDecimal(amountDeposit).subtract(new BigDecimal(amountWithdrawal));
                    BigDecimal expected = calcInterestPosting(productHelper, positiveBase.toString(), days);
                    Assertions.assertEquals(expected, amt);
                }
            }

            Assertions.assertEquals(1L, countOverdraftOnDate(accountId, marchDate.minusDays(1)),
                    "Expected exactly one OVERDRAFT posting on posting date");
            Assertions.assertEquals(1L, countInterestOnDate(accountId, marchDate.minusDays(1)),
                    "Expected exactly one INTEREST posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testPostInterestNotZero() {
        runAt("12 March 2025", () -> {
            final String amountDeposit = "1000";
            final String amountWithdrawal = "1000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(LocalDate.now(Utils.getZoneIdOfTenant()).getYear(), 1, 1);
            final String startStr = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startStr);
            savingsAccountHelper.approveSavingsOnDate(accountId, startStr);
            savingsAccountHelper.activateSavings(accountId, startStr);
            savingsAccountHelper.depositToSavingsAccount(accountId, amountDeposit, startStr, CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate februaryDate = LocalDate.of(2025, 2, 1);

            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            List<HashMap> txsFebruary = getInterestTransactions(accountId);

            long daysFebruary = ChronoUnit.DAYS.between(startDate, februaryDate);
            BigDecimal expectedFebruary = calcInterestPosting(productHelper, amountDeposit, daysFebruary);
            Assertions.assertEquals(expectedFebruary, BigDecimal.valueOf(((Double) txsFebruary.get(0).get("amount"))));

            final LocalDate withdrawalDate = LocalDate.of(2025, 2, 1);
            final String withdrawal = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(withdrawalDate);

            BigDecimal runningBalance = new BigDecimal(txsFebruary.get(0).get("runningBalance").toString());
            String withdrawalRunning = runningBalance.setScale(2, RoundingMode.HALF_UP).toString();

            savingsAccountHelper.withdrawalFromSavingsAccount(accountId, withdrawalRunning, withdrawal,
                    CommonConstants.RESPONSE_RESOURCE_ID);
            savingsAccountHelper.withdrawalFromSavingsAccount(accountId, amountWithdrawal, withdrawal,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            LocalDate marchDate = LocalDate.of(2025, 3, 1);

            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            List<HashMap> txs = getInterestTransactions(accountId);

            for (HashMap tx : txs) {
                BigDecimal amt = BigDecimal.valueOf(((Double) tx.get("amount")));
                @SuppressWarnings("unchecked")
                Map<String, Object> typeMap = (Map<String, Object>) tx.get("transactionType");
                SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) typeMap.get("id")).intValue());
                if (type.isOverDraftInterestPosting()) {
                    long days = ChronoUnit.DAYS.between(withdrawalDate, marchDate);
                    BigDecimal decimalsss = new BigDecimal(txsFebruary.get(0).get("runningBalance").toString())
                            .subtract(runningBalance.setScale(2, RoundingMode.HALF_UP));
                    BigDecimal withdraw = new BigDecimal(amountWithdrawal);
                    BigDecimal res = withdraw.subtract(decimalsss);
                    BigDecimal expected = calcOverdraftPosting(productHelper, res.toString(), days);
                    Assertions.assertEquals(expected, amt);
                }
            }

            Assertions.assertEquals(0L, countInterestOnDate(accountId, marchDate), "Expected exactly one INTEREST posting on posting date");
            Assertions.assertEquals(1L, countOverdraftOnDate(accountId, marchDate),
                    "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testPostInterestForDuplicatePrevention() {
        runAt("18 March 2025", () -> {
            final String amount = "10000";

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account liabilityAccount = accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

            final Integer productId = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final LocalDate startDate = LocalDate.of(2025, 2, 1);

            List<Integer> accountIdList = new ArrayList<>();
            for (int i = 0; i < 800; i++) {

                final Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
                final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
                final Integer accountId = savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, productId,
                        SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);

                savingsAccountHelper.approveSavingsOnDate(accountId, startDateString);
                savingsAccountHelper.activateSavings(accountId, startDateString);
                savingsAccountHelper.depositToSavingsAccount(accountId, amount, startDateString, CommonConstants.RESPONSE_RESOURCE_ID);

                accountIdList.add(accountId);
            }
            Assertions.assertEquals(800, accountIdList.size(), "ERROR: Expected 800");

            schedulerJobHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            for (Integer accountId : accountIdList) {
                List<HashMap> txs = getInterestTransactions(accountId);
                Assertions.assertEquals(1, txs.size(), "ERROR: Duplicate interest postings exist.");
            }
        });
    }

    private void cleanupSavingsAccountsFromDuplicatePreventionTest() {
        try {
            LOG.info("Starting cleanup of savings accounts after duplicate prevention test");

            List<Long> savingsIds = SavingsAccountHelper.getSavingsIdsByStatusId(300);
            if (!savingsIds.isEmpty()) {
                LOG.info("Found {} savings accounts to cleanup", savingsIds.size());

                savingsIds.forEach(savingsId -> {
                    try {

                        savingsAccountHelper.postInterestForSavings(savingsId.intValue());

                        savingsAccountHelper.closeSavingsAccount(savingsId,
                                new PostSavingsAccountsAccountIdRequest().locale("en").dateFormat(Utils.DATE_FORMAT)
                                        .closedOnDate(Utils.dateFormatter.format(Utils.getLocalDateOfTenant())).withdrawBalance(true));

                        LOG.debug("Savings account {} closed successfully", savingsId);
                    } catch (Exception e) {
                        LOG.warn("Unable to close savings account {}: {}", savingsId, e.getMessage());
                    }
                });

                LOG.info("Savings accounts cleanup completed");
            } else {
                LOG.info("No savings accounts found to cleanup");
            }
        } catch (Exception e) {
            LOG.error("Error during savings accounts cleanup: {}", e.getMessage(), e);
        }
    }

    private List<HashMap> getInterestTransactions(Integer savingsAccountId) {
        List<HashMap> all = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
        List<HashMap> filtered = new ArrayList<>();
        for (HashMap tx : all) {
            @SuppressWarnings("unchecked")
            Map<String, Object> txType = (Map<String, Object>) tx.get("transactionType");
            SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) txType.get("id")).intValue());
            if (type.isInterestPosting() || type.isOverDraftInterestPosting()) {
                filtered.add(tx);
            }
        }
        return filtered;
    }

    public Integer createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(final String interestPayableAccount,
            final String savingsControlAccount, final String interestReceivableAccount, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITHOUT OVERDRAFT ---------------------------------------");
        this.productHelper = new SavingsProductHelper().withOverDraftRate("100000", "21")
                .withAccountInterestReceivables(interestReceivableAccount).withSavingsControlAccountId(savingsControlAccount)
                .withInterestPayableAccountId(interestPayableAccount).withInterestCompoundingPeriodTypeAsAnnually()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                .withAccountingRuleAsAccrualBased(accounts);

        final String savingsProductJSON = this.productHelper.build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private BigDecimal calcInterestPosting(SavingsProductHelper productHelper, String amount, long days) {
        BigDecimal rate = productHelper.getNominalAnnualInterestRate().divide(new BigDecimal("100.00"));
        BigDecimal principal = new BigDecimal(amount);
        BigDecimal dayFactor = BigDecimal.ONE.divide(productHelper.getInterestCalculationDaysInYearType(), MathContext.DECIMAL64);
        BigDecimal dailyRate = rate.multiply(dayFactor, MathContext.DECIMAL64);
        BigDecimal periodRate = dailyRate.multiply(BigDecimal.valueOf(days), MathContext.DECIMAL64);
        return principal.multiply(periodRate, MathContext.DECIMAL64).setScale(productHelper.getDecimalCurrency(), RoundingMode.HALF_EVEN);
    }

    private BigDecimal calcOverdraftPosting(SavingsProductHelper productHelper, String amount, long days) {
        BigDecimal rate = productHelper.getNominalAnnualInterestRateOverdraft().divide(new BigDecimal("100.00"));
        BigDecimal principal = new BigDecimal(amount);
        BigDecimal dayFactor = BigDecimal.ONE.divide(productHelper.getInterestCalculationDaysInYearType(), MathContext.DECIMAL64);
        BigDecimal dailyRate = rate.multiply(dayFactor, MathContext.DECIMAL64);
        BigDecimal periodRate = dailyRate.multiply(BigDecimal.valueOf(days), MathContext.DECIMAL64);
        return principal.multiply(periodRate, MathContext.DECIMAL64).setScale(productHelper.getDecimalCurrency(), RoundingMode.HALF_EVEN);
    }

    @SuppressWarnings("unchecked")
    private LocalDate coerceToLocalDate(HashMap tx) {
        String[] candidateKeys = new String[] { "date", "transactionDate", "submittedOnDate", "createdDate" };

        for (String key : candidateKeys) {
            Object v = tx.get(key);
            if (v == null) {
                continue;
            }

            if (v instanceof List<?>) {
                List<?> arr = (List<?>) v;
                if (arr.size() >= 3 && arr.get(0) instanceof Number && arr.get(1) instanceof Number && arr.get(2) instanceof Number) {
                    int year = ((Number) arr.get(0)).intValue();
                    int month = ((Number) arr.get(1)).intValue();
                    int day = ((Number) arr.get(2)).intValue();
                    return LocalDate.of(year, month, day);
                }
            }

            if (v instanceof String) {
                String s = (String) v;
                DateTimeFormatter[] fmts = new DateTimeFormatter[] { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US),
                        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US), DateTimeFormatter.ofPattern("yyyy-MM-dd") };
                for (DateTimeFormatter f : fmts) {
                    try {
                        return LocalDate.parse(s, f);
                    } catch (Exception ignore) {
                        // intentionally ignored
                    }
                }
            }
        }
        return null;
    }

    private boolean isDate(HashMap tx, LocalDate expected) {
        LocalDate got = coerceToLocalDate(tx);
        return got != null && got.isEqual(expected);
    }

    @SuppressWarnings("unchecked")
    private SavingsAccountTransactionType txType(HashMap tx) {
        Map<String, Object> typeMap = (Map<String, Object>) tx.get("transactionType");
        return SavingsAccountTransactionType.fromInt(((Double) typeMap.get("id")).intValue());
    }

    private long countInterestOnDate(Integer accountId, LocalDate date) {
        List<HashMap> all = savingsAccountHelper.getSavingsTransactions(accountId);
        return all.stream().filter(tx -> isDate(tx, date)).map(this::txType).filter(SavingsAccountTransactionType::isInterestPosting)
                .count();
    }

    private long countOverdraftOnDate(Integer accountId, LocalDate date) {
        List<HashMap> all = savingsAccountHelper.getSavingsTransactions(accountId);
        return all.stream().filter(tx -> isDate(tx, date)).map(this::txType)
                .filter(SavingsAccountTransactionType::isOverDraftInterestPosting).count();
    }

    @SuppressWarnings({ "rawtypes" })
    private boolean isReversed(HashMap tx) {
        Object v = tx.get("reversed");
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0;
        }
        if (v instanceof String) {
            return Boolean.parseBoolean((String) v);
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void assertNoAccrualReversals(Integer accountId) {
        List<HashMap> all = savingsAccountHelper.getSavingsTransactions(accountId);
        long reversedAccruals = all.stream().filter(tx -> {
            SavingsAccountTransactionType t = txType(tx);
            return t.isAccrual() && isReversed(tx);
        }).count();
        Assertions.assertEquals(0L, reversedAccruals, "Accrual reversals were found in account transactions");
    }
}
