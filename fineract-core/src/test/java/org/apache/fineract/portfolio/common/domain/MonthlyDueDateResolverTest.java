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
package org.apache.fineract.portfolio.common.domain;

import static org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy.FIRST_DAY_OF_NEXT_MONTH;
import static org.apache.fineract.portfolio.common.domain.MonthEndDueDateStrategy.LAST_DAY_OF_MONTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class MonthlyDueDateResolverTest {

    /**
     * {@code date} is what month arithmetic produced from the previous due date, {@code seed} carries the anchor day.
     */
    @ParameterizedTest(name = "{0} anchored on {1}: clamps to {2}, rolls to {3}")
    @CsvSource({ //
            "2025-02-28, 2025-01-31, 2025-02-28, 2025-03-01", // 31st anchor in a short February
            "2025-02-28, 2025-01-30, 2025-02-28, 2025-03-01", // 30th anchor in a short February
            "2025-02-28, 2025-01-29, 2025-02-28, 2025-03-01", // 29th anchor in a short February
            "2024-02-29, 2024-01-29, 2024-02-29, 2024-02-29", // 29th anchor exists in a leap February
            "2024-02-29, 2024-01-30, 2024-02-29, 2024-03-01", // 30th anchor is still missing in a leap February
            "2025-04-30, 2025-03-31, 2025-04-30, 2025-05-01", // 31st anchor in a 30-day month
            "2025-05-30, 2025-04-30, 2025-05-30, 2025-05-30", // 30th anchor in a 31-day month is never moved
            "2025-03-28, 2025-01-31, 2025-03-31, 2025-03-31", // anchor restored once the month is long enough
            "2025-02-28, 2025-01-28, 2025-02-28, 2025-02-28", // 28th anchor exists in every month
            "2025-02-15, 2025-01-31, 2025-02-15, 2025-02-15", // a date away from the month end is left alone
    })
    void adjustsMonthlyDateOntoAnchorDay(final LocalDate date, final LocalDate seed, final LocalDate clamped, final LocalDate rolled) {
        assertEquals(clamped, MonthlyDueDateResolver.adjustDate(date, seed, PeriodFrequencyType.MONTHS, LAST_DAY_OF_MONTH));
        assertEquals(clamped, MonthlyDueDateResolver.adjustDate(date, seed, PeriodFrequencyType.MONTHS, null),
                "No strategy behaves as the clamp");
        assertEquals(rolled, MonthlyDueDateResolver.adjustDate(date, seed, PeriodFrequencyType.MONTHS, FIRST_DAY_OF_NEXT_MONTH));
    }

    @ParameterizedTest
    @EnumSource(value = PeriodFrequencyType.class, names = { "DAYS", "WEEKS", "YEARS" })
    void leavesOtherFrequenciesAlone(final PeriodFrequencyType frequency) {
        final LocalDate date = LocalDate.of(2025, 2, 28);
        assertEquals(date, MonthlyDueDateResolver.adjustDate(date, LocalDate.of(2025, 1, 31), frequency, FIRST_DAY_OF_NEXT_MONTH));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(MonthEndDueDateStrategy.class)
    void keepsTimeOfDayForDateTimes(final MonthEndDueDateStrategy strategy) {
        final LocalDateTime date = LocalDateTime.of(2025, 2, 28, 10, 15);
        final LocalDateTime expected = strategy == FIRST_DAY_OF_NEXT_MONTH ? LocalDateTime.of(2025, 3, 1, 10, 15) : date;
        assertEquals(expected,
                MonthlyDueDateResolver.adjustDate(date, LocalDateTime.of(2025, 1, 31, 10, 15), PeriodFrequencyType.MONTHS, strategy));
    }

    @ParameterizedTest(name = "{0} anchored on {1} unrolls to {2}")
    @CsvSource({ //
            "2025-03-01, 2025-01-31, 2025-02-28", // rolled out of February
            "2024-03-01, 2024-01-30, 2024-02-29", // rolled out of a leap February
            "2025-05-01, 2025-03-31, 2025-04-30", // rolled out of April
            "2025-04-01, 2025-01-31, 2025-04-01", // March has a 31st, so 1 April is not a rolled date
            "2025-03-01, 2025-01-28, 2025-03-01", // a 28th anchor never rolls
            "2025-03-31, 2025-01-31, 2025-03-31", // not the 1st
    })
    void unrollsOnlyDatesThatWereRolledForward(final LocalDate date, final LocalDate seed, final LocalDate expected) {
        assertEquals(expected, MonthlyDueDateResolver.unroll(date, seed, PeriodFrequencyType.MONTHS, FIRST_DAY_OF_NEXT_MONTH));
    }

    @Test
    void neverUnrollsUnlessRollingForward() {
        final LocalDate date = LocalDate.of(2025, 3, 1);
        final LocalDate seed = LocalDate.of(2025, 1, 31);
        assertEquals(date, MonthlyDueDateResolver.unroll(date, seed, PeriodFrequencyType.MONTHS, LAST_DAY_OF_MONTH));
        assertEquals(date, MonthlyDueDateResolver.unroll(date, seed, PeriodFrequencyType.MONTHS, null));
        assertEquals(date, MonthlyDueDateResolver.unroll(date, seed, PeriodFrequencyType.WEEKS, FIRST_DAY_OF_NEXT_MONTH));
    }

    @ParameterizedTest(name = "{0} + {1} months: clamps to {2}, rolls to {3}")
    @CsvSource({ //
            "2025-01-31, 1, 2025-02-28, 2025-03-01", // the first of the next month, not 3 March
            "2025-01-31, 2, 2025-03-31, 2025-03-31", // anchor survives the roll
            "2025-01-31, 3, 2025-04-30, 2025-05-01", //
            "2024-02-29, 11, 2025-01-29, 2025-01-29", //
            "2024-02-29, 12, 2025-02-28, 2025-03-01", // a leap-day anchor in a common year
            "2025-01-15, 1, 2025-02-15, 2025-02-15", //
    })
    void movesOnWholeMonthsFromAnchor(final LocalDate seed, final long months, final LocalDate clamped, final LocalDate rolled) {
        assertEquals(clamped, MonthlyDueDateResolver.plusMonths(seed, months, LAST_DAY_OF_MONTH));
        assertEquals(clamped, MonthlyDueDateResolver.plusMonths(seed, months, null));
        assertEquals(rolled, MonthlyDueDateResolver.plusMonths(seed, months, FIRST_DAY_OF_NEXT_MONTH));
    }

    @ParameterizedTest(name = "a chain restarted on {0} keeps the anchor day: {1}")
    @CsvSource({ //
            "2025-05-01, false", // a due date moved to the 1st stays on the 1st
            "2025-05-27, false", //
            "2025-05-28, true", // the clamp snaps dates from the 28th on back to the anchor day
            "2025-04-30, true", //
            "2025-05-31, true", //
    })
    void keepsAnchorDayOnlyFromThe28th(final LocalDate restartDate, final boolean keepsAnchorDay) {
        assertEquals(keepsAnchorDay, MonthlyDueDateResolver.keepsAnchorDayAfter(restartDate));
    }
}
