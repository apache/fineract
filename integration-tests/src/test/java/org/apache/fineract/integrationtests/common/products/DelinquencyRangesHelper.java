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
package org.apache.fineract.integrationtests.common.products;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelinquencyRangesHelper {

    private static final String DELINQUENCY_RANGES_URL = "/fineract-provider/api/v1/delinquency/ranges";
    private static final Gson GSON = new JSON().getGson();

    private static final Logger LOG = LoggerFactory.getLogger(DelinquencyRangesHelper.class);

    protected DelinquencyRangesHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<GetDelinquencyRangesResponse> getDelinquencyRanges(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String response = Utils.performServerGet(requestSpec, responseSpec, DELINQUENCY_RANGES_URL + "?" + Utils.TENANT_IDENTIFIER);

        Type delinquencyRangeListType = new TypeToken<ArrayList<GetDelinquencyRangesResponse>>() {}.getType();
        return GSON.fromJson(response, delinquencyRangeListType);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetDelinquencyRangesResponse getDelinquencyRange(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        String response = Utils.performServerGet(requestSpec, responseSpec,
                DELINQUENCY_RANGES_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER);
        LOG.info("----- {}", response);
        return GSON.fromJson(response, GetDelinquencyRangesResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PostDelinquencyRangeResponse createDelinquencyRange(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        final String response = Utils.performServerPost(requestSpec, responseSpec, DELINQUENCY_RANGES_URL + "?" + Utils.TENANT_IDENTIFIER,
                json, null);
        LOG.info("----- {}", response);
        return GSON.fromJson(response, PostDelinquencyRangeResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PutDelinquencyRangeResponse updateDelinquencyRange(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId, final String json) {
        final String response = Utils.performServerPut(requestSpec, responseSpec,
                DELINQUENCY_RANGES_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, json, null);
        LOG.info("----- {}", response);
        return GSON.fromJson(response, PutDelinquencyRangeResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static DeleteDelinquencyRangeResponse deleteDelinquencyRange(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        final String response = Utils.performServerDelete(requestSpec, responseSpec,
                DELINQUENCY_RANGES_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, Utils.emptyJson(), null);
        LOG.info("----- {}", response);
        return GSON.fromJson(response, DeleteDelinquencyRangeResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getAsJSON(Integer minimumAgeDays, Integer maximumAgeDays) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("classification", Utils.uniqueRandomStringGenerator("Delinquency__" + minimumAgeDays + "_" + maximumAgeDays + "__", 4));
        map.put("minimumAgeDays", minimumAgeDays);
        map.put("maximumAgeDays", maximumAgeDays);
        map.put("locale", "en");
        return new Gson().toJson(map);
    }

}
