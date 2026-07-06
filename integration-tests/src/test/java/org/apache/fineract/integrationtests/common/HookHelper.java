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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.HookCreateRequest;
import org.apache.fineract.client.models.HookCreateResponse;
import org.apache.fineract.client.models.HookData;
import org.apache.fineract.client.models.HookDeleteResponse;
import org.apache.fineract.client.models.HookEventData;
import org.apache.fineract.client.models.HookFieldData;
import org.apache.fineract.client.models.HookUpdateRequest;
import org.apache.fineract.client.models.HookUpdateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HookHelper {

    private static final Logger LOG = LoggerFactory.getLogger(HookHelper.class);

    private HookHelper() {}

    public static HookCreateResponse createHook(final String payloadURL) {
        LOG.info("---------------------------------CREATING A HOOK---------------------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().hooks().createHook(getTestHookRequest(payloadURL)));
    }

    private static HookCreateRequest getTestHookRequest(final String payloadURL) {
        final HashMap<String, String> config = new HashMap<>();
        config.put("Content Type", "json");
        config.put("Payload URL", payloadURL);
        final List<HookEventData> events = List.of(new HookEventData().actionName("CREATE").entityName("OFFICE"));
        return new HookCreateRequest().name("Web").displayName(Utils.randomStringGenerator("Hook_DisplayName_", 5)).isActive(true)
                .config(config).events(events);
    }

    public static HookUpdateResponse updateHook(final String payloadURL, final Long hookId) {
        LOG.info("---------------------------------UPDATING HOOK---------------------------------------------");
        final HashMap<String, String> config = new HashMap<>();
        config.put("Content Type", "json");
        config.put("Payload URL", payloadURL);
        final List<HookEventData> events = List.of(new HookEventData().actionName("CREATE").entityName("OFFICE"));
        final HookUpdateRequest request = new HookUpdateRequest().name("Web")
                .displayName(Utils.randomStringGenerator("Hook_DisplayName_", 5)).isActive(true).config(config).events(events);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().hooks().updateHook(hookId, request));
    }

    public static HookDeleteResponse deleteHook(final Long hookId) {
        LOG.info("---------------------------------DELETING HOOK---------------------------------------------");
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().hooks().deleteHook(hookId));
    }

    public static void verifyHookCreatedOnServer(final Long hookId) {
        LOG.info("------------------------------CHECK CREATE HOOK DETAILS------------------------------------\n");
        final HookData response = ok(() -> FineractFeignClientHelper.getFineractFeignClient().hooks().retrieveOneHook(hookId, false));
        assertEquals(hookId, response.getId());
    }

    public static void verifyUpdateHook(final String updateURL, final Long hookId) {
        LOG.info("------------------------------CHECK UPDATE HOOK DETAILS------------------------------------\n");
        final HookData response = ok(() -> FineractFeignClientHelper.getFineractFeignClient().hooks().retrieveOneHook(hookId, false));
        final HookFieldData hookFieldData = response.getConfig().get(1);
        assertEquals(updateURL, hookFieldData.getFieldValue());
    }

    public static void verifyDeleteHook(final Long hookId) {
        LOG.info("------------------------------CHECK DELETE HOOK DETAILS------------------------------------\n");
        final CallFailedRuntimeException exception = fail(
                () -> FineractFeignClientHelper.getFineractFeignClient().hooks().retrieveOneHook(hookId, false));
        assertEquals(404, exception.getStatus());
    }
}
