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
package org.apache.fineract.infrastructure.security.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ext.Provider;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Provider
@Component
@RequiredArgsConstructor
public class FineractInstanceModeApiFilter extends OncePerRequestFilter {

    private final FineractProperties fineractProperties;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {

        if (fineractProperties.getMode().isReadInstance() && isReadMethod(request)) {
            filterChain.doFilter(request, response);
        } else if (fineractProperties.getMode().isWriteInstance()) {
            filterChain.doFilter(request, response);
        } else if (fineractProperties.getMode().isBatchInstance() && request.getPathInfo().startsWith("/jobs")) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
            ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.invalidInstanceTypeMethod(request.getMethod());
            response.getWriter().write(errorResponse.toJson());
        }
    }

    private boolean isReadMethod(HttpServletRequest request) {
        return HttpMethod.GET.equals(request.getMethod());
    }
}
