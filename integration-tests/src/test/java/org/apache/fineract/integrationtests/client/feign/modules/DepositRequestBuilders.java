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
import java.util.List;
import java.util.Set;
import org.apache.fineract.client.models.PostFixedDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsChartSlabs;
import org.apache.fineract.client.models.PostFixedDepositProductsCharts;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest;
import org.apache.fineract.client.models.PostRecurringDepositAccountsRequest;
import org.apache.fineract.client.models.PostRecurringDepositProductsChartSlabs;
import org.apache.fineract.client.models.PostRecurringDepositProductsCharts;
import org.apache.fineract.client.models.PostRecurringDepositProductsRequest;
import org.apache.fineract.client.models.PutFixedDepositAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutRecurringDepositAccountsAccountIdRequest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

/**
 * Typed request builders for the deposit endpoints. The defaults are those of the RestAssured
 * {@code FixedDepositProductHelper}, {@code FixedDepositAccountHelper} and their recurring counterparts, so a migrated
 * test sends the same body it used to.
 */
public final class DepositRequestBuilders {

    private DepositRequestBuilders() {}

    public static PostFixedDepositProductsRequest fixedDepositProduct() {
        return new PostFixedDepositProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("FIXED_DEPOSIT_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description(Utils.randomStringGenerator("", 20))//
                .currencyCode(SavingsTestData.CURRENCY_CODE)//
                .digitsAfterDecimal(4)//
                .inMultiplesOf(100)//
                .locale(SavingsTestData.LOCALE)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.MONTHLY)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .minDepositTerm(6)//
                .minDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .maxDepositTerm(10)//
                .maxDepositTermTypeId(SavingsTestData.PeriodFrequencyType.YEARS)//
                .inMultiplesOfDepositTerm(2)//
                .inMultiplesOfDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .depositAmount(DepositTestData.DEPOSIT_AMOUNT)//
                .preClosurePenalApplicable(true)//
                .preClosurePenalInterest(2.0)//
                .preClosurePenalInterestOnTypeId(DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)//
                .withHoldTax(false);
    }

    public static PostRecurringDepositProductsRequest recurringDepositProduct() {
        return new PostRecurringDepositProductsRequest()//
                .name(Utils.uniqueRandomStringGenerator("RECURRING_DEPOSIT_PRODUCT_", 6))//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .description(Utils.randomStringGenerator("", 20))//
                .currencyCode(SavingsTestData.CURRENCY_CODE)//
                .digitsAfterDecimal(4)//
                .inMultiplesOf(100)//
                .locale(SavingsTestData.LOCALE)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.MONTHLY)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .accountingRule(SavingsTestData.AccountingRule.NONE)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .minDepositTerm(6)//
                .minDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .maxDepositTerm(10)//
                .maxDepositTermTypeId(SavingsTestData.PeriodFrequencyType.YEARS)//
                .inMultiplesOfDepositTerm(2)//
                .inMultiplesOfDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .preClosurePenalApplicable(true)//
                .preClosurePenalInterest(2.0)//
                .preClosurePenalInterestOnTypeId(DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)//
                .isMandatoryDeposit(false)//
                .recurringFrequency(1)//
                .recurringFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .depositAmount(DepositTestData.DEPOSIT_AMOUNT)//
                .minDepositAmount(new BigDecimal("100"))//
                .maxDepositAmount(new BigDecimal("1000000"))//
                .withHoldTax(false);
    }

    public static PostFixedDepositProductsRequest withCashBasedAccounting(PostFixedDepositProductsRequest request, Account assetAccount,
            Account liabilityAccount, Account incomeAccount, Account expenseAccount) {
        return request//
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .savingsReferenceAccountId(accountId(assetAccount))//
                .savingsControlAccountId(accountId(liabilityAccount))//
                .transfersInSuspenseAccountId(accountId(liabilityAccount))//
                .interestOnSavingsAccountId(accountId(expenseAccount))//
                .incomeFromFeeAccountId(accountId(incomeAccount))//
                .incomeFromPenaltyAccountId(accountId(incomeAccount));
    }

    public static PostFixedDepositProductsRequest withAccrualAccounting(PostFixedDepositProductsRequest request, Account assetAccount,
            Account liabilityAccount, Account incomeAccount, Account expenseAccount) {
        return withCashBasedAccounting(request, assetAccount, liabilityAccount, incomeAccount, expenseAccount)//
                .accountingRule(SavingsTestData.AccountingRule.ACCRUAL_PERIODIC)//
                .feesReceivableAccountId(accountId(assetAccount))//
                .penaltiesReceivableAccountId(accountId(assetAccount))//
                .interestPayableAccountId(accountId(liabilityAccount));
    }

    public static PostRecurringDepositProductsRequest withCashBasedAccounting(PostRecurringDepositProductsRequest request,
            Account assetAccount, Account liabilityAccount, Account incomeAccount, Account expenseAccount) {
        return request//
                .accountingRule(SavingsTestData.AccountingRule.CASH_BASED)//
                .savingsReferenceAccountId(accountId(assetAccount))//
                .savingsControlAccountId(accountId(liabilityAccount))//
                .transfersInSuspenseAccountId(accountId(liabilityAccount))//
                .interestOnSavingsAccountId(accountId(expenseAccount))//
                .incomeFromFeeAccountId(accountId(incomeAccount))//
                .incomeFromPenaltyAccountId(accountId(incomeAccount));
    }

    public static PostFixedDepositProductsRequest withChart(PostFixedDepositProductsRequest request, String validFrom, String validTo,
            List<PostFixedDepositProductsChartSlabs> slabs) {
        return withChart(request, validFrom, validTo, slabs, false);
    }

    public static PostFixedDepositProductsRequest withChart(PostFixedDepositProductsRequest request, String validFrom, String validTo,
            List<PostFixedDepositProductsChartSlabs> slabs, boolean isPrimaryGroupingByAmount) {
        return request.charts(Set.of(new PostFixedDepositProductsCharts()//
                .fromDate(validFrom)//
                .endDate(validTo)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .isPrimaryGroupingByAmount(isPrimaryGroupingByAmount)//
                .chartSlabs(Set.copyOf(slabs))));
    }

    public static PostRecurringDepositProductsRequest withChart(PostRecurringDepositProductsRequest request, String validFrom,
            String validTo, List<PostRecurringDepositProductsChartSlabs> slabs) {
        return withChart(request, validFrom, validTo, slabs, false);
    }

    public static PostRecurringDepositProductsRequest withChart(PostRecurringDepositProductsRequest request, String validFrom,
            String validTo, List<PostRecurringDepositProductsChartSlabs> slabs, boolean isPrimaryGroupingByAmount) {
        return request.charts(Set.of(new PostRecurringDepositProductsCharts()//
                .fromDate(validFrom)//
                .endDate(validTo)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .locale(SavingsTestData.LOCALE)//
                .isPrimaryGroupingByAmount(isPrimaryGroupingByAmount)//
                .chartSlabs(Set.copyOf(slabs))));
    }

    public static PostFixedDepositAccountsRequest fixedDepositAccount(Long clientId, Long productId, String submittedOnDate,
            int preClosurePenalInterestOnTypeId) {
        return new PostFixedDepositAccountsRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .monthDayFormat(DepositTestData.MONTH_DAY_FORMAT)//
                .submittedOnDate(submittedOnDate)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.MONTHLY)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .minDepositTerm(6)//
                .minDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .maxDepositTerm(10)//
                .maxDepositTermTypeId(SavingsTestData.PeriodFrequencyType.YEARS)//
                .inMultiplesOfDepositTerm(2)//
                .inMultiplesOfDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .preClosurePenalApplicable(true)//
                .preClosurePenalInterest(new BigDecimal("2"))//
                .preClosurePenalInterestOnTypeId(preClosurePenalInterestOnTypeId)//
                .depositAmount(DepositTestData.DEPOSIT_AMOUNT)//
                .depositPeriod(14)//
                .depositPeriodFrequencyId((long) SavingsTestData.PeriodFrequencyType.MONTHS)//
                .transferInterestToSavings(false);
    }

    public static PostRecurringDepositAccountsRequest recurringDepositAccount(Long clientId, Long productId, String submittedOnDate,
            String expectedFirstDepositOnDate, int preClosurePenalInterestOnTypeId) {
        return new PostRecurringDepositAccountsRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .monthDayFormat(DepositTestData.MONTH_DAY_FORMAT)//
                .submittedOnDate(submittedOnDate)//
                .interestCompoundingPeriodType(SavingsTestData.InterestCompoundingPeriodType.MONTHLY)//
                .interestPostingPeriodType(SavingsTestData.InterestPostingPeriodType.MONTHLY)//
                .interestCalculationType(SavingsTestData.InterestCalculationType.DAILY_BALANCE)//
                .interestCalculationDaysInYearType(SavingsTestData.InterestCalculationDaysInYearType.DAYS_365)//
                .lockinPeriodFrequency(1)//
                .lockinPeriodFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .minDepositTerm(6)//
                .minDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .maxDepositTerm(10)//
                .maxDepositTermTypeId(SavingsTestData.PeriodFrequencyType.YEARS)//
                .inMultiplesOfDepositTerm(2)//
                .inMultiplesOfDepositTermTypeId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .preClosurePenalApplicable(true)//
                .preClosurePenalInterest(new BigDecimal("2"))//
                .preClosurePenalInterestOnTypeId(preClosurePenalInterestOnTypeId)//
                .depositAmount(DepositTestData.RECURRING_DEPOSIT_AMOUNT)//
                .depositPeriod(14)//
                .depositPeriodFrequencyId(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .recurringFrequency(1)//
                .recurringFrequencyType(SavingsTestData.PeriodFrequencyType.MONTHS)//
                .mandatoryRecommendedDepositAmount(DepositTestData.RECURRING_DEPOSIT_AMOUNT)//
                .expectedFirstDepositOnDate(expectedFirstDepositOnDate)//
                .isCalendarInherited(false);
    }

    /** An update is validated against the same parameter set as a create, so it carries the whole account body. */
    public static PutFixedDepositAccountsAccountIdRequest asUpdate(PostFixedDepositAccountsRequest request) {
        return new PutFixedDepositAccountsAccountIdRequest()//
                .clientId(request.getClientId())//
                .productId(request.getProductId())//
                .locale(request.getLocale())//
                .dateFormat(request.getDateFormat())//
                .monthDayFormat(request.getMonthDayFormat())//
                .submittedOnDate(request.getSubmittedOnDate())//
                .depositAmount(request.getDepositAmount())//
                .depositPeriod(request.getDepositPeriod())//
                .depositPeriodFrequencyId(request.getDepositPeriodFrequencyId())//
                .interestCompoundingPeriodType(request.getInterestCompoundingPeriodType())//
                .interestPostingPeriodType(request.getInterestPostingPeriodType())//
                .interestCalculationType(request.getInterestCalculationType())//
                .interestCalculationDaysInYearType(request.getInterestCalculationDaysInYearType())//
                .lockinPeriodFrequency(request.getLockinPeriodFrequency())//
                .lockinPeriodFrequencyType(request.getLockinPeriodFrequencyType())//
                .preClosurePenalApplicable(request.getPreClosurePenalApplicable())//
                .preClosurePenalInterest(request.getPreClosurePenalInterest())//
                .preClosurePenalInterestOnTypeId(request.getPreClosurePenalInterestOnTypeId())//
                .minDepositTerm(request.getMinDepositTerm())//
                .minDepositTermTypeId(request.getMinDepositTermTypeId())//
                .maxDepositTerm(request.getMaxDepositTerm())//
                .maxDepositTermTypeId(request.getMaxDepositTermTypeId())//
                .inMultiplesOfDepositTerm(request.getInMultiplesOfDepositTerm())//
                .inMultiplesOfDepositTermTypeId(request.getInMultiplesOfDepositTermTypeId())//
                .linkAccountId(request.getLinkAccountId())//
                .transferInterestToSavings(request.getTransferInterestToSavings())//
                .maturityInstructionId(request.getMaturityInstructionId());
    }

    /** An update is validated against the same parameter set as a create, so it carries the whole account body. */
    public static PutRecurringDepositAccountsAccountIdRequest asUpdate(PostRecurringDepositAccountsRequest request) {
        return new PutRecurringDepositAccountsAccountIdRequest()//
                .clientId(request.getClientId())//
                .productId(request.getProductId())//
                .locale(request.getLocale())//
                .dateFormat(request.getDateFormat())//
                .monthDayFormat(request.getMonthDayFormat())//
                .submittedOnDate(request.getSubmittedOnDate())//
                .depositAmount(request.getDepositAmount())//
                .depositPeriod(request.getDepositPeriod())//
                .depositPeriodFrequencyId(request.getDepositPeriodFrequencyId())//
                .interestCompoundingPeriodType(request.getInterestCompoundingPeriodType())//
                .interestPostingPeriodType(request.getInterestPostingPeriodType())//
                .interestCalculationType(request.getInterestCalculationType())//
                .interestCalculationDaysInYearType(request.getInterestCalculationDaysInYearType())//
                .lockinPeriodFrequency(request.getLockinPeriodFrequency())//
                .lockinPeriodFrequencyType(request.getLockinPeriodFrequencyType())//
                .preClosurePenalApplicable(request.getPreClosurePenalApplicable())//
                .preClosurePenalInterest(request.getPreClosurePenalInterest())//
                .preClosurePenalInterestOnTypeId(request.getPreClosurePenalInterestOnTypeId())//
                .minDepositTerm(request.getMinDepositTerm())//
                .minDepositTermTypeId(request.getMinDepositTermTypeId())//
                .maxDepositTerm(request.getMaxDepositTerm())//
                .maxDepositTermTypeId(request.getMaxDepositTermTypeId())//
                .inMultiplesOfDepositTerm(request.getInMultiplesOfDepositTerm())//
                .inMultiplesOfDepositTermTypeId(request.getInMultiplesOfDepositTermTypeId())//
                .recurringFrequency(request.getRecurringFrequency())//
                .recurringFrequencyType(request.getRecurringFrequencyType())//
                .mandatoryRecommendedDepositAmount(request.getMandatoryRecommendedDepositAmount())//
                .expectedFirstDepositOnDate(request.getExpectedFirstDepositOnDate())//
                .isCalendarInherited(request.getIsCalendarInherited());
    }

    public static PostFixedDepositAccountsAccountIdRequest approveFixedDeposit(String approvedOnDate) {
        return fixedDepositCommand().approvedOnDate(approvedOnDate);
    }

    public static PostFixedDepositAccountsAccountIdRequest activateFixedDeposit(String activatedOnDate) {
        return fixedDepositCommand().activatedOnDate(activatedOnDate);
    }

    public static PostFixedDepositAccountsAccountIdRequest rejectFixedDeposit(String rejectedOnDate) {
        return fixedDepositCommand().rejectedOnDate(rejectedOnDate);
    }

    public static PostFixedDepositAccountsAccountIdRequest withdrawFixedDeposit(String withdrawnOnDate) {
        return fixedDepositCommand().withdrawnOnDate(withdrawnOnDate);
    }

    public static PostFixedDepositAccountsAccountIdRequest calculatePrematureAmount(String closedOnDate) {
        return fixedDepositCommand().closedOnDate(closedOnDate);
    }

    public static PostFixedDepositAccountsAccountIdRequest prematureCloseFixedDeposit(String closedOnDate, int onAccountClosureId,
            Long toSavingsAccountId) {
        PostFixedDepositAccountsAccountIdRequest request = fixedDepositCommand()//
                .closedOnDate(closedOnDate)//
                .onAccountClosureId(onAccountClosureId);
        return toSavingsAccountId == null ? request
                : request.toSavingsAccountId(toSavingsAccountId).transferDescription("Transferring To Savings Account");
    }

    public static PostFixedDepositAccountsAccountIdRequest fixedDepositCommand() {
        return new PostFixedDepositAccountsAccountIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN);
    }

    public static PostRecurringDepositAccountsAccountIdRequest approveRecurringDeposit(String approvedOnDate) {
        return recurringDepositCommand().approvedOnDate(approvedOnDate);
    }

    public static PostRecurringDepositAccountsAccountIdRequest activateRecurringDeposit(String activatedOnDate) {
        return recurringDepositCommand().activatedOnDate(activatedOnDate);
    }

    public static PostRecurringDepositAccountsAccountIdRequest rejectRecurringDeposit(String rejectedOnDate) {
        return recurringDepositCommand().rejectedOnDate(rejectedOnDate);
    }

    public static PostRecurringDepositAccountsAccountIdRequest withdrawRecurringDeposit(String withdrawnOnDate) {
        return recurringDepositCommand().withdrawnOnDate(withdrawnOnDate);
    }

    public static PostRecurringDepositAccountsAccountIdRequest calculateRecurringPrematureAmount(String closedOnDate) {
        return recurringDepositCommand().closedOnDate(closedOnDate);
    }

    public static PostRecurringDepositAccountsAccountIdRequest prematureCloseRecurringDeposit(String closedOnDate, int onAccountClosureId,
            Long toSavingsAccountId) {
        PostRecurringDepositAccountsAccountIdRequest request = recurringDepositCommand()//
                .closedOnDate(closedOnDate)//
                .onAccountClosureId(onAccountClosureId);
        return toSavingsAccountId == null ? request
                : request.toSavingsAccountId(toSavingsAccountId).transferDescription("Transferring To Savings Account");
    }

    public static PostRecurringDepositAccountsAccountIdRequest recurringDepositCommand() {
        return new PostRecurringDepositAccountsAccountIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN);
    }

    public static PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest depositTransaction(String transactionDate,
            BigDecimal transactionAmount) {
        return new PostRecurringDepositAccountsRecurringDepositAccountIdTransactionsRequest()//
                .locale(SavingsTestData.LOCALE)//
                .dateFormat(SavingsTestData.DATETIME_PATTERN)//
                .transactionDate(transactionDate)//
                .transactionAmount(transactionAmount.doubleValue());
    }

    public static Long accountId(Account account) {
        return account.getAccountID().longValue();
    }
}
