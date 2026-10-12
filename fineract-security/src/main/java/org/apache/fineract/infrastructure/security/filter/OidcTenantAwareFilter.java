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

import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties.OidcIssuerProperties;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.AuthTenantDetailsService;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Resolves the Fineract tenant from an incoming request and sets it in {@link ThreadLocalContextUtil} before Spring
 * Security validates the JWT signature.
 *
 * <p>
 * Resolution priority:
 * <ol>
 * <li>A configurable claim inside the Bearer JWT (parsed without signature verification)</li>
 * <li>The {@code Fineract-Platform-TenantId} HTTP header</li>
 * <li>The {@code tenantIdentifier} query parameter</li>
 * </ol>
 *
 * <p>
 * If no tenant can be resolved the filter does not block the request — downstream authentication will fail with an
 * appropriate error if the tenant context is required.
 */
@Slf4j
@RequiredArgsConstructor
public class OidcTenantAwareFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "Fineract-Platform-TenantId";
    private static final String TENANT_PARAM = "tenantIdentifier";

    private final BearerTokenResolver bearerTokenResolver;
    private final AuthTenantDetailsService tenantDetailsService;
    private final FineractProperties fineractProperties;
    private final TenantOidcConfigService tenantOidcConfigService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = resolveTenantId(request);
            if (tenantId != null) {
                ThreadLocalContextUtil.setTenant(tenantDetailsService.loadTenantById(tenantId, false));
                log.debug("OIDC tenant context set to '{}' for {}", tenantId, request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("OIDC tenant resolution failed for '{}', request will continue without tenant context", request.getRequestURI(), e);
            filterChain.doFilter(request, response);
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String token = null;
        try {
            token = bearerTokenResolver.resolve(request);
        } catch (Exception e) {
            log.debug("Could not extract Bearer token for tenant resolution", e);
        }

        if (token != null) {
            try {
                var claims = JWTParser.parse(token).getJWTClaimsSet();
                String issuer = (String) claims.getClaim("iss");

                // Priority 1: iss → master DB (m_tenant_oidc_config)
                if (issuer != null) {
                    var dbConfig = tenantOidcConfigService.findByIssuerUri(issuer);
                    if (dbConfig.isPresent()) {
                        return dbConfig.get().getTenantId();
                    }
                }

                // Priority 2: iss → YAML issuers[] static config
                if (issuer != null) {
                    var yamlIssuers = fineractProperties.getSecurity().getOidcFederation().getIssuers();
                    if (yamlIssuers != null) {
                        var match = yamlIssuers.stream().filter(i -> issuer.equals(i.getIssuerUri())).map(OidcIssuerProperties::getTenantId)
                                .findFirst();
                        if (match.isPresent()) {
                            return match.get();
                        }
                    }
                }

                // Priority 3: configurable custom claim (e.g. fineract_tenant) — legacy/single-realm
                String claimName = fineractProperties.getSecurity().getOidcFederation().getTenantClaimName();
                if (claimName != null) {
                    String fromClaim = (String) claims.getClaim(claimName);
                    if (fromClaim != null) {
                        return fromClaim;
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to parse JWT for tenant resolution, falling through to header/param", e);
            }
        }

        // Priority 4: HTTP header
        String fromHeader = request.getHeader(TENANT_HEADER);
        if (fromHeader != null) {
            return fromHeader;
        }

        // Priority 5: query parameter
        return request.getParameter(TENANT_PARAM);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/actuator") || path.equals("/login") || path.equals("/error");
    }
}
