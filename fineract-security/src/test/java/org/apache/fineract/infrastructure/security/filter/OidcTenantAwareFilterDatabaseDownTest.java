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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.apache.fineract.infrastructure.core.filters.DatabaseUnavailableFilter;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * The filter must not swallow a database outage (it would continue without a tenant and fail somewhere else, or run the
 * rest of the chain a second time), so that the {@link DatabaseUnavailableFilter} can answer 503.
 */
class OidcTenantAwareFilterDatabaseDownTest {

    private final AuthTenantDetailsService tenantService = mock(AuthTenantDetailsService.class);
    // the properties and the OIDC config service are only used when the request carries a bearer token
    private final OidcTenantAwareFilter underTest = new OidcTenantAwareFilter(new DefaultBearerTokenResolver(), tenantService, null, null);
    private final DatabaseUnavailableFilter frontFilter = new DatabaseUnavailableFilter();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fineract-provider/api/v1/clients");
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final AtomicInteger chainCalls = new AtomicInteger();

    OidcTenantAwareFilterDatabaseDownTest() {
        request.addHeader("Fineract-Platform-TenantId", "default");
    }

    static Stream<RuntimeException> databaseDownExceptions() {
        return Stream.of(new CannotGetJdbcConnectionException("no connection"), new CannotCreateTransactionException("no connection"),
                new TransientDataAccessResourceException("connection is not available"));
    }

    @ParameterizedTest
    @MethodSource("databaseDownExceptions")
    void databaseDownWhileLoadingTheTenantIsServiceUnavailable(final RuntimeException databaseDown) throws Exception {
        when(tenantService.loadTenantById(anyString(), anyBoolean())).thenThrow(databaseDown);

        frontFilter.doFilter(request, response, (req, res) -> underTest.doFilter(req, res, (r, s) -> chainCalls.incrementAndGet()));

        assertServiceUnavailable();
        assertThat(chainCalls).hasValue(0);
    }

    @Test
    void databaseDownLaterInTheChainIsServiceUnavailableAndTheChainRunsOnce() throws Exception {
        when(tenantService.loadTenantById(anyString(), anyBoolean())).thenReturn(mock(FineractPlatformTenant.class));
        final FilterChain failing = (req, res) -> {
            chainCalls.incrementAndGet();
            throw new CannotCreateTransactionException("no connection");
        };

        frontFilter.doFilter(request, response, (req, res) -> underTest.doFilter(req, res, failing));

        assertServiceUnavailable();
        assertThat(chainCalls).hasValue(1);
    }

    @Test
    void otherTenantFailuresStillContinueTheChain() throws Exception {
        when(tenantService.loadTenantById(anyString(), anyBoolean())).thenThrow(new IllegalStateException("not a database problem"));

        frontFilter.doFilter(request, response, (req, res) -> underTest.doFilter(req, res, (r, s) -> chainCalls.incrementAndGet()));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chainCalls).hasValue(1);
    }

    private void assertServiceUnavailable() throws Exception {
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeader("Retry-After")).isEqualTo(DatabaseUnavailableSupport.RETRY_AFTER_SECONDS);
        assertThat(response.getContentAsString()).contains(DatabaseUnavailableSupport.MESSAGE_CODE);
    }
}
