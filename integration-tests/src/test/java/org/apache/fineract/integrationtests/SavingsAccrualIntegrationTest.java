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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsAccrualIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccrualIntegrationTest.class);
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testAccrualsAreGeneratedForTenDayPeriod() {
        runAt("12 August 2021", () -> {
            // --- ARRANGE ---

            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final String interestRate = "10.0";
            final int daysToTest = 10;

            final SavingsProductHelper productHelper = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily()
                    .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                    .withNominalAnnualInterestRate(new BigDecimal(interestRate))
                    .withAccountingRuleAsAccrualBased(new Account[] { assetAccount, liabilityAccount, incomeAccount, expenseAccount });

            final Integer savingsProductId = SavingsProductHelper.createSavingsProduct(productHelper.build(), this.requestSpec,
                    this.responseSpec);
            Assertions.assertNotNull(savingsProductId, "Error creating savings product.");

            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2020");
            Assertions.assertNotNull(clientId, "Error creating client.");

            final LocalDate startDate = LocalDate.of(2021, 8, 12).minusDays(daysToTest);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            Assertions.assertNotNull(savingsAccountId, "Error applying for savings account.");

            this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
            this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);

            final HashMap<String, Object> savingsStatus = SavingsStatusChecker.getStatusOfSavings(this.requestSpec, this.responseSpec,
                    savingsAccountId);
            SavingsStatusChecker.verifySavingsIsActive(savingsStatus);

            this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, "10000", startDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            // --- ACT ---
            schedulerJobHelper.executeAndAwaitJob("Add Accrual Transactions For Savings");

            // --- ASSERT ---
            List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
            List<HashMap> accrualTransactions = new ArrayList<>();
            for (HashMap transaction : allTransactions) {
                Map<String, Object> type = (Map<String, Object>) transaction.get("transactionType");
                if (type != null && Boolean.TRUE.equals(type.get("accrual"))) {
                    accrualTransactions.add(transaction);
                }
            }
            Assertions.assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found.");

            long daysBetween = ChronoUnit.DAYS.between(startDate, LocalDate.of(2021, 8, 12));
            long actualNumberOfTransactions = accrualTransactions.size();

            Assertions.assertTrue(actualNumberOfTransactions >= daysBetween && actualNumberOfTransactions <= daysBetween + 1,
                    "For a period of " + daysBetween + " days, a close number of transactions was expected, but found "
                            + actualNumberOfTransactions);

            BigDecimal principal = new BigDecimal("10000");
            BigDecimal rate = new BigDecimal(interestRate).divide(new BigDecimal(100));
            BigDecimal daysInYear = new BigDecimal("365");

            BigDecimal expectedTotalAccrual = principal.multiply(rate).divide(daysInYear, 8, RoundingMode.HALF_EVEN)
                    .multiply(new BigDecimal(actualNumberOfTransactions)).setScale(2, RoundingMode.HALF_EVEN);

            BigDecimal actualTotalAccrual = savingsAccountHelper.getTotalAccrualAmount(savingsAccountId);

            Assertions.assertEquals(0, expectedTotalAccrual.compareTo(actualTotalAccrual),
                    "The total accrual (" + actualTotalAccrual + ") does not match the expected (" + expectedTotalAccrual + ")");
        });
    }

    @Test
    public void testAccrualsAreReversedAndRecalculatedAfterBackdatedTransaction() {
        runAt("12 August 2021", () -> {
            // --- ARRANGE ---
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final String interestRate = "10.0";
            final int daysToTest = 10;
            final int daysUntilTransaction = 5;

            final SavingsProductHelper productHelper = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily()
                    .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                    .withNominalAnnualInterestRate(new BigDecimal(interestRate))
                    .withAccountingRuleAsAccrualBased(new Account[] { assetAccount, liabilityAccount, incomeAccount, expenseAccount });

            final Integer savingsProductId = SavingsProductHelper.createSavingsProduct(productHelper.build(), this.requestSpec,
                    this.responseSpec);
            Assertions.assertNotNull(savingsProductId);

            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2020");
            Assertions.assertNotNull(clientId);

            final LocalDate today = LocalDate.of(2021, 8, 12);
            final LocalDate startDate = today.minusDays(daysToTest);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);

            final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductId,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            Assertions.assertNotNull(savingsAccountId);

            this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
            this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
            this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, "10000", startDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            // --- ACT ---
            schedulerJobHelper.executeAndAwaitJob("Add Accrual Transactions For Savings");

            final LocalDate backdatedTransactionDate = startDate.plusDays(daysUntilTransaction);
            final String backdatedTransactionDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US)
                    .format(backdatedTransactionDate);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsAccountId, "1000", backdatedTransactionDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            schedulerJobHelper.executeAndAwaitJob("Add Accrual Transactions For Savings");

            // --- ASSERT ---
            List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);

            Map<LocalDate, Map<String, Integer>> accrualsByDate = new HashMap<>();

            for (HashMap transaction : allTransactions) {
                Map<String, Object> type = (Map<String, Object>) transaction.get("transactionType");

                if (type != null && Boolean.TRUE.equals(type.get("accrual"))) {
                    List<Number> dateArray = (List<Number>) transaction.get("date");
                    LocalDate transactionDate = LocalDate.of(dateArray.get(0).intValue(), dateArray.get(1).intValue(),
                            dateArray.get(2).intValue());
                    boolean isReversed = Boolean.TRUE.equals(transaction.get("reversed"));

                    accrualsByDate.putIfAbsent(transactionDate, new HashMap<>(Map.of("TOTAL", 0, "REVERSED", 0)));

                    Map<String, Integer> counts = accrualsByDate.get(transactionDate);
                    counts.put("TOTAL", counts.get("TOTAL") + 1);
                    if (isReversed) {
                        counts.put("REVERSED", counts.get("REVERSED") + 1);
                    }
                }
            }

            for (Map.Entry<LocalDate, Map<String, Integer>> entry : accrualsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                Map<String, Integer> counts = entry.getValue();
                Integer total = counts.get("TOTAL");
                Integer reversed = counts.get("REVERSED");

                if (date.isBefore(backdatedTransactionDate)) {
                    Assertions.assertEquals(1, total, "There should be 1 accrual for the date " + date);
                    Assertions.assertEquals(0, reversed, "The accrual for the date " + date + " should not be reversed.");
                } else {
                    Assertions.assertEquals(2, total, "There should be 2 accruals (original and new) for the date " + date);
                    Assertions.assertEquals(1, reversed, "There should be 1 reversed accrual for the date " + date);
                }
            }
            Assertions.assertFalse(accrualsByDate.isEmpty(), "No accrual transactions were found to verify.");
            BigDecimal expectedDailyInterestOn9k = new BigDecimal("9000").multiply(new BigDecimal("0.10")).divide(new BigDecimal("365"), 4,
                    RoundingMode.HALF_EVEN);

            boolean newAccrualVerified = false;
            for (HashMap transaction : allTransactions) {
                Map<String, Object> type = (Map<String, Object>) transaction.get("transactionType");
                if (type != null && Boolean.TRUE.equals(type.get("accrual")) && !Boolean.TRUE.equals(transaction.get("reversed"))) {
                    List<Number> dateArray = (List<Number>) transaction.get("date");
                    LocalDate transactionDate = LocalDate.of(dateArray.get(0).intValue(), dateArray.get(1).intValue(),
                            dateArray.get(2).intValue());

                    if (!transactionDate.isBefore(backdatedTransactionDate)) {
                        BigDecimal actualAmount = new BigDecimal(transaction.get("amount").toString()).setScale(4, RoundingMode.HALF_EVEN);
                        Assertions.assertEquals(0, expectedDailyInterestOn9k.compareTo(actualAmount), "The new accrual amount ("
                                + actualAmount + ") does not match the expected (" + expectedDailyInterestOn9k + ")");
                        newAccrualVerified = true;
                    }
                }
            }
            Assertions.assertTrue(newAccrualVerified, "Could not verify the mathematical calculation of a new accrual.");
        });
    }
}
