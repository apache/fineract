package org.mifosng.platform.user.service;

import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.UserCommand;

public interface AppUserWritePlatformService {

	Long createUser(final UserCommand command);
	
	Long updateUser(final UserCommand command);
	
	void deleteUser(final Long userId);
	
	Long updateCurrentUser(UserCommand command);
	
	Long updateCurrentUserPassword(ChangePasswordCommand command);
}