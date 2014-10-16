/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class HookHelper {
	
	private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String CREATE_HOOK_URL = "/mifosng-provider/api/v1/hooks?" + Utils.TENANT_IDENTIFIER;

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

}
