/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.infrastructure.security.domain.PlatformUser;
import org.mifosplatform.infrastructure.security.domain.PlatformUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Used in securityContext.xml as implementation of spring security's
 * {@link UserDetailsService}.
 */
@Service(value = "userDetailsService")
public class TenantAwareJpaPlatformUserDetailsService implements PlatformUserDetailsService {

    @Autowired
    private PlatformUserRepository platformUserRepository;

    @Override
    @Cacheable(value = "usersByUsername", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#username+'ubu')")
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {

        // Retrieve active users only
        final boolean deleted = false;
        final boolean enabled = true;

        final PlatformUser appUser = this.platformUserRepository.findByUsernameAndDeletedAndEnabled(username, deleted, enabled);

        if (appUser == null) { throw new UsernameNotFoundException(username + ": not found"); }

        return appUser;
    }
}