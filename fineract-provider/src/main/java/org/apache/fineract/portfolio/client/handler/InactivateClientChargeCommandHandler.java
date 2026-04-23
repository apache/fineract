package org.apache.fineract.portfolio.client.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.service.ClientChargeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = ClientApiConstants.CLIENT_CHARGES_RESOURCE_NAME,
        action = ClientApiConstants.CLIENT_CHARGE_ACTION_INACTIVATE)
public class InactivateClientChargeCommandHandler implements NewCommandSourceHandler {

    private final ClientChargeWritePlatformService writePlatformService;

    @Autowired
    public InactivateClientChargeCommandHandler(final ClientChargeWritePlatformService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.inactivateCharge(
                command.getClientId(),
                command.entityId(),
                command
        );
    }
}
