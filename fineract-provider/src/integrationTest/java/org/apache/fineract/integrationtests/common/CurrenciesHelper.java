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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public class CurrenciesHelper {

    private static final String CURRENCIES_URL = "/fineract-provider/api/v1/currencies";

    public static ArrayList<CurrencyDomain> getAllCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_ALL_CURRENCIES_URL = CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL CURRENCIES -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_CURRENCIES_URL, "");
        ArrayList<HashMap> selectedCurrencyOptions = (ArrayList<HashMap>) response.get("selectedCurrencyOptions");
        ArrayList<HashMap> currencyOptions = (ArrayList<HashMap>) response.get("currencyOptions");
        currencyOptions.addAll(selectedCurrencyOptions);
        final String jsonData = new Gson().toJson(new ArrayList<HashMap>(selectedCurrencyOptions));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    public static ArrayList<CurrencyDomain> getSelectedCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        final String GET_ALL_SELECTED_CURRENCIES_URL = CURRENCIES_URL + "?fields=selectedCurrencyOptions" + "&" + Utils.TENANT_IDENTIFIER;
        System.out.println("------------------------ RETRIEVING ALL SELECTED CURRENCIES -------------------------");
        final HashMap response = Utils.performServerGet(requestSpec, responseSpec, GET_ALL_SELECTED_CURRENCIES_URL, "");
        final String jsonData = new Gson().toJson(response.get("selectedCurrencyOptions"));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    public static CurrencyDomain getCurrencybyCode(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String code) {
        ArrayList<CurrencyDomain> currenciesList = getAllCurrencies(requestSpec, responseSpec);
        for (CurrencyDomain e : currenciesList) {
            if (e.getCode().equals(code)) return e;
        }
        return null;
    }

    public static ArrayList<String> updateSelectedCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final ArrayList<String> currencies) {
        final String CURRENCIES_UPDATE_URL = CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER;
        System.out.println("---------------------------------UPDATE SELECTED CURRENCIES LIST---------------------------------------------");
        HashMap hash = Utils.performServerPut(requestSpec, responseSpec, CURRENCIES_UPDATE_URL, currenciesToJSON(currencies), "changes");
        return (ArrayList<String>) hash.get("currencies");
    }

    private static String currenciesToJSON(final ArrayList<String> currencies) {
        HashMap map = new HashMap<>();
        map.put("currencies", currencies);
        System.out.println("map : " + map);
        return new Gson().toJson(map);
    }
}
