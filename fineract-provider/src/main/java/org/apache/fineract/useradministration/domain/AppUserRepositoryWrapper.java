/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.mifosplatform.useradministration.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserRepositoryWrapper {

    private final AppUserRepository appUserRepository ;
    
    @Autowired
    public AppUserRepositoryWrapper(final AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository ;
    }
    
    public AppUser fetchSystemUser() {
        String userName = "system" ;
        AppUser user = this.appUserRepository.findAppUserByName(userName);
        if(user == null) {
            throw new UserNotFoundException(userName) ;
        }
        return user ;
    }
}
