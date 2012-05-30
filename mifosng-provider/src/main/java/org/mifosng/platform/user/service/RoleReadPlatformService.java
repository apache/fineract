package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.platform.api.data.RoleData;

public interface RoleReadPlatformService {

	Collection<RoleData> retrieveAllRoles();

	RoleData retrieveRole(Long roleId);
}