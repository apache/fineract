/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.entityaccess.handler;

import javax.transaction.Transactional;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.entityaccess.service.MifosEntityAccessWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateEntityToEntityMappingCommandHandler implements NewCommandSourceHandler {
    
    private final MifosEntityAccessWriteService mifosEntityAccessWriteService;

    @Autowired
    public CreateEntityToEntityMappingCommandHandler(final MifosEntityAccessWriteService mifosEntityAccessWriteService) {
        this.mifosEntityAccessWriteService = mifosEntityAccessWriteService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.mifosEntityAccessWriteService.createEntityToEntityMapping(command.entityId(),command);
    }

}
