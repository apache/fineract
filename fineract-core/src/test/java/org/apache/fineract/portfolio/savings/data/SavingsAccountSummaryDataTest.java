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
package org.apache.fineract.portfolio.savings.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargesPaidByData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionDataSummaryWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * The interest posting job recalculates the balance from the running balance on the pivot date and the transactions
 * after it when the pivot date configuration is on. The transactions are built the way the job loads them, because on
 * transaction data a single charge deduction answers to more than one of the charge totals.
 */
class SavingsAccountSummaryDataTest {

    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, 0);
    private static final LocalDate AFTER_PIVOT = LocalDate.of(2025, 2, 10);
    private static final LocalDate POSTING_DATE = LocalDate.of(2025, 2, 28);

    private static MockedStatic<MoneyHelper> moneyHelper;
    private static long nextId = 1;

    @BeforeAll
    static void setUp() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.HALF_EVEN));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void pivotBalanceDeductsEachChargePaidAfterThePivotDateOnce() {
        final SavingsAccountSummaryData summary = summaryWithRunningBalanceOnPivotDate("500");

        update(summary, transaction(SavingsAccountTransactionType.DEPOSIT, "100", AFTER_PIVOT),
                transaction(SavingsAccountTransactionType.WITHDRAWAL, "50", AFTER_PIVOT),
                transaction(SavingsAccountTransactionType.WITHDRAWAL_FEE, "5", AFTER_PIVOT),
                transaction(SavingsAccountTransactionType.ANNUAL_FEE, "7", AFTER_PIVOT), payCharge("40", false), payCharge("3", true),
                transaction(SavingsAccountTransactionType.INTEREST_POSTING, "10", POSTING_DATE));

        // 500 + 100 + 10 - 50 - 5 - 7 - 40 - 3
        assertThat(summary.getAccountBalance()).isEqualByComparingTo("505");
        assertThat(summary.getTotalInterestPosted()).isEqualByComparingTo("10");
        assertThat(summary.getInterestPostedTillDate()).isEqualTo(POSTING_DATE);
    }

    @Test
    void pivotBalanceIgnoresReversedCharges() {
        final SavingsAccountSummaryData summary = summaryWithRunningBalanceOnPivotDate("500");
        final SavingsAccountTransactionData reversedFee = payCharge("40", false, true);

        update(summary, transaction(SavingsAccountTransactionType.WITHDRAWAL, "100", AFTER_PIVOT), reversedFee);

        assertThat(summary.getAccountBalance()).isEqualByComparingTo("400");
    }

    private static void update(final SavingsAccountSummaryData summary, final SavingsAccountTransactionData... transactions) {
        summary.updateSummaryWithPivotConfig(CURRENCY, new SavingsAccountTransactionDataSummaryWrapper(), null, List.of(transactions));
    }

    private static SavingsAccountSummaryData summaryWithRunningBalanceOnPivotDate(final String runningBalanceOnPivotDate) {
        final SavingsAccountSummaryData summary = new SavingsAccountSummaryData(CURRENCY, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
        summary.setRunningBalanceOnPivotDate(new BigDecimal(runningBalanceOnPivotDate));
        return summary;
    }

    private static SavingsAccountTransactionData payCharge(final String amount, final boolean penalty) {
        return payCharge(amount, penalty, false);
    }

    private static SavingsAccountTransactionData payCharge(final String amount, final boolean penalty, final boolean reversed) {
        final SavingsAccountTransactionData transaction = transaction(SavingsAccountTransactionType.PAY_CHARGE, amount, AFTER_PIVOT,
                reversed);
        final long chargeId = nextId++;
        final SavingsAccountChargesPaidByData paidBy = new SavingsAccountChargesPaidByData(chargeId, new BigDecimal(amount));
        paidBy.setSavingsAccountChargeData(new SavingsAccountChargeData(chargeId, new BigDecimal(amount),
                new EnumOptionData(ChargeTimeType.SPECIFIED_DUE_DATE.getValue().longValue(), null, null), penalty));
        transaction.setChargesPaidByData(paidBy);
        return transaction;
    }

    private static SavingsAccountTransactionData transaction(final SavingsAccountTransactionType type, final String amount,
            final LocalDate date) {
        return transaction(type, amount, date, false);
    }

    private static SavingsAccountTransactionData transaction(final SavingsAccountTransactionType type, final String amount,
            final LocalDate date, final boolean reversed) {
        final SavingsAccountTransactionEnumData transactionType = new SavingsAccountTransactionEnumData(type.getValue().longValue(),
                type.getCode(), type.name());
        return SavingsAccountTransactionData.create(nextId++, transactionType, null, 1L, "000000001", date, CURRENCY,
                new BigDecimal(amount), null, BigDecimal.ZERO, reversed, date, false, BigDecimal.ZERO, null, false);
    }
}
