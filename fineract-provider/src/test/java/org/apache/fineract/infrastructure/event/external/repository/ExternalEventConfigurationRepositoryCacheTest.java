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
package org.apache.fineract.infrastructure.event.external.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.apache.fineract.infrastructure.cache.service.TransactionBoundCacheManager;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class ExternalEventConfigurationRepositoryCacheTest {

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldUseTenantScopedCacheKey() throws NoSuchMethodException {
        Method method = ExternalEventConfigurationRepository.class.getMethod("findExternalEventConfigurationByTypeWithNotFoundDetection",
                String.class);
        Cacheable cacheable = method.getAnnotation(Cacheable.class);

        assertThat(cacheable.key()).isEqualTo(
                "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#externalEventType)");
    }

    @Test
    void shouldClearExternalEventConfigurationCacheOnTransactionLifecycleCallbacks() {
        CacheManager delegate = new ConcurrentMapCacheManager(ExternalEventConfigurationRepository.EXTERNAL_EVENT_CONFIGURATION_CACHE_NAME);
        TransactionBoundCacheManager underTest = new TransactionBoundCacheManager(delegate);
        Cache cache = underTest.getCache(ExternalEventConfigurationRepository.EXTERNAL_EVENT_CONFIGURATION_CACHE_NAME);

        cache.put("aKey", true);
        assertThat(cache.get("aKey", Boolean.class)).isTrue();

        underTest.afterBegin();
        assertThat(cache.get("aKey")).isNull();

        cache.put("aKey", true);
        underTest.afterCompletion();
        assertThat(cache.get("aKey")).isNull();
    }
}
