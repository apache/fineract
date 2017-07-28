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
package org.apache.fineract.integrationtests.common.organisation;

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class EntityDatatableChecksHelper {

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    private static final String DATATABLE_CHECK_URL = "/fineract-provider/api/v1/entityDatatableChecks";

    public EntityDatatableChecksHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Integer createEntityDatatableCheck(final String apptableName, final String datatableName, final int status, final Integer productId) {
        return Utils.performServerPost(this.requestSpec, this.responseSpec, DATATABLE_CHECK_URL + "?" + Utils.TENANT_IDENTIFIER,
                getTestEdcAsJSON(apptableName, datatableName, status, productId), "resourceId");
    }

    public Integer deleteEntityDatatableCheck(final Integer entityDatatableCheckId) {
        return Utils.performServerDelete(requestSpec, responseSpec, DATATABLE_CHECK_URL + "/" + entityDatatableCheckId + "?"
                + Utils.TENANT_IDENTIFIER, "resourceId");
    }

    public String retrieveEntityDatatableCheck() {
        return Utils.performServerGet(requestSpec, responseSpec, DATATABLE_CHECK_URL + "?" + Utils.TENANT_IDENTIFIER, null);
    }

    public static String getTestEdcAsJSON(final String apptableName, final String datatableName, final int status, final Integer productId) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("entity", apptableName);
        map.put("status", status);
        map.put("datatableName", datatableName);
        if (productId != null) {
            map.put("productId", productId);
        }
        String requestJsonString = new Gson().toJson(map);
        System.out.println("map : " + requestJsonString);
        return requestJsonString;
    }

}
