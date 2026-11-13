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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties.OidcIssuerProperties;
import org.apache.fineract.infrastructure.core.domain.TenantOidcConfig;
import org.apache.fineract.infrastructure.security.converter.FineractOidcJwtAuthenticationConverter;
import org.apache.fineract.infrastructure.security.domain.OidcFederationType;
import org.apache.fineract.infrastructure.security.service.TenantOidcConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Unit tests for {@link DynamicJwtIssuerAuthenticationManagerResolver}.
 *
 * <p>
 * JWT tokens are created as unsigned PlainJWT instances — only the 'iss' claim extraction is exercised here (no
 * signature verification). The actual NimbusJwtDecoder is built lazily (HTTP calls happen on first decode, not on
 * build), so no network calls occur in these tests as long as a non-null jwksUri is provided (skipping OIDC discovery).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicJwtIssuerAuthenticationManagerResolverTest {

    private static final String ISSUER_KEYCLOAK = "https://keycloak.example.com/realms/tenant-a";
    private static final String ISSUER_GOOGLE = "https://accounts.google.com";
    private static final String JWKS_URI = "https://keycloak.example.com/realms/tenant-a/protocol/openid-connect/certs";

    @Mock
    private TenantOidcConfigService tenantOidcConfigService;
    @Mock
    private FineractOidcJwtAuthenticationConverter jwtConverter;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractSecurityProperties securityProps;
    @Mock
    private FineractSecurityOidcFederationProperties oidcProps;

    private DynamicJwtIssuerAuthenticationManagerResolver resolver;

    @BeforeEach
    void setUp() {
        when(fineractProperties.getSecurity()).thenReturn(securityProps);
        when(securityProps.getOidcFederation()).thenReturn(oidcProps);
        when(oidcProps.getIssuers()).thenReturn(List.of());

        resolver = new DynamicJwtIssuerAuthenticationManagerResolver(
                tenantOidcConfigService, jwtConverter, fineractProperties);
    }

    // -------------------------------------------------------------------------
    // Priority 1: DB config
    // -------------------------------------------------------------------------

    @Test
    void resolvesAuthenticationManagerFromDbConfig() {
        TenantOidcConfig dbConfig = keycloakConfig();
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.of(dbConfig));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        // Should resolve without throwing (decoder built lazily, no HTTP call since jwksUri is set)
        var manager = resolver.resolve(request);

        assertThat(manager).isNotNull();
        verify(tenantOidcConfigService).findByIssuerUri(ISSUER_KEYCLOAK);
    }

    @Test
    void cachesAuthenticationManagerPerIssuer() {
        TenantOidcConfig dbConfig = keycloakConfig();
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.of(dbConfig));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        resolver.resolve(request);
        resolver.resolve(request);
        resolver.resolve(request);

        // DB is consulted only once; subsequent calls use the cached manager
        verify(tenantOidcConfigService, times(1)).findByIssuerUri(ISSUER_KEYCLOAK);
    }

    @Test
    void evictFromCacheForcesDatabaseReLookupOnNextRequest() {
        TenantOidcConfig dbConfig = keycloakConfig();
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.of(dbConfig));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        resolver.resolve(request);
        resolver.evictFromCache(ISSUER_KEYCLOAK);
        resolver.resolve(request);

        verify(tenantOidcConfigService, times(2)).findByIssuerUri(ISSUER_KEYCLOAK);
    }

    // -------------------------------------------------------------------------
    // Priority 2: YAML issuers[] fallback
    // -------------------------------------------------------------------------

    @Test
    void fallsBackToYamlIssuerWhenDbHasNoRecord() {
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.empty());

        OidcIssuerProperties yamlIssuer = yamlIssuer(ISSUER_KEYCLOAK, "tenant_a", JWKS_URI);
        when(oidcProps.getIssuers()).thenReturn(List.of(yamlIssuer));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        var manager = resolver.resolve(request);

        assertThat(manager).isNotNull();
        verify(tenantOidcConfigService).findByIssuerUri(ISSUER_KEYCLOAK);
    }

    @Test
    void correctYamlIssuerIsSelectedWhenMultipleAreConfigured() {
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_GOOGLE)).thenReturn(Optional.empty());

        OidcIssuerProperties keycloakYaml = yamlIssuer(ISSUER_KEYCLOAK, "tenant_a", JWKS_URI);
        OidcIssuerProperties googleYaml = yamlIssuer(ISSUER_GOOGLE, "tenant_b",
                "https://www.googleapis.com/oauth2/v3/certs");
        when(oidcProps.getIssuers()).thenReturn(List.of(keycloakYaml, googleYaml));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_GOOGLE);

        var manager = resolver.resolve(request);

        assertThat(manager).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Test
    void throwsOAuth2ExceptionWhenNoConfigFoundForIssuer() {
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.empty());
        when(oidcProps.getIssuers()).thenReturn(List.of());

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        assertThatThrownBy(() -> resolver.resolve(request))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode())
                        .isEqualTo("unknown_issuer"));
    }

    @Test
    void throwsOAuth2ExceptionWhenRequestHasNoBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/clients");

        assertThatThrownBy(() -> resolver.resolve(request)).isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode()).isEqualTo("missing_issuer"));
    }

    @Test
    void throwsOAuth2ExceptionWhenTokenHasNoIssClaim() {
        // JWT without 'iss' claim
        String tokenWithoutIss = buildPlainJwt(null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenWithoutIss);

        assertThatThrownBy(() -> resolver.resolve(request)).isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(e -> assertThat(((OAuth2AuthenticationException) e).getError().getErrorCode()).isEqualTo("missing_issuer"));
    }

    // -------------------------------------------------------------------------
    // DB config takes priority over YAML
    // -------------------------------------------------------------------------

    @Test
    void dbConfigTakesPriorityOverYamlConfig() {
        TenantOidcConfig dbConfig = keycloakConfig();
        when(tenantOidcConfigService.findByIssuerUri(ISSUER_KEYCLOAK)).thenReturn(Optional.of(dbConfig));

        OidcIssuerProperties yamlIssuer = yamlIssuer(ISSUER_KEYCLOAK, "other_tenant", JWKS_URI);
        when(oidcProps.getIssuers()).thenReturn(List.of(yamlIssuer));

        MockHttpServletRequest request = requestWithBearerToken(ISSUER_KEYCLOAK);

        // Should succeed using DB config (yamlIssuer would also match but DB is checked first)
        var manager = resolver.resolve(request);

        assertThat(manager).isNotNull();
        verify(tenantOidcConfigService).findByIssuerUri(ISSUER_KEYCLOAK);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private MockHttpServletRequest requestWithBearerToken(String issuer) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/clients");
        request.addHeader("Authorization", "Bearer " + buildPlainJwt(issuer));
        return request;
    }

    private String buildPlainJwt(String issuer) {
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder().subject("user-123")
                .expirationTime(new Date(System.currentTimeMillis() + 3600_000));
        if (issuer != null) {
            claimsBuilder.issuer(issuer);
        }
        return new PlainJWT(claimsBuilder.build()).serialize();
    }

    private TenantOidcConfig keycloakConfig() {
        return TenantOidcConfig.builder().id(1L).tenantId("tenant_a").providerType(OidcFederationType.KEYCLOAK).issuerUri(ISSUER_KEYCLOAK)
                .clientId("fineract-client").clientSecret("enc-secret").jwksUri(JWKS_URI) // explicit jwksUri avoids
                                                                                          // OIDC discovery HTTP call
                .usernameClaim("preferred_username").scopes("openid,profile,email").enabled(true).build();
    }

    private OidcIssuerProperties yamlIssuer(String issuerUri, String tenantId, String jwksUri) {
        OidcIssuerProperties props = new OidcIssuerProperties();
        props.setIssuerUri(issuerUri);
        props.setTenantId(tenantId);
        props.setJwksUri(jwksUri);
        return props;
    }

    @Test
    void acceptsAccessTokenTypeOfRfc9068() {
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK).validate(jwt("at+jwt", ISSUER_KEYCLOAK))
                .hasErrors()).isFalse();
    }

    @Test
    void acceptsAccessTokenTypeOfRfc9068AsFullMediaType() {
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK)
                .validate(jwt("application/at+jwt", ISSUER_KEYCLOAK)).hasErrors()).isFalse();
    }

    @Test
    void acceptsJwtTypeAndAbsentType() {
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK).validate(jwt("JWT", ISSUER_KEYCLOAK))
                .hasErrors()).isFalse();
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK).validate(jwt(null, ISSUER_KEYCLOAK))
                .hasErrors()).isFalse();
    }

    @Test
    void rejectsOtherTokenTypes() {
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK).validate(jwt("dpop+jwt", ISSUER_KEYCLOAK))
                .hasErrors()).isTrue();
    }

    @Test
    void rejectsTokenOfAnotherIssuer() {
        assertThat(DynamicJwtIssuerAuthenticationManagerResolver.tokenValidator(ISSUER_KEYCLOAK).validate(jwt("at+jwt", ISSUER_GOOGLE))
                .hasErrors()).isTrue();
    }

    private static Jwt jwt(String type, String issuer) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "ES256").issuer(issuer).subject("user").issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
        return (type == null ? builder : builder.header("typ", type)).build();
    }
}
