package org.mifosplatform.portfolio.savingsdepositproduct.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductLookup;

public interface DepositProductReadPlatformService {

    Collection<DepositProductData> retrieveAllDepositProducts();

    Collection<DepositProductLookup> retrieveAllDepositProductsForLookup();

    DepositProductData retrieveDepositProductData(Long productId);

    DepositProductData retrieveNewDepositProductDetails();

}
