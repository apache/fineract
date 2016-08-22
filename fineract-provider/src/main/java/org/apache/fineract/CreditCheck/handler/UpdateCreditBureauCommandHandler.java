package org.apache.fineract.CreditCheck.handler;

import org.apache.fineract.CreditCheck.service.OrganisationCreditBureauWritePlatflormService;
import org.apache.fineract.commands.annotation.CommandType;
import org.apache.fineract.commands.handler.NewCommandSourceHandler;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CommandType(entity = "OrgCreditBureau", action = "UPDATE")
public class UpdateCreditBureauCommandHandler implements NewCommandSourceHandler
{
    
 private final OrganisationCreditBureauWritePlatflormService writePlatformService;
    
    @Autowired
    public UpdateCreditBureauCommandHandler(final OrganisationCreditBureauWritePlatflormService writePlatformService) {
        this.writePlatformService = writePlatformService;
    }
    
   
    
    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
       
        return this.writePlatformService.updateCreditBureau(command);
    }

}
