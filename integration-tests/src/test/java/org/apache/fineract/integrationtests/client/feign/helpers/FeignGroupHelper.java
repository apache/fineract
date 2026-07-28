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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteGroupsGroupIdResponse;
import org.apache.fineract.client.models.GetGroupsGroupIdResponse;
import org.apache.fineract.client.models.PostGroupsRequest;
import org.apache.fineract.client.models.PostGroupsResponse;
import org.apache.fineract.client.models.PutGroupsGroupIdRequest;
import org.apache.fineract.client.models.PutGroupsGroupIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

/**
 * Typed Feign helper for group operations. Standalone by design: nothing here is added to {@code FeignLoanTestBase}
 * (see FEIGN_BASE_MODULARIZATION.md). Replaced the raw-HTTP {@code FeignGroupCenterHelper} stopgap for groups
 * (removed).
 * <p>
 * Create / retrieve / update / delete are fully typed against the generated {@code GroupsApi}. The command endpoints
 * ({@code ?command=activate|associateClients|disassociateClients|assignStaff}) still go through
 * {@link FeignRawHttpHelper}: the shared {@code PostGroupsGroupIdRequest} model exposes only
 * {@code destinationGroupId}, so the command-specific fields ({@code clientMembers}, {@code staffId},
 * {@code activationDate}, {@code inheritStaffForClientAccounts}) cannot be set via the typed model. This is the
 * sanctioned raw-HTTP fallback for a request model shared across many commands (see pr_review_lessons_learned #8 /
 * #11), not REST-assured.
 */
public class FeignGroupHelper {

    public static final Long DEFAULT_OFFICE_ID = 1L;
    private static final String DEFAULT_ACTIVATION_DATE = "04 March 2011";
    private static final String DEFAULT_SUBMITTED_DATE = "04 March 2011";

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String ASSOCIATE_CLIENTS_COMMAND = "associateClients";
    private static final String DISASSOCIATE_CLIENTS_COMMAND = "disassociateClients";
    private static final String ASSIGN_STAFF_COMMAND = "assignStaff";

    private static final Gson GSON = new Gson();

    private final FineractFeignClient fineractClient;

    public FeignGroupHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    // ------------------------------------------------------------------ typed create / retrieve / update / delete

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

    // ------------------------------------------------------------------ command endpoints (raw HTTP, see class
    // javadoc)

    public void activateGroup(Long groupId) {
        activateGroup(groupId, DEFAULT_ACTIVATION_DATE);
    }

    public void activateGroup(Long groupId, String activationDate) {
        Map<String, Object> body = new HashMap<>();
        body.put("activationDate", activationDate);
        body.put("dateFormat", LoanTestData.DATETIME_PATTERN);
        body.put("locale", LoanTestData.LOCALE);
        FeignRawHttpHelper.post("/groups/" + groupId + "?command=" + ACTIVATE_COMMAND, GSON.toJson(body));
    }

    public void associateClient(Long groupId, Long clientId) {
        Map<String, Object> body = Map.of("clientMembers", List.of(clientId));
        FeignRawHttpHelper.post("/groups/" + groupId + "?command=" + ASSOCIATE_CLIENTS_COMMAND, GSON.toJson(body));
    }

    public void disAssociateClient(Long groupId, Long clientId) {
        Map<String, Object> body = Map.of("clientMembers", List.of(clientId));
        FeignRawHttpHelper.post("/groups/" + groupId + "?command=" + DISASSOCIATE_CLIENTS_COMMAND, GSON.toJson(body));
    }

    /** Assigns a staff member to the group; returns the {@code changes} object (contains {@code staffId}). */
    public JsonObject assignStaff(Long groupId, Long staffId) {
        Map<String, Object> body = Map.of("staffId", staffId);
        return postGroupCommand(groupId, ASSIGN_STAFF_COMMAND, body);
    }

    /**
     * Assigns staff to the group and cascades it to member client accounts. Returns the {@code changes} object.
     */
    public JsonObject assignStaffInheritStaffForClientAccounts(Long groupId, Long staffId) {
        Map<String, Object> body = new HashMap<>();
        body.put("staffId", staffId);
        body.put("inheritStaffForClientAccounts", true);
        return postGroupCommand(groupId, ASSIGN_STAFF_COMMAND, body);
    }

    private JsonObject postGroupCommand(Long groupId, String command, Map<String, Object> body) {
        String response = FeignRawHttpHelper.post("/groups/" + groupId + "?command=" + command, GSON.toJson(body));
        return JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("changes");
    }

    /**
     * Raw JSON-array text of the office's orphan groups (groups with no center parent). Raw because the endpoint
     * returns a bare JSON array that the typed {@code GetGroupsResponse} does not model. Expected {@code "[]"} when
     * none.
     */
    public String retrieveOrphanGroups(Long officeId) {
        return FeignRawHttpHelper.get("/groups?officeId=" + officeId + "&orphansOnly=true").trim();
    }

    // ------------------------------------------------------------------ raw reads (retrieve model gaps, see #8/#20)

    /**
     * Whether the group is active. Raw HTTP because {@code GetGroupsGroupIdResponse} does not model the top-level
     * {@code active} flag — the typed retrieve model cannot express this assertion (#8 / #20), so the read is preserved
     * via raw JSON rather than dropped.
     */
    public boolean isGroupActive(Long groupId) {
        String response = FeignRawHttpHelper.get("/groups/" + groupId);
        return JsonParser.parseString(response).getAsJsonObject().get("active").getAsBoolean();
    }

    /**
     * The client-member ids currently associated with the group (empty list if none). Raw HTTP because
     * {@code GetGroupsGroupIdResponse} does not model {@code clientMembers} (#8 / #20).
     */
    public List<Long> retrieveGroupMemberIds(Long groupId) {
        String response = FeignRawHttpHelper.get("/groups/" + groupId + "?associations=clientMembers");
        JsonElement members = JsonParser.parseString(response).getAsJsonObject().get("clientMembers");
        List<Long> ids = new ArrayList<>();
        if (members != null && members.isJsonArray()) {
            for (JsonElement member : members.getAsJsonArray()) {
                ids.add(member.getAsJsonObject().get("id").getAsLong());
            }
        }
        return ids;
    }
}
