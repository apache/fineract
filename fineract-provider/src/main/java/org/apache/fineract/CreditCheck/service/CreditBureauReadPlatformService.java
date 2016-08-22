package org.apache.fineract.CreditCheck.service;

import java.util.Collection;

import org.apache.fineract.CreditCheck.data.CreditBureauData;

public interface CreditBureauReadPlatformService {
    
    Collection<CreditBureauData> retrieveCreditBureau();
    
    //CreditBureauData findCreditBureau(final Long cbid);
    
   

}
