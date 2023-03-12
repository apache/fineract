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
package org.apache.fineract.infrastructure.core.config.cache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.util.Assert;

@RequiredArgsConstructor
public class SpecifiedCacheSupportingCacheManager implements CacheManager, InitializingBean {

    private JCacheCacheManager delegateCacheManager;
    private NoOpCacheManager noOpCacheManager;

    private final Set<String> supportedCacheNames = new LinkedHashSet<>(16);

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegateCacheManager, "cacheManager cannot be null");
        Assert.notNull(noOpCacheManager, "delegate cannot be null");
        Assert.notEmpty(supportedCacheNames, "supportedCacheNames must not be empty");
        delegateCacheManager.afterPropertiesSet();
    }

    @Override
    public Cache getCache(String name) {
        if (supportedCacheNames.contains(name)) {
            Cache cache = delegateCacheManager.getCache(name);
            if (cache != null) {
                return cache;
            } else {
                return noOpCacheManager.getCache(name);
            }
        } else {
            return noOpCacheManager.getCache(name);
        }
    }

    @Override
    public Collection<String> getCacheNames() {
        synchronized (supportedCacheNames) {
            return Collections.unmodifiableSet(supportedCacheNames);
        }
    }

    public void setDelegateCacheManager(JCacheCacheManager delegateCacheManager) {
        this.delegateCacheManager = delegateCacheManager;
    }

    public void setNoOpCacheManager(NoOpCacheManager noOpCacheManager) {
        this.noOpCacheManager = noOpCacheManager;
    }

    public void setSupportedCaches(String... cacheNames) {
        supportedCacheNames.addAll(Arrays.asList(cacheNames));
    }
}
