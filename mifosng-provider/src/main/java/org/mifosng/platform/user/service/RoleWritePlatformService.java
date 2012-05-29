package org.mifosng.platform.user.service;

import org.mifosng.platform.api.commands.RoleCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface RoleWritePlatformService {

	@PreAuthorize(value = "hasRole('USER_ADMINISTRATION_SUPER_USER_ROLE')")
	Long createRole(RoleCommand command);
	
	@PreAuthorize(value = "hasRole('USER_ADMINISTRATION_SUPER_USER_ROLE')")
	Long updateRole(RoleCommand command);
}