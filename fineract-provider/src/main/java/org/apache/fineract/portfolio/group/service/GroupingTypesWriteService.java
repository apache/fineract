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

import org.apache.fineract.portfolio.group.data.CenterActivateRequest;
import org.apache.fineract.portfolio.group.data.CenterAssociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterCloseRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandResponse;
import org.apache.fineract.portfolio.group.data.CenterCreateRequest;
import org.apache.fineract.portfolio.group.data.CenterCreateResponse;
import org.apache.fineract.portfolio.group.data.CenterDeleteRequest;
import org.apache.fineract.portfolio.group.data.CenterDeleteResponse;
import org.apache.fineract.portfolio.group.data.CenterDisassociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateResponse;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupAssignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupAssociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.data.GroupDeleteRequest;
import org.apache.fineract.portfolio.group.data.GroupDeleteResponse;
import org.apache.fineract.portfolio.group.data.GroupDisassociateClientsRequest;
import org.apache.fineract.portfolio.group.data.GroupUnassignStaffRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateResponse;

public interface GroupingTypesWriteService {

    GroupCreateResponse createGroup(GroupCreateRequest request);

    GroupUpdateResponse updateGroup(GroupUpdateRequest request);

    GroupDeleteResponse deleteGroup(GroupDeleteRequest request);

    GroupCommandResponse activateGroup(GroupActivateRequest request);

    GroupCommandResponse closeGroup(GroupCloseRequest request);

    GroupCommandResponse associateClients(GroupAssociateClientsRequest request);

    GroupCommandResponse disassociateClients(GroupDisassociateClientsRequest request);

    GroupCommandResponse assignStaff(GroupAssignStaffRequest request);

    GroupCommandResponse unassignStaff(GroupUnassignStaffRequest request);

    CenterCreateResponse createCenter(CenterCreateRequest request);

    CenterUpdateResponse updateCenter(CenterUpdateRequest request);

    CenterDeleteResponse deleteCenter(CenterDeleteRequest request);

    CenterCommandResponse activateCenter(CenterActivateRequest request);

    CenterCommandResponse closeCenter(CenterCloseRequest request);

    CenterCommandResponse associateGroups(CenterAssociateGroupsRequest request);

    CenterCommandResponse disassociateGroups(CenterDisassociateGroupsRequest request);
}
