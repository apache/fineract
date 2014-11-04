/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.util.List;

import org.mifosplatform.organisation.holiday.domain.Holiday;
import org.mifosplatform.organisation.workingdays.domain.WorkingDays;

public class HolidayDetailDTO {

    final boolean isHolidayEnabled;
    final List<Holiday> holidays;
    final WorkingDays workingDays;
    final boolean allowTransactionsOnHoliday;
    final boolean allowTransactionsOnNonWorkingDay;

    public HolidayDetailDTO(final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays) {
        this.isHolidayEnabled = isHolidayEnabled;
        this.holidays = holidays;
        this.workingDays = workingDays;
        this.allowTransactionsOnHoliday = false;
        this.allowTransactionsOnNonWorkingDay = false;
    }

    public HolidayDetailDTO(final boolean isHolidayEnabled, final List<Holiday> holidays, final WorkingDays workingDays,
            final boolean allowTransactionsOnHoliday, final boolean allowTransactionsOnNonWorkingDay) {
        this.isHolidayEnabled = isHolidayEnabled;
        this.holidays = holidays;
        this.workingDays = workingDays;
        this.allowTransactionsOnHoliday = allowTransactionsOnHoliday;
        this.allowTransactionsOnNonWorkingDay = allowTransactionsOnNonWorkingDay;
    }

    public boolean isHolidayEnabled() {
        return this.isHolidayEnabled;
    }

    public List<Holiday> getHolidays() {
        return this.holidays;
    }

    public WorkingDays getWorkingDays() {
        return this.workingDays;
    }

    public boolean isAllowTransactionsOnHoliday() {
        return this.allowTransactionsOnHoliday;
    }

    public boolean isAllowTransactionsOnNonWorkingDay() {
        return this.allowTransactionsOnNonWorkingDay;
    }
}
