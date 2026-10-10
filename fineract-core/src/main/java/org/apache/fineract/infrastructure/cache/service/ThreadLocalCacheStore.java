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

import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

/**
 * Per-thread cache storage for the transaction-bound cache manager.
 * <p>
 * Entries live on the current thread only: they are cleared when a transaction begins or completes on that thread and
 * when the request-scoped context is reset. A shared cache cleared on every transaction boundary of every thread has,
 * under concurrent load, a hit rate close to zero and pays a global {@code clear()} per transaction; a thread-local
 * store gives each transaction its own consistent snapshot without any cross-thread interaction.
 */
public final class ThreadLocalCacheStore {

    private static final ThreadLocal<Map<String, Cache>> CACHES = ThreadLocal.withInitial(HashMap::new);

    private ThreadLocalCacheStore() {}

    /** Returns the current thread's cache with the given name, creating it on first use. */
    public static Cache getCache(final String name) {
        return CACHES.get().computeIfAbsent(name, ConcurrentMapCache::new);
    }

    /** Empties every cache of the current thread. Existing {@link Cache} references stay valid but empty. */
    public static void clear() {
        final Map<String, Cache> caches = CACHES.get();
        if (!caches.isEmpty()) {
            caches.values().forEach(Cache::clear);
        }
    }

    /** Drops the current thread's caches entirely (end of request or of a job execution). */
    public static void remove() {
        CACHES.remove();
    }
}
