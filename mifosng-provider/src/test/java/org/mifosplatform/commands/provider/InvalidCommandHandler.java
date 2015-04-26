/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.provider;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public class InvalidCommandHandler implements NewCommandSourceHandler {

    public InvalidCommandHandler(final CommandHandlerProvider commandHandlerProvider) {
        super();
        commandHandlerProvider.registerHandler(this);
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return CommandProcessingResult.commandOnlyResult(command.commandId());
    }
}
