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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collections;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ClientTextSearch;
import org.apache.fineract.client.models.DeleteClientsClientIdResponse;
import org.apache.fineract.client.models.GetClientsClientIdAccountsResponse;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.PageClientSearchData;
import org.apache.fineract.client.models.PagedRequestClientTextSearch;
import org.apache.fineract.client.models.PostClientsClientIdRequest;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PutClientsClientIdRequest;
import org.apache.fineract.client.models.PutClientsClientIdResponse;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignClientHelper {

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String CLOSE_COMMAND = "close";
    private static final String REJECT_COMMAND = "reject";
    private static final String REACTIVATE_COMMAND = "reactivate";
    private static final String WITHDRAW_COMMAND = "withdraw";
    private static final String UNDO_REJECTION_COMMAND = "undoRejection";
    private static final String UNDO_WITHDRAWAL_COMMAND = "undoWithdrawal";
    private static final String ASSIGN_STAFF_COMMAND = "assignStaff";

    private static final Gson GSON = new Gson();

    private final FineractFeignClient fineractClient;

    public FeignClientHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public Long createClient() {
        return createClient(org.apache.fineract.integrationtests.common.ClientHelper.DEFAULT_DATE);
    }

    public Long createClient(String activationDate) {
        String externalId = Utils.randomStringGenerator("EXT_", 7);

        PostClientsRequest request = new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(externalId)//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);

        return createClient(request).getClientId();
    }

    public PostClientsResponse createClient(PostClientsRequest request) {
        return ok(() -> fineractClient.clients().createClient(request));
    }

    public PostClientsResponse createClientPending() {
        return createClientPending(Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
    }

    public PostClientsResponse createClientPending(String submittedOnDate) {
        return createClientPending(ClientRequestBuilders.createPendingClient(submittedOnDate));
    }

    public PostClientsResponse createClientPending(PostClientsRequest request) {
        return ok(() -> fineractClient.clients().createClient(request));
    }

    public GetClientsClientIdResponse getClient(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public CallFailedRuntimeException getClientExpectingError(Long clientId) {
        return fail(() -> fineractClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public GetClientsClientIdAccountsResponse getClientAccounts(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveAllClientAccounts(clientId));
    }

    public PageClientSearchData searchClients(String text) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        return ok(() -> fineractClient.clientSearchV2().searchClientsByText(request));
    }

    public PostClientsClientIdResponse activateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, ACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse closeClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, CLOSE_COMMAND));
    }

    public PostClientsClientIdResponse rejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, REJECT_COMMAND));
    }

    public PostClientsClientIdResponse reactivateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, REACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse withdrawClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, WITHDRAW_COMMAND));
    }

    public PostClientsClientIdResponse undoRejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_REJECTION_COMMAND));
    }

    public PostClientsClientIdResponse undoWithdrawnClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().handleCommandClient(clientId, request, UNDO_WITHDRAWAL_COMMAND));
    }

    public PutClientsClientIdResponse updateClient(Long clientId, PutClientsClientIdRequest request) {
        return ok(() -> fineractClient.clients().updateClient(clientId, request));
    }

    public DeleteClientsClientIdResponse deleteClient(Long clientId) {
        return ok(() -> fineractClient.clients().deleteClient(clientId));
    }

    // ------------------------------------------------------------------ staff (raw HTTP — command/response model gaps)

    /**
     * Assigns a staff member to the client via {@code ?command=assignStaff}. Raw HTTP because the shared
     * {@code PostClientsClientIdRequest} model has no {@code staffId} field — the sanctioned fallback for a
     * command-specific field missing from a model shared across many commands (pr_review_lessons_learned #8 / #11), not
     * REST-assured. Returns the {@code changes} object (contains the new {@code staffId}).
     */
    public JsonObject assignStaffToClient(Long clientId, Long staffId) {
        String response = FeignRawHttpHelper.post("/clients/" + clientId + "?command=" + ASSIGN_STAFF_COMMAND,
                GSON.toJson(Map.of("staffId", staffId)));
        return JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("changes");
    }

    /**
     * The staff id currently assigned to the client, or {@code null} if none. Raw HTTP because
     * {@code GetClientsClientIdResponse} does not expose {@code staffId} (#8 / #20 — never drop a read because the
     * typed model lacks the field).
     */
    public Long getClientStaffId(Long clientId) {
        String response = FeignRawHttpHelper.get("/clients/" + clientId);
        JsonElement staffId = JsonParser.parseString(response).getAsJsonObject().get("staffId");
        return staffId == null || staffId.isJsonNull() ? null : staffId.getAsLong();
    }
}
