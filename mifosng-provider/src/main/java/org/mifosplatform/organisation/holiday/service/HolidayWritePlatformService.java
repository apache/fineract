/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
