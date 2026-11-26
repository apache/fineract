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
package org.apache.fineract.useradministration.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.domain.DefaultPlatformPasswordEncoder;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests how {@link AppUserWritePlatformServiceJpaRepositoryImpl} reads a user's details from the request when it
 * creates or updates a user or changes a password, using a real {@link AppUser} and real requests. The password history
 * and the office and staff checks are tested in {@link AppUserWritePlatformServiceJpaRepositoryImplTest},
 * {@link AppUserUpdatePasswordTest} and {@link AppUserUpdateOfficeLinkedStaffTest}.
 */
@ExtendWith(MockitoExtension.class)
class AppUserWritePlatformServiceJpaRepositoryImplUserDetailsTest {

    private static final Long USER_ID = 10L;
    private static final Long OFFICE_ID = 1L;
    private static final Long OTHER_OFFICE_ID = 2L;
    private static final String CURRENT_PASSWORD = "QwE!5rTy#9uP0";
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final PlatformPasswordEncoder platformPasswordEncoder = new DefaultPlatformPasswordEncoder(PASSWORD_ENCODER);

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserDataValidator userDataValidator;
    @Mock
    private AppUserPreviousPasswordRepository appUserPreviousPasswordRepository;
    @Mock
    private StaffRepository staffRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;

    private AppUserWritePlatformServiceJpaRepositoryImpl underTest;
    private Office office;
    private Office otherOffice;
    private Role role;
    private Role otherRole;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 11, 26))));
        underTest = new AppUserWritePlatformServiceJpaRepositoryImpl(context, userDomainService, platformPasswordEncoder, appUserRepository,
                officeRepositoryWrapper, roleRepository, userDataValidator, appUserPreviousPasswordRepository, staffRepository,
                configurationDomainService);
        office = office(OFFICE_ID);
        otherOffice = office(OTHER_OFFICE_ID);
        role = role(3L);
        otherRole = role(4L);
        lenient().when(roleRepository.findById(3L)).thenReturn(Optional.of(role));
        lenient().when(roleRepository.findById(4L)).thenReturn(Optional.of(otherRole));
        lenient().when(officeRepositoryWrapper.findOneWithNotFoundDetection(OFFICE_ID)).thenReturn(office);
        lenient().when(officeRepositoryWrapper.findOneWithNotFoundDetection(OTHER_OFFICE_ID)).thenReturn(otherOffice);
        final AppUser currentUser = mock(AppUser.class);
        lenient().when(currentUser.getId()).thenReturn(99L);
        lenient().when(context.authenticatedUser(any(CommandWrapper.class))).thenReturn(currentUser);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void createStoresTheTrimmedDetailsAndTheDefaults() {
        final AppUser created = create("{\"username\": \" jdoe \", \"firstname\": \" John \", \"lastname\": \" Doe \", "
                + "\"email\": \" jdoe@example.org \", \"officeId\": 1, \"roles\": [\"3\"], \"sendPasswordToEmail\": false, "
                + "\"password\": \" " + CURRENT_PASSWORD + " \", \"repeatPassword\": \" " + CURRENT_PASSWORD + " \"}", false);

        assertEquals("jdoe", created.getUsername());
        assertEquals("John", created.getFirstname());
        assertEquals("Doe", created.getLastname());
        assertEquals("jdoe@example.org", created.getEmail());
        assertEquals(CURRENT_PASSWORD, created.getPassword());
        assertSame(office, created.getOffice());
        assertEquals(Set.of(role), created.getRoles());
        assertNull(created.getStaff());
        assertFalse(created.getPasswordNeverExpires());
        assertFalse(created.isLoginRetryLimitEnabled());
        assertFalse(created.isPasswordResetAllowed());
        assertFalse(created.isPasswordResetRequired());
        assertFalse(created.getCannotChangePassword());
        assertTrue(created.isEnabled());
        assertTrue(created.isAccountNonLocked());
    }

    @Test
    void createStoresTheOptionalFlags() {
        when(configurationDomainService.isForcePasswordResetOnFirstLoginEnabled()).thenReturn(true);

        final AppUser created = create("{\"username\": \"jdoe\", \"firstname\": \"John\", \"lastname\": \"Doe\", "
                + "\"email\": \"jdoe@example.org\", \"officeId\": 1, \"roles\": [\"3\", \"4\"], \"sendPasswordToEmail\": false, "
                + "\"password\": \"" + CURRENT_PASSWORD + "\", \"repeatPassword\": \"" + CURRENT_PASSWORD + "\", "
                + "\"passwordNeverExpires\": true, \"isLoginRetriesEnabled\": true, \"isPasswordResetAllowed\": true}", false);

        assertEquals(Set.of(role, otherRole), created.getRoles());
        assertTrue(created.getPasswordNeverExpires());
        assertTrue(created.isLoginRetryLimitEnabled());
        assertTrue(created.isPasswordResetAllowed());
        assertTrue(created.isPasswordResetRequired());
    }

    @Test
    void createWithPasswordSentByEmailUsesAGeneratedPassword() {
        final AppUser created = create("{\"username\": \"jdoe\", \"firstname\": \"John\", \"lastname\": \"Doe\", "
                + "\"email\": \"jdoe@example.org\", \"officeId\": 1, \"roles\": [\"3\"], \"sendPasswordToEmail\": true}", true);

        assertEquals(13, created.getPassword().length());
        assertNotEquals("", created.getPassword());
    }

    @Test
    void createOfTheSystemUserNeverEnablesLoginRetries() {
        final AppUser created = create("{\"username\": \"system\", \"firstname\": \"System\", \"lastname\": \"User\", "
                + "\"email\": \"system@example.org\", \"officeId\": 1, \"roles\": [\"3\"], \"sendPasswordToEmail\": false, "
                + "\"password\": \"" + CURRENT_PASSWORD + "\", \"repeatPassword\": \"" + CURRENT_PASSWORD + "\", "
                + "\"isLoginRetriesEnabled\": true, \"isPasswordResetAllowed\": true}", false);

        assertFalse(created.isLoginRetryLimitEnabled());
        assertFalse(created.isPasswordResetAllowed());
    }

    @Test
    void updateReportsAndAppliesTheChangedValuesInOrder() {
        final AppUser user = existingUser("jdoe");
        final String newPassword = "Zx7#vB2!nM5@q";

        final CommandProcessingResult result = underTest.updateUser(USER_ID,
                command("{\"isPasswordResetAllowed\": true, \"isLoginRetriesEnabled\": true, \"passwordNeverExpires\": true, "
                        + "\"email\": \"new@example.org\", \"lastname\": \"Smith\", \"firstname\": \"Jane\", \"username\": \"jsmith\", "
                        + "\"roles\": [\"4\"], \"officeId\": 2, \"password\": \"" + newPassword + "\", \"repeatPassword\": \"" + newPassword
                        + "\"}"));

        final Map<String, Object> changes = result.getChanges();
        assertEquals(List.of("password", "officeId", "roles", "username", "firstname", "lastname", "email", "passwordNeverExpires",
                "isLoginRetriesEnabled", "isPasswordResetAllowed"), List.copyOf(changes.keySet()));
        assertEquals(true, changes.get("password"));
        assertEquals(OTHER_OFFICE_ID, changes.get("officeId"));
        assertArrayEquals(new String[] { "4" }, (String[]) changes.get("roles"));
        assertEquals("jsmith", changes.get("username"));
        assertEquals("Jane", changes.get("firstname"));
        assertEquals("Smith", changes.get("lastname"));
        assertEquals("new@example.org", changes.get("email"));
        assertEquals(true, changes.get("passwordNeverExpires"));
        assertEquals(true, changes.get("isLoginRetriesEnabled"));
        assertEquals(true, changes.get("isPasswordResetAllowed"));

        assertTrue(PASSWORD_ENCODER.matches(newPassword, user.getPassword()));
        assertFalse(firstTimeLoginRemaining(user));
        assertEquals(LocalDate.of(2026, 11, 26), user.getLastTimePasswordUpdated());
        assertSame(otherOffice, user.getOffice());
        assertEquals(Set.of(otherRole), user.getRoles());
        assertEquals("jsmith", user.getUsername());
        assertEquals("Jane", user.getFirstname());
        assertEquals("Smith", user.getLastname());
        assertEquals("new@example.org", user.getEmail());
        assertTrue(user.getPasswordNeverExpires());
        assertTrue(user.isLoginRetryLimitEnabled());
        assertTrue(user.isPasswordResetAllowed());
        assertEquals(OTHER_OFFICE_ID, result.getOfficeId());
        verify(appUserRepository).saveAndFlush(user);
    }

    @Test
    void updateWithTheSameValuesReportsNoChangeAndSavesNothing() {
        final AppUser user = existingUser("jdoe");

        final CommandProcessingResult result = underTest.updateUser(USER_ID,
                command("{\"username\": \"jdoe\", \"firstname\": \"John\", \"lastname\": \"Doe\", \"email\": \"jdoe@example.org\", "
                        + "\"officeId\": 1, \"roles\": [\"3\"], \"password\": \"" + CURRENT_PASSWORD + "\", \"repeatPassword\": \""
                        + CURRENT_PASSWORD + "\", \"passwordNeverExpires\": false, \"isLoginRetriesEnabled\": false, "
                        + "\"isPasswordResetAllowed\": false}"));

        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        assertEquals("jdoe", user.getUsername());
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    /**
     * Documents a known inconsistency, kept on purpose: creating a user trims the username, names and email, but
     * updating a user compares, reports and stores the value as sent, with its surrounding spaces.
     */
    @Test
    void updateKeepsSurroundingSpacesUnlikeCreate() {
        final AppUser user = existingUser("jdoe");

        final CommandProcessingResult result = underTest.updateUser(USER_ID,
                command("{\"firstname\": \" John \", \"email\": \" e@x.org \"}"));

        assertEquals(Map.of("firstname", " John ", "email", " e@x.org "), result.getChanges());
        assertEquals(" John ", user.getFirstname());
        assertEquals(" e@x.org ", user.getEmail());
    }

    /**
     * Documents a known inconsistency, kept on purpose: for a user without staff, the service reports any
     * {@code staffId} in the request, even {@code null}, as a change. Through the API, {@code "staffId": null} sent to
     * {@code PUT /users/{userId}} for such a user reports no change.
     */
    @Test
    void updateReportsANullStaffIdAsAChangeForAUserWithoutStaff() {
        final AppUser user = existingUser("jdoe");

        final CommandProcessingResult result = underTest.updateUser(USER_ID, command("{\"staffId\": null}"));

        assertTrue(result.getChanges().containsKey("staffId"));
        assertNull(result.getChanges().get("staffId"));
        assertNull(user.getStaff());
        verify(appUserRepository).saveAndFlush(user);
    }

    @Test
    void updateOfTheSystemUserNameIsRejected() {
        existingUser("system");

        assertThrows(NoAuthorizationException.class, () -> underTest.updateUser(USER_ID, command("{\"username\": \"other\"}")));

        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void updateOfTheSystemUserNeverEnablesLoginRetries() {
        final AppUser user = existingUser("system");

        final CommandProcessingResult result = underTest.updateUser(USER_ID, command("{\"isLoginRetriesEnabled\": true}"));

        assertTrue(result.getChanges() == null || result.getChanges().isEmpty());
        assertFalse(user.isLoginRetryLimitEnabled());
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void updateDisablingLoginRetriesIsReported() {
        final AppUser user = existingUser("jdoe");
        user.updateLoginRetryLimitEnabled(true);

        final CommandProcessingResult result = underTest.updateUser(USER_ID, command("{\"isLoginRetriesEnabled\": false}"));

        assertEquals(Map.of("isLoginRetriesEnabled", false), result.getChanges());
        assertFalse(user.isLoginRetryLimitEnabled());
    }

    @Test
    void updateOfThePasswordOfAUserThatCannotChangeItIsRejected() {
        final AppUser user = new AppUser(office, new User("jdoe", PASSWORD_ENCODER.encode(CURRENT_PASSWORD), List.of()),
                new HashSet<>(Set.of(role)), "jdoe@example.org", "John", "Doe", null, false, true);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertThrows(NoAuthorizationException.class,
                () -> underTest.updateUser(USER_ID, command("{\"password\": \"Zx7#vB2!nM5@q\", \"repeatPassword\": \"Zx7#vB2!nM5@q\"}")));

        assertTrue(PASSWORD_ENCODER.matches(CURRENT_PASSWORD, user.getPassword()));
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));
    }

    @Test
    void changePasswordReportsAndStoresTheNewPassword() {
        final AppUser user = existingUser("jdoe");
        final String newPassword = "Zx7#vB2!nM5@q";

        final CommandProcessingResult result = underTest.changeUserPassword(USER_ID,
                command("{\"password\": \"" + newPassword + "\", \"repeatPassword\": \"" + newPassword + "\"}"));

        assertEquals(Map.of("password", true), result.getChanges());
        assertTrue(PASSWORD_ENCODER.matches(newPassword, user.getPassword()));
        assertFalse(firstTimeLoginRemaining(user));
        assertEquals(OFFICE_ID, result.getOfficeId());
        verify(appUserRepository).saveAndFlush(user);
        verify(appUserPreviousPasswordRepository).save(any());
    }

    private AppUser create(final String json, final boolean sendPasswordToEmail) {
        underTest.createUser(command(json));
        final ArgumentCaptor<AppUser> created = ArgumentCaptor.forClass(AppUser.class);
        verify(userDomainService).create(created.capture(), eq(sendPasswordToEmail));
        return created.getValue();
    }

    private AppUser existingUser(final String username) {
        final AppUser user = new AppUser(office, new User(username, PASSWORD_ENCODER.encode(CURRENT_PASSWORD), List.of()),
                new HashSet<>(Set.of(role)), "jdoe@example.org", "John", "Doe", null, false, false);
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        return user;
    }

    private static boolean firstTimeLoginRemaining(final AppUser user) {
        return (boolean) ReflectionTestUtils.getField(user, "firstTimeLoginRemaining");
    }

    private JsonCommand command(final String json) {
        return JsonCommand.from(json, fromJsonHelper.parse(json), fromJsonHelper, null, USER_ID, null, null, null, null, null, null, null,
                null, null, null, null, null);
    }

    private static Office office(final Long id) {
        final Office office = mock(Office.class);
        lenient().when(office.getId()).thenReturn(id);
        return office;
    }

    private static Role role(final Long id) {
        final Role role = new Role("Role " + id, "Role " + id);
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }
}
