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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPassword;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.exception.PasswordPreviouslyUsedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
public class AppUserWritePlatformServiceJpaRepositoryImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private PlatformPasswordEncoder platformPasswordEncoder;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserDataValidator fromApiJsonDeserializer;
    @Mock
    private AppUserPreviousPasswordRepository appUserPreviewPasswordRepository;
    @Mock
    private StaffRepositoryWrapper staffRepositoryWrapper;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private ConfigurationDomainService configurationDomainService;

    @InjectMocks
    private AppUserWritePlatformServiceJpaRepositoryImpl underTest;

    private JsonCommand command;
    private AppUser user;
    private AppUser authenticatedUser;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        command = mock(JsonCommand.class);
        user = mock(AppUser.class);
        authenticatedUser = mock(AppUser.class);

        when(command.json()).thenReturn("{}");
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(authenticatedUser);
        doNothing().when(fromApiJsonDeserializer).validateForChangePassword(anyString(), nullable(AppUser.class));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void changeUserPasswordThrowsWhenPasswordPreviouslyUsed() {
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEncodedPassword(command, platformPasswordEncoder)).thenReturn("encoded");
        when(configurationDomainService.getPasswordReuseRestrictionCount()).thenReturn(2);

        AppUserPreviousPassword previousPassword = mock(AppUserPreviousPassword.class);
        when(previousPassword.getPassword()).thenReturn("encoded");
        when(appUserPreviewPasswordRepository.findByUserId(eq(USER_ID), any(PageRequest.class))).thenReturn(List.of(previousPassword));

        assertThrows(PasswordPreviouslyUsedException.class, () -> underTest.changeUserPassword(USER_ID, command));

        verify(appUserRepository, never()).saveAndFlush(user);
        verify(appUserPreviewPasswordRepository, never()).save(any(AppUserPreviousPassword.class));
    }

    @Test
    void changeUserPasswordSavesPreviousPasswordWhenAllowed() {
        Office office = mock(Office.class);
        when(office.getId()).thenReturn(7L);
        when(user.getOffice()).thenReturn(office);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getPassword()).thenReturn("currentEncoded");
        when(user.getEncodedPassword(command, platformPasswordEncoder)).thenReturn("newEncoded");
        when(user.changePassword(command, platformPasswordEncoder)).thenReturn(Map.of("password", "new"));
        when(configurationDomainService.getPasswordReuseRestrictionCount()).thenReturn(2);
        when(appUserPreviewPasswordRepository.findByUserId(eq(USER_ID), any(PageRequest.class))).thenReturn(List.of());

        CommandProcessingResult result = underTest.changeUserPassword(USER_ID, command);

        assertEquals(USER_ID, result.getResourceId());
        verify(appUserRepository).saveAndFlush(user);
        verify(appUserPreviewPasswordRepository).save(any(AppUserPreviousPassword.class));
    }
}
