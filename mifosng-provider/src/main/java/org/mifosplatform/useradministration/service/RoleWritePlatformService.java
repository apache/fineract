package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface RoleWritePlatformService {

    CommandProcessingResult createRole(JsonCommand command);

    CommandProcessingResult updateRole(Long roleId, JsonCommand command);

    CommandProcessingResult updateRolePermissions(Long roleId, JsonCommand command);
}