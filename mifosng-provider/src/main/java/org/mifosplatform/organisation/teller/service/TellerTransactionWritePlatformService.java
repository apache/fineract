/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * Provides the local service for adding teller transactions.
 *
 * @see org.mifosplatform.organisation.teller.domain.TellerTransactionRepository
 * @since 2.0.0
 */
public interface TellerTransactionWritePlatformService {

    /**
     * Stores teller transactions into the data base.
     *
     * @param command the command containing the teller transaction details
     * @return the result i
     */
    public CommandProcessingResult createTellerTransaction(JsonCommand command);
}
