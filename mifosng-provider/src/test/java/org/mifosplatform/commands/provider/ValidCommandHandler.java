/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.provider;

import org.mifosplatform.commands.annotation.CommandType;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.stereotype.Component;

@Component
@CommandType(entity = "HUMAN", action = "UPDATE")
public class ValidCommandHandler implements NewCommandSourceHandler {

    public ValidCommandHandler() {
        super();
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return CommandProcessingResult.commandOnlyResult(command.commandId());
    }
}
