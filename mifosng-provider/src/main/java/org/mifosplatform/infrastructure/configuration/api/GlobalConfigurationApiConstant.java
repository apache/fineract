package org.mifosplatform.infrastructure.configuration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GlobalConfigurationApiConstant {

    public static final String ENABLED = "enabled";
    public static final String VALUE = "value";
    public static final String ID = "id";
    public static final String CONFIGURATION_RESOURCE_NAME = "globalConfiguration";

    public static final Set<String> UPDATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(ENABLED, VALUE));

}
