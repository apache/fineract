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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.integrationtests.common.Utils;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

import static com.jayway.restassured.RestAssured.given;

public class CurrencyHelper {

    private static final String CURRENCY_URL = "/fineract-provider/api/v1/currencies?" + Utils.TENANT_IDENTIFIER;
    private static final String CURRENCY_URL_SELECTED = CURRENCY_URL + "&fields=selectedCurrencyOptions";

    private static final List<String> permittedCurrencyArray = Arrays.asList("currencyOptions",
            "selectedCurrencyOptions");

    private static final List<String> permittedCurrencyArraySelected = Arrays.asList("selectedCurrencyOptions");

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public CurrencyHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ArrayList<Currency> getPermittedCurrencies() {
        return getCurrencies(CURRENCY_URL, permittedCurrencyArray);
    }

    public ArrayList<Currency> getSelectedCurrencies() {
        return getCurrencies(CURRENCY_URL_SELECTED, permittedCurrencyArraySelected);
    }


    private ArrayList<Currency> getCurrencies(final String getUrl, final List<String> permittedCurrencyArrays) {
        System.out.println("--------------------------------- GET CURRENCY OPTIONS -------------------------------");
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when()
                .get(getUrl).andReturn().asString();
        final Gson gson = new Gson();
        Assert.notNull(json);
        final ArrayList<Currency> currencyList = new ArrayList<Currency>();
        final Type typeOfHashMap = new TypeToken<Map<String, List<Currency>>>() { }.getType();
        final Map<String, List<Currency>> responseMap = gson.fromJson(json, typeOfHashMap);
        for(Map.Entry<String, List<Currency>> entry : responseMap.entrySet()) {
            Assert.isTrue(permittedCurrencyArrays.contains(entry.getKey()));
            for(Currency currency : entry.getValue()) {
                currencyList.add(currency);
            }
        }
        return currencyList;
    }

    public List<String> updateCurrencies(final List<String> currencies) {
        System.out.println("--------------------------------- UPDATE CURRENCY OPTIONS -------------------------------");
        final String json = given().spec(requestSpec).body(getUpdateJSON(currencies)).expect().spec(responseSpec).log().ifError().when()
                .put(CURRENCY_URL).andReturn().asString();
        final Gson gson = new Gson();
        Assert.notNull(json);
        final Type typeOfHashMap = new TypeToken<Map<String,Map<String, List<String>>>>() { }.getType();
        final Map<String,Map<String, List<String>>> responseMap = gson.fromJson(json, typeOfHashMap);
        return responseMap.get("changes").get("currencies");
    }

    private String getUpdateJSON(final List<String> currencies) {
        final HashMap<String, List<String>> map = new HashMap<>();
        map.put("currencies", currencies);
        return new Gson().toJson(map);
    }
}
