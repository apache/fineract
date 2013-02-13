/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.data;

import java.util.List;

/**
 * Immutable data object for global configuration.
 */
public class GlobalConfigurationData {

    @SuppressWarnings("unused")
    private final List<GlobalConfigurationPropertyData> globalConfiguration;

    public GlobalConfigurationData(final List<GlobalConfigurationPropertyData> globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }
}