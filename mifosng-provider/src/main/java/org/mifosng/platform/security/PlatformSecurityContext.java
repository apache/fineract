package org.mifosng.platform.security;

import org.mifosplatform.infrastructure.user.domain.AppUser;

public interface PlatformSecurityContext {

    AppUser authenticatedUser();
}