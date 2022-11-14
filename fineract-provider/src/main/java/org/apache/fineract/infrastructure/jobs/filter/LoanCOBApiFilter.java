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

import com.google.common.base.Splitter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.useradministration.exception.UnAuthenticatedUserException;
import org.apache.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class LoanCOBApiFilter extends OncePerRequestFilter {

    private final GLIMAccountInfoRepository glimAccountInfoRepository;
    private final LoanAccountLockService loanAccountLockService;
    private final PlatformSecurityContext context;
    private final InlineLoanCOBExecutorServiceImpl inlineLoanCOBExecutorService;

    private static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE);
    private static final Function<String, Boolean> URL_FUNCTION = s -> s.matches("/loans/\\d+.*") || s.matches("/loans/glimAccount/\\d+.*");
    private static final Integer LOAN_ID_INDEX_IN_URL = 2;
    private static final Integer GLIM_ID_INDEX_IN_URL = 3;
    private static final Integer GLIM_STRING_INDEX_IN_URL = 2;
    private static final String JOB_NAME = "INLINE_LOAN_COB";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isOnApiList(request) || isBypassUser(response)) {
            proceed(filterChain, request, response);
        } else {
            Iterable<String> split = Splitter.on('/').split(request.getPathInfo());
            Supplier<Stream<String>> streamSupplier = () -> StreamSupport.stream(split.spliterator(), false);
            boolean isGlim = isGlim(streamSupplier);
            Long loanIdFromRequest = getLoanId(isGlim, streamSupplier);
            List<Long> loanIds = isGlim ? getGlimChildLoanIds(loanIdFromRequest) : Collections.singletonList(loanIdFromRequest);
            if (isLoanHardLocked(loanIds)) {
                reject(loanIdFromRequest, response, HttpStatus.SC_CONFLICT);
            } else if (isLoanSoftLocked(loanIds)) {
                executeInlineCob(loanIds);
                proceed(filterChain, request, response);
            } else {
                proceed(filterChain, request, response);
            }
        }
    }

    private void executeInlineCob(List<Long> loanIds) {
        inlineLoanCOBExecutorService.execute(loanIds, JOB_NAME);
    }

    private boolean isBypassUser(HttpServletResponse response) throws IOException {
        try {
            return context.authenticatedUser().isBypassUser();
        } catch (UnAuthenticatedUserException e) {
            reject(null, response, HttpStatus.SC_UNAUTHORIZED);
        }
        return false;
    }

    private List<Long> getGlimChildLoanIds(Long loanIdFromRequest) {
        GroupLoanIndividualMonitoringAccount glimAccount = glimAccountInfoRepository.findOneByIsAcceptingChildAndApplicationId(true,
                BigDecimal.valueOf(loanIdFromRequest));
        if (glimAccount != null) {
            return glimAccount.getChildLoan().stream().map(Loan::getId).toList();
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isLoanSoftLocked(List<Long> loanIds) {
        return isLoanLocked(loanIds, false);
    }

    private boolean isLoanHardLocked(List<Long> loanIds) {
        return isLoanLocked(loanIds, true);
    }

    private boolean isLoanLocked(List<Long> loanIds, boolean isHardLock) {
        return isHardLock ? loanIds.stream().anyMatch(loanAccountLockService::isLoanHardLocked)
                : loanIds.stream().anyMatch(loanAccountLockService::isLoanSoftLocked);
    }

    private void proceed(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        filterChain.doFilter(request, response);
    }

    private void reject(Long loanId, HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.loanIsLocked(loanId);
        response.getWriter().write(errorResponse.toJson());
    }

    private Long getLoanId(boolean isGlim, Supplier<Stream<String>> streamSupplier) {
        if (!isGlim) {
            return streamSupplier.get().skip(LOAN_ID_INDEX_IN_URL).findFirst().map(Long::valueOf).orElse(null);
        } else {
            return streamSupplier.get().skip(GLIM_ID_INDEX_IN_URL).findFirst().map(Long::valueOf).orElse(null);
        }
    }

    private boolean isOnApiList(HttpServletRequest request) {
        if (StringUtils.isBlank(request.getPathInfo())) {
            return false;
        }
        return HTTP_METHODS.contains(HttpMethod.valueOf(request.getMethod())) && URL_FUNCTION.apply(request.getPathInfo());
    }

    private boolean isGlim(Supplier<Stream<String>> streamSupplier) {
        return streamSupplier.get().skip(GLIM_STRING_INDEX_IN_URL).findFirst().map(s -> s.equals("glimAccount")).orElse(false);
    }
}
