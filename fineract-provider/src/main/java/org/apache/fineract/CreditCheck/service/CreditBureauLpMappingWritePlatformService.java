package org.apache.fineract.CreditCheck.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface CreditBureauLpMappingWritePlatformService{
    
    CommandProcessingResult addCbLpMapping(Long cb_id,JsonCommand command);
    
    CommandProcessingResult updateCreditBureauLoanProductMapping(JsonCommand command);
    
    

}
