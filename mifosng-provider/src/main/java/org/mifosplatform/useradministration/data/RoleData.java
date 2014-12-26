/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.data;

import java.io.Serializable;
import java.util.Collection;

/**
 * Immutable data object for role data.
 */
public class RoleData implements Serializable {

    private final Long id;
    private final String name;
    private final String description;
    private final Boolean disabled;

    public RolePermissionsData toRolePermissionData(final Collection<PermissionData> permissionUsageData) {
        return new RolePermissionsData(this.id, this.name, this.description, this.disabled, permissionUsageData);
    }

    public RoleData(final Long id, final String name, final String description, final Boolean disabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.disabled = disabled;
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