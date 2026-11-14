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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.Test;

class SavingsAccountLockPeriodCalculatorTest {

    private final SavingsAccountLockPeriodCalculator underTest = new SavingsAccountLockPeriodCalculator();

    @Test
    void calculateDateAccountIsLockedUntil_withDays() {
        LocalDate activationDate = LocalDate.of(2026, 1, 1);
        LocalDate result = underTest.calculateDateAccountIsLockedUntil(activationDate, 15, PeriodFrequencyType.DAYS);
        assertEquals(LocalDate.of(2026, 1, 16), result);
    }

    @Test
    void calculateDateAccountIsLockedUntil_withWeeks() {
        LocalDate activationDate = LocalDate.of(2026, 1, 1);
        LocalDate result = underTest.calculateDateAccountIsLockedUntil(activationDate, 3, PeriodFrequencyType.WEEKS);
        assertEquals(LocalDate.of(2026, 1, 22), result);
    }

    @Test
    void calculateDateAccountIsLockedUntil_withMonths() {
        LocalDate activationDate = LocalDate.of(2026, 1, 15);
        LocalDate result = underTest.calculateDateAccountIsLockedUntil(activationDate, 6, PeriodFrequencyType.MONTHS);
        assertEquals(LocalDate.of(2026, 7, 15), result);
    }

    @Test
    void calculateDateAccountIsLockedUntil_withYears() {
        LocalDate activationDate = LocalDate.of(2026, 3, 10);
        LocalDate result = underTest.calculateDateAccountIsLockedUntil(activationDate, 2, PeriodFrequencyType.YEARS);
        assertEquals(LocalDate.of(2028, 3, 10), result);
    }

    @Test
    void calculateDateAccountIsLockedUntil_withLeapYear() {
        // Feb 29 on leap year + 1 year should result in Feb 28
        LocalDate leapDay = LocalDate.of(2024, 2, 29);
        LocalDate result = underTest.calculateDateAccountIsLockedUntil(leapDay, 1, PeriodFrequencyType.YEARS);
        assertEquals(LocalDate.of(2025, 2, 28), result);
    }

    @Test
    void calculateDateAccountIsLockedUntil_withNullInputs_returnsNull() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        assertNull(underTest.calculateDateAccountIsLockedUntil(null, 5, PeriodFrequencyType.DAYS));
        assertNull(underTest.calculateDateAccountIsLockedUntil(date, null, PeriodFrequencyType.DAYS));
        assertNull(underTest.calculateDateAccountIsLockedUntil(date, 5, null));
    }

    @Test
    void calculateDateAccountIsLockedUntil_withInvalidOrSemiMonthly_returnsNull() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        assertNull(underTest.calculateDateAccountIsLockedUntil(date, 5, PeriodFrequencyType.INVALID));
        assertNull(underTest.calculateDateAccountIsLockedUntil(date, 5, PeriodFrequencyType.SEMI_MONTHLY));
    }
}
