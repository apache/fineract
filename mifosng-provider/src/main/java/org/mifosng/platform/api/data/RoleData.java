package org.mifosng.platform.api.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData {

    private final Long id;
    private final String name;
    private final String description;

    @SuppressWarnings("unused")
    private final Collection<PermissionData> availablePermissions;
    private final Collection<PermissionData> selectedPermissions;
    
//    @SuppressWarnings("unused")
//    private final Collection<RoleData> currentChanges;
    
    public RoleData(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.availablePermissions = null;
        this.selectedPermissions = null;
    }

    public RoleData(final Long id, final String name, final String description, final Collection<PermissionData> selectedPermissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.availablePermissions = null;
        this.selectedPermissions = selectedPermissions;
    }

    public RoleData(final Collection<PermissionData> availablePermissions, final Collection<PermissionData> selectedPermissions) {
        this.id = null;
        this.name = null;
        this.description = null;
        this.availablePermissions = new ArrayList<PermissionData>(availablePermissions);
        this.selectedPermissions = new ArrayList<PermissionData>(selectedPermissions);
    }

    public RoleData(final RoleData role, final Collection<PermissionData> availablePermissions) {
        this.id = role.id;
        this.name = role.name;
        this.description = role.description;
        this.availablePermissions = availablePermissions;
        this.selectedPermissions = role.selectedPermissions;
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

    public Collection<PermissionData> selectedPermissions() {
        return this.selectedPermissions;
    }

    public RolePermissionsData toRolePermissionData(final Collection<PermissionUsageData> permissionUsageData,
            final Collection<PermissionUsageData> currentChanges, 
            final Collection<Collection<PermissionUsageData>> allChanges) {
        return new RolePermissionsData(id, name, description, permissionUsageData, currentChanges, allChanges);
    }
}