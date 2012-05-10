package org.mifosng.platform.security;

import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Responsible for returning the current authenticated user that is currently set in the {@link SecurityContext}.
 */
@Service
public class PlatformSecurityContextThroughSpringSecurity implements PlatformSecurityContext {

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

		if (currentUser == null) {
			throw new UnAuthenticatedUserException();
		}

		return currentUser;
	}
}
