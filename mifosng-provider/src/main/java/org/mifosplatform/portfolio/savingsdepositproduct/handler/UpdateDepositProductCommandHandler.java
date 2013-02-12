package org.mifosplatform.portfolio.savingsdepositproduct.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDepositProductCommandHandler implements NewCommandSourceHandler {
	
private final DepositProductWritePlatformService depositProductWritePlatformService;
	
	@Autowired 
	public UpdateDepositProductCommandHandler(final DepositProductWritePlatformService depositProductWritePlatformService) {
		this.depositProductWritePlatformService = depositProductWritePlatformService;
	}

	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		return this.depositProductWritePlatformService.updateDepositProduct(command.entityId(),command);
	}

}
