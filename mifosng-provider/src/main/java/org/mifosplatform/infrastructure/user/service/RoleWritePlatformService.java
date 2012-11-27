package org.mifosplatform.infrastructure.user.service;

import org.mifosplatform.infrastructure.user.command.RoleCommand;
import org.mifosplatform.infrastructure.user.command.RolePermissionCommand;

public interface RoleWritePlatformService {

    Long createRole(RoleCommand command);

    Long updateRole(RoleCommand command);

    Long updateRolePermissions(RolePermissionCommand command);
}