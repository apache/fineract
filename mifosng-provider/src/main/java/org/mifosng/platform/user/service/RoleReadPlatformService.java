package org.mifosng.platform.user.service;

import java.util.Collection;

import org.mifosng.data.RoleData;

public interface RoleReadPlatformService {

	Collection<RoleData> retrieveAllRoles();

	RoleData retrieveRole(Long roleId);
}