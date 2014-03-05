package org.mifosplatform.portfolio.client.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateClientSavingsAccountCommandHandler implements NewCommandSourceHandler {
    
    private final ClientWritePlatformService clientWritePlatformService;

    @Autowired
    public UpdateClientSavingsAccountCommandHandler(final ClientWritePlatformService clientWritePlatformService){
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Override
    @Transactional
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.clientWritePlatformService.updateDefaultSavingsAccount(command.entityId(), command);
    }

}
