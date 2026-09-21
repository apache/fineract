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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CenterCommandRequest;
import org.apache.fineract.client.models.CenterCreateRequest;
import org.apache.fineract.client.models.CenterCreateResponse;
import org.apache.fineract.client.models.CenterData;
import org.apache.fineract.client.models.CenterDeleteResponse;
import org.apache.fineract.client.models.CenterUpdateRequest;
import org.apache.fineract.client.models.CentersPageResponse;
import org.apache.fineract.client.models.GroupCommandRequest;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

/** Typed Feign helper for center operations. */
public class FeignCenterHelper {

    private static final String CREATED_DATE = "29 December 2014";
    private static final String GROUP_MEMBERS_ASSOCIATION = "groupMembers";
    private static final String ASSOCIATE_GROUPS_COMMAND = "associateGroups";
    private static final String ASSIGN_STAFF_COMMAND = "assignStaff";
    private static final String UNASSIGN_STAFF_COMMAND = "unassignStaff";

    private final FineractFeignClient fineractClient;
    private final NonPagedListingApi nonPagedListingApi;

    public FeignCenterHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.nonPagedListingApi = fineractClient.create(NonPagedListingApi.class);
    }

    /** Creates a center in {@code pending} status (active=false). */
    public CenterCreateResponse createCenter(String name, Long officeId) {
        return createCenter(centerRequest(name, officeId, null, null, null, null));
    }

    /** Creates an {@code active} center with the given activation date. */
    public CenterCreateResponse createActiveCenter(String name, Long officeId, String activationDate) {
        return createCenter(centerRequest(name, officeId, null, null, null, activationDate));
    }

    /** Convenience: an {@code active} center in the default office (1) with a generated name. */
    public CenterCreateResponse createActiveCenter() {
        return createActiveCenter(Utils.uniqueRandomStringGenerator("Center_Name_", 5), 1L, CREATED_DATE);
    }

    /** Convenience: an {@code active} center in the default office (1) with a generated name and the given staff. */
    public CenterCreateResponse createActiveCenterWithStaff(Long staffId) {
        return createCenter(centerRequest(Utils.uniqueRandomStringGenerator("Center_Name_", 5), 1L, null, staffId, null, CREATED_DATE));
    }

    /** Creates a center with external id, staff and group members; active iff an {@code activationDate} is supplied. */
    public CenterCreateResponse createCenter(String name, Long officeId, String externalId, Long staffId, List<Long> groupMembers,
            String activationDate) {
        return createCenter(centerRequest(name, officeId, externalId, staffId, groupMembers, activationDate));
    }

    public CenterCreateResponse createCenter(CenterCreateRequest request) {
        return ok(() -> fineractClient.centers().createCenter(request));
    }

    public CenterDeleteResponse deleteCenter(Long centerId) {
        return ok(() -> fineractClient.centers().deleteCenter(centerId));
    }

    private CenterCreateRequest centerRequest(String name, Long officeId, String externalId, Long staffId, List<Long> groupMembers,
            String activationDate) {
        boolean active = activationDate != null;
        CenterCreateRequest request = new CenterCreateRequest()//
                .name(name)//
                .officeId(officeId)//
                .active(active)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);
        if (externalId != null) {
            request.externalId(externalId);
        }
        if (staffId != null) {
            request.staffId(staffId);
        }
        if (groupMembers != null) {
            request.groupMembers(groupMembers);
        }
        if (active) {
            request.activationDate(activationDate);
        }
        return request;
    }

    public CenterData retrieveCenter(Long centerId) {
        return ok(() -> fineractClient.centers().retrieveOneCenter(centerId, Map.of("associations", GROUP_MEMBERS_ASSOCIATION)));
    }

    /** Retrieves a center expecting failure (e.g. after deletion, or a non-existent id); returns the thrown error. */
    public CallFailedRuntimeException retrieveCenterExpectingError(Long centerId) {
        return fail(() -> fineractClient.centers().retrieveOneCenter(centerId, Map.of("associations", GROUP_MEMBERS_ASSOCIATION)));
    }

    public List<CenterData> listCenters() {
        return ok(nonPagedListingApi::listCenters);
    }

    public List<CenterData> listCentersOrdered() {
        return ok(nonPagedListingApi::listCentersOrdered);
    }

    public List<CenterData> paginatedListCenters() {
        CentersPageResponse response = ok(() -> fineractClient.centers().retrieveAllCenters(Map.of("paged", true, "limit", -1)));
        return response.getPageItems() == null ? List.of() : new ArrayList<>(response.getPageItems());
    }

    public Map<String, Object> updateCenter(Long centerId, CenterUpdateRequest request) {
        return ok(() -> fineractClient.centers().updateCenter(centerId, request)).getChanges();
    }

    /** Associates groups with the center; returns the associated group ids from the {@code changes} response. */
    public List<Long> associateGroups(Long centerId, List<Long> groupMembers) {
        Map<String, Object> changes = ok(() -> fineractClient.centers().handleCommandsCenter(centerId,
                new CenterCommandRequest().groupMembers(groupMembers), ASSOCIATE_GROUPS_COMMAND)).getChanges();
        List<?> changedGroupMembers = (List<?>) changes.get("groupMembers");
        return changedGroupMembers.stream().map(item -> Long.valueOf((String) item)).toList();
    }

    public Map<String, Object> assignStaff(Long centerId, Long staffId) {
        return postGroupCommand(centerId, ASSIGN_STAFF_COMMAND, new GroupCommandRequest().staffId(staffId));
    }

    public Map<String, Object> unassignStaff(Long centerId, Long staffId) {
        return postGroupCommand(centerId, UNASSIGN_STAFF_COMMAND, new GroupCommandRequest().staffId(staffId));
    }

    // A center is a group server-side: the /centers command endpoint rejects assignStaff/unassignStaff.
    private Map<String, Object> postGroupCommand(Long centerId, String command, GroupCommandRequest request) {
        return ok(() -> fineractClient.groups().handleCommandsGroup(centerId, request, Map.of("command", command))).getChanges();
    }
}
