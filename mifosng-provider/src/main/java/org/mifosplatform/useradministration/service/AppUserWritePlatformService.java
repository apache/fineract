package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface AppUserWritePlatformService {

    Long createUser(JsonCommand command);

    EntityIdentifier updateUser(Long userId, JsonCommand command);

    void deleteUser(Long userId, JsonCommand command);

    // we dont put any permissions on this e.g. a user with no admin rights can
    // change their own details (email, username, password etc).
    EntityIdentifier updateUsersOwnAccountDetails(Long userId, JsonCommand command);
}