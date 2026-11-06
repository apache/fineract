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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostTaxesComponentsRequest;
import org.apache.fineract.client.models.PostTaxesGroupRequest;
import org.apache.fineract.client.models.PostTaxesGroupTaxComponents;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class SavingsInterestPostingJobIntegrationTest extends FeignSavingsTestBase {

    private static final String POST_INTEREST_JOB_NAME = "Post Interest For Savings";

    private static final String START_DATE = "10 April 2022";
    private static final String JULY_START_DATE = "01 July 2022";
    private static final String CHARGE_START_DATE = "21 June 2022";
    private static final String DEPOSIT_AMOUNT = "10000";
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000.0");
    private static final BigDecimal OVERDRAFT_INTEREST_RATE = new BigDecimal("10");
    private static final String SEPTEMBER_START_DATE = "23 September 2022";
    private static final String SEPTEMBER_WITHDRAWAL_DATE = "28 September 2022";
    private static final BigDecimal LARGE_OVERDRAFT_LIMIT = new BigDecimal("20000.0");
    private static final BigDecimal NO_OVERDRAFT_INTEREST_RATE = BigDecimal.ZERO;

    private static final Predicate<SavingsAccountTransactionData> INTEREST_POSTING = t -> Boolean.TRUE
            .equals(t.getTransactionType().getInterestPosting());
    private static final Predicate<SavingsAccountTransactionData> OVERDRAFT_INTEREST = t -> Boolean.TRUE
            .equals(t.getTransactionType().getOverDraftInterestPosting());
    private static final Predicate<SavingsAccountTransactionData> ANY_INTEREST_POSTING = INTEREST_POSTING.or(OVERDRAFT_INTEREST);
    private static final Predicate<SavingsAccountTransactionData> WITHHOLD_TAX = t -> Boolean.TRUE
            .equals(t.getTransactionType().getWithholdTax());

    private static final String SAVINGS_TRANSACTION_ID_PREFIX = "S";
    private static final String DEBIT = "DEBIT";
    private static final String CREDIT = "CREDIT";
    private static final BigDecimal FIRST_TAX_PERCENTAGE = new BigDecimal("10");
    private static final BigDecimal SECOND_TAX_PERCENTAGE = new BigDecimal("5");

    private static final LocalDate POSTING_JOB_BUSINESS_DATE = LocalDate.of(2022, Month.JULY, 31);
    private static final String JULY_END_DATE = "31 July 2022";

    private final FeignTaxComponentHelper taxComponentHelper = new FeignTaxComponentHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private final FeignTaxGroupHelper taxGroupHelper = new FeignTaxGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testSavingsBalanceCheckAfterDailyInterestPostingJob() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            final Long savingsId = createActiveDailyPostingSavings(START_DATE);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
            // The RestAssured version read this through JsonPath, which narrowed it to a float and saw "10129.582".
            SavingsTestValidators.verifyAmount(new BigDecimal("10129.5818"), transactions.get(transactions.size() - 48).getRunningBalance(),
                    "Equality check for Balance");
        });
    }

    @Test
    public void testSavingsDailyInterestPostingJobWithAccountingNone() {
        final Long savingsId = createActiveSavings(START_DATE, minimumOpeningBalanceProduct());

        final Long transactionId = deposit(savingsId, "1000", START_DATE).getResourceId();
        assertNotNull(transactionId);

        assertEquals(0, journalEntryHelper.getJournalEntriesByTransactionId(String.valueOf(transactionId)).getPageItems().size(),
                "A product with no accounting should not post journal entries");
    }

    /**
     * A withhold-tax product under the job: every interest posting must come with a withhold tax transaction, and the
     * job must book its journal entries, savings control debited with the whole tax and the credit account of each tax
     * component credited with that component's share of it (the transaction's tax details).
     *
     * The second half pins the job's own correction path. Interest is posted at the period end, so the last posting of
     * the first run is dated 31 July, and a deposit dated that same day is not before the last posting period: the
     * deposit API leaves the posted periods alone, so nothing is reposted and no journal entries are booked by the
     * deposit. The deposit does raise the balance of the July period from its last day on, so the next run of the job
     * recalculates that period, reverses its interest and withhold tax transactions and replaces them. The poster also
     * reverses the journal entries of the withhold tax transaction it reverses, the way the accounting processors do:
     * entries with the debit and credit sides swapped under the same transaction id. The GL therefore carries the July
     * tax once, in the corrected amount, exactly like the account summary does.
     */
    @Test
    public void testPostInterestJobWithholdsTaxAndBooksJournalEntries() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            // posting at the period end is what dates the last posting of the first run on 31 July itself,
            // which the correction half below relies on
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final Account savingsReference = accountHelper.createAssetAccount("Savings Reference");
            final Account savingsControl = accountHelper.createLiabilityAccount("Savings Control");
            final Account interestOnSavings = accountHelper.createExpenseAccount("Interest on Savings");
            final Account incomeFromInterest = accountHelper.createIncomeAccount("Income From Interest");
            final Account firstTaxCredit = accountHelper.createLiabilityAccount("Withhold Tax One");
            final Account secondTaxCredit = accountHelper.createLiabilityAccount("Withhold Tax Two");

            final Long taxGroupId = withholdTaxGroup(firstTaxCredit, secondTaxCredit);
            final Long productId = createSavingsProduct(
                    withholdTaxProduct(taxGroupId, savingsReference, savingsControl, interestOnSavings, incomeFromInterest))
                    .getResourceId();
            final Long savingsId = createActiveSavings(START_DATE, productId);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> postings = interestPostings(savingsId, false, INTEREST_POSTING).toList();
            final List<SavingsAccountTransactionData> withholdTransactions = withholdTransactions(savingsId, false);
            assertTrue(!withholdTransactions.isEmpty(), "The job must create withhold tax transactions");
            assertEquals(postings.size(), withholdTransactions.size(), "Every interest posting must come with a withhold tax transaction");
            for (final SavingsAccountTransactionData withholdTransaction : withholdTransactions) {
                final SavingsAccountTransactionData interestPosting = postings.stream()
                        .filter(t -> withholdTransaction.getDate().equals(t.getDate())).findFirst().orElseThrow();
                verifyWithholdJournalEntries(withholdTransaction, interestPosting, savingsControl, firstTaxCredit, secondTaxCredit);
            }
            final BigDecimal firstRunTax = totalAmount(withholdTransactions);
            SavingsTestValidators.verifyAmount(firstRunTax, savingsHelper.getSavingsSummary(savingsId).getTotalWithholdTax(),
                    "The account summary must carry the total withheld tax");

            final SavingsAccountTransactionData julyWithhold = withholdTransactions.stream()
                    .filter(t -> POSTING_JOB_BUSINESS_DATE.equals(t.getDate())).findFirst().orElseThrow();

            // the deposit is dated on the last posting date itself, so it is not before the last posting
            // period and the deposit API does not repost anything: the posted periods stay as the job left
            // them, and only the raised July balance is left for the job to pick up
            deposit(savingsId, DEPOSIT_AMOUNT, JULY_END_DATE);
            assertEquals(0, withholdTransactions(savingsId, true).size(),
                    "A deposit dated on the last posting date must not repost interest through the deposit API");
            assertEquals(withholdTransactions.size(), withholdTransactions(savingsId, false).size(),
                    "The deposit must not change the withhold transactions before the job runs again");

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", POSTING_JOB_BUSINESS_DATE.plusDays(1).toString());
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> reversedWithholds = withholdTransactions(savingsId, true);
            final List<SavingsAccountTransactionData> correctedWithholds = withholdTransactions(savingsId, false);
            assertEquals(1, reversedWithholds.size(), "The job must reverse only the July withhold transaction");
            assertEquals(POSTING_JOB_BUSINESS_DATE, reversedWithholds.get(0).getDate(),
                    "The reversed withhold transaction must be the one of the July period");
            assertEquals(1, interestPostings(savingsId, true, INTEREST_POSTING).count(),
                    "The job must reverse only the July interest posting");
            assertEquals(withholdTransactions.size(), correctedWithholds.size(),
                    "The correction must replace the reversed withhold transaction with a new one");
            for (final SavingsAccountTransactionData reversedWithhold : reversedWithholds) {
                final List<JournalEntryTransactionItem> entries = journalEntriesOf(reversedWithhold);
                assertEquals(6, entries.size(),
                        "The reversed withhold transaction keeps its entries and gains their " + "reversals under the same transaction id");
                assertEquals(0, entryAmount(entries, DEBIT, savingsControl).compareTo(entryAmount(entries, CREDIT, savingsControl)),
                        "The reversal entries must credit the savings control back with the tax it was debited");
                assertEquals(0, entryAmount(entries, CREDIT, firstTaxCredit).compareTo(entryAmount(entries, DEBIT, firstTaxCredit)),
                        "The reversal entries must debit the first tax component's account back with its credited share");
                assertEquals(0, entryAmount(entries, CREDIT, secondTaxCredit).compareTo(entryAmount(entries, DEBIT, secondTaxCredit)),
                        "The reversal entries must debit the second tax component's account back with its credited share");
            }
            final SavingsAccountTransactionData correctedJulyWithhold = correctedWithholds.stream()
                    .filter(t -> POSTING_JOB_BUSINESS_DATE.equals(t.getDate())).findFirst().orElseThrow();
            assertTrue(correctedJulyWithhold.getAmount().compareTo(julyWithhold.getAmount()) > 0,
                    "The corrected July withhold tax must be higher than the first run's");
            final SavingsAccountTransactionData correctedJulyPosting = interestPostings(savingsId, false, INTEREST_POSTING)
                    .filter(t -> POSTING_JOB_BUSINESS_DATE.equals(t.getDate())).findFirst().orElseThrow();
            verifyWithholdJournalEntries(correctedJulyWithhold, correctedJulyPosting, savingsControl, firstTaxCredit, secondTaxCredit);
            final BigDecimal correctedTax = totalAmount(correctedWithholds);
            SavingsTestValidators.verifyAmount(correctedTax, savingsHelper.getSavingsSummary(savingsId).getTotalWithholdTax(),
                    "After the correction the account summary must carry only the corrected withheld tax");
            final BigDecimal taxCreditedInGl = totalCreditedTax(savingsId, firstTaxCredit, secondTaxCredit);
            assertEquals(0, taxCreditedInGl.compareTo(correctedTax),
                    "The GL must carry only the corrected withheld tax: the reversal entries cancel the first run's "
                            + "July tax, so the July withheld tax is not counted twice in the GL");
        });
    }

    @Test
    public void testDuplicateOverdraftInterestPostingJob() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            final Long savingsId = createActiveSavings(JULY_START_DATE, overdraftDailyPostingProduct());

            withdraw(savingsId, "1000", JULY_START_DATE);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            withdraw(savingsId, "1000", JULY_START_DATE);

            assertEquals(1, countActiveTransactionsOn(savingsId, LocalDate.of(2022, Month.JULY, 10)),
                    "No Duplicate Overdraft Interest Posting");
        });
    }

    @Test
    public void testSavingsDailyInterestPostingJob() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            final Long savingsId = createActiveDailyPostingSavings(START_DATE);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
            final SavingsAccountTransactionData interestPosting = transactions.get(transactions.size() - 3);

            SavingsTestValidators.verifyAmount(new BigDecimal("2.7405"), interestPosting.getAmount(),
                    "Equality check for interest posted amount");
            assertEquals(LocalDate.of(2022, Month.APRIL, 12), interestPosting.getDate(), "Date check for Interest Posting transaction");
            assertEquals(POSTING_JOB_BUSINESS_DATE, interestPosting.getSubmittedOnDate(),
                    "Submitted On Date check for Interest Posting transaction");
        });
    }

    @Test
    public void testSavingsDailyOverdraftInterestPostingJob() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            final Long savingsId = createActiveSavings(START_DATE, overdraftDailyPostingProduct());

            withdraw(savingsId, DEPOSIT_AMOUNT, START_DATE);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
            final SavingsAccountTransactionData interestPosting = transactions.get(transactions.size() - 2);

            SavingsTestValidators.verifyAmount(new BigDecimal("2.7397"), interestPosting.getAmount(),
                    "Equality check for overdatft interest posted amount");
            assertEquals(LocalDate.of(2022, Month.APRIL, 11), interestPosting.getDate(),
                    "Date check for overdraft Interest Posting transaction");
        });
    }

    @Test
    public void testHoldOnOverdrawnAccountIsNotReversedByInterestPostingJob() {
        final Long savingsId = createActiveSavings(START_DATE, overdraftDailyPostingProduct());

        withdraw(savingsId, "100", START_DATE);
        final Long holdTransactionId = savingsTransactionHelper.holdAmount(savingsId, "50", START_DATE, "manual hold").getResourceId();
        assertNotNull(holdTransactionId);

        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

        final SavingsAccountTransactionData holdTransaction = savingsTransactionHelper.getTransaction(savingsId, holdTransactionId);
        assertEquals(Boolean.FALSE, holdTransaction.getReversed(), "Interest posting must not reverse the hold transaction");
        SavingsTestValidators.verifyAmount(new BigDecimal("-100"), holdTransaction.getRunningBalance(),
                "Hold running balance must remain equal to the ledger balance");
        assertEquals(0,
                BigDecimal.ZERO
                        .compareTo(holdTransaction.getOverdraftAmount() == null ? BigDecimal.ZERO : holdTransaction.getOverdraftAmount()),
                "Hold transaction must not carry an overdraft amount");
    }

    @Test
    public void testAccountBalanceWithWithdrawalFeeAfterInterestPostingJob() {
        businessDateHelper.runAt(POSTING_JOB_BUSINESS_DATE.toString(), () -> {
            final Long savingsId = createActiveDailyPostingSavingsWithWithdrawalFee();

            deposit(savingsId, "1000", CHARGE_START_DATE);
            withdraw(savingsId, "100", CHARGE_START_DATE);
            SavingsTestValidators.verifyAmount(new BigDecimal("800.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                    "Verifying account balance is 800");

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
            SavingsTestValidators.verifyAmount(new BigDecimal("800.4384"), transactions.get(transactions.size() - 5).getRunningBalance(),
                    "Equality check for Balance");
        });
    }

    @Test
    public void testRunningPostInterestJobTwiceDoesNotCreateDuplicateInterest() {
        businessDateHelper.runAt("2022-04-13", () -> {
            final Long savingsId = createActiveDailyPostingSavings(START_DATE);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            assertEquals(1, countActiveTransactionsOn(savingsId, LocalDate.of(2022, Month.APRIL, 12)),
                    "Running job twice must not create duplicate interest postings on the same date");
        });
    }

    @Test
    public void testAccountBalanceUnchangedAfterRunningPostInterestJobTwice() {
        businessDateHelper.runAt("2022-04-13", () -> {
            final Long savingsId = createActiveDailyPostingSavings(START_DATE);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            final BigDecimal balanceAfterFirstRun = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            final BigDecimal balanceAfterSecondRun = savingsHelper.getSavingsSummary(savingsId).getAccountBalance();

            SavingsTestValidators.verifyAmount(balanceAfterFirstRun, balanceAfterSecondRun,
                    "Account balance must not change when job runs twice on the same business date");
        });
    }

    @Test
    public void testRunningPostInterestJobTwiceDoesNotCreateDuplicateOverdraftInterest() {
        businessDateHelper.runAt("2022-04-13", () -> {
            final Long savingsId = createActiveSavings(START_DATE, overdraftDailyPostingProduct());
            withdraw(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            assertEquals(1, countActiveInterestOn(savingsId, OVERDRAFT_INTEREST, LocalDate.of(2022, Month.APRIL, 11)),
                    "Running job twice must not create duplicate overdraft interest postings on the same date");
            // A daily posting product posts on every day of the period, so checking one date would miss a duplicate
            // created on any of the others.
            final List<LocalDate> postingDates = interestPostings(savingsId, false, OVERDRAFT_INTEREST)
                    .map(SavingsAccountTransactionData::getDate).toList();
            assertEquals(Set.copyOf(postingDates).size(), postingDates.size(),
                    "Every posted date must hold a single overdraft interest posting, found " + postingDates);
        });
    }

    /**
     * The posting date of a monthly period stays fixed while the business date moves on, so every run of the job looks
     * up the same date again. Each lookup has to find the overdraft interest already posted there.
     */
    @Test
    public void testOverdraftInterestNotDuplicatedOnSuccessiveBusinessDates() {
        businessDateHelper.runAt("2022-09-30", () -> {
            final Long savingsId = createActiveSavings(SEPTEMBER_START_DATE, overdraftMonthlyPostingProduct());
            deposit(savingsId, "5000", SEPTEMBER_START_DATE);
            withdraw(savingsId, "16000", SEPTEMBER_WITHDRAWAL_DATE);

            final LocalDate postingDate = LocalDate.of(2022, Month.OCTOBER, 1);
            for (int day = 0; day < 3; day++) {
                businessDateHelper.updateBusinessDate("BUSINESS_DATE", postingDate.plusDays(day).toString());
                schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            }

            assertEquals(1, countActiveInterestOn(savingsId, OVERDRAFT_INTEREST, postingDate),
                    "Successive business dates must not create duplicate overdraft interest postings on the posting date");
            assertEquals(1, interestPostings(savingsId, false, OVERDRAFT_INTEREST).count(),
                    "Only the September period is closed, so the account must hold a single overdraft interest posting");
        });
    }

    /**
     * A product mapped to an interest receivable GL account builds two posting periods per interval, one for the
     * positive balance days and one for the overdraft days, and both share the posting date. The lookup has to tell the
     * two apart, so neither run of the job reverses the posting of the other type.
     */
    @Test
    public void testBothPostingsSurviveOnAnInterestReceivableProduct() {
        businessDateHelper.runAt("2022-09-30", () -> {
            final Long savingsId = createActiveSavings(SEPTEMBER_START_DATE, overdraftReceivableProduct(OVERDRAFT_INTEREST_RATE));
            deposit(savingsId, "5000", SEPTEMBER_START_DATE);
            withdraw(savingsId, "16000", SEPTEMBER_WITHDRAWAL_DATE);

            final LocalDate postingDate = LocalDate.of(2022, Month.OCTOBER, 1);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", postingDate.toString());
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            assertEquals(1, countActiveInterestOn(savingsId, INTEREST_POSTING, postingDate),
                    "The positive balance days must keep a single regular interest posting");
            assertEquals(1, countActiveInterestOn(savingsId, OVERDRAFT_INTEREST, postingDate),
                    "The overdraft days must keep a single overdraft interest posting");
            assertEquals(0, interestPostings(savingsId, true, ANY_INTEREST_POSTING).count(),
                    "Neither posting may be reversed: each period looks up its own transaction type");
        });
    }

    /**
     * Same two-period product, but the overdraft days earn nothing because the overdraft rate is zero. That period must
     * neither take the regular interest posting that shares its date for its own, nor post a zero amount transaction of
     * its own.
     */
    @Test
    public void testZeroOverdraftInterestPostsNothingAndLeavesTheInterestPostingAlone() {
        businessDateHelper.runAt("2022-09-30", () -> {
            final Long savingsId = createActiveSavings(SEPTEMBER_START_DATE, overdraftReceivableProduct(NO_OVERDRAFT_INTEREST_RATE));
            deposit(savingsId, "5000", SEPTEMBER_START_DATE);
            withdraw(savingsId, "16000", SEPTEMBER_WITHDRAWAL_DATE);

            final LocalDate postingDate = LocalDate.of(2022, Month.OCTOBER, 1);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", postingDate.toString());
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            assertEquals(1, countActiveInterestOn(savingsId, INTEREST_POSTING, postingDate),
                    "The interest earned on the positive balance days must stay posted");
            assertEquals(0, interestPostings(savingsId, true, ANY_INTEREST_POSTING).count(),
                    "A zero earning overdraft period must not reverse the interest posting sharing its date");
            assertEquals(0,
                    interestPostings(savingsId, false, ANY_INTEREST_POSTING)
                            .filter(t -> t.getAmount() == null || t.getAmount().signum() == 0).count(),
                    "A zero earning period must not post a transaction");
        });
    }

    private long countActiveInterestOn(final Long savingsId, final Predicate<SavingsAccountTransactionData> type, final LocalDate date) {
        return interestPostings(savingsId, false, type).filter(t -> date.equals(t.getDate())).count();
    }

    private Stream<SavingsAccountTransactionData> interestPostings(final Long savingsId, final boolean reversed,
            final Predicate<SavingsAccountTransactionData> type) {
        return savingsTransactionHelper.getTransactions(savingsId).stream().filter(t -> reversed == Boolean.TRUE.equals(t.getReversed()))
                .filter(t -> t.getTransactionType() != null).filter(type);
    }

    private long countActiveTransactionsOn(final Long savingsId, final LocalDate date) {
        return savingsTransactionHelper.getTransactions(savingsId).stream().filter(t -> date.equals(t.getDate()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())).count();
    }

    private List<SavingsAccountTransactionData> withholdTransactions(final Long savingsId, final boolean reversed) {
        return savingsTransactionHelper.getTransactions(savingsId).stream().filter(t -> reversed == Boolean.TRUE.equals(t.getReversed()))
                .filter(t -> t.getTransactionType() != null).filter(WITHHOLD_TAX).toList();
    }

    private List<JournalEntryTransactionItem> journalEntriesOf(final SavingsAccountTransactionData transaction) {
        final List<JournalEntryTransactionItem> pageItems = journalEntryHelper
                .getJournalEntriesByTransactionId(SAVINGS_TRANSACTION_ID_PREFIX + transaction.getId()).getPageItems();
        return pageItems == null ? List.of() : pageItems;
    }

    private void verifyWithholdJournalEntries(final SavingsAccountTransactionData withholdTransaction,
            final SavingsAccountTransactionData interestPosting, final Account savingsControl, final Account firstTaxCredit,
            final Account secondTaxCredit) {
        final List<JournalEntryTransactionItem> entries = journalEntriesOf(withholdTransaction);
        assertEquals(3, entries.size(), "A withhold tax transaction must book one debit and one credit entry per tax component");
        final BigDecimal debit = entryAmount(entries, DEBIT, savingsControl);
        final BigDecimal firstShare = entryAmount(entries, CREDIT, firstTaxCredit);
        final BigDecimal secondShare = entryAmount(entries, CREDIT, secondTaxCredit);
        assertNotNull(debit, "Savings control must be debited with the withheld tax");
        assertNotNull(firstShare, "The first tax component's credit account must be credited with its share");
        assertNotNull(secondShare, "The second tax component's credit account must be credited with its share");
        assertEquals(0, debit.compareTo(withholdTransaction.getAmount()), "The debited tax must equal the withhold tax transaction amount");
        assertEquals(0, firstShare.add(secondShare).compareTo(debit),
                "The per-component credits are the tax details of the transaction and must add up to the debited tax");
        // each component is charged on the posted interest at its own percentage, one rounding step aside
        assertCloseTo(interestPosting.getAmount().multiply(FIRST_TAX_PERCENTAGE).movePointLeft(2), firstShare,
                "The first component's share must be its percentage of the posted interest");
        assertCloseTo(interestPosting.getAmount().multiply(SECOND_TAX_PERCENTAGE).movePointLeft(2), secondShare,
                "The second component's share must be its percentage of the posted interest");
    }

    private BigDecimal entryAmount(final List<JournalEntryTransactionItem> entries, final String entryType, final Account account) {
        final Long accountId = SavingsRequestBuilders.accountId(account);
        BigDecimal total = null;
        for (final JournalEntryTransactionItem entry : entries) {
            if (entry.getEntryType() != null && entryType.equals(entry.getEntryType().getValue())
                    && accountId.equals(entry.getGlAccountId())) {
                if (entry.getAmount() == null) {
                    return null;
                }
                final BigDecimal amount = BigDecimal.valueOf(entry.getAmount());
                total = total == null ? amount : total.add(amount);
            }
        }
        return total;
    }

    private BigDecimal totalCreditedTax(final Long savingsId, final Account firstTaxCredit, final Account secondTaxCredit) {
        BigDecimal total = BigDecimal.ZERO;
        for (final boolean reversed : List.of(false, true)) {
            for (final SavingsAccountTransactionData withholdTransaction : withholdTransactions(savingsId, reversed)) {
                final List<JournalEntryTransactionItem> entries = journalEntriesOf(withholdTransaction);
                final BigDecimal firstShare = entryAmount(entries, CREDIT, firstTaxCredit);
                final BigDecimal secondShare = entryAmount(entries, CREDIT, secondTaxCredit);
                assertNotNull(firstShare, "Every withhold transaction must credit the first tax component's account");
                assertNotNull(secondShare, "Every withhold transaction must credit the second tax component's account");
                // the journal entries of a reversed withhold transaction are reversed themselves, so its
                // shares are debited back and must not count towards the credited tax
                total = total.add(firstShare).add(secondShare).subtract(entryAmountOrZero(entries, DEBIT, firstTaxCredit))
                        .subtract(entryAmountOrZero(entries, DEBIT, secondTaxCredit));
            }
        }
        return total;
    }

    private BigDecimal entryAmountOrZero(final List<JournalEntryTransactionItem> entries, final String entryType, final Account account) {
        final BigDecimal amount = entryAmount(entries, entryType, account);
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private BigDecimal totalAmount(final List<SavingsAccountTransactionData> transactions) {
        return transactions.stream().map(SavingsAccountTransactionData::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertCloseTo(final BigDecimal expected, final BigDecimal actual, final String message) {
        assertNotNull(actual, message);
        assertTrue(actual.subtract(expected).abs().compareTo(new BigDecimal("0.01")) <= 0,
                message + " - expected about " + expected + " but was " + actual);
    }

    private Long withholdTaxGroup(final Account firstTaxCredit, final Account secondTaxCredit) {
        final Long firstComponentId = createTaxComponent("Withhold tax one", FIRST_TAX_PERCENTAGE, firstTaxCredit);
        final Long secondComponentId = createTaxComponent("Withhold tax two", SECOND_TAX_PERCENTAGE, secondTaxCredit);
        return taxGroupHelper.createTaxGroup(new PostTaxesGroupRequest()//
                .name(Utils.uniqueRandomStringGenerator("Tax_group_Name_", 5))//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .taxComponents(Set.of(new PostTaxesGroupTaxComponents().taxComponentId(firstComponentId).startDate("01 January 2013"),
                        new PostTaxesGroupTaxComponents().taxComponentId(secondComponentId).startDate("01 January 2013"))))
                .getResourceId();
    }

    private Long createTaxComponent(final String name, final BigDecimal percentage, final Account creditAccount) {
        return taxComponentHelper.createTaxComponent(new PostTaxesComponentsRequest()//
                .name(Utils.uniqueRandomStringGenerator(name + "_", 5))//
                .percentage(percentage.floatValue())//
                .startDate("01 January 2013")//
                .creditAccountType(Integer.valueOf(creditAccount.getAccountType().toString()))//
                .creditAccountId(creditAccount.getAccountID().longValue())//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)).getResourceId();
    }

    /** Cash-based accounting, so the product carries the mappings the poster's journal entries are booked against. */
    private PostSavingsProductsRequest withholdTaxProduct(final Long taxGroupId, final Account savingsReference,
            final Account savingsControl, final Account interestOnSavings, final Account incomeFromInterest) {
        final Account overdraftPortfolioControl = accountHelper.createAssetAccount("Overdraft Portfolio Control");
        final Account transfersInSuspense = accountHelper.createLiabilityAccount("Transfers In Suspense");
        final Account incomeFromFee = accountHelper.createIncomeAccount("Income From Fee");
        final Account incomeFromPenalty = accountHelper.createIncomeAccount("Income From Penalty");
        final Account writeOff = accountHelper.createExpenseAccount("Write Off");
        return SavingsRequestBuilders
                .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY, SavingsTestData.InterestPostingPeriodType.MONTHLY,
                        SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .savingsReferenceAccountId(SavingsRequestBuilders.accountId(savingsReference))//
                .overdraftPortfolioControlId(SavingsRequestBuilders.accountId(overdraftPortfolioControl))//
                .savingsControlAccountId(SavingsRequestBuilders.accountId(savingsControl))//
                .transfersInSuspenseAccountId(SavingsRequestBuilders.accountId(transfersInSuspense))//
                .incomeFromFeeAccountId(SavingsRequestBuilders.accountId(incomeFromFee))//
                .incomeFromPenaltyAccountId(SavingsRequestBuilders.accountId(incomeFromPenalty))//
                .incomeFromInterestId(SavingsRequestBuilders.accountId(incomeFromInterest))//
                .interestOnSavingsAccountId(SavingsRequestBuilders.accountId(interestOnSavings))//
                .writeOffAccountId(SavingsRequestBuilders.accountId(writeOff))//
                .withHoldTax(true)//
                .taxGroupId(taxGroupId);
    }

    private Long createActiveDailyPostingSavings(final String startDate) {
        return createActiveSavings(startDate, dailyPostingProduct());
    }

    private Long createActiveSavings(final String startDate, final Long productId) {
        final Long clientId = createClient(startDate);
        assertNotNull(clientId);

        final Long savingsId = submitSavingsApplication(clientId, productId, startDate).getSavingsId();
        assertNotNull(savingsId);

        approveSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, startDate);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    /** The charge has to be attached while the account is still pending. */
    private Long createActiveDailyPostingSavingsWithWithdrawalFee() {
        final Long clientId = createClient(CHARGE_START_DATE);
        assertNotNull(clientId);

        final Long savingsId = submitSavingsApplication(clientId, dailyPostingProduct(), CHARGE_START_DATE).getSavingsId();
        assertNotNull(savingsId);

        final Long withdrawalChargeId = savingsChargeHelper.createWithdrawalFeeCharge().getResourceId();
        assertNotNull(withdrawalChargeId);
        savingsChargeHelper.addChargeToSavings(savingsId, withdrawalChargeId, SavingsTestData.DEFAULT_CHARGE_AMOUNT.floatValue());

        approveSavings(savingsId, CHARGE_START_DATE);
        SavingsTestValidators.verifySavingsIsApproved(savingsHelper.getSavingsStatus(savingsId));

        activateSavings(savingsId, CHARGE_START_DATE);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsId));
        return savingsId;
    }

    private Long dailyPostingProduct() {
        return createSavingsProduct(dailyPostingProductRequest()).getResourceId();
    }

    /** Monthly posting: the legacy builder never overrode the posting period, so it kept the monthly default. */
    private Long minimumOpeningBalanceProduct() {
        return createSavingsProduct(dailyPostingProductRequest()//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .minRequiredOpeningBalance(new BigDecimal("1000"))//
                .accountingRule(SavingsTestData.AccountingRule.NONE)).getResourceId();
    }

    private Long overdraftDailyPostingProduct() {
        return savingsProductHelper.createSavingsProduct(dailyPostingProductRequest()//
                .allowOverdraft(true)//
                .overdraftLimit(OVERDRAFT_LIMIT)//
                .nominalAnnualInterestRateOverdraft(OVERDRAFT_INTEREST_RATE)).getResourceId();
    }

    /** Accounting is left at NONE, so the product has no interest receivable mapping. */
    private Long overdraftMonthlyPostingProduct() {
        return savingsProductHelper.createSavingsProduct(overdraftMonthlyPostingRequest(OVERDRAFT_INTEREST_RATE)).getResourceId();
    }

    /**
     * Accrual accounting with an interest receivable mapping: the mapping is what makes the interest posting split into
     * a positive balance period and an overdraft period sharing one posting date.
     */
    private Long overdraftReceivableProduct(final BigDecimal overdraftInterestRate) {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");
        final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");

        final PostSavingsProductsRequest request = overdraftMonthlyPostingRequest(overdraftInterestRate)//
                .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC);

        return savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.withAccrualAccountingMappings(request, assetAccount,
                liabilityAccount, incomeAccount, expenseAccount, interestReceivableAccount)).getResourceId();
    }

    private PostSavingsProductsRequest overdraftMonthlyPostingRequest(final BigDecimal overdraftInterestRate) {
        return dailyPostingProductRequest()//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .allowOverdraft(true)//
                .overdraftLimit(LARGE_OVERDRAFT_LIMIT)//
                .nominalAnnualInterestRateOverdraft(overdraftInterestRate);
    }

    private PostSavingsProductsRequest dailyPostingProductRequest() {
        return SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                SavingsTestData.InterestPostingPeriodType.DAILY, SavingsTestData.InterestCalculationType.DAILY_BALANCE);
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
