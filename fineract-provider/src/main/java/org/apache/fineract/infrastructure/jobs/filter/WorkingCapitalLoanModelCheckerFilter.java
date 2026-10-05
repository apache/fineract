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
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanModelProcessingService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rebuilds a Working Capital loan's amortization model before a request touches it, when the stored model was written
 * by an older version of the calculation.
 *
 * <p>
 * Close-of-business rebuilds too, but only once a day and only for the loans it processes. A repayment arriving between
 * runs would otherwise read a model that has silently lost part of itself and write the loss back for good, so the
 * repair has to happen on the request path as well. The counterpart of {@link ProgressiveLoanModelCheckerFilter}.
 */
@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanModelCheckerFilter extends OncePerRequestFilter {

    private final WorkingCapitalLoanModelProcessingService modelProcessingService;
    private final WorkingCapitalLoanModelCheckerHelper helper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        request = new BodyCachingHttpServletRequestWrapper(request);

        if (helper.isOnApiList((BodyCachingHttpServletRequestWrapper) request)) {
            final List<Long> loanIds = helper.calculateRelevantLoanIds((BodyCachingHttpServletRequestWrapper) request);
            if (!loanIds.isEmpty()) {
                modelProcessingService.findLoanIdsRequiringModelRecalculation(loanIds)
                        .forEach(modelProcessingService::recalculateModelAndSave);
            }
        }
        filterChain.doFilter(request, response);
    }
}
