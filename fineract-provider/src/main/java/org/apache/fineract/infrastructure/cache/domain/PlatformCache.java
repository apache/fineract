/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.cache.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "c_cache")
public class PlatformCache extends AbstractPersistableCustom<Long> {

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