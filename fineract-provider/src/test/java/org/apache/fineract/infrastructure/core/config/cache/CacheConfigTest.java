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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.event.external.repository.ExternalEventConfigurationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

class CacheConfigTest {

    /**
     * The global configuration cache is accessed programmatically, not through {@code @Cacheable}, so the annotation
     * scan alone does not create it. Without an Ehcache cache the single-node mode silently stops caching it.
     */
    @Test
    void registersTheTransactionBoundCachesInEhcacheForTheSingleNodeMode() {
        final FineractProperties.FineractCacheDetails defaultTemplate = new FineractProperties.FineractCacheDetails();
        defaultTemplate.setTtl(Duration.ofMinutes(1));
        defaultTemplate.setMaximumEntries(100);
        final FineractProperties.FineractCache cacheProperties = new FineractProperties.FineractCache();
        cacheProperties.setDefaultTemplate(defaultTemplate);
        final FineractProperties fineractProperties = new FineractProperties();
        fineractProperties.setCache(cacheProperties);
        final CacheConfig underTest = new CacheConfig();
        ReflectionTestUtils.setField(underTest, "fineractProperties", fineractProperties);

        final JCacheCacheManager ehCacheManager = underTest.ehCacheManager();
        ehCacheManager.afterPropertiesSet();

        assertThat(ehCacheManager.getCache(GlobalConfigurationRepositoryWrapper.CONFIG_BY_NAME_CACHE_NAME)).isNotNull();
        assertThat(ehCacheManager.getCache(ExternalEventConfigurationRepository.EXTERNAL_EVENT_CONFIGURATION_CACHE_NAME)).isNotNull();
    }
}
