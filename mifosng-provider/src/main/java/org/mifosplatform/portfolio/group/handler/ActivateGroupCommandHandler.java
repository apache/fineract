/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.group.service.GroupingTypesWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivateGroupCommandHandler implements NewCommandSourceHandler {

    private final GroupingTypesWritePlatformService writePlatformService;

    @Autowired
    public ActivateGroupCommandHandler(final GroupingTypesWritePlatformService clientWritePlatformService) {
        this.writePlatformService = clientWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.writePlatformService.activateGroupOrCenter(command.entityId(), command);
    }
}