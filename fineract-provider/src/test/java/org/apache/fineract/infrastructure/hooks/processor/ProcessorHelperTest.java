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
package org.apache.fineract.infrastructure.hooks.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.hooks.data.HookSmsProviderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessorHelperTest {

    private ProcessorHelper processorHelper;

    @BeforeEach
    void setUp() throws Exception {
        processorHelper = new ProcessorHelper();
    }

    @Test
    void sendsToAPayloadUrlWithoutTrailingSlash() {
        final String url = "https://example.com/hook";

        final WebHookService service = processorHelper.createWebHookService(url);

        assertEquals(url, service.sendJsonRequest(url, "CLIENT", "CREATE", "default", null, new JsonObject()).request().url().toString());
        assertEquals(url, service.sendFormRequest(url, "CLIENT", "CREATE", "default", null, Map.of()).request().url().toString());
        assertEquals(url, service.sendEmptyRequest(url).request().url().toString());
    }

    @Test
    void keepsAPayloadUrlWithTrailingSlashAndQueryUnchanged() {
        final String withSlash = "https://example.com/hook/";
        final String withQuery = "https://example.com/hooks/fineract?token=abc";

        assertEquals(withSlash, processorHelper.createWebHookService(withSlash).sendEmptyRequest(withSlash).request().url().toString());
        assertEquals(withQuery, processorHelper.createWebHookService(withQuery).sendEmptyRequest(withQuery).request().url().toString());
    }

    @Test
    void sendsTheSmsBridgeConfigurationToTheHostRoot() {
        final WebHookService service = processorHelper.createWebHookService("https://bridge.example.com/sms");

        assertEquals("https://bridge.example.com/configuration",
                service.sendSmsBridgeConfigRequest(new HookSmsProviderData(Set.of())).request().url().toString());
    }

    @Test
    void rejectsAnInvalidPayloadUrl() {
        assertThrows(IllegalArgumentException.class, () -> processorHelper.createWebHookService("not a url"));
    }
}
