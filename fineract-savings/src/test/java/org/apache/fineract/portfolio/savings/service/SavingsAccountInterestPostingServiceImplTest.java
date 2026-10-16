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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.service.AccountTransfersReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountSummaryData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.domain.SavingsHelper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SavingsAccountInterestPostingServiceImplTest {

    private static final MathContext MATH_CONTEXT = new MathContext(12, RoundingMode.HALF_EVEN);
    private static final LocalDate ACTIVATION_DATE = LocalDate.of(2026, 6, 1);
    private static final LocalDate INTEREST_POSTED_TILL_DATE = LocalDate.of(2026, 8, 1);
    private static final LocalDate POSTING_UP_TO_DATE = LocalDate.of(2026, 10, 15);

    private MockedStatic<MoneyHelper> moneyHelper;
    private SavingsAccountInterestPostingServiceImpl underTest;

    @BeforeEach
    void setUp() {
        moneyHelper = mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MATH_CONTEXT);

        final AccountTransfersReadPlatformService accountTransfersReadPlatformService = mock(AccountTransfersReadPlatformService.class);
        when(accountTransfersReadPlatformService.fetchPostInterestTransactionIds(anyLong())).thenReturn(new ArrayList<>());
        underTest = new SavingsAccountInterestPostingServiceImpl(new SavingsHelper(accountTransfersReadPlatformService));
    }

    @AfterEach
    void tearDown() {
        moneyHelper.close();
    }

    @Test
    void calculatesInterestForIdlePeriodsAfterPivotDateWhenBalanceIsCarriedForward() {
        // An account that was already posted up to the pivot date and has had no activity since: the interest posting
        // read path only loads transactions on or after the pivot date, so the transaction list is empty and the
        // balance is carried forward through runningBalanceOnPivotDate.
        final SavingsAccountData account = idleAccountAfterPivotDate(new BigDecimal("1000"));

        final List<PostingPeriod> postingPeriods = underTest.calculateInterestUsing(MATH_CONTEXT, POSTING_UP_TO_DATE, false, false, 1, null,
                true, account);

        assertEquals(3, postingPeriods.size(), "August, September and the running part of October must each get a posting period");
        final PostingPeriod august = postingPeriods.get(0);
        assertEquals(LocalDate.of(2026, 8, 1), august.getPeriodInterval().startDate());
        assertEquals(0, new BigDecimal("1000").compareTo(august.getOpeningBalance().getAmount()));
        assertTrue(august.getInterestEarned().isGreaterThanZero(), "a non-zero carried-forward balance must earn interest");
    }

    @Test
    void skipsIdlePeriodsAfterPivotDateWhenCarriedForwardBalanceIsZero() {
        final SavingsAccountData account = idleAccountAfterPivotDate(BigDecimal.ZERO);

        final List<PostingPeriod> postingPeriods = underTest.calculateInterestUsing(MATH_CONTEXT, POSTING_UP_TO_DATE, false, false, 1, null,
                true, account);

        assertTrue(postingPeriods.isEmpty(), "there is nothing to accrue on a zero balance without transactions");
    }

    private static SavingsAccountData idleAccountAfterPivotDate(final BigDecimal runningBalanceOnPivotDate) {
        final SavingsAccountSummaryData summary = mock(SavingsAccountSummaryData.class);
        when(summary.getRunningBalanceOnPivotDate()).thenReturn(runningBalanceOnPivotDate);

        final SavingsAccountData account = mock(SavingsAccountData.class);
        when(account.getId()).thenReturn(1L);
        when(account.getCurrency()).thenReturn(new CurrencyData("USD", "US Dollar", 2, 0, "$", "USD"));
        when(account.getSummary()).thenReturn(summary);
        when(account.getSavingsAccountTransactionData()).thenReturn(new ArrayList<>());
        when(account.getLastSavingsAccountTransaction()).thenReturn(mock(SavingsAccountTransactionData.class));
        when(account.getActivationLocalDate()).thenReturn(ACTIVATION_DATE);
        when(account.getStartInterestCalculationDate()).thenReturn(INTEREST_POSTED_TILL_DATE);
        when(account.getInterestPostingPeriodTypeId()).thenReturn(SavingsPostingInterestPeriodType.MONTHLY.getValue());
        when(account.getInterestCompoundingPeriodTypeId()).thenReturn(SavingsCompoundingInterestPeriodType.DAILY.getValue());
        when(account.getInterestCalculationDaysInYearTypeId()).thenReturn(SavingsInterestCalculationDaysInYearType.DAYS_365.getValue());
        when(account.getInterestCalculationTypeId()).thenReturn(SavingsInterestCalculationType.DAILY_BALANCE.getValue());
        when(account.getNominalAnnualInterestRate()).thenReturn(new BigDecimal("5"));
        when(account.getMinBalanceForInterestCalculation()).thenReturn(BigDecimal.ZERO);
        when(account.getMinOverdraftForInterestCalculation()).thenReturn(BigDecimal.ZERO);
        when(account.isAllowOverdraft()).thenReturn(false);
        return account;
    }
}
