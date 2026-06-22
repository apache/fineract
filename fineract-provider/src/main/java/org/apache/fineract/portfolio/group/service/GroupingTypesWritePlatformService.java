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

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCloseResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.data.GroupDeleteRequest;
import org.apache.fineract.portfolio.group.data.GroupDeleteResponse;
import org.apache.fineract.portfolio.group.data.GroupUpdateRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateResponse;

public interface GroupingTypesWritePlatformService {

    CommandProcessingResult createCenter(JsonCommand command);

    CommandProcessingResult updateCenter(Long entityId, JsonCommand command);

    GroupCreateResponse createGroup(GroupCreateRequest request);

    CommandProcessingResult activateGroupOrCenter(Long entityId, JsonCommand command);

    CommandProcessingResult closeCenter(Long centerId, JsonCommand command);

    CommandProcessingResult unassignGroupOrCenterStaff(Long groupId, JsonCommand command);

    CommandProcessingResult assignGroupOrCenterStaff(Long groupId, JsonCommand command);

    CommandProcessingResult associateClientsToGroup(Long groupId, JsonCommand command);

    CommandProcessingResult disassociateClientsFromGroup(Long groupId, JsonCommand command);

    CommandProcessingResult associateGroupsToCenter(Long centerId, JsonCommand command);

    CommandProcessingResult disassociateGroupsToCenter(Long centerId, JsonCommand command);

    GroupUpdateResponse updateGroup(GroupUpdateRequest request);

    GroupDeleteResponse deleteGroup(GroupDeleteRequest request);

    GroupCloseResponse closeGroup(GroupCloseRequest request);
}
