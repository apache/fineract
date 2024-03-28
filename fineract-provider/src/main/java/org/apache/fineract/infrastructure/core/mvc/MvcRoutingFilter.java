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
package org.apache.fineract.infrastructure.core.mvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.mvc.ProfileMvc;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Forwards JAX-RS requests to Spring MVC if a handler exists.
 */
@ProfileMvc
@Component
@RequiredArgsConstructor
public class MvcRoutingFilter extends OncePerRequestFilter {

    private static final String SPRING_MVC_SERVLET = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME;
    private final ServletContext servletContext;
    @Qualifier("requestMappingHandlerMapping")
    private final RequestMappingHandlerMapping handlerMapping;

    @Override
    protected boolean shouldNotFilter(@NotNull HttpServletRequest request) {
        try {
            return handlerMapping.getHandler(request) == null;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get handler for request", e);
        }
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain) throws ServletException, IOException {
        servletContext.getNamedDispatcher(SPRING_MVC_SERVLET).forward(request, response);
    }
}
