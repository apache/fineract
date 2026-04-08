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
package org.apache.fineract.infrastructure.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class IpAddressUtilsTest {

    @AfterEach
    void clearContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    // Proper isolation helper
    private void withRequest(HttpServletRequest request, Runnable testLogic) {
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        try {
            RequestContextHolder.setRequestAttributes(attributes);
            testLogic.run();
        } finally {
            RequestContextHolder.resetRequestAttributes(); // critical cleanup
        }
    }

    @Test
    void getClientIpReturnsEmptyWhenNoRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        String result = IpAddressUtils.getClientIp();

        assertEquals("", result);
    }

    @Test
    void getClientIpReturnsEmptyWhenIpAttributeMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn(null);

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("", result);
        });
    }

    @Test
    void getClientIpReturnsIpWhenAttributePresent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn("192.168.1.1");

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("192.168.1.1", result);
        });
    }

    @Test
    void getClientIpConvertsNonStringAttributeUsingToString() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getAttribute("IP")).thenReturn(12345);

        withRequest(request, () -> {
            String result = IpAddressUtils.getClientIp();
            assertEquals("12345", result);
        });
    }
}
