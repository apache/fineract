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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepRequest;
import org.apache.fineract.client.models.JobBusinessStepConfigData;
import org.apache.fineract.client.models.JobBusinessStepDetail;

public final class IdempotencyHelper {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private IdempotencyHelper() {

    }

    public static JobBusinessStepConfigData getConfiguredBusinessStepsByJobName(final String jobName) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessStepConfiguration()
                .retrieveAllConfiguredBusinessStep(jobName));
    }

    public static JobBusinessStepDetail getAvailableBusinessStepsByJobName(final String jobName) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessStepConfiguration()
                .retrieveAllAvailableBusinessStep(jobName));
    }

    /**
     * Updates the business step order for a job, expecting success. Returns the raw {@link ApiResponse} (rather than
     * just the body, which is empty for this 204 endpoint) so the caller can inspect response headers, in particular
     * the idempotency cache-hit header.
     */
    public static ApiResponse<Void> updateBusinessStepOrder(final String jobName, final List<BusinessStep> businessSteps,
            final String idempotencyKey) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().businessStepConfiguration()
                .updateJobBusinessStepConfigWithHttpInfo(jobName, new BusinessStepRequest().businessSteps(businessSteps),
                        Map.of(IDEMPOTENCY_KEY_HEADER, idempotencyKey)));
    }

    /**
     * Same call as {@link #updateBusinessStepOrder}, but expecting the call to fail (for negative/idempotent-error
     * tests). Returns the {@link CallFailedRuntimeException}, which exposes status, developer message, response
     * headers, and the raw response body.
     */
    public static CallFailedRuntimeException updateBusinessStepOrderExpectingFailure(final String jobName,
            final List<BusinessStep> businessSteps, final String idempotencyKey) {
        return fail(() -> FineractFeignClientHelper.getFineractFeignClient().businessStepConfiguration()
                .updateJobBusinessStepConfigWithHttpInfo(jobName, new BusinessStepRequest().businessSteps(businessSteps),
                        Map.of(IDEMPOTENCY_KEY_HEADER, idempotencyKey)));
    }
}
