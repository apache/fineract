package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.platform.api.data.AppUserData;

public interface AppUserReadPlatformService {

	Collection<AppUserData> retrieveAllUsers();

	AppUserData retrieveNewUserDetails();

	AppUserData retrieveUser(Long userId);
}