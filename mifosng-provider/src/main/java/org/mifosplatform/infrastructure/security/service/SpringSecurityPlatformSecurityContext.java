/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.exception.UnAuthenticatedUserException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Wrapper around spring security's {@link SecurityContext} for extracted the
 * current authenticated {@link AppUser}.
 */
@Service
public class SpringSecurityPlatformSecurityContext implements PlatformSecurityContext {

    @Override
    public AppUser authenticatedUser() {
        AppUser currentUser = null;
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            final Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
        }

        if (currentUser == null) { throw new UnAuthenticatedUserException(); }

        return currentUser;
    }

    @Override
    public void validateAccessRights(final String resourceOfficeHierarchy) {

        final AppUser user = authenticatedUser();
        final String userOfficeHierarchy = user.getOffice().getHierarchy();

        if (!resourceOfficeHierarchy.startsWith(userOfficeHierarchy)) { throw new NoAuthorizationException(
                "The user doesn't have enough permissions to access the resource."); }

    }

    @Override
    public String officeHierarchy() {
        return authenticatedUser().getOffice().getHierarchy();
    }
}
