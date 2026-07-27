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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteCentersCenterIdResponse;
import org.apache.fineract.client.models.PostCentersRequest;
import org.apache.fineract.client.models.PostCentersResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.CenterDomain;
import org.apache.fineract.integrationtests.common.Utils;

/**
 * Typed Feign helper for center operations. Standalone by design: nothing here is added to {@code FeignLoanTestBase}
 * (see FEIGN_BASE_MODULARIZATION.md). Replaces the raw-HTTP {@code FeignGroupCenterHelper} stopgap for centers.
 * <p>
 * <b>create</b> / <b>delete</b> are fully typed against the generated {@code CentersApi} (using the
 * {@code PostCentersRequest} fields added at source in this PR). The <b>retrieve</b> / <b>list</b> / <b>update</b> /
 * <b>associateGroups</b> and staff <b>command</b> endpoints go through {@link FeignRawHttpHelper}, because the
 * generated models can't express what the tests assert: {@code GetCentersCenterIdResponse} has no {@code externalId}/
 * {@code staffId}/{@code groupMembers}; {@code GetCentersResponse.pageItems} is an unordered {@code Set};
 * {@code PutCentersCenterIdRequest} exposes only {@code name}; and the shared command models lack the command-specific
 * fields. This is the sanctioned raw-HTTP fallback for broad response/command model gaps (pr_review_lessons_learned #8
 * / #11 / #20 — never drop an assertion because the model lacks the field), not REST-assured. Retrieval reuses the
 * pure-Gson {@link CenterDomain} POJO so every legacy assertion is preserved verbatim.
 */
public class FeignCenterHelper {

    private static final String CENTERS = "/centers/";
    private static final String GROUPS = "/groups/";
    /** Matches the legacy {@code CenterHelper.CREATED_DATE} used by the active-center convenience creators. */
    private static final String CREATED_DATE = "29 December 2014";

    private static final Gson GSON = new Gson();

    private final FineractFeignClient fineractClient;

    public FeignCenterHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    // ------------------------------------------------------------------ typed create / delete

    /** Creates a center in {@code pending} status (active=false). */
    public PostCentersResponse createCenter(String name, Long officeId) {
        return createCenter(centerRequest(name, officeId, null, null, null, null));
    }

    /** Creates an {@code active} center with the given activation date. */
    public PostCentersResponse createActiveCenter(String name, Long officeId, String activationDate) {
        return createCenter(centerRequest(name, officeId, null, null, null, activationDate));
    }

    /** Convenience: an {@code active} center in the default office (1) with a generated name. */
    public PostCentersResponse createActiveCenter() {
        return createActiveCenter(Utils.uniqueRandomStringGenerator("Center_Name_", 5), 1L, CREATED_DATE);
    }

    /** Convenience: an {@code active} center in the default office (1) with a generated name and the given staff. */
    public PostCentersResponse createActiveCenterWithStaff(Long staffId) {
        return createCenter(centerRequest(Utils.uniqueRandomStringGenerator("Center_Name_", 5), 1L, null, staffId, null, CREATED_DATE));
    }

    /**
     * Creates a center with external id, staff and group members. Mirrors the legacy semantics: the center is
     * {@code active} iff an {@code activationDate} is supplied, otherwise {@code pending}.
     */
    public PostCentersResponse createCenter(String name, Long officeId, String externalId, Long staffId, List<Long> groupMembers,
            String activationDate) {
        return createCenter(centerRequest(name, officeId, externalId, staffId, groupMembers, activationDate));
    }

    public PostCentersResponse createCenter(PostCentersRequest request) {
        return ok(() -> fineractClient.centers().createCenter(request));
    }

    public DeleteCentersCenterIdResponse deleteCenter(Long centerId) {
        return ok(() -> fineractClient.centers().deleteCenter(centerId));
    }

    private PostCentersRequest centerRequest(String name, Long officeId, String externalId, Long staffId, List<Long> groupMembers,
            String activationDate) {
        boolean active = activationDate != null;
        PostCentersRequest request = new PostCentersRequest()//
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

    // ------------------------------------------------------------------ raw retrieve / list (see class javadoc,
    // #8/#20)

    public CenterDomain retrieveCenter(Long centerId) {
        return CenterDomain.fromJSON(FeignRawHttpHelper.get(CENTERS + centerId + "?associations=groupMembers"));
    }

    /** Retrieves a center expecting failure (e.g. after deletion, or a non-existent id); returns the thrown error. */
    public RuntimeException retrieveCenterExpectingError(Long centerId) {
        try {
            retrieveCenter(centerId);
            return null;
        } catch (RuntimeException e) {
            return e;
        }
    }

    public List<CenterDomain> listCenters() {
        return parseCenterList(FeignRawHttpHelper.get("/centers?limit=-1"));
    }

    public List<CenterDomain> listCentersOrdered() {
        return parseCenterList(FeignRawHttpHelper.get("/centers?limit=-1&orderBy=id&sortOrder=asc"));
    }

    public List<CenterDomain> paginatedListCenters() {
        String pageItems = JsonParser.parseString(FeignRawHttpHelper.get("/centers?paged=true&limit=-1")).getAsJsonObject().get("pageItems")
                .toString();
        return parseCenterList(pageItems);
    }

    private static List<CenterDomain> parseCenterList(String json) {
        List<CenterDomain> centers = GSON.fromJson(json, new TypeToken<ArrayList<CenterDomain>>() {}.getType());
        return centers == null ? new ArrayList<>() : centers;
    }

    // ------------------------------------------------------------------ raw update / command endpoints (see class
    // javadoc)

    /**
     * Updates a center with the given field map (e.g. {@code name}, {@code externalId}, {@code staffId}). Raw because
     * {@code PutCentersCenterIdRequest} models only {@code name}. Returns the {@code changes} object.
     */
    public JsonObject updateCenter(Long centerId, Map<String, Object> request) {
        String response = FeignRawHttpHelper.put(CENTERS + centerId, GSON.toJson(request));
        return JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("changes");
    }

    /** Associates groups with the center; returns the associated group ids from the {@code changes} response. */
    public int[] associateGroups(Long centerId, int[] groupMembers) {
        Map<String, Object> body = Map.of("groupMembers", groupMembers);
        String response = FeignRawHttpHelper.post(CENTERS + centerId + "?command=associateGroups", GSON.toJson(body));
        JsonObject changes = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("changes");
        List<String> ids = GSON.fromJson(changes.get("groupMembers"), new TypeToken<ArrayList<String>>() {}.getType());
        int[] result = new int[ids.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(ids.get(i));
        }
        return result;
    }

    /**
     * Assigns a staff member to the center. A center is a group-type entity, so this targets the {@code /groups}
     * command endpoint (mirroring the legacy {@code CenterHelper.assignStaff}). Returns the {@code changes} object.
     */
    public JsonObject assignStaff(Long centerId, Long staffId) {
        return postGroupCommand(centerId, "assignStaff", Map.of("staffId", staffId));
    }

    /** Unassigns the staff member from the center; returns the {@code changes} object (staffId becomes null). */
    public JsonObject unassignStaff(Long centerId, Long staffId) {
        return postGroupCommand(centerId, "unassignStaff", Map.of("staffId", staffId));
    }

    private JsonObject postGroupCommand(Long centerId, String command, Map<String, Object> body) {
        String response = FeignRawHttpHelper.post(GROUPS + centerId + "?command=" + command, GSON.toJson(body));
        return JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("changes");
    }
}
