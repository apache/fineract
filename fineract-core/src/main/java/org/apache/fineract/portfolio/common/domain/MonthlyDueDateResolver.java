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

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

/**
 * Places monthly due dates on their anchor day under a {@link MonthEndDueDateStrategy}.
 * <p>
 * Kept free of ical4j on purpose, so the embeddable progressive schedule generator can use it.
 * </p>
 * <p>
 * Due dates are chained: each one is the previous one plus the repayment frequency, snapped back to the anchor day.
 * Clamping keeps a date inside the month it stands for (28 February stands for February), so adding a month to it lands
 * in the right month. A date rolled forward does not (1 March stands for February), so it has to be
 * {@link #unroll(LocalDate, LocalDate, PeriodFrequencyType, MonthEndDueDateStrategy) unrolled} before the next month is
 * added, otherwise the anchor is lost and every later period falls on the 1st.
 * </p>
 */
public final class MonthlyDueDateResolver {

    private static final int SHORTEST_MONTH_LENGTH = 28;

    private MonthlyDueDateResolver() {}

    /**
     * Snaps a monthly {@code date} computed by month arithmetic back onto the anchor day of {@code seedDate}.
     * <p>
     * Only acts when the anchor day is one some months lack (29th, 30th, or 31st) and {@code date} sits at the end of
     * its month. With {@link MonthEndDueDateStrategy#LAST_DAY_OF_MONTH} or no strategy this is the historical clamp.
     * </p>
     */
    public static Temporal adjustDate(final Temporal date, final Temporal seedDate, final PeriodFrequencyType frequencyType,
            final MonthEndDueDateStrategy strategy) {
        if (frequencyType.isMonthly() && seedDate.get(ChronoField.DAY_OF_MONTH) > SHORTEST_MONTH_LENGTH
                && date.get(ChronoField.DAY_OF_MONTH) >= SHORTEST_MONTH_LENGTH) {
            int noOfDaysInCurrentMonth = YearMonth.from(date).lengthOfMonth();
            int seedDay = seedDate.get(ChronoField.DAY_OF_MONTH);
            if (MonthEndDueDateStrategy.rollsForward(strategy) && noOfDaysInCurrentMonth < seedDay) {
                return date.with(ChronoField.DAY_OF_MONTH, 1).plus(1, ChronoUnit.MONTHS);
            }
            int adjustedDay = Math.min(noOfDaysInCurrentMonth, seedDay);
            return date.with(ChronoField.DAY_OF_MONTH, adjustedDay);
        }
        return date;
    }

    /**
     * Turns a due date rolled forward back into the last day of the month it stands for, so month arithmetic can be
     * chained from it. Any other date is returned unchanged.
     * <p>
     * A date counts as rolled when the strategy rolls forward, the anchor day is missing from the previous month, and
     * the date is the 1st - which is the only way a 29th/30th/31st anchor produces a 1st.
     * </p>
     */
    public static LocalDate unroll(final LocalDate date, final LocalDate seedDate, final PeriodFrequencyType frequencyType,
            final MonthEndDueDateStrategy strategy) {
        if (date == null || seedDate == null || !MonthEndDueDateStrategy.rollsForward(strategy) || !frequencyType.isMonthly()
                || date.getDayOfMonth() != 1) {
            return date;
        }
        final LocalDate endOfPreviousMonth = date.minusDays(1);
        return endOfPreviousMonth.lengthOfMonth() < seedDate.getDayOfMonth() ? endOfPreviousMonth : date;
    }

    /**
     * Whether a chain of due dates continued from {@code date} keeps snapping back onto the anchor day.
     * <p>
     * The clamp only acts on dates from the 28th on, so a chain that restarts on an earlier day - a due date the user
     * moved to the 1st, say - stays on that day for good. Such a chain never clamps, so it never rolls either, and a
     * 1st in it is a real date rather than a rolled one.
     * </p>
     */
    public static boolean keepsAnchorDayAfter(final LocalDate date) {
        return date.getDayOfMonth() >= SHORTEST_MONTH_LENGTH;
    }

    /**
     * The due date {@code months} months after {@code seedDate}, on the seed's day of month where the target month has
     * it and placed by {@code strategy} where it does not. With {@link MonthEndDueDateStrategy#LAST_DAY_OF_MONTH} or no
     * strategy this equals {@code seedDate.plusMonths(months)}.
     */
    public static LocalDate plusMonths(final LocalDate seedDate, final long months, final MonthEndDueDateStrategy strategy) {
        final LocalDate clamped = seedDate.plusMonths(months);
        if (MonthEndDueDateStrategy.rollsForward(strategy) && clamped.getDayOfMonth() < seedDate.getDayOfMonth()) {
            return clamped.withDayOfMonth(1).plusMonths(1);
        }
        return clamped;
    }
}
