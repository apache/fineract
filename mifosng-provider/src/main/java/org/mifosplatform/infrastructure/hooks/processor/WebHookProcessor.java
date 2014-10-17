/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.contentTypeName;
import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.payloadURLName;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.hooks.domain.Hook;
import org.mifosplatform.infrastructure.hooks.domain.HookConfiguration;
import org.mifosplatform.useradministration.domain.AppUser;
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

		final String mifosEndpointUrl = System.getProperty("baseUrl");
		final WebHookService service = ProcessorHelper
				.createWebHookService(url);

		@SuppressWarnings("rawtypes")
		final Callback callback = ProcessorHelper.createCallback(url);

		if (contentType.equalsIgnoreCase("json")
				|| contentType.contains("json")) {
			final JsonObject json = new JsonParser().parse(payload)
					.getAsJsonObject();
			service.sendJsonRequest(entityName, actionName, tenantIdentifier,
					mifosEndpointUrl, json, callback);
		} else {
			Map<String, String> map = new HashMap<>();
			map = new Gson().fromJson(payload, map.getClass());
			service.sendFormRequest(entityName, actionName, tenantIdentifier,
					mifosEndpointUrl, map, callback);
		}

	}

}
