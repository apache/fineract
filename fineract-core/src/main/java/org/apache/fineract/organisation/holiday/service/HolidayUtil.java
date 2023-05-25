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
package org.apache.fineract.organisation.holiday.service;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;

public final class HolidayUtil {

    private HolidayUtil() {

    }

    public static LocalDate getRepaymentRescheduleDateToIfHoliday(LocalDate repaymentDate, final List<Holiday> holidays) {

        for (final Holiday holiday : holidays) {
            if (repaymentDate.equals(holiday.getFromDate()) || repaymentDate.equals(holiday.getToDate())
                    || (repaymentDate.isAfter(holiday.getFromDate()) && repaymentDate.isBefore(holiday.getToDate()))) {
                repaymentDate = getRepaymentRescheduleDateIfHoliday(repaymentDate, holidays);
            }
        }
        return repaymentDate;
    }

    private static LocalDate getRepaymentRescheduleDateIfHoliday(final LocalDate repaymentDate, final List<Holiday> holidays) {

        for (final Holiday holiday : holidays) {
            if (repaymentDate.equals(holiday.getFromDate()) || repaymentDate.equals(holiday.getToDate())
                    || (repaymentDate.isAfter(holiday.getFromDate()) && repaymentDate.isBefore(holiday.getToDate()))) {
                // should be take from holiday
                return holiday.getRepaymentsRescheduledTo();
            }
        }
        return repaymentDate;
    }

    public static boolean isHoliday(final LocalDate date, final List<Holiday> holidays) {
        for (final Holiday holiday : holidays) {
            if (date.isEqual(holiday.getFromDate()) || date.isEqual(holiday.getToDate())
                    || (date.isAfter(holiday.getFromDate()) && date.isBefore(holiday.getToDate()))) {
                return true;
            }
        }

        return false;
    }

    public static Holiday getApplicableHoliday(final LocalDate repaymentDate, final List<Holiday> holidays) {
        Holiday referedHoliday = null;
        for (final Holiday holiday : holidays) {
            if (!repaymentDate.isBefore(holiday.getFromDate()) && !repaymentDate.isAfter(holiday.getToDate())) {
                referedHoliday = holiday;
            }
        }
        return referedHoliday;
    }

    public static void updateRepaymentRescheduleDateToWorkingDayIfItIsHoliday(final AdjustedDateDetailsDTO adjustedDateDetailsDTO,
            final Holiday holiday) {
        if (holiday.getReScheduleType().isRescheduleToSpecificDate()) {
            adjustedDateDetailsDTO.setChangedScheduleDate(holiday.getRepaymentsRescheduledTo());
        }
    }
}
