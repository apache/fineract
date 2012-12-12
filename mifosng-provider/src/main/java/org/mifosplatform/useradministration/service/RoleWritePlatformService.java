package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface RoleWritePlatformService {

    EntityIdentifier createRole(JsonCommand command);

    EntityIdentifier updateRole(Long roleId, JsonCommand command);

    EntityIdentifier updateRolePermissions(Long roleId, JsonCommand command);
}