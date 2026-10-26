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
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.command.PermissionsCommand;
import org.apache.fineract.useradministration.domain.Permission;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionWritePlatformServiceJpaRepositoryImplTest {

    private static final String JSON = "{}";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private PermissionsCommandFromApiJsonDeserializer permissionsDeserializer;
    @Mock
    private JsonCommand command;

    private final Permission datatableCreateGroup = new Permission("datatable", "group", "CREATE");
    private final Permission coreCreateGroup = new Permission("portfolio_group", "GROUP", "CREATE");

    private PermissionWritePlatformServiceJpaRepositoryImpl subject;

    @BeforeEach
    void setUp() {
        subject = new PermissionWritePlatformServiceJpaRepositoryImpl(context, permissionRepository, permissionsDeserializer);
        when(command.json()).thenReturn(JSON);
        when(permissionRepository.findAll()).thenReturn(List.of(datatableCreateGroup, coreCreateGroup));
    }

    @Test
    void enablingMakerCheckerOnCoreGroupPermissionDoesNotTouchDatatablePermissionThatDiffersOnlyByCase() {
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_GROUP", true)));

        subject.updateMakerCheckerPermissions(command);

        assertTrue(coreCreateGroup.hasMakerCheckerEnabled());
        assertFalse(datatableCreateGroup.hasMakerCheckerEnabled());
    }

    @Test
    void enablingMakerCheckerOnDatatablePermissionDoesNotTouchCoreGroupPermission() {
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("CREATE_group", true)));

        subject.updateMakerCheckerPermissions(command);

        assertFalse(coreCreateGroup.hasMakerCheckerEnabled());
        assertTrue(datatableCreateGroup.hasMakerCheckerEnabled());
    }

    @Test
    void caseInsensitiveMatchIsStillUsedWhenNoExactCodeExists() {
        when(permissionsDeserializer.commandFromApiJson(JSON)).thenReturn(new PermissionsCommand(Map.of("create_GROUP", true)));

        subject.updateMakerCheckerPermissions(command);

        assertTrue(datatableCreateGroup.hasMakerCheckerEnabled());
        assertFalse(coreCreateGroup.hasMakerCheckerEnabled());
    }
}
