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

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class ExternalServicesConfigurationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public ExternalServicesConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList<HashMap> getExternalServicesConfigurationByServiceName(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName) {
        final String GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL = "/fineract-provider/api/v1/externalservice/" + serviceName + "?"
                + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING GLOBAL CONFIGURATION BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL, "");
    }

    public static HashMap updateValueForExternaServicesConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName, final String name, final String value) {
        final String EXTERNAL_SERVICES_CONFIG_UPDATE_URL = "/fineract-provider/api/v1/externalservice/" + serviceName + "?"
                + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE VALUE FOR GLOBAL CONFIG---------------------------------------------");
        HashMap map = Utils.performServerPut(requestSpec, responseSpec, EXTERNAL_SERVICES_CONFIG_UPDATE_URL,
                updateExternalServicesConfigUpdateValueAsJSON(name, value), "");

        return (HashMap) map.get("changes");
    }

    public static String updateExternalServicesConfigUpdateValueAsJSON(final String name, final String value) {
        final HashMap<String, String> map = new HashMap<>();
        map.put(name, value);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

}
