package org.mifosplatform.portfolio.charge.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.portfolio.charge.service.ChargeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateChargeDefinitionCommandHandler implements NewCommandSourceHandler {

    private final ChargeWritePlatformService clientWritePlatformService;

    @Autowired
    public UpdateChargeDefinitionCommandHandler(final ChargeWritePlatformService clientWritePlatformService) {
        this.clientWritePlatformService = clientWritePlatformService;
    }

    @Transactional
    @Override
    public EntityIdentifier processCommand(final JsonCommand command) {

        return this.clientWritePlatformService.updateCharge(command.resourceId(), command);
    }
}