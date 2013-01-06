package org.mifosplatform.commands.service;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CommandProcessingService {

    CommandProcessingResult processAndLogCommand(CommandWrapper wrapper, JsonCommand command, boolean isApprovedByChecker);

    CommandProcessingResult logCommand(CommandWrapper wrapper, JsonCommand command);

}
