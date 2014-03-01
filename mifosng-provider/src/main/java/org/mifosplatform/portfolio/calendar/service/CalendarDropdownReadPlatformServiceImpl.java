/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.util.List;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.calendar.domain.CalendarEntityType;
import org.mifosplatform.portfolio.calendar.domain.CalendarFrequencyType;
import org.mifosplatform.portfolio.calendar.domain.CalendarRemindBy;
import org.mifosplatform.portfolio.calendar.domain.CalendarType;
import org.mifosplatform.portfolio.calendar.domain.CalendarWeekDaysType;
import org.springframework.stereotype.Service;

@Service
public class CalendarDropdownReadPlatformServiceImpl implements CalendarDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveCalendarEntityTypeOptions() {
        return CalendarEnumerations.calendarEntityType(CalendarEntityType.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarTypeOptions() {
        return CalendarEnumerations.calendarType(CalendarType.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarRemindByOptions() {
        return CalendarEnumerations.calendarRemindBy(CalendarRemindBy.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarFrequencyTypeOptions() {
        return CalendarEnumerations.calendarFrequencyType(CalendarFrequencyType.values());
    }

    @Override
    public List<EnumOptionData> retrieveCalendarWeekDaysTypeOptions() {
        return CalendarEnumerations.calendarWeekDaysType(CalendarWeekDaysType.values());
    }
}
