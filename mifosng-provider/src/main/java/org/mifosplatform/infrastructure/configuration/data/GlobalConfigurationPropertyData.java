/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

/**
 * Immutable data object for global configuration property.
 */
public class GlobalConfigurationPropertyData {

    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private boolean enabled;

    public GlobalConfigurationPropertyData(final String name, final boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }
}