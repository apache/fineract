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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class SavingsInterestPostingJobIntegrationTest extends FeignSavingsTestBase {

    private static final String POST_INTEREST_JOB_NAME = "Post Interest For Savings";

    private static final String START_DATE = "10 April 2022";
    private static final String JULY_START_DATE = "01 July 2022";
    private static final String CHARGE_START_DATE = "21 June 2022";
    private static final String DEPOSIT_AMOUNT = "10000";
    private static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("10000.0");
    private static final BigDecimal OVERDRAFT_INTEREST_RATE = new BigDecimal("10");

    @Test
    public void testSavingsBalanceCheckAfterDailyInterestPostingJob() {
        final Long savingsId = createActiveDailyPostingSavings(START_DATE);
        deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

        final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        // The RestAssured version read this through JsonPath, which narrowed it to a float and saw "10129.582".
        SavingsTestValidators.verifyAmount(new BigDecimal("10129.5818"), transactions.get(transactions.size() - 48).getRunningBalance(),
                "Equality check for Balance");
    }

    @Test
    public void testSavingsDailyInterestPostingJobWithAccountingNone() {
        final Long savingsId = createActiveSavings(START_DATE, minimumOpeningBalanceProduct());

        final Long transactionId = deposit(savingsId, "1000", START_DATE).getResourceId();
        assertNotNull(transactionId);

        assertEquals(0, journalEntryHelper.getJournalEntriesByTransactionId(String.valueOf(transactionId)).getPageItems().size(),
                "A product with no accounting should not post journal entries");
    }

    @Test
    public void testDuplicateOverdraftInterestPostingJob() {
        final Long savingsId = createActiveSavings(JULY_START_DATE, overdraftDailyPostingProduct());

        withdraw(savingsId, "1000", JULY_START_DATE);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);
        withdraw(savingsId, "1000", JULY_START_DATE);

        assertEquals(1, countActiveTransactionsOn(savingsId, LocalDate.of(2022, Month.JULY, 10)),
                "No Duplicate Overdraft Interest Posting");
    }

    @Test
    public void testSavingsDailyInterestPostingJob() {
        final LocalDate today = Utils.getLocalDateOfTenant();

        businessDateHelper.runAt(today.toString(), () -> {
            final Long savingsId = createActiveDailyPostingSavings(START_DATE);
            deposit(savingsId, DEPOSIT_AMOUNT, START_DATE);

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

            final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
            final SavingsAccountTransactionData interestPosting = transactions.get(transactions.size() - 3);

            SavingsTestValidators.verifyAmount(new BigDecimal("2.7405"), interestPosting.getAmount(),
                    "Equality check for interest posted amount");
            assertEquals(LocalDate.of(2022, Month.APRIL, 12), interestPosting.getDate(), "Date check for Interest Posting transaction");
            assertEquals(today, interestPosting.getSubmittedOnDate(), "Submitted On Date check for Interest Posting transaction");
        });
    }

    @Test
    public void testSavingsDailyOverdraftInterestPostingJob() {
        final Long savingsId = createActiveSavings(START_DATE, overdraftDailyPostingProduct());

        withdraw(savingsId, DEPOSIT_AMOUNT, START_DATE);
        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

        final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        final SavingsAccountTransactionData interestPosting = transactions.get(transactions.size() - 2);

        SavingsTestValidators.verifyAmount(new BigDecimal("2.7397"), interestPosting.getAmount(),
                "Equality check for overdatft interest posted amount");
        assertEquals(LocalDate.of(2022, Month.APRIL, 11), interestPosting.getDate(),
                "Date check for overdraft Interest Posting transaction");
    }

    @Test
    public void testAccountBalanceWithWithdrawalFeeAfterInterestPostingJob() {
        final Long savingsId = createActiveDailyPostingSavingsWithWithdrawalFee();

        deposit(savingsId, "1000", CHARGE_START_DATE);
        withdraw(savingsId, "100", CHARGE_START_DATE);
        SavingsTestValidators.verifyAmount(new BigDecimal("800.0"), savingsHelper.getSavingsSummary(savingsId).getAccountBalance(),
                "Verifying account balance is 800");

        schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB_NAME);

        final List<SavingsAccountTransactionData> transactions = savingsTransactionHelper.getTransactions(savingsId);
        SavingsTestValidators.verifyAmount(new BigDecimal("800.4384"), transactions.get(transactions.size() - 5).getRunningBalance(),
                "Equality check for Balance");
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

    private long countActiveTransactionsOn(final Long savingsId, final LocalDate date) {
        return savingsTransactionHelper.getTransactions(savingsId).stream().filter(t -> date.equals(t.getDate()))
                .filter(t -> !Boolean.TRUE.equals(t.getReversed())).count();
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
