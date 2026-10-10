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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SemiMonthlyScheduleDatesTest {

    @ParameterizedTest
    @CsvSource({ //
            "10, 25, 2022-01, 25", //
            "1, 30, 2022-02, 28", // a second day the month lacks falls on its last day
            "1, 29, 2024-02, 29", // it does exist in a leap February
            "15, 31, 2022-01, 31", // 31 is the last day, whichever it is
            "15, 31, 2022-02, 28", //
            "15, 31, 2024-02, 29", //
            "15, 31, 2022-04, 30" })
    public void secondDayOfIsCappedAtTheLengthOfTheMonth(final int firstDayOfMonth, final int secondDayOfMonth, final String month,
            final int expectedSecondDay) {
        assertEquals(expectedSecondDay, new SemiMonthlyDueDays(firstDayOfMonth, secondDayOfMonth).secondDayOf(YearMonth.parse(month)));
    }

    @Test
    public void scheduleOfTheFifteenthAndTheLastDayAlternatesBetweenThem() {
        assertEquals(
                List.of(LocalDate.of(2022, 1, 15), LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 15), LocalDate.of(2022, 2, 28),
                        LocalDate.of(2022, 3, 15), LocalDate.of(2022, 3, 31), LocalDate.of(2022, 4, 15), LocalDate.of(2022, 4, 30)),
                scheduleFrom(LocalDate.of(2022, 1, 5), dueDays(15, 31), 8));
    }

    @Test
    public void unevenlySpreadDaysAreKeptAsConfigured() {
        assertEquals(List.of(LocalDate.of(2023, 1, 25), LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 25), LocalDate.of(2023, 3, 1)),
                scheduleFrom(LocalDate.of(2023, 1, 5), dueDays(1, 25), 4));
        assertEquals(List.of(LocalDate.of(2023, 1, 10), LocalDate.of(2023, 1, 20), LocalDate.of(2023, 2, 10), LocalDate.of(2023, 2, 20)),
                scheduleFrom(LocalDate.of(2023, 1, 2), dueDays(10, 20), 4));
    }

    @Test
    public void disbursementAfterBothDueDaysStartsOnTheNextMonth() {
        assertEquals(List.of(LocalDate.of(2022, 2, 1), LocalDate.of(2022, 2, 15), LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 15)),
                scheduleFrom(LocalDate.of(2022, 1, 20), dueDays(1, 15), 4));
    }

    /** The highest first day with the last day of the month: the two due days never meet, not even in February. */
    @Test
    public void everyMonthHoldsExactlyTwoDistinctDueDates() {
        final List<LocalDate> dueDates = scheduleFrom(LocalDate.of(2022, 12, 31),
                dueDays(SemiMonthlyScheduleDates.MAX_FIRST_DAY_OF_MONTH, SemiMonthlyScheduleDates.MAX_SECOND_DAY_OF_MONTH), 24);
        assertEquals(24, Set.copyOf(dueDates).size());
        for (int month = 1; month <= 12; month++) {
            final YearMonth yearMonth = YearMonth.of(2023, month);
            assertEquals(2, dueDates.stream().filter(date -> YearMonth.from(date).equals(yearMonth)).count(), "due dates in " + yearMonth);
        }
        assertEquals(List.of(LocalDate.of(2023, 2, 27), LocalDate.of(2023, 2, 28)), dueDates.subList(2, 4));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 15, 31, 2023-01-15", // already a due day, so it is kept
            "2023-01-16, 15, 31, 2023-01-31", //
            "2023-02-01, 15, 31, 2023-02-15", //
            "2023-01-26, 10, 25, 2023-02-10", //
            "2023-01-02, 1, 25, 2023-01-25" })
    public void onOrAfterKeepsADueDayAndMovesAnyOtherDateForward(final String date, final int firstDayOfMonth, final int secondDayOfMonth,
            final String expected) {
        assertEquals(LocalDate.parse(expected),
                SemiMonthlyScheduleDates.onOrAfter(LocalDate.parse(date), dueDays(firstDayOfMonth, secondDayOfMonth)));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 15, 31, true", //
            "2023-01-31, 15, 31, true", //
            "2023-02-28, 15, 31, true", // the last day of a short month
            "2023-02-28, 1, 30, true", // a second day the month lacks
            "2023-01-30, 15, 31, false", //
            "2023-01-20, 15, 31, false", //
            "2023-01-20, 10, 20, true", //
            "2023-01-15, 10, 20, false" })
    public void isDueDateMatchesOnlyTheTwoConfiguredDays(final String date, final int firstDayOfMonth, final int secondDayOfMonth,
            final boolean expected) {
        assertEquals(expected, SemiMonthlyScheduleDates.isDueDate(LocalDate.parse(date), dueDays(firstDayOfMonth, secondDayOfMonth)));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 1, 2023-01-31", //
            "2023-01-15, 3, 2023-02-28", // lands on the last day of a short month
            "2023-01-02, 1, 2023-01-15", // from a date that is not a due day, the first step is shorter
            "2023-02-28, -2, 2023-01-31" })
    public void plusPeriodsWalksOneDueDateAtATime(final String date, final long periods, final String expected) {
        assertEquals(LocalDate.parse(expected), SemiMonthlyScheduleDates.plusPeriods(LocalDate.parse(date), periods, dueDays(15, 31)));
    }

    @ParameterizedTest
    @CsvSource({ //
            "15, 31, 2023-01-15, 2023-01-31, 1", // 16 days, one whole period
            "15, 31, 2023-01-31, 2023-02-15, 1", // 15 days, one whole period as well
            "15, 31, 2023-02-15, 2023-02-28, 1", // 13 days in February, still one period
            "15, 31, 2023-01-15, 2023-03-15, 4", //
            "15, 31, 2023-01-02, 2023-01-15, 0.8666666667", // irregular first period: 13 of the 15 days of 31 Dec - 15
                                                            // Jan
            "15, 31, 2023-01-15, 2023-01-23, 0.5", // half of the 16 days of 15 Jan - 31 Jan
            "1, 25, 2023-01-01, 2023-01-25, 1", // the long period of an uneven pair is one period
            "1, 25, 2023-01-25, 2023-02-01, 1", // and so is the short one
            "1, 25, 2023-01-13, 2023-01-25, 0.5" }) // 12 of the 24 days of 1 Jan - 25 Jan
    public void periodsBetweenCountsWholePeriodsAndProratesTheRest(final int firstDayOfMonth, final int secondDayOfMonth,
            final String start, final String end, final String expected) {
        final BigDecimal periods = SemiMonthlyScheduleDates.periodsBetween(LocalDate.parse(start), LocalDate.parse(end),
                dueDays(firstDayOfMonth, secondDayOfMonth), new MathContext(10));
        assertEquals(0, new BigDecimal(expected).compareTo(periods), "periods between " + start + " and " + end + " was " + periods);
    }

    /**
     * The rule is stored in the interest recalculation calendars and parsed on every read of the loan, so it has to be
     * a valid iCalendar rule. A FREQ=INVALID rule made the parser throw and the loan unreadable.
     */
    @ParameterizedTest
    @CsvSource({ //
            "1, 15, 'FREQ=MONTHLY;BYMONTHDAY=1,15'", //
            "10, 25, 'FREQ=MONTHLY;BYMONTHDAY=10,25'", //
            "5, 29, 'FREQ=MONTHLY;BYMONTHDAY=5,29'", //
            "15, 31, 'FREQ=MONTHLY;BYMONTHDAY=15,-1'" })
    public void recurrenceIsAValidRuleOnTheTwoDueDays(final int firstDayOfMonth, final int secondDayOfMonth, final String expected) {
        final String recurrence = SemiMonthlyScheduleDates.toRecurrence(dueDays(firstDayOfMonth, secondDayOfMonth));
        assertEquals(expected, recurrence);
        assertNotNull(CalendarUtils.getICalRecur(recurrence), "The rule must parse");
    }

    @Test
    public void aMissingDayFailsInsteadOfFallingBackToADefault() {
        assertThrows(GeneralPlatformDomainRuleException.class, () -> SemiMonthlyScheduleDates.requireDueDays(null, 25));
        assertThrows(GeneralPlatformDomainRuleException.class, () -> SemiMonthlyScheduleDates.requireDueDays(10, null));
        assertEquals(dueDays(10, 25), SemiMonthlyScheduleDates.requireDueDays(10, 25));
    }

    /** Starting from a due date, because that is the only input the ideal disbursement date is derived from. */
    @ParameterizedTest
    @CsvSource({ "14, 29", "1, 31", "27, 30", "1, 2" })
    public void previousDueDateIsTheInverseOfTheNextOne(final int firstDayOfMonth, final int secondDayOfMonth) {
        final SemiMonthlyDueDays dueDays = dueDays(firstDayOfMonth, secondDayOfMonth);
        LocalDate date = SemiMonthlyScheduleDates.next(LocalDate.of(2022, 1, 1), dueDays);
        for (int i = 0; i < 30; i++) {
            final LocalDate next = SemiMonthlyScheduleDates.next(date, dueDays);
            assertEquals(date, SemiMonthlyScheduleDates.previous(next, dueDays),
                    "The date before " + next + " must be the one it was generated from");
            date = next;
        }
    }

    private static SemiMonthlyDueDays dueDays(final int firstDayOfMonth, final int secondDayOfMonth) {
        return new SemiMonthlyDueDays(firstDayOfMonth, secondDayOfMonth);
    }

    private List<LocalDate> scheduleFrom(final LocalDate disbursementDate, final SemiMonthlyDueDays dueDays, final int numberOfRepayments) {
        final List<LocalDate> dueDates = new ArrayList<>();
        LocalDate date = disbursementDate;
        for (int i = 0; i < numberOfRepayments; i++) {
            date = SemiMonthlyScheduleDates.next(date, dueDays);
            dueDates.add(date);
        }
        return dueDates;
    }
}
