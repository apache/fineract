/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor;

import static org.mifosplatform.infrastructure.hooks.api.HookApiConstants.apiKeyName;

import org.mifosplatform.infrastructure.hooks.domain.Hook;
import org.mifosplatform.infrastructure.hooks.domain.HookConfiguration;
import org.mifosplatform.infrastructure.hooks.domain.HookConfigurationRepository;
import org.mifosplatform.infrastructure.hooks.processor.data.SmsProviderData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import retrofit.Callback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class TwilioHookProcessor implements HookProcessor {

	private final HookConfigurationRepository hookConfigurationRepository;

	@Autowired
	public TwilioHookProcessor(
			final HookConfigurationRepository hookConfigurationRepository) {
		this.hookConfigurationRepository = hookConfigurationRepository;
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
			final String payload, final String entityName,
			final String actionName, final String tenantIdentifier,
			final String authToken, final Hook hook) {

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
			final JsonObject json = new JsonParser().parse(payload)
					.getAsJsonObject();
			service.sendSmsBridgeRequest(entityName, actionName,
					tenantIdentifier, apiKey, json, callback);
		}

	}

}
