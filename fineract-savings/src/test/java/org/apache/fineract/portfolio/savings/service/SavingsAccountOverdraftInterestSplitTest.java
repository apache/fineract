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
package org.apache.fineract.portfolio.savings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * The overdraft interest split (overdraft allowed and an interest receivable GL account mapped) must include every
 * transaction whose balance falls in a posting period, not only those dated in the period's first calendar month.
 */
class SavingsAccountOverdraftInterestSplitTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    // same MathContext the Post Interest For Savings job uses
    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_EVEN);
    private static final CurrencyData CURRENCY = new MonetaryCurrency("USD", 2, null).toData();

    private static final LocalDate ACTIVATION_DATE = LocalDate.of(2025, 1, 1);

    private final SavingsAccountInterestPostingServiceImpl service = new SavingsAccountInterestPostingServiceImpl(
            new SavingsHelper(mock(AccountTransfersReadPlatformService.class)));

    @BeforeAll
    static void setUp() {
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(MATH_CONTEXT);
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void tearDown() {
        MONEY_HELPER.close();
    }

    @Test
    void overdraftSplitQuarterlyPostingIncludesTransactionsAfterFirstMonthOfPeriod() {
        final LocalDate businessDate = LocalDate.of(2025, 4, 2);
        final LocalDate quarterEnd = LocalDate.of(2025, 3, 31);

        final BigDecimal withoutSplit = interestFor(accountWithTwoDeposits(false, false), businessDate, ACTIVATION_DATE, quarterEnd);
        final BigDecimal withSplit = interestFor(accountWithTwoDeposits(true, true), businessDate, ACTIVATION_DATE, quarterEnd);

        // 10,000 for 45 days (Jan 1 - Feb 14), then 20,000 plus the interest so far for 45 days (Feb 15 - Mar 31),
        // compounded daily at 10% / 365
        final BigDecimal dailyGrowth = BigDecimal.ONE.add(new BigDecimal("0.10").divide(BigDecimal.valueOf(365), MathContext.DECIMAL64))
                .pow(45, MathContext.DECIMAL64).subtract(BigDecimal.ONE);
        final BigDecimal firstBalanceInterest = new BigDecimal("10000").multiply(dailyGrowth);
        final BigDecimal expected = new BigDecimal("20000").add(firstBalanceInterest).multiply(dailyGrowth).add(firstBalanceInterest)
                .setScale(2, RoundingMode.HALF_EVEN);

        assertThat(withoutSplit).as("Q1 interest without the overdraft split").isEqualByComparingTo(expected);
        assertThat(withSplit).as("Q1 interest with the overdraft split").isEqualByComparingTo(expected);
    }

    @Test
    void overdraftSplitMonthlyPostingCountsOverdraftCarriedIntoNextMonthOnce() {
        final LocalDate businessDate = LocalDate.of(2025, 3, 1);
        final LocalDate januaryEnd = LocalDate.of(2025, 1, 31);
        final LocalDate februaryStart = LocalDate.of(2025, 2, 1);
        final LocalDate februaryEnd = LocalDate.of(2025, 2, 28);

        // overdrawn from Jan 16 into February, back in credit from Feb 20: the split path must match the unsplit
        // calculation for every period, neither missing the carried overdraft days nor counting them twice
        final SavingsAccountData withoutSplit = accountOverdrawnAcrossMonthEnd(false);
        final SavingsAccountData withSplit = accountOverdrawnAcrossMonthEnd(true);

        // the split posts the overdraft and the credit part of a month as separate periods, each rounded to the
        // currency
        // scale, so the total may differ from the unsplit calculation by one rounding step
        assertThat(interestFor(withSplit, businessDate, ACTIVATION_DATE, januaryEnd)).as("January interest with the overdraft split")
                .isCloseTo(interestFor(withoutSplit, businessDate, ACTIVATION_DATE, januaryEnd), within(new BigDecimal("0.01")));
        assertThat(interestFor(withSplit, businessDate, februaryStart, februaryEnd)).as("February interest with the overdraft split")
                .isCloseTo(interestFor(withoutSplit, businessDate, februaryStart, februaryEnd), within(new BigDecimal("0.01")));
    }

    private BigDecimal interestFor(final SavingsAccountData account, final LocalDate businessDate, final LocalDate periodStart,
            final LocalDate periodEnd) {
        final List<PostingPeriod> postingPeriods = service.calculateInterestUsing(MATH_CONTEXT, businessDate, false, false, 1, null, false,
                account);
        return postingPeriods.stream()
                .filter(period -> period.getPeriodInterval().startDate().equals(periodStart)
                        && period.getPeriodInterval().endDate().equals(periodEnd))
                .map(PostingPeriod::getInterestEarned).reduce(Money.zero(CURRENCY), Money::plus).getAmount();
    }

    private SavingsAccountData accountWithTwoDeposits(final boolean allowOverdraft, final boolean interestReceivableMapped) {
        final List<SavingsAccountTransactionData> transactions = new ArrayList<>();
        transactions.add(transaction(1L, SavingsAccountTransactionType.DEPOSIT, ACTIVATION_DATE, "10000", null));
        transactions.add(transaction(2L, SavingsAccountTransactionType.DEPOSIT, LocalDate.of(2025, 2, 15), "10000", null));
        return account(transactions, SavingsPostingInterestPeriodType.QUATERLY, allowOverdraft, interestReceivableMapped);
    }

    private SavingsAccountData accountOverdrawnAcrossMonthEnd(final boolean interestReceivableMapped) {
        final List<SavingsAccountTransactionData> transactions = new ArrayList<>();
        transactions.add(transaction(1L, SavingsAccountTransactionType.DEPOSIT, ACTIVATION_DATE, "10000", null));
        // overdraft amounts preset to what the daily balance recalculation derives, so it leaves the transactions as-is
        transactions.add(transaction(2L, SavingsAccountTransactionType.WITHDRAWAL, LocalDate.of(2025, 1, 16), "20000", "10000"));
        transactions.add(transaction(3L, SavingsAccountTransactionType.DEPOSIT, LocalDate.of(2025, 2, 20), "20000", "10000"));
        return account(transactions, SavingsPostingInterestPeriodType.MONTHLY, true, interestReceivableMapped);
    }

    private SavingsAccountData account(final List<SavingsAccountTransactionData> transactions,
            final SavingsPostingInterestPeriodType postingPeriodType, final boolean allowOverdraft,
            final boolean interestReceivableMapped) {
        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getId()).thenReturn(1L);
        when(account.getCurrency()).thenReturn(CURRENCY);
        when(account.getSummary()).thenReturn(mock(SavingsAccountSummaryData.class));
        when(account.getSavingsAccountTransactionData()).thenReturn(transactions);
        when(account.getActivationLocalDate()).thenReturn(ACTIVATION_DATE);
        when(account.getStartInterestCalculationDate()).thenReturn(ACTIVATION_DATE);
        when(account.getNominalAnnualInterestRate()).thenReturn(new BigDecimal("10"));
        when(account.getNominalAnnualInterestRateOverdraft()).thenReturn(new BigDecimal("21"));
        when(account.getMinBalanceForInterestCalculation()).thenReturn(BigDecimal.ZERO);
        when(account.getMinOverdraftForInterestCalculation()).thenReturn(BigDecimal.ZERO);
        when(account.getInterestPostingPeriodTypeId()).thenReturn(postingPeriodType.getValue());
        when(account.getInterestCompoundingPeriodTypeId()).thenReturn(SavingsCompoundingInterestPeriodType.DAILY.getValue());
        when(account.getInterestCalculationTypeId()).thenReturn(SavingsInterestCalculationType.DAILY_BALANCE.getValue());
        when(account.getInterestCalculationDaysInYearTypeId()).thenReturn(SavingsInterestCalculationDaysInYearType.DAYS_365.getValue());
        when(account.isAllowOverdraft()).thenReturn(allowOverdraft);
        // an unmapped account is read as 0 (ResultSet#getLong on a NULL column), which selects the unsplit calculation
        when(account.getGlAccountIdForInterestReceivable()).thenReturn(interestReceivableMapped ? 1L : 0L);
        return account;
    }

    private static SavingsAccountTransactionData transaction(final Long id, final SavingsAccountTransactionType type, final LocalDate date,
            final String amount, final String overdraftAmount) {
        final SavingsAccountTransactionData transaction = SavingsAccountTransactionData.create(id,
                SavingsEnumerations.transactionType(type), null, 1L, "000000001", date, CURRENCY, new BigDecimal(amount), null,
                BigDecimal.ZERO, false, date, false, null, null);
        if (overdraftAmount != null) {
            transaction.updateOverdraftAmount(new BigDecimal(overdraftAmount));
        }
        return transaction;
    }
}
