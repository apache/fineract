package org.mifosplatform.organisation.holiday.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.holiday.domain.Holiday;

public class HolidayUtil {

    public static LocalDate getRepaymentRescheduleDateToIfHoliday(final LocalDate repaymentDate, final List<Holiday> holidays) {
        for (final Holiday holiday : holidays) {
            if (repaymentDate.equals(holiday.getFromDateLocalDate()) || repaymentDate.equals(holiday.getToDateLocalDate())
                    || (repaymentDate.isAfter(holiday.getFromDateLocalDate()) && repaymentDate.isBefore(holiday.getToDateLocalDate()))) {
                // should be take from holiday
                return holiday.getRepaymentsRescheduledToLocalDate();
            }
        }
        return repaymentDate;
    }
}
