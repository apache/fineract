package org.mifosplatform.infrastructure.configuration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface GlobalConfigurationWritePlatformService {

    CommandProcessingResult update(JsonCommand command);
}