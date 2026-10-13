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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountSubStatusEnumData;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Validates the end-to-end execution of the Savings Close of Business (SAVINGS_COB) job triggered inline, mirroring the
 * way Loan COB is triggered through {@code POST /jobs/{jobName}/inline}.
 */
public class SavingsInlineCOBIntegrationTest extends FeignSavingsTestBase {

    // Default of fineract.api.body-item-size-limit.inline-loan-cob, which the savings inline COB shares with the loan
    // one.
    private static final int INLINE_COB_MAX_SAVINGS_IDS = 1000;
    private static final String SAVINGS_COB_JOB_NAME = "SAVINGS_COB";
    private static final String SAVINGS_JOURNAL_TRANSACTION_PREFIX = "S";
    private static final LocalDate ACTIVATION_DATE = LocalDate.of(2024, 1, 1);
    private static final String ACTIVATION_DATE_STRING = "01 January 2024";
    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("1000");
    // Monthly posting: the January period ends on 2024-01-31, so closing 2024-02-01 (business date 2024-02-02) is the
    // first COB day on which that period is due for posting.
    private static final LocalDate FIRST_PERIOD_END_DATE = LocalDate.of(2024, 1, 31);
    private static final LocalDate COB_DATE_AFTER_FIRST_PERIOD = LocalDate.of(2024, 2, 1);

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void inlineSavingsCOBMovesAccountToInactiveSubStatus() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));

        final LocalDate activationDate = LocalDate.of(2024, 1, 1);
        // Business date on the day the account is funded; COB date 45 days later so the account is well past the
        // product's 30-day inactivity threshold and should be flagged inactive by
        // UpdateSavingsDormantStatusBusinessStep.
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, activationDate);
        updateBusinessDate(BusinessDateType.COB_DATE, activationDate.minusDays(1));

        final Long clientId = createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createDormancyTrackingSavingsProduct();
        Assertions.assertNotNull(savingsProductId);

        final Long savingsId = submitSavingsApplication(clientId, savingsProductId, "01 January 2024").getSavingsId();
        Assertions.assertNotNull(savingsId);

        approveSavings(savingsId, "01 January 2024");
        activateSavings(savingsId, "01 January 2024");
        deposit(savingsId, "5000", "01 January 2024");

        SavingsAccountSubStatusEnumData subStatus = savingsHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getNone(), "Savings sub-status should be NONE right after activation");

        // Advance the COB date past the inactivity threshold and run the Savings COB inline for this account only.
        final LocalDate cobDate = activationDate.plusDays(45);
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        updateBusinessDate(BusinessDateType.COB_DATE, cobDate);

        executeInlineSavingsCOB(List.of(savingsId));

        subStatus = savingsHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getInactive(), "Savings sub-status should be INACTIVE after inline Savings COB");
    }

    @Test
    public void inlineSavingsCOBPostsInterestForElapsedPostingPeriod() {
        enableBusinessDateOnActivation();
        final Long savingsId = createFundedInterestBearingSavingsAccount();

        final BigDecimal balanceBeforeCOB = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();
        assertThat(balanceBeforeCOB).isEqualByComparingTo(DEPOSIT_AMOUNT);
        assertThat(activeInterestPostings(savingsId)).as("No interest must be posted before the COB runs").isEmpty();

        moveCOBDateTo(COB_DATE_AFTER_FIRST_PERIOD);
        executeInlineSavingsCOB(List.of(savingsId));

        final List<SavingsAccountTransactionData> interestPostings = activeInterestPostings(savingsId);
        assertThat(interestPostings).as("Inline Savings COB must post interest exactly once for the elapsed January period").hasSize(1);
        final SavingsAccountTransactionData interestPosting = interestPostings.getFirst();
        final BigDecimal interestAmount = interestPosting.getAmount();
        assertThat(interestAmount).as("Interest posted on a positive balance with a positive rate").isNotNull()
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(interestPosting.getDate()).as("Interest posting must be dated within the closed period, not after the COB date")
                .isIn(FIRST_PERIOD_END_DATE, COB_DATE_AFTER_FIRST_PERIOD);

        final BigDecimal balanceAfterCOB = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();
        assertThat(balanceAfterCOB).as("Balance must grow by exactly the posted interest")
                .isEqualByComparingTo(balanceBeforeCOB.add(interestAmount));

        assertJournalEntriesBalanced(interestPosting.getId(), interestAmount);
    }

    @Test
    public void inlineSavingsCOBRunTwiceForSameDateDoesNotDuplicateInterestPosting() {
        enableBusinessDateOnActivation();
        final Long savingsId = createFundedInterestBearingSavingsAccount();

        moveCOBDateTo(COB_DATE_AFTER_FIRST_PERIOD);
        executeInlineSavingsCOB(List.of(savingsId));

        final List<SavingsAccountTransactionData> postingsAfterFirstRun = activeInterestPostings(savingsId);
        assertThat(postingsAfterFirstRun).hasSize(1);
        final BigDecimal balanceAfterFirstRun = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

        // Same COB date again: the account is already closed for it, so nothing must be posted a second time.
        executeInlineSavingsCOB(List.of(savingsId));

        final List<SavingsAccountTransactionData> postingsAfterSecondRun = activeInterestPostings(savingsId);
        assertThat(postingsAfterSecondRun).as("Re-running inline Savings COB for the same date must not post interest again").hasSize(1);
        assertThat(postingsAfterSecondRun.getFirst().getId()).as("The original interest posting must be kept, not replaced")
                .isEqualTo(postingsAfterFirstRun.getFirst().getId());
        assertThat(savingsHelper.getSavingsSummary(savingsId).getAccountBalance())
                .as("Balance must not change when inline Savings COB runs twice for the same date")
                .isEqualByComparingTo(balanceAfterFirstRun);
    }

    @Test
    public void inlineSavingsCOBRejectsRequestOverItemSizeLimit() {
        final List<Long> savingsIds = LongStream.rangeClosed(1, INLINE_COB_MAX_SAVINGS_IDS + 1).boxed().toList();

        final CallFailedRuntimeException exception = fail(
                () -> fineractClient().inlineJob().executeInlineJob(SAVINGS_COB_JOB_NAME, new InlineJobRequest().savingsIds(savingsIds)));

        Assertions.assertEquals(400, exception.getStatus());
        assertThat(exception.getResponseBody()).contains("Size of the savings IDs list cannot be over " + INLINE_COB_MAX_SAVINGS_IDS);
    }

    private void enableBusinessDateOnActivation() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, ACTIVATION_DATE);
        updateBusinessDate(BusinessDateType.COB_DATE, ACTIVATION_DATE.minusDays(1));
    }

    private void updateBusinessDate(final BusinessDateType type, final LocalDate date) {
        businessDateHelper.updateBusinessDate(type.name(), date.toString());
    }

    private void moveCOBDateTo(final LocalDate cobDate) {
        updateBusinessDate(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        updateBusinessDate(BusinessDateType.COB_DATE, cobDate);
    }

    /**
     * Creates an active account on {@link #ACTIVATION_DATE} under a cash-based accounting product with the default 10%
     * nominal rate, daily compounding and monthly posting, funded with {@link #DEPOSIT_AMOUNT} on activation.
     */
    private Long createFundedInterestBearingSavingsAccount() {
        final Long clientId = createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createCashBasedInterestBearingSavingsProduct();
        Assertions.assertNotNull(savingsProductId);

        final Long savingsId = submitSavingsApplication(clientId, savingsProductId, ACTIVATION_DATE_STRING).getSavingsId();
        Assertions.assertNotNull(savingsId);

        approveSavings(savingsId, ACTIVATION_DATE_STRING);
        activateSavings(savingsId, ACTIVATION_DATE_STRING);
        deposit(savingsId, DEPOSIT_AMOUNT.toPlainString(), ACTIVATION_DATE_STRING);
        return savingsId;
    }

    private Long createCashBasedInterestBearingSavingsProduct() {
        final Account assetAccount = accountHelper.createAssetAccount();
        final Account liabilityAccount = accountHelper.createLiabilityAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final PostSavingsProductsRequest request = SavingsRequestBuilders.withCashBasedAccounting(
                SavingsRequestBuilders.defaultSavingsProduct(), assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        return createSavingsProduct(request).getResourceId();
    }

    private List<SavingsAccountTransactionData> activeInterestPostings(final Long savingsId) {
        return savingsTransactionHelper.getTransactions(savingsId).stream() //
                .filter(t -> t.getTransactionType() != null && Boolean.TRUE.equals(t.getTransactionType().getInterestPosting())) //
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())) //
                .toList();
    }

    /**
     * Cash-based interest posting must book one balanced entry pair (debit interest expense, credit savings control)
     * for the posted amount.
     */
    private void assertJournalEntriesBalanced(final Long savingsTransactionId, final BigDecimal expectedAmount) {
        final List<JournalEntryTransactionItem> entries = journalEntryHelper
                .getJournalEntriesByTransactionId(SAVINGS_JOURNAL_TRANSACTION_PREFIX + savingsTransactionId).getPageItems();
        assertThat(entries).as("Interest posting on a cash-based product must produce journal entries").isNotEmpty();

        // The generated client exposes the entry amount as Double; BigDecimal.valueOf keeps its shortest decimal form.
        final BigDecimal debits = sumByEntryType(entries, "DEBIT");
        final BigDecimal credits = sumByEntryType(entries, "CREDIT");
        assertThat(debits).as("Journal entries of the interest posting must be balanced").isEqualByComparingTo(credits);
        assertThat(debits).as("Journal entries must book the posted interest amount").isEqualByComparingTo(expectedAmount);
    }

    private static BigDecimal sumByEntryType(final List<JournalEntryTransactionItem> entries, final String entryType) {
        return entries.stream() //
                .filter(e -> e.getEntryType() != null && entryType.equals(e.getEntryType().getValue())) //
                .map(e -> BigDecimal.valueOf(e.getAmount())) //
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Long createDormancyTrackingSavingsProduct() {
        // defaultSavingsProduct() already configures daily compounding, monthly posting and daily-balance calculation;
        // here we only add the dormancy thresholds the UpdateSavingsDormantStatusBusinessStep relies on.
        final PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct() //
                .isDormancyTrackingActive(true) //
                .daysToInactive(30L) //
                .daysToDormancy(60L) //
                .daysToEscheat(90L);
        return createSavingsProduct(request).getResourceId();
    }

    private void executeInlineSavingsCOB(final List<Long> savingsIds) {
        ok(() -> fineractClient().inlineJob().executeInlineJob(SAVINGS_COB_JOB_NAME, new InlineJobRequest().savingsIds(savingsIds)));
    }
}
