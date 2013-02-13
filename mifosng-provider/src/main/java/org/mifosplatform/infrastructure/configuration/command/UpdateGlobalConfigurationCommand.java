/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.command;

import java.util.Map;

/**
 * Immutable command for updating global configuration settings.
 */
public class UpdateGlobalConfigurationCommand {

    private final Map<String, Boolean> globalConfiguration;

    public UpdateGlobalConfigurationCommand(final Map<String, Boolean> globalConfigurationMap) {
        this.globalConfiguration = globalConfigurationMap;
    }

    public Map<String, Boolean> getGlobalConfiguration() {
        return this.globalConfiguration;
    }
}