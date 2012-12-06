package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.useradministration.command.RoleCommand;

public interface RoleWritePlatformService {

    Long createRole(RoleCommand command);

    Long updateRole(RoleCommand command);

    EntityIdentifier updateRolePermissions(Long roleId, JsonCommand command);
}