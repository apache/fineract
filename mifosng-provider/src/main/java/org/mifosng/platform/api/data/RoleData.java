package org.mifosng.platform.api.data;

import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData {

    private final Long id;
    private final String name;
    private final String description;

    // @SuppressWarnings("unused")
    // private final Collection<RoleData> currentChanges;

    public RoleData(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public RoleData() {
        this.id = null;
        this.name = null;
        this.description = null;
    }

    public RoleData(final RoleData role) {
        this.id = role.id;
        this.name = role.name;
        this.description = role.description;
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

    public RolePermissionsData toRolePermissionData(final Collection<PermissionUsageData> permissionUsageData,
            final Collection<PermissionUsageData> currentChanges, final Collection<Collection<PermissionUsageData>> allChanges) {
        return new RolePermissionsData(id, name, description, permissionUsageData, currentChanges, allChanges);
    }
}