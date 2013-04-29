/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import java.util.Collection;

import org.mifosplatform.portfolio.calendar.data.CalendarData;

public interface CalendarReadPlatformService {

    CalendarData retrieveCalendar(final Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId);
    
    Collection<CalendarData> retrieveParentCalendarsByEntity(final Long entityId, final Integer entityTypeId);

    Collection<CalendarData> retrieveAllCalendars();

    CalendarData retrieveNewCalendarDetails();
    
    CalendarData generateRecurringDate(final CalendarData calendarData);
    
    Collection<CalendarData> generateRecurringDates(final Collection<CalendarData> calendarsData);
    
    CalendarData retrieveLoanCalendar(final Long loanId);
}
