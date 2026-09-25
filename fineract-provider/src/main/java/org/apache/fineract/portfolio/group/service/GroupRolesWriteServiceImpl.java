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

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.data.GroupAssignRoleRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupUnassignRoleRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateRoleRequest;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRole;
import org.apache.fineract.portfolio.group.domain.GroupRoleRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class GroupRolesWriteServiceImpl implements GroupRolesWriteService {

    private final PlatformSecurityContext context;
    private final GroupRepositoryWrapper groupRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRoleRepositoryWrapper groupRoleRepository;

    @Transactional
    @Override
    public GroupCommandResponse assignRole(final GroupAssignRoleRequest request) {
        try {
            context.authenticatedUser();
            final CodeValue role = codeValueRepository.findOneWithNotFoundDetection(request.getRole());
            final Client client = clientRepository.findOneWithNotFoundDetection(request.getClientId());
            final Group group = groupRepository.findOneWithNotFoundDetection(request.getGroupId());
            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(request.getClientId(), request.getGroupId());
            }
            final GroupRole groupRole = GroupRole.createGroupRole(group, client, role);
            groupRoleRepository.saveAndFlush(groupRole);
            return GroupCommandResponse.builder().clientId(client.getId()).groupId(group.getId()).resourceId(groupRole.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleGroupDataIntegrityIssues(request.getGroupId(), request.getClientId(), request.getRole(), dve.getMostSpecificCause(),
                    dve);
        }
    }

    @Transactional
    @Override
    public GroupCommandResponse updateRole(final GroupUpdateRoleRequest request) {
        try {
            context.authenticatedUser();
            final Group group = groupRepository.findOneWithNotFoundDetection(request.getGroupId());
            final GroupRole groupRole = groupRoleRepository.findOneWithNotFoundDetection(request.getRoleId());
            final Map<String, Object> actualChanges = new LinkedHashMap<>(2);
            if (request.getClientId() != null && !request.getClientId().equals(groupRole.getClient().getId())) {
                actualChanges.put(GroupingTypesApiConstants.clientIdParamName, request.getClientId());
                final Client client = clientRepository.findOneWithNotFoundDetection(request.getClientId());
                if (!group.hasClientAsMember(client)) {
                    throw new ClientNotInGroupException(request.getClientId(), request.getGroupId());
                }
                groupRole.updateClient(client);
            }
            if (request.getRole() != null && !request.getRole().equals(groupRole.getRole().getId())) {
                actualChanges.put(GroupingTypesApiConstants.roleParamName, request.getRole());
                groupRole.updateRole(codeValueRepository.findOneWithNotFoundDetection(request.getRole()));
            }
            groupRoleRepository.saveAndFlush(groupRole);
            return GroupCommandResponse.builder().groupId(group.getId()).resourceId(groupRole.getId()).changes(actualChanges).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            throw handleGroupDataIntegrityIssues(request.getGroupId(), request.getClientId(), request.getRole(), dve.getMostSpecificCause(),
                    dve);
        }
    }

    @Transactional
    @Override
    public GroupCommandResponse unassignRole(final GroupUnassignRoleRequest request) {
        context.authenticatedUser();
        final GroupRole groupRole = groupRoleRepository.findOneWithNotFoundDetection(request.getRoleId());
        groupRoleRepository.delete(groupRole);
        return GroupCommandResponse.builder().groupId(request.getGroupId()).resourceId(groupRole.getId()).build();
    }

    private RuntimeException handleGroupDataIntegrityIssues(final Long groupId, final Long clientId, final Long roleId,
            final Throwable realCause, final NonTransientDataAccessException dve) {
        if (realCause != null && realCause.getMessage() != null && realCause.getMessage().contains("UNIQUE_GROUP_ROLES")) {
            return new PlatformDataIntegrityException("error.msg.group.role.already.exists",
                    "Group Role with roleId `" + roleId + "`, clientId `" + clientId + "`, groupId `" + groupId + "` already exists.",
                    GroupingTypesApiConstants.clientIdParamName, roleId, clientId, groupId);
        }
        log.error("Error occurred.", dve);
        return ErrorHandler.getMappable(dve, "error.msg.group.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}
