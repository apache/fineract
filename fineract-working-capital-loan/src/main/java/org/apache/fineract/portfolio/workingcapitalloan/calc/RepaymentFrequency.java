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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.apache.fineract.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;

/**
 * The length of one schedule period, {@code every} units of {@code type}.
 *
 * <p>
 * Due dates are always counted from the anchor rather than stepped from the previous due date, so a schedule disbursed
 * on the 31st falls due on the last day of every shorter month and returns to the 31st after it, instead of drifting to
 * the 28th.
 *
 * <p>
 * Days and weeks measure the period in days against the npv day count, months in months against a year of twelve, so
 * seven days and one week are the same period.
 */
record RepaymentFrequency(WorkingCapitalLoanPeriodFrequencyType type, int every) {

    static final RepaymentFrequency DAILY = new RepaymentFrequency(WorkingCapitalLoanPeriodFrequencyType.DAYS, 1);

    private static final int DAYS_PER_WEEK = 7;
    private static final int MONTHS_PER_YEAR = 12;

    RepaymentFrequency {
        Objects.requireNonNull(type, "type");
        if (!type.isRepaymentFrequency()) {
            throw new IllegalArgumentException("unsupported repayment frequency type: " + type);
        }
        if (every < 1) {
            throw new IllegalArgumentException("repaymentEvery must be at least 1, got: " + every);
        }
    }

    /** Schedules and loans that predate the setting carry no frequency and were always scheduled daily. */
    static RepaymentFrequency of(final WorkingCapitalLoanPeriodFrequencyType type, final Integer every) {
        if (type == null && every == null) {
            return DAILY;
        }
        if (type == null || every == null) {
            throw new IllegalArgumentException(
                    "repayment frequency type and repaymentEvery must be set together, got: " + type + "/" + every);
        }
        return new RepaymentFrequency(type, every);
    }

    LocalDate dueDate(final LocalDate anchor, final long periods) {
        try {
            return type.plus(anchor, Math.multiplyExact(periods, every));
        } catch (final DateTimeException e) {
            throw (ArithmeticException) new ArithmeticException(
                    "due date " + periods + " x " + every + " " + type + " after " + anchor + " is outside the supported date range")
                    .initCause(e);
        }
    }

    /** The fewest periods after {@code anchor} whose due date is on or after {@code date}. */
    long periodsUntil(final LocalDate anchor, final LocalDate date) {
        long periods = Math.floorDiv(unitsBetween(anchor, date), unitsPerPeriod());
        while (dueDate(anchor, periods).isBefore(date)) {
            periods++;
        }
        while (!dueDate(anchor, periods - 1).isBefore(date)) {
            periods--;
        }
        return periods;
    }

    int unitsPerPeriod() {
        return type == WorkingCapitalLoanPeriodFrequencyType.WEEKS ? Math.multiplyExact(DAYS_PER_WEEK, every) : every;
    }

    int unitsPerYear(final int npvDayCount) {
        return type == WorkingCapitalLoanPeriodFrequencyType.MONTHS ? MONTHS_PER_YEAR : npvDayCount;
    }

    private long unitsBetween(final LocalDate from, final LocalDate to) {
        return type == WorkingCapitalLoanPeriodFrequencyType.MONTHS ? ChronoUnit.MONTHS.between(from, to)
                : ChronoUnit.DAYS.between(from, to);
    }
}
