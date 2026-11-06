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
package org.apache.fineract.portfolio.savings.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SavingsAccountInterestCalculationTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, 0);
    private static final MathContext MC = MathContext.DECIMAL64;
    private static final LocalDate ACTIVATED_ON = LocalDate.of(2024, 1, 1);

    private final Office office = mock(Office.class);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 7, 1))));
        MoneyHelper.initializeTenantRoundingMode("default", 6); // HALF_EVEN
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        MoneyHelper.clearCache();
    }

    @Test
    void dailyCompoundingMonthlyPostingOnDailyBalance() {
        final SavingsAccount account = account("5", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        depositsAndWithdrawal(account);

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 3, 31), null, false);

        assertEquals(
                "2024-01-01..2024-01-31 posted 2024-02-01 interest 5.42 closing 1500.00; 2024-02-01..2024-02-29 posted 2024-03-01 interest 5.44 closing 1300.00; 2024-03-01..2024-03-31 posted 2024-04-01 interest 5.58 closing 1300.00",
                describe(periods));
        assertEquals("16.44", account.getSummary().getTotalInterestEarned().toPlainString());
    }

    @Test
    void monthlyCompoundingQuarterlyPostingOnAverageDailyBalance() {
        final SavingsAccount account = account("4", SavingsCompoundingInterestPeriodType.MONTHLY, SavingsPostingInterestPeriodType.QUATERLY,
                SavingsInterestCalculationType.AVERAGE_DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_360);
        depositsAndWithdrawal(account);

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 6, 30), null, false);

        assertEquals(
                "2024-01-01..2024-03-31 posted 2024-04-01 interest 13.30 closing 1300.00; 2024-04-01..2024-06-30 posted 2024-07-01 interest 13.32 closing 1300.00",
                describe(periods));
        assertEquals("26.62", account.getSummary().getTotalInterestEarned().toPlainString());
    }

    @Test
    void overdrawnDaysEarnOverdraftInterest() {
        final SavingsAccount account = account("3", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        setField(account, "allowOverdraft", true);
        setField(account, "overdraftLimit", new BigDecimal("1000"));
        setField(account, "nominalAnnualInterestRateOverdraft", new BigDecimal("12"));
        setField(account, "minOverdraftForInterestCalculation", BigDecimal.ZERO);
        account.addTransaction(SavingsAccountTransaction.deposit(account, office, null, ACTIVATED_ON, money("100"), null));
        account.addTransaction(SavingsAccountTransaction.withdrawal(account, office, null, LocalDate.of(2024, 1, 20), money("400"), null));

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 2, 29), null, false);

        assertEquals(
                "2024-01-01..2024-01-31 posted 2024-02-01 interest -1.03 closing -300.00; 2024-02-01..2024-02-29 posted 2024-03-01 interest -2.88 closing -300.00",
                describe(periods));
        assertEquals("-3.91", account.getSummary().getTotalInterestEarned().toPlainString());
    }

    @Test
    void postInterestOnDateEndsAPostingPeriodOnThatDate() {
        final SavingsAccount account = account("5", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        depositsAndWithdrawal(account);

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 3, 31), LocalDate.of(2024, 2, 15), false);

        assertEquals(
                "2024-01-01..2024-01-31 posted 2024-02-01 interest 5.42 closing 1500.00; 2024-02-01..2024-02-14 posted 2024-02-15 interest 2.75 closing 1300.00; 2024-02-15..2024-02-29 posted 2024-03-01 interest 2.69 closing 1300.00; 2024-03-01..2024-03-31 posted 2024-04-01 interest 5.58 closing 1300.00",
                describe(periods));
    }

    @Test
    void laterStartInterestCalculationDateStartsFromThePivotBalance() {
        final SavingsAccount account = account("5", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        depositsAndWithdrawal(account);
        account.setStartInterestCalculationDate(LocalDate.of(2024, 1, 20));
        account.getSummary().setRunningBalanceOnPivotDate(new BigDecimal("1500"));

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 3, 31), null, false);

        assertEquals(
                "2024-01-20..2024-01-31 posted 2024-02-01 interest 0.00 closing 0.00; 2024-02-01..2024-02-29 posted 2024-03-01 interest 3.57 closing 1300.00; 2024-03-01..2024-03-31 posted 2024-04-01 interest 5.55 closing 1300.00",
                describe(periods));
    }

    @Test
    void backdatedTransactionsUseThePivotBalanceAndTransactions() {
        final SavingsAccount account = account("5", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        account.getSummary().setRunningBalanceOnPivotDate(new BigDecimal("1500"));
        account.setSavingsAccountTransactions(
                List.of(SavingsAccountTransaction.withdrawal(account, office, null, LocalDate.of(2024, 2, 10), money("200"), null)));

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 3, 31), null, true);

        assertEquals(
                "2024-01-01..2024-01-31 posted 2024-02-01 interest 0.00 closing 0.00; 2024-02-01..2024-02-29 posted 2024-03-01 interest 3.57 closing 1300.00; 2024-03-01..2024-03-31 posted 2024-04-01 interest 5.55 closing 1300.00",
                describe(periods));
        assertEquals("9.12", account.getSummary().getTotalInterestEarned().toPlainString());
    }

    @Test
    void noInterestRateAndNoOverdraftGivesNoPostingPeriods() {
        final SavingsAccount account = account("0", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        depositsAndWithdrawal(account);

        final List<PostingPeriod> periods = calculate(account, LocalDate.of(2024, 3, 31), null, false);

        assertEquals("", describe(periods));
        assertEquals("1300.00", account.getSummary().getAccountBalance().toPlainString());
        assertNull(account.getSummary().getTotalInterestEarned());
    }

    @Test
    void calculatorReturnsThePostingPeriodsWithoutUpdatingTheSummary() {
        final SavingsAccount account = account("5", SavingsCompoundingInterestPeriodType.DAILY, SavingsPostingInterestPeriodType.MONTHLY,
                SavingsInterestCalculationType.DAILY_BALANCE, SavingsInterestCalculationDaysInYearType.DAYS_365);
        depositsAndWithdrawal(account);
        final LocalDate upTo = LocalDate.of(2024, 3, 31);
        account.recalculateDailyBalances(Money.zero(CURRENCY), upTo, false, false);

        final List<PostingPeriod> periods = new SavingsAccountInterestCalculator().calculatePostingPeriods(account, MC, upTo, false, false,
                1, null, false);

        assertEquals("2024-01-01..2024-01-31 posted 2024-02-01 interest 5.42 closing 1500.00; "
                + "2024-02-01..2024-02-29 posted 2024-03-01 interest 5.44 closing 1300.00; "
                + "2024-03-01..2024-03-31 posted 2024-04-01 interest 5.58 closing 1300.00", describe(periods));
        assertNull(account.getSummary().getTotalInterestEarned());
        assertNull(account.getSummary().getLastInterestCalculationDate());
    }

    private List<PostingPeriod> calculate(final SavingsAccount account, final LocalDate upTo, final LocalDate postInterestOnDate,
            final boolean backdatedTxnsAllowedTill) {
        return account.calculateInterestUsing(MC, upTo, false, false, 1, postInterestOnDate, backdatedTxnsAllowedTill, false);
    }

    private void depositsAndWithdrawal(final SavingsAccount account) {
        account.addTransaction(SavingsAccountTransaction.deposit(account, office, null, ACTIVATED_ON, money("1000"), null));
        account.addTransaction(SavingsAccountTransaction.deposit(account, office, null, LocalDate.of(2024, 1, 15), money("500"), null));
        account.addTransaction(SavingsAccountTransaction.withdrawal(account, office, null, LocalDate.of(2024, 2, 10), money("200"), null));
    }

    private static SavingsAccount account(final String nominalAnnualInterestRate, final SavingsCompoundingInterestPeriodType compounding,
            final SavingsPostingInterestPeriodType posting, final SavingsInterestCalculationType calculation,
            final SavingsInterestCalculationDaysInYearType daysInYear) {
        final SavingsAccount account = new SavingsAccount();
        setField(account, "currency", CURRENCY);
        setField(account, "activatedOnDate", ACTIVATED_ON);
        setField(account, "nominalAnnualInterestRate", new BigDecimal(nominalAnnualInterestRate));
        setField(account, "nominalAnnualInterestRateOverdraft", BigDecimal.ZERO);
        setField(account, "interestCompoundingPeriodType", compounding.getValue());
        setField(account, "interestPostingPeriodType", posting.getValue());
        setField(account, "interestCalculationType", calculation.getValue());
        setField(account, "interestCalculationDaysInYearType", daysInYear.getValue());
        setField(account, "summary", new SavingsAccountSummary());

        final AccountTransfersReadPlatformService transfers = mock(AccountTransfersReadPlatformService.class);
        when(transfers.fetchPostInterestTransactionIds(anyLong())).thenReturn(List.of());
        account.setHelpers(new SavingsAccountTransactionSummaryWrapper(), new SavingsHelper(transfers), null);
        return account;
    }

    private static Money money(final String amount) {
        return Money.of(CURRENCY, new BigDecimal(amount));
    }

    private static String describe(final List<PostingPeriod> periods) {
        return periods.stream()
                .map(p -> p.getPeriodInterval().startDate() + ".." + p.getPeriodInterval().endDate() + " posted "
                        + p.dateOfPostingTransaction() + " interest " + p.getInterestEarned().getAmount() + " closing "
                        + p.closingBalance().getAmount())
                .collect(Collectors.joining("; "));
    }

    private static void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = SavingsAccount.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }
}
