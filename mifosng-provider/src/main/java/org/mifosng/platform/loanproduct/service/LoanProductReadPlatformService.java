package org.mifosng.platform.loanproduct.service;

import java.util.Collection;

import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.LoanProductLookup;

public interface LoanProductReadPlatformService {

	Collection<LoanProductData> retrieveAllLoanProducts();

	Collection<LoanProductLookup> retrieveAllLoanProductsForLookup();

	LoanProductData retrieveLoanProduct(Long productId);

	LoanProductData retrieveNewLoanProductDetails();
}