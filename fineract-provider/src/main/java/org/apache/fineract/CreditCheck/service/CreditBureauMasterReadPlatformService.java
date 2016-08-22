package org.apache.fineract.CreditCheck.service;

import java.util.Collection;

import org.apache.fineract.CreditCheck.data.CreditBureauMasterData;

public interface CreditBureauMasterReadPlatformService {
    
    Collection<CreditBureauMasterData> retrieveCreditBureauByCountry(String country);
    
    Collection<CreditBureauMasterData> retrieveCreditBureauByCountry();

}
