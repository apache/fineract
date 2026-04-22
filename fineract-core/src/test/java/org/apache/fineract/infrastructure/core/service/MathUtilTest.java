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

package org.apache.fineract.infrastructure.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import org.junit.jupiter.api.Test;

class MathUtilTest {

    @Test
    void nullToDefaultReturnsDefaultWhenValueNull() {
        assertEquals(5L, MathUtil.nullToDefault(null, 5L));
    }

    @Test
    void nullToDefaultReturnsValueWhenNotNull() {
        assertEquals(10L, MathUtil.nullToDefault(10L, 5L));
    }

    @Test
    void nullToZeroLongReturnsZeroWhenNull() {
        assertEquals(0L, MathUtil.nullToZero((Long) null));
    }

    @Test
    void zeroToNullReturnsNullWhenZero() {
        assertNull(MathUtil.zeroToNull(0L));
    }

    @Test
    void negativeToZeroReturnsZeroForNegativeValue() {
        assertEquals(0L, MathUtil.negativeToZero(-10L));
    }

    @Test
    void negativeToZeroReturnsSameValueForPositive() {
        assertEquals(20L, MathUtil.negativeToZero(20L));
    }

    @Test
    void isGreaterThanZeroReturnsTrueForPositive() {
        assertTrue(MathUtil.isGreaterThanZero(5L));
    }

    @Test
    void isLessThanZeroReturnsTrueForNegative() {
        assertTrue(MathUtil.isLessThanZero(-5L));
    }

    @Test
    void addHandlesNullValues() {
        assertEquals(10L, MathUtil.add(null, 10L));
    }

    @Test
    void addReturnsSumOfValues() {
        assertEquals(30L, MathUtil.add(10L, 20L));
    }

    @Test
    void subtractReturnsCorrectDifference() {
        assertEquals(15L, MathUtil.subtract(20L, 5L));
    }

    @Test
    void absReturnsPositiveValue() {
        assertEquals(25L, MathUtil.abs(-25L));
    }

    @Test
    void bigDecimalAddReturnsCorrectSum() {
        BigDecimal result = MathUtil.add(new BigDecimal("10.5"), new BigDecimal("5.5"), new MathContext(10));
        assertEquals(0, result.compareTo(new BigDecimal("16.0")));
    }

    @Test
    void bigDecimalSubtractReturnsCorrectDifference() {
        BigDecimal result = MathUtil.subtract(new BigDecimal("20"), new BigDecimal("5"), new MathContext(10));
        assertEquals(0, result.compareTo(new BigDecimal("15")));
    }

    @Test
    void percentageOfCalculatesCorrectValue() {
        BigDecimal result = MathUtil.percentageOf(new BigDecimal("200"), new BigDecimal("10"), new MathContext(10));
        assertEquals(0, result.compareTo(new BigDecimal("20")));
    }

    @Test
    void percentageOfReturnsZeroWhenValueZero() {
        BigDecimal result = MathUtil.percentageOf(BigDecimal.ZERO, new BigDecimal("10"), new MathContext(10));
        assertEquals(0, result.compareTo(BigDecimal.ZERO));
    }

    @Test
    void stripTrailingZerosRemovesExtraZeros() {
        BigDecimal result = MathUtil.stripTrailingZeros(new BigDecimal("10.5000"));
        assertEquals(0, result.compareTo(new BigDecimal("10.5")));
    }

    @Test
    void stripTrailingZerosReturnsNullWhenInputNull() {
        assertNull(MathUtil.stripTrailingZeros(null));
    }

    @Test
    void subtractHandlesNullSecond() {
        assertEquals(10L, MathUtil.subtract(10L, (Long) null));
    }

    @Test
    void addMultipleValuesWorks() {
        assertEquals(60L, MathUtil.add(10L, 20L, 30L));
    }

    @Test
    void subtractToZeroNeverNegative() {
        assertEquals(0L, MathUtil.subtractToZero(10L, 20L));
    }
}
