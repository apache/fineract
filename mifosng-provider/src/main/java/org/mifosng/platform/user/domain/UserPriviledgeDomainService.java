package org.mifosng.platform.user.domain;

import org.mifosng.platform.organisation.domain.Organisation;


public interface UserPriviledgeDomainService {

    void createAllOrganisationRolesAndPermissions(Organisation organisation);

}
