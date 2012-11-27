package org.mifosplatform.infrastructure.user.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.user.api.data.RoleData;

public interface RoleReadPlatformService {

    Collection<RoleData> retrieveAllRoles();

    RoleData retrieveRole(Long roleId);
}