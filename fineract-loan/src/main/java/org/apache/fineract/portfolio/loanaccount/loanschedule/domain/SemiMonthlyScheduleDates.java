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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.YearMonth;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.service.DateUtils;

/**
 * Resolves the two due dates of a {@code SEMI_MONTHLY} repayment frequency.
 * <p>
 * A semi-monthly loan is repaid twice every calendar month, which always yields 24 repayments a year. Only the first
 * day is configured; the second one is derived so that the two days can never be out of order or duplicated:
 * <ul>
 * <li>day {@code 1} pairs with day {@code 15}, the middle of the month</li>
 * <li>day {@code 15} pairs with the last day of the month, whichever it is for that month</li>
 * <li>every day in between pairs with itself plus fifteen days</li>
 * </ul>
 * The derived day never exceeds the length of the month. In practice that cap only applies to a first day of 14 in a
 * non leap February, where the 29th does not exist and the second repayment falls on the 28th instead.
 * <p>
 * Capping the first day at 15 is what keeps both repayments inside the same calendar month: a later first day would
 * push its pair into the following month and break the two repayments per month invariant.
 */
public final class SemiMonthlyScheduleDates {

    public static final int MIN_FIRST_DAY_OF_MONTH = 1;
    public static final int MAX_FIRST_DAY_OF_MONTH = 15;
    private static final int DAYS_BETWEEN_REPAYMENTS = 15;
    private static final int MIDDLE_OF_MONTH = 15;

    private SemiMonthlyScheduleDates() {}

    /**
     * The configured first due day. The validators make sure every semi-monthly product and loan has one, so a missing
     * day is an inconsistent state that must fail instead of falling back to an arbitrary day.
     */
    public static int requireFirstDayOfMonth(final Integer firstDayOfMonth) {
        if (firstDayOfMonth == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.semi.monthly.first.repayment.day.missing",
                    "A semi-monthly repayment schedule needs the first repayment day of the month");
        }
        return firstDayOfMonth;
    }

    /**
     * The date {@code periods} due dates after {@code date}; a negative count walks backwards.
     */
    public static LocalDate plusPeriods(final LocalDate date, final long periods, final int firstDayOfMonth) {
        LocalDate result = date;
        for (long period = 0; period < Math.abs(periods); period++) {
            result = periods > 0 ? next(result, firstDayOfMonth) : previous(result, firstDayOfMonth);
        }
        return result;
    }

    /**
     * The number of semi-monthly periods between two dates. Each semi-monthly period the interval overlaps contributes
     * the share of its own days that the interval covers, so two consecutive due dates are exactly one period apart,
     * even though the two periods of a month have different lengths, and an irregular period is prorated.
     */
    public static BigDecimal periodsBetween(final LocalDate startDate, final LocalDate endDate, final int firstDayOfMonth,
            final MathContext mc) {
        BigDecimal periods = BigDecimal.ZERO;
        LocalDate periodStart = isDueDate(startDate, firstDayOfMonth) ? startDate : previous(startDate, firstDayOfMonth);
        while (DateUtils.isBefore(periodStart, endDate)) {
            final LocalDate periodEnd = next(periodStart, firstDayOfMonth);
            final LocalDate overlapStart = DateUtils.isAfter(startDate, periodStart) ? startDate : periodStart;
            final LocalDate overlapEnd = DateUtils.isBefore(endDate, periodEnd) ? endDate : periodEnd;
            final BigDecimal overlapDays = BigDecimal.valueOf(DateUtils.getExactDifferenceInDays(overlapStart, overlapEnd));
            final BigDecimal periodDays = BigDecimal.valueOf(DateUtils.getExactDifferenceInDays(periodStart, periodEnd));
            periods = periods.add(overlapDays.divide(periodDays, mc), mc);
            periodStart = periodEnd;
        }
        return periods;
    }

    /**
     * The second due day of the given month for a loan whose first due day is {@code firstDayOfMonth}.
     */
    public static int secondDayOfMonth(final int firstDayOfMonth, final YearMonth month) {
        if (firstDayOfMonth == MIN_FIRST_DAY_OF_MONTH) {
            return MIDDLE_OF_MONTH;
        }
        if (firstDayOfMonth == MAX_FIRST_DAY_OF_MONTH) {
            return month.lengthOfMonth();
        }
        return Math.min(firstDayOfMonth + DAYS_BETWEEN_REPAYMENTS, month.lengthOfMonth());
    }

    /**
     * The iCalendar recurrence of the two due days, for the calendars Fineract stores alongside a loan (interest
     * recalculation rest and compounding dates "same as repayment period"). Calendars have no semi-monthly frequency,
     * so it is expressed as a monthly rule on two days of the month; {@code -1} is the last day of the month.
     * <p>
     * The rule only describes the schedule: the due dates themselves are always derived by {@link #next}. For a first
     * day of 14 the rule names the 29th, which iCalendar skips in a non-leap February where {@link #next} uses the
     * 28th.
     */
    public static String toRecurrence(final int firstDayOfMonth) {
        final String secondDayOfMonth;
        if (firstDayOfMonth == MIN_FIRST_DAY_OF_MONTH) {
            secondDayOfMonth = String.valueOf(MIDDLE_OF_MONTH);
        } else if (firstDayOfMonth == MAX_FIRST_DAY_OF_MONTH) {
            secondDayOfMonth = "-1";
        } else {
            secondDayOfMonth = String.valueOf(firstDayOfMonth + DAYS_BETWEEN_REPAYMENTS);
        }
        return "FREQ=MONTHLY;BYMONTHDAY=" + firstDayOfMonth + "," + secondDayOfMonth;
    }

    /**
     * The first due date strictly after {@code date}. A disbursement made after both due days of its month therefore
     * gets its first repayment on the first due day of the following month.
     */
    public static LocalDate next(final LocalDate date, final int firstDayOfMonth) {
        final YearMonth month = YearMonth.from(date);
        final int dayOfMonth = date.getDayOfMonth();
        if (dayOfMonth < firstDayOfMonth) {
            return month.atDay(firstDayOfMonth);
        }
        final int secondDayOfMonth = secondDayOfMonth(firstDayOfMonth, month);
        if (dayOfMonth < secondDayOfMonth) {
            return month.atDay(secondDayOfMonth);
        }
        return month.plusMonths(1).atDay(firstDayOfMonth);
    }

    /**
     * The first due date on or after {@code date}, so a date that already is a due day is kept as it is.
     */
    public static LocalDate onOrAfter(final LocalDate date, final int firstDayOfMonth) {
        return next(date.minusDays(1), firstDayOfMonth);
    }

    /**
     * Whether {@code date} is one of the two due days of its month.
     */
    public static boolean isDueDate(final LocalDate date, final int firstDayOfMonth) {
        return onOrAfter(date, firstDayOfMonth).equals(date);
    }

    /**
     * The last due date strictly before {@code date}, used to derive the disbursement date a first repayment date
     * implies.
     */
    public static LocalDate previous(final LocalDate date, final int firstDayOfMonth) {
        final YearMonth month = YearMonth.from(date);
        final int dayOfMonth = date.getDayOfMonth();
        final int secondDayOfMonth = secondDayOfMonth(firstDayOfMonth, month);
        if (dayOfMonth > secondDayOfMonth) {
            return month.atDay(secondDayOfMonth);
        }
        if (dayOfMonth > firstDayOfMonth) {
            return month.atDay(firstDayOfMonth);
        }
        final YearMonth previousMonth = month.minusMonths(1);
        return previousMonth.atDay(secondDayOfMonth(firstDayOfMonth, previousMonth));
    }
}
