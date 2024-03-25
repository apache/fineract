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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.models.PostDelinquencyBucketRequest;
import org.apache.fineract.client.models.PostDelinquencyRangeRequest;
import org.apache.fineract.client.services.DelinquencyRangeAndBucketsManagementApi;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DelinquencyGlobalInitializerStep implements FineractGlobalInitializerStep {

    public static final String DEFAULT_LOCALE = "en";
    public static final List<Integer> DEFAULT_DELINQUENCY_RANGES = Arrays.asList(1, 3, 30, 60, 90, 120, 150, 180, 240);
    public static final String DEFAULT_DELINQUENCY_BUCKET_NAME = "Default delinquency bucket";

    private final DelinquencyRangeAndBucketsManagementApi delinquencyApi;

    @Override
    public void initialize() throws Exception {
        setDefaultDelinquencyRanges();
        setDefaultDelinquencyBucket();
    }

    public void setDefaultDelinquencyRanges() throws IOException {
        for (int i = 0; i < DEFAULT_DELINQUENCY_RANGES.size() - 1; i++) {
            PostDelinquencyRangeRequest postDelinquencyRangeRequest = new PostDelinquencyRangeRequest();
            postDelinquencyRangeRequest.classification("Delinquency range " + DEFAULT_DELINQUENCY_RANGES.get(i).toString());
            postDelinquencyRangeRequest.locale(DEFAULT_LOCALE);
            if (DEFAULT_DELINQUENCY_RANGES.get(i) == 1) {
                postDelinquencyRangeRequest.minimumAgeDays(1);
                postDelinquencyRangeRequest.maximumAgeDays(3);
            } else {
                postDelinquencyRangeRequest.minimumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(i) + 1);
                postDelinquencyRangeRequest.maximumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(i + 1));
            }

            delinquencyApi.createDelinquencyRange(postDelinquencyRangeRequest).execute();
        }

        PostDelinquencyRangeRequest lastRange = new PostDelinquencyRangeRequest();
        lastRange.classification("Delinquency range " + DEFAULT_DELINQUENCY_RANGES.get(DEFAULT_DELINQUENCY_RANGES.size() - 1).toString());
        lastRange.locale(DEFAULT_LOCALE);
        lastRange.minimumAgeDays(DEFAULT_DELINQUENCY_RANGES.get(DEFAULT_DELINQUENCY_RANGES.size() - 1) + 1);
        lastRange.maximumAgeDays(null);

        delinquencyApi.createDelinquencyRange(lastRange).execute();
    }

    public void setDefaultDelinquencyBucket() throws IOException {
        List<Long> rangesNr = new ArrayList<>();

        for (int i = 1; i < DEFAULT_DELINQUENCY_RANGES.size() + 1; i++) {
            rangesNr.add((long) DEFAULT_DELINQUENCY_RANGES.indexOf(DEFAULT_DELINQUENCY_RANGES.get(i - 1)));
        }
        rangesNr.add((long) DEFAULT_DELINQUENCY_RANGES.size());

        PostDelinquencyBucketRequest postDelinquencyBucketRequest = new PostDelinquencyBucketRequest();
        postDelinquencyBucketRequest.name(DEFAULT_DELINQUENCY_BUCKET_NAME);
        postDelinquencyBucketRequest.ranges(rangesNr);

        delinquencyApi.createDelinquencyBucket(postDelinquencyBucketRequest).execute();
    }
}
