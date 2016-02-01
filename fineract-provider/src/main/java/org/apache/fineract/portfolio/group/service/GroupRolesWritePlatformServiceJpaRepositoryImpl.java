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

import java.util.Map;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.api.GroupingTypesApiConstants;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRole;
import org.apache.fineract.portfolio.group.domain.GroupRoleRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.serialization.GroupRolesDataValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class GroupRolesWritePlatformServiceJpaRepositoryImpl implements GroupRolesWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(GroupRolesWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final GroupRepositoryWrapper groupRepository;
    private final GroupRolesDataValidator fromApiJsonDeserializer;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRoleRepositoryWrapper groupRoleRepository;

    @Autowired
    public GroupRolesWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final GroupRepositoryWrapper groupRepository, final GroupRolesDataValidator fromApiJsonDeserializer,
            final CodeValueRepositoryWrapper codeValueRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRoleRepositoryWrapper groupRoleRepository) {
        this.context = context;
        this.groupRepository = groupRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.codeValueRepository = codeValueRepository;
        this.clientRepository = clientRepository;
        this.groupRoleRepository = groupRoleRepository;
    }

    @Override
    public CommandProcessingResult createRole(final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreateGroupRole(command);

            final Long roleId = command.longValueOfParameterNamed(GroupingTypesApiConstants.roleParamName);
            final CodeValue role = this.codeValueRepository.findOneWithNotFoundDetection(roleId);

            final Long clientId = command.longValueOfParameterNamed(GroupingTypesApiConstants.clientIdParamName);
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            final Group group = this.groupRepository.findOneWithNotFoundDetection(command.getGroupId());
            if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(clientId, command.getGroupId()); }
            final GroupRole groupRole = GroupRole.createGroupRole(group, client, role);
            this.groupRoleRepository.save(groupRole);
            return new CommandProcessingResultBuilder().withClientId(client.getId()).withGroupId(group.getId())
                    .withEntityId(groupRole.getId()).build();

        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

    }

    private void handleGroupDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();

        if (realCause.getMessage().contains("UNIQUE_GROUP_ROLES")) {
            final String clientId = command.stringValueOfParameterNamed(GroupingTypesApiConstants.clientIdParamName);
            final String roleId = command.stringValueOfParameterNamed(GroupingTypesApiConstants.roleParamName);
            final String errorMessageForUser = "Group Role with roleId `" + roleId + "`, clientId `" + clientId + "`, groupId `"
                    + command.getGroupId() + "` already exists.";
            final String errorMessageForMachine = "error.msg.group.role.already.exists";
            throw new PlatformDataIntegrityException(errorMessageForMachine, errorMessageForUser,
                    GroupingTypesApiConstants.clientIdParamName, roleId, clientId, command.getGroupId());
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.group.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Override
    public CommandProcessingResult updateRole(final JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdateRole(command);

            final Group group = this.groupRepository.findOneWithNotFoundDetection(command.getGroupId());

            final GroupRole groupRole = this.groupRoleRepository.findOneWithNotFoundDetection(command.entityId());
            final Map<String, Object> actualChanges = groupRole.update(command);

            if (actualChanges.containsKey(GroupingTypesApiConstants.roleParamName)) {
                final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.roleParamName);

                CodeValue role = null;
                if (newValue != null) {
                    role = this.codeValueRepository.findOneWithNotFoundDetection(newValue);
                }
                groupRole.updateRole(role);
            }

            if (actualChanges.containsKey(GroupingTypesApiConstants.clientIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(GroupingTypesApiConstants.clientIdParamName);

                Client client = null;
                if (newValue != null) {
                    client = this.clientRepository.findOneWithNotFoundDetection(newValue);
                    if (!group.hasClientAsMember(client)) { throw new ClientNotInGroupException(newValue, command.getGroupId()); }
                }
                groupRole.updateClient(client);
            }

            this.groupRoleRepository.saveAndFlush(groupRole);
            return new CommandProcessingResultBuilder().with(actualChanges).withGroupId(group.getId()).withEntityId(groupRole.getId())
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleGroupDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Override
    public CommandProcessingResult deleteRole(final Long ruleId) {
        this.context.authenticatedUser();
        final GroupRole groupRole = this.groupRoleRepository.findOneWithNotFoundDetection(ruleId);
        this.groupRoleRepository.delete(groupRole);
        return new CommandProcessingResultBuilder().withEntityId(groupRole.getId()).build();
    }

}
