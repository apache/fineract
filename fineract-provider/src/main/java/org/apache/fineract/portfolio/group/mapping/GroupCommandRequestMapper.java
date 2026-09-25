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
package org.apache.fineract.portfolio.group.mapping;

import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignRoleRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupAssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandRequest;
import org.apache.fineract.portfolio.group.data.GroupDisassociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupSaveCollectionSheetRequest;
import org.apache.fineract.portfolio.group.data.GroupTransferClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignRoleRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateRoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface GroupCommandRequestMapper {

    GroupActivateRequest toActivate(GroupCommandRequest source, Long id);

    GroupCloseRequest toClose(GroupCommandRequest source, Long id);

    GroupAssociateClientsRequest toAssociateClients(GroupCommandRequest source, Long id);

    GroupDisassociateClientsRequest toDisassociateClients(GroupCommandRequest source, Long id);

    GroupAssignStaffRequest toAssignStaff(GroupCommandRequest source, Long id);

    GroupUnassignStaffRequest toUnassignStaff(GroupCommandRequest source, Long id);

    @Mapping(target = "groupId", source = "groupId")
    GroupAssignRoleRequest toAssignRole(GroupCommandRequest source, Long groupId);

    @Mapping(target = "groupId", source = "groupId")
    @Mapping(target = "roleId", source = "roleId")
    GroupUnassignRoleRequest toUnassignRole(GroupCommandRequest source, Long groupId, Long roleId);

    @Mapping(target = "groupId", source = "groupId")
    @Mapping(target = "roleId", source = "roleId")
    GroupUpdateRoleRequest toUpdateRole(GroupCommandRequest source, Long groupId, Long roleId);

    GroupTransferClientsRequest toTransferClients(GroupCommandRequest source, Long id);

    GroupSaveCollectionSheetRequest toSaveCollectionSheet(GroupCommandRequest source, Long id);
}
