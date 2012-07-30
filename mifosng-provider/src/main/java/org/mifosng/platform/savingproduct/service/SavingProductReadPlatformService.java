package org.mifosng.platform.savingproduct.service;

import java.util.Collection;

import org.mifosng.platform.api.data.SavingProductData;
import org.mifosng.platform.api.data.SavingProductLookup;

public interface SavingProductReadPlatformService {

	Collection<SavingProductData> retrieveAllSavingProducts();

	Collection<SavingProductLookup> retrieveAllSavingProductsForLookup();

	SavingProductData retrieveSavingProduct(Long productId);

	SavingProductData retrieveNewSavingProductDetails();	
}
