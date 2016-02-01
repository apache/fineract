/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;

public class HolidayUtil {

    public static LocalDate getRepaymentRescheduleDateToIfHoliday(LocalDate repaymentDate, final List<Holiday> holidays) {

        for (final Holiday holiday : holidays) {
            if (repaymentDate.equals(holiday.getFromDateLocalDate()) || repaymentDate.equals(holiday.getToDateLocalDate())
                    || (repaymentDate.isAfter(holiday.getFromDateLocalDate()) && repaymentDate.isBefore(holiday.getToDateLocalDate()))) {
                repaymentDate = getRepaymentRescheduleDateIfHoliday(repaymentDate, holidays);
            }
        }
        return repaymentDate;
    }

    private static LocalDate getRepaymentRescheduleDateIfHoliday(final LocalDate repaymentDate, final List<Holiday> holidays) {

        for (final Holiday holiday : holidays) {
            if (repaymentDate.equals(holiday.getFromDateLocalDate()) || repaymentDate.equals(holiday.getToDateLocalDate())
                    || (repaymentDate.isAfter(holiday.getFromDateLocalDate()) && repaymentDate.isBefore(holiday.getToDateLocalDate()))) {
                // should be take from holiday
                return holiday.getRepaymentsRescheduledToLocalDate();
            }
        }
        return repaymentDate;
    }

    public static boolean isHoliday(final LocalDate date, final List<Holiday> holidays) {
        for (final Holiday holiday : holidays) {
            if (date.isEqual(holiday.getFromDateLocalDate()) || date.isEqual(holiday.getToDateLocalDate())
                    || (date.isAfter(holiday.getFromDateLocalDate()) && date.isBefore(holiday.getToDateLocalDate()))) { return true; }
        }

        return false;
    }
}
