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
package org.apache.fineract.portfolio.savings.domain.interest;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CompoundInterestHelperTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);
    private static final LocalDate JANUARY_START = LocalDate.of(2025, 1, 1);
    private static final LocalDate JANUARY_END = LocalDate.of(2025, 1, 31);

    private static MockedStatic<MoneyHelper> moneyHelper;

    @BeforeAll
    static void setUp() {
        moneyHelper = Mockito.mockStatic(MoneyHelper.class);
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(MathContext.DECIMAL64);
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.HALF_EVEN);
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void secondPartOfASplitIntervalMustComeRightAfterTheFirst() {
        final PostingPeriod january = postingPeriod(JANUARY_START, JANUARY_END);
        final PostingPeriod januaryOverdrawn = postingPeriod(JANUARY_START, JANUARY_END);
        final PostingPeriod february = postingPeriod(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28));
        january.setSecondPartOfInterval(januaryOverdrawn);

        assertThatThrownBy(() -> new CompoundInterestHelper().calculateInterestForAllPostingPeriods(CURRENCY,
                List.of(january, february, januaryOverdrawn), null, false)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(JANUARY_START.toString());
        assertThatThrownBy(
                () -> new CompoundInterestHelper().calculateInterestForAllPostingPeriods(CURRENCY, List.of(january), null, false))
                .as("second part missing").isInstanceOf(IllegalStateException.class);
    }

    private static PostingPeriod postingPeriod(final LocalDate start, final LocalDate end) {
        return PostingPeriod.createFrom(LocalDateInterval.create(start, end), Money.zero(CURRENCY), List.of(), CURRENCY,
                SavingsCompoundingInterestPeriodType.DAILY, SavingsInterestCalculationType.DAILY_BALANCE, new BigDecimal("0.1"), 365, end,
                List.of(), false, Money.zero(CURRENCY), false, false, 1);
    }
}
