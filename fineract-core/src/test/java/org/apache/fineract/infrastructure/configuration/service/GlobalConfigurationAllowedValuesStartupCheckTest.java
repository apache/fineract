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
package org.apache.fineract.infrastructure.configuration.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationAllowedValues;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepository;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.tenant.TenantDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GlobalConfigurationAllowedValuesStartupCheckTest {

    private static final FineractPlatformTenant ORIGINAL_TENANT = FineractPlatformTenant.builder().tenantIdentifier("original").build();
    private static final FineractPlatformTenant FIRST_TENANT = FineractPlatformTenant.builder().tenantIdentifier("first").build();
    private static final FineractPlatformTenant SECOND_TENANT = FineractPlatformTenant.builder().tenantIdentifier("second").build();

    @Mock
    private TenantDetailsService tenantDetailsService;
    @Mock
    private GlobalConfigurationRepository globalConfigurationRepository;
    @InjectMocks
    private GlobalConfigurationAllowedValuesStartupCheck underTest;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(ORIGINAL_TENANT);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void readsEveryRestrictedConfigWithinEachTenant() {
        final List<String> tenantsRead = new ArrayList<>();
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of(FIRST_TENANT, SECOND_TENANT));
        when(globalConfigurationRepository.findOneByName(anyString())).thenAnswer(invocation -> {
            tenantsRead.add(ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            return new GlobalConfigurationProperty().setName(invocation.getArgument(0)).setStringValue("not-allowed");
        });

        underTest.afterPropertiesSet();

        final int restrictedConfigs = GlobalConfigurationAllowedValues.ALL.size();
        verify(globalConfigurationRepository, times(2 * restrictedConfigs)).findOneByName(anyString());
        assertEquals(restrictedConfigs, tenantsRead.stream().filter("first"::equals).count());
        assertEquals(restrictedConfigs, tenantsRead.stream().filter("second"::equals).count());
        assertSame(ORIGINAL_TENANT, ThreadLocalContextUtil.getTenant());
    }

    @Test
    void doesNotFailStartupWhenReadingATenantFails() {
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of(FIRST_TENANT, SECOND_TENANT));
        when(globalConfigurationRepository.findOneByName(anyString())).thenThrow(new IllegalStateException("database unavailable"));

        assertDoesNotThrow(underTest::afterPropertiesSet);

        // one failed read per tenant: the failure stops that tenant's check, not the next tenant's
        verify(globalConfigurationRepository, times(2)).findOneByName(anyString());
        assertSame(ORIGINAL_TENANT, ThreadLocalContextUtil.getTenant());
    }

    @Test
    void skipsConfigurationsMissingFromTheDatabase() {
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of(FIRST_TENANT));
        when(globalConfigurationRepository.findOneByName(anyString())).thenReturn(null);

        assertDoesNotThrow(underTest::afterPropertiesSet);
    }

    @Test
    void doesNothingWithoutTenants() {
        when(tenantDetailsService.findAllTenants()).thenReturn(List.of());

        underTest.afterPropertiesSet();

        verify(globalConfigurationRepository, never()).findOneByName(anyString());
    }
}
