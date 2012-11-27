package org.mifosplatform.infrastructure.user.service;

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.RolePermissionCommand;

public interface RoleWritePlatformService {

    Long createRole(RoleCommand command);

    Long updateRole(RoleCommand command);

    Long updateRolePermissions(RolePermissionCommand command);
}