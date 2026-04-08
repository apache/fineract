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

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link GlobalConfigurationRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
@Slf4j
public class GlobalConfigurationRepositoryWrapper {

    private final GlobalConfigurationRepository repository;

    @Autowired
    public GlobalConfigurationRepositoryWrapper(final GlobalConfigurationRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "configByName", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#propertyName)")
    public GlobalConfigurationProperty findOneByNameWithNotFoundDetection(final String propertyName) {
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

    @CacheEvict(value = "configByName", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#propertyName)")
    public void removeFromCache(String propertyName) {
        log.debug("Cache entry evicted {}", propertyName);
    }
}
