package org.mifosplatform.useradministration.service;

import org.mifosplatform.useradministration.command.RoleCommand;
import org.mifosplatform.useradministration.command.RolePermissionCommand;

public interface RoleWritePlatformService {

    Long createRole(RoleCommand command);

    Long updateRole(RoleCommand command);

    Long updateRolePermissions(RolePermissionCommand command);
}