/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.data;

import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData {

    private final Long id;
    private final String name;
    private final String description;

    public RolePermissionsData toRolePermissionData(final Collection<PermissionData> permissionUsageData) {
        return new RolePermissionsData(id, name, description, permissionUsageData);
    }

    public RoleData(final Long id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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