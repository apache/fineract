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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.apache.fineract.infrastructure.security.data.FineractJwtAuthenticationToken;
import org.apache.fineract.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class FineractJwtAuthenticationTokenConverterTest {

    @Mock
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @InjectMocks
    private FineractJwtAuthenticationTokenConverter converter;

    private Jwt jwt;

    @BeforeEach
    void setUp() {
        jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject("alice").claim("scope", List.of("ALL_FUNCTIONS"))
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build();
    }

    @Test
    void tokenCarriesCurrentAuthoritiesOfUserInsteadOfScopes() {
        final AppUser appUser = mock(AppUser.class);
        final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("READ_LOAN"));
        when(appUser.getAuthorities()).thenReturn(authorities);
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(appUser);

        final FineractJwtAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getPrincipal()).isSameAs(appUser);
        assertThat(token.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("READ_LOAN");
    }

    @Test
    void wrapsUnknownUserAsOAuth2AuthenticationException() {
        when(userDetailsService.loadUserByUsername("alice")).thenThrow(new UsernameNotFoundException("alice"));

        assertThatThrownBy(() -> converter.convert(jwt)).isInstanceOf(OAuth2AuthenticationException.class)
                .hasCauseInstanceOf(UsernameNotFoundException.class);
    }
}
