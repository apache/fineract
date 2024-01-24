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
package org.apache.fineract.infrastructure.security.filter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import org.apache.fineract.infrastructure.cache.domain.CacheType;
import org.apache.fineract.infrastructure.cache.service.CacheWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.PlatformRequestLog;
import org.apache.fineract.infrastructure.security.exception.InvalidTenantIdentifierException;
import org.apache.fineract.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 *
 * This filter is responsible for extracting multi-tenant from the request and setting Cross-Origin details to response.
 *
 * If multi-tenant are valid, the details of the tenant are stored in {@link FineractPlatformTenant} and stored in a
 * {@link ThreadLocal} variable for this request using {@link ThreadLocalContextUtil}.
 *
 * If multi-tenant are invalid, a http error response is returned.
 *
 * Used to support Oauth2 authentication and the service is loaded only when "oauth" profile is active.
 */
@RequiredArgsConstructor
@Slf4j
public class TenantAwareTenantIdentifierFilter extends GenericFilterBean {

    private static final AtomicBoolean FIRST_PROCESSED_REQUEST = new AtomicBoolean();

    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
    private final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CacheWritePlatformService cacheWritePlatformService;

    private final BusinessDateReadPlatformService businessDateReadPlatformService;

    private static final String TENANT_ID_REQUEST_HEADER = "Fineract-Platform-TenantId";
    private static final boolean EXCEPTION_IF_HEADER_MISSING = true;
    private static final String API_URI = "/api/v1/";

    @Override
    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final StopWatch task = new StopWatch();
        task.start();

        try {
            ThreadLocalContextUtil.reset();
            // allows for Cross-Origin
            // Requests (CORs) to be performed against the platform API.
            response.setHeader("Access-Control-Allow-Origin", "*"); // NOSONAR
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            final String reqHead = request.getHeader("Access-Control-Request-Headers");

            if (null != reqHead && !reqHead.isEmpty()) {
                response.setHeader("Access-Control-Allow-Headers", reqHead);
            }

            if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {

                String tenantIdentifier = request.getHeader(TENANT_ID_REQUEST_HEADER);
                if (org.apache.commons.lang3.StringUtils.isBlank(tenantIdentifier)) {
                    tenantIdentifier = request.getParameter("tenantIdentifier");
                }

                if (tenantIdentifier == null && EXCEPTION_IF_HEADER_MISSING) {
                    throw new InvalidTenantIdentifierException("No tenant identifier found: Add request header of '"
                            + TENANT_ID_REQUEST_HEADER + "' or add the parameter 'tenantIdentifier' to query string of request URL.");
                }

                String pathInfo = request.getRequestURI();
                boolean isReportRequest = false;
                if (pathInfo != null && pathInfo.contains("report")) {
                    isReportRequest = true;
                }
                final FineractPlatformTenant tenant = basicAuthTenantDetailsService.loadTenantById(tenantIdentifier, isReportRequest);
                ThreadLocalContextUtil.setTenant(tenant);
                HashMap<BusinessDateType, LocalDate> businessDates = businessDateReadPlatformService.getBusinessDates();
                ThreadLocalContextUtil.setBusinessDates(businessDates);
                String authToken = request.getHeader("Authorization");

                if (authToken != null && authToken.startsWith("bearer ")) {
                    ThreadLocalContextUtil.setAuthToken(authToken.replaceFirst("bearer ", ""));
                }

                if (!FIRST_PROCESSED_REQUEST.get()) {
                    final String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(),
                            request.getContextPath() + API_URI);
                    System.setProperty("baseUrl", baseUrl);

                    final boolean ehcacheEnabled = configurationDomainService.isEhcacheEnabled();
                    if (ehcacheEnabled) {
                        cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
                    } else {
                        cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
                    }
                    FIRST_PROCESSED_REQUEST.set(true);
                }
                chain.doFilter(request, response);
            }
        } catch (final InvalidTenantIdentifierException e) {
            // deal with exception at low level
            SecurityContextHolder.getContext().setAuthentication(null);

            response.addHeader("WWW-Authenticate", "Basic realm=\"" + "Fineract Platform API" + "\"");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            ThreadLocalContextUtil.reset();
            task.stop();
            final PlatformRequestLog logRequest = PlatformRequestLog.from(task, request);
            log.debug("{}", toApiJsonSerializer.serialize(logRequest));
        }

    }
}
