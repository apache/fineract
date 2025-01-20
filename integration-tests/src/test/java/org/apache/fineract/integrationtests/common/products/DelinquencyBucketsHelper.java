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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencySummary;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;

@Slf4j
public class DelinquencyBucketsHelper {

    private static final String DELINQUENCY_BUCKETS_URL = "/fineract-provider/api/v1/delinquency/buckets";
    private static final Gson GSON = new JSON().getGson();

    protected DelinquencyBucketsHelper() {}

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static ArrayList<GetDelinquencyBucketsResponse> getDelinquencyBuckets(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String response = Utils.performServerGet(requestSpec, responseSpec, DELINQUENCY_BUCKETS_URL + "?" + Utils.TENANT_IDENTIFIER);

        Type delinquencyBucketListType = new TypeToken<ArrayList<GetDelinquencyBucketsResponse>>() {}.getType();
        return GSON.fromJson(response, delinquencyBucketListType);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static GetDelinquencyBucketsResponse getDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        String response = Utils.performServerGet(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER);
        return GSON.fromJson(response, GetDelinquencyBucketsResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PostDelinquencyBucketResponse createDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        log.info("JSON: {}", json);
        final String response = Utils.performServerPost(requestSpec, responseSpec, DELINQUENCY_BUCKETS_URL + "?" + Utils.TENANT_IDENTIFIER,
                json, null);
        return GSON.fromJson(response, PostDelinquencyBucketResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static PutDelinquencyBucketResponse updateDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId, final String json) {
        log.info("JSON: {}", json);
        final String response = Utils.performServerPut(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, json, null);
        return GSON.fromJson(response, PutDelinquencyBucketResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static DeleteDelinquencyBucketResponse deleteDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        final String response = Utils.performServerDelete(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, Utils.emptyJson(), null);
        return GSON.fromJson(response, DeleteDelinquencyBucketResponse.class);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static String getAsJSON(final List<Integer> rangeIds) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", Utils.uniqueRandomStringGenerator("Delinquency_Bucket_", 4));
        map.put("ranges", rangeIds.toArray());
        return new Gson().toJson(map);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createDelinquencyBucket(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            List<Pair<Integer, Integer>> ranges) {
        List<Integer> rangeIds = ranges.stream().map(r -> createDelinquencyRange(requestSpec, responseSpec, r)).toList();
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        assertNotNull(delinquencyBucketResponse);
        return delinquencyBucketResponse.getResourceId();
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createDelinquencyRange(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            Pair<Integer, Integer> range) {
        String jsonRange = DelinquencyRangesHelper.getAsJSON(range.getLeft(), range.getRight());
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        return delinquencyRangeResponse.getResourceId();
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createDelinquencyBucket(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        ArrayList<Integer> rangeIds = new ArrayList<>();

        // First Range
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 60);
        GetDelinquencyRangesResponse range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec,
                delinquencyRangeResponse.getResourceId());

        // Second Range
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec, delinquencyRangeResponse.getResourceId());

        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        assertNotNull(delinquencyBucketResponse);

        return delinquencyBucketResponse.getResourceId();
    }

    public static void evaluateLoanCollectionData(GetLoansLoanIdResponse getLoansLoanIdResponse, Integer pastDueDays,
            Double amountExpected) {
        GetLoansLoanIdDelinquencySummary getCollectionData = getLoansLoanIdResponse.getDelinquent();
        if (getCollectionData != null) {
            log.info("Loan Delinquency Data in Days {} and Amount {}", getCollectionData.getPastDueDays(),
                    getCollectionData.getDelinquentAmount());
            assertEquals(pastDueDays, getCollectionData.getPastDueDays(), "Past due days");
            assertEquals(amountExpected, getCollectionData.getDelinquentAmount(), "Amount expected");
        } else {
            log.info("Loan Delinquency Data is null");
        }

        GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        if (delinquencyRange != null) {
            log.info("Loan Delinquency Classification is {} : ({} - {}) {}", delinquencyRange.getClassification(),
                    delinquencyRange.getMinimumAgeDays(), delinquencyRange.getMaximumAgeDays(), pastDueDays);
            assertTrue(delinquencyRange.getMinimumAgeDays() <= pastDueDays, "Min Age Days");
            assertTrue(delinquencyRange.getMaximumAgeDays() >= pastDueDays, "Max Age Days");
        } else {
            log.info("Loan Delinquency Classification is null");
        }
    }

}
