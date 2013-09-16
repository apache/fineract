/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache.data;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;

public class CacheData {

    @SuppressWarnings("unused")
    private final EnumOptionData cacheType;
    @SuppressWarnings("unused")
    private final boolean enabled;

    public static CacheData instance(final EnumOptionData cacheType, final boolean enabled) {
        return new CacheData(cacheType, enabled);
    }

    private CacheData(final EnumOptionData cacheType, final boolean enabled) {
        this.cacheType = cacheType;
        this.enabled = enabled;
    }
}