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
package org.apache.fineract.infrastructure.jobs.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.conditions.LoanCOBEnabledCondition;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.jobs.exception.LoanIdsHardLockedException;
import org.apache.fineract.useradministration.exception.UnAuthenticatedUserException;
import org.apache.http.HttpStatus;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Conditional(LoanCOBEnabledCondition.class)
public class LoanCOBApiFilter extends OncePerRequestFilter {

    private final LoanCOBFilterHelper helper;

    private static class Reject {

        private final String message;
        private final Integer statusCode;

        Reject(String message, Integer statusCode) {
            this.message = message;
            this.statusCode = statusCode;
        }

        public static Reject reject(Long loanId, int status) {
            return new Reject(ApiGlobalErrorResponse.loanIsLocked(loanId).toJson(), status);
        }

        public void toServletResponse(HttpServletResponse response) throws IOException {
            response.setStatus(statusCode);
            response.getWriter().write(message);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        request = new BodyCachingHttpServletRequestWrapper(request);

        if (!helper.isOnApiList(request)) {
            proceed(filterChain, request, response);
        } else {
            try {
                boolean bypassUser = helper.isBypassUser();
                if (bypassUser) {
                    proceed(filterChain, request, response);
                } else {
                    try {
                        List<Long> loanIds = helper.calculateRelevantLoanIds(request);
                        if (!loanIds.isEmpty() && helper.isLoanBehind(loanIds)) {
                            helper.executeInlineCob(loanIds);
                        }
                        proceed(filterChain, request, response);
                    } catch (LoanIdsHardLockedException e) {
                        Reject.reject(e.getLoanIdFromRequest(), HttpStatus.SC_CONFLICT).toServletResponse(response);
                    }
                }
            } catch (UnAuthenticatedUserException e) {
                Reject.reject(null, HttpStatus.SC_UNAUTHORIZED).toServletResponse(response);
            }
        }
    }

    private void proceed(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        filterChain.doFilter(request, response);
    }

}
