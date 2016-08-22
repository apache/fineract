package org.apache.fineract.CreditCheck.handler;

import org.apache.fineract.CreditCheck.service.OrganisationCreditBureauWritePlatflormService;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@CommandType(entity="OrgCreditBureau",action="CREATE")
public class AddOrgCBCommandHandler implements NewCommandSourceHandler
{

private final OrganisationCreditBureauWritePlatflormService writePlatformService;
    
    @Autowired
    public AddOrgCBCommandHandler(final OrganisationCreditBureauWritePlatflormService writePlatformService)
    {
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        
        return this.writePlatformService.addOrgCreditBureau(command.getOcbId(),command);
    }
}
