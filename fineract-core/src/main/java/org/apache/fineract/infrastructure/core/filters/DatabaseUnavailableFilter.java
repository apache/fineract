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
import org.apache.fineract.infrastructure.core.exceptionmapper.DatabaseUnavailableSupport;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Answers HTTP 503 with Retry-After when the database is not available while the request is still travelling through
 * the servlet filters, i.e. before the JAX-RS layer (and its exception mappers) is reached.
 *
 * It runs just ahead of the Spring Security filter chain (whose registration has the default order of -100), so one
 * place covers the Basic, OAuth2 and OIDC chains and the authentication filters need no knowledge of it. If the
 * response is already committed nothing can be changed any more, so the exception is rethrown untouched.
 */
public class DatabaseUnavailableFilter extends OncePerRequestFilter implements Ordered {

    public static final int ORDER = -101;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (ServletException | IOException | RuntimeException e) {
            final Throwable unavailable = DatabaseUnavailableSupport.findDatabaseUnavailable(e);
            if (unavailable == null || response.isCommitted()) {
                throw e;
            }
            DatabaseUnavailableSupport.writeServiceUnavailable(response, unavailable);
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
