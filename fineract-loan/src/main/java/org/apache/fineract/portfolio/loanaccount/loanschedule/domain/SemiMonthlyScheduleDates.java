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
 * Resolves the due dates of a {@code SEMI_MONTHLY} repayment frequency.
 * <p>
 * A semi-monthly loan is repaid twice every calendar month, on the two configured {@link SemiMonthlyDueDays}, which
 * always yields 24 repayments a year. The days do not have to be evenly spread: any pair where the second day is
 * greater than the first is valid, so the two periods of a month can have very different lengths.
 * <p>
 * The first day is capped at 27 so that it exists in every month and can never meet the second one: the second day is
 * capped at the length of the month, so a first day of 28 with a second day of 29 or later would fall twice on the 28th
 * of a non leap February.
 */
public final class SemiMonthlyScheduleDates {

    public static final int MIN_FIRST_DAY_OF_MONTH = 1;
    public static final int MAX_FIRST_DAY_OF_MONTH = 27;
    public static final int MIN_SECOND_DAY_OF_MONTH = 2;
    /** A second day of 31 is the last day of every month. */
    public static final int MAX_SECOND_DAY_OF_MONTH = 31;

    private SemiMonthlyScheduleDates() {}

    /**
     * The configured due days. The validators make sure every semi-monthly product and loan has both, so a missing day
     * is an inconsistent state that must fail instead of falling back to an arbitrary day.
     */
    public static SemiMonthlyDueDays requireDueDays(final Integer firstDayOfMonth, final Integer secondDayOfMonth) {
        if (firstDayOfMonth == null || secondDayOfMonth == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.semi.monthly.repayment.days.missing",
                    "A semi-monthly repayment schedule needs the first and the second repayment day of the month");
        }
        return new SemiMonthlyDueDays(firstDayOfMonth, secondDayOfMonth);
    }

    /**
     * The date {@code periods} due dates after {@code date}; a negative count walks backwards.
     */
    public static LocalDate plusPeriods(final LocalDate date, final long periods, final SemiMonthlyDueDays dueDays) {
        LocalDate result = date;
        for (long period = 0; period < Math.abs(periods); period++) {
            result = periods > 0 ? next(result, dueDays) : previous(result, dueDays);
        }
        return result;
    }

    /**
     * The number of semi-monthly periods between two dates. Each semi-monthly period the interval overlaps contributes
     * the share of its own days that the interval covers, so two consecutive due dates are exactly one period apart,
     * even though the two periods of a month have different lengths, and an irregular period is prorated.
     */
    public static BigDecimal periodsBetween(final LocalDate startDate, final LocalDate endDate, final SemiMonthlyDueDays dueDays,
            final MathContext mc) {
        BigDecimal periods = BigDecimal.ZERO;
        LocalDate periodStart = isDueDate(startDate, dueDays) ? startDate : previous(startDate, dueDays);
        while (DateUtils.isBefore(periodStart, endDate)) {
            final LocalDate periodEnd = next(periodStart, dueDays);
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
     * The iCalendar recurrence of the two due days, for the calendars Fineract stores alongside a loan (interest
     * recalculation rest and compounding dates "same as repayment period"). Calendars have no semi-monthly frequency,
     * so it is expressed as a monthly rule on two days of the month; {@code -1} is the last day of the month.
     * <p>
     * The rule only describes the schedule: the due dates themselves are always derived by {@link #next}. A second day
     * of 29 or 30 is named as such, and iCalendar skips it in the months that lack it, where {@link #next} uses the
     * last day of the month.
     */
    public static String toRecurrence(final SemiMonthlyDueDays dueDays) {
        final String secondDayOfMonth = dueDays.secondDayOfMonth() == MAX_SECOND_DAY_OF_MONTH ? "-1"
                : String.valueOf(dueDays.secondDayOfMonth());
        return "FREQ=MONTHLY;BYMONTHDAY=" + dueDays.firstDayOfMonth() + "," + secondDayOfMonth;
    }

    /**
     * The first due date strictly after {@code date}. A disbursement made after both due days of its month therefore
     * gets its first repayment on the first due day of the following month.
     */
    public static LocalDate next(final LocalDate date, final SemiMonthlyDueDays dueDays) {
        final YearMonth month = YearMonth.from(date);
        final int dayOfMonth = date.getDayOfMonth();
        if (dayOfMonth < dueDays.firstDayOfMonth()) {
            return month.atDay(dueDays.firstDayOfMonth());
        }
        final int secondDayOfMonth = dueDays.secondDayOf(month);
        if (dayOfMonth < secondDayOfMonth) {
            return month.atDay(secondDayOfMonth);
        }
        return month.plusMonths(1).atDay(dueDays.firstDayOfMonth());
    }

    /**
     * The first due date on or after {@code date}, so a date that already is a due day is kept as it is.
     */
    public static LocalDate onOrAfter(final LocalDate date, final SemiMonthlyDueDays dueDays) {
        return next(date.minusDays(1), dueDays);
    }

    /**
     * Whether {@code date} is one of the two due days of its month.
     */
    public static boolean isDueDate(final LocalDate date, final SemiMonthlyDueDays dueDays) {
        return onOrAfter(date, dueDays).equals(date);
    }

    /**
     * The last due date strictly before {@code date}, used to derive the disbursement date a first repayment date
     * implies.
     */
    public static LocalDate previous(final LocalDate date, final SemiMonthlyDueDays dueDays) {
        final YearMonth month = YearMonth.from(date);
        final int dayOfMonth = date.getDayOfMonth();
        final int secondDayOfMonth = dueDays.secondDayOf(month);
        if (dayOfMonth > secondDayOfMonth) {
            return month.atDay(secondDayOfMonth);
        }
        if (dayOfMonth > dueDays.firstDayOfMonth()) {
            return month.atDay(dueDays.firstDayOfMonth());
        }
        final YearMonth previousMonth = month.minusMonths(1);
        return previousMonth.atDay(dueDays.secondDayOf(previousMonth));
    }
}
