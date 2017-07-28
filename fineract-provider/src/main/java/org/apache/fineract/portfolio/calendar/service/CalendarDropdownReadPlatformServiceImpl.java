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
package org.apache.fineract.portfolio.calendar.service;

import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarRemindBy;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
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
    @Override
    public List<EnumOptionData> retrieveCalendarFrequencyNthDayTypeOptions() {
        return CalendarEnumerations.calendarFrequencyNthDayType();
    }
}
