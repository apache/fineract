/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CacheApiConstants {

    public static final String RESOURCE_NAME = "CACHE";
    public static final String cacheTypeParameter = "cacheType";
    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(cacheTypeParameter));

}
