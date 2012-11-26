package org.mifosng.platform.user.domain;

public interface UserDomainService {

    void create(AppUser appUser, boolean isApprovedByChecker);
}
