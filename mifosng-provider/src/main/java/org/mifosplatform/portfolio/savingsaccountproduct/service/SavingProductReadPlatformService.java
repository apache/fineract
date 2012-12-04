package org.mifosplatform.portfolio.savingsaccountproduct.service;

import java.util.Collection;

import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductLookup;

public interface SavingProductReadPlatformService {

    Collection<SavingProductData> retrieveAllSavingProducts();

    Collection<SavingProductLookup> retrieveAllSavingProductsForLookup();

    SavingProductData retrieveSavingProduct(Long productId);

    SavingProductData retrieveNewSavingProductDetails();
}
