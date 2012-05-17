package org.mifosng.platform;

import org.mifosng.data.command.UpdateUsernamePasswordCommand;

public interface WritePlatformService {

	void updateUsernamePasswordOnFirstTimeLogin(UpdateUsernamePasswordCommand command);
}