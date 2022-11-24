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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.service.CommandSourceService;
import org.apache.fineract.commands.service.SynchronousCommandProcessingService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@RequiredArgsConstructor
@Slf4j
@Component
public class IdempotencyStoreFilter extends OncePerRequestFilter {

    private final CommandSourceRepository commandSourceRepository;
    private final CommandSourceService commandSourceService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        Mutable<ContentCachingResponseWrapper> wrapper = new MutableObject<>();
        if (isAllowedContentTypeRequest(request)) {
            wrapper.setValue(new ContentCachingResponseWrapper(response));
        }

        filterChain.doFilter(request, wrapper.getValue() != null ? wrapper.getValue() : response);
        Optional<Long> commandId = getCommandId(request);
        boolean isSuccessWithoutStored = isStoreIdempotencyKey(request) && commandId.isPresent() && isAllowedContentTypeResponse(response)
                && wrapper.getValue() != null;
        if (isSuccessWithoutStored) {
            commandSourceRepository.findById(commandId.get()).ifPresent(commandSource -> {
                commandSource.setResultStatusCode(response.getStatus());
                commandSource.setResult(new String(wrapper.getValue().getContentAsByteArray(), StandardCharsets.UTF_8));
                commandSourceService.saveResult(commandSource);
            });
        }
        if (wrapper.getValue() != null) {
            wrapper.getValue().copyBodyToResponse();
        }
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
        return Optional.ofNullable(request.getAttribute(SynchronousCommandProcessingService.IDEMPOTENCY_KEY_STORE_FLAG))
                .filter(Boolean.class::isInstance).map(Boolean.class::cast).orElse(false);
    }

    private Optional<Long> getCommandId(HttpServletRequest request) {
        return Optional.ofNullable(request.getAttribute(SynchronousCommandProcessingService.COMMAND_SOURCE_ID))
                .filter(Long.class::isInstance).map(Long.class::cast);
    }
}
