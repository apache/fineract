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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppUserUpdateOfficeLinkedStaffTest {

    private static final Long USER_ID = 1L;
    private static final Long NEW_OFFICE_ID = 5L;
    private static final Long STAFF_ID = 3L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private PlatformPasswordEncoder platformPasswordEncoder;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private UserDataValidator fromApiJsonDeserializer;
    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private AppUserWritePlatformServiceJpaRepositoryImpl underTest;

    private JsonCommand command;
    private AppUser user;

    @BeforeEach
    void setUp() {
        command = mock(JsonCommand.class);
        user = mock(AppUser.class);
        final Office newOffice = mock(Office.class);
        when(newOffice.getId()).thenReturn(NEW_OFFICE_ID);

        when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(mock(AppUser.class));
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(user.update(command, platformPasswordEncoder)).thenReturn(Map.of("officeId", NEW_OFFICE_ID));
        when(officeRepositoryWrapper.findOneWithNotFoundDetection(NEW_OFFICE_ID)).thenReturn(newOffice);
        when(user.getOffice()).thenReturn(newOffice);
        when(user.getStaffId()).thenReturn(STAFF_ID);
    }

    @Test
    void updateUserRejectsOfficeChangeWhenLinkedStaffIsInAnotherOffice() {
        when(staffRepository.findByOffice(STAFF_ID, NEW_OFFICE_ID)).thenReturn(Optional.empty());

        assertThrows(StaffNotFoundException.class, () -> underTest.updateUser(USER_ID, command));

        verify(appUserRepository, never()).saveAndFlush(user);
    }

    @Test
    void updateUserAllowsOfficeChangeWhenLinkedStaffIsInNewOffice() {
        when(staffRepository.findByOffice(STAFF_ID, NEW_OFFICE_ID)).thenReturn(Optional.of(mock(Staff.class)));

        final CommandProcessingResult result = underTest.updateUser(USER_ID, command);

        assertEquals(USER_ID, result.getResourceId());
        verify(appUserRepository).saveAndFlush(user);
    }
}
