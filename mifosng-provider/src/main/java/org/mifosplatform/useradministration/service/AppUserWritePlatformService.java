package org.mifosplatform.useradministration.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface AppUserWritePlatformService {

    CommandProcessingResult createUser(JsonCommand command);

    CommandProcessingResult updateUser(Long userId, JsonCommand command);

    CommandProcessingResult deleteUser(Long userId);

    // we dont put any permissions on this e.g. a user with no admin rights can
    // change their own details (email, username, password etc).
    CommandProcessingResult updateUsersOwnAccountDetails(Long userId, JsonCommand command);
}