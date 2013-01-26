package org.mifosplatform.useradministration.data;

import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData {

    private final Long id;
    private final String name;
    private final String description;
    @SuppressWarnings("unused")
    private final RoleData currentChanges;

    public RolePermissionsData toRolePermissionData(final Collection<PermissionData> permissionUsageData,
            final Collection<PermissionData> currentChanges) {
        return new RolePermissionsData(id, name, description, permissionUsageData, currentChanges);
    }

    public static RoleData changes(final String name, final String description) {
        return new RoleData(null, name, description, null);
    }

    public static RoleData integrateChanges(final RoleData role, final RoleData currentChanges) {
        return new RoleData(role.id, role.name, role.description, currentChanges);
    }

    public RoleData(final Long id, final String name, final String description, final RoleData currentChanges) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currentChanges = currentChanges;
    }

    @Override
    public boolean equals(final Object obj) {
        final RoleData role = (RoleData) obj;
        return this.id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}