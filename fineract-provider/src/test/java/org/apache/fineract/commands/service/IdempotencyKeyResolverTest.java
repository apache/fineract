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

import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class IdempotencyKeyResolverTest {

    @Mock
    private IdempotencyKeyGenerator idempotencyKeyGenerator;

    @Mock
    private FineractProperties fineractProperties;

    @InjectMocks
    private IdempotencyKeyResolver underTest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testIPKResolveFromRequest() {
        String idkh = "foo";
        String idk = "bar";
        Mockito.when(fineractProperties.getIdempotencyKeyHeaderName()).thenReturn(idkh);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(idkh, idk);
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        CommandWrapper wrapper = CommandWrapper.wrap("act", "ent", 1L, 1L);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }

    @Test
    public void testIPKResolveFromGenerate() {
        String idk = "idk";
        Mockito.when(idempotencyKeyGenerator.create()).thenReturn(idk);
        RequestContextHolder.setRequestAttributes(null);
        CommandWrapper wrapper = CommandWrapper.wrap("act", "ent", 1L, 1L);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }

    @Test
    public void testIPKResolveFromWrapper() {
        RequestContextHolder.setRequestAttributes(null);
        String idk = "idk";
        CommandWrapper wrapper = new CommandWrapper(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, idk);
        String resolvedIdk = underTest.resolve(wrapper);
        Assertions.assertEquals(idk, resolvedIdk);
    }
}
