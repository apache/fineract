package org.mifosng.platform.infrastructure;

import org.mifosplatform.infrastructure.security.domain.PlatformUser;


public interface PlatformPasswordEncoder {

    String encode(PlatformUser appUser);

}
