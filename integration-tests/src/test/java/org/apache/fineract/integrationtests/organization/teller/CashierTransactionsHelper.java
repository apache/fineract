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
package org.apache.fineract.integrationtests.organization.teller;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse;
import org.apache.fineract.client.models.GetTellersTellerIdCashiersCashiersIdTransactionsResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashierTransactionsHelper extends IntegrationTest {

    private final ResponseSpecification responseSpecification;
    private final RequestSpecification requestSpecification;

    private static final String CREATE_CASHIER_URL = "/fineract-provider/api/v1/tellers/1/cashiers";
    private static final String CREATE_TELLER_URL = "/fineract-provider/api/v1/tellers";
    private static final Logger LOG = LoggerFactory.getLogger(CashierTransactionsHelper.class);

    public CashierTransactionsHelper(final RequestSpecification requestSpecification, final ResponseSpecification responseSpecification) {
        this.requestSpecification = requestSpecification;
        this.responseSpecification = responseSpecification;
    }

    public GetTellersTellerIdCashiersCashiersIdTransactionsResponse getTellersTellerIdCashiersCashiersIdTransactionsResponse(Long tellerId,
            Long cashierId, String currencyCode, int offset, int limit, String orderBy, String sortOrder) {
        return ok(fineract().tellers.getTransactionsForCashier(tellerId, cashierId, currencyCode, offset, limit, orderBy, sortOrder));
    }

    public GetTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse getTellersTellerIdCashiersCashiersIdSummaryAndTransactionsResponse(
            Long tellerId, Long cashierId, String currencyCode, int offset, int limit, String orderBy, String sortOrder) {
        return ok(fineract().tellers.getTransactionsWithSummaryForCashier(tellerId, cashierId, currencyCode, offset, limit, orderBy,
                sortOrder));
    }

    public static Integer createTeller(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return (Integer) createTellerWithJson(requestSpec, responseSpec, createTellerAsJSON()).get("resourceId");
    }

    public static Map<String, Object> createTellerWithJson(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String json) {

        final String url = CREATE_TELLER_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, json, "");
    }

    public static String createTellerAsJSON() {

        final Map<String, Object> map = getMapWithStartDate();

        map.put("officeId", 1);
        map.put("name", Utils.uniqueRandomStringGenerator("Teller 1", 5));
        map.put("description", Utils.uniqueRandomStringGenerator("Teller For Testing", 4));
        map.put("status", 300);

        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static Map<String, Object> getMapWithStartDate() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("startDate", "20 September 2011");

        return map;
    }

    public static Integer createCashier(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        return (Integer) createCashierWithJson(requestSpec, responseSpec, createCashierAsJSON()).get("resourceId");
    }

    public static Map<String, Object> createCashierWithJson(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        final String url = CREATE_CASHIER_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, url, json, "");
    }

    public static String createCashierAsJSON() {

        final Map<String, Object> map = getMapWithDates();

        map.put("staffId", 1);
        map.put("description", Utils.uniqueRandomStringGenerator("test__", 4));
        LOG.info("map :  {}", map);
        return new Gson().toJson(map);
    }

    public static Map<String, Object> getMapWithDates() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("locale", "en");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("startDate", "01 January 2023");
        map.put("endDate", "31 December 2023");
        map.put("isFullDay", true);

        return map;
    }

}
