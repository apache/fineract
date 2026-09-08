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
package org.apache.fineract.integrationtests.savings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.common.AccountingConstants;
import org.apache.fineract.client.models.AccountTransferRequest;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.client.feign.modules.AccountTransferRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.Account.AccountType;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AccountTransferOverdraftTest extends FeignSavingsTestBase {

    private static final String ACTIVATION_DATE = "01 January 2013";
    private static final String BACKDATED_DEPOSIT_DATE = "27 February 2013";
    private static final String TRANSFER_DATE = "28 February 2013";
    private static final BigDecimal OPENING_BALANCE = BigDecimal.valueOf(100);
    private static final BigDecimal TRANSFER_AMOUNT = BigDecimal.valueOf(110);
    private static final BigDecimal BACKDATED_DEPOSIT_AMOUNT = BigDecimal.valueOf(5);
    private static final BigDecimal ORIGINAL_OVERDRAFT_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal RECALCULATED_OVERDRAFT_AMOUNT = BigDecimal.valueOf(5);
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("1000.0");
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";

    private static FeignFinancialActivityAccountHelper financialActivityAccountHelper;

    @BeforeAll
    public static void setupFinancialActivityAccountHelper() {
        financialActivityAccountHelper = new FeignFinancialActivityAccountHelper(FineractFeignClientHelper.getFineractFeignClient());
    }

    @Test
    void preserveTransferLinkWhenBackdatedDepositRecalculatesOverdraft() {
        runAt("01 March 2013", () -> runWithPostReversalTransactions(() -> {
            final var accounting = createAccountingFixture();
            try {
                final var savingsProductId = createOverdraftSavingsProduct(accounting);
                final var sourceClientId = createClient();
                final var destinationClientId = createClient();
                final var sourceSavingsId = createApproveActivateSavings(sourceClientId, savingsProductId, ACTIVATION_DATE);
                final var destinationSavingsId = createApproveActivateSavings(destinationClientId, savingsProductId, ACTIVATION_DATE);

                deposit(sourceSavingsId, OPENING_BALANCE.toPlainString(), ACTIVATION_DATE);
                final var accountTransferId = transfer(sourceClientId, sourceSavingsId, destinationClientId, destinationSavingsId);

                final var originalWithdrawal = findActiveWithdrawal(sourceSavingsId);
                assertNotNull(originalWithdrawal.getTransfer());
                final var transferId = originalWithdrawal.getTransfer().getId();

                final var backdatedDepositId = deposit(sourceSavingsId, BACKDATED_DEPOSIT_AMOUNT.toPlainString(), BACKDATED_DEPOSIT_DATE)
                        .getResourceId();

                assertRecalculationHistory(sourceSavingsId);
                final var recalculatedWithdrawal = findActiveWithdrawal(sourceSavingsId);
                assertNotNull(recalculatedWithdrawal.getTransfer());
                assertEquals(transferId, recalculatedWithdrawal.getTransfer().getId());
                final var auditOnlyReversal = findAuditOnlyReversal(sourceSavingsId, originalWithdrawal.getId());

                assertReversedWithdrawalJournalEntries(originalWithdrawal.getId(), accounting);
                assertReplacementWithdrawalJournalEntries(recalculatedWithdrawal.getId(), accounting);
                assertBackdatedDepositJournalEntries(backdatedDepositId, accounting);
                assertNoJournalEntries(auditOnlyReversal.getId());

                accountTransferHelper.undoTransfer(accountTransferId);
                assertBalance(sourceSavingsId, BigDecimal.valueOf(105));
                assertBalance(destinationSavingsId, BigDecimal.ZERO);
            } finally {
                accounting.deleteCreatedFinancialActivityMapping();
            }
        }));
    }

    private Long createOverdraftSavingsProduct(final AccountingFixture accounting) {
        final PostSavingsProductsRequest request = SavingsRequestBuilders.withCashBasedAccounting(
                SavingsRequestBuilders.defaultSavingsProduct()//
                        .minRequiredOpeningBalance(BigDecimal.ZERO)//
                        .allowOverdraft(true)//
                        .overdraftLimit(OVERDRAFT_LIMIT),
                accounting.savingsReferenceAccount(), accounting.savingsControlAccount(), accounting.incomeAccount(),
                accounting.expenseAccount());
        return savingsProductHelper.createSavingsProduct(request).getResourceId();
    }

    private Long transfer(final Long sourceClientId, final Long sourceSavingsId, final Long destinationClientId,
            final Long destinationSavingsId) {
        final AccountTransferRequest request = AccountTransferRequestBuilders.transfer(TRANSFER_DATE, sourceClientId, sourceSavingsId,
                PortfolioAccountType.SAVINGS, destinationClientId, destinationSavingsId, PortfolioAccountType.SAVINGS,
                TRANSFER_AMOUNT.toPlainString());
        return accountTransferHelper.createAccountTransfer(request).getResourceId();
    }

    private void assertBalance(final Long savingsId, final BigDecimal expectedBalance) {
        assertEquals(0, expectedBalance.compareTo(savingsHelper.getSavingsSummary(savingsId).getAccountBalance()));
    }

    private SavingsAccountTransactionData findActiveWithdrawal(final Long savingsId) {
        return transferWithdrawals(savingsId).stream().filter(transaction -> !Boolean.TRUE.equals(transaction.getReversed())).findFirst()
                .orElseThrow();
    }

    private SavingsAccountTransactionData findAuditOnlyReversal(final Long savingsId, final Long originalTransactionId) {
        final var auditOnlyReversals = savingsTransactionHelper.getTransactions(savingsId).stream()
                .filter(transaction -> Boolean.TRUE.equals(transaction.getIsReversal()))
                .filter(transaction -> originalTransactionId.equals(transaction.getOriginalTransactionId())).toList();
        assertEquals(1, auditOnlyReversals.size());
        return auditOnlyReversals.get(0);
    }

    private void assertRecalculationHistory(final Long savingsId) {
        final var withdrawals = transferWithdrawals(savingsId);
        assertEquals(2, withdrawals.size());
        assertEquals(1, withdrawals.stream().filter(transaction -> Boolean.TRUE.equals(transaction.getReversed())).count());
        assertEquals(1, withdrawals.stream().filter(transaction -> !Boolean.TRUE.equals(transaction.getReversed())).count());
    }

    private List<SavingsAccountTransactionData> transferWithdrawals(final Long savingsId) {
        return savingsTransactionHelper.getTransactions(savingsId).stream()
                .filter(transaction -> !Boolean.TRUE.equals(transaction.getIsReversal()))
                .filter(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getWithdrawal()))
                .filter(transaction -> TRANSFER_AMOUNT.compareTo(transaction.getAmount()) == 0)
                .filter(transaction -> LocalDate.of(2013, 2, 28).equals(transaction.getDate())).toList();
    }

    private AccountingFixture createAccountingFixture() {
        final var savingsReferenceAccount = accountHelper.createAssetAccount();
        final var savingsControlAccount = accountHelper.createLiabilityAccount();
        final var incomeAccount = accountHelper.createIncomeAccount();
        final var expenseAccount = accountHelper.createExpenseAccount();
        final var financialActivityId = AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue();
        final var existingMapping = financialActivityAccountHelper.getAllMappings().stream()
                .filter(mapping -> mapping.getFinancialActivityData() != null
                        && financialActivityId.equals(mapping.getFinancialActivityData().getId()))
                .findFirst();

        if (existingMapping.isPresent()) {
            final GetFinancialActivityAccountsResponse mapping = existingMapping.orElseThrow();
            return new AccountingFixture(savingsReferenceAccount, savingsControlAccount, incomeAccount, expenseAccount,
                    new Account(Math.toIntExact(mapping.getGlAccountData().getId()), AccountType.LIABILITY), null);
        }

        final var liabilityTransferAccount = accountHelper.createLiabilityAccount();
        final var createdMapping = financialActivityAccountHelper.createMapping(financialActivityId, liabilityTransferAccount);
        assertNotNull(createdMapping.getResourceId());
        return new AccountingFixture(savingsReferenceAccount, savingsControlAccount, incomeAccount, expenseAccount,
                liabilityTransferAccount, createdMapping.getResourceId());
    }

    private void runWithPostReversalTransactions(final Runnable action) {
        final var configurationName = GlobalConfigurationConstants.ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS;
        final var originallyEnabled = Boolean.TRUE
                .equals(globalConfigurationHelper.getGlobalConfigurationByName(configurationName).getEnabled());
        try {
            globalConfigurationHelper.updateGlobalConfiguration(configurationName, new PutGlobalConfigurationsRequest().enabled(true));
            action.run();
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(configurationName,
                    new PutGlobalConfigurationsRequest().enabled(originallyEnabled));
        }
    }

    private void assertReversedWithdrawalJournalEntries(final Long transactionId, final AccountingFixture accounting) {
        assertJournalEntries(transactionId, new ExpectedJournalEntry(accounting.savingsControlAccount(), DEBIT, OPENING_BALANCE),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), CREDIT, OPENING_BALANCE),
                new ExpectedJournalEntry(accounting.savingsReferenceAccount(), DEBIT, ORIGINAL_OVERDRAFT_AMOUNT),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), CREDIT, ORIGINAL_OVERDRAFT_AMOUNT),
                new ExpectedJournalEntry(accounting.savingsControlAccount(), CREDIT, OPENING_BALANCE),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), DEBIT, OPENING_BALANCE),
                new ExpectedJournalEntry(accounting.savingsReferenceAccount(), CREDIT, ORIGINAL_OVERDRAFT_AMOUNT),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), DEBIT, ORIGINAL_OVERDRAFT_AMOUNT));
    }

    private void assertReplacementWithdrawalJournalEntries(final Long transactionId, final AccountingFixture accounting) {
        assertJournalEntries(transactionId,
                new ExpectedJournalEntry(accounting.savingsControlAccount(), DEBIT,
                        TRANSFER_AMOUNT.subtract(RECALCULATED_OVERDRAFT_AMOUNT)),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), CREDIT,
                        TRANSFER_AMOUNT.subtract(RECALCULATED_OVERDRAFT_AMOUNT)),
                new ExpectedJournalEntry(accounting.savingsReferenceAccount(), DEBIT, RECALCULATED_OVERDRAFT_AMOUNT),
                new ExpectedJournalEntry(accounting.liabilityTransferAccount(), CREDIT, RECALCULATED_OVERDRAFT_AMOUNT));
    }

    private void assertBackdatedDepositJournalEntries(final Long transactionId, final AccountingFixture accounting) {
        assertJournalEntries(transactionId, new ExpectedJournalEntry(accounting.savingsReferenceAccount(), DEBIT, BACKDATED_DEPOSIT_AMOUNT),
                new ExpectedJournalEntry(accounting.savingsControlAccount(), CREDIT, BACKDATED_DEPOSIT_AMOUNT));
    }

    private void assertNoJournalEntries(final Long transactionId) {
        final var journalEntries = journalEntries(transactionId);
        assertTrue(journalEntries.isEmpty(), () -> "Audit-only reversal must not create journal entries: " + journalEntries);
    }

    private void assertJournalEntries(final Long transactionId, final ExpectedJournalEntry... expectedEntries) {
        final var remainingEntries = new ArrayList<>(journalEntries(transactionId));
        assertEquals(expectedEntries.length, remainingEntries.size(),
                () -> "Unexpected journal entry count for savings transaction " + transactionId + ": " + remainingEntries);

        for (final var expected : expectedEntries) {
            final var matchingEntry = remainingEntries.stream().filter(expected::matches).findFirst();
            assertTrue(matchingEntry.isPresent(),
                    () -> "Missing journal entry " + expected + " for savings transaction " + transactionId + ": " + remainingEntries);
            remainingEntries.remove(matchingEntry.orElseThrow());
        }
        assertFalse(remainingEntries.iterator().hasNext());
    }

    private List<JournalEntryTransactionItem> journalEntries(final Long transactionId) {
        final var journalEntries = journalEntryHelper.getJournalEntries("S" + transactionId);
        assertNotNull(journalEntries);
        assertNotNull(journalEntries.getPageItems());
        return journalEntries.getPageItems();
    }

    private record ExpectedJournalEntry(Account account, String entryType, BigDecimal amount) {

        private boolean matches(final JournalEntryTransactionItem actual) {
            return account.getAccountID().longValue() == actual.getGlAccountId() && entryType.equals(actual.getEntryType().getValue())
                    && amount.compareTo(BigDecimal.valueOf(actual.getAmount())) == 0;
        }
    }

    private record AccountingFixture(Account savingsReferenceAccount, Account savingsControlAccount, Account incomeAccount,
            Account expenseAccount, Account liabilityTransferAccount, Long createdFinancialActivityMappingId) {

        private void deleteCreatedFinancialActivityMapping() {
            if (createdFinancialActivityMappingId != null) {
                assertEquals(createdFinancialActivityMappingId,
                        financialActivityAccountHelper.deleteMapping(createdFinancialActivityMappingId).getResourceId());
            }
        }
    }
}
