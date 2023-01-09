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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sun.research.ws.wadl.HTTPMethods;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoanCOBApiFilterTest {

    @InjectMocks
    private LoanCOBApiFilter testObj;
    @Mock
    private LoanAccountLockService loanAccountLockService;
    @Mock
    private GLIMAccountInfoRepository glimAccountInfoRepository;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private InlineLoanCOBExecutorServiceImpl inlineLoanCOBExecutorService;

    @Test
    void shouldProceedWhenUrlDoesNotMatch() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        given(request.getPathInfo()).willReturn("/jobs/2/inline");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldProceedWhenUserHasBypassPermission() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/jobs/2/inline");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(context.authenticatedUser()).willReturn(appUser);
        given(appUser.isBypassUser()).willReturn(true);

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldProceedWhenLoanIsNotLocked() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/loans/2/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(loanAccountLockService.isLoanSoftLocked(2L)).willReturn(false);
        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldRunInlineCOBAndProceedWhenLoanIsSoftLocked() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/loans/2/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(loanAccountLockService.isLoanSoftLocked(2L)).willReturn(true);
        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(inlineLoanCOBExecutorService, times(1)).execute(Collections.singletonList(2L), "INLINE_LOAN_COB");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldRejectWhenLoanIsHardLocked() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/loans/2/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(true);
        given(response.getWriter()).willReturn(writer);
        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(response, times(1)).setStatus(HttpStatus.SC_CONFLICT);
    }

    @Test
    void shouldRejectWhenGlimLoanIsHardLocked() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);
        GroupLoanIndividualMonitoringAccount glimAccount = mock(GroupLoanIndividualMonitoringAccount.class);
        Loan loan = mock(Loan.class);
        Long loanId = 2L;
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/loans/glimAccount/2");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(glimAccountInfoRepository.findOneByIsAcceptingChildAndApplicationId(true, BigDecimal.valueOf(2))).willReturn(glimAccount);
        given(glimAccount.getChildLoan()).willReturn(Collections.singleton(loan));
        given(loan.getId()).willReturn(loanId);
        given(loanAccountLockService.isLoanHardLocked(loanId)).willReturn(true);
        given(response.getWriter()).willReturn(writer);
        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(response, times(1)).setStatus(HttpStatus.SC_CONFLICT);
    }
}
