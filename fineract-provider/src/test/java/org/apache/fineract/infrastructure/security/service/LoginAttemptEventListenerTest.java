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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class LoginAttemptEventListenerTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache usersCache;
    @Mock
    private Cache usersByUsernameCache;

    private LoginAttemptEventListener listener;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil
                .setTenant(FineractPlatformTenant.builder().id(1L).tenantIdentifier("default").name("default").timezoneId("UTC").build());
        listener = new LoginAttemptEventListener(configurationDomainService, appUserRepository, cacheManager);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldIncrementFailedAttemptsAndLockAccountWhenMaxReached() {
        when(configurationDomainService.isMaxLoginRetriesEnabled()).thenReturn(true);
        when(configurationDomainService.retrieveMaxLoginRetries()).thenReturn(3);
        when(cacheManager.getCache("users")).thenReturn(usersCache);
        when(cacheManager.getCache("usersByUsername")).thenReturn(usersByUsernameCache);

        AppUser user = buildUser("user");
        user.updateLoginRetryLimitEnabled(true);
        when(appUserRepository.findAppUserByName("user")).thenReturn(user);

        AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("user", "bad"), new BadCredentialsException("bad"));

        listener.onAuthenticationFailure(failureEvent);
        listener.onAuthenticationFailure(failureEvent);
        listener.onAuthenticationFailure(failureEvent);

        assertEquals(3, user.getFailedLoginAttempts());
        assertFalse(user.isAccountNonLocked());
        verify(appUserRepository, times(3)).saveAndFlush(user);
    }

    @Test
    void shouldResetFailedAttemptsOnSuccessfulLogin() {
        AppUser user = buildUser("user");
        user.updateLoginRetryLimitEnabled(true);
        user.registerFailedLoginAttempt(10);
        user.registerFailedLoginAttempt(10);
        when(cacheManager.getCache("users")).thenReturn(usersCache);
        when(cacheManager.getCache("usersByUsername")).thenReturn(usersByUsernameCache);

        AuthenticationSuccessEvent successEvent = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken(user, "pass", user.getAuthorities()));

        listener.onAuthenticationSuccess(successEvent);

        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        verify(appUserRepository).saveAndFlush(user);
    }

    @Test
    void shouldNotUpdateWhenLimitDisabled() {
        when(configurationDomainService.isMaxLoginRetriesEnabled()).thenReturn(false);

        AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("user", "bad"), new BadCredentialsException("bad"));

        listener.onAuthenticationFailure(failureEvent);

        verifyNoInteractions(appUserRepository);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void shouldNotUpdateWhenLoginRetriesDisabledForUser() {
        when(configurationDomainService.isMaxLoginRetriesEnabled()).thenReturn(true);
        when(configurationDomainService.retrieveMaxLoginRetries()).thenReturn(3);

        AppUser user = buildUser("user");
        user.updateLoginRetryLimitEnabled(false);
        when(appUserRepository.findAppUserByName("user")).thenReturn(user);

        AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("user", "bad"), new BadCredentialsException("bad"));

        listener.onAuthenticationFailure(failureEvent);

        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        verify(appUserRepository, times(0)).saveAndFlush(user);
    }

    @Test
    void shouldNotLockSystemUser() {
        when(configurationDomainService.isMaxLoginRetriesEnabled()).thenReturn(true);
        when(configurationDomainService.retrieveMaxLoginRetries()).thenReturn(3);

        AppUser user = buildUser("system");
        user.updateLoginRetryLimitEnabled(true);
        when(appUserRepository.findAppUserByName("system")).thenReturn(user);

        AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("system", "bad"), new BadCredentialsException("bad"));

        listener.onAuthenticationFailure(failureEvent);

        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isAccountNonLocked());
        verify(appUserRepository, times(0)).saveAndFlush(user);
    }

    @Test
    void shouldLockUserWithCannotChangePasswordWhenEnabled() {
        when(configurationDomainService.isMaxLoginRetriesEnabled()).thenReturn(true);
        when(configurationDomainService.retrieveMaxLoginRetries()).thenReturn(1);
        when(cacheManager.getCache("users")).thenReturn(usersCache);
        when(cacheManager.getCache("usersByUsername")).thenReturn(usersByUsernameCache);

        AppUser user = buildUser("api-user", true);
        user.updateLoginRetryLimitEnabled(true);
        when(appUserRepository.findAppUserByName("api-user")).thenReturn(user);

        AuthenticationFailureBadCredentialsEvent failureEvent = new AuthenticationFailureBadCredentialsEvent(
                new UsernamePasswordAuthenticationToken("api-user", "bad"), new BadCredentialsException("bad"));

        listener.onAuthenticationFailure(failureEvent);

        assertEquals(1, user.getFailedLoginAttempts());
        assertFalse(user.isAccountNonLocked());
        verify(appUserRepository, times(1)).saveAndFlush(user);
    }

    private AppUser buildUser(String username) {
        return buildUser(username, false);
    }

    private AppUser buildUser(String username, boolean cannotChangePassword) {
        Office office = mock(Office.class);
        User springUser = new User(username, "pass", true, true, true, true, List.of(new SimpleGrantedAuthority("ALL_FUNCTIONS")));
        return new AppUser(office, springUser, new HashSet<>(), "user@example.com", "First", "Last", null, false, cannotChangePassword);
    }
}
