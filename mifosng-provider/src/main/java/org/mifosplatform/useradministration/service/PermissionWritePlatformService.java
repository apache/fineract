package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PermissionWritePlatformService {

    CommandProcessingResult updateMakerCheckerPermissions(JsonCommand command);
}