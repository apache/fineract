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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateRequest;
import org.apache.fineract.infrastructure.hooks.data.HookUpdateResponse;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookRepository;
import org.apache.fineract.infrastructure.hooks.domain.HookTemplateRepository;
import org.apache.fineract.infrastructure.hooks.mapper.HookEventMapper;
import org.apache.fineract.infrastructure.hooks.processor.ProcessorHelper;
import org.apache.fineract.template.domain.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HookWritePlatformServiceImplTest {

    @Mock
    private HookRepository hookRepository;
    @Mock
    private HookTemplateRepository hookTemplateRepository;
    @Mock
    private TemplateRepository ugdTemplateRepository;
    @Mock
    private ProcessorHelper processorHelper;
    @Mock
    private HookEventMapper hookEventMapper;

    private HookWritePlatformServiceImpl service;
    private Hook hook;

    @BeforeEach
    void setUp() {
        service = new HookWritePlatformServiceImpl(hookRepository, hookTemplateRepository, ugdTemplateRepository, processorHelper,
                hookEventMapper);
        hook = new Hook().setName("Notify CRM").setIsActive(true);
        when(hookRepository.findById(1L)).thenReturn(Optional.of(hook));
    }

    @Test
    void updateHookAppliesNewDisplayNameAndActiveFlag() {
        final HookUpdateResponse response = service
                .updateHook(HookUpdateRequest.builder().id(1L).displayName("Notify CRM v2").isActive(false).build());

        assertEquals("Notify CRM v2", hook.getName());
        assertFalse(hook.getIsActive());
        assertTrue(response.getChanges().containsKey(HookUpdateRequest.Fields.displayName));
        assertTrue(response.getChanges().containsKey(HookUpdateRequest.Fields.isActive));
        verify(hookRepository).saveAndFlush(hook);
    }

    @Test
    void updateHookKeepsDisplayNameAndActiveFlagWhenNotSent() {
        service.updateHook(HookUpdateRequest.builder().id(1L).build());

        assertEquals("Notify CRM", hook.getName());
        assertTrue(hook.getIsActive());
        verify(hookRepository, never()).saveAndFlush(hook);
    }
}
