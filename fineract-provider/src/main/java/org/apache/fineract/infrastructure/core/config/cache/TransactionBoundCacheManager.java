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

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.persistence.TransactionLifecycleCallback;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@RequiredArgsConstructor
public class TransactionBoundCacheManager implements TransactionLifecycleCallback, CacheManager {

    private final CacheManager delegate;

    @Override
    public void afterCompletion() {
        resetCaches();
    }

    @Override
    public void afterBegin() {
        resetCaches();
    }

    private void resetCaches() {
        Collection<String> cacheNames = delegate.getCacheNames();
        cacheNames.forEach(c -> {
            Cache cache = delegate.getCache(c);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Override
    public Cache getCache(String name) {
        return delegate.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
