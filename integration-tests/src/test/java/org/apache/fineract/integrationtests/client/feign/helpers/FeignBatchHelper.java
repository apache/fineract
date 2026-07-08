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

import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;

public class FeignBatchHelper {

    private final FineractFeignClient fineractClient;

    public FeignBatchHelper(FineractFeignClient fineractClient) {
        this.fineractClient = fineractClient;
    }

    /**
     * Posts the batch requests with {@code enclosingTransaction=true}; the whole batch is rolled back if any request
     * fails. Returns the individual {@link BatchResponse}s on success.
     */
    public List<BatchResponse> executeEnclosingTransaction(List<BatchRequest> requests) {
        return ok(() -> fineractClient.batch().handleBatchRequests(requests, true));
    }

    /**
     * Posts the batch requests with {@code enclosingTransaction=false}; each request is committed independently.
     * Returns the individual {@link BatchResponse}s (inspect their {@code statusCode}).
     */
    public List<BatchResponse> executeWithoutEnclosingTransaction(List<BatchRequest> requests) {
        return ok(() -> fineractClient.batch().handleBatchRequests(requests, false));
    }

    /**
     * Posts the batch requests with {@code enclosingTransaction=false} expecting the call itself to fail (e.g. the loan
     * is locked). Returns the parsed {@link ErrorResponse} carrying the HTTP status code.
     */
    public ErrorResponse executeWithoutEnclosingTransactionExpectingError(List<BatchRequest> requests) {
        try {
            ok(() -> fineractClient.batch().handleBatchRequests(requests, false));
            throw new AssertionError("Expected batch request to fail but it succeeded");
        } catch (CallFailedRuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setHttpStatusCode(e.getStatus());
            return errorResponse;
        }
    }
}
