package org.mifosng.platform.organisation.service;

import org.mifosng.platform.user.domain.AppUser;

public interface PlatformSecurityContext {

	AppUser authenticatedUser();

}
