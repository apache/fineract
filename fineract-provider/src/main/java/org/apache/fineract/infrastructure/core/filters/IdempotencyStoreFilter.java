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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.batch.domain.Header;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@RequiredArgsConstructor
@Slf4j
@Component
public class IdempotencyStoreFilter extends OncePerRequestFilter implements BatchFilter {

    private final CommandSourceRepository commandSourceRepository;
    private final CommandSourceService commandSourceService;

    private final FineractProperties fineractProperties;

    private final FineractRequestContextHolder fineractRequestContextHolder;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        Mutable<ContentCachingResponseWrapper> wrapper = new MutableObject<>();
        if (isAllowedContentTypeRequest(request)) {
            wrapper.setValue(new ContentCachingResponseWrapper(response));
        }
        extractIdempotentKeyFromHttpServletRequest(request).ifPresent(idempotentKey -> fineractRequestContextHolder
                .setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idempotentKey, request));

        filterChain.doFilter(request, wrapper.getValue() != null ? wrapper.getValue() : response);
        Optional<Long> commandId = getCommandId(request);
        boolean isSuccessWithoutStored = isStoreIdempotencyKey(request) && commandId.isPresent() && isAllowedContentTypeResponse(response)
                && wrapper.getValue() != null;
        if (isSuccessWithoutStored) {
            storeCommandResult(response.getStatus(), new String(wrapper.getValue().getContentAsByteArray(), StandardCharsets.UTF_8),
                    commandId);
        }
        if (wrapper.getValue() != null) {
            wrapper.getValue().copyBodyToResponse();
        }
    }

    private void storeCommandResult(int response, String body, Optional<Long> commandId) {
        commandSourceRepository.findById(commandId.get()).ifPresent(commandSource -> {
            commandSource.setResultStatusCode(response);
            commandSource.setResult(body);
            commandSourceService.saveResult(commandSource);
        });
    }

    private Optional<String> extractIdempotentKeyFromHttpServletRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(fineractProperties.getIdempotencyKeyHeaderName()));
    }

    private boolean isAllowedContentTypeResponse(HttpServletResponse response) {
        return Optional.ofNullable(response.getContentType()).map(String::toLowerCase).map(ct -> ct.contains("application/json"))
                .orElse(false);
    }

    private boolean isAllowedContentTypeRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getContentType()).map(String::toLowerCase).map(ct -> ct.contains("application/json"))
                .orElse(false);
    }

    private boolean isStoreIdempotencyKey(HttpServletRequest request) {
        return Optional
                .ofNullable(
                        fineractRequestContextHolder.getAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_STORE_FLAG, request))
                .filter(Boolean.class::isInstance).map(Boolean.class::cast).orElse(false);
    }

    private Optional<Long> getCommandId(HttpServletRequest request) {
        return Optional
                .ofNullable(fineractRequestContextHolder.getAttribute(SynchronousCommandProcessingService.COMMAND_SOURCE_ID, request))
                .filter(Long.class::isInstance).map(Long.class::cast);
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

    @Override
    public BatchResponse doFilter(BatchRequest batchRequest, UriInfo uriInfo, BatchFilterChain chain) {
        extractIdempotentKeyFromBatchRequest(batchRequest).ifPresent(idempotentKey -> fineractRequestContextHolder
                .setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idempotentKey));
        BatchResponse result = chain.serviceCall(batchRequest, uriInfo);
        Optional<Long> commandId = getCommandId(null);
        boolean isSuccessWithoutStored = isStoreIdempotencyKey(null) && commandId.isPresent();
        if (isSuccessWithoutStored) {
            storeCommandResult(result.getStatusCode(), result.getBody(), commandId);
        }
        return result;
    }
}
