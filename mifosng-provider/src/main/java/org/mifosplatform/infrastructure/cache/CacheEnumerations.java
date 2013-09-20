/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache;

import org.mifosplatform.infrastructure.cache.domain.CacheType;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class CacheEnumerations {

    public static EnumOptionData cacheType(final int id) {
        return cacheType(CacheType.fromInt(id));
    }

    public static EnumOptionData cacheType(final CacheType cacheType) {
        EnumOptionData optionData = new EnumOptionData(CacheType.INVALID.getValue().longValue(), CacheType.INVALID.getCode(), "Invalid");
        switch (cacheType) {
            case INVALID:
                optionData = new EnumOptionData(CacheType.INVALID.getValue().longValue(), CacheType.INVALID.getCode(), "Invalid");
            break;
            case NO_CACHE:
                optionData = new EnumOptionData(CacheType.NO_CACHE.getValue().longValue(), CacheType.NO_CACHE.getCode(), "No cache");
            break;
            case SINGLE_NODE:
                optionData = new EnumOptionData(CacheType.SINGLE_NODE.getValue().longValue(), CacheType.SINGLE_NODE.getCode(),
                        "Single node");
            break;
            case MULTI_NODE:
                optionData = new EnumOptionData(CacheType.MULTI_NODE.getValue().longValue(), CacheType.MULTI_NODE.getCode(), "Multi node");
            break;
        }

        return optionData;
    }
}
