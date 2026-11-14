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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.data.OidcTokenRoles;
import org.apache.fineract.infrastructure.security.exception.OidcUserNotFoundException;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OidcAppUserResolutionServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private OfficeRepository officeRepository;
    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache usersCache;
    @Mock
    private Cache usersByUsernameCache;
    @Mock
    private FineractProperties.FineractSecurityProperties securityProperties;
    @Mock
    private FineractProperties.FineractSecurityProperties.FineractSecurityOidcFederationProperties oidcProps;
    @Mock
    private FineractProperties.FineractDefaultValues defaultProps;
    @Mock
    private AppUser existingUser;
    @Mock
    private Office headOffice;
    @Mock
    private FineractPlatformTenant platformTenant;

    @InjectMocks
    private OidcAppUserResolutionServiceImpl service;

    @BeforeEach
    void setUp() {
        when(fineractProperties.getDefaults()).thenReturn(defaultProps);
        when(defaultProps.getOfficeId()).thenReturn(1L);
        when(fineractProperties.getSecurity()).thenReturn(securityProperties);
        when(securityProperties.getOidcFederation()).thenReturn(oidcProps);
        // Required by AppUser constructor via DateUtils.getLocalDateOfTenant()
        when(platformTenant.getTimezoneId()).thenReturn("UTC");
        ThreadLocalContextUtil.setTenant(platformTenant);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void returnsExistingUserFoundByUsername() {
        when(appUserRepository.findAppUserByName("alice")).thenReturn(existingUser);

        AppUser result = service.resolveOrCreate("alice", "alice@example.com", "Alice", "Smith", OidcTokenRoles.undecided());

        assertThat(result).isSameAs(existingUser);
        verify(appUserRepository, never()).findActiveUserByEmail(any());
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void fallsBackToEmailWhenUsernameNotFound() {
        when(appUserRepository.findAppUserByName("alice")).thenReturn(null);
        when(appUserRepository.findActiveUserByEmail("alice@example.com")).thenReturn(existingUser);

        AppUser result = service.resolveOrCreate("alice", "alice@example.com", "Alice", "Smith", OidcTokenRoles.undecided());

        assertThat(result).isSameAs(existingUser);
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void throwsOidcUserNotFoundWhenUserMissingAndAutoCreateDisabled() {
        when(appUserRepository.findAppUserByName("ghost")).thenReturn(null);
        when(appUserRepository.findActiveUserByEmail("ghost@example.com")).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(false);

        assertThatThrownBy(() ->
                service.resolveOrCreate("ghost", "ghost@example.com", "Ghost", "User", OidcTokenRoles.undecided()))
                .isInstanceOf(OidcUserNotFoundException.class)
                .extracting(e -> ((OidcUserNotFoundException) e).getSubject())
                .isEqualTo("ghost");
    }

    @Test
    void autoCreatesUserWhenEnabledAndUserNotFound() {
        when(appUserRepository.findAppUserByName("newuser")).thenReturn(null);
        when(appUserRepository.findActiveUserByEmail("new@example.com")).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(true);
        when(oidcProps.getDefaultRoles()).thenReturn("");
        when(officeRepository.findById(1L)).thenReturn(Optional.of(headOffice));

        AppUser savedUser = org.mockito.Mockito.mock(AppUser.class);
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = service.resolveOrCreate("newuser", "new@example.com", "New", "User", OidcTokenRoles.undecided());

        assertThat(result).isSameAs(savedUser);
        verify(appUserRepository).saveAndFlush(any(AppUser.class));
    }

    @Test
    void throwsWhenHeadOfficeNotFoundDuringAutoCreate() {
        when(appUserRepository.findAppUserByName("newuser")).thenReturn(null);
        when(appUserRepository.findActiveUserByEmail(any())).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(true);
        when(officeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.resolveOrCreate("newuser", "new@example.com", "New", "User", OidcTokenRoles.undecided()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Head office");
    }

    @Test
    void skipsEmailLookupWhenEmailIsNull() {
        when(appUserRepository.findAppUserByName("alice")).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(false);

        assertThatThrownBy(() ->
                service.resolveOrCreate("alice", null, "Alice", "Smith", OidcTokenRoles.undecided()))
                .isInstanceOf(OidcUserNotFoundException.class);

        verify(appUserRepository, never()).findActiveUserByEmail(any());
    }

    @Test
    void leavesRolesOfExistingUserAloneWhenTheTokenDoesNotDecideThem() {
        when(appUserRepository.findAppUserByName("alice")).thenReturn(existingUser);

        AppUser result = service.resolveOrCreate("alice", null, null, null, OidcTokenRoles.undecided());

        assertThat(result).isSameAs(existingUser);
        verify(existingUser, never()).getRoles();
        verify(roleRepository, never()).findEnabledByLowerCaseNames(any());
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void grantsAndRemovesRolesOfExistingUserAsTheTokenNamesThem() {
        Role teller = role("Teller");
        Role auditor = role("Auditor");
        Set<Role> current = existingUserWithRoles(teller);
        when(oidcProps.getDefaultRoles()).thenReturn("");
        when(roleRepository.findEnabledByLowerCaseNames(Set.of("auditor", "no such role"))).thenReturn(List.of(auditor));

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of("Auditor", "No Such Role"), Set.of()));

        assertThat(current).containsExactly(auditor);
        verify(appUserRepository).saveAndFlush(existingUser);
    }

    @Test
    void removesAllRolesWhenTheTokenNamesNone() {
        Set<Role> current = existingUserWithRoles(role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("");

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of(), Set.of()));

        assertThat(current).isEmpty();
        verify(roleRepository, never()).findEnabledByLowerCaseNames(any());
        verify(appUserRepository).saveAndFlush(existingUser);
    }

    @Test
    void neverRemovesDefaultRoles() {
        Role readOnly = role("Read-Only Role");
        Set<Role> current = existingUserWithRoles(readOnly, role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("Read-Only Role");

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of(), Set.of()));

        assertThat(current).containsExactly(readOnly);
    }

    @Test
    void grantsAndRemovesOnlyTheRolesTheIssuerManages() {
        Role superUser = role("Super user");
        Role teller = role("Teller");
        Role auditor = role("Auditor");
        Set<Role> current = existingUserWithRoles(superUser, teller);
        when(oidcProps.getDefaultRoles()).thenReturn("");
        when(roleRepository.findEnabledByLowerCaseNames(Set.of("auditor"))).thenReturn(List.of(auditor));

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of("Auditor", "Branch Manager"), Set.of("teller", "auditor")));

        // Super user is not managed, so it stays; Branch Manager is not managed, so it is not even looked up.
        assertThat(current).containsExactlyInAnyOrder(superUser, auditor);
        verify(roleRepository).findEnabledByLowerCaseNames(Set.of("auditor"));
    }

    @Test
    void doesNotGrantAnUnmanagedRoleSuchAsSuperUser() {
        Set<Role> current = existingUserWithRoles(role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("");

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of("Teller", "Super user"), Set.of("teller")));

        assertThat(current).hasSize(1);
        verify(roleRepository, never()).findEnabledByLowerCaseNames(any());
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void neitherQueriesNorSavesWhenTheTokenNamesTheRolesTheUserHas() {
        existingUserWithRoles(role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("");

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of("teller"), Set.of()));

        verify(roleRepository, never()).findEnabledByLowerCaseNames(any());
        verify(appUserRepository, never()).saveAndFlush(any());
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void doesNotSaveWhenTheOtherNamesOfTheTokenAreNoEnabledRoles() {
        existingUserWithRoles(role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("");
        when(roleRepository.findEnabledByLowerCaseNames(Set.of("role of another system"))).thenReturn(List.of());

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of("Teller", "Role Of Another System"), Set.of()));

        verify(roleRepository).findEnabledByLowerCaseNames(Set.of("role of another system"));
        verify(appUserRepository, never()).saveAndFlush(any());
    }

    @Test
    void evictsTheUserCachesWhenRolesChange() {
        existingUserWithRoles(role("Teller"));
        when(oidcProps.getDefaultRoles()).thenReturn("");
        when(cacheManager.getCache("users")).thenReturn(usersCache);
        when(cacheManager.getCache("usersByUsername")).thenReturn(usersByUsernameCache);

        service.resolveOrCreate("alice", null, null, null, tokenRoles(Set.of(), Set.of()));

        verify(usersCache).clear();
        verify(usersByUsernameCache).clear();
    }

    @Test
    void autoCreatedUserTakesItsRolesFromTheToken() {
        Role readOnly = role("Read-Only Role");
        Role teller = role("Teller");
        when(appUserRepository.findAppUserByName("newuser")).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(true);
        when(oidcProps.getDefaultRoles()).thenReturn("Read-Only Role");
        when(roleRepository.getRoleByName("Read-Only Role")).thenReturn(readOnly);
        when(roleRepository.findEnabledByLowerCaseNames(Set.of("teller"))).thenReturn(List.of(teller));
        when(officeRepository.findById(1L)).thenReturn(Optional.of(headOffice));
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.resolveOrCreate("newuser", null, null, null, tokenRoles(Set.of("Teller", "Super user"), Set.of("teller")));

        verify(appUserRepository).saveAndFlush(argThat(user -> user.getRoles().equals(Set.of(readOnly, teller))));
    }

    @Test
    void autoCreatedUserGetsOnlyDefaultRolesWhenTheTokenDoesNotDecide() {
        Role readOnly = role("Read-Only Role");
        when(appUserRepository.findAppUserByName("newuser")).thenReturn(null);
        when(oidcProps.isAutoCreateUser()).thenReturn(true);
        when(oidcProps.getDefaultRoles()).thenReturn("Read-Only Role");
        when(roleRepository.getRoleByName("Read-Only Role")).thenReturn(readOnly);
        when(officeRepository.findById(1L)).thenReturn(Optional.of(headOffice));
        when(appUserRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.resolveOrCreate("newuser", null, null, null, OidcTokenRoles.undecided());

        verify(roleRepository, never()).findEnabledByLowerCaseNames(any());
        verify(appUserRepository).saveAndFlush(argThat(user -> user.getRoles().equals(Set.of(readOnly))));
    }

    private Set<Role> existingUserWithRoles(Role... roles) {
        Set<Role> current = new HashSet<>(Set.of(roles));
        when(appUserRepository.findAppUserByName("alice")).thenReturn(existingUser);
        when(existingUser.getRoles()).thenReturn(current);
        when(appUserRepository.saveAndFlush(existingUser)).thenReturn(existingUser);
        return current;
    }

    private static OidcTokenRoles tokenRoles(Set<String> names, Set<String> managedRoles) {
        return OidcTokenRoles.of("https://idp.example.com", names, managedRoles);
    }

    private static Role role(String name) {
        Role role = org.mockito.Mockito.mock(Role.class);
        when(role.getName()).thenReturn(name);
        return role;
    }
}
