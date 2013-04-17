/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.data;

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
    private final Collection<PermissionData> permissionUsageData;

    public RolePermissionsData(final Long id, final String name, final String description,
            final Collection<PermissionData> permissionUsageData) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissionUsageData = permissionUsageData;
    }
}