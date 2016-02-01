/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.cache;

import org.mifosplatform.infrastructure.cache.service.RuntimeDelegatingCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.DefaultKeyGenerator;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("deprecation")
@Configuration
@EnableCaching
public class PlatformCacheConfiguration implements CachingConfigurer {

    @Autowired
    private RuntimeDelegatingCacheManager delegatingCacheManager;

    @Bean
    @Override
    public CacheManager cacheManager() {
        return this.delegatingCacheManager;
    }

    @Override
    public KeyGenerator keyGenerator() {
        return new DefaultKeyGenerator();
    }
}