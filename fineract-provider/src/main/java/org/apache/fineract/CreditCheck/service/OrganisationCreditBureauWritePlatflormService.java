package org.apache.fineract.CreditCheck.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface OrganisationCreditBureauWritePlatflormService 
{

    CommandProcessingResult addOrgCreditBureau(Long ocb_id,JsonCommand command);
    
    CommandProcessingResult updateCreditBureau(JsonCommand command);
}
