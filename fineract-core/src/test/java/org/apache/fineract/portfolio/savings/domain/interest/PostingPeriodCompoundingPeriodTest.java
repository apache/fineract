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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.LocalDateInterval;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class PostingPeriodCompoundingPeriodTest {

    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);
    private static final int MARCH = 3;

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

    @AfterEach
    void resetContext() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void annualCompoundingWithMarchFiscalYearEndsOnLeapDayFebruary() {
        setBusinessDate(LocalDate.of(2024, 6, 15));

        List<CompoundingPeriod> periods = compoundingPeriods(LocalDate.of(2023, 3, 1), LocalDate.of(2024, 2, 29),
                SavingsCompoundingInterestPeriodType.ANNUAL);

        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getPeriodInterval().startDate()).isEqualTo(LocalDate.of(2023, 3, 1));
        assertThat(periods.get(0).getPeriodInterval().endDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    @Test
    void annualCompoundingWithMarchFiscalYearEndsOnFebruary28WhenNextFebruaryIsNotLeap() {
        setBusinessDate(LocalDate.of(2024, 6, 15));

        List<CompoundingPeriod> periods = compoundingPeriods(LocalDate.of(2024, 3, 1), LocalDate.of(2025, 2, 28),
                SavingsCompoundingInterestPeriodType.ANNUAL);

        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getPeriodInterval().endDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    void quarterlyCompoundingUsesLengthOfFebruaryOfThePeriodYearNotTheBusinessDateYear() {
        // business date is in a leap year, the period is in a year that is not
        setBusinessDate(LocalDate.of(2024, 6, 15));

        List<CompoundingPeriod> periods = compoundingPeriods(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 2, 28),
                SavingsCompoundingInterestPeriodType.QUATERLY);

        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getPeriodInterval().endDate()).isEqualTo(LocalDate.of(2025, 2, 28));
    }

    @Test
    void quarterlyCompoundingKeepsLeapDayFebruaryWhenTheBusinessDateYearIsNotLeap() {
        // business date is in a year that is not leap, the period is in a leap year
        setBusinessDate(LocalDate.of(2025, 6, 15));

        List<CompoundingPeriod> periods = compoundingPeriods(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 29),
                SavingsCompoundingInterestPeriodType.QUATERLY);

        assertThat(periods).hasSize(1);
        assertThat(periods.get(0).getPeriodInterval().endDate()).isEqualTo(LocalDate.of(2024, 2, 29));
    }

    private static void setBusinessDate(final LocalDate date) {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, date)));
    }

    private static List<CompoundingPeriod> compoundingPeriods(final LocalDate postingStart, final LocalDate postingEnd,
            final SavingsCompoundingInterestPeriodType compoundingType) {
        PostingPeriod postingPeriod = PostingPeriod.createFrom(LocalDateInterval.create(postingStart, postingEnd), Money.zero(CURRENCY),
                List.of(), CURRENCY, compoundingType, SavingsInterestCalculationType.DAILY_BALANCE, new BigDecimal("0.365"), 365,
                postingEnd, List.of(), false, Money.zero(CURRENCY), false, false, MARCH);
        return postingPeriod.getCompoundingPeriods();
    }
}
