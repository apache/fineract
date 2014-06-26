/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.organisation.workingdays.service;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.workingdays.domain.RepaymentRescheduleType;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;

public class WorkingDaysUtil {

    public static LocalDate getOffSetDateIfNonWorkingDay(final LocalDate date, final LocalDate nextMeetingDate,
            final WorkingDays workingDays) {

        // If date is not a non working day then return date.
        if (isWorkingDay(workingDays, date)) { return date; }

        final RepaymentRescheduleType rescheduleType = RepaymentRescheduleType.fromInt(workingDays.getRepaymentReschedulingType());

        switch (rescheduleType) {
            case INVALID:
                return date;
            case SAME_DAY:
                return date;
            case MOVE_TO_NEXT_WORKING_DAY:
                return getOffSetDateIfNonWorkingDay(date.plusDays(1), nextMeetingDate, workingDays);
            case MOVE_TO_NEXT_REPAYMENT_MEETING_DAY:
                return nextMeetingDate;
            case MOVE_TO_PREVIOUS_WORKING_DAY:
                return getOffSetDateIfNonWorkingDay(date.minusDays(1), nextMeetingDate, workingDays);
            default:
                return date;
        }
    }

    public static boolean isWorkingDay(final WorkingDays workingDays, final LocalDate date) {
        return CalendarUtils.isValidRedurringDate(workingDays.getRecurrence(), date, date);
    }
    
    public static boolean isNonWorkingDay(final WorkingDays workingDays, final LocalDate date) {
        return !isWorkingDay(workingDays, date);
    }
}
