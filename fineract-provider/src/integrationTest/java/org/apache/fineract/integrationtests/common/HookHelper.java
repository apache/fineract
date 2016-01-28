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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class HookHelper {
	
	private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_HOOK_URL = "/fineract-provider/api/v1/hooks?" + Utils.TENANT_IDENTIFIER;
    
    public HookHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }
    
    public Integer createHook(final String payloadURL) {
        System.out.println("---------------------------------CREATING A HOOK---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_HOOK_URL, getTestHookAsJson(payloadURL),
                "resourceId");
    }
    
    public String getTestHookAsJson(final String payloadURL) {
    	final HashMap<String, Object> map = new HashMap<>();
    	map.put("name", "Web");
    	map.put("displayName", Utils.randomNameGenerator("Hook_DisplayName_", 5));
        map.put("isActive", "true");
        final HashMap<String, String> config = new HashMap<>();
        config.put("Content Type", "json");
        config.put("Payload URL", payloadURL);
        map.put("config", config);
        final ArrayList<HashMap<String, String>> events = new ArrayList<>();
        final HashMap<String, String> createOfficeEvent = new HashMap<>();
        createOfficeEvent.put("actionName", "CREATE");
        createOfficeEvent.put("entityName", "OFFICE");
        events.add(createOfficeEvent);
        map.put("events", events);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
    
    public Integer updateHook(final String payloadURL, final Long hookId) {
        System.out.println("---------------------------------UPDATING HOOK---------------------------------------------");
        final String UPDATE_HOOK_URL = "/fineract-provider/api/v1/hooks/" + hookId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(this.requestSpec, this.responseSpec, UPDATE_HOOK_URL, getTestHookAsJson(payloadURL), "resourceId");
    }

    public Integer deleteHook(final Long hookId) {
        System.out.println("---------------------------------DELETING HOOK---------------------------------------------");
        final String DELETE_HOOK_URL = "/fineract-provider/api/v1/hooks/" + hookId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, this.responseSpec, DELETE_HOOK_URL, "resourceId");
    }
    
    public void verifyHookCreatedOnServer(final Long hookId) {
        System.out.println("------------------------------CHECK CREATE HOOK DETAILS------------------------------------\n");
        final String GET_URL = "/fineract-provider/api/v1/hooks/" + hookId + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseHookId = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_URL, "id");
        assertEquals(hookId.toString(), responseHookId.toString());
    }
    
    public void verifyUpdateHook(final String updateURL, final Long hookId) {
        System.out.println("------------------------------CHECK UPDATE HOOK DETAILS------------------------------------\n");
        final String GET_URL = "/fineract-provider/api/v1/hooks/" + hookId + "?" + Utils.TENANT_IDENTIFIER;
        ArrayList map = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_URL, "config");
        HashMap<String, String> hash = (HashMap<String, String>) map.get(1);
        assertEquals(updateURL, hash.get("fieldValue"));
    }
    
    public void verifyDeleteHook(final Long hookId) {
        System.out.println("------------------------------CHECK DELETE HOOK DETAILS------------------------------------\n");
        final String GET_URL = "/fineract-provider/api/v1/hooks/" + hookId + "?" + Utils.TENANT_IDENTIFIER;
        ResponseSpecification responseSpec404 = new ResponseSpecBuilder().expectStatusCode(404).build();
        ArrayList array = Utils.performServerGet(this.requestSpec, responseSpec404, GET_URL, "errors");
		HashMap<String, String> map = (HashMap<String, String>)array.get(0);
        assertEquals("error.msg.hook.identifier.not.found",map.get("userMessageGlobalisationCode"));
    }
}
