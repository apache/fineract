package org.mifosplatform.infrastructure.configuration.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Cieyou on 1/16/14.
 */
public class GlobalConfigurationApiConstant {


    public static final String enabled = "enabled";
    public static final String value = "value";
    public static final String id ="id";
    public static final String CONFIGURATION_RESOURCE_NAME = "globalConfiguration";


    public static final Set<String> UPDATE_CONFIGURATION_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(enabled,
            value));

}
