package org.mifosng.platform.user.service;

import org.mifosng.platform.api.commands.UserCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AppUserWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'CREATE_USER')")
    Long createUser(final UserCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'UPDATE_USER')")
    Long updateUser(final UserCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'DELETE_USER')")
    void deleteUser(final Long userId);

    // we dont put any permissions on this e.g. a user with no admin rights can
    // change their own details (email, username, password etc).
    void updateUsersOwnAccountDetails(UserCommand command);
}