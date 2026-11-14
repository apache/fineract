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
package org.apache.fineract.infrastructure.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties.OidcIssuerProperties;
import org.apache.fineract.infrastructure.security.data.OidcTokenRoles;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;

class FineractOidcUserServiceTest {

    private static final String ISSUER = "https://idp.example.com/realms/a";
    private static final String OTHER_ISSUER = "https://idp.example.com/realms/b";

    private final OidcIssuerProperties issuer = new OidcIssuerProperties();
    private final OidcAppUserResolutionService resolutionService = mock(OidcAppUserResolutionService.class);
    private FineractOidcUserService service;

    @BeforeEach
    void setUp() {
        issuer.setIssuerUri(ISSUER);
        OidcIssuerProperties otherIssuer = new OidcIssuerProperties();
        otherIssuer.setIssuerUri(OTHER_ISSUER);
        FineractSecurityOidcFederationProperties oidcProperties = new FineractSecurityOidcFederationProperties();
        oidcProperties.setIssuers(List.of(issuer, otherIssuer));
        FineractSecurityProperties securityProperties = new FineractSecurityProperties();
        securityProperties.setOidcFederation(oidcProperties);
        FineractProperties fineractProperties = new FineractProperties();
        fineractProperties.setSecurity(securityProperties);
        service = new FineractOidcUserService(resolutionService, fineractProperties);
    }

    @Test
    void tokenDoesNotDecideRolesWithoutRolesClaimOnItsIssuer() {
        assertThat(service.extractRoles(jwt(ISSUER, "roles", List.of("Teller"))).decides()).isFalse();
    }

    @Test
    void tokenOfAnotherIssuerDoesNotDecideRoles() {
        issuer.setRolesClaim("roles");

        assertThat(service.extractRoles(jwt(OTHER_ISSUER, "roles", List.of("Teller"))).decides()).isFalse();
        assertThat(service.extractRoles(jwt("https://unknown.example.com", "roles", List.of("Teller"))).decides()).isFalse();
    }

    @Test
    void tokenWithoutTheClaimDoesNotDecideRoles() {
        issuer.setRolesClaim("realm_access.roles");

        assertThat(service.extractRoles(jwt(ISSUER, "other", "x")).decides()).isFalse();
        assertThat(service.extractRoles(jwt(ISSUER, "realm_access", Map.of("groups", List.of("a")))).decides()).isFalse();
    }

    @Test
    void claimThatIsNeitherListNorStringDoesNotDecideRoles() {
        issuer.setRolesClaim("roles");

        assertThat(service.extractRoles(jwt(ISSUER, "roles", 42)).decides()).isFalse();
    }

    @Test
    void emptyClaimDecidesThatTheUserHasNoRoles() {
        issuer.setRolesClaim("roles");

        OidcTokenRoles roles = service.extractRoles(jwt(ISSUER, "roles", List.of()));

        assertThat(roles.decides()).isTrue();
        assertThat(roles.names()).isEmpty();
    }

    @Test
    void rolesFromListClaim() {
        issuer.setRolesClaim("roles");

        OidcTokenRoles roles = service.extractRoles(jwt(ISSUER, "roles", List.of("Teller", "Branch Manager")));

        assertThat(roles.decides()).isTrue();
        assertThat(roles.issuer()).isEqualTo(ISSUER);
        assertThat(roles.names()).containsExactlyInAnyOrder("Teller", "Branch Manager");
    }

    @Test
    void rolesFromCommaSeparatedClaim() {
        issuer.setRolesClaim("roles");

        assertThat(service.extractRoles(jwt(ISSUER, "roles", "Teller, Branch Manager,")).names()).containsExactlyInAnyOrder("Teller",
                "Branch Manager");
    }

    @Test
    void rolesFromNestedClaim() {
        issuer.setRolesClaim("realm_access.roles");

        assertThat(service.extractRoles(jwt(ISSUER, "realm_access", Map.of("roles", List.of("Teller")))).names()).containsExactly("Teller");
    }

    @Test
    void literalClaimNameWinsOverNestedPath() {
        issuer.setRolesClaim("https://example.com/roles.v1");

        assertThat(service.extractRoles(jwt(ISSUER, "https://example.com/roles.v1", List.of("Teller"))).names()).containsExactly("Teller");
    }

    @Test
    void managedRolesOfTheIssuerLimitWhatTheTokenManages() {
        issuer.setRolesClaim("roles");
        issuer.setManagedRoles(List.of("Teller", " Branch Manager "));

        OidcTokenRoles roles = service.extractRoles(jwt(ISSUER, "roles", List.of("Teller", "Super user")));

        assertThat(roles.manages("teller")).isTrue();
        assertThat(roles.manages("Branch Manager")).isTrue();
        assertThat(roles.manages("Super user")).isFalse();
    }

    @Test
    void everyRoleIsManagedWithoutAList() {
        issuer.setRolesClaim("roles");

        assertThat(service.extractRoles(jwt(ISSUER, "roles", List.of("Teller"))).manages("Super user")).isTrue();
    }

    @Test
    void resolveUserPassesWhatTheTokenSaysAboutRoles() {
        issuer.setRolesClaim("roles");
        Jwt jwt = jwt(ISSUER, "roles", List.of("Teller"));
        AppUser appUser = mock(AppUser.class);
        when(resolutionService.resolveOrCreate(any(), any(), any(), any(), any())).thenReturn(appUser);

        assertThat(service.resolveUser(jwt, "alice")).isSameAs(appUser);
        verify(resolutionService).resolveOrCreate("alice", null, null, null, OidcTokenRoles.of(ISSUER, Set.of("Teller"), Set.of()));
    }

    @Test
    void resolvesAgainAfterAConcurrentWrite() {
        Jwt jwt = jwt(ISSUER, "roles", List.of("Teller"));
        AppUser appUser = mock(AppUser.class);
        when(resolutionService.resolveOrCreate(any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key")).thenReturn(appUser);

        assertThat(service.resolveUser(jwt, "alice")).isSameAs(appUser);
        verify(resolutionService, times(2)).resolveOrCreate("alice", null, null, null, OidcTokenRoles.undecided());
    }

    @Test
    void givesUpAfterASecondFailure() {
        Jwt jwt = jwt(ISSUER, "roles", List.of("Teller"));
        when(resolutionService.resolveOrCreate(any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.resolveUser(jwt, "alice")).isInstanceOf(DataIntegrityViolationException.class);
        verify(resolutionService, times(2)).resolveOrCreate(any(), any(), any(), any(), any());
    }

    private static Jwt jwt(String issuer, String claim, Object value) {
        return Jwt.withTokenValue("token").header("alg", "RS256").issuer(issuer).subject("sub-123").claim(claim, value)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(300)).build();
    }
}
