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

    @BeforeAll
    static void setUp() {
        moneyHelper.when(MoneyHelper::getMathContext).thenReturn(new MathContext(12, RoundingMode.UP));
        moneyHelper.when(MoneyHelper::getRoundingMode).thenReturn(RoundingMode.UP);
        tenDollars = Money.of(CURRENCY, BigDecimal.TEN);
        oneDollar = Money.of(CURRENCY, BigDecimal.ONE);
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
}
