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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.command.PermissionsCommand;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleWritePlatformServiceJpaRepositoryImplAuthorizationTest {

    private static final Long ROLE_ID = 1L;
    private static final String JSON = "{}";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RoleDataValidator roleDataValidator;
    @Mock
    private PermissionsCommandFromApiJsonDeserializer permissionsDeserializer;
    @Mock
    private JsonCommand command;
    @Mock
    private AppUser caller;

    private final Permission allFunctions = new Permission("special", "FUNCTIONS", "ALL");
    private final Permission readLoan = new Permission("loan", "LOAN", "READ");
    private final Permission createLoan = new Permission("loan", "LOAN", "CREATE");

    private Role role;
    private RoleWritePlatformServiceJpaRepositoryImpl subject;

    @BeforeEach
    void setUp() {
        role = new Role("test-role", "test role");
        subject = new RoleWritePlatformServiceJpaRepositoryImpl(context, roleRepository, permissionRepository, roleDataValidator,
                permissionsDeserializer);
        when(context.authenticatedUser()).thenReturn(caller);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
    }

    private void roleIsSuperUser() {
        role.updatePermission(allFunctions, true);
    }

    @Test
    void updateOfSuperUserRoleWithoutAllFunctionsIsRejected() {
        roleIsSuperUser();

        assertThrows(NoAuthorizationException.class, () -> subject.updateRole(ROLE_ID, command));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    @Test
    void permissionUpdateOfSuperUserRoleWithoutAllFunctionsIsRejected() {
        roleIsSuperUser();

        assertThrows(NoAuthorizationException.class, () -> subject.updateRolePermissions(ROLE_ID, command));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    @Test
    void disableOfSuperUserRoleWithoutAllFunctionsIsRejected() {
        roleIsSuperUser();

        assertThrows(NoAuthorizationException.class, () -> subject.disableRole(ROLE_ID));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    @Test
    void enableOfSuperUserRoleWithoutAllFunctionsIsRejected() {
        roleIsSuperUser();

        assertThrows(NoAuthorizationException.class, () -> subject.enableRole(ROLE_ID));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    @Test
    void deleteOfSuperUserRoleWithoutAllFunctionsIsRejected() {
        roleIsSuperUser();

        assertThrows(NoAuthorizationException.class, () -> subject.deleteRole(ROLE_ID));

        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void disableOfSuperUserRoleWithAllFunctionsIsAllowed() {
        roleIsSuperUser();
        when(caller.hasAnyPermission("ALL_FUNCTIONS")).thenReturn(true);
        when(roleRepository.getCountOfRolesAssociatedWithUsers(ROLE_ID)).thenReturn(0);

        subject.disableRole(ROLE_ID);

        verify(roleRepository).saveAndFlush(role);
    }

    @Test
    void grantingAPermissionTheCallerLacksIsRejected() {
        when(command.json()).thenReturn(JSON);
        when(permissionRepository.findAll()).thenReturn(List.of(createLoan));
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_LOAN", true)));
        when(caller.hasAnyPermission("CREATE_LOAN")).thenReturn(false);

        assertThrows(NoAuthorizationException.class, () -> subject.updateRolePermissions(ROLE_ID, command));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    @Test
    void grantingAPermissionTheCallerHoldsIsAllowed() {
        when(command.json()).thenReturn(JSON);
        when(permissionRepository.findAll()).thenReturn(List.of(createLoan));
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_LOAN", true)));
        when(caller.hasAnyPermission("CREATE_LOAN")).thenReturn(true);

        subject.updateRolePermissions(ROLE_ID, command);

        assertTrue(role.getPermissions().contains(createLoan));
    }

    @Test
    void readPermissionCanBeGrantedByHolderOfAllFunctionsRead() {
        when(command.json()).thenReturn(JSON);
        when(permissionRepository.findAll()).thenReturn(List.of(readLoan));
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("READ_LOAN", true)));
        when(caller.hasAnyPermission("READ_LOAN", "ALL_FUNCTIONS_READ")).thenReturn(true);

        subject.updateRolePermissions(ROLE_ID, command);

        assertTrue(role.getPermissions().contains(readLoan));
    }

    @Test
    void grantingAllFunctionsWithoutHoldingItIsRejected() {
        when(command.json()).thenReturn(JSON);
        when(permissionRepository.findAll()).thenReturn(List.of(allFunctions));
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("ALL_FUNCTIONS", true)));

        assertThrows(NoAuthorizationException.class, () -> subject.updateRolePermissions(ROLE_ID, command));
    }
}
