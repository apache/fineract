package org.mifosplatform.infrastructure.user.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.user.api.data.AppUserData;

public interface AppUserReadPlatformService {

    Collection<AppUserData> retrieveAllUsers();

    AppUserData retrieveNewUserDetails();

    AppUserData retrieveUser(Long userId);
}