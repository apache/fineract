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

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetCodeValuesDataResponse;
import org.apache.fineract.client.models.PostClientsClientIdResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostCodeValuesDataRequest;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignClientHelper {

    private static final String CLIENT_CLOSURE_REASON_CODE = "ClientClosureReason";
    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final String LOCALE = "en";

    interface ClientCommandApi {

        @RequestLine("POST /v1/clients/{clientId}?command={command}")
        @Headers({ "Content-Type: application/json", "Accept: application/json" })
        PostClientsClientIdResponse executeCommand(@Param("clientId") Long clientId, @Param("command") String command,
                Map<String, Object> body);
    }

    private final FineractFeignClient fineractClient;
    private final ClientCommandApi clientCommandApi;

    public FeignClientHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
        this.clientCommandApi = fineractClient.create(ClientCommandApi.class);
    }

    public Long createClient() {
        return createClient(Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
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

        return createClient(request);
    }

    public Long createClient(PostClientsRequest request) {
        PostClientsResponse response = ok(() -> fineractClient.clients().create6(request));
        return response.getClientId();
    }

    public GetClientsClientIdResponse getClient(Long clientId) {
        return ok(() -> fineractClient.clients().retrieveOne11(clientId, Collections.emptyMap()));
    }

    public PostClientsClientIdResponse closeClient(Long clientId) {
        Long closureReasonId = getOrCreateClosureReasonId();
        String closureDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        Map<String, Object> body = new HashMap<>();
        body.put("closureDate", closureDate);
        body.put("closureReasonId", closureReasonId);
        body.put("dateFormat", DATE_FORMAT);
        body.put("locale", LOCALE);

        return ok(() -> clientCommandApi.executeCommand(clientId, "close", body));
    }

    public PostClientsClientIdResponse reactivateClient(Long clientId) {
        String reactivationDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        Map<String, Object> body = new HashMap<>();
        body.put("reactivationDate", reactivationDate);
        body.put("dateFormat", DATE_FORMAT);
        body.put("locale", LOCALE);

        return ok(() -> clientCommandApi.executeCommand(clientId, "reactivate", body));
    }

    private Long getOrCreateClosureReasonId() {
        List<GetCodeValuesDataResponse> codeValues = ok(
                () -> fineractClient.codeValues().retrieveAllCodeValues1(CLIENT_CLOSURE_REASON_CODE));

        if (codeValues != null && !codeValues.isEmpty()) {
            return codeValues.get(0).getId();
        }

        PostCodeValuesDataRequest request = new PostCodeValuesDataRequest().name("Test Closure Reason").isActive(true).position(1);

        return ok(() -> fineractClient.codeValues().createCodeValue1(CLIENT_CLOSURE_REASON_CODE, request)).getSubResourceId();
    }
}
