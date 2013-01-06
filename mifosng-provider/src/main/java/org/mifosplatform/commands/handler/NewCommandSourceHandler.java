package org.mifosplatform.commands.handler;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface NewCommandSourceHandler {

    CommandProcessingResult processCommand(JsonCommand command);
}
