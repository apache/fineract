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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutSavingsProductsProductIdRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

public final class SavingsRequestBuilders {

    private SavingsRequestBuilders() {}

    public static PostSavingsProductsRequest defaultSavingsProduct() {
        return new PostSavingsProductsRequest()//
                .name("Savings Product " + System.currentTimeMillis())//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description("Test savings product")//
                .currencyCode("USD")//
                .digitsAfterDecimal(4)//
                .inMultiplesOf(0)//
                .nominalAnnualInterestRate(10.0)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.DAILY)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .locale(SavingsTestData.LOCALE);
    }

    /** Overdraft, enforced minimum balance and withholding tax are left off so they cannot skew interest figures. */
    public static PostSavingsProductsRequest savingsProduct(int interestCompoundingPeriodType, int interestPostingPeriodType,
            int interestCalculationType) {
        return defaultSavingsProduct()//
                .interestCompoundingPeriodType(interestCompoundingPeriodType)//
                .interestPostingPeriodType(interestPostingPeriodType)//
                .interestCalculationType(interestCalculationType)//
                .withdrawalFeeForTransfers(true)//
                .allowOverdraft(false)//
                .enforceMinRequiredBalance(false)//
                .withHoldTax(false);
    }

    /**
     * Applies the GL account mappings an accrual-based savings product needs, in place, and returns the same request so
     * it can be chained.
     * <p>
     * The interest receivable account is left unmapped here; use
     * {@link #withAccrualAccountingMappings(PostSavingsProductsRequest, Account, Account, Account, Account, Account)}
     * to point it at a specific account.
     */
    public static Map<String, Object> accrualAccountingMappings(Account assetAccount, Account liabilityAccount, Account incomeAccount,
            Account expenseAccount) {
        Map<String, Object> mappings = new HashMap<>();
        String assetAccountId = accountId(assetAccount);
        mappings.put(SavingsTestData.RawProductField.SAVINGS_REFERENCE_ACCOUNT_ID, assetAccountId);
        mappings.put(SavingsTestData.RawProductField.OVERDRAFT_PORTFOLIO_CONTROL_ID, assetAccountId);
        mappings.put(SavingsTestData.RawProductField.FEES_RECEIVABLE_ACCOUNT_ID, assetAccountId);
        mappings.put(SavingsTestData.RawProductField.PENALTIES_RECEIVABLE_ACCOUNT_ID, assetAccountId);

        String liabilityAccountId = accountId(liabilityAccount);
        mappings.put(SavingsTestData.RawProductField.SAVINGS_CONTROL_ACCOUNT_ID, liabilityAccountId);
        mappings.put(SavingsTestData.RawProductField.TRANSFERS_IN_SUSPENSE_ACCOUNT_ID, liabilityAccountId);
        mappings.put(SavingsTestData.RawProductField.INTEREST_PAYABLE_ACCOUNT_ID, liabilityAccountId);

        String expenseAccountId = accountId(expenseAccount);
        mappings.put(SavingsTestData.RawProductField.INTEREST_ON_SAVINGS_ACCOUNT_ID, expenseAccountId);
        mappings.put(SavingsTestData.RawProductField.WRITE_OFF_ACCOUNT_ID, expenseAccountId);

        String incomeAccountId = accountId(incomeAccount);
        mappings.put(SavingsTestData.RawProductField.INCOME_FROM_FEE_ACCOUNT_ID, incomeAccountId);
        mappings.put(SavingsTestData.RawProductField.INCOME_FROM_PENALTY_ACCOUNT_ID, incomeAccountId);
        mappings.put(SavingsTestData.RawProductField.INCOME_FROM_INTEREST_ID, incomeAccountId);
        return mappings;
    }

    public static Map<String, Object> accrualAccountingMappings(Account assetAccount, Account liabilityAccount, Account incomeAccount,
            Account expenseAccount, Account interestReceivableAccount) {
        Map<String, Object> mappings = accrualAccountingMappings(assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        mappings.put(SavingsTestData.RawProductField.INTEREST_RECEIVABLE_ACCOUNT_ID, accountId(interestReceivableAccount));
        return mappings;
    }

    /**
     * The raw product path sends the account ids as strings, which is the shape the server's locale-aware parsing
     * expects and the shape the RestAssured product builder used.
     */
    public static String accountId(Account account) {
        return String.valueOf(account.getAccountID());
    }

    public static PostSavingsAccountsRequest submitSavingsApplication(Long clientId, Long productId, String submittedOnDate) {
        return new PostSavingsAccountsRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .submittedOnDate(submittedOnDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsAccountIdRequest approveSavings(String approvedOnDate) {
        return new PostSavingsAccountsAccountIdRequest()//
                .approvedOnDate(approvedOnDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsAccountIdRequest activateSavings(String activatedOnDate) {
        return new PostSavingsAccountsAccountIdRequest()//
                .activatedOnDate(activatedOnDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsAccountIdRequest closeSavings(String closedOnDate, boolean withdrawBalance) {
        return new PostSavingsAccountsAccountIdRequest()//
                .closedOnDate(closedOnDate)//
                .withdrawBalance(withdrawBalance)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsAccountIdRequest rejectSavings(String rejectedOnDate) {
        return new PostSavingsAccountsAccountIdRequest()//
                .rejectedOnDate(rejectedOnDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsAccountIdRequest withdrawnByApplicant(String withdrawnOnDate) {
        return new PostSavingsAccountsAccountIdRequest()//
                .withdrawnOnDate(withdrawnOnDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountTransactionsRequest deposit(String amount, String transactionDate) {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionAmount(new BigDecimal(amount))//
                .transactionDate(transactionDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .paymentTypeId(1);
    }

    public static PostSavingsAccountTransactionsRequest withdrawal(String amount, String transactionDate) {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionAmount(new BigDecimal(amount))//
                .transactionDate(transactionDate)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .paymentTypeId(1);
    }

    public static PostSavingsAccountTransactionsRequest postInterestAsOn(String transactionDate) {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionDate(transactionDate)//
                .postInterestManualOrAutomatic(true)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }

    /** The server rejects a hold without a reason, and accepts no payment type on one. */
    public static PostSavingsAccountTransactionsRequest holdAmount(String amount, String transactionDate, String reasonForBlock) {
        return new PostSavingsAccountTransactionsRequest()//
                .transactionAmount(new BigDecimal(amount))//
                .transactionDate(transactionDate)//
                .reasonForBlock(reasonForBlock)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE);
    }
}
