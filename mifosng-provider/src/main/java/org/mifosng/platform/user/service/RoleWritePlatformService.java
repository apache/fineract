package org.mifosng.platform.user.service;

import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.RolePermissionCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface RoleWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'CREATE_ROLE')")
    Long createRole(RoleCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'UPDATE_ROLE')")
    Long updateRole(RoleCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'PERMISSIONS_ROLE')")
    Long updateRolePermissions(RolePermissionCommand command);
}