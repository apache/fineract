package org.mifosplatform.portfolio.savingsaccount.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsaccount.service.SavingAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateSavingAccountCommandHandler  implements NewCommandSourceHandler {
	
	private final SavingAccountWritePlatformService savingAccountWritePlatformService;
	
	@Autowired
	public CreateSavingAccountCommandHandler(final SavingAccountWritePlatformService savingAccountWritePlatformService) {
		this.savingAccountWritePlatformService = savingAccountWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.savingAccountWritePlatformService.createSavingAccount(command);
	}

}
