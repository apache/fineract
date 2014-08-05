/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GlobalConfigurationApiConstant {

    public static final String ENABLED = "enabled";
    public static final String VALUE = "value";
    public static final String ID = "id";
    public static final String CONFIGURATION_RESOURCE_NAME = "globalConfiguration";

    public static final Set<String> UPDATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ENABLED, VALUE));

}
