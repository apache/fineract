/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.organisation.teller.service.TellerTransactionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateTellerTransactionCommandHandler implements NewCommandSourceHandler {

    private final TellerTransactionWritePlatformService writePlatformService;

    @Autowired
    public CreateTellerTransactionCommandHandler(final TellerTransactionWritePlatformService writePlatformService) {
        super();
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.createTellerTransaction(command);
    }
}
