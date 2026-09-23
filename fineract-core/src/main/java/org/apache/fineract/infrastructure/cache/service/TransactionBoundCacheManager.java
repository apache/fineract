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
package org.apache.fineract.infrastructure.cache.service;

import java.util.Collection;
import java.util.Set;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;

/**
 * Cache manager whose entries live for a single transaction of the current thread.
 * <p>
 * The supported cache names come from the delegate (see {@code CacheConfig}); every other name resolves to a no-op
 * cache. Storage is thread-local ({@link ThreadLocalCacheStore}): it is cleared when a transaction begins and again
 * when it completes on that thread, so a transaction always starts from the database, exactly as before, but other
 * threads' transactions no longer wipe the entries and no lock is shared between threads.
 * <p>
 * It is deliberately not a {@code TransactionLifecycleCallback}: {@link RuntimeDelegatingCacheManager} forwards the
 * transaction hooks only while this manager is the active one, so the transaction-scoped clearing never touches the
 * caches of another mode (for example the TTL-based single-node cache).
 */
public class TransactionBoundCacheManager implements CacheManager {

    private final Set<String> supportedCacheNames;
    private final NoOpCacheManager noOpCacheManager = new NoOpCacheManager();

    public TransactionBoundCacheManager(final CacheManager delegate) {
        this.supportedCacheNames = Set.copyOf(delegate.getCacheNames());
    }

    /** Called right after a transaction has begun on the current thread. */
    public void afterBegin() {
        ThreadLocalCacheStore.clear();
    }

    /** Called after a transaction has completed (committed or rolled back) on the current thread. */
    public void afterCompletion() {
        ThreadLocalCacheStore.clear();
    }

    @Override
    public Cache getCache(final String name) {
        if (supportedCacheNames.contains(name)) {
            return ThreadLocalCacheStore.getCache(name);
        }
        return noOpCacheManager.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return supportedCacheNames;
    }
}
