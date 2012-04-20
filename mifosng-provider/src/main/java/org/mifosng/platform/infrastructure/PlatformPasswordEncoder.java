package org.mifosng.platform.infrastructure;


public interface PlatformPasswordEncoder {

    String encode(PlatformUser appUser);

}
