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
package org.apache.fineract.integrationtests.teller;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.organisation.teller.domain.TellerStatus;

public final class TellerHelper {

    private static final Gson GSON = new JSON().getGson();

    private static final String TELLER_API_URL_START = "/fineract-provider/api/v1/tellers/";

    private TellerHelper() {}

    public static Integer createTeller(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        final String url = TELLER_API_URL_START + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, createTellerAsJSON(), "resourceId");
    }

    public static Object createCashier(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer tellerId, final Integer staffId) {
        final String url = TELLER_API_URL_START + tellerId + "/cashiers?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, createCashierAsJSON(staffId));
    }

    public static Object allocateCashToCashier(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer tellerId, final Long cashierId, final BigDecimal txnAmount) {
        final String url = TELLER_API_URL_START + tellerId + "/cashiers/" + cashierId + "/allocate?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, allocateCashAsJSON(txnAmount));
    }

    public static GetTellersTellerIdCashiersResponse getCashiers(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer tellerId) {
        final String url = TELLER_API_URL_START + tellerId + "/cashiers?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        return GSON.fromJson(response, GetTellersTellerIdCashiersResponse.class);
    }

    public static GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse getCashierSummaryAndTransactions(
            final RequestSpecification requestSpec, final ResponseSpecification responseSpec, final Integer tellerId,
            final Long cashierId) {
        final String url = TELLER_API_URL_START + tellerId + "/cashiers/" + cashierId + "/summaryandtransactions?currencyCode=USD&"
                + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, url, null);
        return GSON.fromJson(response, GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse.class);
    }

    public static String allocateCashAsJSON(final BigDecimal txnAmount) {

        final Map<String, Object> map = getMapWithDate();

        map.put("txnDate", "1 October 2024");
        map.put("currencyCode", "USD");
        map.put("txnAmount", txnAmount);
        map.put("txnNote", "allocate cash");

        return GSON.toJson(map);
    }

    public static String createCashierAsJSON(final Integer staffId) {

        final Map<String, Object> map = getMapWithDate();

        map.put("staffId", staffId);
        map.put("description", "");
        map.put("isFullDay", true);
        map.put("startDate", "1 October 2024");
        map.put("endDate", "1 October 2025");

        return GSON.toJson(map);
    }

    public static String createTellerAsJSON() {

        final Map<String, Object> map = getMapWithDate();

        map.put("officeId", 1);
        map.put("name", Utils.uniqueRandomStringGenerator("john_", 5));
        map.put("status", TellerStatus.ACTIVE.getValue());
        map.put("description", "");
        map.put("startDate", "1 October 2020");
        map.put("endDate", "");

        return GSON.toJson(map);
    }

    public static Map<String, Object> getMapWithDate() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");

        return map;
    }
}
