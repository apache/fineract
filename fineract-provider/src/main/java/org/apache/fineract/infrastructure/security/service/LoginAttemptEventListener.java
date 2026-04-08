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

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class LoginAttemptEventListener {

    private static final String USERS_CACHE = "users";
    private static final String USERS_BY_USERNAME_CACHE = "usersByUsername";

    private final ConfigurationDomainService configurationDomainService;
    private final AppUserRepository appUserRepository;
    private final CacheManager cacheManager;

    @Transactional
    @EventListener
    public void onAuthenticationFailure(final AbstractAuthenticationFailureEvent event) {
        if (!configurationDomainService.isMaxLoginRetriesEnabled()) {
            return;
        }
        final Integer maxRetries = configurationDomainService.retrieveMaxLoginRetries();
        if (maxRetries == null || maxRetries <= 0) {
            return;
        }

        Authentication authentication = event.getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return;
        }

        AuthenticationException exception = event.getException();
        if (exception instanceof LockedException) {
            return;
        }

        AppUser user = appUserRepository.findAppUserByName(authentication.getName());
        if (user == null || !user.isAccountNonLocked() || !user.isLoginRetryLimitEnabled()) {
            return;
        }

        user.registerFailedLoginAttempt(maxRetries);
        appUserRepository.saveAndFlush(user);
        evictUserCaches();
    }

    @Transactional
    @EventListener
    public void onAuthenticationSuccess(final AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUser user)) {
            return;
        }

        if (user.getFailedLoginAttempts() <= 0) {
            return;
        }

        user.resetFailedLoginAttempts();
        appUserRepository.saveAndFlush(user);
        evictUserCaches();
    }

    private void evictUserCaches() {
        Cache usersCache = cacheManager.getCache(USERS_CACHE);
        if (usersCache != null) {
            usersCache.clear();
        }
        Cache usersByUsernameCache = cacheManager.getCache(USERS_BY_USERNAME_CACHE);
        if (usersByUsernameCache != null) {
            usersByUsernameCache.clear();
        }
    }
}
