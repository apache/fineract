package org.mifosplatform.portfolio.savingsaccountproduct.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateSavingProductCommandHandler implements NewCommandSourceHandler {
	
	private final SavingProductWritePlatformService savingProductWritePlatformService;
	
	@Autowired
	public UpdateSavingProductCommandHandler(final SavingProductWritePlatformService savingProductWritePlatformService) {
		this.savingProductWritePlatformService = savingProductWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.savingProductWritePlatformService.updateSavingProduct(command.entityId(), command);
	}

}
