package org.mifosplatform.useradministration.service;

import java.util.Collection;

import org.mifosplatform.useradministration.data.AppUserData;

public interface AppUserReadPlatformService {

    Collection<AppUserData> retrieveAllUsers();

    AppUserData retrieveNewUserDetails();

    AppUserData retrieveUser(Long userId);
}