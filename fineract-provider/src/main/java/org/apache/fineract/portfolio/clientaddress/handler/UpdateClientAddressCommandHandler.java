package org.apache.fineract.portfolio.clientaddress.handler;

import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.clientaddress.service.AddressWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity="ADDRESS",action="UPDATE")
public class UpdateClientAddressCommandHandler implements NewCommandSourceHandler
{

 private final AddressWritePlatformService writePlatformService;
    
    @Autowired
    public UpdateClientAddressCommandHandler(final AddressWritePlatformService writePlatformService)
    {
        this.writePlatformService=writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
       return this.writePlatformService.updateClientAddress(command.getClientId(),command.entityId(),command.getStatus(), command);
        
    }
    
}
