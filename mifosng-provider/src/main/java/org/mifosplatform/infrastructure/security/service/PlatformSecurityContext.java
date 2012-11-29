package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.infrastructure.user.domain.AppUser;

public interface PlatformSecurityContext {

    AppUser authenticatedUser();
}