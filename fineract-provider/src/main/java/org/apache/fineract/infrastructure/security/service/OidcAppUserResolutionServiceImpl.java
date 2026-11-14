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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.security.data.OidcTokenRoles;
import org.apache.fineract.infrastructure.security.exception.OidcUserNotFoundException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OidcAppUserResolutionServiceImpl implements OidcAppUserResolutionService {

    private static final String USERS_CACHE = "users";
    private static final String USERS_BY_USERNAME_CACHE = "usersByUsername";

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final OfficeRepository officeRepository;
    private final FineractProperties fineractProperties;
    private final CacheManager cacheManager;

    // Stateless encoder — safe to create once per class
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    @Transactional
    public AppUser resolveOrCreate(String username, String email, String firstName, String lastName, OidcTokenRoles tokenRoles) {
        FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties oidcConfig = fineractProperties.getSecurity()
                .getOidcFederation();

        // 1. Lookup by username
        AppUser user = appUserRepository.findAppUserByName(username);
        if (user != null) {
            log.debug("OIDC user resolved by username: '{}'", username);
            return synchronizeRoles(user, tokenRoles, oidcConfig);
        }

        // 2. Fallback: lookup by email
        if (email != null) {
            user = appUserRepository.findActiveUserByEmail(email);
            if (user != null) {
                log.debug("OIDC user resolved by email: '{}'", email);
                return synchronizeRoles(user, tokenRoles, oidcConfig);
            }
        }

        // 3. Auto-create when enabled
        if (!oidcConfig.isAutoCreateUser()) {
            log.warn("OIDC user '{}' not found in Fineract and auto-create is disabled", username);
            throw new OidcUserNotFoundException(username);
        }

        log.info("Auto-creating Fineract user for OIDC subject '{}'", username);
        return createUser(username, email, firstName, lastName, tokenRoles, oidcConfig);
    }

    /**
     * Brings the user's roles in line with a token that decides them. This runs whenever such a token is resolved to a
     * user, which for a Bearer token is every request.
     *
     * The token grants the enabled roles it names and removes the roles it does not name, both only among the roles its
     * issuer manages. The default roles are never removed. A token that names the roles the user holds costs no query;
     * names the user does not hold are looked up with one query.
     */
    private AppUser synchronizeRoles(AppUser user, OidcTokenRoles tokenRoles,
            FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties oidcConfig) {
        if (!tokenRoles.decides()) {
            return user;
        }
        Set<String> named = managedNames(tokenRoles);
        Set<String> defaults = Splitter.on(',').splitToList(oidcConfig.getDefaultRoles()).stream().map(String::trim)
                .map(OidcAppUserResolutionServiceImpl::lowerCase).collect(Collectors.toSet());
        Predicate<Role> removed = role -> {
            String name = lowerCase(role.getName());
            return tokenRoles.manages(name) && !named.contains(name) && !defaults.contains(name);
        };

        Set<String> missing = new HashSet<>(named);
        user.getRoles().forEach(role -> missing.remove(lowerCase(role.getName())));
        List<Role> granted = missing.isEmpty() ? List.of() : roleRepository.findEnabledByLowerCaseNames(missing);
        if (granted.size() < missing.size()) {
            log.debug("OIDC token of user '{}' names roles that are not enabled Fineract roles, skipping them: {} (found: {})",
                    user.getUsername(), missing, roleNames(granted));
        }
        if (granted.isEmpty() && user.getRoles().stream().noneMatch(removed)) {
            return user;
        }

        List<String> before = roleNames(user.getRoles());
        user.getRoles().removeIf(removed);
        user.getRoles().addAll(granted);
        AppUser saved = appUserRepository.saveAndFlush(user);
        evictUserCaches();
        // The only record of the change: it is not a command, so it is neither audited nor subject to maker-checker.
        log.info("Roles of user '{}' (id={}) changed by an OIDC token of issuer '{}': {} -> {}", saved.getUsername(), saved.getId(),
                tokenRoles.issuer(), before, roleNames(saved.getRoles()));
        return saved;
    }

    /** The names in the token that its issuer manages, in lower case. */
    private static Set<String> managedNames(OidcTokenRoles tokenRoles) {
        return tokenRoles.names().stream().map(OidcAppUserResolutionServiceImpl::lowerCase).filter(tokenRoles::manages)
                .collect(Collectors.toSet());
    }

    private static List<String> roleNames(Collection<Role> roles) {
        return roles.stream().map(Role::getName).sorted().toList();
    }

    private static String lowerCase(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /** As a regular change of a user does: the caches hold users with their roles. */
    private void evictUserCaches() {
        for (String name : List.of(USERS_CACHE, USERS_BY_USERNAME_CACHE)) {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    private AppUser createUser(String username, String email, String firstName, String lastName, OidcTokenRoles tokenRoles,
            FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties oidcConfig) {

        final Office headOffice = officeRepository.findById(fineractProperties.getDefaults().getOfficeId())
                .orElseThrow(() -> new IllegalStateException("Head office (id=1) not found — cannot auto-create OIDC user"));

        String encodedPassword = PASSWORD_ENCODER.encode(new RandomPasswordGenerator(20).generate());

        User springUser = new User(username, encodedPassword, true, true, true, true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Set<Role> roles = resolveRoles(oidcConfig.getDefaultRoles(), Set.of());
        if (tokenRoles.decides()) {
            Set<String> named = managedNames(tokenRoles);
            List<Role> granted = named.isEmpty() ? List.of() : roleRepository.findEnabledByLowerCaseNames(named);
            if (granted.size() < named.size()) {
                log.warn("OIDC token of new user '{}' names roles that are not enabled Fineract roles, skipping them: {} (found: {})",
                        username, named, roleNames(granted));
            }
            roles.addAll(granted);
        }

        String resolvedEmail = email != null ? email : username + "@oidc.placeholder";
        String resolvedFirstName = firstName != null ? firstName : username;
        String resolvedLastName = lastName != null ? lastName : "";

        AppUser appUser = new AppUser(headOffice, springUser, roles, resolvedEmail, resolvedFirstName, resolvedLastName, null, true, false);

        AppUser saved = appUserRepository.saveAndFlush(appUser);
        log.info("Auto-created Fineract user '{}' (id={}) from OIDC identity of issuer '{}' with roles {}", username, saved.getId(),
                tokenRoles.issuer(), roleNames(roles));
        return saved;
    }

    private Set<Role> resolveRoles(String defaultRolesConfig, Set<String> requestedRoles) {
        Set<Role> result = new HashSet<>();
        Stream.concat(Arrays.stream(defaultRolesConfig.split(",")), requestedRoles.stream()).map(String::trim)
                .filter(name -> !name.isEmpty()).forEach(name -> {
                    Role role = roleRepository.getRoleByName(name);
                    if (role != null) {
                        result.add(role);
                    } else {
                        log.warn("OIDC role mapping: role '{}' not found in Fineract — skipping", name);
                    }
                });
        return result;
    }
}
