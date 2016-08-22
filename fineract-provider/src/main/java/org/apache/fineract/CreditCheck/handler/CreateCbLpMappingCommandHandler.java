package org.apache.fineract.CreditCheck.handler;

import org.apache.fineract.CreditCheck.service.CreditBureauLpMappingWritePlatformService;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity="CB_LP_Mapping",action="CREATE")
public class CreateCbLpMappingCommandHandler implements NewCommandSourceHandler {
    
    private final CreditBureauLpMappingWritePlatformService writePlatformService;
    
    @Autowired
    public CreateCbLpMappingCommandHandler(final CreditBureauLpMappingWritePlatformService writePlatformService)
    {
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        
        return this.writePlatformService.addCbLpMapping(command.getCbId(),command);
    }
    

}
