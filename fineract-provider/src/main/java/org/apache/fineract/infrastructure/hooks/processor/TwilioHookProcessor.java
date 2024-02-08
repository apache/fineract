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

import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.apiKeyName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.domain.FineractContext;
import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookConfiguration;
import org.apache.fineract.infrastructure.hooks.domain.HookConfigurationRepository;
import org.apache.fineract.infrastructure.hooks.processor.data.SmsProviderData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.template.service.TemplateMergeService;
import org.springframework.stereotype.Service;
import retrofit2.Callback;

@Service
@RequiredArgsConstructor
public class TwilioHookProcessor implements HookProcessor {

    private final HookConfigurationRepository hookConfigurationRepository;
    private final TemplateMergeService templateMergeService;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ProcessorHelper processorHelper;

    @Override
    public void process(final Hook hook, final String payload, final String entityName, final String actionName,
            final FineractContext context) throws IOException {

        final SmsProviderData smsProviderData = new SmsProviderData(hook.getConfig());

        sendRequest(smsProviderData, payload, entityName, actionName, hook, context);
    }

    @SuppressWarnings("unchecked")
    private void sendRequest(final SmsProviderData smsProviderData, final String payload, String entityName, String actionName,
            final Hook hook, final FineractContext context) throws IOException {

        final WebHookService service = processorHelper.createWebHookService(smsProviderData.getUrl());

        @SuppressWarnings("rawtypes")
        final Callback callback = processorHelper.createCallback(smsProviderData.getUrl());

        String apiKey = this.hookConfigurationRepository.findOneByHookIdAndFieldName(hook.getId(), apiKeyName);
        if (apiKey == null) {
            smsProviderData.setUrl(null);
            smsProviderData.setEndpoint(System.getProperty("baseUrl"));
            smsProviderData.setTenantId(context.getTenantContext().getTenantIdentifier());
            smsProviderData.setMifosToken(context.getAuthTokenContext());
            apiKey = service.sendSmsBridgeConfigRequest(smsProviderData).execute().body();
            final HookConfiguration apiKeyEntry = HookConfiguration.createNew(hook, "string", apiKeyName, apiKey);
            this.hookConfigurationRepository.save(apiKeyEntry);
        }

        if (apiKey != null && !apiKey.equals("")) {
            JsonObject json;
            if (hook.getUgdTemplate() != null) {
                entityName = "sms";
                actionName = "send";
                json = processUgdTemplate(payload, hook);
                if (json == null) {
                    return;
                }
            } else {
                json = JsonParser.parseString(payload).getAsJsonObject();
            }
            service.sendSmsBridgeRequest(entityName, actionName, context.getTenantContext().getTenantIdentifier(), apiKey, json)
                    .enqueue(callback);
        }
    }

    private JsonObject processUgdTemplate(final String payload, final Hook hook) throws IOException {
        JsonObject json = null;
        @SuppressWarnings("unchecked")
        final HashMap<String, Object> map = new ObjectMapper().readValue(payload, HashMap.class);
        map.put("BASE_URI", System.getProperty("baseUrl"));
        if (map.containsKey("clientId")) {
            final Long clientId = Long.valueOf(Integer.toString((int) map.get("clientId")));
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final String mobileNo = client.mobileNo();
            if (mobileNo != null && !mobileNo.isEmpty()) {
                final String compiledMessage = this.templateMergeService.compile(hook.getUgdTemplate(), map).replace("<p>", "")
                        .replace("</p>", "");
                final Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("mobileNo", mobileNo);
                jsonMap.put("message", compiledMessage);
                final String jsonString = new Gson().toJson(jsonMap);
                json = JsonParser.parseString(jsonString).getAsJsonObject();
            }
        }
        return json;
    }

}
