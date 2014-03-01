/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.meeting.data.MeetingData;

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

}
