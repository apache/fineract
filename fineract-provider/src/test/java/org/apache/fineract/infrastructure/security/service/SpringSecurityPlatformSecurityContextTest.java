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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.security.exception.ResetPasswordException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.exception.UnAuthenticatedUserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SpringSecurityPlatformSecurityContextTest {

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private AppUser appUser;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @InjectMocks
    private SpringSecurityPlatformSecurityContext securityContextProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAppUserWhenPrincipalIsAppUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(appUser);
        when(configurationDomainService.isPasswordForcedResetEnable()).thenReturn(false);

        AppUser result = securityContextProvider.authenticatedUser();

        assertEquals(appUser, result, "authenticatedUser() should return AppUser");
    }

    @Test
    void shouldThrowUnAuthenticatedUserExceptionWhenPrincipalIsNotAppUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        assertThrows(UnAuthenticatedUserException.class,
                     () -> securityContextProvider.authenticatedUser(),
                     "authenticatedUser() should throw UnAuthenticatedUserException when "
                             + "Principal is not AppUser");
    }

    @Test
    void shouldThrowUnAuthenticatedUserExceptionWhenAuthenticationIsNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(UnAuthenticatedUserException.class,
                     () -> securityContextProvider.authenticatedUser(),
                     "authenticatedUser() should throw UnAuthenticatedUserException when "
                             + "Authentication is null");
    }

    @Test
    void shouldThrowResetPasswordExceptionWhenPasswordMustBeReset() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(appUser);
        SpringSecurityPlatformSecurityContext spyContextProvider = spy(securityContextProvider);
        doReturn(true).when(spyContextProvider).doesPasswordHasToBeRenewed(appUser);

        assertThrows(ResetPasswordException.class,
                     spyContextProvider::authenticatedUser,
                     "authenticatedUser() should throw ResetPasswordException when password needs"
                             + " to be reset");
    }
}
