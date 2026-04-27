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
package org.apache.fineract.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

class TemporaryPasswordAwareAuthenticationProviderTest {

    private TemporaryPasswordAwareAuthenticationProvider subject;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = mock(PasswordEncoder.class);
        subject = new TemporaryPasswordAwareAuthenticationProvider();
        subject.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void authenticateShouldSucceedWithPermanentPassword() {
        AppUser user = mockEnabledUser();
        when(user.getPassword()).thenReturn("{bcrypt}main");
        when(passwordEncoder.matches("secret", "{bcrypt}main")).thenReturn(true);
        subject.setUserDetailsService(username -> user);

        subject.authenticate(UsernamePasswordAuthenticationToken.unauthenticated("demo", "secret"));
    }

    @Test
    void authenticateShouldSucceedWithValidTemporaryPassword() {
        AppUser user = mockEnabledUser();
        when(user.getPassword()).thenReturn("{bcrypt}main");
        when(user.hasValidTemporaryPassword()).thenReturn(true);
        when(user.getTemporaryPassword()).thenReturn("{bcrypt}temp");
        when(passwordEncoder.matches("temporary-secret", "{bcrypt}main")).thenReturn(false);
        when(passwordEncoder.matches("temporary-secret", "{bcrypt}temp")).thenReturn(true);
        subject.setUserDetailsService(username -> user);

        subject.authenticate(UsernamePasswordAuthenticationToken.unauthenticated("demo", "temporary-secret"));
    }

    @Test
    void authenticateShouldFailWithExpiredTemporaryPassword() {
        AppUser user = mockEnabledUser();
        when(user.getPassword()).thenReturn("{bcrypt}main");
        when(user.hasValidTemporaryPassword()).thenReturn(false);
        when(passwordEncoder.matches("temporary-secret", "{bcrypt}main")).thenReturn(false);
        subject.setUserDetailsService(username -> user);

        assertThrows(BadCredentialsException.class,
                () -> subject.authenticate(UsernamePasswordAuthenticationToken.unauthenticated("demo", "temporary-secret")));
    }

    private AppUser mockEnabledUser() {
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("demo");
        when(user.getAuthorities()).thenReturn(Collections.emptyList());
        when(user.isAccountNonExpired()).thenReturn(true);
        when(user.isAccountNonLocked()).thenReturn(true);
        when(user.isCredentialsNonExpired()).thenReturn(true);
        when(user.isEnabled()).thenReturn(true);
        return user;
    }
}
