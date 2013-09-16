/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "c_cache")
public class PlatformCache extends AbstractPersistable<Long> {

    @Column(name = "cache_type_enum")
    private Integer cacheType;

    protected PlatformCache() {
        this.cacheType = null;
    }

    public PlatformCache(final CacheType cacheType) {
        this.cacheType = cacheType.getValue();
    }

    public boolean isNoCachedEnabled() {
        return CacheType.fromInt(this.cacheType).isNoCache();
    }

    public boolean isEhcacheEnabled() {
        return CacheType.fromInt(this.cacheType).isEhcache();
    }

    public boolean isDistributedCacheEnabled() {
        return CacheType.fromInt(this.cacheType).isDistributedCache();
    }

    public void update(final CacheType cacheType) {
        this.cacheType = cacheType.getValue();
    }
}