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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.DefaultPlatformPasswordEncoder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPassword;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AppUserUpdatePasswordTest {

    private static final Long USER_ID = 1L;
    private static final Long ADMIN_ID = 2L;
    private static final String RAW_PASSWORD = "QwE!5rTy#9uP0";
    private static final String OTHER_RAW_PASSWORD = "pX268-4Pfv|kF6";
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Mock
    private PlatformSecurityContext context;
    @Spy
    private PlatformPasswordEncoder platformPasswordEncoder = new DefaultPlatformPasswordEncoder(PASSWORD_ENCODER);
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private UserDataValidator fromApiJsonDeserializer;
    @Mock
    private AppUserPreviousPasswordRepository appUserPreviewPasswordRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;

    @InjectMocks
    private AppUserWritePlatformServiceJpaRepositoryImpl underTest;

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final AppUser admin = mock(AppUser.class);
    private String currentPassword;
    private AppUser user;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        currentPassword = PASSWORD_ENCODER.encode(RAW_PASSWORD);
        user = new AppUser(mock(Office.class), new User("user", currentPassword, List.of()), new HashSet<>(), "user@example.com", "First",
                "Last", null, false, false);

        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(admin);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void updateUserIgnoresUnchangedPassword() {
        lenient().when(admin.getId()).thenReturn(ADMIN_ID);
        lenient().when(configurationDomainService.isForcePasswordResetOnFirstLoginEnabled()).thenReturn(true);

        CommandProcessingResult result = underTest.updateUser(USER_ID, passwordCommand(RAW_PASSWORD));

        assertFalse(result.hasChanges());
        assertEquals(currentPassword, user.getPassword());
        assertFalse(user.isPasswordResetRequired());
        verify(appUserRepository, never()).saveAndFlush(user);
        verify(appUserPreviewPasswordRepository, never()).save(any(AppUserPreviousPassword.class));
    }

    @Test
    void updateUserSetsPasswordResetRequiredWhenAdminChangesPassword() {
        when(admin.getId()).thenReturn(ADMIN_ID);
        when(configurationDomainService.isForcePasswordResetOnFirstLoginEnabled()).thenReturn(true);

        CommandProcessingResult result = underTest.updateUser(USER_ID, passwordCommand(OTHER_RAW_PASSWORD));

        assertTrue(result.getChanges().containsKey("password"));
        assertTrue(user.isPasswordResetRequired());
        assertTrue(platformPasswordEncoder.matches(OTHER_RAW_PASSWORD, user.getPassword()));
        verify(appUserRepository).saveAndFlush(user);
    }

    private JsonCommand passwordCommand(final String password) {
        return new JsonCommand(USER_ID,
                fromJsonHelper.parse("{\"password\": \"" + password + "\", \"repeatPassword\": \"" + password + "\"}"), fromJsonHelper);
    }
}
