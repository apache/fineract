package org.mifosplatform.infrastructure.security.service;

import org.mifosplatform.infrastructure.security.domain.PlatformUser;

public interface PlatformPasswordEncoder {

    String encode(PlatformUser appUser);
}