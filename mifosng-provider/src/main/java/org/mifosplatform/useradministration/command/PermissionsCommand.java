package org.mifosplatform.useradministration.command;

import java.util.Map;

/**
 * Immutable command for updating permissions (initially maker-checker).
 */
public class PermissionsCommand {

    private final Map<String, Boolean> permissions;
    private final transient boolean makerCheckerApproval;

    public PermissionsCommand(final Map<String, Boolean> permissionsMap, final boolean makerCheckerApproval) {
        this.permissions = permissionsMap;
        this.makerCheckerApproval = makerCheckerApproval;
    }

    public Map<String, Boolean> getPermissions() {
        return this.permissions;
    }

    public boolean isApprovedByChecker() {
        return this.makerCheckerApproval;
    }
}