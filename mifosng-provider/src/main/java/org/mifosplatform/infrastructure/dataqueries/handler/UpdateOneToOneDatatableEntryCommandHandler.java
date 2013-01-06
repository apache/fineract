package org.mifosplatform.infrastructure.dataqueries.handler;

import java.util.Map;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOneToOneDatatableEntryCommandHandler implements NewCommandSourceHandler {

    private final ReadWriteNonCoreDataService writePlatformService;

    @Autowired
    public UpdateOneToOneDatatableEntryCommandHandler(final ReadWriteNonCoreDataService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        final Map<String, Object> changes = this.writePlatformService.updateDatatableEntryOneToOne(command.entityName(), command.getApptableId(),
                command);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(command.getApptableId()).with(changes)
                .build();
    }
}