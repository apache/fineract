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
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SemiMonthlyScheduleDatesTest {

    @ParameterizedTest
    @CsvSource({ //
            "1, 2022-01, 15", // the lower anchor pairs with the middle of the month
            "2, 2022-01, 17", //
            "10, 2022-01, 25", //
            "13, 2022-02, 28", // the last day that exists in every month
            "14, 2022-02, 28", // 29 does not exist in a non leap February, so it is capped
            "14, 2024-02, 29", // it does exist in a leap one
            "14, 2022-01, 29", //
            "15, 2022-01, 31", // the upper anchor is the last day, whichever it is
            "15, 2022-02, 28", //
            "15, 2024-02, 29", //
            "15, 2022-04, 30" })
    public void secondDayOfMonthFollowsTheConfiguredRule(final int firstDayOfMonth, final String month, final int expectedSecondDay) {
        assertEquals(expectedSecondDay, SemiMonthlyScheduleDates.secondDayOfMonth(firstDayOfMonth, YearMonth.parse(month)));
    }

    @Test
    public void scheduleOfTheUpperAnchorAlternatesBetweenTheFifteenthAndTheLastDay() {
        assertEquals(
                List.of(LocalDate.of(2022, 1, 15), LocalDate.of(2022, 1, 31), LocalDate.of(2022, 2, 15), LocalDate.of(2022, 2, 28),
                        LocalDate.of(2022, 3, 15), LocalDate.of(2022, 3, 31), LocalDate.of(2022, 4, 15), LocalDate.of(2022, 4, 30)),
                scheduleFrom(LocalDate.of(2022, 1, 5), 15, 8));
    }

    @Test
    public void disbursementAfterBothDueDaysStartsOnTheNextMonth() {
        assertEquals(List.of(LocalDate.of(2022, 2, 1), LocalDate.of(2022, 2, 15), LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 15)),
                scheduleFrom(LocalDate.of(2022, 1, 20), 1, 4));
    }

    @Test
    public void everyMonthHoldsExactlyTwoDueDates() {
        final List<LocalDate> dueDates = scheduleFrom(LocalDate.of(2023, 12, 31), 10, 24);
        assertEquals(24, dueDates.size());
        assertEquals(LocalDate.of(2024, 1, 10), dueDates.getFirst());
        assertEquals(LocalDate.of(2024, 12, 25), dueDates.getLast());
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 15, 2023-01-15", // already a due day, so it is kept
            "2023-01-16, 15, 2023-01-31", //
            "2023-02-01, 15, 2023-02-15", //
            "2023-01-26, 10, 2023-02-10" })
    public void onOrAfterKeepsADueDayAndMovesAnyOtherDateForward(final String date, final int firstDayOfMonth, final String expected) {
        assertEquals(LocalDate.parse(expected), SemiMonthlyScheduleDates.onOrAfter(LocalDate.parse(date), firstDayOfMonth));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 15, true", //
            "2023-01-31, 15, true", //
            "2023-02-28, 15, true", // the last day of a short month
            "2023-01-30, 15, false", //
            "2023-01-20, 15, false", //
            "2023-01-25, 10, true" })
    public void isDueDateMatchesOnlyTheTwoConfiguredDays(final String date, final int firstDayOfMonth, final boolean expected) {
        assertEquals(expected, SemiMonthlyScheduleDates.isDueDate(LocalDate.parse(date), firstDayOfMonth));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 1, 2023-01-31", //
            "2023-01-15, 3, 2023-02-28", // lands on the last day of a short month
            "2023-01-02, 1, 2023-01-15", // from a date that is not a due day, the first step is shorter
            "2023-02-28, -2, 2023-01-31" })
    public void plusPeriodsWalksOneDueDateAtATime(final String date, final long periods, final String expected) {
        assertEquals(LocalDate.parse(expected), SemiMonthlyScheduleDates.plusPeriods(LocalDate.parse(date), periods, 15));
    }

    @ParameterizedTest
    @CsvSource({ //
            "2023-01-15, 2023-01-31, 1", // 16 days, one whole period
            "2023-01-31, 2023-02-15, 1", // 15 days, one whole period as well
            "2023-02-15, 2023-02-28, 1", // 13 days in February, still one period
            "2023-01-15, 2023-03-15, 4", //
            "2023-01-02, 2023-01-15, 0.8666666667", // irregular first period: 13 of the 15 days of 31 Dec - 15 Jan
            "2023-01-15, 2023-01-23, 0.5" }) // half of the 16 days of 15 Jan - 31 Jan
    public void periodsBetweenCountsWholePeriodsAndProratesTheRest(final String start, final String end, final String expected) {
        final BigDecimal periods = SemiMonthlyScheduleDates.periodsBetween(LocalDate.parse(start), LocalDate.parse(end), 15,
                new MathContext(10));
        assertEquals(0, new BigDecimal(expected).compareTo(periods), "periods between " + start + " and " + end + " was " + periods);
    }

    /**
     * The rule is stored in the interest recalculation calendars and parsed on every read of the loan, so it has to be
     * a valid iCalendar rule. A FREQ=INVALID rule made the parser throw and the loan unreadable.
     */
    @ParameterizedTest
    @CsvSource({ //
            "1, 'FREQ=MONTHLY;BYMONTHDAY=1,15'", //
            "10, 'FREQ=MONTHLY;BYMONTHDAY=10,25'", //
            "14, 'FREQ=MONTHLY;BYMONTHDAY=14,29'", //
            "15, 'FREQ=MONTHLY;BYMONTHDAY=15,-1'" })
    public void recurrenceIsAValidRuleOnTheTwoDueDays(final int firstDayOfMonth, final String expected) {
        final String recurrence = SemiMonthlyScheduleDates.toRecurrence(firstDayOfMonth);
        assertEquals(expected, recurrence);
        assertNotNull(CalendarUtils.getICalRecur(recurrence), "The rule must parse");
    }

    @Test
    public void aMissingFirstDayFailsInsteadOfFallingBackToADefault() {
        assertThrows(GeneralPlatformDomainRuleException.class, () -> SemiMonthlyScheduleDates.requireFirstDayOfMonth(null));
        assertEquals(10, SemiMonthlyScheduleDates.requireFirstDayOfMonth(10));
    }

    /** Starting from a due date, because that is the only input the ideal disbursement date is derived from. */
    @Test
    public void previousDueDateIsTheInverseOfTheNextOne() {
        final int firstDayOfMonth = 14;
        LocalDate date = SemiMonthlyScheduleDates.next(LocalDate.of(2022, 1, 1), firstDayOfMonth);
        for (int i = 0; i < 30; i++) {
            final LocalDate next = SemiMonthlyScheduleDates.next(date, firstDayOfMonth);
            assertEquals(date, SemiMonthlyScheduleDates.previous(next, firstDayOfMonth),
                    "The date before " + next + " must be the one it was generated from");
            date = next;
        }
    }

    private List<LocalDate> scheduleFrom(final LocalDate disbursementDate, final int firstDayOfMonth, final int numberOfRepayments) {
        final List<LocalDate> dueDates = new ArrayList<>();
        LocalDate date = disbursementDate;
        for (int i = 0; i < numberOfRepayments; i++) {
            date = SemiMonthlyScheduleDates.next(date, firstDayOfMonth);
            dueDates.add(date);
        }
        return dueDates;
    }
}
