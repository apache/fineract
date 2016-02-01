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

import java.util.Map;

import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CacheWritePlatformServiceJpaRepositoryImpl implements CacheWritePlatformService {

    private final ConfigurationDomainService configurationDomainService;
    private final RuntimeDelegatingCacheManager cacheService;

    @Autowired
    public CacheWritePlatformServiceJpaRepositoryImpl(final ConfigurationDomainService configurationDomainService,
            @Qualifier("runtimeDelegatingCacheManager") final RuntimeDelegatingCacheManager cacheService) {
        this.configurationDomainService = configurationDomainService;
        this.cacheService = cacheService;
    }

    @Transactional
    @Override
    public Map<String, Object> switchToCache(final CacheType toCacheType) {

        final boolean ehCacheEnabled = this.configurationDomainService.isEhcacheEnabled();

        final Map<String, Object> changes = this.cacheService.switchToCache(ehCacheEnabled, toCacheType);

        if (!changes.isEmpty()) {
            this.configurationDomainService.updateCache(toCacheType);
        }

        return changes;
    }
}