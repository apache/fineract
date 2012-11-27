package org.mifosplatform.infrastructure.user.domain;

public interface UserDomainService {

    void create(AppUser appUser, boolean isApprovedByChecker);
}
