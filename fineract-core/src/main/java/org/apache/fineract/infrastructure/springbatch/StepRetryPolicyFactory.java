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
package org.apache.fineract.infrastructure.springbatch;

import java.time.Duration;
import org.springframework.core.retry.RetryPolicy;

/**
 * Builds the retry policy of a fault-tolerant chunk-oriented step out of a configured retry limit, preserving the
 * meaning that limit had under Spring Batch 5.
 * <p>
 * {@code ChunkOrientedStepBuilder.retryLimit(n)} is deliberately not used for this. It forwards to
 * {@code RetryPolicy.Builder#maxRetries(long)}, which differs from Batch 5's {@code SimpleRetryPolicy(n)} twice over:
 * <ul>
 * <li>{@code maxRetries} counts retries <em>after</em> the initial attempt ({@code total attempts = 1 + maxRetries}),
 * whereas {@code SimpleRetryPolicy(n)} meant {@code n} attempts in total - so routing the configured value straight
 * through would grant every item one extra attempt.</li>
 * <li>{@code RetryPolicy.Builder#build()} installs a default back-off when none is given: an {@code ExponentialBackOff}
 * with a 1000 ms initial interval and a multiplier of 1, i.e. a one second pause before every retry. Batch 5's
 * {@code RetryTemplate} defaulted to {@code NoBackOffPolicy} and retried immediately, so the default would silently add
 * up to {@code (limit - 1) * 1s} of sleeping to each failing item - on LOAN_COB, up to {@code skipLimit} times per
 * partition.</li>
 * </ul>
 * Both are corrected here, so {@code fineract.partitioned-job.partitioned-job-properties[*].retry-limit} keeps meaning
 * "maximum number of attempts per item" and retries stay immediate.
 */
public final class StepRetryPolicyFactory {

    private StepRetryPolicyFactory() {}

    /**
     * @param maxAttempts
     *            total number of attempts per item, the initial one included; values below 1 are treated as 1
     * @param retryableException
     *            exception type (and its subtypes and nested causes) that should be retried
     * @return a policy that retries immediately until {@code maxAttempts} attempts have been made
     */
    public static RetryPolicy immediateRetryPolicy(int maxAttempts, Class<? extends Throwable> retryableException) {
        return RetryPolicy.builder() //
                .includes(retryableException) //
                .maxRetries(Math.max(maxAttempts - 1, 0)) //
                .delay(Duration.ZERO) //
                .build();
    }
}
