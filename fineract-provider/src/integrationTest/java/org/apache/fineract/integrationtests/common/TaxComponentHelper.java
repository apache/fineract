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

import java.util.HashMap;

import org.apache.fineract.integrationtests.common.accounting.Account;

import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

public class TaxComponentHelper {

    private static final String CREATE_TAX_COMPONENT_URL = "/fineract-provider/api/v1/taxes/component?" + Utils.TENANT_IDENTIFIER;

    public static Integer createTaxComponent(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String percentage, final Integer liabilityAccountId) {
        System.out.println("---------------------------------CREATING A TAX COMPONENT---------------------------------------------");
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_TAX_COMPONENT_URL,
                getTaxComponentAsJSON(percentage, liabilityAccountId), "resourceId");
    }

    public static String getTaxComponentAsJSON(final String percentage, final Integer creditAccountId) {
        final HashMap<String, String> map = getBasicTaxComponentMap(percentage);
        if (creditAccountId != null) {
            map.put("creditAccountType", Account.AccountType.LIABILITY.toString());
            map.put("creditAcountId", String.valueOf(creditAccountId));
        }
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }

    public static HashMap<String, String> getBasicTaxComponentMap(final String percentage) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("name", randomNameGenerator("Tax_component_Name_", 5));
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("percentage", percentage);
        map.put("startDate", "01 January 2013");
        return map;
    }

    public static String randomNameGenerator(final String prefix, final int lenOfRandomSuffix) {
        return Utils.randomStringGenerator(prefix, lenOfRandomSuffix);
    }

}