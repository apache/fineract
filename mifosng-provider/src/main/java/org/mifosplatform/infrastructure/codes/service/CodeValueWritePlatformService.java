package org.mifosplatform.infrastructure.codes.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CodeValueWritePlatformService {

    CommandProcessingResult createCodeValue(JsonCommand command);

    CommandProcessingResult updateCodeValue(Long codeValueId, JsonCommand command);

    CommandProcessingResult deleteCodeValue(Long codeValueId);
}