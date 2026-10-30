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
package org.apache.fineract.infrastructure.hooks.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Runs {@link HookReadPlatformServiceImpl#retrieveHooksByEvent} through a real Spring cache, so the cache key is
 * checked, not only the repository call.
 */
@SpringJUnitConfig(HookReadPlatformServiceImplCacheTest.TestConfig.class)
class HookReadPlatformServiceImplCacheTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("hooks");
        }

        @Bean
        HookRepository hookRepository() {
            return mock(HookRepository.class);
        }

        @Bean
        HookReadPlatformService hookReadPlatformService(final HookRepository hookRepository) {
            return new HookReadPlatformServiceImpl(mock(JdbcTemplate.class), hookRepository);
        }
    }

    @Autowired
    private HookReadPlatformService hookReadPlatformService;
    @Autowired
    private HookRepository hookRepository;
    @Autowired
    private CacheManager cacheManager;

    private final Hook clientCreateHook = mock(Hook.class);
    private final Hook loanApproveHook = mock(Hook.class);

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        cacheManager.getCache("hooks").clear();
        reset(hookRepository);
        when(hookRepository.findAllHooksListeningToEvent("CLIENT", "CREATE")).thenReturn(List.of(clientCreateHook));
        when(hookRepository.findAllHooksListeningToEvent("LOAN", "APPROVE")).thenReturn(List.of(loanApproveHook));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void secondEventDoesNotGetTheHooksCachedForTheFirstEvent() {
        assertEquals(List.of(clientCreateHook), hookReadPlatformService.retrieveHooksByEvent("CLIENT", "CREATE"));
        assertEquals(List.of(loanApproveHook), hookReadPlatformService.retrieveHooksByEvent("LOAN", "APPROVE"));
    }

    @Test
    void hooksAreStillCachedPerEvent() {
        hookReadPlatformService.retrieveHooksByEvent("CLIENT", "CREATE");
        hookReadPlatformService.retrieveHooksByEvent("LOAN", "APPROVE");

        assertEquals(List.of(clientCreateHook), hookReadPlatformService.retrieveHooksByEvent("CLIENT", "CREATE"));
        assertEquals(List.of(loanApproveHook), hookReadPlatformService.retrieveHooksByEvent("LOAN", "APPROVE"));

        verify(hookRepository, times(1)).findAllHooksListeningToEvent("CLIENT", "CREATE");
        verify(hookRepository, times(1)).findAllHooksListeningToEvent("LOAN", "APPROVE");
    }
}
