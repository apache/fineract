package org.mifosng.platform.security;

import org.mifosng.platform.user.domain.AppUser;

public interface PlatformSecurityContext {

	AppUser authenticatedUser();

}
