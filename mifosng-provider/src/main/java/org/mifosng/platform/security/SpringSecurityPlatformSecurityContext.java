package org.mifosng.platform.security;

import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosplatform.infrastructure.user.domain.AppUser;
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
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            Authentication auth = context.getAuthentication();
            if (auth != null) {
                currentUser = (AppUser) auth.getPrincipal();
            }
        }

        if (currentUser == null) { throw new UnAuthenticatedUserException(); }

        return currentUser;
    }
}
