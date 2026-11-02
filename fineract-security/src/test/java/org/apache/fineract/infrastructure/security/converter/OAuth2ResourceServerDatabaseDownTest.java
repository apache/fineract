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
package org.apache.fineract.infrastructure.security.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import java.time.Instant;
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.apache.fineract.infrastructure.core.filters.DatabaseUnavailableFilter;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * The OAuth2 resource server chain loads the user for every token. A database outage there must reach the
 * {@link DatabaseUnavailableFilter} (Spring Security only handles AuthenticationExceptions) and be answered with 503,
 * not with a 401 that tells the client its token is bad.
 */
class OAuth2ResourceServerDatabaseDownTest {

    @Test
    void databaseDownDuringUserLookupIsServiceUnavailable() throws Exception {
        final TenantAwareJpaPlatformUserDetailsService userDetailsService = mock(TenantAwareJpaPlatformUserDetailsService.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new CannotCreateTransactionException("no connection"));
        final Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("mifos").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60)).build();
        final JwtAuthenticationProvider provider = new JwtAuthenticationProvider(token -> jwt);
        provider.setJwtAuthenticationConverter(new FineractJwtAuthenticationTokenConverter(userDetailsService));
        final BearerTokenAuthenticationFilter bearerFilter = new BearerTokenAuthenticationFilter(new ProviderManager(provider));

        final MockFilterChain terminal = new MockFilterChain();
        final FilterChain securityChain = (req, res) -> bearerFilter.doFilter(req, res, terminal);
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fineract-provider/api/v1/clients");
        request.addHeader("Authorization", "Bearer token");
        final MockHttpServletResponse response = new MockHttpServletResponse();

        try {
            new DatabaseUnavailableFilter().doFilter(request, response, securityChain);
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeader("Retry-After")).isEqualTo(DatabaseUnavailableSupport.RETRY_AFTER_SECONDS);
        assertThat(response.getContentAsString()).contains(DatabaseUnavailableSupport.MESSAGE_CODE);
        assertThat(terminal.getRequest()).as("the request must not reach the application").isNull();
    }
}
