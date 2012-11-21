package org.mifosng.platform.accounting.api.commands;

import java.util.Map;

public class RolePermissionCommand {

    private final Long roleId;
    private final Map<String, Boolean> permissions;

    public RolePermissionCommand(final Long roleId, final Map<String, Boolean> permissionsMap) {
        this.roleId = roleId;
        this.permissions = permissionsMap;
    }

    public Map<String, Boolean> getPermissions() {
        return this.permissions;
    }

    public Long getRoleId() {
        return this.roleId;
    }
}
