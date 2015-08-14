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

public class ExternalServicesConfigurationHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public ExternalServicesConfigurationHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public static ArrayList<HashMap> getExternalServicesConfigurationByServiceName(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName) {
        final String GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL = "/mifosng-provider/api/v1/externalservice/" + serviceName + "?"
                + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING GLOBAL CONFIGURATION BY ID -------------------------");
        return Utils.performServerGet(requestSpec, responseSpec, GET_EXTERNAL_SERVICES_CONFIG_BY_SERVICE_NAME_URL, "");
    }

    public static HashMap updateValueForExternaServicesConfiguration(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String serviceName, final String name, final String value) {
        final String EXTERNAL_SERVICES_CONFIG_UPDATE_URL = "/mifosng-provider/api/v1/externalservice/" + serviceName + "?"
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
