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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookConfiguration;
import org.apache.fineract.infrastructure.hooks.domain.HookConfigurationRepository;
import org.apache.fineract.infrastructure.hooks.processor.data.SmsProviderData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.template.service.TemplateMergeService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import retrofit.Callback;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class TwilioHookProcessor implements HookProcessor {

    private final HookConfigurationRepository hookConfigurationRepository;
    private final TemplateMergeService templateMergeService;
    private final ClientRepositoryWrapper clientRepositoryWrapper;

    @Autowired
    public TwilioHookProcessor(
            final HookConfigurationRepository hookConfigurationRepository,
            final TemplateMergeService templateMergeService,
            final ClientRepositoryWrapper clientRepositoryWrapper) {
        this.hookConfigurationRepository = hookConfigurationRepository;
        this.templateMergeService = templateMergeService;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
    }

    @Override
    public void process(final Hook hook,
            @SuppressWarnings("unused") final AppUser appUser,
            final String payload, final String entityName,
            final String actionName, final String tenantIdentifier,
            final String authToken) {

        final SmsProviderData smsProviderData = new SmsProviderData(
                hook.getHookConfig());

        sendRequest(smsProviderData, payload, entityName, actionName,
                tenantIdentifier, authToken, hook);
    }

    @SuppressWarnings("unchecked")
    private void sendRequest(final SmsProviderData smsProviderData,
            final String payload, String entityName, String actionName,
            final String tenantIdentifier, final String authToken,
            final Hook hook) {

        final WebHookService service = ProcessorHelper
                .createWebHookService(smsProviderData.getUrl());

        @SuppressWarnings("rawtypes")
        final Callback callback = ProcessorHelper
                .createCallback(smsProviderData.getUrl());

        String apiKey = this.hookConfigurationRepository
                .findOneByHookIdAndFieldName(hook.getId(), apiKeyName);
        if (apiKey == null) {
            smsProviderData.setUrl(null);
            smsProviderData.setEndpoint(System.getProperty("baseUrl"));
            smsProviderData.setTenantId(tenantIdentifier);
            smsProviderData.setMifosToken(authToken);
            apiKey = service.sendSmsBridgeConfigRequest(smsProviderData);
            final HookConfiguration apiKeyEntry = HookConfiguration.createNew(
                    hook, "string", apiKeyName, apiKey);
            this.hookConfigurationRepository.save(apiKeyEntry);
        }

        if (apiKey != null && !apiKey.equals("")) {
            JsonObject json = null;
            if (hook.getUgdTemplate() != null) {
                entityName = "sms";
                actionName = "send";
                json = processUgdTemplate(payload, hook, authToken);
                if (json == null) {
                    return;
                }
            } else {
                json = new JsonParser().parse(payload).getAsJsonObject();
            }
            service.sendSmsBridgeRequest(entityName, actionName,
                    tenantIdentifier, apiKey, json, callback);
        }

    }

    private JsonObject processUgdTemplate(final String payload,
            final Hook hook, final String authToken) {
        JsonObject json = null;
        try {
            @SuppressWarnings("unchecked")
            final HashMap<String, Object> map = new ObjectMapper().readValue(
                    payload, HashMap.class);
            map.put("BASE_URI", System.getProperty("baseUrl"));
            if (map.containsKey("clientId")) {
                final Long clientId = new Long(Integer.toString((int) map
                        .get("clientId")));
                final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
                final String mobileNo = client.mobileNo();
                if (mobileNo != null && !mobileNo.isEmpty()) {
                    this.templateMergeService.setAuthToken(authToken);
                    final String compiledMessage = this.templateMergeService
                            .compile(hook.getUgdTemplate(), map)
                            .replace("<p>", "").replace("</p>", "");
                    final Map<String, String> jsonMap = new HashMap<>();
                    jsonMap.put("mobileNo", mobileNo);
                    jsonMap.put("message", compiledMessage);
                    final String jsonString = new Gson().toJson(jsonMap);
                    json = new JsonParser().parse(jsonString).getAsJsonObject();
                }
            }
        } catch (IOException e) {
        }
        return json;
    }

}
