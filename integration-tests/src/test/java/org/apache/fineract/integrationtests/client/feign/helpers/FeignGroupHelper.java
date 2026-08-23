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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.AssociateClientsRequest;
import org.apache.fineract.client.models.DeleteGroupsGroupIdResponse;
import org.apache.fineract.client.models.DisassociateClientsRequest;
import org.apache.fineract.client.models.GetGroupsGroupIdClientMembers;
import org.apache.fineract.client.models.GetGroupsGroupIdResponse;
import org.apache.fineract.client.models.GetGroupsPageItems;
import org.apache.fineract.client.models.GroupActivateRequest;
import org.apache.fineract.client.models.GroupAssignStaffRequest;
import org.apache.fineract.client.models.GroupAssignStaffResponse;
import org.apache.fineract.client.models.PostGroupsRequest;
import org.apache.fineract.client.models.PostGroupsResponse;
import org.apache.fineract.client.models.PutGroupsGroupIdRequest;
import org.apache.fineract.client.models.PutGroupsGroupIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

/** Typed Feign helper for group operations. */
public class FeignGroupHelper {

    public static final Long DEFAULT_OFFICE_ID = 1L;
    private static final String DEFAULT_ACTIVATION_DATE = "04 March 2011";
    private static final String DEFAULT_SUBMITTED_DATE = "04 March 2011";

    private static final String CLIENT_MEMBERS_ASSOCIATION = "clientMembers";

    private final FineractFeignClient fineractClient;
    private final NonPagedListingApi nonPagedListingApi;

    public FeignGroupHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.nonPagedListingApi = fineractClient.create(NonPagedListingApi.class);
    }

    /** Creates a group in {@code pending} status (active=false) in the default office. */
    public PostGroupsResponse createGroup() {
        return createGroup(DEFAULT_OFFICE_ID);
    }

    /** Creates a group in {@code pending} status (active=false). */
    public PostGroupsResponse createGroup(Long officeId) {
        PostGroupsRequest request = new PostGroupsRequest()//
                .officeId(officeId)//
                .name(Utils.uniqueRandomStringGenerator("Group_Name_", 5))//
                .externalId(UUID.randomUUID().toString())//
                .active(false)//
                .submittedOnDate(DEFAULT_SUBMITTED_DATE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        return createGroup(request);
    }

    /** Creates an {@code active} group in the default office. */
    public PostGroupsResponse createActiveGroup() {
        return createActiveGroup(DEFAULT_OFFICE_ID, DEFAULT_ACTIVATION_DATE);
    }

    public PostGroupsResponse createActiveGroup(Long officeId, String activationDate) {
        PostGroupsRequest request = new PostGroupsRequest()//
                .officeId(officeId)//
                .name(Utils.uniqueRandomStringGenerator("Group_Name_", 5))//
                .externalId(UUID.randomUUID().toString())//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        return createGroup(request);
    }

    public PostGroupsResponse createGroup(PostGroupsRequest request) {
        return ok(() -> fineractClient.groups().createGroup(request));
    }

    public GetGroupsGroupIdResponse retrieveGroup(Long groupId) {
        Map<String, Object> params = Map.of();
        return ok(() -> fineractClient.groups().retrieveOneGroup(groupId, params));
    }

    /** Retrieves a group with the given {@code associations} (e.g. {@code clientMembers}, {@code all}). */
    public GetGroupsGroupIdResponse retrieveGroupWithAssociations(Long groupId, String associations) {
        Map<String, Object> params = Map.of("associations", associations);
        return ok(() -> fineractClient.groups().retrieveOneGroup(groupId, params));
    }

    public PutGroupsGroupIdResponse updateGroup(Long groupId, String name) {
        PutGroupsGroupIdRequest request = new PutGroupsGroupIdRequest().name(name);
        return ok(() -> fineractClient.groups().updateGroup(groupId, request));
    }

    public DeleteGroupsGroupIdResponse deleteGroup(Long groupId) {
        return ok(() -> fineractClient.groups().deleteGroup(groupId));
    }

    public void activateGroup(Long groupId) {
        activateGroup(groupId, DEFAULT_ACTIVATION_DATE);
    }

    public void activateGroup(Long groupId, String activationDate) {
        GroupActivateRequest request = new GroupActivateRequest().activationDate(activationDate).dateFormat(LoanTestData.DATETIME_PATTERN)
                .locale(LoanTestData.LOCALE);
        ok(() -> fineractClient.groups().activateGroup(groupId, request));
    }

    public void associateClient(Long groupId, Long clientId) {
        AssociateClientsRequest request = new AssociateClientsRequest().clientMembers(Set.of(clientId));
        ok(() -> fineractClient.groups().associateClients(groupId, request));
    }

    public void disAssociateClient(Long groupId, Long clientId) {
        DisassociateClientsRequest request = new DisassociateClientsRequest().clientMembers(Set.of(clientId));
        ok(() -> fineractClient.groups().disassociateClients(groupId, request));
    }

    /** Assigns a staff member to the group; returns the staffId from changes. */
    public Long assignStaff(Long groupId, Long staffId) {
        GroupAssignStaffRequest request = new GroupAssignStaffRequest().staffId(staffId);
        GroupAssignStaffResponse response = ok(() -> fineractClient.groups().assignStaff(groupId, request));
        return extractStaffId(response.getChanges());
    }

    /** Assigns staff to the group and cascades to member client accounts; returns the staffId. */
    public Long assignStaffInheritStaffForClientAccounts(Long groupId, Long staffId) {
        GroupAssignStaffRequest request = new GroupAssignStaffRequest().staffId(staffId).inheritStaffForClientAccounts(true);
        GroupAssignStaffResponse response = ok(() -> fineractClient.groups().assignStaff(groupId, request));
        return extractStaffId(response.getChanges());
    }

    private static Long extractStaffId(Map<String, Object> changes) {
        if (changes == null) {
            return null;
        }
        Object v = changes.get("staffId");
        return v == null ? null : Long.valueOf(v.toString());
    }

    /**
     * The office's groups that have no center as parent. Must stay on the non-paged listing, see
     * {@link NonPagedListingApi}.
     */
    public List<GetGroupsPageItems> retrieveOrphanGroups(Long officeId) {
        return ok(() -> nonPagedListingApi.listOrphanGroups(officeId));
    }

    /** Whether the group is active. */
    public boolean isGroupActive(Long groupId) {
        return Boolean.TRUE.equals(retrieveGroup(groupId).getActive());
    }

    /** The client-member ids associated with the group (empty if none). */
    public List<Long> retrieveGroupMemberIds(Long groupId) {
        Set<GetGroupsGroupIdClientMembers> members = retrieveGroupWithAssociations(groupId, CLIENT_MEMBERS_ASSOCIATION).getClientMembers();
        return members == null ? List.of() : members.stream().map(GetGroupsGroupIdClientMembers::getId).toList();
    }
}
