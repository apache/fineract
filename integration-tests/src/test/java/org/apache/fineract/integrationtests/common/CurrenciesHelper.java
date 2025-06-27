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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
public final class CurrenciesHelper {

    private CurrenciesHelper() {

    }

    private static final Logger LOG = LoggerFactory.getLogger(CurrenciesHelper.class);
    private static final String CURRENCIES_URL = "/fineract-provider/api/v1/currencies";

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<CurrencyDomain> getAllCurrencies(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        LOG.info("------------------------ RETRIEVING ALL CURRENCIES -------------------------");
        HashMap response = Utils.performServerGet(requestSpec, responseSpec, CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER, "");
        var selectedCurrencyOptions = (ArrayList<HashMap>) response.get("selectedCurrencyOptions");
        var currencyOptions = (ArrayList<HashMap>) response.get("currencyOptions");
        currencyOptions.addAll(selectedCurrencyOptions);
        var jsonData = new Gson().toJson(selectedCurrencyOptions);
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<CurrencyDomain> getSelectedCurrencies(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        LOG.info("------------------------ RETRIEVING ALL SELECTED CURRENCIES -------------------------");
        HashMap response = Utils.performServerGet(requestSpec, responseSpec,
                CURRENCIES_URL + "?fields=selectedCurrencyOptions" + "&" + Utils.TENANT_IDENTIFIER, "");
        var jsonData = new Gson().toJson(response.get("selectedCurrencyOptions"));
        return new Gson().fromJson(jsonData, new TypeToken<ArrayList<CurrencyDomain>>() {}.getType());
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static CurrencyDomain getCurrencybyCode(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String code) {
        var currencies = getAllCurrencies(requestSpec, responseSpec);
        for (var currency : currencies) {
            if (currency.getCode().equals(code)) {
                return currency;
            }
        }
        return null;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static List<String> updateSelectedCurrencies(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final List<String> currencies) {
        LOG.info(
                "---------------------------------UPDATE SELECTED CURRENCIES LIST (deprecated)---------------------------------------------");
        // TODO: this nested "changes" map makes no sense whatsover... in the future just use "currencies" (straight
        // forward, no nesting, no complexity)
        Map changes = Utils.performServerPut(requestSpec, responseSpec, CURRENCIES_URL + "?" + Utils.TENANT_IDENTIFIER,
                currenciesToJSON(currencies), "changes");
        return (List<String>) changes.get("currencies");
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private static String currenciesToJSON(final List<String> currencies) {
        return new Gson().toJson(Map.of("currencies", currencies));
    }
}
