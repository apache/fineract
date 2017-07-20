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
package org.apache.fineract.organisation.workingdays.service;

import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;
import org.apache.fineract.organisation.workingdays.domain.RepaymentRescheduleType;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.joda.time.LocalDate;

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
    
    public static void updateWorkingDayIfRepaymentDateIsNonWorkingDay(final AdjustedDateDetailsDTO adjustedDateDetailsDTO, final WorkingDays workingDays) {
        final LocalDate changedScheduleDate = getOffSetDateIfNonWorkingDay(adjustedDateDetailsDTO.getChangedScheduleDate(),
                adjustedDateDetailsDTO.getNextRepaymentPeriodDueDate(), workingDays);
        adjustedDateDetailsDTO.setChangedScheduleDate(changedScheduleDate);
    }
    
    public static RepaymentRescheduleType getRepaymentRescheduleType(final WorkingDays workingDays, final LocalDate date) {
        RepaymentRescheduleType rescheduleType = RepaymentRescheduleType.fromInt(workingDays.getRepaymentReschedulingType());
        return rescheduleType;
    }
}
