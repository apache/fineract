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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.workingdays.data.AdjustedDateDetailsDTO;

public final class HolidayUtil {

    private HolidayUtil() {

    }

    public static LocalDate getRepaymentRescheduleDateToIfHoliday(LocalDate repaymentDate, final List<Holiday> holidays) {
        for (final Holiday holiday : holidays) {
            if (isHoliday(repaymentDate, holiday)) {
                repaymentDate = holiday.getRepaymentsRescheduledTo();
            }
        }
        return repaymentDate;
    }

    public static boolean isHoliday(final LocalDate date, final List<Holiday> holidays) {
        return getApplicableHoliday(date, holidays) != null;
    }

    public static Holiday getApplicableHoliday(final LocalDate repaymentDate, final List<Holiday> holidays) {
        for (final Holiday holiday : holidays) {
            if (isHoliday(repaymentDate, holiday)) {
                return holiday;
            }
        }
        return null;
    }

    public static boolean isHoliday(LocalDate date, Holiday holiday) {
        return !DateUtils.isBefore(date, holiday.getFromDate()) && !DateUtils.isAfter(date, holiday.getToDate());
    }

    public static void updateRepaymentRescheduleDateToWorkingDayIfItIsHoliday(final AdjustedDateDetailsDTO adjustedDateDetailsDTO,
            final Holiday holiday) {
        if (holiday.getReScheduleType().isRescheduleToSpecificDate()) {
            adjustedDateDetailsDTO.setChangedScheduleDate(holiday.getRepaymentsRescheduledTo());
        }
    }
}
