package org.mifosplatform.portfolio.calendar.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CalendarWritePlatformService {

    CommandProcessingResult createCalendar(JsonCommand command);

    CommandProcessingResult updateCalendar(JsonCommand command);

    CommandProcessingResult deleteCalendar(Long calendarId);
}
