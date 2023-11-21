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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPassword;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AppUserWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private PlatformPasswordEncoder platformPasswordEncoder;
    @Mock
    private ConfigurationReadPlatformService configurationReadPlatformService;
    @Mock
    private AppUserPreviousPasswordRepository appUserPreviewPasswordRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserWritePlatformServiceJpaRepositoryImpl appUserService;

    private AppUser user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        user = mock(AppUser.class);
    }

    @AfterEach
    public void tearDown() {
        user = null;
    }

    @Test
    public void testThatResetPasswordRequiresGlobalConfigurationEnabledToWork() {
        try {

            user = mock(AppUser.class);
            JsonCommand command = mock(JsonCommand.class);
            String encodedPassword = "{SHA-256}{1}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a"; // decoded-password:

            when(user.getId()).thenReturn(1L);
            when(user.getPassword()).thenReturn(encodedPassword);
            Mockito.mockStatic(DateUtils.class);
            when(DateUtils.getLocalDateOfTenant()).thenReturn(LocalDate.of(2023, 9, 10));
            when(user.getEncodedPassword(command, platformPasswordEncoder)).thenReturn(encodedPassword);

            GlobalConfigurationPropertyData config = new GlobalConfigurationPropertyData();
            config.setEnabled(false);
            config.setValue(1L); // Number of previous passwords
            when(configurationReadPlatformService.retrieveGlobalConfiguration("Restrict-re-use-of-password")).thenReturn(config);
            appUserService.getCurrentPasswordToSaveAsPreview(user, command);

            Mockito.verify(appUserPreviewPasswordRepository, Mockito.times(0)).findByUserId(eq(1L), any());
            Mockito.verify(configurationReadPlatformService, Mockito.times(1)).retrieveGlobalConfiguration(Mockito.anyString());

        } catch (Exception e) {
            fail("testThatResetPasswordRequiresGlobalConfigurationEnabledToWork has failed");
            assertThat(e.getMessage(), CoreMatchers.containsString(
                    "Reset Password is terminated. Please reach-out to your admin to enable [Restrict-re-use-of-password] in global configuration"));

        }

    }

    @Test
    public void testThatICanNotResetPasswordToTheAlreadyUsedPassword() {
        try {
            user = mock(AppUser.class);
            JsonCommand command = mock(JsonCommand.class);
            String encodedPassword = "{SHA-256}{1}5787039480429368bf94732aacc771cd0a3ea02bcf504ffe1185ab94213bc63a"; // decoded-password:

            String originalPassword = "password";

            when(user.getId()).thenReturn(1L);
            when(user.getPassword()).thenReturn(encodedPassword);
            when(DateUtils.getLocalDateOfTenant()).thenReturn(LocalDate.of(2023, 9, 10));
            when(user.getEncodedPassword(command, platformPasswordEncoder)).thenReturn(encodedPassword);
            when(command.stringValueOfParameterNamed("password")).thenReturn(originalPassword);

            GlobalConfigurationPropertyData config = new GlobalConfigurationPropertyData();
            config.setEnabled(true);
            config.setValue(1L); // Number of previous passwords
            when(configurationReadPlatformService.retrieveGlobalConfiguration("Restrict-re-use-of-password")).thenReturn(config);

            List<AppUserPreviousPassword> passwordHistory = new ArrayList<>();
            AppUserPreviousPassword previousPassword = new AppUserPreviousPassword(user);
            passwordHistory.add(previousPassword);
            when(appUserPreviewPasswordRepository.findByUserId(eq(1L), any())).thenReturn(passwordHistory);
            when(passwordEncoder.matches(originalPassword, encodedPassword)).thenReturn(Boolean.TRUE);

            appUserService.getCurrentPasswordToSaveAsPreview(user, command);

            fail("testThatICanNotResetPasswordToTheAlreadyUsedPassword has failed");

        } catch (Exception e) {
            Mockito.verify(passwordEncoder, Mockito.times(1)).matches(Mockito.anyString(), Mockito.anyString());
            Mockito.verify(configurationReadPlatformService, Mockito.times(1)).retrieveGlobalConfiguration(Mockito.anyString());
            Mockito.verify(appUserPreviewPasswordRepository, Mockito.times(1)).findByUserId(eq(1L), any());
            assertThat(e.getMessage(), CoreMatchers.containsString("The submitted password has already been used in the past"));
        }
    }

}
