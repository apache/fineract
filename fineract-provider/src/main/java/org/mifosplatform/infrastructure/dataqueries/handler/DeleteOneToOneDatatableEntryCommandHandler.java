/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteOneToOneDatatableEntryCommandHandler implements NewCommandSourceHandler {

    private final ReadWriteNonCoreDataService writePlatformService;

    @Autowired
    public DeleteOneToOneDatatableEntryCommandHandler(final ReadWriteNonCoreDataService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        final CommandProcessingResult commandProcessingResult = this.writePlatformService.deleteDatatableEntries(command.entityName(),
                command.entityId());

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(command.entityId()) //
                .withOfficeId(commandProcessingResult.getOfficeId()) //
                .withGroupId(commandProcessingResult.getGroupId()) //
                .withClientId(commandProcessingResult.getClientId()) //
                .withSavingsId(commandProcessingResult.getSavingsId()) //
                .withLoanId(commandProcessingResult.getLoanId()) //
                .build();
    }
}