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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes" })
public class GlobalConfigurationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public GlobalConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList getAllGlobalConfigurations(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String GET_ALL_GLOBAL_CONFIG_URL = "/fineract-provider/api/v1/configurations?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL GLOBAL CONFIGURATIONS -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_GLOBAL_CONFIG_URL, "");
        return (ArrayList) response.get("globalConfiguration");
    }

    public static HashMap getGlobalConfigurationById(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String configId) {
        final String GET_GLOBAL_CONFIG_BY_ID_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING GLOBAL CONFIGURATION BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_GLOBAL_CONFIG_BY_ID_URL, "");
    }

    public static Integer updateValueForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String configId, final String value) {
        final String GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE VALUE FOR GLOBAL CONFIG---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, GLOBAL_CONFIG_UPDATE_URL, updateGlobalConfigUpdateValueAsJSON(value),
                "resourceId");
    }

    public static Integer updateEnabledFlagForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String configId, final Boolean enabled) {
        final String GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        System.out
                .println("---------------------------------UPDATE GLOBAL CONFIG FOR ENABLED FLAG---------------------------------------------");
        return Utils.performServerPut(requestSpec, responseSpec, GLOBAL_CONFIG_UPDATE_URL,
                updateGlobalConfigUpdateEnabledFlagAsJSON(enabled), "resourceId");
    }

    public static ArrayList getGlobalConfigurationIsCacheEnabled(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_IS_CACHE_GLOBAL_CONFIG_URL = "/fineract-provider/api/v1/caches?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING IS CACHE ENABLED GLOBAL CONFIGURATION -------------------------");
        final ArrayList<HashMap> response = Utils.performServerGet(requestSpec, responseSpec, GET_IS_CACHE_GLOBAL_CONFIG_URL, "");
        return response;
    }

    public static HashMap updateIsCacheEnabledForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String cacheType) {
        final String IS_CACHE_GLOBAL_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/caches?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------UPDATE GLOBAL CONFIG FOR IS CACHE ENABLED----------------------");
        return Utils.performServerPut(requestSpec, responseSpec, IS_CACHE_GLOBAL_CONFIG_UPDATE_URL,
                updateIsCacheEnabledGlobalConfigUpdateAsJSON(cacheType), "changes");
    }
    
    public static Object updatePasswordResetDaysForGlobalConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer configId, final String value, final String enabled, final String jsonAttributeToGetBack) {
        final String UPDATE_URL = "/fineract-provider/api/v1/configurations/" + configId + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------UPDATE GLOBAL CONFIG FOR FORCE PASSWORD RESET DAYS----------------------");
        return Utils.performServerPut(requestSpec, responseSpec, UPDATE_URL,
                updatePasswordResetDaysGlobalConfigAsJSON(value, enabled), jsonAttributeToGetBack);
    }

    public static String updateGlobalConfigUpdateValueAsJSON(final String value) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("value", value);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
    
    public static String updatePasswordResetDaysGlobalConfigAsJSON(final String value, final String enabled) {
        final HashMap<String, String> map = new HashMap<>();
        if(value != null){
            map.put("value", value);
        }
        map.put("enabled", enabled);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String updateGlobalConfigUpdateEnabledFlagAsJSON(final Boolean enabled) {
        final HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("enabled", enabled);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static String updateIsCacheEnabledGlobalConfigUpdateAsJSON(final String cacheType) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("cacheType", cacheType);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

}