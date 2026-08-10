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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * The single implementation of how recorded pauses reshape a schedule period, shared by the breach and the delinquency
 * schedules so the rule cannot drift between them.
 */
public final class WorkingCapitalLoanPausePeriodUtils {

    private WorkingCapitalLoanPausePeriodUtils() {}

    /**
     * Returns whether a new inclusive pause period shares at least one day with an existing one. Touching periods
     * (where one ends on the day the other starts) are treated as overlapping.
     */
    public static boolean inclusivePausePeriodsOverlap(final LocalDate parsedPauseStart, final LocalDate parsedPauseEnd,
            final LocalDate existingPauseStart, final LocalDate existingPauseEnd) {
        if (parsedPauseStart == null || parsedPauseEnd == null || existingPauseStart == null || existingPauseEnd == null) {
            return false;
        }
        return !parsedPauseStart.isAfter(existingPauseEnd) && !parsedPauseEnd.isBefore(existingPauseStart);
    }

    /**
     * Inclusive pause length: both start and end dates count as paused days.
     */
    public static long calculatePauseExtensionDays(final LocalDate pauseStart, final LocalDate pauseEnd) {
        if (pauseStart == null || pauseEnd == null || pauseStart.isAfter(pauseEnd)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(pauseStart, pauseEnd) + 1;
    }

    /**
     * Replays the pauses over an inclusive period, in start date order. An overlapping pause pushes the end date out by
     * its full inclusive length, and the overlap check runs against the end date as it grows, so a pause falling into
     * the already extended window is absorbed as well. A pause that started before the period pushes the start date out
     * by the same length, keeping the period length unchanged.
     */
    public static WorkingCapitalLoanPeriodBounds applyPauses(final LocalDate fromDate, final LocalDate baseToDate,
            final List<WorkingCapitalLoanPausePeriod> pauses) {
        if (fromDate == null || baseToDate == null || pauses == null) {
            return new WorkingCapitalLoanPeriodBounds(fromDate, baseToDate);
        }
        LocalDate shiftedFromDate = fromDate;
        LocalDate shiftedToDate = baseToDate;
        for (final WorkingCapitalLoanPausePeriod pause : sortedByStartDate(pauses)) {
            final LocalDate pauseStart = pause.startDate();
            final LocalDate pauseEnd = pause.endDate();
            if (pauseEnd.isBefore(shiftedFromDate) || pauseStart.isAfter(shiftedToDate)) {
                continue;
            }
            final long pauseDays = calculatePauseExtensionDays(pauseStart, pauseEnd);
            shiftedToDate = shiftedToDate.plusDays(pauseDays);
            if (shiftedFromDate.isAfter(pauseStart)) {
                shiftedFromDate = shiftedFromDate.plusDays(pauseDays);
            }
        }
        return new WorkingCapitalLoanPeriodBounds(shiftedFromDate, shiftedToDate);
    }

    /**
     * End date of {@link #applyPauses}, so validation computing the date up front and the mutation applying it cannot
     * disagree.
     */
    public static LocalDate extendToDate(final LocalDate fromDate, final LocalDate baseToDate,
            final List<WorkingCapitalLoanPausePeriod> pauses) {
        return applyPauses(fromDate, baseToDate, pauses).toDate();
    }

    private static List<WorkingCapitalLoanPausePeriod> sortedByStartDate(final List<WorkingCapitalLoanPausePeriod> pauses) {
        return pauses.stream().filter(Objects::nonNull).filter(WorkingCapitalLoanPausePeriod::isValid)
                .sorted(Comparator.comparing(WorkingCapitalLoanPausePeriod::startDate)).toList();
    }
}
