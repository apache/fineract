package org.mifosplatform.useradministration.command;

import java.util.Map;

/**
 * Immutable command for updating permissions (initially maker-checker).
 */
public class PermissionsCommand {

    private final Map<String, Boolean> permissions;

    public PermissionsCommand(final Map<String, Boolean> permissionsMap) {
        this.permissions = permissionsMap;
    }

    public Map<String, Boolean> getPermissions() {
        return this.permissions;
    }
}