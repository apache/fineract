package org.mifosplatform.portfolio.account.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.portfolio.account.service.StandingInstructionWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteStandingInstructionCommandHandler implements NewCommandSourceHandler {

    private StandingInstructionWritePlatformService standingInstructionWritePlatformService;

    @Autowired
    public DeleteStandingInstructionCommandHandler(StandingInstructionWritePlatformService standingInstructionWritePlatformService) {
        this.standingInstructionWritePlatformService = standingInstructionWritePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        return this.standingInstructionWritePlatformService.delete(command.entityId());
    }

}
