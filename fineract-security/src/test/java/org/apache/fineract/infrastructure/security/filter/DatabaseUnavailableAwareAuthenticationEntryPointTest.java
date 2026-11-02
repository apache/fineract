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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Uses Spring Security's own Basic filter and provider (what TenantAwareBasicAuthenticationFilter extends) to show what
 * the client is told when the user lookup fails.
 */
class DatabaseUnavailableAwareAuthenticationEntryPointTest {

    @Test
    void databaseDownDuringUserLookupIsServiceUnavailable() throws Exception {
        final MockFilterChain chain = new MockFilterChain();

        final MockHttpServletResponse response = run(username -> {
            throw new CannotGetJdbcConnectionException("no connection");
        }, chain);

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getHeader("Retry-After")).isEqualTo(DatabaseUnavailableSupport.RETRY_AFTER_SECONDS);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains(DatabaseUnavailableSupport.MESSAGE_CODE);
        assertThat(chain.getRequest()).as("the chain must not be called").isNull();
    }

    @Test
    void unknownUserStaysUnauthorized() throws Exception {
        final MockHttpServletResponse response = run(username -> {
            throw new UsernameNotFoundException("no such user");
        }, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader("Retry-After")).isNull();
    }

    private MockHttpServletResponse run(final UserDetailsService userDetailsService, final MockFilterChain chain) throws Exception {
        final BasicAuthenticationEntryPoint basic = new BasicAuthenticationEntryPoint();
        basic.setRealmName("Fineract Platform API");
        final BasicAuthenticationFilter filter = new BasicAuthenticationFilter(
                new ProviderManager(new DaoAuthenticationProvider(userDetailsService)),
                new DatabaseUnavailableAwareAuthenticationEntryPoint(basic));
        final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/fineract-provider/api/v1/clients");
        request.addHeader("Authorization",
                "Basic " + Base64.getEncoder().encodeToString("mifos:password".getBytes(StandardCharsets.UTF_8)));
        final MockHttpServletResponse response = new MockHttpServletResponse();
        try {
            filter.doFilter(request, response, chain);
        } finally {
            SecurityContextHolder.clearContext();
        }
        return response;
    }
}
