/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.configuration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ExternalServiceConfigurationApiConstant {

    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String EXTERNAL_SERVICE_RESOURCE_NAME = "externalServiceConfiguration";

    public static final Set<String> EXTERNAL_SERVICE_CONFIGURATION_DATA_PARAMETERS = new HashSet<>(Arrays.asList(NAME, VALUE));
}
