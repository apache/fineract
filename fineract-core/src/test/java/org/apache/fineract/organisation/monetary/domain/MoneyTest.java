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
package org.apache.fineract.organisation.monetary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MoneyTest {

    private static MockedStatic<MoneyHelper> moneyHelper = Mockito.mockStatic(MoneyHelper.class);
    private static final MonetaryCurrency CURRENCY = new MonetaryCurrency("USD", 2, null);
    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    private static Money tenDollars;
    private static Money oneDollar;
    private static Money zeroDollar;
    private static Money negativeFiveDollars;

    @BeforeAll
    static void setUp() {
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.UP));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.UP);
        tenDollars = Money.of(CURRENCY, BigDecimal.TEN);
        oneDollar = Money.of(CURRENCY, BigDecimal.ONE);
        zeroDollar = Money.of(CURRENCY, BigDecimal.ZERO);
        negativeFiveDollars = Money.of(CURRENCY, new BigDecimal("-5"));
    }

    @AfterAll
    static void tearDown() {
        moneyHelper.close();
    }

    @Test
    void testPlusWithNullInIterable() {
        List<Money> monies = Arrays.asList(oneDollar, null, oneDollar);
        Money result = tenDollars.plus(monies);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("12.00")), "Should sum non-null values and skip nulls");
    }

    @Test
    void testPlusWithEmptyIterable() {
        List<Money> emptyList = Collections.emptyList();
        Money result = tenDollars.plus(emptyList);
        assertEquals(0, result.getAmount().compareTo(BigDecimal.TEN), "Should return the same amount when adding empty list");
    }

    @Test
    void testPlusWithNullMoney() {
        Money result = tenDollars.plus((Money) null, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(BigDecimal.TEN), "Should return the same amount when adding null Money");
    }

    @Test
    void testMinusWithNullMoney() {
        Money result = tenDollars.minus((Money) null, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(BigDecimal.TEN), "Should return the same amount when subtracting null Money");
    }

    @Test
    void testAddWithNullMoney() {
        Money result = tenDollars.add((Money) null, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(BigDecimal.TEN), "Should return the same amount when adding null Money");
    }

    @Test
    void testPlusMoney() {
        Money result = tenDollars.plus(oneDollar, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("11.00")), "Should correctly add two Money amounts");
    }

    @Test
    void testMinusMoney() {
        Money result = tenDollars.minus(oneDollar, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("9.00")), "Should correctly subtract two Money amounts");
    }

    @Test
    void testAddMoney() {
        Money result = tenDollars.add(oneDollar, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("11.00")), "Should correctly add two Money amounts");
    }

    // @formatter:off
    @ParameterizedTest(name = "plus(double {0}) = {1}")
    @CsvSource({
            "0.0,   10.00",
            "1.0,   11.00",
            "0.01,  10.01",
            "5.5,   15.50",
            "10.0,  20.00",
            "0.99,  10.99",
            "100.0, 110.00",
            "999999.99, 1000009.99",
            "-1.0, 9.00",
            "-0.01,9.99",
            "-10.0,0.00",
            "-10.01,-0.01",
            "-100.0,-90.00",
            "-999999.99,-999989.99",
            "0.1, 10.10",
            "0.2, 10.20",
            "0.10000000000000001, 10.10",
    })
    // @formatter:on
    void testPlusDouble(double amountToAdd, BigDecimal expected) {
        Money result = tenDollars.plus(amountToAdd);
        assertEquals(0, result.getAmount().compareTo(expected),
                () -> String.format("tenDollars.plus(%s): expected %s but got %s", amountToAdd, expected, result.getAmount()));
    }

    @Test
    void testTotalVarargs() {
        Money result = Money.total(oneDollar, oneDollar, oneDollar);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("3.00")));
    }

    @Test
    void testTotalVarargsThrowsOnEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, Money::total);
        assertEquals("Money array must not be empty", exception.getMessage());
    }

    @Test
    void testTotalIterable() {
        List<Money> monies = Arrays.asList(tenDollars, oneDollar);
        Money result = Money.total(monies);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("11.00")));
    }

    @Test
    void testTotalIterableThrowsOnEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Money.total(Collections.emptyList()));
        assertEquals("Money iterator must not be empty", exception.getMessage());
    }

    @Test
    void testTotalIterableSingleElement() {
        Money result = Money.total(Collections.singletonList(tenDollars));
        assertEquals(0, result.getAmount().compareTo(BigDecimal.TEN));
    }

    // @formatter:off
    @ParameterizedTest
    @CsvSource({
            "76.0, 50, 100.0",
            "75.0, 100, 100.0",
            "0.0, 50, 0.0",
            "74.0, 50, 50.0",
    })
    // @formatter:on
    void testRoundToMultiplesOfDouble(double value, int multiple, double expected) {
        double result = Money.roundToMultiplesOf(value, multiple);
        assertEquals(expected, result);
    }

    @Test
    void testRoundToMultiplesOfDoubleNegativeValuePositiveMultipleReturnsNaN() {
        double result = Money.roundToMultiplesOf(-75.0, 50);
        assertTrue(Double.isNaN(result));
    }

    // @formatter:off
    @ParameterizedTest(name = "roundToMultiplesOf({0}, {1}) = {2}")
    @CsvSource({
            "100,  50,  100",
            "50,   50,  50",
            "0,    50,  0",
            "75,   1,   75",
            "99,   1,   99",
            "300,  100, 300",
            "40,   20,  40",
            "75,   0,   75",
            "100,  0,   100",
            "75, -50, 75",
    })
    // @formatter:on
    void testRoundToMultiplesOfBigDecimal(BigDecimal existingVal, Integer inMultiplesOf, BigDecimal expected) {
        BigDecimal result = Money.roundToMultiplesOf(existingVal, inMultiplesOf);
        assertEquals(0, result.compareTo(expected),
                () -> String.format("roundToMultiplesOf(%s, %s): expected %s but got %s", existingVal, inMultiplesOf, expected, result));
    }

    @Test
    void testRoundToMultiplesOfMoney() {
        MonetaryCurrency currencyNoDecimal = new MonetaryCurrency("USD", 0, 50);
        Money money = Money.of(currencyNoDecimal, new BigDecimal("75"));
        Money result = Money.roundToMultiplesOf(money, 50);
        assertEquals("USD", result.getCurrencyCode());
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("100")));
    }

    @Test
    void testRoundToMultiplesOfMoneyWithMathContext() {
        MonetaryCurrency currencyNoDecimal = new MonetaryCurrency("USD", 0, 50);
        Money money = Money.of(currencyNoDecimal, new BigDecimal("75"));
        Money result = Money.roundToMultiplesOf(money, 50, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("100")));
    }

    @Test
    void testRoundToMultiplesOfMoneyZeroMultiple() {
        Money money = Money.of(CURRENCY, new BigDecimal("75"));
        Money result = Money.roundToMultiplesOf(money, 0);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("75.00")));
    }

    // @formatter:off
    @ParameterizedTest(name = "ceiling({0}, {1}) = NaN")
    @CsvSource({
            "-75.0,   50.0",
            "-1.0,    1.0",
            "-0.01,   0.01",
            "-1000.0, 0.001",
            "75.0,    -50.0",
            "1.0,     -1.0",
            "0.01,    -0.01",
            "1000.0,  -0.001",
    })
    // @formatter:on
    void testCeilingReturnsNaN(double n, double s) {
        assertTrue(Double.isNaN(Money.ceiling(n, s)), () -> String.format("ceiling(%s, %s) should be NaN", n, s));
    }

    // @formatter:off
    @ParameterizedTest(name = "ceiling({0}, {1}) = {2}")
    @CsvSource({
            "0.0,    50.0,   0.0",
            "0.0,   -50.0,   0.0",
            "0.0,    0.0,    0.0",
            "0.0,    1.0,    0.0",
            "50.0,   0.0,    0.0",
            "0.01,   0.0,    0.0",
            "50.0,   50.0,   50.0",
            "100.0,  50.0,   100.0",
            "75.0,   25.0,   75.0",
            "51.0,   50.0,   100.0",
            "75.0,   50.0,   100.0",
            "99.0,   50.0,   100.0",
            "1.0,    50.0,   50.0",
            "26.0,   25.0,   50.0",
            "-50.0,  -50.0,  -50.0",
            "1.0,    0.3,    1.2",
            "0.1,    0.3,    0.3",
            "1000.0, 50.0,   1000.0",
            "1001.0, 50.0,   1050.0",
    })
    // @formatter:on
    void testCeilingReturnsDouble(double n, double s, double expected) {
        assertEquals(expected, Money.ceiling(n, s), 0.0001, () -> String.format("ceiling(%s, %s): expected %s", n, s, expected));
    }

    // @formatter:off
    @ParameterizedTest(name = "floor({0}, {1}) = NaN")
    @CsvSource({
            "-75.0,   50.0",
            "-1.0,    1.0",
            "-0.01,   0.01",
            "-1000.0, 0.001",
            "-50.0,   50.0",
            "75.0,    -50.0",
            "1.0,     -1.0",
            "0.01,    -0.01",
            "1000.0,  -0.001",
            "50.0,    -50.0",
            "1.0,     0.0",
            "75.0,    0.0",
            "-1.0,    0.0",
            "-75.0,   0.0",
            "0.001,   0.0",
            "-0.001,  0.0",
    })
    // @formatter:on
    void testFloorReturnsNaN(double n, double s) {
        assertTrue(Double.isNaN(Money.floor(n, s)), () -> String.format("floor(%s, %s) should be NaN", n, s));
    }

    // @formatter:off
    @ParameterizedTest(name = "floor({0}, {1}) = {2}")
    @CsvSource({
            "0.0,    50.0,    0.0",
            "0.0,   -50.0,    0.0",
            "0.0,    1.0,     0.0",
            "0.0,    0.0,     0.0",
            "50.0,   50.0,    50.0",
            "100.0,  50.0,    100.0",
            "75.0,   25.0,    75.0",
            "200.0,  100.0,   200.0",
            "51.0,   50.0,    50.0",
            "75.0,   50.0,    50.0",
            "99.0,   50.0,    50.0",
            "1.0,    50.0,    0.0",
            "24.0,   25.0,    0.0",
            "26.0,   25.0,    25.0",
            "149.0,  50.0,    100.0",
            "-50.0,  -50.0,   -50.0",
            "-75.0,  -50.0,   -50.0",
            "-100.0, -50.0,   -100.0",
            "-1.0,   -50.0,    0.0",
            "1.0,    0.3,     0.9",
            "0.5,    0.3,     0.3",
            "0.1,    0.3,     0.0",
            "1000.0,  50.0,   1000.0",
            "1001.0,  50.0,   1000.0",
            "1049.0,  50.0,   1000.0",
            "1050.0,  50.0,   1050.0",
    })
    // @formatter:on
    void testFloorReturnsDouble(double n, double s, double expected) {
        assertEquals(expected, Money.floor(n, s), 0.0001, () -> String.format("floor(%s, %s): expected %s", n, s, expected));
    }

    @Test
    void testGetAmountDefaultedToNullIfZeroReturnsNullForZero() {
        assertNull(zeroDollar.getAmountDefaultedToNullIfZero());
    }

    @Test
    void testGetAmountDefaultedToNullIfZeroReturnsAmountForNonZero() {
        assertNotNull(tenDollars.getAmountDefaultedToNullIfZero());
        assertEquals(0, tenDollars.getAmountDefaultedToNullIfZero().compareTo(BigDecimal.TEN));
    }

    @Test
    void testDividedByBigDecimalOne() {
        Money result = tenDollars.dividedBy(BigDecimal.ONE, MATH_CONTEXT);
        assertSame(tenDollars, result, "Dividing by 1 should return the same instance");
    }

    @Test
    void testDividedByBigDecimal() {
        Money result = tenDollars.dividedBy(new BigDecimal("2"), MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testDividedByDoubleOne() {
        Money result = tenDollars.dividedBy(1.0, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testDividedByDouble() {
        Money result = tenDollars.dividedBy(2.0, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testDividedByLongOneWithMathContext() {
        Money result = tenDollars.dividedBy(1L, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testDividedByLongWithMathContext() {
        Money result = tenDollars.dividedBy(2L, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testDividedByLongOneNoMathContext() {
        Money result = tenDollars.dividedBy(1L);
        assertSame(tenDollars, result);
    }

    @Test
    void testDividedByLongNoMathContext() {
        Money result = tenDollars.dividedBy(2L);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testMultipliedByBigDecimalOne() {
        Money result = tenDollars.multipliedBy(BigDecimal.ONE);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultipliedByBigDecimal() {
        Money result = tenDollars.multipliedBy(new BigDecimal("3"));
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void testMultipliedByBigDecimalWithMathContext() {
        Money result = tenDollars.multipliedBy(new BigDecimal("3"), MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void testMultipliedByBigDecimalOneWithMathContext() {
        Money result = tenDollars.multipliedBy(BigDecimal.ONE, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultipliedByDoubleOne() {
        Money result = tenDollars.multipliedBy(1.0);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultipliedByDouble() {
        Money result = tenDollars.multipliedBy(2.5);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("25.00")));
    }

    @Test
    void testMultipliedByLongOne() {
        Money result = tenDollars.multipliedBy(1L);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultipliedByLong() {
        Money result = tenDollars.multipliedBy(3L);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void testMultipliedByLongWithMathContext() {
        Money result = tenDollars.multipliedBy(3L, MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("30.00")));
    }

    @Test
    void testMultipliedByLongOneWithMathContext() {
        Money result = tenDollars.multipliedBy(1L, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultiplyRetainScaleBigDecimalOne() {
        Money result = tenDollars.multiplyRetainScale(BigDecimal.ONE, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testMultiplyRetainScaleBigDecimal() {
        Money result = tenDollars.multiplyRetainScale(new BigDecimal("1.5"), MATH_CONTEXT);
        assertEquals(2, result.getAmount().scale(), "Scale should be retained at currency decimal places");
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("15.00")));
    }

    @Test
    void testMultiplyRetainScaleDouble() {
        Money result = tenDollars.multiplyRetainScale(1.5, MATH_CONTEXT);
        assertEquals(2, result.getAmount().scale());
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("15.00")));
    }

    @Test
    void testMultiplyRetainScaleDoubleOne() {
        Money result = tenDollars.multiplyRetainScale(1.0, MATH_CONTEXT);
        assertSame(tenDollars, result);
    }

    @Test
    void testPercentageOf() {
        Money result = tenDollars.percentageOf(new BigDecimal("50"), MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testPercentageOfHundred() {
        Money result = tenDollars.percentageOf(new BigDecimal("100"), MATH_CONTEXT);
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("10.00")));
    }

    @Test
    void testPercentageOfZero() {
        Money result = tenDollars.percentageOf(BigDecimal.ZERO, MATH_CONTEXT);
        assertTrue(result.isZero());
    }

    @Test
    void testNegatedPositiveMoney() {
        Money result = tenDollars.negated();
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("-10.00")));
    }

    @Test
    void testNegatedZeroReturnsSelf() {
        Money result = zeroDollar.negated();
        assertSame(zeroDollar, result);
    }

    @Test
    void testNegatedWithMathContext() {
        Money result = tenDollars.negated(MATH_CONTEXT);
        assertTrue(result.isLessThanZero());
    }

    @Test
    void testNegatedNegativeMoney() {
        Money result = negativeFiveDollars.negated();
        assertTrue(result.isGreaterThanZero());
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testAbsPositiveMoney() {
        Money result = tenDollars.abs();
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("10.00")));
    }

    @Test
    void testAbsNegativeMoney() {
        Money result = negativeFiveDollars.abs();
        assertTrue(result.isGreaterThanZero());
        assertEquals(0, result.getAmount().compareTo(new BigDecimal("5.00")));
    }

    @Test
    void testAbsZero() {
        Money result = zeroDollar.abs();
        assertTrue(result.isZero());
    }

    @Test
    void testAbsWithMathContext() {
        Money result = negativeFiveDollars.abs(MATH_CONTEXT);
        assertTrue(result.isGreaterThanZero());
    }

    @Test
    void testZeroInstanceMethod() {
        Money result = tenDollars.zero();
        assertTrue(result.isZero());
        assertEquals("USD", result.getCurrencyCode());
    }

    @Test
    void testZeroInstanceMethodWithMathContext() {
        Money result = tenDollars.zero(MATH_CONTEXT);
        assertTrue(result.isZero());
    }

    @Test
    void testGetMcReturnsMathContext() {
        assertNotNull(tenDollars.getMc());
    }

    @Test
    void testCopy() {
        Money copy = tenDollars.copy();
        assertEquals(0, copy.getAmount().compareTo(tenDollars.getAmount()));
        assertEquals(tenDollars.getCurrencyCode(), copy.getCurrencyCode());
        assertNotSame(tenDollars, copy);
    }

    @Test
    void testCopyWithBigDecimal() {
        Money copy = tenDollars.copy(new BigDecimal("25"));
        assertEquals(0, copy.getAmount().compareTo(new BigDecimal("25.00")));
        assertEquals("USD", copy.getCurrencyCode());
    }

    @Test
    void testCopyWithDouble() {
        Money copy = tenDollars.copy(5.0);
        assertEquals(0, copy.getAmount().compareTo(new BigDecimal("5.00")));
    }
}
