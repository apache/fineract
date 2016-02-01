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

import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.contentTypeName;
import static org.apache.fineract.infrastructure.hooks.api.HookApiConstants.payloadURLName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.fineract.infrastructure.hooks.domain.Hook;
import org.apache.fineract.infrastructure.hooks.domain.HookConfiguration;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

import retrofit.Callback;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class WebHookProcessor implements HookProcessor {

	@Override
	public void process(final Hook hook,
			@SuppressWarnings("unused") final AppUser appUser,
			final String payload, final String entityName,
			final String actionName, final String tenantIdentifier,
			final String authToken) {

		final Set<HookConfiguration> config = hook.getHookConfig();

		String url = "";
		String contentType = "";

		for (final HookConfiguration conf : config) {
			final String fieldName = conf.getFieldName();
			if (fieldName.equals(payloadURLName)) {
				url = conf.getFieldValue();
			}
			if (fieldName.equals(contentTypeName)) {
				contentType = conf.getFieldValue();
			}
		}

		sendRequest(url, contentType, payload, entityName, actionName,
				tenantIdentifier, authToken);

	}

	@SuppressWarnings("unchecked")
	private void sendRequest(final String url, final String contentType,
			final String payload, final String entityName,
			final String actionName, final String tenantIdentifier,
			@SuppressWarnings("unused") final String authToken) {

		final String fineractEndpointUrl = System.getProperty("baseUrl");
		final WebHookService service = ProcessorHelper
				.createWebHookService(url);

		@SuppressWarnings("rawtypes")
		final Callback callback = ProcessorHelper.createCallback(url);

		if (contentType.equalsIgnoreCase("json")
				|| contentType.contains("json")) {
			final JsonObject json = new JsonParser().parse(payload)
					.getAsJsonObject();
			service.sendJsonRequest(entityName, actionName, tenantIdentifier,
					fineractEndpointUrl, json, callback);
		} else {
			Map<String, String> map = new HashMap<>();
			map = new Gson().fromJson(payload, map.getClass());
			service.sendFormRequest(entityName, actionName, tenantIdentifier,
					fineractEndpointUrl, map, callback);
		}

	}

}
