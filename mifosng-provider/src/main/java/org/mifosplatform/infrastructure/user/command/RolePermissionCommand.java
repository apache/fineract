package org.mifosplatform.infrastructure.user.command;

import java.util.Map;

/**
 * Immutable command for updating permissions against a role.
 */
public class RolePermissionCommand {

    private final transient Long roleId;
    private final Map<String, Boolean> permissions;

    private final transient boolean makerCheckerApproval;

    public RolePermissionCommand(final Long roleId, final Map<String, Boolean> permissionsMap, final boolean makerCheckerApproval) {
        this.roleId = roleId;
        this.permissions = permissionsMap;
        this.makerCheckerApproval = makerCheckerApproval;
    }

    public Map<String, Boolean> getPermissions() {
        return this.permissions;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }
}