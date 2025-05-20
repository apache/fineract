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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CacheConfig {

    public static final String CONFIG_BY_NAME_CACHE_NAME = "configByName";
    @Autowired
    private FineractProperties fineractProperties;

    @Bean
    public TransactionBoundCacheManager defaultCacheManager(JCacheCacheManager ehCacheManager) {
        SpecifiedCacheSupportingCacheManager cacheManager = new SpecifiedCacheSupportingCacheManager();
        cacheManager.setNoOpCacheManager(new NoOpCacheManager());
        cacheManager.setDelegateCacheManager(ehCacheManager);
        cacheManager.setSupportedCaches(CONFIG_BY_NAME_CACHE_NAME);
        return new TransactionBoundCacheManager(cacheManager);
    }

    @Bean
    public JCacheCacheManager ehCacheManager() {
        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
        jCacheCacheManager.setCacheManager(getInternalEhCacheManager());
        return jCacheCacheManager;
    }

    private CacheManager getInternalEhCacheManager() {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        // Default cache configuration template
        Duration defaultTimeToLive = fineractProperties.getCache().getDefaultTemplate().getTtl();
        Integer defaultMaxEntries = fineractProperties.getCache().getDefaultTemplate().getMaximumEntries();
        javax.cache.configuration.Configuration<Object, Object> defaultTemplate = generateCacheConfiguration(defaultMaxEntries,
                defaultTimeToLive);
        // Scan all packages (entire classpath)
        Reflections reflections = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath())
                .addScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated));
        // Find all methods annotated with @Cacheable
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(Cacheable.class);
        Set<String> cacheNames = annotatedMethods.stream().map(method -> method.getAnnotation(Cacheable.class))
                .flatMap(annotation -> Stream.concat(Arrays.stream(annotation.value()), Arrays.stream(annotation.cacheNames())))
                .collect(Collectors.toSet());
        // Find all types annotated with @Cacheable
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Cacheable.class);
        cacheNames.addAll(annotatedClasses.stream().map(clazz -> clazz.getAnnotation(Cacheable.class))
                .flatMap(annotation -> Stream.concat(Arrays.stream(annotation.value()), Arrays.stream(annotation.cacheNames())))
                .collect(Collectors.toSet()));
        // Find all types annotated with @CacheConfig
        Set<Class<?>> annotatedCacheConfigClasses = reflections
                .getTypesAnnotatedWith(org.springframework.cache.annotation.CacheConfig.class);
        cacheNames.addAll(annotatedCacheConfigClasses.stream()
                .map(clazz -> clazz.getAnnotation(org.springframework.cache.annotation.CacheConfig.class))
                .flatMap(annotation -> Arrays.stream(annotation.cacheNames())).collect(Collectors.toSet()));
        // Register the caches into the cache manager
        cacheNames.forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) == null) {
                javax.cache.configuration.Configuration<Object, Object> configurationTemplate = generateCustomCacheConfiguration(cacheName,
                        defaultTemplate, defaultTimeToLive, defaultMaxEntries);
                cacheManager.createCache(cacheName, configurationTemplate);
            }
        });
        Set<String> incorrectConfigurations = new HashSet<>(fineractProperties.getCache().getCustomTemplates().keySet());
        incorrectConfigurations.removeAll(cacheNames);
        if (!incorrectConfigurations.isEmpty()) {
            log.warn("The following cache configurations are defined but cache does not exists: {}", incorrectConfigurations);
        }
        return cacheManager;
    }

    private javax.cache.configuration.Configuration<Object, Object> generateCustomCacheConfiguration(String cacheIdentifier,
            javax.cache.configuration.Configuration<Object, Object> defaultTemplate, Duration defaultTimeToLive,
            Integer defaultMaxEntries) {
        javax.cache.configuration.Configuration<Object, Object> configurationTemplate = defaultTemplate;
        if (fineractProperties.getCache().getCustomTemplates().containsKey(cacheIdentifier)) {
            Duration timeToLiveExpiration = Objects.requireNonNullElse(
                    fineractProperties.getCache().getCustomTemplates().get(cacheIdentifier).getTtl(), defaultTimeToLive);
            Integer maxEntries = Objects.requireNonNullElse(
                    fineractProperties.getCache().getCustomTemplates().get(cacheIdentifier).getMaximumEntries(), defaultMaxEntries);
            configurationTemplate = generateCacheConfiguration(maxEntries, timeToLiveExpiration);
        }
        return configurationTemplate;
    }

    private static javax.cache.configuration.Configuration<Object, Object> generateCacheConfiguration(Integer defaultMaxEntries,
            Duration defaultTimeToLive) {
        return Eh107Configuration.fromEhcacheCacheConfiguration(CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(defaultMaxEntries))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(defaultTimeToLive)).build());
    }
}
