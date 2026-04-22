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
package org.apache.fineract.useradministration.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.apache.fineract.infrastructure.core.service.PlatformEmailService;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceImplTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PlatformEmailService emailService;
    @Mock
    private AppUser user;
    @Mock
    private Office office;
    @Captor
    private ArgumentCaptor<OffsetDateTime> expiryCaptor;

    private ForgotPasswordServiceImpl subject;

    @BeforeEach
    void setUp() {
        subject = new ForgotPasswordServiceImpl(appUserRepository, passwordEncoder, emailService);
    }

    @Test
    void requestPasswordResetShouldBeNoopWhenUserNotFound() {
        when(appUserRepository.findActiveUserByEmail(EMAIL)).thenReturn(null);

        subject.requestPasswordReset(EMAIL);

        verify(passwordEncoder, never()).encode(anyString());
        verify(emailService, never()).sendForgotPasswordEmail(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void requestPasswordResetShouldBeNoopWhenResetIsDisabledForUser() {
        when(appUserRepository.findActiveUserByEmail(EMAIL)).thenReturn(user);
        when(user.isPasswordResetAllowed()).thenReturn(false);

        subject.requestPasswordReset(EMAIL);

        verify(passwordEncoder, never()).encode(anyString());
        verify(emailService, never()).sendForgotPasswordEmail(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void requestPasswordResetShouldGenerateTemporaryPasswordAndSendMail() {
        when(appUserRepository.findActiveUserByEmail(EMAIL)).thenReturn(user);
        when(user.isPasswordResetAllowed()).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("{bcrypt}encoded-temporary-password");
        when(user.getOffice()).thenReturn(office);
        when(office.getName()).thenReturn("Head Office");
        when(user.getFirstname()).thenReturn("Demo");
        when(user.getLastname()).thenReturn("User");
        when(user.getUsername()).thenReturn("demo");

        OffsetDateTime minExpected = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).minusSeconds(5);
        subject.requestPasswordReset(EMAIL);
        OffsetDateTime maxExpected = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).plusSeconds(5);

        verify(user).updateTemporaryPassword(eq("{bcrypt}encoded-temporary-password"), expiryCaptor.capture());
        verify(appUserRepository).saveAndFlush(user);
        verify(emailService).sendForgotPasswordEmail(eq("Head Office"), eq("Demo User"), eq(EMAIL), eq("demo"), anyString());

        OffsetDateTime actualExpiry = expiryCaptor.getValue();
        assertFalse(actualExpiry.isBefore(minExpected));
        assertTrue(actualExpiry.isBefore(maxExpected));
    }
}
