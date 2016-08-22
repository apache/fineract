package org.apache.fineract.CreditCheck.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;

public interface CreditBureauWritePlatformService {
    
    CommandProcessingResult addCreditBureau(JsonCommand command);
    
    CommandProcessingResult updateCreditBureau(JsonCommand command);

}
