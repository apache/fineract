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
package org.apache.fineract.integrationtests.common.funds;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import org.apache.fineract.integrationtests.common.Utils;

import java.util.HashMap;
import java.util.List;

public class FundsResourceHandler {

    private static final String FUNDS_URL = "/fineract-provider/api/v1/funds";
    private static final String CREATE_FUNDS_URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;

    public static Integer createFund(final String fundJSON,
                                     final RequestSpecification requestSpec,
                                     final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_FUNDS_URL, fundJSON, "resourceId");
    }

    public static List<FundsHelper> retrieveAllFunds(final RequestSpecification requestSpec,
                                                                 final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "?" + Utils.TENANT_IDENTIFIER;
        List<HashMap<String, Object>> list = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        final String jsonData = new Gson().toJson(list);
        return new Gson().fromJson(jsonData, new TypeToken<List<FundsHelper>>(){}.getType());
    }

    public static String retrieveFund(final Long fundID,
                                      final RequestSpecification requestSpec,
                                      final ResponseSpecification responseSpec) {
        final String URL = FUNDS_URL + "/" + fundID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, URL, "");
        return new Gson().toJson(response);
    }

    public static FundsHelper updateFund(final Long fundID,
                                         final String newName,
                                         final String newExternalId,
                                         final RequestSpecification requestSpec,
                                         final ResponseSpecification responseSpec) {
        FundsHelper fh = FundsHelper.create(newName).externalId(newExternalId).build();
        String updateJSON = new Gson().toJson(fh);

        final String URL = FUNDS_URL + "/" + fundID + "?" + Utils.TENANT_IDENTIFIER;
        final HashMap<String, String> response = Utils.performServerPut(requestSpec, responseSpec, URL, updateJSON, "changes");
        final String jsonData = new Gson().toJson(response);
        return new Gson().fromJson(jsonData, FundsHelper.class);
    }

}
