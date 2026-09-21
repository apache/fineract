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
package org.apache.fineract.portfolio.group.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.data.GroupAssignRoleRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRole;
import org.apache.fineract.portfolio.group.domain.GroupRoleRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GroupRolesWriteServiceImplTest {

    private final GroupRepositoryWrapper groupRepository = mock(GroupRepositoryWrapper.class);
    private final CodeValueRepositoryWrapper codeValues = mock(CodeValueRepositoryWrapper.class);
    private final ClientRepositoryWrapper clients = mock(ClientRepositoryWrapper.class);
    private final GroupRoleRepositoryWrapper groupRoles = mock(GroupRoleRepositoryWrapper.class);
    private final GroupRolesWriteServiceImpl service = new GroupRolesWriteServiceImpl(mock(PlatformSecurityContext.class), groupRepository,
            codeValues, clients, groupRoles);

    @Test
    void assignRoleRejectsClientOutsideGroup() {
        Group group = mock(Group.class);
        when(group.hasClientAsMember(any())).thenReturn(false);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(group);
        when(clients.findOneWithNotFoundDetection(2L)).thenReturn(mock(Client.class));
        when(codeValues.findOneWithNotFoundDetection(3L)).thenReturn(mock(CodeValue.class));

        assertThrows(ClientNotInGroupException.class,
                () -> service.assignRole(GroupAssignRoleRequest.builder().groupId(1L).clientId(2L).role(3L).build()));
    }

    @Test
    void assignRoleReturnsIds() {
        Group group = mock(Group.class);
        Client client = mock(Client.class);
        when(client.getId()).thenReturn(2L);
        when(group.getId()).thenReturn(1L);
        when(group.hasClientAsMember(client)).thenReturn(true);
        when(groupRepository.findOneWithNotFoundDetection(1L)).thenReturn(group);
        when(clients.findOneWithNotFoundDetection(2L)).thenReturn(client);
        when(codeValues.findOneWithNotFoundDetection(3L)).thenReturn(mock(CodeValue.class));
        doAnswer(inv -> {
            GroupRole r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 99L);
            return null;
        }).when(groupRoles).saveAndFlush(any(GroupRole.class));

        GroupCommandResponse response = service.assignRole(GroupAssignRoleRequest.builder().groupId(1L).clientId(2L).role(3L).build());

        assertEquals(1L, response.getGroupId());
        assertEquals(2L, response.getClientId());
        assertEquals(99L, response.getResourceId());
    }
}
