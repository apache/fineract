package org.mifosplatform.useradministration.service;

import java.util.Collection;

import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.data.AppUserLookup;

public interface AppUserReadPlatformService {

    Collection<AppUserData> retrieveAllUsers();
    
    Collection<AppUserLookup> retrieveSearchTemplate();

    AppUserData retrieveNewUserDetails();

    AppUserData retrieveUser(Long userId);
}