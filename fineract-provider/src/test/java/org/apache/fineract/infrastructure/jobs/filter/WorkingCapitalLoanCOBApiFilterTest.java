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

import static org.apache.fineract.infrastructure.jobs.filter.WorkingCapitalLoanCOBFilterHelperImpl.LOAN_PATH_PATTERN;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sun.research.ws.wadl.HTTPMethods;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.cob.data.COBIdAndLastClosedBusinessDate;
import org.apache.fineract.cob.domain.WorkingCapitalLoanAccountLock;
import org.apache.fineract.cob.service.AbstractAccountLockService;
import org.apache.fineract.cob.service.InlineCommonLockableCOBExecutorService;
import org.apache.fineract.cob.workingcapitalloan.WorkingCapitalLoanRetrieveIdService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.http.BodyCachingHttpServletRequestWrapper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.exception.UnAuthenticatedUserException;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WorkingCapitalLoanCOBApiFilterTest {

    private static final String INLINE_WC_JOB = "INLINE_WORKING_CAPITAL_LOAN_COB";

    private WorkingCapitalLoanCOBApiFilter testObj;
    @InjectMocks
    private WorkingCapitalLoanCOBFilterHelperImpl helper;
    @Mock
    private AbstractAccountLockService<WorkingCapitalLoanAccountLock> loanAccountLockService;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private InlineCommonLockableCOBExecutorService<WorkingCapitalLoanAccountLock> inlineLoanCOBExecutorService;
    @Mock
    private WorkingCapitalLoanRepository loanRepository;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractQueryProperties fineractQueryProperties;
    @Mock
    private WorkingCapitalLoanRetrieveIdService retrieveIdService;

    @BeforeEach
    public void setUp() {
        testObj = new WorkingCapitalLoanCOBApiFilter(helper);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private static void setBusinessDates() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "USPPWC", "PPWC", "Asia/Kolkata", null));
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        LocalDate businessDate = LocalDate.now(ZoneId.systemDefault());
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate);
        businessDates.put(BusinessDateType.COB_DATE, businessDate.minusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
    }

    private static MockHttpServletRequest request(String pathInfo) throws IOException {
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        given(request.getPathInfo()).willReturn(pathInfo);
        given(request.getMethod()).willReturn(HTTPMethods.POST.value());
        given(request.getInputStream())
                .willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new ByteArrayInputStream(new byte[0])));
        return request;
    }

    @Test
    void shouldWorkingCapitalLoanExternalAndRescheduleMatch() {
        String externalId = UUID.randomUUID().toString();
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/working-capital-loans/12").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/working-capital-loans/12?correct=parameter").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/rescheduleworking-capital-loans/12").matches());
        Assertions.assertTrue(LOAN_PATH_PATTERN.matcher("/v1/working-capital-loans/external-id/" + externalId).matches());
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/working-capital-loans/12").replaceAll("$1"));
        Assertions.assertEquals("12", LOAN_PATH_PATTERN.matcher("/v1/rescheduleworking-capital-loans/12").replaceAll("$1"));
        Assertions.assertEquals(externalId,
                LOAN_PATH_PATTERN.matcher("/v1/working-capital-loans/external-id/" + externalId).replaceAll("$1"));
    }

    @Test
    void shouldProceedWhenUrlDoesNotMatch() throws ServletException, IOException {
        MockHttpServletRequest request = request("/v1/jobs/2/inline");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        testObj.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenUrlMatchesButLoanIdInvalid() throws ServletException, IOException {
        setBusinessDates();
        MockHttpServletRequest request = request("/v1/working-capital-loans/invalid2LoanId/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenUserHasBypassPermission() throws ServletException, IOException {
        MockHttpServletRequest request = request("/v1/working-capital-loans/2/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        AppUser appUser = mock(AppUser.class);
        given(context.authenticatedUser()).willReturn(appUser);
        given(appUser.isBypassUser()).willReturn(true);

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenLoanIsNotLockedAndNoLoanIsBehind() throws ServletException, IOException {
        setBusinessDates();
        MockHttpServletRequest request = request("/v1/working-capital-loans/2/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        LocalDate cobDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveIdService.retrieveLoanIdsBehindDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());
        given(retrieveIdService.retrieveLoanBehindOnDisbursementDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldProceedWhenExternalLoanIsNotLockedAndNotBehind() throws ServletException, IOException {
        setBusinessDates();
        String uuid = UUID.randomUUID().toString();
        MockHttpServletRequest request = request("/v1/working-capital-loans/external-id/" + uuid + "/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        LocalDate cobDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanRepository.findIdByExternalId(any())).willReturn(2L);
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveIdService.retrieveLoanIdsBehindDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());
        given(retrieveIdService.retrieveLoanBehindOnDisbursementDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldRunInlineCOBAndProceedWhenLoanIsBehind() throws ServletException, IOException {
        setBusinessDates();
        MockHttpServletRequest request = request("/v1/working-capital-loans/2?command=approve");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        LocalDate cobDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        COBIdAndLastClosedBusinessDate result = mock(COBIdAndLastClosedBusinessDate.class);
        given(result.getId()).willReturn(2L);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveIdService.retrieveLoanIdsBehindDate(eq(cobDate), anyList())).willReturn(Collections.singletonList(result));
        given(retrieveIdService.retrieveLoanBehindOnDisbursementDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(1)).execute(Collections.singletonList(2L), INLINE_WC_JOB);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldRunInlineCOBWhenLoanIsBehindOnDisbursementDateOnly() throws ServletException, IOException {
        setBusinessDates();
        MockHttpServletRequest request = request("/v1/working-capital-loans/2?command=disburse");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        LocalDate cobDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        COBIdAndLastClosedBusinessDate result = mock(COBIdAndLastClosedBusinessDate.class);
        given(result.getId()).willReturn(2L);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(false);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        // behind ONLY via the disbursement-date check; the last-closed check returns nothing
        given(retrieveIdService.retrieveLoanIdsBehindDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());
        given(retrieveIdService.retrieveLoanBehindOnDisbursementDate(eq(cobDate), anyList())).willReturn(Collections.singletonList(result));

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(1)).execute(Collections.singletonList(2L), INLINE_WC_JOB);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldNotRunInlineCOBAndProceedForLoanCreation() throws ServletException, IOException {
        MockHttpServletRequest request = request("/v1/working-capital-loans");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldNotRunInlineCOBForCatchUp() throws ServletException, IOException {
        MockHttpServletRequest request = request("/v1/working-capital-loans/catch-up");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));

        testObj.doFilterInternal(request, response, filterChain);

        verify(inlineLoanCOBExecutorService, times(0)).execute(anyList(), eq(INLINE_WC_JOB));
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldRejectWhenLoanIsHardLockedAndNotOverrulable() throws ServletException, IOException {
        MockHttpServletRequest request = request("/v1/working-capital-loans/2/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        PrintWriter writer = mock(PrintWriter.class);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(true);
        given(loanAccountLockService.isAnyLockOverrulable(List.of(2L))).willReturn(false);
        given(response.getWriter()).willReturn(writer);

        testObj.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpStatus.SC_CONFLICT);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldProceedWhenLoanIsHardLockedButOverrulable() throws ServletException, IOException {
        setBusinessDates();
        MockHttpServletRequest request = request("/v1/working-capital-loans/2/charges");
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        LocalDate cobDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        given(context.authenticatedUser()).willReturn(mock(AppUser.class));
        given(loanAccountLockService.isAnyLoanHardLocked(List.of(2L))).willReturn(true);
        given(loanAccountLockService.isAnyLockOverrulable(List.of(2L))).willReturn(true);
        given(fineractProperties.getQuery()).willReturn(fineractQueryProperties);
        given(fineractQueryProperties.getInClauseParameterSizeLimit()).willReturn(65000);
        given(retrieveIdService.retrieveLoanIdsBehindDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());
        given(retrieveIdService.retrieveLoanBehindOnDisbursementDate(eq(cobDate), anyList())).willReturn(Collections.emptyList());

        testObj.doFilterInternal(request, response, filterChain);

        verify(response, times(0)).setStatus(HttpStatus.SC_CONFLICT);
        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), eq(response));
    }

    @Test
    void shouldThrowAuthenticationCredentialsNotFoundException_WhenUnAuthenticatedUserExceptionIsThrown() throws IOException {
        WorkingCapitalLoanCOBFilterHelper spyHelper = spy(helper);
        testObj = new WorkingCapitalLoanCOBApiFilter(spyHelper);
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(request.getInputStream())
                .willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new ByteArrayInputStream(new byte[0])));
        doReturn(true).when(spyHelper).isOnApiList(any(BodyCachingHttpServletRequestWrapper.class));
        doThrow(new UnAuthenticatedUserException()).when(spyHelper).isBypassUser();

        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> testObj.doFilterInternal(request, response, filterChain));
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldProceed_WhenBypassUser() throws Exception {
        WorkingCapitalLoanCOBFilterHelper spyHelper = spy(helper);
        testObj = new WorkingCapitalLoanCOBApiFilter(spyHelper);
        MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        MockHttpServletResponse response = mock(MockHttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        given(request.getInputStream())
                .willReturn(new BodyCachingHttpServletRequestWrapper.CachedBodyServletInputStream(new ByteArrayInputStream(new byte[0])));
        doReturn(true).when(spyHelper).isOnApiList(any(BodyCachingHttpServletRequestWrapper.class));
        doReturn(true).when(spyHelper).isBypassUser();

        testObj.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
}
