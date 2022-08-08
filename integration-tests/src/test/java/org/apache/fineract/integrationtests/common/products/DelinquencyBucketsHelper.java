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

import com.google.gson.Gson;
import com.linecorp.armeria.internal.shaded.guava.reflect.TypeToken;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelinquencyBucketsHelper {

    private static final String DELINQUENCY_BUCKETS_URL = "/fineract-provider/api/v1/delinquency/buckets";
    private static final Gson GSON = new JSON().getGson();

    private static final Logger LOG = LoggerFactory.getLogger(DelinquencyBucketsHelper.class);

    protected DelinquencyBucketsHelper() {}

    public static ArrayList<GetDelinquencyBucketsResponse> getDelinquencyBuckets(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String response = Utils.performServerGet(requestSpec, responseSpec, DELINQUENCY_BUCKETS_URL + "?" + Utils.TENANT_IDENTIFIER);

        Type delinquencyBucketListType = new TypeToken<ArrayList<GetDelinquencyBucketsResponse>>() {}.getType();
        return GSON.fromJson(response, delinquencyBucketListType);
    }

    public static GetDelinquencyBucketsResponse getDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        String response = Utils.performServerGet(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER);
        return GSON.fromJson(response, GetDelinquencyBucketsResponse.class);
    }

    public static PostDelinquencyBucketResponse createDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final String json) {
        LOG.info("JSON: {}", json);
        final String response = Utils.performServerPost(requestSpec, responseSpec, DELINQUENCY_BUCKETS_URL + "?" + Utils.TENANT_IDENTIFIER,
                json, null);
        return GSON.fromJson(response, PostDelinquencyBucketResponse.class);
    }

    public static PutDelinquencyBucketResponse updateDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId, final String json) {
        LOG.info("JSON: {}", json);
        final String response = Utils.performServerPut(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, json, null);
        return GSON.fromJson(response, PutDelinquencyBucketResponse.class);
    }

    public static DeleteDelinquencyBucketResponse deleteDelinquencyBucket(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer resourceId) {
        final String response = Utils.performServerDelete(requestSpec, responseSpec,
                DELINQUENCY_BUCKETS_URL + "/" + resourceId + "?" + Utils.TENANT_IDENTIFIER, Utils.emptyJson(), null);
        return GSON.fromJson(response, DeleteDelinquencyBucketResponse.class);
    }

    public static String getAsJSON(final ArrayList<Integer> rangeIds) {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", Utils.randomNameGenerator("Delinquency_Bucket_", 4));
        map.put("ranges", rangeIds.toArray());
        return new Gson().toJson(map);
    }

}
