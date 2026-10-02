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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.apache.fineract.infrastructure.core.config.cache.TransactionBoundCacheManager;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class GlobalConfigurationRepositoryWrapperCacheTest {

    private GlobalConfigurationRepository repository;
    private TransactionBoundCacheManager cacheManager;
    private GlobalConfigurationRepositoryWrapper underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "Asia/Kolkata", null));
        repository = mock(GlobalConfigurationRepository.class);
        // Build the stubs before stubbing findAll: nesting when() calls trips Mockito's unfinished-stubbing check.
        final List<GlobalConfigurationProperty> properties = List.of(property("maker-checker"), property("enable-business-date"));
        when(repository.findAllReadOnly()).thenReturn(properties);
        cacheManager = new TransactionBoundCacheManager(
                new ConcurrentMapCacheManager(GlobalConfigurationRepositoryWrapper.CONFIG_BY_NAME_CACHE_NAME));
        underTest = new GlobalConfigurationRepositoryWrapper(repository, cacheManager);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void loadsTheWholeTableOncePerTransactionWhateverTheNumberOfLookups() {
        cacheManager.afterBegin();

        assertThat(underTest.findOneByNameWithNotFoundDetection("maker-checker").getName()).isEqualTo("maker-checker");
        assertThat(underTest.findOneByNameWithNotFoundDetection("enable-business-date").getName()).isEqualTo("enable-business-date");
        assertThat(underTest.findOneByNameWithNotFoundDetection("maker-checker").getName()).isEqualTo("maker-checker");

        verify(repository, times(1)).findAllReadOnly();
    }

    @Test
    void reloadsAtTheNextTransaction() {
        cacheManager.afterBegin();
        underTest.findOneByNameWithNotFoundDetection("maker-checker");
        cacheManager.afterCompletion();

        cacheManager.afterBegin();
        underTest.findOneByNameWithNotFoundDetection("maker-checker");

        verify(repository, times(2)).findAllReadOnly();
    }

    @Test
    void reloadsAfterAnEviction() {
        underTest.findOneByNameWithNotFoundDetection("maker-checker");
        underTest.removeFromCache("maker-checker");
        underTest.findOneByNameWithNotFoundDetection("maker-checker");

        verify(repository, times(2)).findAllReadOnly();
    }

    @Test
    void reportsUnknownPropertiesAsNotFound() {
        assertThatThrownBy(() -> underTest.findOneByNameWithNotFoundDetection("does-not-exist"))
                .isInstanceOf(GlobalConfigurationPropertyNotFoundException.class);
    }

    private static GlobalConfigurationProperty property(String name) {
        GlobalConfigurationProperty property = mock(GlobalConfigurationProperty.class);
        when(property.getName()).thenReturn(name);
        return property;
    }
}
