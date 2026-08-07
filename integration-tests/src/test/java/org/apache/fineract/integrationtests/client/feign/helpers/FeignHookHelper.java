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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.HookCreateRequest;
import org.apache.fineract.client.models.HookCreateResponse;
import org.apache.fineract.client.models.HookData;
import org.apache.fineract.client.models.HookDeleteResponse;
import org.apache.fineract.client.models.HookEventData;
import org.apache.fineract.client.models.HookFieldData;
import org.apache.fineract.client.models.HookUpdateRequest;
import org.apache.fineract.client.models.HookUpdateResponse;
import org.apache.fineract.infrastructure.hooks.api.HookApiConstants;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignHookHelper {

    private static final String JSON_CONTENT_TYPE = "json";
    private static final String OFFICE_ENTITY = "OFFICE";
    private static final String CREATE_ACTION = "CREATE";

    /** The {@code template} query flag; these tests only need the hook itself. */
    private static final boolean WITHOUT_TEMPLATE = false;

    private final FineractFeignClient fineractClient;

    public FeignHookHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public HookCreateResponse createHook(String payloadUrl) {
        HookCreateRequest request = new HookCreateRequest()//
                .name(HookApiConstants.webTemplateName)//
                .displayName(Utils.randomStringGenerator("Hook_DisplayName_", 5))//
                .isActive(true)//
                .config(webHookConfig(payloadUrl))//
                .events(officeCreatedEvents());
        return ok(() -> fineractClient.hooks().createHook(request));
    }

    public HookUpdateResponse updateHook(Long hookId, String payloadUrl) {
        HookUpdateRequest request = new HookUpdateRequest()//
                .name(HookApiConstants.webTemplateName)//
                .displayName(Utils.randomStringGenerator("Hook_DisplayName_", 5))//
                .isActive(true)//
                .config(webHookConfig(payloadUrl))//
                .events(officeCreatedEvents());
        return ok(() -> fineractClient.hooks().updateHook(hookId, request));
    }

    public HookDeleteResponse deleteHook(Long hookId) {
        return ok(() -> fineractClient.hooks().deleteHook(hookId));
    }

    public HookData retrieveHook(Long hookId) {
        return ok(() -> fineractClient.hooks().retrieveOneHook(hookId, WITHOUT_TEMPLATE));
    }

    public CallFailedRuntimeException retrieveHookExpectingError(Long hookId) {
        return fail(() -> fineractClient.hooks().retrieveOneHook(hookId, WITHOUT_TEMPLATE));
    }

    /** Located by field name because the server promises no order for the configuration list. */
    public String retrievePayloadUrl(Long hookId) {
        return retrieveHook(hookId).getConfig().stream()//
                .filter(field -> HookApiConstants.payloadURLName.equals(field.getFieldName()))//
                .map(HookFieldData::getFieldValue)//
                .findFirst()//
                .orElseThrow(
                        () -> new AssertionError("Hook " + hookId + " has no '" + HookApiConstants.payloadURLName + "' configuration"));
    }

    private Map<String, String> webHookConfig(String payloadUrl) {
        Map<String, String> config = new HashMap<>();
        config.put(HookApiConstants.contentTypeName, JSON_CONTENT_TYPE);
        config.put(HookApiConstants.payloadURLName, payloadUrl);
        return config;
    }

    private List<HookEventData> officeCreatedEvents() {
        return List.of(new HookEventData().actionName(CREATE_ACTION).entityName(OFFICE_ENTITY));
    }
}
