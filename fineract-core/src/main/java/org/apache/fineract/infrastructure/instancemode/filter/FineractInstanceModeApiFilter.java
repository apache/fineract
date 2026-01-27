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
package org.apache.fineract.infrastructure.instancemode.filter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.fineract.infrastructure.instancemode.filter.FineractInstanceModeApiFilter.ExceptionListItem.item;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.HttpMethod;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class FineractInstanceModeApiFilter extends OncePerRequestFilter {

    private static final List<ExceptionListItem> EXCEPTION_LIST = List.of(
            item(FineractProperties.FineractModeProperties::isBatchManagerEnabled, pi -> pi.startsWith("/v1/jobs")),
            item(FineractProperties.FineractModeProperties::isBatchManagerEnabled, pi -> pi.startsWith("/v1/scheduler")),
            item(FineractProperties.FineractModeProperties::isBatchManagerEnabled, pi -> pi.startsWith("/v1/loans/catch-up")),
            item(FineractProperties.FineractModeProperties::isBatchManagerEnabled, pi -> pi.startsWith("/v1/loans/is-catch-up-running")),
            item(p -> true, pi -> pi.startsWith("/v1/instance-mode")),
            // Batches with all GET requests need to be allowed in read-only instances, hence this check will be
            // moved
            // under the Api Resource.
            item(p -> true, pi -> pi.startsWith("/v1/batches")));

    private final FineractProperties fineractProperties;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        if (isOnExceptionList(request) || isActuatorApi(request)) {
            proceed(filterChain, request, response);
        } else {
            if (isPathOnExceptionList(request)) {
                reject(request, response);
            } else {
                if (fineractProperties.getMode().isReadEnabled() && isReadMethod(request)) {
                    proceed(filterChain, request, response);
                } else if (fineractProperties.getMode().isWriteEnabled()) {
                    proceed(filterChain, request, response);
                } else {
                    reject(request, response);
                }
            }
        }
    }

    private boolean isActuatorApi(HttpServletRequest request) {
        if (isBlank(request.getServletPath())) {
            return false;
        }
        return request.getServletPath().startsWith("/actuator");
    }

    private void proceed(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.invalidInstanceTypeMethod(request.getMethod());
        response.getWriter().write(errorResponse.toJson());
    }

    private boolean isPathOnExceptionList(HttpServletRequest request) {
        if (isBlank(request.getPathInfo())) {
            return false;
        }
        return EXCEPTION_LIST.stream().anyMatch(item -> item.getPathFunction().apply(request.getPathInfo()));
    }

    private boolean isOnExceptionList(HttpServletRequest request) {
        if (isBlank(request.getPathInfo())) {
            return false;
        }
        return EXCEPTION_LIST.stream().anyMatch(
                item -> item.getModeFunction().apply(fineractProperties.getMode()) && item.getPathFunction().apply(request.getPathInfo()));
    }

    private boolean isReadMethod(HttpServletRequest request) {
        return HttpMethod.GET.equals(request.getMethod());
    }

    @RequiredArgsConstructor
    @Getter
    static class ExceptionListItem {

        private final Function<FineractProperties.FineractModeProperties, Boolean> modeFunction;
        private final Function<String, Boolean> pathFunction;

        public static ExceptionListItem item(Function<FineractProperties.FineractModeProperties, Boolean> modeFunction,
                Function<String, Boolean> pathFunction) {
            return new ExceptionListItem(modeFunction, pathFunction);
        }
    }
}
