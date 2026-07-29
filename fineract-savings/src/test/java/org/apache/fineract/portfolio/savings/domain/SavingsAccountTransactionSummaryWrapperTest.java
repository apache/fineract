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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * The summary totals are calculated outside the entity and stored as-is by {@link SavingsAccountSummary}.
 */
class SavingsAccountTransactionSummaryWrapperTest {

    private static final MockedStatic<MoneyHelper> MONEY_HELPER = Mockito.mockStatic(MoneyHelper.class);
    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);

    private static final LocalDate FIRST_POSTING_DATE = LocalDate.of(2025, 1, 31);
    private static final LocalDate SECOND_POSTING_DATE = LocalDate.of(2025, 2, 28);

    @BeforeAll
    static void setUp() {
        MONEY_HELPER.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        MONEY_HELPER.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void tearDown() {
        MONEY_HELPER.close();
    }

    @Test
    void summaryTotalsSumEachTransactionTypeAndDeriveTheBalance() {
        final List<SavingsAccountTransaction> transactions = List.of(deposit("1000"), withdrawal("200"),
                interestPosting("15", FIRST_POSTING_DATE), withholdTax("3"));

        final SavingsAccountSummaryTotals totals = SavingsAccountTransactionSummaryWrapper.calculateSummaryTotals(CURRENCY, transactions);

        assertThat(totals.totalDeposits()).isEqualByComparingTo("1000");
        assertThat(totals.totalWithdrawals()).isEqualByComparingTo("200");
        assertThat(totals.totalInterestPosted()).isEqualByComparingTo("15");
        assertThat(totals.totalWithholdTax()).isEqualByComparingTo("3");
        // totals without matching transactions stay null, as before the extraction
        assertThat(totals.totalWithdrawalFees()).isNull();
        assertThat(totals.totalOverdraftInterestDerived()).isNull();
        // 1000 + 15 - 200 - 3
        assertThat(totals.accountBalance()).isEqualByComparingTo("812");
        assertThat(totals.interestPostedTillDate()).isEqualTo(FIRST_POSTING_DATE);
    }

    @Test
    void interestPostedTillDateSkipsReversalTransactions() {
        final SavingsAccountTransaction reversal = interestPosting("15", SECOND_POSTING_DATE);
        when(reversal.isReversalTransaction()).thenReturn(true);

        final SavingsAccountSummaryTotals totals = SavingsAccountTransactionSummaryWrapper.calculateSummaryTotals(CURRENCY,
                List.of(deposit("1000"), interestPosting("10", FIRST_POSTING_DATE), reversal));

        assertThat(totals.interestPostedTillDate()).isEqualTo(FIRST_POSTING_DATE);
        assertThat(totals.totalInterestPosted()).isEqualByComparingTo("10");
    }

    @Test
    void summaryKeepsInterestPostedTillDateWhenThereIsNoInterestPosting() {
        final SavingsAccountSummary summary = new SavingsAccountSummary();
        summary.setInterestPostedTillDate(FIRST_POSTING_DATE);

        summary.updateSummary(SavingsAccountTransactionSummaryWrapper.calculateSummaryTotals(CURRENCY, List.of(deposit("500"))));

        assertThat(summary.getInterestPostedTillDate()).isEqualTo(FIRST_POSTING_DATE);
        assertThat(summary.getTotalDeposits()).isEqualByComparingTo("500");
        assertThat(summary.getAccountBalance()).isEqualByComparingTo("500");
    }

    @Test
    void pivotTotalsStartFromTheRunningBalanceOnThePivotDate() {
        final List<SavingsAccountTransaction> transactions = List.of(deposit("100"), withdrawal("50"),
                interestPosting("10", SECOND_POSTING_DATE), withholdTax("2"));

        final SavingsAccountPivotSummaryTotals totals = SavingsAccountTransactionSummaryWrapper.calculatePivotSummaryTotals(CURRENCY,
                transactions, new BigDecimal("500"));

        assertThat(totals.totalInterestPosted()).isEqualByComparingTo("10");
        assertThat(totals.totalWithholdTax()).isEqualByComparingTo("2");
        assertThat(totals.totalOverdraftInterestDerived()).isNull();
        // 500 + 100 + 10 - 50 - 2
        assertThat(totals.accountBalance()).isEqualByComparingTo("558");
        assertThat(totals.interestPostedTillDate()).isEqualTo(SECOND_POSTING_DATE);
    }

    private static SavingsAccountTransaction deposit(final String amount) {
        final SavingsAccountTransaction transaction = transaction(amount, LocalDate.of(2025, 1, 1));
        when(transaction.isDepositAndNotReversed()).thenReturn(true);
        return transaction;
    }

    private static SavingsAccountTransaction withdrawal(final String amount) {
        final SavingsAccountTransaction transaction = transaction(amount, LocalDate.of(2025, 1, 15));
        when(transaction.isWithdrawal()).thenReturn(true);
        return transaction;
    }

    private static SavingsAccountTransaction interestPosting(final String amount, final LocalDate date) {
        final SavingsAccountTransaction transaction = transaction(amount, date);
        when(transaction.isInterestPostingAndNotReversed()).thenReturn(true);
        return transaction;
    }

    private static SavingsAccountTransaction withholdTax(final String amount) {
        final SavingsAccountTransaction transaction = transaction(amount, FIRST_POSTING_DATE);
        when(transaction.isWithHoldTaxAndNotReversed()).thenReturn(true);
        return transaction;
    }

    private static SavingsAccountTransaction transaction(final String amount, final LocalDate date) {
        // built before stubbing: Money.of uses the mocked MoneyHelper, which cannot be called inside when(...)
        final Money money = Money.of(CURRENCY, new BigDecimal(amount));
        final SavingsAccountTransaction transaction = mock(SavingsAccountTransaction.class);
        when(transaction.getAmount()).thenReturn(new BigDecimal(amount));
        when(transaction.getAmount(CURRENCY)).thenReturn(money);
        when(transaction.getTransactionDate()).thenReturn(date);
        when(transaction.isNotReversed()).thenReturn(true);
        return transaction;
    }
}
