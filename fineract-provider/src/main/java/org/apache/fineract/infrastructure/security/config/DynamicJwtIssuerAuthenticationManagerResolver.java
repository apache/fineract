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
package org.apache.fineract.infrastructure.security.config;

import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties.OidcIssuerProperties;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.security.converter.FineractOidcJwtAuthenticationConverter;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTypeValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

/**
 * Resolves an {@link AuthenticationManager} per incoming request by matching the JWT {@code iss} claim against:
 * <ol>
 * <li>Per-tenant OIDC config stored in {@code m_tenant_oidc_config} (master DB, via
 * {@link TenantOidcConfigService}).</li>
 * <li>Static {@code issuers[]} list from {@code application.yml} — fallback for dev/CI environments.</li>
 * </ol>
 *
 * <p>
 * Built {@link AuthenticationManager} instances are cached in memory keyed by {@code issuerUri}. The cache entry is
 * invalidated via {@link #evictFromCache(String)} when the DB config changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("fineract.security.oidc-federation.enabled")
public class DynamicJwtIssuerAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private final TenantOidcConfigService tenantOidcConfigService;
    private final FineractOidcJwtAuthenticationConverter jwtConverter;
    private final FineractProperties fineractProperties;

    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    // RFC 9068 section 4: "at+jwt" or "application/at+jwt"
    private static final List<String> ACCEPTED_TOKEN_TYPES = List.of("JWT", "at+jwt", "application/at+jwt");

    // issuerUri → AuthenticationManager (cached after first build)
    private final ConcurrentHashMap<String, AuthenticationManager> managerCache = new ConcurrentHashMap<>();

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String issuer = extractIssuerUnchecked(request);
        if (issuer == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_issuer", "Cannot determine JWT issuer from request", null));
        }
        return managerCache.computeIfAbsent(issuer, this::buildAuthenticationManager);
    }

    private AuthenticationManager buildAuthenticationManager(String issuerUri) {
        // Priority 1: DB-configured per-tenant IdP
        Optional<TenantOidcConfig> dbConfig = tenantOidcConfigService.findByIssuerUri(issuerUri);
        if (dbConfig.isPresent()) {
            log.debug("Building AuthenticationManager from DB config for issuer: {}", issuerUri);
            return buildFromDbConfig(dbConfig.get());
        }

        // Priority 2: YAML issuers[] static fallback
        List<OidcIssuerProperties> yamlIssuers = fineractProperties.getSecurity().getOidcFederation().getIssuers();
        if (yamlIssuers != null) {
            Optional<OidcIssuerProperties> yamlMatch = yamlIssuers.stream().filter(i -> issuerUri.equals(i.getIssuerUri())).findFirst();
            if (yamlMatch.isPresent()) {
                log.debug("Building AuthenticationManager from YAML config for issuer: {}", issuerUri);
                return buildFromYamlIssuer(yamlMatch.get());
            }
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("unknown_issuer", "No OIDC configuration found for issuer: " + issuerUri, null));
    }

    private AuthenticationManager buildFromDbConfig(TenantOidcConfig config) {
        return authenticationManagerFor(config.getIssuerUri(), config.getJwksUri(), defaultAudience());
    }

    private AuthenticationManager buildFromYamlIssuer(OidcIssuerProperties issuerProps) {
        String audience = issuerProps.getAudience() != null && !issuerProps.getAudience().isBlank() ? issuerProps.getAudience()
                : defaultAudience();
        return authenticationManagerFor(issuerProps.getIssuerUri(), issuerProps.getJwksUri(), audience);
    }

    private String defaultAudience() {
        return fineractProperties.getSecurity().getOidcFederation().getAudience();
    }

    private AuthenticationManager authenticationManagerFor(String issuerUri, String jwksUri, String audience) {
        NimbusJwtDecoder decoder = jwksUri != null && !jwksUri.isBlank() ? NimbusJwtDecoder.withJwkSetUri(jwksUri).build()
                : NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
        decoder.setJwtValidator(tokenValidator(issuerUri, audience));

        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(jwtConverter);
        return provider::authenticate;
    }

    /**
     * Spring Security's default validators with two changes: besides {@code JWT} (or no type at all) the JOSE header's
     * {@code typ} may be {@code at+jwt} or {@code application/at+jwt}, the type of an RFC 9068 access token; and with
     * an audience configured, the token must name it in its {@code aud} claim, so that a token issued for another
     * service is refused.
     */
    static OAuth2TokenValidator<Jwt> tokenValidator(String issuerUri, String audience) {
        JwtTypeValidator typeValidator = new JwtTypeValidator(ACCEPTED_TOKEN_TYPES);
        typeValidator.setAllowEmpty(true);
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>(List.of(new JwtIssuerValidator(issuerUri), typeValidator));
        if (audience != null && !audience.isBlank()) {
            validators.add(new JwtAudienceValidator(audience));
        }
        return JwtValidators.createDefaultWithValidators(validators);
    }

    private String extractIssuerUnchecked(HttpServletRequest request) {
        try {
            String token = bearerTokenResolver.resolve(request);
            if (token == null) {
                return null;
            }
            return (String) JWTParser.parse(token).getJWTClaimsSet().getClaim("iss");
        } catch (Exception e) {
            log.debug("Could not extract 'iss' claim from Bearer token", e);
            return null;
        }
    }

    /**
     * Removes a cached {@link AuthenticationManager} for the given issuer URI. Call this after updating or deleting a
     * {@code m_tenant_oidc_config} record so the next request rebuilds the decoder from the fresh DB state.
     */
    public void evictFromCache(String issuerUri) {
        managerCache.remove(issuerUri);
        log.info("Evicted AuthenticationManager cache for issuer: {}", issuerUri);
    }
}
