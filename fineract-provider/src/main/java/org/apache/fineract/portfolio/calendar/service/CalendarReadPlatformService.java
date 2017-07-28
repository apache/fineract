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

import java.util.Collection;
import java.util.List;

import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.joda.time.LocalDate;

public interface CalendarReadPlatformService {

    CalendarData retrieveCalendar(final Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId, List<Integer> calendarTypeOptions);

    Collection<CalendarData> retrieveParentCalendarsByEntity(final Long entityId, final Integer entityTypeId,
            List<Integer> calendarTypeOptions);

    Collection<CalendarData> retrieveAllCalendars();

    CalendarData retrieveNewCalendarDetails();

    Collection<LocalDate> generateRecurringDates(final CalendarData calendarData, final boolean withHistory, final LocalDate tillDate);

    Collection<LocalDate> generateNextTenRecurringDates(final CalendarData calendarData);

    Collection<CalendarData> updateWithRecurringDates(final Collection<CalendarData> calendarsData);

    CalendarData retrieveLoanCalendar(final Long loanId);

    CalendarData retrieveCollctionCalendarByEntity(final Long entityId, final Integer entityTypeId);

    LocalDate generateNextEligibleMeetingDateForCollection(CalendarData calendarData, MeetingData lastMeetingData);

    Boolean isCalendarAssociatedWithEntity(final Long entityId, final Long calendarId, Long entityTypeId);

}
