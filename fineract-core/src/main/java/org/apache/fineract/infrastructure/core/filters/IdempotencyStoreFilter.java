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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@RequiredArgsConstructor
@Slf4j
public class IdempotencyStoreFilter extends OncePerRequestFilter {

    private final FineractRequestContextHolder fineractRequestContextHolder;
    private final IdempotencyStoreHelper helper;
    private final FineractProperties fineractProperties;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        Mutable<ContentCachingResponseWrapper> wrapper = new MutableObject<>();
        if (helper.isAllowedContentTypeRequest(request)) {
            wrapper.setValue(new ContentCachingResponseWrapper(response));
        }
        extractIdempotentKeyFromHttpServletRequest(request).ifPresent(idempotentKey -> fineractRequestContextHolder
                .setAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_ATTRIBUTE, idempotentKey, request));

        filterChain.doFilter(request, wrapper.getValue() != null ? wrapper.getValue() : response);
        Optional<Long> commandId = helper.getCommandId(request);
        boolean isSuccessWithoutStored = commandId.isPresent() && wrapper.getValue() != null && helper.isStoreIdempotencyKey(request)
                && helper.isAllowedContentTypeResponse(response);
        if (isSuccessWithoutStored) {
            helper.storeCommandResult(response.getStatus(), Optional.ofNullable(wrapper.getValue())
                    .map(ContentCachingResponseWrapper::getContentAsByteArray).map(s -> new String(s, StandardCharsets.UTF_8)).orElse(null),
                    commandId.get());
        }
        if (wrapper.getValue() != null) {
            wrapper.getValue().copyBodyToResponse();
        }
    }

    private Optional<String> extractIdempotentKeyFromHttpServletRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(fineractProperties.getIdempotencyKeyHeaderName()));
    }
}
