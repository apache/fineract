/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.hooks.processor;

import java.util.Map;

import org.mifosplatform.infrastructure.hooks.processor.data.SmsProviderData;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;

import com.google.gson.JsonObject;

public interface WebHookService {

	final static String ENTITY_HEADER = "X-Mifos-Entity";
	final static String ACTION_HEADER = "X-Mifos-Action";
	final static String TENANT_HEADER = "X-Mifos-Platform-TenantId";
	final static String ENDPOINT_HEADER = "X-Mifos-Endpoint";
	final static String API_KEY_HEADER = "X-Mifos-API-Key";

	// Ping
	@GET("/")
	Response sendEmptyRequest();

	// Template - Web
	@POST("/")
	void sendJsonRequest(@Header(ENTITY_HEADER) String entityHeader,
			@Header(ACTION_HEADER) String actionHeader,
			@Header(TENANT_HEADER) String tenantHeader,
			@Header(ENDPOINT_HEADER) String endpointHeader,
			@Body JsonObject result, Callback<Response> callBack);

	@FormUrlEncoded
	@POST("/")
	void sendFormRequest(@Header(ENTITY_HEADER) String entityHeader,
			@Header(ACTION_HEADER) String actionHeader,
			@Header(TENANT_HEADER) String tenantHeader,
			@Header(ENDPOINT_HEADER) String endpointHeader,
			@FieldMap Map<String, String> params, Callback<Response> callBack);

	// Template - SMS Bridge
	@POST("/")
	void sendSmsBridgeRequest(@Header(ENTITY_HEADER) String entityHeader,
			@Header(ACTION_HEADER) String actionHeader,
			@Header(TENANT_HEADER) String tenantHeader,
			@Header(API_KEY_HEADER) String apiKeyHeader,
			@Body JsonObject result, Callback<Response> callBack);

	@POST("/configuration")
	String sendSmsBridgeConfigRequest(@Body SmsProviderData config);

}
