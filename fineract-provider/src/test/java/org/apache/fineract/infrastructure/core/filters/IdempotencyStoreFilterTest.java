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
package org.apache.fineract.infrastructure.core.filters;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyStoreFilterTest {

    private static final String IDEMPOTENCY_KEY_HEADER_NAME = "Idempotency-Key";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private FineractRequestContextHolder fineractRequestContextHolder;

    @Mock
    private IdempotencyStoreHelper helper;

    private FineractProperties fineractProperties;

    private IdempotencyStoreFilter underTest;

    @BeforeEach
    void setUp() {
        fineractProperties = new FineractProperties();
        fineractProperties.setIdempotencyKeyHeaderName(IDEMPOTENCY_KEY_HEADER_NAME);
        underTest = new IdempotencyStoreFilter(fineractRequestContextHolder, helper, fineractProperties);
    }

    @Test
    public void testDoFilterInternalShouldNotWarnWhenValidationIsDisabled() throws Exception {
        // given
        fineractProperties.setIdempotencyKeyValidationEnabled(false);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        // helper.isAllowedContentTypeRequest is called once, for the wrapper decision; the header-read
        // path reuses that result rather than calling it again
        verify(helper, times(1)).isAllowedContentTypeRequest(request);
        verify(request, times(1)).getHeader(IDEMPOTENCY_KEY_HEADER_NAME);
        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
    }

    @Test
    public void testDoFilterInternalShouldNotWarnWhenContentTypeIsNotAllowed() throws Exception {
        // given
        fineractProperties.setIdempotencyKeyValidationEnabled(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(false);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(helper, times(1)).isAllowedContentTypeRequest(request);
        verify(request, times(1)).getHeader(IDEMPOTENCY_KEY_HEADER_NAME);
        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
    }

    @Test
    public void testDoFilterInternalShouldNotWarnWhenHeaderIsPresent() throws Exception {
        // given
        fineractProperties.setIdempotencyKeyValidationEnabled(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn("some-idempotency-key");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(request, times(1)).getHeader(IDEMPOTENCY_KEY_HEADER_NAME);
        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
    }

    @Test
    public void testDoFilterInternalShouldCompleteNormallyWhenValidationEnabledAndHeaderMissing() throws Exception {
        // given
        fineractProperties.setIdempotencyKeyValidationEnabled(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        // the warning is logged inline in the header-read path; behaviourally the request still
        // proceeds normally through the filter chain
        verify(request, times(1)).getHeader(IDEMPOTENCY_KEY_HEADER_NAME);
        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
    }
}
