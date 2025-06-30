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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class CallerIpTrackingFilter extends OncePerRequestFilter {

    private final FineractProperties fineractProperties;

    /**
     * Common headers used to get client IP from different proxies.
     *
     * "X-Forwarded-For", // Standard header used by proxies "Proxy-Client-IP", // Used by some Apache proxies
     * "WL-Proxy-Client-IP", // Used by WebLogic "HTTP_X_FORWARDED_FOR", // Alternative to X-Forwarded-For
     * "HTTP_X_FORWARDED", // Variation of X-Forwarded "HTTP_X_CLUSTER_CLIENT_IP", // Used in clustered environments
     * "HTTP_CLIENT_IP", // Fallback, less common "HTTP_FORWARDED_FOR", // Less standard, used in some setups
     * "HTTP_FORWARDED", // Standardized header (RFC 7239) that can include client IP, proxy info, and protocol
     * "HTTP_VIA", // Shows intermediate proxies "REMOTE_ADDR" // Server's perceived client IP
     */

    private static final String[] IP_HEADER_CANDIDATES = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };

    public String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !ip.isEmpty()) {
                log.trace("CALLER IP : {}", ip);
                return ip;
            }
        }
        log.trace("getRemoteAddr method : {}", request.getRemoteAddr());
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {
        if (fineractProperties.getIpTracking().isEnabled()) {
            handleClientIp(request, response, filterChain);
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private void handleClientIp(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            String clientIpAddress = getClientIpAddress(request);
            if (StringUtils.isNotBlank(clientIpAddress)) {
                log.trace("Found Client IP in header : {}", clientIpAddress);
                request.setAttribute("IP", clientIpAddress);
            }
            filterChain.doFilter(request, response);
        } finally {
            request.setAttribute("IP", "");
        }
    }

    @Override
    protected boolean isAsyncDispatch(final HttpServletRequest request) {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

}
