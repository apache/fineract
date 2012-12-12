package org.mifosplatform.commands.handler;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface NewCommandSourceHandler {

    EntityIdentifier processCommand(JsonCommand command);
}
