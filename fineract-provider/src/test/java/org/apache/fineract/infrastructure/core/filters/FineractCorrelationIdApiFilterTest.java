package org.apache.fineract.infrastructure.core.filters;

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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.filters.CorrelationHeaderFilter;
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
class FineractCorrelationIdApiFilterTest {

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
    private CorrelationHeaderFilter underTest;

    @BeforeEach
    void setUp() throws IOException {
        given(response.getWriter()).willReturn(outputWriter);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenFineractIsInCorrelationAndIsGetApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractCorrelationProperties correlationProperties = CorrelationMock.createCorrelationProps(true, "X-Correlation-ID");
        given(fineractProperties.getCorrelation()).willReturn(correlationProperties);        
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/loans");
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);        
        mutableRequest.putHeader(correlationProperties.getHeaderName(), "123456ABCDEF");
        // when
        underTest.doFilterInternal(mutableRequest, response, filterChain);
        // then
        verify(filterChain).doFilter(mutableRequest, response);
    }

    @Test
    void testDoFilterInternal_ShouldLetReadApisThrough_WhenFineractIsNotInCorrelationAndIsGetApi() throws ServletException, IOException {
        // given
        FineractProperties.FineractCorrelationProperties correlationProperties = CorrelationMock.createCorrelationProps(false, "X-Correlation-ID");
        given(fineractProperties.getCorrelation()).willReturn(correlationProperties);        
        given(request.getMethod()).willReturn(HttpMethod.GET.name());
        given(request.getPathInfo()).willReturn("/loans");
        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);        
        mutableRequest.putHeader(correlationProperties.getHeaderName(), "123456ABCDEF");
        // when
        underTest.doFilterInternal(mutableRequest, response, filterChain);
        // then
        verify(filterChain).doFilter(mutableRequest, response);
    }

   
}
