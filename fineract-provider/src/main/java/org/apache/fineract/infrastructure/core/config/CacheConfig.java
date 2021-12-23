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

package org.apache.fineract.infrastructure.core.config;

import java.time.Duration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public JCacheCacheManager ehCacheManager() {
        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
        jCacheCacheManager.setCacheManager(getCustomCacheManager());
        return jCacheCacheManager;
    }

    private CacheManager getCustomCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();

        javax.cache.configuration.Configuration<Object, Object> defaultTemplate = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(10000))
                        .withExpiry(ExpiryPolicyBuilder.noExpiration()).build());

        cacheManager.createCache("users", defaultTemplate);
        cacheManager.createCache("usersByUsername", defaultTemplate);
        cacheManager.createCache("tenantsById", defaultTemplate);
        cacheManager.createCache("offices", defaultTemplate);
        cacheManager.createCache("officesForDropdown", defaultTemplate);
        cacheManager.createCache("officesById", defaultTemplate);
        cacheManager.createCache("charges", defaultTemplate);
        cacheManager.createCache("funds", defaultTemplate);
        cacheManager.createCache("code_values", defaultTemplate);
        cacheManager.createCache("codes", defaultTemplate);
        cacheManager.createCache("hooks", defaultTemplate);
        cacheManager.createCache("tfConfig", defaultTemplate);

        javax.cache.configuration.Configuration<Object, Object> accessTokenTemplate = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(10000))
                        .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofHours(2))).build());

        cacheManager.createCache("userTFAccessToken", accessTokenTemplate);

        return cacheManager;
    }
}
