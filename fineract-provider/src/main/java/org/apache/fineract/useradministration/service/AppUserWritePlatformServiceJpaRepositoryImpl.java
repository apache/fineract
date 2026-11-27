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

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.PlatformEmailSendException;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserPreviousPassword;
import org.apache.fineract.useradministration.domain.AppUserPreviousPasswordRepository;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.exception.PasswordMustBeDifferentException;
import org.apache.fineract.useradministration.exception.PasswordPreviouslyUsedException;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Slf4j
@RequiredArgsConstructor
public class AppUserWritePlatformServiceJpaRepositoryImpl implements AppUserWritePlatformService {

    private final PlatformSecurityContext context;
    private final UserDomainService userDomainService;
    private final PlatformPasswordEncoder platformPasswordEncoder;
    private final AppUserRepository appUserRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final RoleRepository roleRepository;
    private final UserDataValidator fromApiJsonDeserializer;
    private final AppUserPreviousPasswordRepository appUserPreviewPasswordRepository;
    private final StaffRepository staffRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult createUser(final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final String officeIdParamName = "officeId";
            final Long officeId = command.longValueOfParameterNamed(officeIdParamName);

            final Office userOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);

            final String[] roles = command.arrayValueOfParameterNamed("roles");
            final Set<Role> allRoles = assembleSetOfRoles(roles);

            final String staffIdParamName = "staffId";
            final Long staffId = command.longValueOfParameterNamed(staffIdParamName);

            Staff linkedStaff;
            if (staffId != null) {
                linkedStaff = this.staffRepository.findByOffice(staffId, userOffice.getId())
                        .orElseThrow(() -> new StaffNotFoundException(staffId));
            } else {
                linkedStaff = null;
            }

            AppUser appUser = newAppUser(userOffice, linkedStaff, allRoles, command);
            if (this.configurationDomainService.isForcePasswordResetOnFirstLoginEnabled()) {
                appUser.updatePasswordResetRequired(true);
            }

            final Boolean sendPasswordToEmail = command.booleanObjectValueOfParameterNamed("sendPasswordToEmail");
            this.userDomainService.create(appUser, sendPasswordToEmail);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(appUser.getId()) //
                    .withOfficeId(userOffice.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
        } catch (final JpaSystemException | PersistenceException | AuthenticationServiceException dve) {
            log.error("createUser: JpaSystemException | PersistenceException | AuthenticationServiceException", dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        } catch (final PlatformEmailSendException e) {
            log.error("createUser: PlatformEmailSendException", e);

            final String email = command.stringValueOfParameterNamed("email");
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.user.email.invalid",
                    "Sending email failed; is parameter email is invalid? More details available in server log: " + e.getMessage(), "email",
                    email);

            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    List.of(error), e);
        }
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult changeUserPassword(final Long userId, final JsonCommand command) {
        try {
            this.context.authenticatedUser(new CommandWrapperBuilder().changeUserPassword(userId).build());
            this.fromApiJsonDeserializer.validateForChangePassword(command.json(),
                    this.context.authenticatedUser(new CommandWrapperBuilder().changeUserPassword(userId).build()));
            final AppUser userToUpdate = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
            final AppUserPreviousPassword currentPasswordToSaveAsPreview = getCurrentPasswordToSaveAsPreview(userToUpdate, command);
            final Map<String, Object> changes = new LinkedHashMap<>(1);
            applyPasswordChange(userToUpdate, command, changes);
            if (changes.isEmpty() && command.hasParameter(AppUserConstants.PASSWORD)) {
                throw new PasswordMustBeDifferentException();
            }
            if (!changes.isEmpty()) {
                userToUpdate.updatePasswordResetRequired(false);
                this.appUserRepository.saveAndFlush(userToUpdate);
                if (currentPasswordToSaveAsPreview != null) {
                    this.appUserPreviewPasswordRepository.save(currentPasswordToSaveAsPreview);
                }
            }
            return new CommandProcessingResultBuilder() //
                    .withEntityId(userId) //
                    .withOfficeId(userToUpdate.getOffice().getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
        } catch (final JpaSystemException | PersistenceException | AuthenticationServiceException dve) {
            log.error("changeUserPassword: JpaSystemException | PersistenceException | AuthenticationServiceException", dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        }
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult updateUser(final Long userId, final JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser(new CommandWrapperBuilder().updateUser(null).build());

            this.fromApiJsonDeserializer.validateForUpdate(command.json(), currentUser);

            final AppUser userToUpdate = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

            final AppUserPreviousPassword currentPasswordToSaveAsPreview = getCurrentPasswordToSaveAsPreview(userToUpdate, command);

            final Map<String, Object> changes = applyChanges(userToUpdate, command);

            // A user's own update is not subject to maker-checker, so it must not widen the user's own access
            if (userId.equals(currentUser.getId()) && (changes.containsKey("roles") || changes.containsKey("officeId"))
                    && !currentUser.hasAnyPermission("ALL_FUNCTIONS")) {
                throw new NoAuthorizationException("User has no authority to change their own roles or office");
            }

            if (changes.containsKey("officeId")) {
                final Long officeId = (Long) changes.get("officeId");
                final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
                userToUpdate.changeOffice(office);
            }

            if (changes.containsKey("staffId")) {
                final Long staffId = (Long) changes.get("staffId");
                Staff linkedStaff = null;
                if (staffId != null) {
                    linkedStaff = this.staffRepository.findByOffice(staffId, userToUpdate.getOffice().getId())
                            .orElseThrow(() -> new StaffNotFoundException(staffId));
                }
                userToUpdate.changeStaff(linkedStaff);
            } else if (changes.containsKey("officeId") && userToUpdate.getStaffId() != null) {
                final Long staffId = userToUpdate.getStaffId();
                this.staffRepository.findByOffice(staffId, userToUpdate.getOffice().getId())
                        .orElseThrow(() -> new StaffNotFoundException(staffId));
            }

            if (changes.containsKey("roles")) {
                final String[] roleIds = (String[]) changes.get("roles");
                final Set<Role> allRoles = assembleSetOfRoles(roleIds);

                userToUpdate.updateRoles(allRoles);
            }

            if (!changes.isEmpty()) {
                if ((changes.containsKey("password") || changes.containsKey("passwordEncoded")) && !currentUser.getId().equals(userId)
                        && this.configurationDomainService.isForcePasswordResetOnFirstLoginEnabled()) {
                    userToUpdate.updatePasswordResetRequired(true);
                }
                this.appUserRepository.saveAndFlush(userToUpdate);

                if (currentPasswordToSaveAsPreview != null) {
                    this.appUserPreviewPasswordRepository.save(currentPasswordToSaveAsPreview);
                }

            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(userId) //
                    .withOfficeId(userToUpdate.getOffice().getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            throw handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
        } catch (final JpaSystemException | PersistenceException | AuthenticationServiceException dve) {
            log.error("updateUser: JpaSystemException | PersistenceException | AuthenticationServiceException", dve);
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            throw handleDataIntegrityIssues(command, throwable, dve);
        }
    }

    /**
     * Check the submitted password against the last N used passwords, and return the current password to save in the
     * password history.
     * <p>
     * A plain-text password is checked with {@link PlatformPasswordEncoder#matches}, which runs one bcrypt verification
     * per history row. With a reuse count of 0 (the whole history), the cost grows with the size of the history. That
     * is accepted because passwords change rarely.
     * <p>
     * A {@code passwordEncoded} value (a password the client has already hashed) has no plain text, so it is compared
     * with {@code equals}. Stored hashes are salted, so such a value is practically never matched and reuse is not
     * detected on that path.
     */
    private AppUserPreviousPassword getCurrentPasswordToSaveAsPreview(final AppUser user, final JsonCommand command) {
        final String passWordEncodedValue = getEncodedPassword(user, command);

        AppUserPreviousPassword currentPasswordToSaveAsPreview = null;

        if (passWordEncodedValue != null) {
            final Integer passwordReuseRestrictionCount = this.configurationDomainService.getPasswordReuseRestrictionCount();
            if (passwordReuseRestrictionCount != null) {
                final String rawPassword = command.stringValueOfParameterNamedAllowingNull(AppUserConstants.PASSWORD);
                final Predicate<String> matchesSubmittedPassword = rawPassword != null
                        ? storedPassword -> this.platformPasswordEncoder.matches(rawPassword, storedPassword)
                        : passWordEncodedValue::equals;
                List<AppUserPreviousPassword> previousPasswords;
                if (passwordReuseRestrictionCount == 0) {
                    previousPasswords = this.appUserPreviewPasswordRepository.findByUserId(user.getId(),
                            PageRequest.of(0, Integer.MAX_VALUE, Sort.Direction.DESC, "removalDate"));
                } else {
                    PageRequest pageRequest = PageRequest.of(0, passwordReuseRestrictionCount, Sort.Direction.DESC, "removalDate");
                    previousPasswords = this.appUserPreviewPasswordRepository.findByUserId(user.getId(), pageRequest);
                }
                for (AppUserPreviousPassword aPreviewPassword : previousPasswords) {
                    if (matchesSubmittedPassword.test(aPreviewPassword.getPassword())) {
                        throw new PasswordPreviouslyUsedException();
                    }
                }
            }

            currentPasswordToSaveAsPreview = new AppUserPreviousPassword(user);
        }

        return currentPasswordToSaveAsPreview;
    }

    private static AppUser newAppUser(final Office userOffice, final Staff linkedStaff, final Set<Role> allRoles,
            final JsonCommand command) {
        final String username = command.stringValueOfParameterNamed("username");
        String password = command.stringValueOfParameterNamed("password");
        final Boolean sendPasswordToEmail = command.booleanObjectValueOfParameterNamed("sendPasswordToEmail");

        if (sendPasswordToEmail) {
            password = new RandomPasswordGenerator(13).generate();
        }

        boolean passwordNeverExpire = false;

        if (command.parameterExists(AppUserConstants.PASSWORD_NEVER_EXPIRES)) {
            passwordNeverExpire = command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.PASSWORD_NEVER_EXPIRES);
        }

        final boolean userEnabled = true;
        final boolean userAccountNonExpired = true;
        final boolean userCredentialsNonExpired = true;
        final boolean userAccountNonLocked = true;
        final boolean cannotChangePassword = false;
        boolean loginRetryLimitEnabled = false;
        if (command.parameterExists(AppUserConstants.IS_LOGIN_RETRIES_ENABLED)) {
            loginRetryLimitEnabled = command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_LOGIN_RETRIES_ENABLED);
        }

        final Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("DUMMY_ROLE_NOT_USED_OR_PERSISTED_TO_AVOID_EXCEPTION"));

        final User user = new User(username, password, userEnabled, userAccountNonExpired, userCredentialsNonExpired, userAccountNonLocked,
                authorities);

        final String email = command.stringValueOfParameterNamed("email");
        final String firstname = command.stringValueOfParameterNamed("firstname");
        final String lastname = command.stringValueOfParameterNamed("lastname");

        final AppUser appUser = new AppUser(userOffice, user, allRoles, email, firstname, lastname, linkedStaff, passwordNeverExpire,
                cannotChangePassword);
        // the entity never enables login retries for the system user
        appUser.updateLoginRetryLimitEnabled(loginRetryLimitEnabled);
        if (command.parameterExists(AppUserConstants.IS_PASSWORD_RESET_ALLOWED)) {
            appUser.updatePasswordResetAllowed(command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_PASSWORD_RESET_ALLOWED));
        }
        return appUser;
    }

    /**
     * Applies the changes of {@code PUT /users/{userId}}, in the order of the former {@code AppUser.update}. The
     * office, staff and roles are only reported here; {@link #updateUser} looks them up and applies them.
     */
    private Map<String, Object> applyChanges(final AppUser user, final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        // unencoded password provided
        applyPasswordChange(user, command, actualChanges);
        final String officeIdParamName = "officeId";
        if (command.isChangeInLongParameterNamed(officeIdParamName, user.getOffice().getId())) {
            final Long newValue = command.longValueOfParameterNamed(officeIdParamName);
            actualChanges.put(officeIdParamName, newValue);
        }

        final String staffIdParamName = "staffId";
        if (command.hasParameter(staffIdParamName)
                && (user.getStaff() == null || command.isChangeInLongParameterNamed(staffIdParamName, user.getStaff().getId()))) {
            final Long newValue = command.longValueOfParameterNamed(staffIdParamName);
            actualChanges.put(staffIdParamName, newValue);
        }

        final String rolesParamName = "roles";
        if (command.isChangeInArrayParameterNamed(rolesParamName, getRolesAsIdStringArray(user))) {
            final String[] newValue = command.arrayValueOfParameterNamed(rolesParamName);
            actualChanges.put(rolesParamName, newValue);
        }

        final String usernameParamName = "username";
        if (command.isChangeInStringParameterNamed(usernameParamName, user.getUsername())) {
            final String newValue = command.stringValueOfParameterNamed(usernameParamName);
            user.updateUsername(newValue);
            actualChanges.put(usernameParamName, newValue);
        }

        final String firstnameParamName = "firstname";
        if (command.isChangeInStringParameterNamed(firstnameParamName, user.getFirstname())) {
            final String newValue = command.stringValueOfParameterNamed(firstnameParamName);
            actualChanges.put(firstnameParamName, newValue);
            user.updateFirstname(newValue);
        }

        final String lastnameParamName = "lastname";
        if (command.isChangeInStringParameterNamed(lastnameParamName, user.getLastname())) {
            final String newValue = command.stringValueOfParameterNamed(lastnameParamName);
            actualChanges.put(lastnameParamName, newValue);
            user.updateLastname(newValue);
        }

        final String emailParamName = "email";
        if (command.isChangeInStringParameterNamed(emailParamName, user.getEmail())) {
            final String newValue = command.stringValueOfParameterNamed(emailParamName);
            actualChanges.put(emailParamName, newValue);
            user.updateEmail(newValue);
        }

        final String passwordNeverExpire = "passwordNeverExpires";

        if (command.hasParameter(passwordNeverExpire)
                && command.isChangeInBooleanParameterNamed(passwordNeverExpire, user.getPasswordNeverExpires())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(passwordNeverExpire);
            actualChanges.put(passwordNeverExpire, newValue);
            user.updatePasswordNeverExpires(newValue);
        }

        if (command.hasParameter(AppUserConstants.IS_LOGIN_RETRIES_ENABLED)) {
            final boolean previousValue = user.isLoginRetryLimitEnabled();
            final boolean requestedValue = command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_LOGIN_RETRIES_ENABLED);
            // only touch the entity on a change: disabling also resets the failed attempts and the lock
            if (requestedValue != previousValue) {
                // the entity never enables login retries for the system user
                user.updateLoginRetryLimitEnabled(requestedValue);
                if (user.isLoginRetryLimitEnabled() != previousValue) {
                    actualChanges.put(AppUserConstants.IS_LOGIN_RETRIES_ENABLED, user.isLoginRetryLimitEnabled());
                }
            }
        }

        if (command.hasParameter(AppUserConstants.IS_PASSWORD_RESET_ALLOWED)
                && command.isChangeInBooleanParameterNamed(AppUserConstants.IS_PASSWORD_RESET_ALLOWED, user.isPasswordResetAllowed())) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(AppUserConstants.IS_PASSWORD_RESET_ALLOWED);
            actualChanges.put(AppUserConstants.IS_PASSWORD_RESET_ALLOWED, newValue);
            user.updatePasswordResetAllowed(newValue);
        }
        return actualChanges;
    }

    private void applyPasswordChange(final AppUser user, final JsonCommand command, final Map<String, Object> actualChanges) {
        final String passwordParamName = AppUserConstants.PASSWORD;
        if (command.hasParameter(passwordParamName)
                && command.isChangeInPasswordParameterNamed(passwordParamName, user.getPassword(), this.platformPasswordEncoder)) {
            final String passwordEncodedValue = command.passwordValueOfParameterNamed(passwordParamName, this.platformPasswordEncoder,
                    user.getId());
            actualChanges.put(passwordParamName, true);
            user.updatePassword(passwordEncodedValue);
        }
    }

    private String getEncodedPassword(final AppUser user, final JsonCommand command) {
        final String passwordParamName = "password";
        final String passwordEncodedParamName = "passwordEncoded";
        String passwordEncodedValue = null;

        if (command.hasParameter(passwordParamName)) {
            if (command.isChangeInPasswordParameterNamed(passwordParamName, user.getPassword(), this.platformPasswordEncoder)) {

                passwordEncodedValue = command.passwordValueOfParameterNamed(passwordParamName, this.platformPasswordEncoder, user.getId());

            }
        } else if (command.hasParameter(passwordEncodedParamName)
                && command.isChangeInStringParameterNamed(passwordEncodedParamName, user.getPassword())) {

            passwordEncodedValue = command.stringValueOfParameterNamed(passwordEncodedParamName);

        }

        return passwordEncodedValue;
    }

    private static String[] getRolesAsIdStringArray(final AppUser user) {
        final List<String> roleIds = new ArrayList<>();

        for (final Role role : user.getRoles()) {
            roleIds.add(role.getId().toString());
        }

        return roleIds.toArray(new String[0]);
    }

    private Set<Role> assembleSetOfRoles(final String[] rolesArray) {
        final Set<Role> allRoles = new HashSet<>();

        if (!ObjectUtils.isEmpty(rolesArray)) {
            for (final String roleId : rolesArray) {
                final Long id = Long.valueOf(roleId);
                final Role role = this.roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
                allRoles.add(role);
            }
        }

        return allRoles;
    }

    @Override
    @Transactional
    @Caching(evict = { @CacheEvict(value = "users", allEntries = true), @CacheEvict(value = "usersByUsername", allEntries = true) })
    public CommandProcessingResult deleteUser(final Long userId) {
        final AppUser user = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isDeleted()) {
            throw new UserNotFoundException(userId);
        }

        user.delete();
        this.appUserRepository.save(user);

        return new CommandProcessingResultBuilder() //
                .withEntityId(userId) //
                .withOfficeId(user.getOffice().getId()) //
                .build();
    }

    /*
     * Return an exception to throw, no matter what the data integrity issue is.
     */
    private RuntimeException handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        // TODO: this needs to be fixed. The error condition should be independent from the underlying message and
        // naming of the constraint
        if (realCause.getMessage().contains("username_org")) {
            final String username = command.stringValueOfParameterNamed("username");
            final String defaultMessage = "User with username " + username + " already exists.";
            return new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessage, "username", username);
        }
        log.error("handleDataIntegrityIssues: Neither duplicate username nor existing user; unknown error occurred", dve);
        return ErrorHandler.getMappable(dve, "error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }
}
