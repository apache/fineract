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
package org.apache.fineract.infrastructure.configuration.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * Wrapper for {@link GlobalConfigurationRepository} that loads the whole configuration table once per cache lifetime
 * instead of issuing one query per property name.
 * <p>
 * The table is small (a few dozen rows), while a single write request looks up the same handful of properties over and
 * over: under load it was the most frequently executed statement of the platform (about 17 lookups per request). The
 * cache entry is a map of all properties keyed by name, stored under the tenant-scoped key {@code <tenant>::all} in the
 * {@value #CONFIG_BY_NAME_CACHE_NAME} cache. With the default transaction-bound cache manager the map lives for one
 * transaction, which keeps the consistency guarantees exactly as they were (every transaction still starts from the
 * database) with one round trip instead of one per property. With the single-node cache the map expires with the
 * configured TTL, as the per-name entries did before.
 */
@Service
@Slf4j
public class GlobalConfigurationRepositoryWrapper {

    public static final String CONFIG_BY_NAME_CACHE_NAME = "configByName";
    static final String ALL_PROPERTIES_KEY_SUFFIX = "::all";

    private final GlobalConfigurationRepository repository;
    private final CacheManager cacheManager;

    @Autowired
    public GlobalConfigurationRepositoryWrapper(final GlobalConfigurationRepository repository,
            @Qualifier("runtimeDelegatingCacheManager") final CacheManager cacheManager) {
        this.repository = repository;
        this.cacheManager = cacheManager;
    }

    public GlobalConfigurationProperty findOneByNameWithNotFoundDetection(final String propertyName) {
        final GlobalConfigurationProperty property = allPropertiesByName().get(propertyName);
        if (property == null) {
            throw new GlobalConfigurationPropertyNotFoundException(propertyName);
        }
        return property;
    }

    /**
     * Loads a property as a managed entity, bypassing the read-only snapshot. Use this when the property is going to be
     * modified and saved; read-only callers use {@link #findOneByNameWithNotFoundDetection(String)}.
     */
    public GlobalConfigurationProperty findOneByNameForUpdateWithNotFoundDetection(final String propertyName) {
        final GlobalConfigurationProperty property = this.repository.findOneByName(propertyName);
        if (property == null) {
            throw new GlobalConfigurationPropertyNotFoundException(propertyName);
        }
        return property;
    }

    public GlobalConfigurationProperty findOneWithNotFoundDetection(final Long configId) {
        return this.repository.findById(configId).orElseThrow(() -> new GlobalConfigurationPropertyNotFoundException(configId));
    }

    public void save(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.save(globalConfigurationProperty);
    }

    public void saveAndFlush(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.saveAndFlush(globalConfigurationProperty);
    }

    public void delete(final GlobalConfigurationProperty globalConfigurationProperty) {
        this.repository.delete(globalConfigurationProperty);
    }

    /**
     * Drops the cached configuration of the current tenant so that the next lookup reloads it from the database. The
     * property name is kept in the signature for the existing callers; the whole map is evicted because a change to any
     * property invalidates it.
     */
    public void removeFromCache(final String propertyName) {
        final Cache cache = cacheManager.getCache(CONFIG_BY_NAME_CACHE_NAME);
        if (cache != null) {
            cache.evict(allPropertiesKey());
        }
        log.debug("Cache entry evicted {}", propertyName);
    }

    private Map<String, GlobalConfigurationProperty> allPropertiesByName() {
        final Cache cache = cacheManager.getCache(CONFIG_BY_NAME_CACHE_NAME);
        if (cache == null) {
            return loadAllPropertiesByName();
        }
        return cache.get(allPropertiesKey(), this::loadAllPropertiesByName);
    }

    private Map<String, GlobalConfigurationProperty> loadAllPropertiesByName() {
        return this.repository.findAllReadOnly().stream().collect(
                Collectors.toMap(GlobalConfigurationProperty::getName, Function.identity(), (first, second) -> first, LinkedHashMap::new));
    }

    private static String allPropertiesKey() {
        return ThreadLocalContextUtil.getTenant().getTenantIdentifier() + ALL_PROPERTIES_KEY_SUFFIX;
    }
}
