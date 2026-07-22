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
import org.apache.fineract.portfolio.group.data.AssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.AssociateClientsResponse;
import org.apache.fineract.portfolio.group.data.DisassociateClientsRequest;
import org.apache.fineract.portfolio.group.data.DisassociateClientsResponse;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupActivateResponse;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffResponse;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffResponse;

public interface GroupingTypesWritePlatformService {

    GroupActivateResponse activateGroup(GroupActivateRequest request);

    GroupAssignStaffResponse assignGroupStaff(GroupAssignStaffRequest request);

    GroupUnassignStaffResponse unassignGroupStaff(GroupUnassignStaffRequest request);

    AssociateClientsResponse associateClientsToGroup(AssociateClientsRequest request);

    DisassociateClientsResponse disassociateClientsFromGroup(DisassociateClientsRequest request);

    CommandProcessingResult createCenter(JsonCommand command);

    CommandProcessingResult updateCenter(Long entityId, JsonCommand command);

    CommandProcessingResult createGroup(Long centerId, JsonCommand command);

    CommandProcessingResult updateGroup(Long groupId, JsonCommand command);

    CommandProcessingResult deleteGroup(Long groupId);

    CommandProcessingResult closeGroup(Long groupId, JsonCommand command);

    CommandProcessingResult closeCenter(Long centerId, JsonCommand command);

    CommandProcessingResult associateGroupsToCenter(Long centerId, JsonCommand command);

    CommandProcessingResult disassociateGroupsToCenter(Long centerId, JsonCommand command);
}
