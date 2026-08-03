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
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.FinancialActivityAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.savings.base.BaseSavingsIntegrationTest;
import org.junit.jupiter.api.Test;

class AccountTransferOverdraftTest extends BaseSavingsIntegrationTest {

    private static final String ACTIVATION_DATE = "01 January 2013";
    private static final String BACKDATED_DEPOSIT_DATE = "27 February 2013";
    private static final String TRANSFER_DATE = "28 February 2013";
    private static final BigDecimal OPENING_BALANCE = BigDecimal.valueOf(100);
    private static final BigDecimal TRANSFER_AMOUNT = BigDecimal.valueOf(110);
    private static final BigDecimal BACKDATED_DEPOSIT_AMOUNT = BigDecimal.valueOf(5);
    private static final BigDecimal ORIGINAL_OVERDRAFT_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal RECALCULATED_OVERDRAFT_AMOUNT = BigDecimal.valueOf(5);
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";

    @Test
    void preserveTransferLinkWhenBackdatedDepositRecalculatesOverdraft() {
        runAt("01 March 2013", () -> runWithPostReversalTransactions(() -> {
            final var accounting = createAccountingFixture();
            try {
                final var savingsProductId = createOverdraftSavingsProduct(accounting);
                final var sourceClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
                final var destinationClientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
                final var sourceSavingsId = createActiveSavingsAccount(sourceClientId, savingsProductId);
                final var destinationSavingsId = createActiveSavingsAccount(destinationClientId, savingsProductId);

                deposit(sourceSavingsId, ACTIVATION_DATE, OPENING_BALANCE);
                final var accountTransferId = transfer(sourceClientId, sourceSavingsId, destinationClientId, destinationSavingsId);

                final var originalWithdrawal = findActiveWithdrawal(sourceSavingsId);
                assertNotNull(originalWithdrawal.getTransfer());
                final var transferId = originalWithdrawal.getTransfer().getId();

                final var backdatedDepositId = deposit(sourceSavingsId, BACKDATED_DEPOSIT_DATE, BACKDATED_DEPOSIT_AMOUNT).getResourceId();

                assertRecalculationHistory(sourceSavingsId);
                final var recalculatedWithdrawal = findActiveWithdrawal(sourceSavingsId);
                assertNotNull(recalculatedWithdrawal.getTransfer());
                assertEquals(transferId, recalculatedWithdrawal.getTransfer().getId());
                final var auditOnlyReversal = findAuditOnlyReversal(sourceSavingsId, originalWithdrawal.getId());

                assertReversedWithdrawalJournalEntries(originalWithdrawal.getId(), accounting);
                assertReplacementWithdrawalJournalEntries(recalculatedWithdrawal.getId(), accounting);
                assertBackdatedDepositJournalEntries(backdatedDepositId, accounting);
                assertNoJournalEntries(auditOnlyReversal.getId());

                ok(fineractClient().accountTransfers.accountTransferOperation(accountTransferId, "undo"));
                assertBalance(sourceSavingsId, BigDecimal.valueOf(105));
                assertBalance(destinationSavingsId, BigDecimal.ZERO);
            } finally {
                accounting.deleteCreatedFinancialActivityMapping();
            }
        }));
    }

    private Long createOverdraftSavingsProduct(final AccountingFixture accounting) {
        final var product = new SavingsProductHelper().withCurrencyCode("USD").withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance().withMinimumOpenningBalance("0")
                .withOverDraft("1000.0").withAccountingRuleAsCashBased(new Account[] { accounting.savingsReferenceAccount(),
                        accounting.incomeAccount(), accounting.expenseAccount(), accounting.savingsControlAccount() })
                .build();
        return SavingsProductHelper.createSavingsProduct(product, requestSpec, responseSpec).longValue();
    }

    private Long createActiveSavingsAccount(final Long clientId, final Long productId) {
        final var savingsId = applySavingsAccount(applySavingsRequest(clientId, productId, ACTIVATION_DATE)).getSavingsId();
        approveSavingsAccount(savingsId, ACTIVATION_DATE);
        activateSavingsAccount(savingsId, ACTIVATION_DATE);
        return savingsId;
    }

    private Long transfer(final Long sourceClientId, final Long sourceSavingsId, final Long destinationClientId,
            final Long destinationSavingsId) {
        return ok(fineractClient().accountTransfers.createAccountTransfer(new AccountTransferRequest()
                .fromClientId(sourceClientId.toString()).fromAccountId(sourceSavingsId.toString()).fromAccountType("2").fromOfficeId("1")
                .toClientId(destinationClientId.toString()).toAccountId(destinationSavingsId.toString()).toAccountType("2").toOfficeId("1")
                .transferDate(TRANSFER_DATE).transferAmount(TRANSFER_AMOUNT.toPlainString()).transferDescription("Transfer")
                .dateFormat(DATETIME_PATTERN).locale("en_GB"))).getResourceId();
    }

    private void assertBalance(final Long savingsId, final BigDecimal expectedBalance) {
        final var account = ok(fineractClient().savingsAccounts.retrieveSavingsAccount(savingsId, false, null, "summary"));
        assertEquals(0, expectedBalance.compareTo(account.getSummary().getAccountBalance()));
    }

    private SavingsAccountTransactionData findActiveWithdrawal(final Long savingsId) {
        return transferWithdrawals(savingsId).stream().filter(transaction -> !Boolean.TRUE.equals(transaction.getReversed())).findFirst()
                .orElseThrow();
    }

    private SavingsAccountTransactionData findAuditOnlyReversal(final Long savingsId, final Long originalTransactionId) {
        final var auditOnlyReversals = getTransactions(savingsId).stream()
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
        return getTransactions(savingsId).stream().filter(transaction -> !Boolean.TRUE.equals(transaction.getIsReversal()))
                .filter(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getWithdrawal()))
                .filter(transaction -> TRANSFER_AMOUNT.compareTo(transaction.getAmount()) == 0)
                .filter(transaction -> LocalDate.of(2013, 2, 28).equals(transaction.getDate())).toList();
    }

    private AccountingFixture createAccountingFixture() {
        final var accountHelper = new AccountHelper(requestSpec, responseSpec);
        final var savingsReferenceAccount = accountHelper.createAssetAccount();
        final var savingsControlAccount = accountHelper.createLiabilityAccount();
        final var incomeAccount = accountHelper.createIncomeAccount();
        final var expenseAccount = accountHelper.createExpenseAccount();
        final var financialActivityAccountHelper = new FinancialActivityAccountHelper(requestSpec);
        final var financialActivityId = AccountingConstants.FinancialActivity.LIABILITY_TRANSFER.getValue();
        final var existingMapping = financialActivityAccountHelper.getAllFinancialActivityAccounts().stream()
                .filter(mapping -> mapping.getFinancialActivityData() != null
                        && financialActivityId.equals(mapping.getFinancialActivityData().getId()))
                .findFirst();

        if (existingMapping.isPresent()) {
            final var glAccountId = existingMapping.orElseThrow().getGlAccountData().getId();
            return new AccountingFixture(savingsReferenceAccount, savingsControlAccount, incomeAccount, expenseAccount,
                    new Account(Math.toIntExact(glAccountId), Account.AccountType.LIABILITY), financialActivityAccountHelper, null);
        }

        final var liabilityTransferAccount = accountHelper.createLiabilityAccount();
        final var mapping = financialActivityAccountHelper.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId(financialActivityId.longValue()).glAccountId(liabilityTransferAccount.getAccountID().longValue()));
        assertNotNull(mapping.getResourceId());
        return new AccountingFixture(savingsReferenceAccount, savingsControlAccount, incomeAccount, expenseAccount,
                liabilityTransferAccount, financialActivityAccountHelper, mapping.getResourceId());
    }

    private void runWithPostReversalTransactions(final Runnable action) {
        final var configurationName = GlobalConfigurationConstants.ENABLE_POST_REVERSAL_TXNS_FOR_REVERSE_TRANSACTIONS;
        final var configuration = globalConfigurationHelper.getGlobalConfigurationByName(configurationName);
        final var originallyEnabled = Boolean.TRUE.equals(configuration.getEnabled());
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
            Account expenseAccount, Account liabilityTransferAccount, FinancialActivityAccountHelper financialActivityAccountHelper,
            Long createdFinancialActivityMappingId) {

        private void deleteCreatedFinancialActivityMapping() {
            if (createdFinancialActivityMappingId != null) {
                final var response = financialActivityAccountHelper.deleteFinancialActivityAccount(createdFinancialActivityMappingId);
                assertEquals(createdFinancialActivityMappingId, response.getResourceId());
            }
        }
    }
}
