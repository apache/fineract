package org.mifosng.platform.user.service;

import org.mifosng.platform.api.commands.UserCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AppUserWritePlatformService {

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'CREATE_USER')")
    Long createUser(UserCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'UPDATE_USER')")
    Long updateUser(UserCommand command);

    @PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'USER_ADMINISTRATION_SUPER_USER', 'DELETE_USER')")
    void deleteUser(UserCommand command);

    // we dont put any permissions on this e.g. a user with no admin rights can
    // change their own details (email, username, password etc).
    Long updateUsersOwnAccountDetails(UserCommand command);
}