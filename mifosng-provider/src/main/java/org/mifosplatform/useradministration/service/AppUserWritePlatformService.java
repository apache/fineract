package org.mifosplatform.useradministration.service;

import org.mifosplatform.useradministration.command.UserCommand;

public interface AppUserWritePlatformService {

    Long createUser(UserCommand command);

    Long updateUser(UserCommand command);

    void deleteUser(UserCommand command);

    // we dont put any permissions on this e.g. a user with no admin rights can
    // change their own details (email, username, password etc).
    Long updateUsersOwnAccountDetails(UserCommand command);
}