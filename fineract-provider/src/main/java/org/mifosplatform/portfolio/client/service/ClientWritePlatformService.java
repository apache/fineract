/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface ClientWritePlatformService {

    CommandProcessingResult createClient(JsonCommand command);

    CommandProcessingResult updateClient(Long clientId, JsonCommand command);

    CommandProcessingResult activateClient(Long clientId, JsonCommand command);

    CommandProcessingResult deleteClient(Long clientId);

    CommandProcessingResult unassignClientStaff(Long clientId, JsonCommand command);

    CommandProcessingResult closeClient(final Long clientId, final JsonCommand command);

    CommandProcessingResult assignClientStaff(Long clientId, JsonCommand command);

    CommandProcessingResult updateDefaultSavingsAccount(Long clientId, JsonCommand command);

    CommandProcessingResult rejectClient(Long entityId, JsonCommand command);

    CommandProcessingResult withdrawClient(Long entityId, JsonCommand command);

    CommandProcessingResult reActivateClient(Long entityId, JsonCommand command);

}