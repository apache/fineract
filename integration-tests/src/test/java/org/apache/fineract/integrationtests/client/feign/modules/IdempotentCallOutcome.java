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
package org.apache.fineract.integrationtests.client.feign.modules;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ApiResponse;
import org.apache.fineract.infrastructure.core.exception.AbstractIdempotentCommandException;

public record IdempotentCallOutcome<T>(int status, boolean servedFromCache, T data, CallFailedRuntimeException failure) {

    public static <T> IdempotentCallOutcome<T> capture(Supplier<ApiResponse<T>> call) {
        try {
            ApiResponse<T> response = call.get();
            return new IdempotentCallOutcome<>(response.getStatusCode(), hasCacheHeader(response.getHeaders()), response.getData(), null);
        } catch (CallFailedRuntimeException e) {
            return new IdempotentCallOutcome<>(e.getStatus(), hasCacheHeader(e.getHeaders()), null, e);
        }
    }

    public String responseBody() {
        return failure == null ? null : failure.getResponseBody();
    }

    public List<String> errorCodes() {
        if (failure == null) {
            return List.of();
        }
        return FeignErrors.reportedErrors(failure).stream().map(FeignErrors.ReportedError::userMessageGlobalisationCode).toList();
    }

    @Override
    public String toString() {
        return "status=" + status + ", servedFromCache=" + servedFromCache + ", data=" + data + ", body=" + responseBody();
    }

    private static boolean hasCacheHeader(Map<String, Collection<String>> headers) {
        return headers != null
                && headers.keySet().stream().anyMatch(AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER::equalsIgnoreCase);
    }
}
