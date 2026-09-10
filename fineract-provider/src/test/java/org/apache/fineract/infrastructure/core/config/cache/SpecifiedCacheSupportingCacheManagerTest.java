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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SpecifiedCacheSupportingCacheManagerTest {

    @Test
    void shouldAllowConcurrentAccessToSupportedCacheNames() throws Exception {
        SpecifiedCacheSupportingCacheManager cacheManager = new SpecifiedCacheSupportingCacheManager();
        cacheManager.setSupportedCaches("cache1", "cache2", "cache3");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        Future<?> reader = executor.submit(() -> {
            startSignal.await();

            for (int i = 0; i < 1_000; i++) {
                Collection<String> cacheNames = cacheManager.getCacheNames();

                for (String name : cacheNames) {
                    assertNotNull(name);
                }
            }

            return null;
        });

        Future<?> writer = executor.submit(() -> {
            startSignal.await();

            for (int i = 0; i < 1_000; i++) {
                cacheManager.setSupportedCaches("cache-" + i);
            }

            return null;
        });

        startSignal.countDown();

        try {
            reader.get(10, TimeUnit.SECONDS);
            writer.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}
