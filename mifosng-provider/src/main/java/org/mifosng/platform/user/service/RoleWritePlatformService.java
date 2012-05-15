package org.mifosng.platform.user.service;

import org.mifosng.data.command.RoleCommand;

public interface RoleWritePlatformService {

	Long createRole(RoleCommand command);
	
	Long updateRole(RoleCommand command);
}