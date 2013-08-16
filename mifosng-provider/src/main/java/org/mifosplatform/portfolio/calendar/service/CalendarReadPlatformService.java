/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.service;

import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.calendar.data.CalendarData;

import java.util.Collection;
import java.util.List;

public interface CalendarReadPlatformService {

    CalendarData retrieveCalendar(final Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarData> retrieveCalendarsByEntity(final Long entityId, final Integer entityTypeId, List<Integer> calendarTypeOptions);
    
    Collection<CalendarData> retrieveParentCalendarsByEntity(final Long entityId, final Integer entityTypeId, List<Integer> calendarTypeOptions);

    Collection<CalendarData> retrieveAllCalendars();

    CalendarData retrieveNewCalendarDetails();
    
    CalendarData generateRecurringDate(final CalendarData calendarData);

    CalendarData generateRecurringDate(final CalendarData calendarData, final LocalDate tillDate);
    
    Collection<CalendarData> generateRecurringDates(final Collection<CalendarData> calendarsData);
    
    CalendarData retrieveLoanCalendar(final Long loanId);
    
    CalendarData retrieveCollctionCalendarByEntity(final Long entityId, final Integer entityTypeId);

}
