package org.mifosplatform.infrastructure.dataqueries.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDatatableEntryCommandHandler implements
		NewCommandSourceHandler {

	private final ReadWriteNonCoreDataService writePlatformService;

	@Autowired
	public CreateDatatableEntryCommandHandler(
			final ReadWriteNonCoreDataService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public EntityIdentifier processCommand(final JsonCommand command) {

		this.writePlatformService.newDatatableEntry(command.entityName(),
				command.resourceId(), command);

		return EntityIdentifier.resourceResult(command.resourceId(),
				command.commandId());
	}
}