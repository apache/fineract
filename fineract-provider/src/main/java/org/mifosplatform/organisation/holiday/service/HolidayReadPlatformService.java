/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.holiday.service;

import java.util.Collection;
import java.util.Date;

import org.mifosplatform.organisation.holiday.data.HolidayData;

public interface HolidayReadPlatformService {

    Collection<HolidayData> retrieveAllHolidaysBySearchParamerters(final Long officeId, Date fromDate, Date toDate);

    HolidayData retrieveHoliday(final Long holidayId);
}
