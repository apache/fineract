package org.mifosplatform.infrastructure.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.DefaultKeyGenerator;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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