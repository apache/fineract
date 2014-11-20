/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.organisation.teller.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.teller.service.TellerWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles a create teller command.
 *
 * @author Markus Geiss
 * @see org.mifosplatform.organisation.teller.service.TellerWritePlatformService
 * @since 2.0.0
 */
@Service
public class CreateTellerCommandHandler implements NewCommandSourceHandler {

    private final TellerWritePlatformService writePlatformService;

    /**
     * Creates a new instance
     *
     * @param writePlatformService the {@code TellerWritePlatformService} used to access the backend
     */
    @Autowired
    public CreateTellerCommandHandler(final TellerWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.createTeller(command);
    }
}

