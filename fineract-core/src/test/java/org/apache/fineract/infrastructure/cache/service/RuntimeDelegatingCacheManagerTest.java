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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class RuntimeDelegatingCacheManagerTest {

    private static final String CACHE = "configByName";
    private static final String KEY = "tenant::all";

    private TransactionBoundCacheManager transactionBound;
    private RuntimeDelegatingCacheManager underTest;

    @BeforeEach
    void setUp() throws Exception {
        transactionBound = new TransactionBoundCacheManager(new ConcurrentMapCacheManager(CACHE));
        underTest = new RuntimeDelegatingCacheManager(new ConcurrentMapCacheManager(CACHE), transactionBound);
        underTest.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        ThreadLocalCacheStore.remove();
    }

    @Test
    void clearsTransactionBoundEntriesOnTransactionHooksWhileActive() {
        Cache cache = underTest.getCache(CACHE);
        cache.put(KEY, "value");

        underTest.afterBegin();
        assertThat(cache.get(KEY)).isNull();

        cache.put(KEY, "value");
        underTest.afterCompletion();
        assertThat(cache.get(KEY)).isNull();
    }

    @Test
    void doesNotClearAnyCacheOnTransactionHooksOnceSwitchedToSingleNode() {
        Cache transactionBoundCache = transactionBound.getCache(CACHE);
        transactionBoundCache.put(KEY, "transaction-bound");
        underTest.switchToCache(false, CacheType.SINGLE_NODE);
        Cache singleNodeCache = underTest.getCache(CACHE);
        singleNodeCache.put(KEY, "single-node");

        underTest.afterBegin();
        underTest.afterCompletion();

        assertThat(singleNodeCache.get(KEY, String.class)).isEqualTo("single-node");
        assertThat(transactionBoundCache.get(KEY, String.class)).isEqualTo("transaction-bound");
    }

    @Test
    void clearsAgainAfterSwitchingBackToNoCache() {
        underTest.switchToCache(false, CacheType.SINGLE_NODE);
        underTest.switchToCache(true, CacheType.NO_CACHE);
        Cache cache = underTest.getCache(CACHE);
        cache.put(KEY, "value");

        underTest.afterBegin();

        assertThat(cache.get(KEY)).isNull();
    }
}
