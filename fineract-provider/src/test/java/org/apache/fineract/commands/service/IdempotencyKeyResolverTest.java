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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private DeterministicIdempotencyKeyGenerator deterministicIdempotencyKeyGenerator;

    @Mock
    private IdempotencyKeyGenerator randomKeyGenerator;

    @InjectMocks
    private IdempotencyKeyResolver underTest;

    @Spy
    private FineractRequestContextHolder fineractRequestContextHolder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        BatchRequestContextHolder.setRequestAttributes(new HashMap<>());
        when(randomKeyGenerator.create()).thenReturn("random-key");
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
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    public void testIPKResolveFromWrapper() {
        String idk = "idk";
        CommandWrapper wrapper = new CommandWrapper(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, idk, null, null);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldUseHeaderIdempotencyKeyWhenPresent() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn("header-key");

        String result = underTest.resolve(wrapper);

        Assertions.assertEquals("header-key", result);
        verify(deterministicIdempotencyKeyGenerator, never()).generate(anyString(), anyString());
    }

    @Test
    void shouldFallbackToRandomWhenJsonMissing() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn(null);
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(1L);

        String result = underTest.resolve(wrapper);

        Assertions.assertEquals("random-key", result);
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldFallbackToRandomWhenNoClientAndEntity() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"a\":1}");
        when(wrapper.getClientId()).thenReturn(null);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getJobName()).thenReturn(null);

        String result = underTest.resolve(wrapper);

        Assertions.assertEquals("random-key", result);
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldFallbackToRandomForConfigurationsEndpoint() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"a\":1}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(1L);
        when(wrapper.getHref()).thenReturn("/configurations/123");

        String result = underTest.resolve(wrapper);

        Assertions.assertEquals("random-key", result);
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldFallbackToRandomForJobCommands() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"a\":1}");
        when(wrapper.getClientId()).thenReturn(null);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getJobName()).thenReturn("LOAN_CLOSE_OF_BUSINESS");

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertEquals("random-key", result.key());
        Assertions.assertFalse(result.isDeterministic());
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldFallbackToRandomForNonTransferCreate() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"name\":\"Test\"}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getActionName()).thenReturn("CREATE");
        when(wrapper.getEntityName()).thenReturn("CLIENT");
        when(wrapper.getHref()).thenReturn("/clients");
        when(wrapper.getJobName()).thenReturn(null);

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertEquals("random-key", result.key());
        Assertions.assertFalse(result.isDeterministic());
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldFallbackToRandomForNonCreateOperation() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"a\":1}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(1L);
        when(wrapper.getActionName()).thenReturn("UPDATE");
        when(wrapper.getEntityName()).thenReturn("ACCOUNTTRANSFER");
        when(wrapper.getHref()).thenReturn("/accounttransfers/1");
        when(wrapper.getJobName()).thenReturn(null);

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertEquals("random-key", result.key());
        Assertions.assertFalse(result.isDeterministic());
        verifyNoInteractions(deterministicIdempotencyKeyGenerator);
    }

    @Test
    void shouldMarkDeterministicFalseForRandomFallback() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn(null);

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertFalse(result.isDeterministic());
    }

    @Test
    void shouldUseDeterministicKeyForAccountTransfer() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"fromAccountId\":\"1\",\"toAccountId\":\"2\",\"transferAmount\":\"500\"}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getActionName()).thenReturn("CREATE");
        when(wrapper.getEntityName()).thenReturn("ACCOUNTTRANSFER");
        when(wrapper.getHref()).thenReturn("/accounttransfers");
        when(wrapper.getJobName()).thenReturn(null);
        when(deterministicIdempotencyKeyGenerator.generate(anyString(), anyString())).thenReturn("transfer-det-key");

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertEquals("transfer-det-key", result.key());
        Assertions.assertTrue(result.isDeterministic());
        verify(deterministicIdempotencyKeyGenerator).generate(anyString(), anyString());
    }

    @Test
    void shouldMarkDeterministicTrueForAccountTransfer() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"fromAccountId\":\"1\",\"toAccountId\":\"2\",\"transferAmount\":\"100\"}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getActionName()).thenReturn("CREATE");
        when(wrapper.getEntityName()).thenReturn("ACCOUNTTRANSFER");
        when(wrapper.getHref()).thenReturn("/accounttransfers");
        when(wrapper.getJobName()).thenReturn(null);
        when(deterministicIdempotencyKeyGenerator.generate(anyString(), anyString())).thenReturn("det-key");

        IdempotencyKeyResolver.ResolvedKey result = underTest.resolveWithMeta(wrapper);

        Assertions.assertEquals("det-key", result.key());
        Assertions.assertTrue(result.isDeterministic());
    }

    @Test
    void shouldReturnSameDeterministicKeyForRetryOfSameTransfer() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        when(wrapper.getIdempotencyKey()).thenReturn(null);
        when(wrapper.getJson()).thenReturn("{\"fromAccountId\":\"1\",\"toAccountId\":\"2\",\"transferAmount\":\"500\"}");
        when(wrapper.getClientId()).thenReturn(1L);
        when(wrapper.getEntityId()).thenReturn(null);
        when(wrapper.getActionName()).thenReturn("CREATE");
        when(wrapper.getEntityName()).thenReturn("ACCOUNTTRANSFER");
        when(wrapper.getHref()).thenReturn("/accounttransfers");
        when(wrapper.getJobName()).thenReturn(null);
        when(deterministicIdempotencyKeyGenerator.generate(anyString(), anyString())).thenReturn("same-key");

        String key1 = underTest.resolve(wrapper);
        fineractRequestContextHolder.setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, null);
        String key2 = underTest.resolve(wrapper);

        Assertions.assertEquals(key1, key2);
    }
}
