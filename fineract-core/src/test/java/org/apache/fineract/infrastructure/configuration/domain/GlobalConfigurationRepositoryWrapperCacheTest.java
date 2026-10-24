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
import org.apache.fineract.infrastructure.cache.service.TransactionBoundCacheManager;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.exception.GlobalConfigurationPropertyNotFoundException;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class GlobalConfigurationRepositoryWrapperCacheTest {

    private GlobalConfigurationRepository repository;
    private TransactionBoundCacheManager cacheManager;
    private GlobalConfigurationRepositoryWrapper underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "test", "Test Tenant", "Asia/Kolkata", null));
        repository = mock(GlobalConfigurationRepository.class);
        when(repository.findAllReadOnly()).thenReturn(List.of(property("maker-checker", true), property("enable-business-date", false)));
        cacheManager = new TransactionBoundCacheManager(
                new ConcurrentMapCacheManager(GlobalConfigurationRepositoryWrapper.CONFIG_BY_NAME_CACHE_NAME));
        underTest = new GlobalConfigurationRepositoryWrapper(repository, cacheManager);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
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
        underTest.removeFromCache();
        underTest.findOneByNameWithNotFoundDetection("maker-checker");

        verify(repository, times(2)).findAllReadOnly();
    }

    @Test
    void evictsAgainAfterTheCommitSoALookupBeforeTheCommitCannotPinTheStaleSnapshot() {
        // A cache that outlives the transaction, as the single-node Ehcache does
        underTest = new GlobalConfigurationRepositoryWrapper(repository,
                new ConcurrentMapCacheManager(GlobalConfigurationRepositoryWrapper.CONFIG_BY_NAME_CACHE_NAME));
        underTest.findOneByNameWithNotFoundDetection("maker-checker");

        TransactionSynchronizationManager.initSynchronization();
        underTest.removeFromCache();
        // The maker-checker check of the command pipeline runs after the handler, inside the same transaction: it
        // reloads and caches the snapshot before the change is committed
        underTest.findOneByNameWithNotFoundDetection("maker-checker");
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        TransactionSynchronizationManager.clearSynchronization();

        underTest.findOneByNameWithNotFoundDetection("maker-checker");

        verify(repository, times(3)).findAllReadOnly();
    }

    @Test
    void evictsOnlyOnceOutsideATransaction() {
        underTest.findOneByNameWithNotFoundDetection("maker-checker");
        underTest.removeFromCache();

        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isFalse();
        underTest.findOneByNameWithNotFoundDetection("maker-checker");
        verify(repository, times(2)).findAllReadOnly();
    }

    @Test
    void modifyingAReturnedPropertyDoesNotChangeTheCachedConfiguration() {
        final GlobalConfigurationPropertyData returned = underTest.findOneByNameWithNotFoundDetection("maker-checker");
        returned.setEnabled(false).setValue(99L);

        final GlobalConfigurationPropertyData reread = underTest.findOneByNameWithNotFoundDetection("maker-checker");

        assertThat(reread).isNotSameAs(returned);
        assertThat(reread.isEnabled()).isTrue();
        assertThat(reread.getValue()).isNull();
        verify(repository, times(1)).findAllReadOnly();
    }

    @Test
    void reportsUnknownPropertiesAsNotFound() {
        assertThatThrownBy(() -> underTest.findOneByNameWithNotFoundDetection("does-not-exist"))
                .isInstanceOf(GlobalConfigurationPropertyNotFoundException.class);
    }

    private static GlobalConfigurationProperty property(final String name, final boolean enabled) {
        return new GlobalConfigurationProperty().setName(name).setEnabled(enabled);
    }
}
