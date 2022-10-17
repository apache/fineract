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
import com.sun.research.ws.wadl.HTTPMethods;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
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
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.core.data.ApiGlobalErrorResponse;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class LoanCOBApiFilter extends OncePerRequestFilter {

    private final GLIMAccountInfoRepository glimAccountInfoRepository;
    private final LoanAccountLockService loanAccountLockService;

    private static final List<HTTPMethods> HTTP_METHODS = List.of(HTTPMethods.POST, HTTPMethods.PUT, HTTPMethods.DELETE);
    private static final Function<String, Boolean> URL_FUNCTION = s -> s.matches("/loans/\\d+.*") || s.matches("/loans/glimAccount/\\d+.*");
    private static final Integer LOAN_ID_INDEX_IN_URL = 2;
    private static final Integer GLIM_ID_INDEX_IN_URL = 3;
    private static final Integer GLIM_STRING_INDEX_IN_URL = 2;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isOnApiList(request)) {
            proceed(filterChain, request, response);
        } else {
            Iterable<String> split = Splitter.on('/').split(request.getPathInfo());
            Supplier<Stream<String>> streamSupplier = () -> StreamSupport.stream(split.spliterator(), false);
            boolean isGlim = isGlim(streamSupplier);
            Long loanId = getLoanId(isGlim, streamSupplier);
            if (isLoanLocked(loanId, isGlim)) {
                reject(loanId, response);
            } else {
                proceed(filterChain, request, response);
            }
        }
    }

    private boolean isLoanLocked(Long loanId, boolean isGlim) {
        if (!isGlim) {
            return loanAccountLockService.isLoanHardLocked(loanId);
        } else {
            GroupLoanIndividualMonitoringAccount glimAccount = glimAccountInfoRepository.findOneByIsAcceptingChildAndApplicationId(true,
                    BigDecimal.valueOf(loanId));
            if (glimAccount != null) {
                Set<Loan> loans = glimAccount.getChildLoan();
                List<Long> loanIds = loans.stream().map(Loan::getId).toList();
                return loanIds.stream().anyMatch(loanAccountLockService::isLoanHardLocked);
            } else {
                return false;
            }
        }
    }

    private void proceed(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        filterChain.doFilter(request, response);
    }

    private void reject(Long loanId, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SC_CONFLICT);
        ApiGlobalErrorResponse errorResponse = ApiGlobalErrorResponse.loanIsLocked(loanId);
        response.getWriter().write(errorResponse.toJson());
    }

    private Long getLoanId(boolean isGlim, Supplier<Stream<String>> streamSupplier) {
        if (!isGlim) {
            if (streamSupplier.get().count() >= LOAN_ID_INDEX_IN_URL + 1) {
                return Long.valueOf(streamSupplier.get().skip(LOAN_ID_INDEX_IN_URL).findFirst().get());
            } else {
                return null;
            }
        } else {
            if (streamSupplier.get().count() >= GLIM_ID_INDEX_IN_URL + 1) {
                return Long.valueOf(streamSupplier.get().skip(GLIM_ID_INDEX_IN_URL).findFirst().get());
            } else {
                return null;
            }
        }
    }

    private boolean isOnApiList(HttpServletRequest request) {
        if (StringUtils.isBlank(request.getPathInfo())) {
            return false;
        }
        return HTTP_METHODS.contains(HTTPMethods.fromValue(request.getMethod())) && URL_FUNCTION.apply(request.getPathInfo());
    }

    private boolean isGlim(Supplier<Stream<String>> streamSupplier) {
        if (streamSupplier.get().count() >= GLIM_STRING_INDEX_IN_URL + 1) {
            return "glimAccount".equals(streamSupplier.get().skip(GLIM_STRING_INDEX_IN_URL).findFirst().get());
        }
        return false;
    }
}
