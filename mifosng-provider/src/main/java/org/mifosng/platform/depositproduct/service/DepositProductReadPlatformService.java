package org.mifosng.platform.depositproduct.service;

import java.util.Collection;

import org.mifosng.platform.api.data.DepositProductData;
import org.mifosng.platform.api.data.DepositProductLookup;

public interface DepositProductReadPlatformService {
	
	Collection<DepositProductData> retrieveAllDepositProducts();
	
	Collection<DepositProductLookup> retrieveAllDepositProductsForLookup();
	
	DepositProductData retrieveDepositProductData(Long productId);
	
	DepositProductData retrieveNewDepositProductDetails();

}
