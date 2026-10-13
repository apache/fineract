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

import com.google.gson.JsonObject;
import java.util.Map;
import org.apache.fineract.infrastructure.hooks.data.HookSmsProviderData;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface WebHookService {

    String ENTITY_HEADER = "X-Fineract-Entity";
    String ACTION_HEADER = "X-Fineract-Action";
    String TENANT_HEADER = "Fineract-Platform-TenantId";
    String ENDPOINT_HEADER = "X-Fineract-Endpoint";
    String API_KEY_HEADER = "X-Fineract-API-Key";

    // The Payload URL is passed as the full request URL (@Url), so it doesn't have to end with "/".

    // Ping
    @GET
    Call<Void> sendEmptyRequest(@Url String url);

    // Template - Web
    @POST
    Call<Void> sendJsonRequest(@Url String url, @Header(ENTITY_HEADER) String entityHeader, @Header(ACTION_HEADER) String actionHeader,
            @Header(TENANT_HEADER) String tenantHeader, @Header(ENDPOINT_HEADER) String endpointHeader, @Body JsonObject result);

    @FormUrlEncoded
    @POST
    Call<Void> sendFormRequest(@Url String url, @Header(ENTITY_HEADER) String entityHeader, @Header(ACTION_HEADER) String actionHeader,
            @Header(TENANT_HEADER) String tenantHeader, @Header(ENDPOINT_HEADER) String endpointHeader,
            @FieldMap Map<String, String> params);

    // Template - SMS Bridge
    @POST
    Call<Void> sendSmsBridgeRequest(@Url String url, @Header(ENTITY_HEADER) String entityHeader, @Header(ACTION_HEADER) String actionHeader,
            @Header(TENANT_HEADER) String tenantHeader, @Header(API_KEY_HEADER) String apiKeyHeader, @Body JsonObject result);

    @POST("/configuration")
    Call<String> sendSmsBridgeConfigRequest(@Body HookSmsProviderData config);

}
