package org.mifosng.platform.user.service;

import org.mifosng.platform.api.commands.UserCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface AppUserWritePlatformService {

	@PreAuthorize(value = "hasRole('USER_ADMINISTRATION_SUPER_USER_ROLE')")
	Long createUser(final UserCommand command);
	
	@PreAuthorize(value = "hasRole('USER_ADMINISTRATION_SUPER_USER_ROLE')")
	Long updateUser(final UserCommand command);
	
	@PreAuthorize(value = "hasRole('USER_ADMINISTRATION_SUPER_USER_ROLE')")
	void deleteUser(final Long userId);
}