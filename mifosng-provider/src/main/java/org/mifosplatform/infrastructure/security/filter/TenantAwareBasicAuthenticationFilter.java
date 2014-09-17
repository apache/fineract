/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.StopWatch;
import org.mifosplatform.infrastructure.cache.domain.CacheType;
import org.mifosplatform.infrastructure.cache.service.CacheWritePlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.mifosplatform.infrastructure.security.data.PlatformRequestLog;
import org.mifosplatform.infrastructure.security.exception.InvalidTenantIdentiferException;
import org.mifosplatform.infrastructure.security.service.BasicAuthTenantDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Service;

/**
 * A customised version of spring security's {@link BasicAuthenticationFilter}.
 * 
 * This filter is responsible for extracting multi-tenant and basic auth
 * credentials from the request and checking that the details provided are
 * valid.
 * 
 * If multi-tenant and basic auth credentials are valid, the details of the
 * tenant are stored in {@link MifosPlatformTenant} and stored in a
 * {@link ThreadLocal} variable for this request using
 * {@link ThreadLocalContextUtil}.
 * 
 * If multi-tenant and basic auth credentials are invalid, a http error response
 * is returned.
 */
@Service(value = "basicAuthenticationProcessingFilter")
public class TenantAwareBasicAuthenticationFilter extends BasicAuthenticationFilter {

    private static boolean firstRequestProcessed = false;
    private final static Logger logger = LoggerFactory.getLogger(TenantAwareBasicAuthenticationFilter.class);
    
    private final BasicAuthTenantDetailsService basicAuthTenantDetailsService;
    private final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CacheWritePlatformService cacheWritePlatformService;

    private final String tenantRequestHeader = "X-Mifos-Platform-TenantId";
    private final boolean exceptionIfHeaderMissing = true;

    @Autowired
    public TenantAwareBasicAuthenticationFilter(final AuthenticationManager authenticationManager,
            final AuthenticationEntryPoint authenticationEntryPoint, final BasicAuthTenantDetailsService basicAuthTenantDetailsService,
            final ToApiJsonSerializer<PlatformRequestLog> toApiJsonSerializer, final ConfigurationDomainService configurationDomainService,
            final CacheWritePlatformService cacheWritePlatformService) {
        super(authenticationManager, authenticationEntryPoint);
        this.basicAuthTenantDetailsService = basicAuthTenantDetailsService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.configurationDomainService = configurationDomainService;
        this.cacheWritePlatformService = cacheWritePlatformService;
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        final StopWatch task = new StopWatch();
        task.start();

        try {

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                // ignore to allow 'preflight' requests from AJAX applications
                // in different origin (domain name)
            } else {

                String tenantIdentifier = request.getHeader(this.tenantRequestHeader);
                if (org.apache.commons.lang.StringUtils.isBlank(tenantIdentifier)) {
                    tenantIdentifier = request.getParameter("tenantIdentifier");
                }

                if (tenantIdentifier == null && this.exceptionIfHeaderMissing) { throw new InvalidTenantIdentiferException(
                        "No tenant identifier found: Add request header of '" + this.tenantRequestHeader
                                + "' or add the parameter 'tenantIdentifier' to query string of request URL."); }

                // check tenants database for tenantId
                final MifosPlatformTenant tenant = this.basicAuthTenantDetailsService.loadTenantById(tenantIdentifier);

                ThreadLocalContextUtil.setTenant(tenant);
                String authToken = request.getHeader("Authorization");

                if (authToken != null && authToken.startsWith("Basic ")) {
                    ThreadLocalContextUtil.setAuthToken(authToken.replaceFirst("Basic ", ""));
                }

                if (!firstRequestProcessed) {
                	final String baseUrl = request.getRequestURL().toString().replace(request.getPathInfo(), "/");
                	System.setProperty("baseUrl", baseUrl);
                	
                    final boolean ehcacheEnabled = this.configurationDomainService.isEhcacheEnabled();
                    if (ehcacheEnabled) {
                        this.cacheWritePlatformService.switchToCache(CacheType.SINGLE_NODE);
                    } else {
                        this.cacheWritePlatformService.switchToCache(CacheType.NO_CACHE);
                    }
                    TenantAwareBasicAuthenticationFilter.firstRequestProcessed = true;
                }
            }
            
            super.doFilter(req, res, chain);
        } catch (final InvalidTenantIdentiferException e) {
            // deal with exception at low level
            SecurityContextHolder.getContext().setAuthentication(null);

            response.addHeader("WWW-Authenticate", "Basic realm=\"" + "Mifos Platform API" + "\"");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            task.stop();
            final PlatformRequestLog log = PlatformRequestLog.from(task, request);
            logger.info(this.toApiJsonSerializer.serialize(log));
        }
    }
}