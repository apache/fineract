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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestValidators;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(2)
public class SavingsAccrualIntegrationTest extends FeignSavingsTestBase {

    private static final String ACCRUAL_JOB = "Add Accrual Transactions For Savings";

    private static final String BUSINESS_DATE = "2021-08-12";
    private static final LocalDate TODAY = LocalDate.of(2021, 8, 12);
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2020";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private static final Double INTEREST_RATE = 10.0;
    private static final String DEPOSIT_AMOUNT = "10000";
    private static final int DAYS_TO_TEST = 10;
    private static final int DAYS_UNTIL_TRANSACTION = 5;
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final BigDecimal PERCENT = new BigDecimal("100");

    @Test
    public void testAccrualsAreGeneratedForTenDayPeriod() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final LocalDate startDate = TODAY.minusDays(DAYS_TO_TEST);
            final Long savingsAccountId = createFundedSavingsAccount(startDate);

            schedulerHelper.executeAndAwaitJob(ACCRUAL_JOB);

            final List<SavingsAccountTransactionData> accrualTransactions = savingsTransactionHelper
                    .getAccrualTransactions(savingsAccountId);
            assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found.");

            final long daysBetween = ChronoUnit.DAYS.between(startDate, TODAY);
            final long actualNumberOfTransactions = accrualTransactions.size();
            assertTrue(actualNumberOfTransactions >= daysBetween && actualNumberOfTransactions <= daysBetween + 1, "For a period of "
                    + daysBetween + " days, a close number of transactions was expected, but found " + actualNumberOfTransactions);

            final BigDecimal expectedTotalAccrual = dailyInterest(new BigDecimal(DEPOSIT_AMOUNT), 8)
                    .multiply(BigDecimal.valueOf(actualNumberOfTransactions)).setScale(2, RoundingMode.HALF_EVEN);

            SavingsTestValidators.verifyAmount(expectedTotalAccrual, savingsTransactionHelper.getTotalAccrualAmount(savingsAccountId),
                    "Verifying the total accrual");
        });
    }

    @Test
    public void testAccrualsAreReversedAndRecalculatedAfterBackdatedTransaction() {
        businessDateHelper.runAt(BUSINESS_DATE, () -> {
            final LocalDate startDate = TODAY.minusDays(DAYS_TO_TEST);
            final Long savingsAccountId = createFundedSavingsAccount(startDate);

            schedulerHelper.executeAndAwaitJob(ACCRUAL_JOB);

            final LocalDate backdatedTransactionDate = startDate.plusDays(DAYS_UNTIL_TRANSACTION);
            final BigDecimal withdrawalAmount = new BigDecimal("1000");
            withdraw(savingsAccountId, withdrawalAmount.toPlainString(), DATE_FORMATTER.format(backdatedTransactionDate));

            schedulerHelper.executeAndAwaitJob(ACCRUAL_JOB);

            final List<SavingsAccountTransactionData> accrualTransactions = savingsTransactionHelper
                    .getAccrualTransactions(savingsAccountId);
            verifyAccrualsWereReversedFrom(backdatedTransactionDate, accrualTransactions);

            final BigDecimal remainingBalance = new BigDecimal(DEPOSIT_AMOUNT).subtract(withdrawalAmount);
            final BigDecimal expectedDailyInterest = dailyInterest(remainingBalance, 4);

            boolean newAccrualVerified = false;
            for (SavingsAccountTransactionData accrual : accrualTransactions) {
                if (!Boolean.TRUE.equals(accrual.getReversed()) && !accrual.getDate().isBefore(backdatedTransactionDate)) {
                    SavingsTestValidators.verifyAmount(expectedDailyInterest, accrual.getAmount().setScale(4, RoundingMode.HALF_EVEN),
                            "Verifying the recalculated accrual of " + accrual.getDate());
                    newAccrualVerified = true;
                }
            }
            assertTrue(newAccrualVerified, "Could not verify the mathematical calculation of a new accrual.");
        });
    }

    /** Dates before the backdated transaction keep one accrual; dates from it carry the reversed one plus a new one. */
    private void verifyAccrualsWereReversedFrom(final LocalDate backdatedTransactionDate,
            final List<SavingsAccountTransactionData> accrualTransactions) {
        final Map<LocalDate, Long> accrualsByDate = new HashMap<>();
        final Map<LocalDate, Long> reversedAccrualsByDate = new HashMap<>();
        for (SavingsAccountTransactionData accrual : accrualTransactions) {
            accrualsByDate.merge(accrual.getDate(), 1L, Long::sum);
            if (Boolean.TRUE.equals(accrual.getReversed())) {
                reversedAccrualsByDate.merge(accrual.getDate(), 1L, Long::sum);
            }
        }
        assertFalse(accrualsByDate.isEmpty(), "No accrual transactions were found to verify.");

        for (Map.Entry<LocalDate, Long> entry : accrualsByDate.entrySet()) {
            final LocalDate date = entry.getKey();
            final long reversed = reversedAccrualsByDate.getOrDefault(date, 0L);
            if (date.isBefore(backdatedTransactionDate)) {
                assertEquals(1L, entry.getValue(), "There should be 1 accrual for the date " + date);
                assertEquals(0L, reversed, "The accrual for the date " + date + " should not be reversed.");
            } else {
                assertEquals(2L, entry.getValue(), "There should be 2 accruals (original and new) for the date " + date);
                assertEquals(1L, reversed, "There should be 1 reversed accrual for the date " + date);
            }
        }
    }

    private BigDecimal dailyInterest(final BigDecimal balance, final int scale) {
        return balance.multiply(BigDecimal.valueOf(INTEREST_RATE)).divide(PERCENT.multiply(DAYS_IN_YEAR), scale, RoundingMode.HALF_EVEN);
    }

    private Long createFundedSavingsAccount(final LocalDate startDate) {
        final Account assetAccount = accountHelper.createAssetAccount("assetAccount");
        final Account liabilityAccount = accountHelper.createLiabilityAccount("liabilityAccount");
        final Account incomeAccount = accountHelper.createIncomeAccount("incomeAccount");
        final Account expenseAccount = accountHelper.createExpenseAccount("expenseAccount");

        final Long savingsProductId = savingsProductHelper.createSavingsProduct(SavingsRequestBuilders.withAccrualAccountingMappings(
                SavingsRequestBuilders
                        .savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                                SavingsTestData.InterestPostingPeriodType.MONTHLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE)
                        .nominalAnnualInterestRate(INTEREST_RATE)//
                        .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC),
                assetAccount, liabilityAccount, incomeAccount, expenseAccount)).getResourceId();
        assertNotNull(savingsProductId, "Error creating savings product.");

        final Long clientId = createClient(CLIENT_ACTIVATION_DATE);
        assertNotNull(clientId, "Error creating client.");

        final String startDateString = DATE_FORMATTER.format(startDate);
        final Long savingsAccountId = submitSavingsApplication(clientId, savingsProductId, startDateString).getSavingsId();
        assertNotNull(savingsAccountId, "Error applying for savings account.");

        approveSavings(savingsAccountId, startDateString);
        activateSavings(savingsAccountId, startDateString);
        SavingsTestValidators.verifySavingsIsActive(savingsHelper.getSavingsStatus(savingsAccountId));

        deposit(savingsAccountId, DEPOSIT_AMOUNT, startDateString);
        return savingsAccountId;
    }
}
