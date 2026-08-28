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
import org.apache.fineract.client.models.PostSavingsAccountsSavingsAccountIdChargesRequest;
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
     * Fills every mapping from one account per type, so several mappings share an account. Only for tests that need a
     * valid accrual product; a test asserting which mapping was posted to must map each one to its own account.
     */
    public static PostSavingsProductsRequest withAccrualAccountingMappings(PostSavingsProductsRequest request, Account assetAccount,
            Account liabilityAccount, Account incomeAccount, Account expenseAccount) {
        return request//
                .savingsReferenceAccountId(accountId(assetAccount))//
                .overdraftPortfolioControlId(accountId(assetAccount))//
                .feesReceivableAccountId(accountId(assetAccount))//
                .penaltiesReceivableAccountId(accountId(assetAccount))//
                .savingsControlAccountId(accountId(liabilityAccount))//
                .transfersInSuspenseAccountId(accountId(liabilityAccount))//
                .interestPayableAccountId(accountId(liabilityAccount))//
                .interestOnSavingsAccountId(accountId(expenseAccount))//
                .writeOffAccountId(accountId(expenseAccount))//
                .incomeFromFeeAccountId(accountId(incomeAccount))//
                .incomeFromPenaltyAccountId(accountId(incomeAccount))//
                .incomeFromInterestId(accountId(incomeAccount));
    }

    public static PostSavingsProductsRequest withAccrualAccountingMappings(PostSavingsProductsRequest request, Account assetAccount,
            Account liabilityAccount, Account incomeAccount, Account expenseAccount, Account interestReceivableAccount) {
        return withAccrualAccountingMappings(request, assetAccount, liabilityAccount, incomeAccount, expenseAccount)
                .interestReceivableAccountId(accountId(interestReceivableAccount));
    }

    /** An update is validated against the same parameter set as a create, so it carries the whole product body. */
    public static PutSavingsProductsProductIdRequest withAccrualAccountingMappings(PutSavingsProductsProductIdRequest request,
            Account assetAccount, Account liabilityAccount, Account incomeAccount, Account expenseAccount,
            Account interestReceivableAccount) {
        return request//
                .savingsReferenceAccountId(accountId(assetAccount))//
                .overdraftPortfolioControlId(accountId(assetAccount))//
                .feesReceivableAccountId(accountId(assetAccount))//
                .penaltiesReceivableAccountId(accountId(assetAccount))//
                .savingsControlAccountId(accountId(liabilityAccount))//
                .transfersInSuspenseAccountId(accountId(liabilityAccount))//
                .interestPayableAccountId(accountId(liabilityAccount))//
                .interestOnSavingsAccountId(accountId(expenseAccount))//
                .writeOffAccountId(accountId(expenseAccount))//
                .incomeFromFeeAccountId(accountId(incomeAccount))//
                .incomeFromPenaltyAccountId(accountId(incomeAccount))//
                .incomeFromInterestId(accountId(incomeAccount))//
                .interestReceivableAccountId(accountId(interestReceivableAccount));
    }

    public static Long accountId(Account account) {
        return account.getAccountID().longValue();
    }

    public static ChargeRequest savingsWithdrawalFeeCharge() {
        return new ChargeRequest()//
                .active(true)//
                .name(Utils.uniqueRandomStringGenerator("Charge_Savings_", 6))//
                .currencyCode(SavingsTestData.CURRENCY_CODE)//
                .amount(SavingsTestData.DEFAULT_CHARGE_AMOUNT)//
                .chargeAppliesTo(SavingsTestData.ChargeAppliesTo.SAVINGS)//
                .chargeTimeType(SavingsTestData.ChargeTimeType.WITHDRAWAL_FEE)//
                .chargeCalculationType(SavingsTestData.ChargeCalculationType.FLAT)//
                .locale(SavingsTestData.LOCALE);
    }

    public static PostSavingsAccountsSavingsAccountIdChargesRequest savingsAccountCharge(Long chargeId, Float amount) {
        return new PostSavingsAccountsSavingsAccountIdChargesRequest()//
                .chargeId(chargeId)//
                .amount(amount)//
                .locale(SavingsTestData.LOCALE);
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
