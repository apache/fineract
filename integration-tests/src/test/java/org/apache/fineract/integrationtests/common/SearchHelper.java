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
import java.lang.reflect.Type;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetSearchResponse;
import org.apache.fineract.client.util.JSON;

@Slf4j
public final class SearchHelper {

    private static final Gson GSON = new JSON().getGson();

    private SearchHelper() {

    }

    private static final String SEARCH_URL = "/fineract-provider/api/v1/search?" + Utils.TENANT_IDENTIFIER;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<GetSearchResponse> getSearch(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String query, final Boolean exactMatch, final String resources) {
        final String urlSearch = SEARCH_URL + "&exactMatch=" + exactMatch.toString() + "&query=" + query + "&resource=" + resources;
        log.info("URL to search: {}", urlSearch);
        final String response = Utils.performServerGet(requestSpec, responseSpec, urlSearch);
        log.info("Result: {}", response);
        Type searchResourcesListType = new TypeToken<ArrayList<GetSearchResponse>>() {}.getType();
        return GSON.fromJson(response, searchResourcesListType);
    }

}
