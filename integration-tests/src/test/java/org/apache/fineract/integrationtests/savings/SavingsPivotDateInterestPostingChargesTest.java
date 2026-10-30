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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.junit.jupiter.api.Test;

/**
 * With the pivot date configuration on, the Post Interest For Savings job recalculates the account balance from the
 * running balance on the pivot date and the transactions after it. A fee paid after the pivot date has to stay in that
 * balance.
 *
 * <p>
 * The fee is paid through the pay charge command, which always recalculates the whole summary without the pivot date
 * configuration, so the balance is correct before the job runs and only the job's recalculation is under test.
 */
class SavingsPivotDateInterestPostingChargesTest extends FeignSavingsTestBase {

    private static final String POST_INTEREST_JOB = "Post Interest For Savings";

    private static final String ACTIVATION_DATE = "01 August 2021";
    private static final String AUGUST_FEE_DATE = "10 August 2021";
    private static final String SEPTEMBER_FEE_DATE = "10 September 2021";
    // the days after the August and September month ends, so each run posts exactly one more month of interest
    private static final String AFTER_AUGUST = "2021-09-02";
    private static final String AFTER_SEPTEMBER = "2021-10-02";

    private static final String DEPOSIT = "1000";
    private static final BigDecimal FEE = new BigDecimal("40");

    @Test
    void firstInterestPostingKeepsAFeePaidSinceActivationInTheBalance() {
        withPivotDateConfiguration(() -> runAt(AFTER_AUGUST, () -> {
            final Long savingsId = createFundedSavingsAccount();
            payFee(savingsId, AUGUST_FEE_DATE);
            final BigDecimal balanceBeforePosting = balanceOf(savingsId);
            assertEquals(0, new BigDecimal(DEPOSIT).subtract(FEE).compareTo(balanceBeforePosting),
                    "The pay charge command should leave the fee in the balance");

            schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB);

            assertBalanceIsPreviousBalancePlusNewInterest(savingsId, balanceBeforePosting, List.of());
        }));
    }

    @Test
    void laterInterestPostingKeepsAFeePaidSinceThePivotDateInTheBalance() {
        withPivotDateConfiguration(() -> {
            final Long[] savingsId = new Long[1];
            final List<Long> augustPostings = new ArrayList<>();
            runAt(AFTER_AUGUST, () -> {
                savingsId[0] = createFundedSavingsAccount();
                schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB);
                augustPostings.addAll(interestPostingIdsOf(savingsId[0]));
                assertEquals(1, augustPostings.size(), "The August run should post one month of interest");
            });
            runAt(AFTER_SEPTEMBER, () -> {
                payFee(savingsId[0], SEPTEMBER_FEE_DATE);
                final BigDecimal balanceBeforePosting = balanceOf(savingsId[0]);

                schedulerHelper.executeAndAwaitJob(POST_INTEREST_JOB);

                assertBalanceIsPreviousBalancePlusNewInterest(savingsId[0], balanceBeforePosting, augustPostings);
            });
        });
    }

    private void assertBalanceIsPreviousBalancePlusNewInterest(final Long savingsId, final BigDecimal balanceBeforePosting,
            final List<Long> earlierPostings) {
        final List<SavingsAccountTransactionData> newPostings = interestPostingsOf(savingsId).stream()
                .filter(posting -> !earlierPostings.contains(posting.getId())).toList();
        assertEquals(1, newPostings.size(), "The job should post one month of interest");
        final BigDecimal interest = newPostings.get(0).getAmount();
        assertTrue(interest.signum() > 0, "The job should post a positive amount of interest");

        final BigDecimal expected = balanceBeforePosting.add(interest);
        final BigDecimal actual = balanceOf(savingsId);
        assertEquals(0, expected.compareTo(actual), "Expected balance " + expected + " (" + balanceBeforePosting + " + interest " + interest
                + ") after the interest posting, but was " + actual);
    }

    private Long createFundedSavingsAccount() {
        final Long productId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders
                        .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.MONTHLY,
                                SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                        .nominalAnnualInterestRate(10.0))
                .getResourceId();
        assertNotNull(productId);
        final Long clientId = createClient(ACTIVATION_DATE);
        final Long savingsId = createApproveActivateSavings(clientId, productId, ACTIVATION_DATE);
        deposit(savingsId, DEPOSIT, ACTIVATION_DATE);
        return savingsId;
    }

    private void payFee(final Long savingsId, final String date) {
        final Long chargeId = savingsChargeHelper
                .createCharge(SavingsRequestBuilders.savingsSpecifiedDueDateCharge(FEE.doubleValue(), SavingsTestData.CURRENCY_CODE))
                .getResourceId();
        final Long savingsChargeId = savingsChargeHelper.addChargeWithDueDate(savingsId, chargeId, date, FEE.toPlainString())
                .getResourceId();
        savingsChargeHelper.payCharge(savingsId, savingsChargeId, FEE.toPlainString(), date);
    }

    private BigDecimal balanceOf(final Long savingsId) {
        return savingsHelper.getSavingsSummary(savingsId).getAccountBalance();
    }

    private List<Long> interestPostingIdsOf(final Long savingsId) {
        return interestPostingsOf(savingsId).stream().map(SavingsAccountTransactionData::getId).toList();
    }

    private List<SavingsAccountTransactionData> interestPostingsOf(final Long savingsId) {
        return savingsTransactionHelper.getTransactions(savingsId).stream()
                .filter(transaction -> transaction.getTransactionType() != null
                        && Boolean.TRUE.equals(transaction.getTransactionType().getInterestPosting())
                        && !Boolean.TRUE.equals(transaction.getReversed()))
                .sorted(Comparator.comparing(SavingsAccountTransactionData::getId)).toList();
    }

    /** Disabling backdated transactions before interest posting is what turns the pivot date configuration on. */
    private void withPivotDateConfiguration(final Runnable action) {
        globalConfigurationHelper.updateGlobalConfiguration(
                GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING,
                new PutGlobalConfigurationsRequest().enabled(false));
        try {
            action.run();
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(
                    GlobalConfigurationConstants.ALLOW_BACKDATED_TRANSACTION_BEFORE_INTEREST_POSTING,
                    new PutGlobalConfigurationsRequest().enabled(true));
        }
    }
}
