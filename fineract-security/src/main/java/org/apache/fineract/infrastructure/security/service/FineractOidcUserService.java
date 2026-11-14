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

import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.config.FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties.OidcIssuerProperties;
import org.apache.fineract.infrastructure.security.data.FineractOidcUser;
import org.apache.fineract.infrastructure.security.data.OidcTokenRoles;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.stereotype.Service;

/**
 * Maps OIDC identities (browser login or Bearer JWT) to Fineract {@link AppUser} entities. User resolution and
 * auto-creation are delegated to {@link OidcAppUserResolutionService} whose implementation lives in fineract-provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FineractOidcUserService {

    private final OidcAppUserResolutionService resolutionService;
    private final FineractProperties fineractProperties;

    /**
     * Resolves the Fineract AppUser for a Bearer JWT from an external IdP. Used by
     * {@code FineractOidcJwtAuthenticationConverter}.
     */
    public AppUser resolveUser(Jwt jwt, String username) {
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        log.debug("Resolving Fineract user for OIDC subject '{}' (username claim: '{}')", jwt.getSubject(), username);

        return resolveOrCreate(username, email, firstName, lastName, extractRoles(jwt));
    }

    /**
     * Resolves the Fineract AppUser for a browser-based OIDC login and wraps the result in a {@link FineractOidcUser}.
     * Used by the OAuth2 login redirect flow.
     */
    public FineractOidcUser processOidcUser(OidcUser oidcUser, String tenantId) {
        String usernameClaim = fineractProperties.getSecurity().getOidcFederation().getUsernameClaim();

        String username = oidcUser.getClaimAsString(usernameClaim);
        if (username == null) {
            username = oidcUser.getSubject();
        }

        String email = oidcUser.getEmail();
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();

        log.debug("Processing OIDC user '{}' for tenant '{}'", username, tenantId);

        AppUser appUser = resolveOrCreate(username, email, firstName, lastName, extractRoles(oidcUser));

        Collection<? extends GrantedAuthority> authorities = appUser.getAuthorities();
        OidcIdToken idToken = oidcUser.getIdToken();
        OidcUserInfo userInfo = oidcUser.getUserInfo();

        return new FineractOidcUser(authorities, idToken, userInfo, appUser, tenantId);
    }

    /**
     * Parallel requests of one user may all find that the user is to be created, or its roles to be changed. One of
     * them writes; the others fail on the same rows and find, on a second try, what the first one wrote.
     */
    private AppUser resolveOrCreate(String username, String email, String firstName, String lastName, OidcTokenRoles roles) {
        try {
            return resolutionService.resolveOrCreate(username, email, firstName, lastName, roles);
        } catch (JpaSystemException | DataIntegrityViolationException e) {
            log.debug("Resolving OIDC user '{}' again after a concurrent write", username, e);
            return resolutionService.resolveOrCreate(username, email, firstName, lastName, roles);
        }
    }

    /**
     * Extracts the username from a JWT using the configured claim, falling back to {@code sub}.
     */
    public String extractUsername(Jwt jwt) {
        String claimName = fineractProperties.getSecurity().getOidcFederation().getUsernameClaim();
        String username = jwt.getClaimAsString(claimName);
        return username != null ? username : jwt.getSubject();
    }

    /**
     * Reads what the token says about its user's roles. Only an issuer whose entry in {@code issuers[]} has a
     * {@code rolesClaim} says anything, and only in a token that carries that claim: a list, or a comma-separated
     * string. A dotted claim name reaches into nested claims when no claim has that literal name, e.g.
     * {@code realm_access.roles}. In every other case the token leaves the user's roles as they are.
     */
    public OidcTokenRoles extractRoles(ClaimAccessor token) {
        String issuer = token.getClaimAsString(JwtClaimNames.ISS);
        List<OidcIssuerProperties> issuers = fineractProperties.getSecurity().getOidcFederation().getIssuers();
        OidcIssuerProperties config = issuer == null || issuers == null ? null
                : issuers.stream().filter(candidate -> issuer.equals(candidate.getIssuerUri())).findFirst().orElse(null);
        if (config == null || config.getRolesClaim() == null || config.getRolesClaim().isBlank()) {
            return OidcTokenRoles.undecided();
        }
        String claimName = config.getRolesClaim();
        Object value = token.getClaim(claimName);
        if (value == null && claimName.contains(".")) {
            value = token.getClaims();
            for (String part : Splitter.on('.').split(claimName)) {
                value = value instanceof Map<?, ?> map ? map.get(part) : null;
            }
        }
        Collection<?> names = value instanceof Collection<?> collection ? collection
                : value instanceof String string ? Splitter.on(',').splitToList(string) : null;
        if (names == null) {
            return OidcTokenRoles.undecided();
        }
        Set<String> roles = new LinkedHashSet<>();
        for (Object name : names) {
            if (name != null && !name.toString().isBlank()) {
                roles.add(name.toString().trim());
            }
        }
        Set<String> managedRoles = new LinkedHashSet<>();
        if (config.getManagedRoles() != null) {
            for (String name : config.getManagedRoles()) {
                if (name != null && !name.isBlank()) {
                    managedRoles.add(name.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
        return OidcTokenRoles.of(issuer, roles, managedRoles);
    }

    /**
     * Extracts the tenant ID from a JWT using the configured claim. Returns null if the claim is absent — the filter
     * layer handles fallback to header / query param.
     */
    public String extractTenantId(Jwt jwt) {
        String claimName = fineractProperties.getSecurity().getOidcFederation().getTenantClaimName();
        return jwt.getClaimAsString(claimName);
    }
}
