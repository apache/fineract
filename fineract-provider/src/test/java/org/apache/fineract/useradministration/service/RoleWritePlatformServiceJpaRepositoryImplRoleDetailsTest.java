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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.PermissionRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.apache.fineract.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests the creation and update of a role's name and description in {@link RoleWritePlatformServiceJpaRepositoryImpl}.
 * The permission updates are tested in {@link RoleWritePlatformServiceJpaRepositoryImplTest}.
 */
@ExtendWith(MockitoExtension.class)
class RoleWritePlatformServiceJpaRepositoryImplRoleDetailsTest {

    private static final Long ROLE_ID = 1L;

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();

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

    private RoleWritePlatformServiceJpaRepositoryImpl subject;

    @BeforeEach
    void setUp() {
        subject = new RoleWritePlatformServiceJpaRepositoryImpl(context, roleRepository, permissionRepository, roleDataValidator,
                permissionsDeserializer);
    }

    @Test
    void createStoresTheTrimmedNameAndDescription() {
        subject.createRole(command("{\"name\": \"  Teller  \", \"description\": \" Front desk \"}"));

        final ArgumentCaptor<Role> saved = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).saveAndFlush(saved.capture());
        assertEquals("Teller", saved.getValue().getName());
        assertEquals("Front desk", description(saved.getValue()));
        assertFalse(saved.getValue().isDisabled());
    }

    @Test
    void createWithADuplicateNameReportsTheNameAsSent() {
        when(roleRepository.saveAndFlush(any(Role.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate", new SQLException("Duplicate entry for key 'unq_name'")));

        final PlatformDataIntegrityException exception = assertThrows(PlatformDataIntegrityException.class,
                () -> subject.createRole(command("{\"name\": \"Teller \", \"description\": \"Front desk\"}")));

        assertEquals("error.msg.role.duplicate.name", exception.getGlobalisationMessageCode());
        assertEquals("Role with name `Teller ` already exists", exception.getDefaultUserMessage());
        assertEquals("name", exception.getParameterName());
    }

    @Test
    void updateReportsAndStoresTheChangedValuesInOrder() {
        final Role role = existingRole();

        final CommandProcessingResult result = subject.updateRole(ROLE_ID,
                command("{\"description\": \"New description\", \"name\": \"New name\"}"));

        assertEquals(List.of("name", "description"), List.copyOf(result.getChanges().keySet()));
        assertEquals(Map.of("name", "New name", "description", "New description"), result.getChanges());
        assertEquals(ROLE_ID, result.getResourceId());
        assertEquals("New name", role.getName());
        assertEquals("New description", description(role));
        verify(roleRepository).saveAndFlush(role);
    }

    @Test
    void updateOfOnlyTheDescriptionKeepsTheName() {
        final Role role = existingRole();

        final CommandProcessingResult result = subject.updateRole(ROLE_ID, command("{\"description\": \"New description\"}"));

        assertEquals(Map.of("description", "New description"), result.getChanges());
        assertEquals("Teller", role.getName());
        assertEquals("New description", description(role));
        verify(roleRepository).saveAndFlush(role);
    }

    @Test
    void updateWithTheSameValuesReportsNoChangeAndSavesNothing() {
        final Role role = existingRole();

        final CommandProcessingResult result = subject.updateRole(ROLE_ID,
                command("{\"name\": \"Teller\", \"description\": \"Front desk\"}"));

        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        assertEquals("Teller", role.getName());
        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    /**
     * Documents a known inconsistency, kept on purpose: creating a role trims the name and description, but updating it
     * compares, reports and stores the value as sent, with its surrounding spaces.
     */
    @Test
    void updateKeepsSurroundingSpacesUnlikeCreate() {
        final Role role = existingRole();

        final CommandProcessingResult result = subject.updateRole(ROLE_ID,
                command("{\"name\": \" Teller \", \"description\": \"Front desk \"}"));

        assertEquals(Map.of("name", " Teller ", "description", "Front desk "), result.getChanges());
        assertEquals(" Teller ", role.getName());
        assertEquals("Front desk ", description(role));
        verify(roleRepository).saveAndFlush(role);
    }

    @Test
    void updateOfAnUnknownRoleFailsAndSavesNothing() {
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> subject.updateRole(ROLE_ID, command("{\"name\": \"New name\"}")));

        verify(roleRepository, never()).saveAndFlush(any(Role.class));
    }

    private Role existingRole() {
        final Role role = new Role("Teller", "Front desk");
        ReflectionTestUtils.setField(role, "id", ROLE_ID);
        when(roleRepository.findById(ROLE_ID)).thenReturn(Optional.of(role));
        return role;
    }

    private static String description(final Role role) {
        return (String) ReflectionTestUtils.getField(role, "description");
    }

    private JsonCommand command(final String json) {
        return JsonCommand.from(json, fromJsonHelper.parse(json), fromJsonHelper, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
    }
}
