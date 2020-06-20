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

import static io.restassured.RestAssured.given;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class CurrencyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CurrencyHelper.class);
    private static final String CURRENCY_URL = "/fineract-provider/api/v1/currencies?" + Utils.TENANT_IDENTIFIER;
    private static final String CURRENCY_URL_SELECTED = CURRENCY_URL + "&fields=selectedCurrencyOptions";

    private static final List<String> PERMITTED_CURRENCY_ARRAY = Arrays.asList("currencyOptions", "selectedCurrencyOptions");

    private static final List<String> PERMITTED_CURRENCY_ARRAY_SELECTED = Arrays.asList("selectedCurrencyOptions");

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public CurrencyHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public ArrayList<Currency> getPermittedCurrencies() {
        return getCurrencies(CURRENCY_URL, PERMITTED_CURRENCY_ARRAY);
    }

    public ArrayList<Currency> getSelectedCurrencies() {
        return getCurrencies(CURRENCY_URL_SELECTED, PERMITTED_CURRENCY_ARRAY_SELECTED);
    }

    private ArrayList<Currency> getCurrencies(final String getUrl, final List<String> permittedCurrencyArrays) {
        LOG.info("--------------------------------- GET CURRENCY OPTIONS -------------------------------");
        final String json = given().spec(requestSpec).expect().spec(responseSpec).log().ifError().when().get(getUrl).andReturn().asString();
        final Gson gson = new Gson();
        Assert.notNull(json, "json");
        final ArrayList<Currency> currencyList = new ArrayList<Currency>();
        final Type typeOfHashMap = new TypeToken<Map<String, List<Currency>>>() {}.getType();
        final Map<String, List<Currency>> responseMap = gson.fromJson(json, typeOfHashMap);
        for (Map.Entry<String, List<Currency>> entry : responseMap.entrySet()) {
            Assert.isTrue(permittedCurrencyArrays.contains(entry.getKey()), "permittedCurrencyArrays");
            for (Currency currency : entry.getValue()) {
                currencyList.add(currency);
            }
        }
        return currencyList;
    }

    public List<String> updateCurrencies(final List<String> currencies) {
        LOG.info("--------------------------------- UPDATE CURRENCY OPTIONS -------------------------------");
        final String json = given().spec(requestSpec).body(getUpdateJSON(currencies)).expect().spec(responseSpec).log().ifError().when()
                .put(CURRENCY_URL).andReturn().asString();
        final Gson gson = new Gson();
        Assert.notNull(json, "json");
        final Type typeOfHashMap = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
        final Map<String, Map<String, List<String>>> responseMap = gson.fromJson(json, typeOfHashMap);
        return responseMap.get("changes").get("currencies");
    }

    private String getUpdateJSON(final List<String> currencies) {
        final HashMap<String, List<String>> map = new HashMap<>();
        map.put("currencies", currencies);
        return new Gson().toJson(map);
    }
}
