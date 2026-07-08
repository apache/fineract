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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DeleteDelinquencyBucketResponse;
import org.apache.fineract.client.models.DeleteDelinquencyRangeResponse;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.DelinquencyRangeResponse;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PutDelinquencyBucketResponse;
import org.apache.fineract.client.models.PutDelinquencyRangeResponse;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;

public class FeignDelinquencyHelper {

    private final FineractFeignClient fineractClient;

    public FeignDelinquencyHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    public PostDelinquencyRangeResponse createRange(DelinquencyRangeRequest request) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().createRange(request));
    }

    public DelinquencyRangeResponse getRange(Long rangeId) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().getRange(rangeId));
    }

    public List<DelinquencyRangeResponse> getRanges() {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().getRanges());
    }

    public PutDelinquencyRangeResponse updateRange(Long rangeId, DelinquencyRangeRequest request) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().updateRange(rangeId, request));
    }

    public DeleteDelinquencyRangeResponse deleteRange(Long rangeId) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().deleteRange(rangeId));
    }

    public PostDelinquencyBucketResponse createBucket(DelinquencyBucketRequest request) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().createBucket(request));
    }

    public Long createBucket(List<Pair<Integer, Integer>> rangesDef) {
        List<Long> rangeIds = new ArrayList<>();
        rangesDef.forEach(
                range -> rangeIds.add(createRange(new DelinquencyRangeRequest().classification(Utils.randomStringGenerator("DLQ_R_", 10))
                        .minimumAgeDays(range.getLeft()).maximumAgeDays(range.getRight()).locale(LoanTestData.LOCALE)).getResourceId()));
        return createBucket(new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(rangeIds))
                .getResourceId();
    }

    public Long createDefaultBucket() {
        Long range1Id = createRange(new DelinquencyRangeRequest().classification(Utils.randomStringGenerator("DLQ_R_", 10))
                .minimumAgeDays(1).maximumAgeDays(3).locale(LoanTestData.LOCALE)).getResourceId();
        Long range2Id = createRange(new DelinquencyRangeRequest().classification(Utils.randomStringGenerator("DLQ_R_", 10))
                .minimumAgeDays(4).maximumAgeDays(60).locale(LoanTestData.LOCALE)).getResourceId();
        return createBucket(
                new DelinquencyBucketRequest().name(Utils.randomStringGenerator("DLQ_B_", 10)).ranges(List.of(range1Id, range2Id)))
                .getResourceId();
    }

    public DelinquencyBucketResponse getBucket(Long bucketId) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().getBucket(bucketId));
    }

    public List<DelinquencyBucketResponse> getBuckets() {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().getBuckets());
    }

    public PutDelinquencyBucketResponse updateBucket(Long bucketId, DelinquencyBucketRequest request) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().updateBucket(bucketId, request));
    }

    public DeleteDelinquencyBucketResponse deleteBucket(Long bucketId) {
        return ok(() -> fineractClient.delinquencyRangeAndBucketsManagement().deleteBucket(bucketId));
    }
}
