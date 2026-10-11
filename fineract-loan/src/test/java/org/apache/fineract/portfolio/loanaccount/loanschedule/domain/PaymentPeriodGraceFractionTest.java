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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;

class PaymentPeriodGraceFractionTest {

    private final DefaultPaymentPeriodsInOneYearCalculator calculator = new DefaultPaymentPeriodsInOneYearCalculator();
    private final LocalDate start = LocalDate.of(2026, 1, 1);

    @Test
    void sevenDaysOfGraceInTwoWeeksShouldBeHalfAPeriod() {
        assertFraction("0.5", start.plusDays(14), start.plusDays(7), PeriodFrequencyType.WEEKS, 2);
    }

    @Test
    void oneDayOfGraceInTwoDaysShouldBeHalfAPeriod() {
        assertFraction("0.5", start.plusDays(2), start.plusDays(1), PeriodFrequencyType.DAYS, 2);
    }

    @Test
    void doublingThePeriodShouldHalveTheGraceFraction() {
        assertFraction("0.25", start.plusDays(28), start.plusDays(7), PeriodFrequencyType.WEEKS, 4);
    }

    @Test
    void thirtyDaysOfGraceInTwoStandardMonthsShouldBeHalfAPeriod() {
        assertFraction("0.5", start.plusDays(60), start.plusDays(30), PeriodFrequencyType.MONTHS, 2);
    }

    @Test
    void oneStandardYearOfGraceInTwoStandardYearsShouldBeHalfAPeriod() {
        assertFraction("0.5", start.plusDays(730), start.plusDays(365), PeriodFrequencyType.YEARS, 2);
    }

    @Test
    void sameDateIntervalShouldAgreeAcrossDaysAndWeeks() {
        BigDecimal daily = calculator.calculatePortionOfRepaymentPeriodInterestChargingGrace(start, start.plusDays(14), start.plusDays(7),
                PeriodFrequencyType.DAYS, 14, MathContext.DECIMAL64);
        BigDecimal weekly = calculator.calculatePortionOfRepaymentPeriodInterestChargingGrace(start, start.plusDays(14), start.plusDays(7),
                PeriodFrequencyType.WEEKS, 2, MathContext.DECIMAL64);

        assertEquals(0, daily.compareTo(weekly));
    }

    @Test
    void entirePeriodOfGraceShouldRemainOne() {
        assertFraction("1", start.plusDays(14), start.plusDays(14), PeriodFrequencyType.WEEKS, 2);
    }

    @Test
    void entireWeekOfGraceInSingleWeekPeriodShouldRemainOne() {
        assertFraction("1", start.plusDays(7), start.plusDays(7), PeriodFrequencyType.WEEKS, 1);
    }

    @Test
    void missingGraceDateShouldRemainZero() {
        assertFraction("0", start.plusDays(14), null, PeriodFrequencyType.WEEKS, 2);
    }

    @Test
    void noGraceAtStartShouldRemainZero() {
        assertFraction("0", start.plusDays(14), start, PeriodFrequencyType.WEEKS, 2);
    }

    private void assertFraction(String expected, LocalDate due, LocalDate chargedFrom, PeriodFrequencyType frequency, int every) {
        BigDecimal actual = calculator.calculatePortionOfRepaymentPeriodInterestChargingGrace(start, due, chargedFrom, frequency, every,
                MathContext.DECIMAL64);

        assertEquals(0, new BigDecimal(expected).compareTo(actual), () -> "Expected " + expected + " but got " + actual);
    }
}
