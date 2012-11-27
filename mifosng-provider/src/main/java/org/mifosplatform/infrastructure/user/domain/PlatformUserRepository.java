package org.mifosplatform.infrastructure.user.domain;

import org.mifosng.platform.infrastructure.PlatformUser;

public interface PlatformUserRepository {

    PlatformUser findByUsername(String username);

}