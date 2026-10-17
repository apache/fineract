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
package org.apache.fineract.infrastructure.security.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * With the database down, loading the tenant failed with an exception that nothing handled, and the client got an HTTP
 * 500 from the container. It is a temporary condition and is answered with 503 and Retry-After.
 */
class TenantAwareBasicAuthenticationFilterDatabaseDownTest {

    private final AuthTenantDetailsService tenantService = mock(AuthTenantDetailsService.class);
    private final TenantAwareBasicAuthenticationFilter underTest = new TenantAwareBasicAuthenticationFilter(
            mock(AuthenticationManager.class), mock(AuthenticationEntryPoint.class), mock(ToApiJsonSerializer.class), null, null, null,
            tenantService, null);

    private HttpServletRequest request() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Fineract-Platform-TenantId")).thenReturn("default");
        when(request.getRequestURI()).thenReturn("/fineract-provider/api/v1/clients");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8443/fineract-provider/api/v1/clients"));
        return request;
    }

    @Test
    void noJdbcConnectionWhileLoadingTheTenantIsServiceUnavailable() throws Exception {
        when(tenantService.loadTenantById(anyString(), anyBoolean())).thenThrow(new CannotGetJdbcConnectionException("no connection"));
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        underTest.doFilterInternal(request(), response, chain);

        verify(response).addHeader("Retry-After", "5");
        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The database is currently not available");
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void aTransactionThatCannotBeStartedWhileLoadingTheTenantIsServiceUnavailable() throws Exception {
        when(tenantService.loadTenantById(anyString(), anyBoolean())).thenThrow(new CannotCreateTransactionException("no connection"));
        HttpServletResponse response = mock(HttpServletResponse.class);

        underTest.doFilterInternal(request(), response, mock(FilterChain.class));

        verify(response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "The database is currently not available");
    }
}
