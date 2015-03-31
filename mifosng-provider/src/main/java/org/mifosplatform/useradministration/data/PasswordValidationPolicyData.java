/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.data;

import java.io.Serializable;

/**
 * Immutable data object for role data.
 */
public class PasswordValidationPolicyData implements Serializable {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final boolean active;

    public PasswordValidationPolicyData(final Long id, final Boolean active, final String description) {
        this.id = id;
        this.active = active;
        this.description = description;
    }

}