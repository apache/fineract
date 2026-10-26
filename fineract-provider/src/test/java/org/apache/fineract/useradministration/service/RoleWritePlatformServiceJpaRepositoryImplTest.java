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
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.command.PermissionsCommand;
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
class RoleWritePlatformServiceJpaRepositoryImplTest {

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

    private final Permission datatableCreateGroup = new Permission("datatable", "group", "CREATE");
    private final Permission coreCreateGroup = new Permission("portfolio_group", "GROUP", "CREATE");

    private Role role;
    private RoleWritePlatformServiceJpaRepositoryImpl subject;

    @BeforeEach
    void setUp() {
        role = new Role("test-role", "test role");
        subject = new RoleWritePlatformServiceJpaRepositoryImpl(context, roleRepository, permissionRepository, roleDataValidator,
                permissionsDeserializer);
        when(command.json()).thenReturn(JSON);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
        when(permissionRepository.findAll()).thenReturn(List.of(datatableCreateGroup, coreCreateGroup));
    }

    @Test
    void uncheckingCoreGroupPermissionRemovesItEvenWhenDatatablePermissionDiffersOnlyByCase() {
        role.updatePermission(coreCreateGroup, true);
        role.updatePermission(datatableCreateGroup, true);
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_GROUP", false)));

        subject.updateRolePermissions(ROLE_ID, command);

        assertFalse(role.getPermissions().contains(coreCreateGroup));
        assertTrue(role.getPermissions().contains(datatableCreateGroup));
    }

    @Test
    void uncheckingDatatablePermissionLeavesCoreGroupPermissionGranted() {
        role.updatePermission(coreCreateGroup, true);
        role.updatePermission(datatableCreateGroup, true);
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_group", false)));

        subject.updateRolePermissions(ROLE_ID, command);

        assertTrue(role.getPermissions().contains(coreCreateGroup));
        assertFalse(role.getPermissions().contains(datatableCreateGroup));
    }

    @Test
    void caseInsensitiveMatchIsStillUsedWhenNoExactCodeExists() {
        role.updatePermission(datatableCreateGroup, true);
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("create_GROUP", false)));

        subject.updateRolePermissions(ROLE_ID, command);

        assertFalse(role.getPermissions().contains(datatableCreateGroup));
    }
}
