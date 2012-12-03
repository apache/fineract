package org.mifosplatform.useradministration.service;

import java.util.Collection;

import org.mifosplatform.useradministration.data.RoleData;

public interface RoleReadPlatformService {

    Collection<RoleData> retrieveAllRoles();

    RoleData retrieveRole(Long roleId);
}