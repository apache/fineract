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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        given(request.getPathInfo()).willReturn("/loans");
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
        given(request.getPathInfo()).willReturn("/loans");
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
        given(request.getPathInfo()).willReturn("/loans");
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
        given(request.getPathInfo()).willReturn("/jobs/1");
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
        given(request.getPathInfo()).willReturn("/loans");
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
        given(request.getPathInfo()).willReturn("/loans");
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
        given(request.getPathInfo()).willReturn("/loans/1");
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
        given(request.getPathInfo()).willReturn("/jobs/1");
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
        given(request.getPathInfo()).willReturn("/jobs/1");
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
        given(request.getPathInfo()).willReturn("/jobs");
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
}
