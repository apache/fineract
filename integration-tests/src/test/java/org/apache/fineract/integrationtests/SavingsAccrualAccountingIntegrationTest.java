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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class SavingsAccrualAccountingIntegrationTest extends FeignSavingsTestBase {

    private static final String ACCRUAL_JOB = "Add Accrual Transactions For Savings";
    private static final String SAVINGS_TRANSACTION_ID_PREFIX = "S";
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";

    private static final String BUSINESS_DATE = "2021-08-12";
    private static final LocalDate TODAY = LocalDate.of(2021, 8, 12);
    private static final int DAYS_TO_SUBTRACT = 10;
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2020";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private static final String AMOUNT = "10000";
    private static final Double INTEREST_RATE = 10.0;
    private static final Double OVERDRAFT_INTEREST_RATE = 21.0;
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000");

    private static final BigDecimal PERCENT = new BigDecimal("100");
    private static final int DIGITS_AFTER_DECIMAL = 4;

    @Test
    public void testPositiveAccrualPostsCorrectJournalEntries() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final AccrualAccounts accounts = createDistinctAccrualAccounts();

            final PostSavingsProductsRequest product = withMappings(accrualProduct(INTEREST_RATE), accounts);

            final Long savingsProductId = savingsProductHelper.createSavingsProduct(product).getResourceId();
            assertNotNull(savingsProductId, "Failed to create savings product.");

            final Long savingsAccountId = createActiveSavingsAccount(savingsProductId);
            deposit(savingsAccountId, AMOUNT, startDateString());

            schedulerHelper.executeAndAwaitJob(ACCRUAL_JOB);

            final List<SavingsAccountTransactionData> accrualTransactions = savingsTransactionHelper
                    .getAccrualTransactions(savingsAccountId);
            assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found.");

            final List<JournalEntryTransactionItem> journalEntries = journalEntriesOf(accrualTransactions.get(0));
            assertFalse(journalEntries.isEmpty(), "No journal entries found for positive accrual.");
            assertHasEntry(journalEntries, DEBIT, accounts.interestOnSavings(),
                    "DEBIT to Interest on Savings (Expense) Account not found for positive accrual.");
            assertHasEntry(journalEntries, CREDIT, accounts.interestPayable(),
                    "CREDIT to Interest Payable (Liability) Account not found for positive accrual.");

            verifyEveryAccrualIsOneDayOfInterest(accrualTransactions, INTEREST_RATE);
        });
    }

    @Test
    public void testNegativeAccrualPostsCorrectJournalEntries() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final AccrualAccounts accounts = createDistinctAccrualAccounts();

            final PostSavingsProductsRequest product = withMappings(accrualProduct(OVERDRAFT_INTEREST_RATE).allowOverdraft(true), accounts)
                    .overdraftLimit(OVERDRAFT_LIMIT)//
                    .nominalAnnualInterestRateOverdraft(BigDecimal.valueOf(OVERDRAFT_INTEREST_RATE));

            final Long savingsProductId = savingsProductHelper.createSavingsProduct(product).getResourceId();
            assertNotNull(savingsProductId, "Savings product with overdraft creation failed.");

            final Long savingsAccountId = createActiveSavingsAccount(savingsProductId);
            withdraw(savingsAccountId, AMOUNT, startDateString());

            schedulerHelper.executeAndAwaitJob(ACCRUAL_JOB);

            final List<SavingsAccountTransactionData> accrualTransactions = savingsTransactionHelper
                    .getAccrualTransactions(savingsAccountId);
            assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found for overdraft.");

            final List<JournalEntryTransactionItem> journalEntries = journalEntriesOf(accrualTransactions.get(0));
            assertFalse(journalEntries.isEmpty(), "No journal entries found for negative accrual.");
            assertHasEntry(journalEntries, DEBIT, accounts.interestReceivable(),
                    "DEBIT to Interest Receivable (Asset) Account not found for negative accrual.");
            assertHasEntry(journalEntries, CREDIT, accounts.incomeFromInterest(),
                    "CREDIT to Overdraft Interest Income Account not found for negative accrual.");

            verifyEveryAccrualIsOneDayOfInterest(accrualTransactions, OVERDRAFT_INTEREST_RATE);
        });
    }

    private void verifyEveryAccrualIsOneDayOfInterest(final List<SavingsAccountTransactionData> accrualTransactions,
            final Double interestRate) {
        final BigDecimal expectedDailyInterest = dailyInterest(interestRate);
        for (SavingsAccountTransactionData accrual : accrualTransactions) {
            SavingsTestValidators.verifyAmount(expectedDailyInterest, accrual.getAmount(), "Verifying the accrual of " + accrual.getDate());
        }
    }

    /** Ordered the way the server does it: the annual rate becomes a daily one before it is applied. */
    private BigDecimal dailyInterest(final Double interestRate) {
        final BigDecimal interestRateAsFraction = BigDecimal.valueOf(interestRate).divide(PERCENT);
        final BigDecimal multiplicand = BigDecimal.ONE
                .divide(BigDecimal.valueOf(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365), MathContext.DECIMAL64);
        final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        return new BigDecimal(AMOUNT).multiply(dailyInterestRate, MathContext.DECIMAL64).setScale(DIGITS_AFTER_DECIMAL,
                RoundingMode.HALF_EVEN);
    }

    private List<JournalEntryTransactionItem> journalEntriesOf(final SavingsAccountTransactionData transaction) {
        final List<JournalEntryTransactionItem> pageItems = journalEntryHelper
                .getJournalEntriesByTransactionId(SAVINGS_TRANSACTION_ID_PREFIX + transaction.getId()).getPageItems();
        return pageItems == null ? List.of() : pageItems;
    }

    private void assertHasEntry(final List<JournalEntryTransactionItem> journalEntries, final String entryType, final Account account,
            final String message) {
        final Long expectedAccountId = SavingsRequestBuilders.accountId(account);
        assertTrue(journalEntries.stream().anyMatch(entry -> entry.getEntryType() != null
                && entryType.equals(entry.getEntryType().getValue()) && expectedAccountId.equals(entry.getGlAccountId())), message);
    }

    /**
     * Every mapping gets its own account so that a journal entry posted against the wrong one cannot satisfy an
     * assertion aimed at another.
     */
    private AccrualAccounts createDistinctAccrualAccounts() {
        return new AccrualAccounts(accountHelper.createAssetAccount("Savings Reference"),
                accountHelper.createAssetAccount("Overdraft Portfolio Control"), accountHelper.createAssetAccount("Fees Receivable"),
                accountHelper.createAssetAccount("Penalties Receivable"), accountHelper.createAssetAccount("Interest Receivable (Asset)"),
                accountHelper.createLiabilityAccount("Savings Control"), accountHelper.createLiabilityAccount("Transfers In Suspense"),
                accountHelper.createLiabilityAccount("Interest Payable (Liability)"), accountHelper.createIncomeAccount("Income from Fees"),
                accountHelper.createIncomeAccount("Income from Penalties"), accountHelper.createIncomeAccount("Overdraft Interest Income"),
                accountHelper.createExpenseAccount("Interest on Savings (Expense)"), accountHelper.createExpenseAccount("Write Off"));
    }

    private PostSavingsProductsRequest withMappings(final PostSavingsProductsRequest request, final AccrualAccounts accounts) {
        return request//
                .savingsReferenceAccountId(SavingsRequestBuilders.accountId(accounts.savingsReference()))//
                .overdraftPortfolioControlId(SavingsRequestBuilders.accountId(accounts.overdraftPortfolioControl()))//
                .feesReceivableAccountId(SavingsRequestBuilders.accountId(accounts.feesReceivable()))//
                .penaltiesReceivableAccountId(SavingsRequestBuilders.accountId(accounts.penaltiesReceivable()))//
                .interestReceivableAccountId(SavingsRequestBuilders.accountId(accounts.interestReceivable()))//
                .savingsControlAccountId(SavingsRequestBuilders.accountId(accounts.savingsControl()))//
                .transfersInSuspenseAccountId(SavingsRequestBuilders.accountId(accounts.transfersInSuspense()))//
                .interestPayableAccountId(SavingsRequestBuilders.accountId(accounts.interestPayable()))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(accounts.incomeFromFee()))//
                .incomeFromPenaltyAccountId(SavingsRequestBuilders.accountId(accounts.incomeFromPenalty()))//
                .incomeFromInterestId(SavingsRequestBuilders.accountId(accounts.incomeFromInterest()))//
                .interestOnSavingsAccountId(SavingsRequestBuilders.accountId(accounts.interestOnSavings()))//
                .writeOffAccountId(SavingsRequestBuilders.accountId(accounts.writeOff()));
    }

    private record AccrualAccounts(Account savingsReference, Account overdraftPortfolioControl, Account feesReceivable,
            Account penaltiesReceivable, Account interestReceivable, Account savingsControl, Account transfersInSuspense,
            Account interestPayable, Account incomeFromFee, Account incomeFromPenalty, Account incomeFromInterest,
            Account interestOnSavings, Account writeOff) {
    }

    private Long createActiveSavingsAccount(final Long savingsProductId) {
        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        assertNotNull(clientId);

        final String startDateString = startDateString();
        final Long savingsAccountId = submitSavingsApplication(clientId, savingsProductId, startDateString).getSavingsId();
        assertNotNull(savingsAccountId);

        approveSavings(savingsAccountId, startDateString);
        activateSavings(savingsAccountId, startDateString);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsAccountId));
        return savingsAccountId;
    }

    private String startDateString() {
        return DATE_FORMATTER.format(TODAY.minusDays(DAYS_TO_SUBTRACT));
    }

    private PostSavingsProductsRequest accrualProduct(final Double nominalAnnualInterestRate) {
        return SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.MONTHLY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .nominalAnnualInterestRate(nominalAnnualInterestRate)//
                .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC);
    }
}
