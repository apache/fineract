package org.mifosplatform.organisation.holiday.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface HolidayWritePlatformService {

    CommandProcessingResult createHoliday(JsonCommand command);
}
