/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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