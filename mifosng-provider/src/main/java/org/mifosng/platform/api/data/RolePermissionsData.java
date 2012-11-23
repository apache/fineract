package org.mifosng.platform.api.data;

import java.util.Collection;

/**
 * Immutable data object representing a role with associated permissions.
 */
public class RolePermissionsData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;

    @SuppressWarnings("unused")
    private final Collection<PermissionUsageData> permissionUsageData;
    @SuppressWarnings("unused")
    private final Collection<PermissionUsageData> currentChanges;
    @SuppressWarnings("unused")
    private final Collection<Collection<PermissionUsageData>> allChanges;

    public RolePermissionsData(final Long id, final String name, final String description,
            final Collection<PermissionUsageData> permissionUsageData, 
            final Collection<PermissionUsageData> currentChanges, 
            final Collection<Collection<PermissionUsageData>> allChanges) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissionUsageData = permissionUsageData;
        this.currentChanges = currentChanges;
        this.allChanges = allChanges;
    }
}