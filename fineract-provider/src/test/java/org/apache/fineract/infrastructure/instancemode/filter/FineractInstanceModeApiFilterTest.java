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
package org.apache.fineract.infrastructure.instancemode.filter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.instancemode.InstanceModeMock;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FineractInstanceModeApiFilterTest {

    @Mock
    private FineractProperties fineractProperties;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter outputWriter;

    @InjectMocks
    private FineractInstanceModeApiFilter underTest;

    @BeforeEach
    void setUp() throws IOException {
        given(response.getWriter()).willReturn(outputWriter);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenFineractIsInAllModeAndIsGetApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, true, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenFineractIsInReadOnlyModeAndIsGetApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenFineractIsInReadOnlyModeAndIsHealthApi()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetWriteApisThrough_WhenFineractIsInReadOnlyModeAndIsPostApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetBatchApisThrough_WhenFineractIsInReadOnlyModeAndIsJobsApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenFineractIsInWriteModeAndIsGetApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetWriteApisThrough_WhenFineractIsInWriteModeAndIsPostApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetWriteApisThrough_WhenFineractIsInWriteModeAndIsPutApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.PUT.name());
        given(request.getPathInfo()).willReturn("/v1/loans/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenFineractIsInWriteModeAndIsHelathApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetBatchApisThrough_WhenFineractIsInWriteModeAndIsJobsApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchApisThrough_WhenFineractIsInBatchModeAndIsJobsApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/jobs/1");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchApisThrough_WhenFineractIsInBatchModeAndIsListingJobsApi()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/jobs");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetActuatorApisThrough_WhenFineractIsInBatchModeAndIsHealthApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getServletPath()).willReturn("/actuator/health");
        given(request.getPathInfo()).willReturn(null);
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenFineractIsInReadMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenFineractIsInWriteMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, true, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetBatchesApisThrough_WhenFineractIsInBatchMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/batches");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetLoanCOBCatchUpApiThrough_WhenFineractIsInBatchManagerMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/catch-up");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetLoanCOBCatchUpApiThrough_WhenFineractIsNotInBatchManagerMode()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/catch-up");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetLoanCOBCatchUpStatusApiThrough_WhenFineractIsInBatchManagerMode()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/is-catch-up-running");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetLoanCOBCatchUpStatusApiThrough_WhenFineractIsNotInBatchManagerMode()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, true, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/loans/is-catch-up-running");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }

    @Test
    void testDoFilterInternal_ShouldLetOtherLoanCatchUpApisThrough_WhenFineractIsInBatchManagerAndReadModeAndIsGetApi()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans/oldest-cob-closed");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetOtherLoanCatchUpApisThrough_WhenFineractIsInReadModeAndIsGetApi()
            throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, false, false, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/v1/loans/oldest-cob-closed");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetSchedulerApiThrough_WhenFineractIsInBatchManagerMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(false, false, false, true);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/scheduler?command=start");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_ShouldNotLetSchedulerApiThrough_WhenFineractIsNotInBatchManagerMode() throws ServletException, IOException {
        // given
        FineractProperties.FineractModeProperties modeProperties = InstanceModeMock.createModeProps(true, true, true, false);
        given(fineractProperties.getMode()).willReturn(modeProperties);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        given(request.getPathInfo()).willReturn("/v1/scheduler?command=start");
        // when
        underTest.doFilterInternal(request, response, filterChain);
        // then
        verifyNoInteractions(filterChain);
        verify(response).setStatus(HttpStatus.SC_METHOD_NOT_ALLOWED);
    }
}
