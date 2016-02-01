/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.service.CalendarUtils;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;

public class DepositAccountUtils {

    public static final int GENERATE_MINIMUM_NUMBER_OF_FUTURE_INSTALMENTS = 5;

    public static LocalDate calculateNextDepositDate(final LocalDate lastDepositDate, final PeriodFrequencyType frequency,
            final int recurringEvery) {
        LocalDate nextDepositDate = lastDepositDate;

        switch (frequency) {
            case DAYS:
                nextDepositDate = lastDepositDate.plusDays(recurringEvery);
            break;
            case WEEKS:
                nextDepositDate = lastDepositDate.plusWeeks(recurringEvery);
            break;
            case MONTHS:
                nextDepositDate = lastDepositDate.plusMonths(recurringEvery);
            break;
            case YEARS:
                nextDepositDate = lastDepositDate.plusYears(recurringEvery);
            break;
            case INVALID:
            break;
        }
        return nextDepositDate;
    }

    public static LocalDate calculateNextDepositDate(final LocalDate lastDepositDate, final String recurrence) {
        final PeriodFrequencyType frequencyType = CalendarFrequencyType.from(CalendarUtils.getFrequency(recurrence));
        Integer frequency = CalendarUtils.getInterval(recurrence);
        frequency = frequency == -1 ? 1 : frequency;
        return calculateNextDepositDate(lastDepositDate, frequencyType, frequency);
    }

}
