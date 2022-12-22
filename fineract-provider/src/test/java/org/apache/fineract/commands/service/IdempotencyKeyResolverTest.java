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
package org.apache.fineract.commands.service;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.domain.BatchRequestContextHolder;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class IdempotencyKeyResolverTest {

    @Mock
    private IdempotencyKeyGenerator idempotencyKeyGenerator;

    @InjectMocks
    private IdempotencyKeyResolver underTest;

    @Spy
    private FineractRequestContextHolder fineractRequestContextHolder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        BatchRequestContextHolder.setRequestAttributes(new HashMap<>());
    }

    @AfterEach
    public void tearDown() {
        BatchRequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void testIPKResolveFromRequest() {
        String idk = "bar";
        fineractRequestContextHolder.setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idk);
        CommandWrapper wrapper = CommandWrapper.wrap("act", "ent", 1L, 1L);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }

    @Test
    public void testIPKResolveFromGenerate() {
        String idk = "idk";
        when(idempotencyKeyGenerator.create()).thenReturn(idk);
        CommandWrapper wrapper = CommandWrapper.wrap("act", "ent", 1L, 1L);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }

    @Test
    public void testIPKResolveFromWrapper() {
        String idk = "idk";
        CommandWrapper wrapper = new CommandWrapper(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, idk);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }
}
