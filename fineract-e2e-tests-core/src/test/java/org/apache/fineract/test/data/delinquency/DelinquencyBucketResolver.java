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
package org.apache.fineract.test.data.delinquency;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.test.data.DelinquencyBucket;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DelinquencyBucketResolver {

    private final FineractFeignClient fineractClient;

    // Injected lazily so resolveBucketId() can call resolve() through the cache-backed Spring proxy instead of
    // "this" - a direct self-invocation would bypass the @Cacheable interceptor and re-fetch all buckets every time.
    private final DelinquencyBucketResolver self;

    public DelinquencyBucketResolver(FineractFeignClient fineractClient, @Lazy DelinquencyBucketResolver self) {
        this.fineractClient = fineractClient;
        this.self = self;
    }

    @Cacheable(key = "#delinquencyBucket.name()", value = "delinquencyBucketsByName")
    public long resolve(DelinquencyBucket delinquencyBucket) {
        String delinquencyBucketName = delinquencyBucket.name();
        log.debug("Resolving account type by name [{}]", delinquencyBucketName);
        List<DelinquencyBucketResponse> delinquencyBucketResponses = ok(
                () -> fineractClient.delinquencyRangeAndBucketsManagement().getBuckets());
        DelinquencyBucketResponse foundAtr = delinquencyBucketResponses.stream()//
                .filter(atr -> delinquencyBucketName.equals(atr.getName()))//
                .findAny()//
                .orElseThrow(() -> new IllegalArgumentException("Delinquency bucket [%s] not found".formatted(delinquencyBucketName)));//

        return foundAtr.getId();
    }

    public Long resolveBucketId(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return self.resolve(DelinquencyBucket.valueOf(value));
        }
    }
}
