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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsAccrualAccountingIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccrualAccountingIntegrationTest.class);
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
    public void testPositiveAccrualPostsCorrectJournalEntries() {
        // --- ARRANGE ---
        LOG.info("------------------------- INITIATING POSITIVE ACCRUAL ACCOUNTING TEST -------------------------");
        final int daysToSubtract = 10;
        final String amount = "10000";

        final Account savingsReferenceAccount = this.accountHelper.createAssetAccount("Savings Reference");
        final Account interestOnSavingsAccount = this.accountHelper.createExpenseAccount("Interest on Savings (Expense)");
        final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");
        final Account interestPayableAccount = this.accountHelper.createLiabilityAccount("Interest Payable (Liability)");
        final Account incomeFromFeesAccount = this.accountHelper.createIncomeAccount("Income from Fees");
        final Account[] accountList = { savingsReferenceAccount, savingsControlAccount, interestOnSavingsAccount, interestPayableAccount,
                incomeFromFeesAccount };

        final SavingsProductHelper productHelper = new SavingsProductHelper().withNominalAnnualInterestRate(new BigDecimal("10.0"))
                .withAccountingRuleAsAccrualBased(accountList)
                .withSavingsReferenceAccountId(savingsReferenceAccount.getAccountID().toString())
                .withSavingsControlAccountId(savingsControlAccount.getAccountID().toString())
                .withInterestOnSavingsAccountId(interestOnSavingsAccount.getAccountID().toString())
                .withInterestPayableAccountId(interestPayableAccount.getAccountID().toString())
                .withIncomeFromFeeAccountId(incomeFromFeesAccount.getAccountID().toString());

        final Integer savingsProductId = SavingsProductHelper.createSavingsProduct(productHelper.build(), this.requestSpec,
                this.responseSpec);
        Assertions.assertNotNull(savingsProductId, "Failed to create savings product.");

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2020");
        final LocalDate startDate = LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(daysToSubtract);
        final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
        final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductId,
                SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
        this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
        this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
        this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, amount, startDateString, CommonConstants.RESPONSE_RESOURCE_ID);

        // --- ACT ---
        schedulerJobHelper.executeAndAwaitJob("Add Accrual Transactions For Savings");

        // --- ASSERT ---
        List<HashMap> accrualTransactions = getAccrualTransactions(savingsAccountId);
        Assertions.assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found.");

        Number firstTransactionIdNumber = (Number) accrualTransactions.get(0).get("id");
        ArrayList<HashMap> journalEntries = journalEntryHelper.getJournalEntriesByTransactionId("S" + firstTransactionIdNumber.intValue());
        Assertions.assertFalse(journalEntries.isEmpty(), "No journal entries found for positive accrual.");

        boolean debitFound = false;
        boolean creditFound = false;
        for (Map<String, Object> entry : journalEntries) {
            String entryType = (String) ((HashMap) entry.get("entryType")).get("value");
            Integer accountId = ((Number) entry.get("glAccountId")).intValue();
            if ("DEBIT".equals(entryType) && accountId.equals(interestOnSavingsAccount.getAccountID())) {
                debitFound = true;
            }
            if ("CREDIT".equals(entryType) && accountId.equals(interestPayableAccount.getAccountID())) {
                creditFound = true;
            }
        }

        Assertions.assertTrue(debitFound, "DEBIT to Interest on Savings (Expense) Account not found for positive accrual.");
        Assertions.assertTrue(creditFound, "CREDIT to Interest Payable (Liability) Account not found for positive accrual.");

        BigDecimal interest = getCalculateAccrualsForDay(productHelper, amount);

        for (HashMap accrual : accrualTransactions) {
            BigDecimal amountAccrualTransaccion = BigDecimal.valueOf((Double) accrual.get("amount"));
            Assertions.assertEquals(interest, amountAccrualTransaccion);
        }
        LOG.info("VALIDATE AMOUNT AND ACCOUNT");

    }

    @Test
    public void testNegativeAccrualPostsCorrectJournalEntries() {
        // --- ARRANGE ---
        LOG.info("------------------------- INITIATING NEGATIVE ACCRUAL (OVERDRAFT) ACCOUNTING TEST -------------------------");
        final int daysToSubtract = 10;
        final String amount = "10000";

        final Account savingsReferenceAccount = this.accountHelper.createAssetAccount("Savings Reference");
        final Account overdraftPortfolioControl = this.accountHelper.createAssetAccount("Overdraft Portfolio");
        final Account interestReceivableAccount = this.accountHelper.createAssetAccount("Interest Receivable (Asset)");
        final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");
        final Account interestPayableAccount = this.accountHelper.createLiabilityAccount("Interest Payable");
        final Account overdraftInterestIncomeAccount = this.accountHelper.createIncomeAccount("Overdraft Interest Income");
        final Account expenseAccount = this.accountHelper.createExpenseAccount("Interest on Savings (Expense)");

        final Account[] accountList = { savingsReferenceAccount, savingsControlAccount, expenseAccount, overdraftInterestIncomeAccount };

        final String overdraftLimit = "10000";
        final String overdraftInterestRate = "21.0";
        final SavingsProductHelper productHelper = new SavingsProductHelper()
                .withNominalAnnualInterestRate(new BigDecimal(overdraftInterestRate)).withAccountingRuleAsAccrualBased(accountList)
                .withOverDraftRate(overdraftLimit, overdraftInterestRate)
                .withSavingsReferenceAccountId(savingsReferenceAccount.getAccountID().toString())
                .withSavingsControlAccountId(savingsControlAccount.getAccountID().toString())
                .withInterestReceivableAccountId(interestReceivableAccount.getAccountID().toString())
                .withIncomeFromInterestId(overdraftInterestIncomeAccount.getAccountID().toString())
                .withInterestPayableAccountId(interestPayableAccount.getAccountID().toString())
                .withInterestOnSavingsAccountId(expenseAccount.getAccountID().toString())
                .withOverdraftPortfolioControlId(overdraftPortfolioControl.getAccountID().toString());

        final Integer savingsProductId = SavingsProductHelper.createSavingsProduct(productHelper.build(), this.requestSpec,
                this.responseSpec);
        Assertions.assertNotNull(savingsProductId, "Savings product with overdraft creation failed.");

        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2020");
        final LocalDate startDate = LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(daysToSubtract);
        final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
        final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductId,
                SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
        this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
        this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsAccountId, "10000", startDateString,
                CommonConstants.RESPONSE_RESOURCE_ID);

        // --- ACT ---
        schedulerJobHelper.executeAndAwaitJob("Add Accrual Transactions For Savings");

        // --- ASSERT ---
        List<HashMap> accrualTransactions = getAccrualTransactions(savingsAccountId);
        Assertions.assertFalse(accrualTransactions.isEmpty(), "No accrual transactions were found for overdraft.");

        Number firstTransactionIdNumber = (Number) accrualTransactions.get(0).get("id");
        ArrayList<HashMap> journalEntries = journalEntryHelper.getJournalEntriesByTransactionId("S" + firstTransactionIdNumber.intValue());
        Assertions.assertFalse(journalEntries.isEmpty(), "No journal entries found for negative accrual.");

        boolean debitFound = false;
        boolean creditFound = false;
        for (Map<String, Object> entry : journalEntries) {
            String entryType = (String) ((HashMap) entry.get("entryType")).get("value");
            Integer accountId = ((Number) entry.get("glAccountId")).intValue();
            if ("DEBIT".equals(entryType) && accountId.equals(interestReceivableAccount.getAccountID())) {
                debitFound = true;
            }
            if ("CREDIT".equals(entryType) && accountId.equals(overdraftInterestIncomeAccount.getAccountID())) {
                creditFound = true;
            }
        }

        Assertions.assertTrue(debitFound, "DEBIT to Interest Receivable (Asset) Account not found for negative accrual.");
        Assertions.assertTrue(creditFound, "CREDIT to Overdraft Interest Income Account not found for negative accrual.");

        BigDecimal interest = getCalculateAccrualsForDay(productHelper, amount);

        for (HashMap accrual : accrualTransactions) {
            BigDecimal amountAccrualTransaccion = BigDecimal.valueOf((Double) accrual.get("amount"));
            Assertions.assertEquals(interest, amountAccrualTransaccion);
        }
        LOG.info("VALIDATE AMOUNT AND ACCOUNT");
    }

    private List<HashMap> getAccrualTransactions(Integer savingsAccountId) {
        List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
        List<HashMap> accrualTransactions = new ArrayList<>();
        for (HashMap transaction : allTransactions) {
            Map<String, Object> type = (Map<String, Object>) transaction.get("transactionType");
            if (type != null && Boolean.TRUE.equals(type.get("accrual"))) {
                accrualTransactions.add(transaction);
            }
        }
        return accrualTransactions;
    }

    private BigDecimal getCalculateAccrualsForDay(SavingsProductHelper productHelper, String amount) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal interestRateAsFraction = productHelper.getNominalAnnualInterestRate().divide(new BigDecimal(100.00));
        BigDecimal realBalanceForInterestCalculation = new BigDecimal(amount);

        final BigDecimal multiplicand = BigDecimal.ONE.divide(productHelper.getInterestCalculationDaysInYearType(), MathContext.DECIMAL64);
        final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        final BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(1), MathContext.DECIMAL64);
        interest = realBalanceForInterestCalculation.multiply(periodicInterestRate, MathContext.DECIMAL64)
                .setScale(productHelper.getDecimalCurrency(), RoundingMode.HALF_EVEN);

        return interest;
    }
}
