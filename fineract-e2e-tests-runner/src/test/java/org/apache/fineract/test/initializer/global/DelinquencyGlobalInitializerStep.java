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
package org.apache.fineract.test.initializer.global;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DelinquencyBucketData;
import org.apache.fineract.client.models.DelinquencyBucketRequest;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DelinquencyGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String DEFAULT_LOCALE = "en";
    public static final List<Integer> DEFAULT_DELINQUENCY_RANGES = Arrays.asList(1, 3, 30, 60, 90, 120, 150, 180, 240);
    public static final String DEFAULT_DELINQUENCY_BUCKET_NAME = "Default delinquency bucket";

    private final FineractFeignClient fineractClient;

    private final List<Long> createdRangeIds = new ArrayList<>();

    @Override
    public void initialize() {
        setDefaultDelinquencyRanges();
        setDefaultDelinquencyBucket();
    }

    public void setDefaultDelinquencyRanges() {
        List<DelinquencyRangeData> existingRanges;
        try {
            existingRanges = fineractClient.delinquencyRangeAndBucketsManagement().getDelinquencyRanges(Map.of());
        } catch (Exception e) {
            log.debug("Could not retrieve existing delinquency ranges, will create them", e);
            existingRanges = new ArrayList<>();
        }

        for (int i = 0; i < DEFAULT_DELINQUENCY_RANGES.size() - 1; i++) {
            String classification = "Delinquency range " + DEFAULT_DELINQUENCY_RANGES.get(i).toString();

            DelinquencyRangeData existingRange = existingRanges.stream().filter(r -> classification.equals(r.getClassification()))
                    .findFirst().orElse(null);

            if (existingRange != null) {
                createdRangeIds.add(existingRange.getId());
                continue;
            }

            DelinquencyRangeRequest postDelinquencyRangeRequest = new DelinquencyRangeRequest();
            postDelinquencyRangeRequest.classification(classification);
            postDelinquencyRangeRequest.locale(DEFAULT_LOCALE);
            if (DEFAULT_DELINQUENCY_RANGES.get(i) == 1) {
                postDelinquencyRangeRequest.minimumAgeDays(1);
                postDelinquencyRangeRequest.maximumAgeDays(3);
            } else {
                postDelinquencyRangeRequest.minimumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(i) + 1);
                postDelinquencyRangeRequest.maximumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(i + 1));
            }

            PostDelinquencyRangeResponse response = ok(() -> fineractClient.delinquencyRangeAndBucketsManagement()
                    .createDelinquencyRange(postDelinquencyRangeRequest, Map.of()));
            createdRangeIds.add(response.getResourceId());
        }

        String lastClassification = "Delinquency range " + DEFAULT_DELINQUENCY_RANGES.get(DEFAULT_DELINQUENCY_RANGES.size() - 1).toString();
        DelinquencyRangeData existingLastRange = existingRanges.stream().filter(r -> lastClassification.equals(r.getClassification()))
                .findFirst().orElse(null);

        if (existingLastRange != null) {
            createdRangeIds.add(existingLastRange.getId());
            return;
        }

        DelinquencyRangeRequest lastRange = new DelinquencyRangeRequest();
        lastRange.classification(lastClassification);
        lastRange.locale(DEFAULT_LOCALE);
        lastRange.minimumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(DEFAULT_DELINQUENCY_RANGES.size() - 1) + 1);
        lastRange.maximumAgeDays(null);

        PostDelinquencyRangeResponse lastResponse = ok(
                () -> fineractClient.delinquencyRangeAndBucketsManagement().createDelinquencyRange(lastRange, Map.of()));
        createdRangeIds.add(lastResponse.getResourceId());
    }

    public void setDefaultDelinquencyBucket() {
        try {
            List<DelinquencyBucketData> existingBuckets = fineractClient.delinquencyRangeAndBucketsManagement()
                    .getDelinquencyBuckets(Map.of());
            boolean bucketExists = existingBuckets.stream().anyMatch(b -> DEFAULT_DELINQUENCY_BUCKET_NAME.equals(b.getName()));

            if (bucketExists) {
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve existing delinquency buckets, will create default bucket", e);
        }

        DelinquencyBucketRequest postDelinquencyBucketRequest = new DelinquencyBucketRequest();
        postDelinquencyBucketRequest.name(DEFAULT_DELINQUENCY_BUCKET_NAME);
        postDelinquencyBucketRequest.ranges(createdRangeIds);

        executeVoid(() -> fineractClient.delinquencyRangeAndBucketsManagement().createDelinquencyBucket(postDelinquencyBucketRequest,
                Map.of()));
    }
}
