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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.service.IpAddressUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IpTrackingFilterTest {

    private static final String[] IP_HEADER_CANDIDATES = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private FineractProperties fineractProperties;
    private CallerIpTrackingFilter underTest;

    @BeforeEach
    public void setup() {
        fineractProperties = new FineractProperties();
        FineractProperties.FineractIpTrackingProperties props = new FineractProperties.FineractIpTrackingProperties();
        props.setEnabled(true);
        fineractProperties.setIpTracking(props);
        underTest = new CallerIpTrackingFilter(fineractProperties);
    }

    @ParameterizedTest
    @ValueSource(strings = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" })
    void testGetClientIp_UsesCorrectHeaderEnabled(String headerName) throws ServletException, IOException {
        // given
        given(request.getHeader(headerName)).willReturn("192.168.1.100");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(request, times(1)).setAttribute("IP", "192.168.1.100");
        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" })
    void testGetClientIpAddress_UsesCorrectHeaderDisabled(String headerName) throws ServletException, IOException {
        // given
        FineractProperties.FineractIpTrackingProperties props = new FineractProperties.FineractIpTrackingProperties();
        props.setEnabled(false);
        fineractProperties.setIpTracking(props);
        underTest = new CallerIpTrackingFilter(fineractProperties);

        given(request.getHeader(headerName)).willReturn("192.168.1.100");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(request, never()).setAttribute("IP", "192.168.1.100");
        verify(filterChain).doFilter(request, response);

    }

    @ParameterizedTest
    @ValueSource(strings = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" })
    void testGetClientIpAddress_UsesCorrectHeaderIpAdressUtilsEnable(String headerName) throws ServletException, IOException {
        // given
        given(request.getHeader(headerName)).willReturn("192.168.1.100");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(request, times(1)).setAttribute("IP", IpAddressUtils.getClientIp());
        verify(filterChain).doFilter(request, response);

    }

    @ParameterizedTest
    @ValueSource(strings = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" })
    void testGetClientIpAddress_UsesCorrectHeaderIpAdressUtilsDisabled(String headerName) throws ServletException, IOException {
        // given
        FineractProperties.FineractIpTrackingProperties props = new FineractProperties.FineractIpTrackingProperties();
        props.setEnabled(false);
        fineractProperties.setIpTracking(props);
        underTest = new CallerIpTrackingFilter(fineractProperties);
        given(request.getHeader(headerName)).willReturn("192.168.1.100");

        // when
        underTest.doFilterInternal(request, response, filterChain);

        // then
        verify(request, never()).setAttribute("IP", IpAddressUtils.getClientIp());
        verify(filterChain).doFilter(request, response);

    }

}
