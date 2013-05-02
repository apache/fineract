/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.calendar.service.CalendarWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCalendarCommandHandler implements NewCommandSourceHandler {

    private final CalendarWritePlatformService calendarWritePlatformService;

    @Autowired
    public UpdateCalendarCommandHandler(final CalendarWritePlatformService calendarWritePlatformService) {
        this.calendarWritePlatformService = calendarWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.calendarWritePlatformService.updateCalendar(command);
    }

}
