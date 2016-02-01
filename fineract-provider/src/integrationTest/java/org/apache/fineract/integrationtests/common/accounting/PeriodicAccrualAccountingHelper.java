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
package org.apache.fineract.integrationtests.common.accounting;

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class PeriodicAccrualAccountingHelper {

    private static final String PERIODIC_ACCRUAL_URL = "/fineract-provider/api/v1/runaccruals";
    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public PeriodicAccrualAccountingHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Object runPeriodicAccrualAccounting(String date) {
        String json = getRunPeriodicAccrual(date);
        return Utils.performServerPost(this.requestSpec, this.responseSpec, PERIODIC_ACCRUAL_URL + "?" + Utils.TENANT_IDENTIFIER, json, "");
    }

    private String getRunPeriodicAccrual(String date) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en_GB");
        map.put("tillDate", date);
        return new Gson().toJson(map);
    }

}
