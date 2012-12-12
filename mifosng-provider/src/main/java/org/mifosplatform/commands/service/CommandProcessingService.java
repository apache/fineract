package org.mifosplatform.commands.service;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface CommandProcessingService {

    EntityIdentifier processAndLogCommand(CommandWrapper wrapper, JsonCommand command, boolean isApprovedByChecker);

    EntityIdentifier logCommand(CommandWrapper wrapper, JsonCommand command);

}
