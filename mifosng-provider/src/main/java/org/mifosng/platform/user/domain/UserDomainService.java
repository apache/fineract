package org.mifosng.platform.user.domain;

import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.Organisation;


public interface UserDomainService {

    void createDefaultAdminUser(Organisation organisation, Office office);

    void create(AppUser appUser);
}
