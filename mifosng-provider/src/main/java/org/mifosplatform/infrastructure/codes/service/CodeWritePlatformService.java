package org.mifosplatform.infrastructure.codes.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface CodeWritePlatformService {

    CommandProcessingResult createCode(JsonCommand command);

    CommandProcessingResult updateCode(Long codeId, JsonCommand command);

    CommandProcessingResult deleteCode(Long codeId);
}