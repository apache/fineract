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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class AppUserRetrieveUserHierarchyTest {

    private static final Long USER_ID = 321L;
    private static final Long STAFF_ID = 11L;

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private OfficeReadPlatformService officeReadPlatformService;
    @Mock
    private RoleReadPlatformService roleReadPlatformService;
    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserReadPlatformServiceImpl underTest;

    private AppUser target;

    @BeforeEach
    void setUp() {
        final AppUser caller = mock(AppUser.class);
        final Office callerOffice = mock(Office.class);
        when(callerOffice.getHierarchy()).thenReturn(".7.");
        when(caller.getOffice()).thenReturn(callerOffice);
        when(context.authenticatedUser()).thenReturn(caller);

        target = mock(AppUser.class);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(target));
    }

    @Test
    void retrieveUserOutsideCallerHierarchyIsNotFound() {
        givenTargetOfficeHierarchy(".2.");

        assertThrows(UserNotFoundException.class, () -> underTest.retrieveUser(USER_ID));

        verifyNoInteractions(roleReadPlatformService, jdbcTemplate);
    }

    @Test
    void retrieveUserInCallerHierarchyLoadsStaffFromAnyOffice() {
        final Office targetOffice = givenTargetOfficeHierarchy(".7.568.");
        when(targetOffice.getId()).thenReturn(568L);
        when(target.getId()).thenReturn(USER_ID);
        when(target.getRoles()).thenReturn(Set.of());
        when(target.getStaff()).thenReturn(mock(Staff.class));
        when(target.getStaffId()).thenReturn(STAFF_ID);
        when(roleReadPlatformService.retrieveAll()).thenReturn(new ArrayList<>());
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(STAFF_ID)))
                .thenReturn(List.of(StaffData.builder().id(STAFF_ID).officeId(2L).build()));

        final AppUserData result = underTest.retrieveUser(USER_ID);

        assertEquals(USER_ID, result.getId());
        final ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(RowMapper.class), eq(STAFF_ID));
        assertFalse(sql.getValue().contains("hierarchy"));
    }

    private Office givenTargetOfficeHierarchy(final String hierarchy) {
        final Office office = mock(Office.class);
        when(office.getHierarchy()).thenReturn(hierarchy);
        when(target.getOffice()).thenReturn(office);
        return office;
    }
}
