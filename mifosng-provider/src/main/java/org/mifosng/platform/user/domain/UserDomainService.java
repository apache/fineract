package org.mifosng.platform.user.domain;

import org.mifosng.platform.organisation.domain.Office;

public interface UserDomainService {

	void createDefaultAdminUser(Office office);

	void create(AppUser appUser);
}
