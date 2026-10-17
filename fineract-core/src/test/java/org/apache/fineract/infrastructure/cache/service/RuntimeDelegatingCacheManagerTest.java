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

import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class RuntimeDelegatingCacheManagerTest {

    private static final String CACHE = "configByName";

    private ConcurrentMapCacheManager ehCacheManager;
    private RecordingTransactionScopedCacheManager transactionScoped;
    private RuntimeDelegatingCacheManager underTest;

    @BeforeEach
    void setUp() throws Exception {
        ehCacheManager = new ConcurrentMapCacheManager(CACHE);
        transactionScoped = new RecordingTransactionScopedCacheManager();
        underTest = new RuntimeDelegatingCacheManager(ehCacheManager, transactionScoped);
        underTest.afterPropertiesSet();
    }

    @Test
    void forwardsTransactionHooksWhileTheTransactionScopedManagerIsActive() {
        underTest.afterBegin();
        underTest.afterCompletion();

        assertThat(transactionScoped.begins).isEqualTo(1);
        assertThat(transactionScoped.completions).isEqualTo(1);
    }

    @Test
    void doesNotClearSharedCachesOnTransactionHooksOnceSwitchedToSingleNode() {
        underTest.switchToCache(false, CacheType.SINGLE_NODE);
        Cache cache = underTest.getCache(CACHE);
        cache.put("tenant::all", "value");

        underTest.afterBegin();
        underTest.afterCompletion();

        assertThat(transactionScoped.begins).isZero();
        assertThat(transactionScoped.completions).isZero();
        assertThat(cache.get("tenant::all", String.class)).isEqualTo("value");
    }

    @Test
    void forwardsAgainAfterSwitchingBackToNoCache() {
        underTest.switchToCache(false, CacheType.SINGLE_NODE);
        underTest.switchToCache(true, CacheType.NO_CACHE);

        underTest.afterBegin();

        assertThat(transactionScoped.begins).isEqualTo(1);
    }

    /** Minimal transaction-scoped manager that only counts the hooks it receives. */
    private static final class RecordingTransactionScopedCacheManager implements TransactionScopedCacheManager {

        private final ConcurrentMapCacheManager delegate = new ConcurrentMapCacheManager(CACHE);
        private int begins;
        private int completions;

        @Override
        public void afterBegin() {
            begins++;
        }

        @Override
        public void afterCompletion() {
            completions++;
        }

        @Override
        public Cache getCache(String name) {
            return delegate.getCache(name);
        }

        @Override
        public Collection<String> getCacheNames() {
            return List.of(CACHE);
        }
    }
}
