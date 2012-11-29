package org.mifosplatform.infrastructure.security.domain;


public interface PlatformUserRepository {

    PlatformUser findByUsername(String username);

}