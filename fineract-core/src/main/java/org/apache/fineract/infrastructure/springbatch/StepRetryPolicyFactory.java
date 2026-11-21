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
 * Builds the retry policy of the fault-tolerant COB worker steps.
 * <p>
 * They do not retry, and must not. Spring Batch 5 retried statefully: a failed item rethrew, the chunk transaction
 * rolled back, and the next attempt ran in a new transaction against freshly read state. Spring Batch 6's
 * {@code ChunkOrientedStep} retries inside the transaction the item failed in. A COB failure has by then marked that
 * transaction rollback-only - through the participating {@code @Transactional} of {@code COBBusinessStepService.run},
 * or through EclipseLink on a failed flush - so a retry either fails again, or appears to succeed: no error reaches
 * Batch, scan mode never starts, and the chunk is silently rolled back at its end while being recorded as committed,
 * leaving its accounts locked with no error.
 * <p>
 * Recovery comes from scan mode instead, which writes each item of a failed chunk again in a transaction of its own:
 * that is the one meaningful retry, and it is built in. An account that fails there stays locked with its error until
 * the next COB, catch-up or inline COB run.
 * <p>
 * {@code ChunkOrientedStepBuilder.retryLimit(n)} is not used: it forwards to
 * {@code RetryPolicy.Builder#maxRetries(long)}, whose default back-off would also pause one second before every retry.
 */
public final class StepRetryPolicyFactory {

    private StepRetryPolicyFactory() {}

    /**
     * @return a policy that makes exactly one attempt per item and never retries
     */
    public static RetryPolicy noRetryPolicy() {
        return RetryPolicy.builder() //
                .includes(Throwable.class) //
                .maxRetries(0) //
                .delay(Duration.ZERO) //
                .build();
    }
}
