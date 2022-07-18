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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DelinquencyBucketsIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void testCreateDelinquencyRanges() {
        // given
        final String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);

        // when
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = DelinquencyRangesHelper.createDelinquencyRange(requestSpec,
                responseSpec, jsonRange);
        final ArrayList<GetDelinquencyRangesResponse> ranges = DelinquencyRangesHelper.getDelinquencyRanges(requestSpec, responseSpec);

        // then
        assertNotNull(delinquencyRangeResponse01);
        assertNotNull(ranges);
        assertEquals(1, ranges.get(0).getMinimumAgeDays(), "Expected Min Age Days to 1");
        assertEquals(3, ranges.get(0).getMaximumAgeDays(), "Expected Max Age Days to 3");
    }

    @Test
    public void testUpdateDelinquencyRanges() {
        // given
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        final PostDelinquencyRangeResponse delinquencyRangeResponse01 = DelinquencyRangesHelper.createDelinquencyRange(requestSpec,
                responseSpec, jsonRange);
        jsonRange = DelinquencyRangesHelper.getAsJSON(1, 7);
        assertNotNull(delinquencyRangeResponse01);

        // when
        final PutDelinquencyRangeResponse delinquencyRangeResponse02 = DelinquencyRangesHelper.updateDelinquencyRange(requestSpec,
                responseSpec, delinquencyRangeResponse01.getResourceId(), jsonRange);
        final GetDelinquencyRangesResponse range = DelinquencyRangesHelper.getDelinquencyRange(requestSpec, responseSpec,
                delinquencyRangeResponse01.getResourceId());
        final DeleteDelinquencyRangeResponse deleteDelinquencyRangeResponse = DelinquencyRangesHelper.deleteDelinquencyRange(requestSpec,
                responseSpec, delinquencyRangeResponse01.getResourceId());

        // then
        assertNotNull(delinquencyRangeResponse02);
        assertNotNull(deleteDelinquencyRangeResponse);
        assertNotNull(range);
        assertNotEquals(3, range.getMaximumAgeDays());
        assertEquals(1, range.getMinimumAgeDays());
        assertEquals(7, range.getMaximumAgeDays());
    }

    @Test
    public void testDelinquencyBuckets() {
        // given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        // Update
        jsonRange = DelinquencyRangesHelper.getAsJSON(31, 60);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PutDelinquencyBucketResponse updateDelinquencyBucketResponse = DelinquencyBucketsHelper.updateDelinquencyBucket(requestSpec,
                responseSpec, delinquencyBucketResponse.getResourceId(), jsonBucket);
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        // Read
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketResponse.getResourceId());

        // when
        final ArrayList<GetDelinquencyBucketsResponse> bucketList = DelinquencyBucketsHelper.getDelinquencyBuckets(requestSpec,
                responseSpec);

        // then
        assertNotNull(bucketList);
        assertNotNull(delinquencyBucket);
        assertEquals(2, delinquencyBucket.getRanges().size());
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(updateDelinquencyBucketResponse);
    }

    @Test
    public void testDelinquencyBucketDelete() {
        // given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        PostDelinquencyBucketResponse delinquencyBucketResponse = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec,
                responseSpec, jsonBucket);
        // Delete
        DeleteDelinquencyBucketResponse deleteDelinquencyBucketResponse = DelinquencyBucketsHelper.deleteDelinquencyBucket(requestSpec,
                responseSpec, delinquencyBucketResponse.getResourceId());

        // when
        final ArrayList<GetDelinquencyBucketsResponse> bucketList = DelinquencyBucketsHelper.getDelinquencyBuckets(requestSpec,
                responseSpec);

        // then
        assertNotNull(bucketList);
        assertNotNull(delinquencyBucketResponse);
        assertNotNull(deleteDelinquencyBucketResponse);
    }

    @Test
    public void testDelinquencyBucketsRangeAgeOverlaped() {
        // Given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(3, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        final ResponseSpecification response403Spec = new ResponseSpecBuilder().expectStatusCode(403).build();

        // When
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, response403Spec, jsonBucket);
    }

    @Test
    public void testDelinquencyBucketsNameDuplication() {
        // Given
        ArrayList<Integer> rangeIds = new ArrayList<>();
        String jsonRange = DelinquencyRangesHelper.getAsJSON(1, 3);
        PostDelinquencyRangeResponse delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec,
                jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        jsonRange = DelinquencyRangesHelper.getAsJSON(4, 30);
        // Create
        delinquencyRangeResponse = DelinquencyRangesHelper.createDelinquencyRange(requestSpec, responseSpec, jsonRange);
        rangeIds.add(delinquencyRangeResponse.getResourceId());
        String jsonBucket = DelinquencyBucketsHelper.getAsJSON(rangeIds);
        final ResponseSpecification response403Spec = new ResponseSpecBuilder().expectStatusCode(403).build();

        // When
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec, jsonBucket);

        // Then
        DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, response403Spec, jsonBucket);
    }

}
