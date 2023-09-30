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
package org.apache.fineract.infrastructure.core.filters;

import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotencyStoreBatchFilter implements BatchFilter {

    private final FineractRequestContextHolder fineractRequestContextHolder;
    private final IdempotencyStoreHelper helper;
    private final FineractProperties fineractProperties;

    @Override
    public BatchResponse doFilter(BatchRequest batchRequest, UriInfo uriInfo, BatchFilterChain chain) {
        extractIdempotentKeyFromBatchRequest(batchRequest).ifPresent(idempotentKey -> fineractRequestContextHolder
                .setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idempotentKey));
        BatchResponse result = chain.serviceCall(batchRequest, uriInfo);
        Optional<Long> commandId = helper.getCommandId(null);
        boolean isSuccessWithoutStored = commandId.isPresent() && helper.isStoreIdempotencyKey(null);
        if (isSuccessWithoutStored) {
            helper.storeCommandResult(result.getStatusCode(), result.getBody(), commandId.get());
        }
        return result;
    }

    private Optional<String> extractIdempotentKeyFromBatchRequest(BatchRequest request) {
        if (request.getHeaders() == null) {
            return Optional.empty();
        }
        return request.getHeaders() //
                .stream().filter(header -> header.getName().equals(fineractProperties.getIdempotencyKeyHeaderName())) //
                .map(Header::getValue) //
                .findAny(); //

    }
}
