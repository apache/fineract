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
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractRequestContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IdempotencyStoreFilterTest {

    private static final String IDEMPOTENCY_KEY_HEADER_NAME = "Idempotency-Key";

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private IdempotencyStoreHelper helper;

    @Mock
    private FineractRequestContextHolder fineractRequestContextHolder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;
    private FineractProperties fineractProperties;
    private IdempotencyStoreFilter underTest;

    @BeforeEach
    public void setUp() {
        fineractProperties = new FineractProperties();
        fineractProperties.setIdempotencyKeyHeaderName(IDEMPOTENCY_KEY_HEADER_NAME);
        underTest = new IdempotencyStoreFilter(fineractRequestContextHolder, helper, fineractProperties, configurationDomainService);

        logger = (Logger) LoggerFactory.getLogger(IdempotencyStoreFilter.class.getName());
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    public void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    public void testDoFilterInternalShouldNotLogWarningWhenValidationIsDisabled() throws Exception {
        given(configurationDomainService.isIdempotencyValidationEnabled()).willReturn(false);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        underTest.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
        Assertions.assertFalse(hasMissingIdempotencyKeyHeaderWarning());
    }

    @Test
    public void testDoFilterInternalShouldLogWarningWhenValidationIsEnabledAndHeaderIsMissing() throws Exception {
        given(configurationDomainService.isIdempotencyValidationEnabled()).willReturn(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        underTest.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
        Assertions.assertTrue(hasMissingIdempotencyKeyHeaderWarning());
    }

    @Test
    public void testDoFilterInternalShouldNotLogWarningWhenValidationIsEnabledAndHeaderIsPresent() throws Exception {
        given(configurationDomainService.isIdempotencyValidationEnabled()).willReturn(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(true);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn("client-key");

        underTest.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
        Assertions.assertFalse(hasMissingIdempotencyKeyHeaderWarning());
    }

    @Test
    public void testDoFilterInternalShouldNotLogWarningWhenRequestContentTypeIsNotAllowed() throws Exception {
        given(configurationDomainService.isIdempotencyValidationEnabled()).willReturn(true);
        given(helper.isAllowedContentTypeRequest(request)).willReturn(false);
        given(request.getHeader(IDEMPOTENCY_KEY_HEADER_NAME)).willReturn(null);

        underTest.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(eq(request), any(HttpServletResponse.class));
        Assertions.assertFalse(hasMissingIdempotencyKeyHeaderWarning());
    }

    private boolean hasMissingIdempotencyKeyHeaderWarning() {
        return listAppender.list.stream().anyMatch(event -> event.getLevel().equals(Level.WARN) && event.getFormattedMessage().equals(
                "Idempotency key header [Idempotency-Key] is missing. Clients should provide it to avoid unintended duplicate command processing."));
    }
}
