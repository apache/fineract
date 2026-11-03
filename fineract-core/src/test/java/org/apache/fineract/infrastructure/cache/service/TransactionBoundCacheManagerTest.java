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

import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class TransactionBoundCacheManagerTest {

    private static final String CACHE = "configByName";
    private static final String KEY = "tenant::all";

    private TransactionBoundCacheManager underTest;

    @BeforeEach
    void setUp() {
        underTest = new TransactionBoundCacheManager(new ConcurrentMapCacheManager(CACHE));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalCacheStore.remove();
    }

    @Test
    void transactionHooksOnAnotherThreadDoNotClearThisThreadsEntries() throws InterruptedException {
        underTest.getCache(CACHE).put(KEY, "thread-a");
        final AtomicReference<Object> seenByOtherThread = new AtomicReference<>("not-run");

        final Thread otherThread = new Thread(() -> {
            underTest.afterBegin();
            final Cache.ValueWrapper value = underTest.getCache(CACHE).get(KEY);
            seenByOtherThread.set(value == null ? null : value.get());
            underTest.afterCompletion();
            ThreadLocalCacheStore.remove();
        });
        otherThread.start();
        otherThread.join();

        assertThat(seenByOtherThread.get()).isNull();
        assertThat(underTest.getCache(CACHE).get(KEY, String.class)).isEqualTo("thread-a");
    }

    @Test
    void contextResetDropsTheThreadsStore() {
        final Cache beforeReset = underTest.getCache(CACHE);
        beforeReset.put(KEY, "value");

        ThreadLocalContextUtil.reset();

        final Cache afterReset = underTest.getCache(CACHE);
        assertThat(afterReset).isNotSameAs(beforeReset);
        assertThat(afterReset.get(KEY)).isNull();
    }

    @Test
    void unsupportedCacheNamesDoNotStoreAnything() {
        final Cache cache = underTest.getCache("unsupported");
        cache.put(KEY, "value");

        assertThat(cache.get(KEY)).isNull();
    }
}
