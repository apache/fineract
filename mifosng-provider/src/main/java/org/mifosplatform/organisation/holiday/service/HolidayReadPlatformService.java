package org.mifosplatform.organisation.holiday.service;

import java.util.Collection;
import java.util.Date;

import org.mifosplatform.organisation.holiday.data.HolidayData;

public interface HolidayReadPlatformService {

    Collection<HolidayData> retrieveAllHolidaysBySearchParamerters(final Long officeId, Date fromDate, Date toDate);
    
    HolidayData retrieveHoliday(final Long holidayId);
}
