package org.mifosplatform.infrastructure.cache;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CacheApiConstants {

    public static final String RESOURCE_NAME = "CACHE";
    public static final String cacheTypeParameter = "cacheType";
    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<String>(Arrays.asList(cacheTypeParameter));

}
