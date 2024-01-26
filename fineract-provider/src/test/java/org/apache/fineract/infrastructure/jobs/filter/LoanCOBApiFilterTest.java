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

import static org.apache.fineract.infrastructure.jobs.filter.LoanCOBFilterHelper.LOAN_GLIMACCOUNT_PATH_PATTERN;
import static org.apache.fineract.infrastructure.jobs.filter.LoanCOBFilterHelper.LOAN_PATH_PATTERN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sun.research.ws.wadl.HTTPMethods;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import org.apache.fineract.cob.data.LoanIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.loan.RetrieveLoanIdService;
import org.apache.fineract.cob.service.InlineLoanCOBExecutorServiceImpl;
import org.apache.fineract.cob.service.LoanAccountLockService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    private LoanCOBApiFilter testObj;
    @InjectMocks
    private LoanCOBFilterHelper helper;
    @Mock
    private LoanAccountLockService loanAccountLockService;
    @Mock
    private GLIMAccountInfoRepository glimAccountInfoRepository;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private InlineLoanCOBExecutorServiceImpl inlineLoanCOBExecutorService;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractQueryProperties fineractQueryProperties;
    @Mock
    private LoanRescheduleRequestRepository loanRescheduleRequestRepository;
    @Mock
    private RetrieveLoanIdService retrieveLoanIdService;

    @BeforeEach
    public void setUp() {
        testObj = new LoanCOBApiFilter(helper);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldLoanAndExternalMatchToo() {
        String externalId = UUID.randomUUID().toString();
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/loans/12").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/loans/12?correct=parameter").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/loans/12?correct=parameter").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/rescheduleloans/12").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/rescheduleloans/12?correct=parameter").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/rescheduleloans/12?correct=parameter").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/loans/external-id/" + externalId).matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/loans/external-id/" + externalId + "?additional=parameter").matches());
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/loans/12").replaceAll("$1"));
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/loans/12?correct=parameter").replaceAll("$1"));
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/rescheduleloans/12").replaceAll("$1"));
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/rescheduleloans/12?correct=parameter").replaceAll("$1"));
        Assertions.assertEquals(externalId, LOAN_PATH_PATTERN.matcher("/v1/loans/external-id/" + externalId).replaceAll("$1"));
        Assertions.assertEquals(externalId,
                LOAN_PATH_PATTERN.matcher("/v1/loans/external-id/" + externalId + "?additional=parameter").replaceAll("$1"));
    }

    @Test
    void shouldGlimAccountMatch() {
        Assertions.assertTrue(LOAN_GLIMACCOUNT_PATH_PATTERN.matcher("/v1/loans/glimAccount/12").matches());
        Assertions.assertTrue(LOAN_GLIMACCOUNT_PATH_PATTERN.matcher("/v1/loans/glimAccount/12?additional=parameter").matches());
        Assertions.assertEquals("12", LOAN_GLIMACCOUNT_PATH_PATTERN.matcher("/v1/loans/glimAccount/12").replaceAll("$1"));
        Assertions.assertEquals("12",
                LOAN_GLIMACCOUNT_PATH_PATTERN.matcher("/v1/loans/glimAccount/12?additional=parameter").replaceAll("$1"));
    }

    @Test
    void shouldProceedWhenUrlDoesNotMatch() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        given(request.getPathInfo()).willReturn("/v1/jobs/2/inline");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenUrlDoesNotMatchWithInvalidLoanId() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        given(request.getPathInfo()).willReturn("/v1/loans/invalid2LoanId/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(context.authenticatedUser()).willReturn(appUser);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenUserHasBypassPermission() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/v1/jobs/2/inline");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(context.authenticatedUser()).willReturn(appUser);
        given(appUser.isBypassUser()).willReturn(true);

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenLoanIsNotLockedAndNoLoanIsBehind() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        given(request.getPathInfo()).willReturn("/v1/loans/2/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(context.authenticatedUser()).willReturn(appUser);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenExternalLoanIsNotLockedAndNotBehind() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        String uuid = UUID.randomUUID().toString();
        given(request.getPathInfo()).willReturn("/v1/loans/external-id/" + uuid + "/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(context.authenticatedUser()).willReturn(appUser);
        given(loanRepository.findIdByExternalId(any())).willReturn(2L);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenRescheduleLoanIsNotLockedAndNotBehind() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        Long resourceId = 123L;
        given(request.getPathInfo()).willReturn("/v1/rescheduleloans/" + resourceId + "/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(loanRescheduleRequestRepository.getLoanIdByRescheduleRequestId(resourceId)).willReturn(Optional.of(2L));
        given(context.authenticatedUser()).willReturn(appUser);

        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldRunInlineCOBAndProceedWhenLoanIsBehind() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        LoanIdAndLastClosedBusinessDate result = mock(LoanIdAndLastClosedBusinessDate.class);
        given(result.getId()).willReturn(2L);
        given(result.getLastClosedBusinessDate()).willReturn(businessDate.minusDays(2));
        given(request.getPathInfo()).willReturn("/v1/loans/2?command=approve");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.singletonList(result));
        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(inlineLoanCOBExecutorService, times(1)).execute(Collections.singletonList(2L), "INLINE_LOAN_COB");
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldNotRunInlineCOBAndProceedWhenLoanIsNotBehind() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        LoanIdAndLastClosedBusinessDate result = mock(LoanIdAndLastClosedBusinessDate.class);
        given(result.getId()).willReturn(2L);
        given(request.getPathInfo()).willReturn("/v1/loans/2?command=approve");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
        given(loanAccountLockService.isLoanHardLocked(2L)).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveLoanIdService.retrieveLoanIdsBehindDate(eq(ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE)),
                anyList())).willReturn(Collections.emptyList());

        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(inlineLoanCOBExecutorService, times(0)).execute(Collections.singletonList(2L), "INLINE_LOAN_COB");
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldNotRunInlineCOBAndProceedWhenLoanIsBehindForLoanCreation() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/v1/loans");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));

        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(inlineLoanCOBExecutorService, times(0)).execute(Collections.singletonList(2L), "INLINE_LOAN_COB");
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldNotRunInlineCOBForCatchUp() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/v1/loans/catch-up");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));

        given(context.authenticatedUser()).willReturn(appUser);

        testObj.doFilterInternal(request, response, filterChain);
        verify(inlineLoanCOBExecutorService, times(0)).execute(Collections.singletonList(2L), "INLINE_LOAN_COB");
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldRejectWhenLoanIsHardLocked() throws ServletException, IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);
        AppUser appUser = mock(AppUser.class);

        given(request.getPathInfo()).willReturn("/v1/loans/2/charges");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
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

        given(request.getPathInfo()).willReturn("/v1/loans/glimAccount/2");
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream()).willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new byte[0]));
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
