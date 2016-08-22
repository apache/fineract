package org.apache.fineract.CreditCheck.service;

import java.util.Collection;

import org.apache.fineract.CreditCheck.data.CreditBureauLpMappingData;
import org.apache.fineract.CreditCheck.data.CreditBureauProduct;

public interface CreditBureauLpMappingReadPlatformService {

    Collection<CreditBureauLpMappingData> readCreditBureauLpMapping();
    
   Collection<CreditBureauLpMappingData> fetchLoanProducts();
    
    //CreditBureauLpMappingData findCreditLpMapping(final Long cbid);
   
  // Collection<CreditBureauLpMappingData> findCreditLpMapping(final Long cbid);
    
    //Collection<CreditBureauProduct> findCreditBureauProductByCreditBureau(final Long lpId);
}
