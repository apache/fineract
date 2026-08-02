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
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.integrationtests.common.Utils;

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
