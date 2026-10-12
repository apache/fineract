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

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.ParallelExecutionHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(1)
public class SavingsInterestPostingTest extends FeignSavingsTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsInterestPostingTest.class);

    private static final String ACCRUALS_JOB_NAME = "Add Accrual Transactions For Savings";
    private static final String POST_INTEREST_JOB_NAME = "Post Interest For Savings";

    private static final int DUPLICATE_PREVENTION_ACCOUNT_COUNT = 50;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2025";

    private static final Double INTEREST_RATE = 10.0;
    private static final Double OVERDRAFT_INTEREST_RATE = 21.0;
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("100000");
    private static final BigDecimal PERCENT = new BigDecimal("100.00");
    private static final int DIGITS_AFTER_DECIMAL = 4;

    private final Set<Long> createdSavingsAccountIds = Collections.synchronizedSet(new LinkedHashSet<>());

    @AfterEach
    public void cleanupAfterTest() {
        closeTrackedSavingsAccounts();
    }

    @Test
    public void testPostInterestWithOverdraftProduct() {
        businessDateHelper.runAt("2025-03-12", () -> {
            final BigDecimal amount = new BigDecimal("10000");
            final LocalDate startDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final LocalDate postingDate = LocalDate.of(2025, Month.MARCH, 2).minusDays(1);

            final Long accountId = createActiveOverdraftAccount(startDate);
            deposit(accountId, amount.toPlainString(), DATE_FORMATTER.format(startDate));

            runAccrualAndPostInterestJobs();

            final long days = ChronoUnit.DAYS.between(startDate, postingDate);
            final List<SavingsAccountTransactionData> interestTransactions = getInterestTransactions(accountId);
            assertFalse(interestTransactions.isEmpty(), "No interest postings were found");
            SavingsTestValidators.verifyAmount(interestForPeriod(INTEREST_RATE, amount, days), interestTransactions.get(0).getAmount(),
                    "Verifying the posted interest");

            assertEquals(1L, countInterestPostingsOn(accountId, postingDate), "Expected exactly one INTEREST posting on posting date");
            assertEquals(0L, countOverdraftPostingsOn(accountId, postingDate), "Expected NO OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftInterestWithOverdraftProduct() {
        businessDateHelper.runAt("2025-03-12", () -> {
            final BigDecimal amount = new BigDecimal("10000");
            final LocalDate startDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final LocalDate postingDate = LocalDate.of(2025, Month.MARCH, 2).minusDays(1);

            final Long accountId = createActiveOverdraftAccount(startDate);
            withdraw(accountId, amount.toPlainString(), DATE_FORMATTER.format(startDate));

            runAccrualAndPostInterestJobs();

            final long days = ChronoUnit.DAYS.between(startDate, postingDate);
            final List<SavingsAccountTransactionData> interestTransactions = getInterestTransactions(accountId);
            assertFalse(interestTransactions.isEmpty(), "No interest postings were found");
            SavingsTestValidators.verifyAmount(interestForPeriod(OVERDRAFT_INTEREST_RATE, amount, days),
                    interestTransactions.get(0).getAmount(), "Verifying the posted overdraft interest");

            assertTrue(interestTransactions.get(0).getRunningBalance().signum() < 0, "Running balance is not less than zero");

            assertEquals(0L, countInterestPostingsOn(accountId, postingDate), "Expected NO INTEREST posting on posting date");
            assertEquals(1L, countOverdraftPostingsOn(accountId, postingDate), "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftAndInterestPosting_WithOverdraftProduct_WhitBalanceLessZero() {
        businessDateHelper.runAt("2025-03-12", () -> {
            final BigDecimal amountDeposit = new BigDecimal("10000");
            final BigDecimal amountWithdrawal = new BigDecimal("20000");
            final LocalDate startDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final LocalDate withdrawalDate = LocalDate.of(2025, Month.FEBRUARY, 16);
            final LocalDate postingDate = LocalDate.of(2025, Month.MARCH, 2).minusDays(1);

            final Long accountId = createActiveOverdraftAccount(startDate);
            deposit(accountId, amountDeposit.toPlainString(), DATE_FORMATTER.format(startDate));
            withdraw(accountId, amountWithdrawal.toPlainString(), DATE_FORMATTER.format(withdrawalDate));

            runAccrualAndPostInterestJobs();

            for (SavingsAccountTransactionData transaction : getInterestTransactions(accountId)) {
                final BigDecimal expected;
                if (Boolean.TRUE.equals(transaction.getTransactionType().getInterestPosting())) {
                    expected = interestForPeriod(INTEREST_RATE, amountDeposit, ChronoUnit.DAYS.between(startDate, withdrawalDate));
                } else {
                    expected = interestForPeriod(OVERDRAFT_INTEREST_RATE, amountWithdrawal.subtract(amountDeposit),
                            ChronoUnit.DAYS.between(withdrawalDate, postingDate));
                }
                SavingsTestValidators.verifyAmount(expected, transaction.getAmount(), "Verifying the posting of " + transaction.getDate());
            }

            assertEquals(1L, countInterestPostingsOn(accountId, postingDate), "Expected exactly one INTEREST posting on posting date");
            assertEquals(1L, countOverdraftPostingsOn(accountId, postingDate), "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testOverdraftAndInterestPosting_WithOverdraftProduct_WhitBalanceGreaterZero() {
        businessDateHelper.runAt("2025-03-12", () -> {
            final BigDecimal amountDeposit = new BigDecimal("20000");
            final BigDecimal amountWithdrawal = new BigDecimal("10000");
            final LocalDate startDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final LocalDate depositDate = LocalDate.of(2025, Month.FEBRUARY, 16);
            final LocalDate postingDate = LocalDate.of(2025, Month.MARCH, 2).minusDays(1);

            final Long accountId = createActiveOverdraftAccount(startDate);
            withdraw(accountId, amountWithdrawal.toPlainString(), DATE_FORMATTER.format(startDate));
            deposit(accountId, amountDeposit.toPlainString(), DATE_FORMATTER.format(depositDate));

            runAccrualAndPostInterestJobs();

            for (SavingsAccountTransactionData transaction : getInterestTransactions(accountId)) {
                final BigDecimal expected;
                if (Boolean.TRUE.equals(transaction.getTransactionType().getOverDraftInterestPosting())) {
                    expected = interestForPeriod(OVERDRAFT_INTEREST_RATE, amountWithdrawal,
                            ChronoUnit.DAYS.between(startDate, depositDate));
                } else {
                    expected = interestForPeriod(INTEREST_RATE, amountDeposit.subtract(amountWithdrawal),
                            ChronoUnit.DAYS.between(depositDate, postingDate));
                }
                SavingsTestValidators.verifyAmount(expected, transaction.getAmount(), "Verifying the posting of " + transaction.getDate());
            }

            assertEquals(1L, countOverdraftPostingsOn(accountId, postingDate), "Expected exactly one OVERDRAFT posting on posting date");
            assertEquals(1L, countInterestPostingsOn(accountId, postingDate), "Expected exactly one INTEREST posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testPostInterestNotZero() {
        businessDateHelper.runAt("2025-03-12", () -> {
            final BigDecimal amountDeposit = new BigDecimal("1000");
            final BigDecimal amountWithdrawal = new BigDecimal("1000");
            final LocalDate startDate = LocalDate.of(2025, Month.JANUARY, 1);
            final LocalDate februaryDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final LocalDate marchDate = LocalDate.of(2025, Month.MARCH, 1);

            final Long accountId = createActiveOverdraftAccount(startDate);
            deposit(accountId, amountDeposit.toPlainString(), DATE_FORMATTER.format(startDate));

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> februaryPostings = getInterestTransactions(accountId);
            assertFalse(februaryPostings.isEmpty(), "No interest postings were found for February");
            SavingsTestValidators.verifyAmount(
                    interestForPeriod(INTEREST_RATE, amountDeposit, ChronoUnit.DAYS.between(startDate, februaryDate)),
                    februaryPostings.get(0).getAmount(), "Verifying the February posting");

            // withdrawing the running balance leaves the account at zero, so the next withdrawal opens the overdraft
            final BigDecimal runningBalance = februaryPostings.get(0).getRunningBalance();
            final BigDecimal roundedRunningBalance = runningBalance.setScale(2, RoundingMode.HALF_UP);
            final String withdrawalDateString = DATE_FORMATTER.format(februaryDate);
            withdraw(accountId, roundedRunningBalance.toPlainString(), withdrawalDateString);
            withdraw(accountId, amountWithdrawal.toPlainString(), withdrawalDateString);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final BigDecimal overdrawnBalance = amountWithdrawal.subtract(runningBalance.subtract(roundedRunningBalance));
            for (SavingsAccountTransactionData transaction : getInterestTransactions(accountId)) {
                if (Boolean.TRUE.equals(transaction.getTransactionType().getOverDraftInterestPosting())) {
                    SavingsTestValidators.verifyAmount(
                            interestForPeriod(OVERDRAFT_INTEREST_RATE, overdrawnBalance, ChronoUnit.DAYS.between(februaryDate, marchDate)),
                            transaction.getAmount(), "Verifying the overdraft posting of " + transaction.getDate());
                }
            }

            assertEquals(0L, countInterestPostingsOn(accountId, marchDate), "Expected NO INTEREST posting on posting date");
            assertEquals(1L, countOverdraftPostingsOn(accountId, marchDate), "Expected exactly one OVERDRAFT posting on posting date");

            assertNoAccrualReversals(accountId);
        });
    }

    @Test
    public void testPostInterestForDuplicatePrevention() {
        businessDateHelper.runAt("2025-03-18", () -> {
            final String amount = "10000";
            final LocalDate startDate = LocalDate.of(2025, Month.FEBRUARY, 1);
            final String startDateString = DATE_FORMATTER.format(startDate);

            final Long productId = createOverdraftAccrualProduct();

            final List<Long> accountIdList = new CopyOnWriteArrayList<>();
            ParallelExecutionHelper.runInParallel(IntStream.range(0, DUPLICATE_PREVENTION_ACCOUNT_COUNT).boxed().toList(), (i) -> {
                final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
                final Long accountId = createTrackedSavingsAccount(clientId, productId, startDateString);

                approveSavings(accountId, startDateString);
                activateSavings(accountId, startDateString);
                deposit(accountId, amount, startDateString);
                accountIdList.add(accountId);
            });
            assertEquals(DUPLICATE_PREVENTION_ACCOUNT_COUNT, accountIdList.size(), "ERROR: Expected " + DUPLICATE_PREVENTION_ACCOUNT_COUNT);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(2)).untilAsserted(() -> {
                ParallelExecutionHelper.runInParallel(accountIdList, (accountId) -> assertEquals(1,
                        getInterestTransactions(accountId).size(), "ERROR: Duplicate interest postings exist for account " + accountId));
            });
        });
    }

    @Test
    public void testNoDuplicateInterestPostingWhenMixingManualAndJobRunsAcrossBusinessDates() {
        final String accountOpeningDate = "01 January 2025";
        final String firstManualPostingDate = "02 February 2025";
        final String manualAsOnPostingDate = "15 March 2025";
        final String depositAmount = "10000";

        final Long[] savingsAccountId = new Long[1];
        final int[] interestTxAfterFirstManualPost = new int[1];
        final int[] interestTxAfterManualAsOnPost = new int[1];

        businessDateHelper.runAt("2025-01-01", () -> {
            final Long clientId = createClient(accountOpeningDate);
            final Long productId = createSavingsProduct(
                    SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.ANNUAL,
                            SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                    .getResourceId();

            final Long accountId = createTrackedSavingsAccount(clientId, productId, accountOpeningDate);
            approveSavings(accountId, accountOpeningDate);
            activateSavings(accountId, accountOpeningDate);
            deposit(accountId, depositAmount, accountOpeningDate);
            savingsAccountId[0] = accountId;
        });

        businessDateHelper.runAt("2025-02-02", () -> {
            savingsHelper.postInterest(savingsAccountId[0]);
            interestTxAfterFirstManualPost[0] = getActiveInterestTransactions(savingsAccountId[0]).size();
            assertTrue(interestTxAfterFirstManualPost[0] > 0, "Expected interest transactions after first manual posting");
        });

        businessDateHelper.runAt("2025-03-15", () -> {
            savingsTransactionHelper.postInterestAsOn(savingsAccountId[0], manualAsOnPostingDate);
            interestTxAfterManualAsOnPost[0] = getActiveInterestTransactions(savingsAccountId[0]).size();
            assertTrue(interestTxAfterManualAsOnPost[0] > interestTxAfterFirstManualPost[0],
                    "Expected additional interest transaction(s) after manual post-as-on execution");
        });

        businessDateHelper.runAt("2025-04-02", () -> {
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> activeInterestTransactions = getActiveInterestTransactions(savingsAccountId[0]);
            assertTrue(activeInterestTransactions.size() >= interestTxAfterManualAsOnPost[0],
                    "Scheduler run should not reduce number of posted interest transactions");

            final Map<LocalDate, Integer> interestTransactionCountByDate = new HashMap<>();
            for (SavingsAccountTransactionData transaction : activeInterestTransactions) {
                assertNotNull(transaction.getDate(), "Could not determine date of an interest transaction");
                interestTransactionCountByDate.merge(transaction.getDate(), 1, Integer::sum);
            }

            interestTransactionCountByDate
                    .forEach((txDate, txCount) -> assertEquals(1, txCount, "Multiple interest postings found on " + txDate));
        });
    }

    /** Ordered the way the server does it: the annual rate becomes a daily one before it meets the balance. */
    private BigDecimal interestForPeriod(final Double annualRate, final BigDecimal balance, final long days) {
        final BigDecimal rate = BigDecimal.valueOf(annualRate).divide(PERCENT);
        final BigDecimal dayFactor = BigDecimal.ONE.divide(BigDecimal.valueOf(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365),
                MathContext.DECIMAL64);
        final BigDecimal dailyRate = rate.multiply(dayFactor, MathContext.DECIMAL64);
        final BigDecimal periodRate = dailyRate.multiply(BigDecimal.valueOf(days), MathContext.DECIMAL64);
        return balance.multiply(periodRate, MathContext.DECIMAL64).setScale(DIGITS_AFTER_DECIMAL, RoundingMode.HALF_EVEN);
    }

    private void runAccrualAndPostInterestJobs() {
        schedulerHelper.executeAndAwaitJob(ACCRUALS_JOB_NAME);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
    }

    private List<SavingsAccountTransactionData> getInterestTransactions(final Long savingsAccountId) {
        return savingsTransactionHelper.getTransactions(savingsAccountId).stream().filter(SavingsInterestPostingTest::isInterestPosting)
                .toList();
    }

    private List<SavingsAccountTransactionData> getActiveInterestTransactions(final Long savingsAccountId) {
        return getInterestTransactions(savingsAccountId).stream().filter(transaction -> !Boolean.TRUE.equals(transaction.getReversed()))
                .toList();
    }

    private static boolean isInterestPosting(final SavingsAccountTransactionData transaction) {
        return transaction.getTransactionType() != null && (Boolean.TRUE.equals(transaction.getTransactionType().getInterestPosting())
                || Boolean.TRUE.equals(transaction.getTransactionType().getOverDraftInterestPosting()));
    }

    private long countInterestPostingsOn(final Long savingsAccountId, final LocalDate date) {
        return savingsTransactionHelper.getTransactions(savingsAccountId).stream().filter(t -> date.equals(t.getDate()))
                .filter(t -> t.getTransactionType() != null && Boolean.TRUE.equals(t.getTransactionType().getInterestPosting())).count();
    }

    private long countOverdraftPostingsOn(final Long savingsAccountId, final LocalDate date) {
        return savingsTransactionHelper.getTransactions(savingsAccountId).stream().filter(t -> date.equals(t.getDate()))
                .filter(t -> t.getTransactionType() != null && Boolean.TRUE.equals(t.getTransactionType().getOverDraftInterestPosting()))
                .count();
    }

    private void assertNoAccrualReversals(final Long savingsAccountId) {
        final long reversedAccruals = savingsTransactionHelper.getAccrualTransactions(savingsAccountId).stream()
                .filter(transaction -> Boolean.TRUE.equals(transaction.getReversed())).count();
        assertEquals(0L, reversedAccruals, "Accrual reversals were found in account transactions");
    }

    private Long createActiveOverdraftAccount(final LocalDate startDate) {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        assertNotNull(clientId);

        final Long productId = createOverdraftAccrualProduct();
        final String startDateString = DATE_FORMATTER.format(startDate);
        final Long accountId = createTrackedSavingsAccount(clientId, productId, startDateString);

        approveSavings(accountId, startDateString);
        activateSavings(accountId, startDateString);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(accountId));
        return accountId;
    }

    private Long createOverdraftAccrualProduct() {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");
        final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
        final Account savingsControlAccount = accountHelper.createLiabilityAccount("Savings Control");
        final Account interestPayableAccount = accountHelper.createLiabilityAccount("Interest Payable");

        final PostSavingsProductsRequest request = SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.ANNUAL, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .nominalAnnualInterestRate(INTEREST_RATE)//
                .allowOverdraft(true)//
                .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC);

        final PostSavingsProductsRequest product = SavingsRequestBuilders
                .withAccrualAccountingMappings(request, assetAccount, liabilityAccount, incomeAccount, expenseAccount,
                        interestReceivableAccount)
                .savingsControlAccountId(SavingsRequestBuilders.accountId(savingsControlAccount))//
                .interestPayableAccountId(SavingsRequestBuilders.accountId(interestPayableAccount))//
                .overdraftLimit(OVERDRAFT_LIMIT)//
                .nominalAnnualInterestRateOverdraft(BigDecimal.valueOf(OVERDRAFT_INTEREST_RATE));

        final Long productId = savingsProductHelper.createSavingsProduct(product).getResourceId();
        assertNotNull(productId, "Error creating savings product.");
        return productId;
    }

    private Long createTrackedSavingsAccount(final Long clientId, final Long productId, final String submittedOnDate) {
        final Long accountId = submitSavingsApplication(clientId, productId, submittedOnDate).getSavingsId();
        assertNotNull(accountId, "Error applying for savings account.");
        createdSavingsAccountIds.add(accountId);
        return accountId;
    }

    /** Closing these keeps later runs of the interest posting job from having to walk them all. */
    private void closeTrackedSavingsAccounts() {
        if (createdSavingsAccountIds.isEmpty()) {
            return;
        }
        final String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        createdSavingsAccountIds.forEach(savingsId -> {
            try {
                savingsHelper.postInterest(savingsId);
                closeSavings(savingsId, today, true);
            } catch (RuntimeException e) {
                LOG.warn("Unable to close savings account {}: {}", savingsId, e.getMessage());
            }
        });
        createdSavingsAccountIds.clear();
    }
}
