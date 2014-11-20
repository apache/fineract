/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.useradministration.domain.AppUser;

/**
 * Provides the local service for adding, modifying and deleting tellers.
 *
 * @author Markus Geiss
 * @see org.mifosplatform.organisation.teller.domain.TellerRepository
 * @since 2.0.0
 */
public interface TellerWritePlatformService {

    /**
     * Creates a new teller.
     *
     * @param command the command to create a new teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.mifosplatform.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult createTeller(JsonCommand command);

    /**
     * Modifies a new teller.
     *
     * @param tellerId the primary key of the teller
     * @param command  the command to modifya new teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.mifosplatform.organisation.teller.exception.TellerNotFoundException
     * @throws org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.mifosplatform.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult modifyTeller(Long tellerId, JsonCommand command);

    /**
     * deletes a new teller.
     *
     * @param tellerId the primary key of the teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.mifosplatform.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult deleteTeller(Long tellerId);
    
    /**
     * Allocates a cashier to an existing teller. The allocation can be for a duration
     * from a date to a date
     * from a certain start time to an end time.
     *
	 * @param command the command to allocate a cashier for a specific teller
     * @return {@code CommandProcessingResult} if successful
     * @throws org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException
     * @throws org.mifosplatform.infrastructure.core.exception.InvalidJsonException
     */
    public CommandProcessingResult allocateCashierToTeller(Long tellerId, JsonCommand command);

	CommandProcessingResult updateCashierAllocation(Long tellerId, Long cashierId,
			JsonCommand command);

	CommandProcessingResult deleteCashierAllocation(final Long tellerId, Long cashierId, 
			JsonCommand command);

	CommandProcessingResult allocateCashToCashier(Long cashierId,
			JsonCommand command);

	CommandProcessingResult settleCashFromCashier(Long cashierId,
			JsonCommand command);
}
