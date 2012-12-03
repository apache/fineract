package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.useradministration.domain.AppUser;

public interface PlatformSecurityContext {

    AppUser authenticatedUser();
}