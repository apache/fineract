package org.mifosplatform.organisation.holiday.service;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface HolidayWritePlatformService {

    CommandProcessingResult createHoliday(JsonCommand command);
    
    CommandProcessingResult updateHoliday(JsonCommand command);
    
    CommandProcessingResult activateHoliday(final Long holidayId);
    
    CommandProcessingResult deleteHoliday(final Long holidayId);
    
    boolean isHoliday(Long officeId, LocalDate transactionDate);
    
    boolean isTransactionAllowedOnHoliday();
}
