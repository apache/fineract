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
package org.apache.fineract.portfolio.loanaccount.data;

import java.util.List;

import org.apache.fineract.organisation.holiday.domain.Holiday;
import org.apache.fineract.organisation.workingdays.domain.WorkingDays;

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
