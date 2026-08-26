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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.junit.jupiter.api.Test;

/**
 * Numerical edges the amortization feature files cannot reach: a schedule extreme enough to push the annual rate past
 * what a {@code double} holds is not bookable through the API, and exactness at zero is invisible to an assertion
 * rounded to two decimals.
 */
class TvmFunctionsTest {

    /** The default tenant's context: {@link MoneyHelper#PRECISION} digits, and the rate rounding the model pins. */
    private static final MathContext MC = new MathContext(MoneyHelper.PRECISION, RoundingMode.HALF_EVEN);

    private static final int DAY_COUNT = 360;

    /** The reference schedule: 9000 net against a 1000 discount fee, repaid by 200 daily payments of 50. */
    private static final BigDecimal REFERENCE_DAILY_RATE = new BigDecimal("0.0010678144878367712");

    @Test
    void irr_paymentsExactlyRepayTheBalance_returnsExactlyZero() {
        final List<BigDecimal> cashFlows = List.of(new BigDecimal("-200"), new BigDecimal("100"), new BigDecimal("100"));

        assertEquals(BigDecimal.ZERO, TvmFunctions.irr(cashFlows, MC));
    }

    @Test
    void irr_discountFeeSchedule_solvesTheDailyRate() {
        assertCloseTo(REFERENCE_DAILY_RATE, TvmFunctions.irr(referenceCashFlows(), MC), "1E-12");
    }

    @Test
    void irr_seriesShorterThanTwoEntries_rejected() {
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.irr(List.of(BigDecimal.ONE), MC));
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.irr((List<BigDecimal>) null, MC));
    }

    @Test
    void annualize_sameDailyRate_differsBetweenA360AndA365DayYear() {
        final BigDecimal over360 = TvmFunctions.annualize(REFERENCE_DAILY_RATE, DAY_COUNT, MC);
        final BigDecimal over365 = TvmFunctions.annualize(REFERENCE_DAILY_RATE, 365, MC);

        assertEquals(new BigDecimal("0.468451"), over360.setScale(6, RoundingMode.HALF_EVEN));
        assertEquals(new BigDecimal("0.476308"), over365.setScale(6, RoundingMode.HALF_EVEN));
    }

    @Test
    void annualize_zeroPeriodicRate_returnsZero() {
        assertEquals(0, TvmFunctions.annualize(BigDecimal.ZERO, DAY_COUNT, MC).signum());
    }

    @Test
    void annualize_dayCountNotPositiveOrRateAtMinusOne_rejected() {
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.annualize(REFERENCE_DAILY_RATE, 0, MC));
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.annualize(BigDecimal.ONE.negate(), DAY_COUNT, MC));
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.annualize(new BigDecimal("-1.5"), DAY_COUNT, MC));
    }

    /**
     * Exactness, not closeness: a loan with no discount fee solves to a zero IRR, and only an exact zero back keeps the
     * schedule free of a phantom discount factor.
     */
    @Test
    void deannualize_zeroAnnualRate_returnsExactlyZero() {
        assertEquals(BigDecimal.ZERO, TvmFunctions.deannualize(BigDecimal.ZERO, DAY_COUNT, MC));
    }

    @Test
    void deannualize_ofAnAnnualizedRate_returnsTheOriginal() {
        final BigDecimal annual = TvmFunctions.annualize(REFERENCE_DAILY_RATE, DAY_COUNT, MC);

        assertCloseTo(REFERENCE_DAILY_RATE, TvmFunctions.deannualize(annual, DAY_COUNT, MC), "1E-15");
    }

    @Test
    void annualize_ofADeannualizedRate_returnsTheOriginal() {
        final BigDecimal annual = new BigDecimal("0.25");
        final BigDecimal daily = TvmFunctions.deannualize(annual, DAY_COUNT, MC);

        assertCloseTo(annual, TvmFunctions.annualize(daily, DAY_COUNT, MC), "1E-12");
    }

    /**
     * A rate change on a small balance against a largely unearned discount fee solves to a daily rate in the thousands,
     * which over a 360-day year compounds past what a {@code double} holds. Seeding from {@code base.doubleValue()}
     * yielded Infinity there, and the {@link BigDecimal} built from it threw.
     */
    @Test
    void deannualize_annualRateBeyondDoubleRange_solvesWithoutOverflowing() {
        final BigDecimal annualRate = BigDecimal.ONE.scaleByPowerOfTen(1331).subtract(BigDecimal.ONE);

        assertCloseTo(new BigDecimal("4978.9183503647691"), TvmFunctions.deannualize(annualRate, DAY_COUNT, MC), "1E-9");
    }

    /** The same seed underflowed to zero at the other end, and the iteration then divided by it. */
    @Test
    void deannualize_annualRateBelowDoubleRange_solvesWithoutUnderflowing() {
        final BigDecimal annualRate = BigDecimal.ONE.scaleByPowerOfTen(-400).subtract(BigDecimal.ONE);

        assertCloseTo(new BigDecimal("-0.92257363173188733"), TvmFunctions.deannualize(annualRate, DAY_COUNT, MC), "1E-15");
    }

    @Test
    void deannualize_dayCountNotPositiveOrRateAtMinusOne_rejected() {
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.deannualize(new BigDecimal("0.25"), 0, MC));
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.deannualize(BigDecimal.ONE.negate(), DAY_COUNT, MC));
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.deannualize(new BigDecimal("-1.5"), DAY_COUNT, MC));
    }

    @Test
    void discountFactor_zeroDays_isOne() {
        assertEquals(BigDecimal.ONE, TvmFunctions.discountFactor(REFERENCE_DAILY_RATE, 0, MC));
    }

    @Test
    void discountFactor_compoundsTheRateOverTheGivenDays() {
        assertCloseTo(new BigDecimal("0.99800299600499431"), TvmFunctions.discountFactor(new BigDecimal("0.001"), 2, MC), "1E-15");
    }

    @Test
    void discountFactor_negativeDays_rejected() {
        assertThrows(IllegalArgumentException.class, () -> TvmFunctions.discountFactor(REFERENCE_DAILY_RATE, -1, MC));
    }

    private static List<BigDecimal> referenceCashFlows() {
        final BigDecimal dailyPayment = new BigDecimal("50");
        return Stream.concat(Stream.of(new BigDecimal("-9000")), IntStream.range(0, 200).mapToObj(period -> dailyPayment)).toList();
    }

    private static void assertCloseTo(final BigDecimal expected, final BigDecimal actual, final String tolerance) {
        final BigDecimal difference = actual.subtract(expected).abs();
        assertTrue(difference.compareTo(new BigDecimal(tolerance)) < 0,
                () -> "expected " + expected + " within " + tolerance + " but was " + actual + ", off by " + difference);
    }
}
